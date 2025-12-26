package service

import (
	"context"
	"transcoding-service/internal/model"
)

type TranscodingService struct {
	repository TranscodingRepository
}

func (s *TranscodingService) ProcessJob(ctx context.Context, event *model.TranscodingJob) error {
	// here we should do the process

	return s.repository.Save(ctx, &event)
}
