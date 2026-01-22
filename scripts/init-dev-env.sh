#!/usr/bin/env fish

# Init docker stack
sudo docker compose up -d

echo "Waiting for LocalStack to start..."
while not curl -s http://localhost:4566/_localstack/health >/dev/null
    echo "LocalStack not ready yet..."
    sleep 2
end
echo "LocalStack is up!"

# Transcoding job queue
set -x QUEUE_NAME "transcoding-job"

aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name $QUEUE_NAME
#
# Buckets
set -x RAW_BUCKET_NAME "baboo-raw-bucket"
set -x PROCESSED_BUCKET_NAME "baboo-processed-bucket"

aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket $RAW_BUCKET_NAME --region us-east-1
aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket $PROCESSED_BUCKET_NAME --region us-east-1

echo "Queue URL: $JOB_REQUEST_QUEUE"
echo "RAW_BUCKET_NAME=$RAW_BUCKET_NAME"
echo "PROCESSED_BUCKET_NAME=$PROCESSED_BUCKET_NAME"

