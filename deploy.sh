IMAGE_TAG="$1"
ACCOUNT_ID="355103369647"
REPO="speed-violation"
REGION="us-east-2"
IMAGE="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPO:$IMAGE_TAG"
CONTAINER_NAME="app"

aws ecr get-login-password --region "$REGION" \
  | docker login --username AWS --password-stdin "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

docker pull "$IMAGE"

docker stop "$CONTAINER_NAME" 2>/dev/null || true
docker rm "$CONTAINER_NAME" 2>/dev/null || true

docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  -p 8080:8080 \
  --log-driver awslogs \
  --log-opt awslogs-group=/app/production \
  --log-opt awslogs-stream="$CONTAINER_NAME" \
  "$IMAGE"

echo "Starting health check..."
for i in {1..12}; do
  if curl -sf http://localhost:8080/api/actuator/health | grep -q '"status":"UP"'; then
    echo "Deployment successful."
    docker image prune -af --filter "until=168h" || true
    exit 0
  fi
  echo "Attempt $i: Health check failed, retrying in 5s..."
  sleep 5
done

echo "Health check failed after deployment." >&2
exit 1