## About the service


myapp/
├── cmd/
│   ├── api/
│   │   └── main.go        # REST server
│   └── worker/
│       └── main.go        # Consumer SQS
│
├── internal/
│   ├── config/
│   │   └── config.go
│   │
│   ├── http/
│   │   ├── handlers/
│   │   ├── middlewares/
│   │   └── router.go
│   │
│   ├── queue/
│   │   ├── sqs/
│   │   │   ├── consumer.go
│   │   │   ├── producer.go
│   │   │   └── client.go
│   │   └── interfaces.go
│   │
│   ├── service/
│   │   ├── order_service.go
│   │   └── user_service.go
│   │
│   ├── repository/
│   │   ├── order_repo.go
│   │   └── user_repo.go
│   │
│   ├── domain/
│   │   ├── order.go
│   │   └── user.go
│   │
│   └── logger/
│       └── logger.go
│
├── pkg/                  # libs reutilizáveis (opcional)
│   └── observability/
│
├── migrations/
├── docker/
├── scripts/
├── go.mod
└── go.sum

