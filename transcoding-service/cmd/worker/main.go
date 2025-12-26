package main

import (
	"context"
	"log"
	"transcoding-service/internal/aws"
	"transcoding-service/internal/config"
	"transcoding-service/internal/database/mongo"
	"transcoding-service/internal/queue/sqs"
	mongorepo "transcoding-service/internal/repository/mongo"
	"transcoding-service/internal/service"
	s3storage "transcoding-service/internal/storage/s3"
)

func main() {
	ctx := context.Background()

	cfg := config.LoadConfig()

	awsCfg, err := aws.LoadConfig(ctx)
	if err != nil {
		log.Fatalf("failed to load AWS config: %v", err)
	}

	// MongoDB
	mongoClient, err := mongo.Connect(ctx, cfg.DBURL)
	if err != nil {
		log.Fatalf("failed to connect to MongoDB: %v", err)
	}
	db := mongoClient.Database("baboo")
	repo := mongorepo.NewTranscodingRepository(db)

	// S3
	s3Client := s3storage.New(awsCfg)
	storage := s3storage.NewStorageService(s3Client)

	// Service
	ts := &service.TranscodingService{
		Repository: repo,
		Storage:    storage,
	}

	// SQS
	sqsClient := sqs.New(awsCfg)
	queueURL := "YOUR_SQS_QUEUE_URL" // TODO: set via config/env
	consumer := sqs.NewConsumer(sqsClient, queueURL)
	worker := sqs.NewTranscodingWorker(ts)

	consumer.Start(ctx, func(msg []byte) error {
		return worker.Handle(ctx, msg, cfg)
	})
}
