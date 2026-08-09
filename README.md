# speed-violation-service

Microserviço de apuração de infrações por excesso de velocidade, 
desenvolvido para a prova prática de Desenvolvedor Backend Java da Velsis.

Recebe leituras de equipamentos de fiscalização, aplica a margem de tolerância legal, 
calcula o percentual de excesso sobre a velocidade regulamentada e classifica a infração conforme o Art. 218 do CTB.

## Stack

- Java 21
- Spring Boot 3.5 (Web, Actuator, Configuration Processor)
- Lombok
- springdoc-openapi (Swagger UI)
- JUnit 5 + AssertJ + Spring Test (MockMvc)
- JaCoCo (cobertura de testes, gate mínimo de 80% na camada de regras de negócio)
- Maven
- Docker
- GitHub Actions + AWS (ECR, EC2 via SSM) para o pipeline de deploy

## Arquitetura e decisões técnicas

O serviço tem um único domínio (apuração de infrações) e um único fluxo de negócio ponta a ponta, 
então optei por uma organização em camadas simples ao invés de introduzir módulos, 
ports/adapters ou qualquer forma de arquitetura hexagonal:

```
controller/    → exposição HTTP
validation/    → validação de entrada e das regras de formato de placa
service/       → regras de negócio (apuração) e orquestração (persistência)
repository/    → armazenamento em memória
model/         → entidade de domínio (Violation)
dto/           → contratos de entrada e saída
enums/         → OriginType, ViolationSeverity
exception/     → exceção de domínio + handler centralizado
config/        → propriedades externalizadas
```

A separação entre `ViolationEvaluationService` (cálculo puro: tolerância, percentual, gravidade) 
e `ViolationService` (orquestração: chama a apuração, decide o que persistir, monta a resposta) foi proposital. 
A lógica de apuração é a parte que mais precisa de testes de fronteira e não deveria carregar nenhuma 
dependência de persistência ou de infraestrutura, isso mantém os testes de regra de negócio rápidos e sem mocks.

A validação de entrada (`EvaluationRequestValidator`) roda antes de qualquer chamada ao service e centraliza 
todos os erros 400 do RF2, incluindo o header `x-origin`. Preferi concentrar essa validação em uma classe dedicada, 
com um record de saída (`ValidatedRequest`) que já carrega os dados no formato 
que o resto da aplicação espera (placa normalizada, timestamp já parseado, origem já como enum). 
Isso evita que o controller e o service fiquem espalhando `if`s de validação e garante que, a partir da validação, 
o restante do fluxo trabalha só com dados já confiáveis.

A validação de placa fica isolada em `LicensePlateValidator`, com os dois padrões (formato antigo e Mercosul) 
como constantes estáticas. Reutilizável fora do fluxo de validação da requisição, 
caso outro ponto da aplicação precise validar uma placa no futuro.

Para o armazenamento, usei `ConcurrentHashMap<String, List<Violation>>` com `CopyOnWriteArrayList` por placa. 
O padrão de acesso esperado aqui é bem mais leitura do que escrita, pois nem toda leitura vira infração, 
e só infrações são persistidas, o que faz esse par ser uma escolha razoável: leituras concorrentes sem lock 
e escritas (menos frequentes) sem perder registros.

O tratamento de erros é centralizado em `GlobalExceptionHandler`, que nunca expõe stack trace ao cliente 
e diferencia nível de log entre erro de validação (`warn`) e erro inesperado. Os identificadores de erro (`INVALID_LICENSE_PLATE`, `INVALID_ORIGIN`, etc.) 
são propagados desde a validação até a resposta HTTP, então o cliente da API recebe um código estável 
para tratar programaticamente, não só uma mensagem textual.

## Pré-requisitos

- JDK 21
- Maven 3.9+ (ou use o `./mvnw` incluído no projeto)
- Docker, caso prefira rodar via container

## Como executar

### Localmente com Maven

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080/api`.

### Com Docker

```bash
docker build -t speed-violation-service .
docker run -p 8080:8080 speed-violation-service
```

### Configuração

Todos os parâmetros abaixo são externalizáveis via `application.properties` ou variável de ambiente:

| Propriedade | Variável de ambiente | Padrão |
|---|---|---|
| `server.port` | `SERVER_PORT` | `8080` |
| `velsis.violation.tolerance-margin-kmh` | `TOLERANCE_MARGIN_KMH` | `7` |
| `velsis.violation.tolerance-margin-percentage` | `TOLERANCE_MARGIN_PERCENTAGE` | `7` |
| `velsis.violation.percentage-threshold` | `PERCENTAGE_THRESHOLD` | `100` |

O context path é fixo em `/api`.

## Exemplos de uso (curl)

### Infração detectada

```bash
curl -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{
    "licensePlate": "ABC1D23",
    "measuredSpeed": 92,
    "speedLimit": 60,
    "equipmentId": "RAD-CWB-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }'
```

```json
{
  "licensePlate": "ABC1D23",
  "equipmentId": "RAD-CWB-001",
  "measuredSpeed": 92,
  "consideredSpeed": 85,
  "speedLimit": 60,
  "excessPercentage": 41.67,
  "hasViolation": true,
  "violation": { "severity": "SERIOUS", "ctbCode": "218-II" },
  "processedAt": "2026-06-08T14:30:05Z"
}
```

### Sem infração (dentro da tolerância)

```bash
curl -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{
    "licensePlate": "ABC1D23",
    "measuredSpeed": 64,
    "speedLimit": 60,
    "equipmentId": "RAD-CWB-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }'
```

```json
{
  "licensePlate": "ABC1D23",
  "equipmentId": "RAD-CWB-001",
  "measuredSpeed": 64,
  "consideredSpeed": 57,
  "speedLimit": 60,
  "excessPercentage": 0.0,
  "hasViolation": false,
  "violation": null,
  "processedAt": "2026-06-08T14:30:05Z"
}
```

### Erro de validação (placa inválida)

```bash
curl -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -H "x-origin: FIXED" \
  -d '{
    "licensePlate": "XYZ",
    "measuredSpeed": 92,
    "speedLimit": 60,
    "equipmentId": "RAD-CWB-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }'
```

```json
{
  "error": "INVALID_LICENSE_PLATE",
  "message": "Invalid license plate format",
  "timestamp": "2026-06-08T14:30:05Z"
}
```

### Erro de validação (header ausente)

```bash
curl -X POST http://localhost:8080/api/v1/violations/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "licensePlate": "ABC1D23",
    "measuredSpeed": 92,
    "speedLimit": 60,
    "equipmentId": "RAD-CWB-001",
    "captureTimestamp": "2026-06-08T14:30:00Z"
  }'
```

```json
{
  "error": "INVALID_ORIGIN",
  "message": "x-origin header is required",
  "timestamp": "2026-06-08T14:30:05Z"
}
```

### Consulta por placa

```bash
curl "http://localhost:8080/api/v1/violations?licensePlate=ABC1D23"
```

```json
[
  {
    "licensePlate": "ABC1D23",
    "equipmentId": "RAD-CWB-001",
    "measuredSpeed": 92,
    "consideredSpeed": 85,
    "speedLimit": 60,
    "excessPercentage": 41.67,
    "severity": "SERIOUS",
    "ctbCode": "218-II",
    "origin": "FIXED",
    "captureTimestamp": "2026-06-08T14:30:00Z",
    "processedAt": "2026-06-08T14:30:05Z"
  }
]
```

### Documentação interativa

Com a aplicação no ar, o Swagger UI fica disponível em `http://localhost:8080/api/docs`.

## Testes

```bash
./mvnw test
```

O relatório de cobertura do JaCoCo é gerado em `target/site/jacoco/index.html`. 
O build falha (fase `verify`) se a camada `service` ficar abaixo de 80% de cobertura de linha:

```bash
./mvnw verify
```

A suíte cobre:
- Cálculo de velocidade considerada (tolerância absoluta e percentual, incluindo o limiar de 100 km/h avaliado sobre o limite regulamentado, não sobre a velocidade medida).
- Percentual de excesso e classificação de gravidade, com casos de fronteira exatos em 20% e 50%.
- Validação de placa nos dois formatos e rejeição de formatos inválidos, incluindo trimming de espaços.
- Validação de velocidade, limite, equipamento, timestamp (formato inválido e no futuro) e header `x-origin` (ausente, case errado, valor desconhecido).
- Persistência: infrações são gravadas só quando há violação, e o repositório suporta escrita concorrente sem perda de registros (teste com 50 threads na mesma placa).
- Integração ponta a ponta via `MockMvc`, cobrindo os dois status de sucesso e os principais cenários de erro.

## Da prova para produção

O escopo pedido é um microserviço de demonstração, e as decisões acima refletem isso. Documentando o que mudaria se este serviço fosse para produção de fato:

- **Persistência**: hoje as infrações vivem em memória e somem a cada restart do processo. Para um serviço 
real de fiscalização isso não é aceitável, portanto o próximo passo seria um banco relacional (o domínio é pequeno 
e bem definido, então nem precisaria de nada além de Postgres) com a mesma separação repository/service que já existe hoje, trocando só a implementação.

- **Autenticação e autorização**: nenhum endpoint exige autenticação. Um serviço que recebe leituras de equipamentos de fiscalização e expõe 
dados de infração por placa deveria, no mínimo, autenticar o equipamento que envia a leitura (mTLS ou API key por equipamento) 
e proteger a consulta por placa contra acesso indevido.

- **Rede**: a aplicação está publicada expondo a porta 8080 diretamente. Em produção o tráfego deveria passar  
por um load balancer (ALB, já que o pipeline já é AWS) com TLS terminado ali, e a instância 
não deveria ser publicamente alcançável na porta da aplicação.

- **Observabilidade**: os logs atuais diferenciam erro de validação de erro inesperado, mas são só texto. 
Para operar isso com múltiplas instâncias, o caminho natural é log estruturado (JSON) com correlação de request, 
mais métricas de negócio (taxa de infração por severidade, por origem) além do que o Actuator já providencia.

- **Escala horizontal**: como o estado vive em memória no processo, rodar mais de uma instância hoje faria cada réplica enxergar 
só uma fração das infrações. Isso é resolvido naturalmente ao migrar a persistência para um banco compartilhado.

## Suposições assumidas

- O header `x-origin` é validado como case-sensitive, conforme especificado no enunciado (`fixed` é rejeitado, `FIXED` é aceito).

- `captureTimestamp` é aceito estritamente no formato `Instant` ISO-8601 (ex.: `2026-06-08T14:30:00Z`). 
Formatos de data sem componente de hora/timezone são tratados como inválidos.

- A placa é normalizada para maiúsculas e sem espaços nas bordas antes de ser usada como chave de persistência e de consulta, 
para que `abc1d23` e `ABC1D23` sejam tratados como a mesma placa.

- Quando `consideredSpeed` é igual à `speedLimit`, não há infração. A regra do RF3 (`<=`) foi aplicada de forma literal.

- O endpoint de consulta por placa não pagina resultados; para o volume esperado de um serviço de demonstração 
isso não é um problema, mas seria o primeiro ajuste necessário junto com a migração para banco de dados.