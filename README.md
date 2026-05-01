# Reminder System

A distributed microservices-based recurring reminder system built with Spring Boot, PostgreSQL, RabbitMQ, and Redis.

## 🏗️ Architecture

- **api-gateway** (port 8080) - Spring Cloud Gateway with JWT verification
- **auth-service** (port 8084) - User authentication & JWT token generation
- **reminder-service** (port 8081) - Reminder CRUD operations with RRULE support
- **scheduler-service** (port 8082) - Background job polling reminders and publishing to message queue
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
```

Edit `.env` and update the following:
- Database credentials (POSTGRES_USER, POSTGRES_PASSWORD)
- **JWT_SECRET** - Change to a secure random string (minimum 32 characters)
- Service ports if needed

**Example JWT_SECRET generation:**
```bash
# Use a random string generator or create your own secure key
JWT_SECRET=your-secure-random-256-bit-secret-key-here-change-this
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

### 6. Build all services

```bash
.\mvnw.cmd clean install -DskipTests
```

### 7. Run services

Each service can be run independently:

```bash
# Terminal 1 - Auth Service
cd auth-service
.\mvnw.cmd spring-boot:run

# Terminal 2 - Reminder Service
cd reminder-service
.\mvnw.cmd spring-boot:run

# Terminal 3 - Notification Service
cd notification-service
.\mvnw.cmd spring-boot:run

# Terminal 4 - Scheduler Service
cd scheduler-service
.\mvnw.cmd spring-boot:run

# Terminal 5 - API Gateway
cd api-gateway
.\mvnw.cmd spring-boot:run
```

## 📊 Database Schema

The system uses three main tables:

- **users** - User accounts with BCrypt password hashing (managed by auth-service)
- **reminders** - One-time and recurring reminders with RRULE support
- **execution_logs** - Audit trail with idempotency guarantees

Migrations are managed by Flyway and run automatically on startup.

## 🔗 Access Points

- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8084
- **Reminder Service**: http://localhost:8081
- **Scheduler Service**: http://localhost:8082
- **Notification Service**: http://localhost:8083
- **pgAdmin**: http://localhost:5050
  - Email: (configured in .env)
  - Password: (configured in .env)
- **RabbitMQ Management**: http://localhost:15672
  - Username: (configured in .env)
  - Password: (configured in .env)
- **Redis**: localhost:6379

## 📚 Documentation

- [Architecture](docs/architecture.md)
- [Database Schema](docs/database-schema.md)
- [Setup Summary](SETUP-SUMMARY.md)

## 🔐 Security

- **JWT Authentication**: Auth-service generates JWT tokens (access + refresh)
- **API Gateway**: Verifies JWT tokens on all requests (except /api/auth/**)
- **BCrypt Password Hashing**: Cost factor 10 for secure password storage
- **Token Expiration**: Access token (1 hour), Refresh token (24 hours)
- **Stateless Sessions**: No server-side session storage

### Authentication Flow

1. **Register/Login**: POST to `/api/auth/register` or `/api/auth/login`
2. **Receive Tokens**: Get `accessToken` and `refreshToken` in response
3. **Access Protected Routes**: Include header: `Authorization: Bearer <accessToken>`
4. **Refresh Token**: POST to `/api/auth/refresh` with refresh token when access token expires

### API Endpoints

**Auth Service (via API Gateway)**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get tokens
- `POST /api/auth/refresh` - Refresh access token

**Protected Routes** (require JWT)
- `/api/reminders/**` - Reminder operations
- `/api/scheduler/**` - Scheduler operations
- `/api/notifications/**` - Notification operations

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
