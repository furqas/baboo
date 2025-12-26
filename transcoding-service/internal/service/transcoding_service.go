package service

import (
	"context"
	"log"
	"os/exec"
	"path/filepath"
	"transcoding-service/internal/config"
	"transcoding-service/internal/model"
)

type TranscodingService struct {
	Repository TranscodingRepository
	Storage    StorageService
}

const OUTPUT_DIR = "/tmp/output"

func (s *TranscodingService) ProcessJob(ctx context.Context, event *model.TranscodingJob, cfg *config.Config) error {

	log.Printf("Received a process job, videoId: %s, key: %s, userId: %s", event.VideoId, event.S3Key, event.UserId)

	filePath, err := s.Storage.GetRawVideo(ctx, event.S3Key, event.FileName, cfg.BucketName)

	if err != nil {
		return err
	}

	manifestPath := filepath.Join(OUTPUT_DIR, "manifest.mpd")

	args := []string{
		"-y",
		"-i", filePath,
		"-filter_complex",
		"[0:v]split=4[v1080][v720][v480][v360];" +
			"[v1080]scale=-2:1080[v1080out];" +
			"[v720]scale=-2:720[v720out];" +
			"[v480]scale=-2:480[v480out];" +
			"[v360]scale=-2:360[v360out]",
		"-map", "[v1080out]", "-c:v:0", "libx264", "-b:v:0", "5000k", "-profile:v:0", "high",
		"-map", "[v720out]", "-c:v:1", "libx264", "-b:v:1", "3000k", "-profile:v:1", "main",
		"-map", "[v480out]", "-c:v:2", "libx264", "-b:v:2", "1500k", "-profile:v:2", "main",
		"-map", "[v360out]", "-c:v:3", "libx264", "-b:v:3", "800k", "-profile:v:3", "baseline",
		"-map", "0:a?", "-c:a", "aac", "-b:a", "128k",
		"-g", "48", "-keyint_min", "48", "-sc_threshold", "0",
		"-f", "dash", "-seg_duration", "6", "-use_template", "1", "-use_timeline", "1",
		manifestPath,
	}

	cmd := exec.Command("ffmpeg", args...)
	cmd.Stdout = nil
	cmd.Stdout = nil

	if err := cmd.Run(); err != nil {
		log.Printf("Error while executing fffmpeg command: %s\n", err)
	}
	//
	// if err != nil {
	// 	return
	// }

	log.Printf("Transcoding job processed successfully for file: %s\n", filePath)

	return s.Repository.Save(ctx, event)
}
