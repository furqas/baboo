package config

import "os"

type Config struct {
	Port       string
	DBURL      string
	LogLevel   string
	BucketName string
}

func LoadConfig() *Config {
	cfg := &Config{
		Port:       getEnv("PORT", "8080"),
		DBURL:      getEnv("DB_URL", ""),
		LogLevel:   getEnv("LOG_LEVEL", "info"),
		BucketName: getEnv("BUCKET_NAME", "baboo_dev_bucket"),
	}

	if cfg.DBURL == "" {
		panic("DB_URL is required")
	}

	return cfg
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
