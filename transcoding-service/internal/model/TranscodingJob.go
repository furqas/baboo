package model

type TranscodingJob struct {
	VideoId  string `json:"videoId"`
	S3Key    string `json:"s3Key"`
	UserId   string `json:"userId"`
	FileName string `json:"fileName"`
}
