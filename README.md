# Reminder System

A distributed microservices-based recurring reminder system built with Spring Boot, PostgreSQL, RabbitMQ, and Redis.

## 🏗️ Architecture

- **api-gateway** (port 8080) - Spring Cloud Gateway with JWT authentication
- **reminder-service** (port 8081) - User authentication & reminder CRUD operations
- **scheduler-service** - Background job polling reminders and publishing to message queue
- **notification-service** (port 8083) - Consumes messages and dispatches notifications

## 🚀 Tech Stack

- **Backend**: Java 17, Spring Boot 3.5.14
- **Database**: PostgreSQL 16
- **Message Broker**: RabbitMQ 3.13
- **Cache/Lock**: Redis 7 + Redisson
- **Migration**: Flyway
- **Containerization**: Docker Compose

## 📋 Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.6+

## 🛠️ Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd reminder-system
```

### 2. Configure environment

```bash
cp .env.example .env
# Edit .env with your configuration
```

### 3. Setup RabbitMQ definitions

```bash
cp infra/rabbitmq/definitions.example.json infra/rabbitmq/definitions.json
```

Edit `infra/rabbitmq/definitions.json` and replace:
- `your_username_here` with your RabbitMQ username (default: `guest`)
- `your_password_here` with your RabbitMQ password (default: `guest`)

**For development**, you can use:
```json
{
  "name": "guest",
  "password": "guest",
  "tags": ["administrator"]
}
```

**Security Note**: Never commit `definitions.json` to git as it contains credentials.

### 4. Start infrastructure

```bash
docker-compose up -d
```

This will start:
- PostgreSQL (port 5432)
- pgAdmin (port 5050)
- Redis (port 6379)
- RabbitMQ (port 5672, Management UI: 15672)

### 5. Sync environment to services

```bash
.\sync-env.bat
```

### 6. Run services

Each service can be run independently:

```bash
# Terminal 1 - Reminder Service
cd reminder-service
.\mvnw.cmd spring-boot:run

# Terminal 2 - Notification Service
cd notification-service
.\mvnw.cmd spring-boot:run

# Terminal 3 - Scheduler Service
cd scheduler-service
.\mvnw.cmd spring-boot:run

# Terminal 4 - API Gateway
cd api-gateway
.\mvnw.cmd spring-boot:run
```

## 📊 Database Schema

The system uses three main tables:

- **users** - User accounts with BCrypt password hashing
- **reminders** - One-time and recurring reminders with RRULE support
- **execution_logs** - Audit trail with idempotency guarantees

Migrations are managed by Flyway and run automatically on startup.

## 🔗 Access Points

- **API Gateway**: http://localhost:8080
- **Reminder Service**: http://localhost:8081
- **Notification Service**: http://localhost:8083
- **pgAdmin**: http://localhost:5050
  - Email: admin@reminder.com
  - Password: admin
- **RabbitMQ Management**: http://localhost:15672
  - Username: guest
  - Password: guest
- **Redis**: localhost:6379

## 📚 Documentation

- [Architecture](docs/architecture.md)
- [Database Schema](docs/database-schema.md)
- [Setup Summary](SETUP-SUMMARY.md)

## 🔐 Security

- JWT-based authentication at API Gateway
- BCrypt password hashing (cost factor 12)
- X-User-Id header injection for downstream services

## 🎯 Key Features

- **Distributed Locking**: Using Redis + Redisson for scheduler coordination
- **Idempotency**: Unique idempotency keys prevent duplicate notifications
- **RRULE Support**: RFC 5545 compliant recurring rules via ical4j
- **Dead Letter Queue**: Failed messages routed to DLQ for manual inspection
- **Graceful Shutdown**: Proper cleanup of resources on service termination

## 📝 License

[Your License Here]

## 👥 Contributors

[Your Name/Team]
