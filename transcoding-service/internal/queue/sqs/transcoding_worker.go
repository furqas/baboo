package sqs

import (
	"context"
	"encoding/json"
	"transcoding-service/internal/model"
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

func (w *TranscodingWorker) Handle(ctx context.Context, msg []byte) error {
	var event model.TranscodingJob

	if err := json.Unmarshal(msg, &event); err != nil {
		return err
	}

	return w.service.ProcessJob(ctx, &event)
}
