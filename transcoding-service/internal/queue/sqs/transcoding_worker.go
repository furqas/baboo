package sqs

import (
	"context"
	"encoding/json"
	"transcoding-service/internal/config"
	"transcoding-service/internal/event"
	"transcoding-service/internal/service"
)

type TranscodingWorker struct {
	service *service.TranscodingService
}

func NewTranscodingWorker(service *service.TranscodingService) *TranscodingWorker {
	return &TranscodingWorker{
		service: service,
	}
}

func (w *TranscodingWorker) Handle(ctx context.Context, msg []byte, cfg *config.Config) error {
	var event event.TranscodingEvent

	if err := json.Unmarshal(msg, &event); err != nil {
		return err
	}

	model := event.ToModel()

	processedPath, err := w.service.ProcessJob(ctx, model, cfg)

	if err != nil {
		return err
	}

	err = w.service.SendChunksToStorage(ctx, processedPath, model)

	return err
}
