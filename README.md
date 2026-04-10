# Microservices Backend System

A production-ready microservices backend built with **Spring Boot 3** and **Java 21**, featuring JWT authentication, service discovery, an API gateway, and two independent services communicating over HTTP.

---

## Architecture Overview

```
                        ┌─────────────────┐
                        │   API Gateway   │
          Client ──────►│   Port: 8080    │
                        └────────┬────────┘
                                 │ routes requests
                    ┌────────────┴────────────┐
                    ▼                         ▼
           ┌────────────────┐      ┌─────────────────────┐
           │  User Service  │      │ Transaction Service  │
           │  Port: 8081    │      │   Port: 8082         │
           └───────┬────────┘      └──────────┬──────────┘
                   │                          │
                   ▼                          ▼
            ┌─────────────┐         ┌──────────────────┐
            │   userdb    │         │  transactiondb   │
            │  MySQL 8.0  │         │   MySQL 8.0      │
            │  Port: 3306 │         │   Port: 3307     │
            └─────────────┘         └──────────────────┘

         All services register with:
         ┌──────────────────────┐
         │   Service Registry   │
         │   (Eureka) Port 8761 │
         └──────────────────────┘
```

---

## Services

| Service                 | Port | Description                                              |
| ----------------------- | ---- | -------------------------------------------------------- |
| **Service Registry**    | 8761 | Eureka server — service discovery for all microservices  |
| **API Gateway**         | 8080 | Single entry point — routes and validates JWT tokens     |
| **User Service**        | 8081 | Handles user registration, login, and profile management |
| **Transaction Service** | 8082 | Handles financial transactions (CREDIT/DEBIT)            |

---

## Tech Stack

| Category          | Technology                   |
| ----------------- | ---------------------------- |
| Language          | Java 21                      |
| Framework         | Spring Boot 3.2.4            |
| Service Discovery | Spring Cloud Netflix Eureka  |
| API Gateway       | Spring Cloud Gateway         |
| Security          | Spring Security + JWT (JJWT) |
| Database          | MySQL 8.0 (per service)      |
| ORM               | Spring Data JPA / Hibernate  |
| Containerization  | Docker + Docker Compose      |
| Testing           | JUnit 5, Mockito, AssertJ    |
| Build Tool        | Maven                        |

---

## Features

- **JWT Authentication** — Stateless token-based auth; tokens validated at the gateway level
- **Role-based Access Control** — `USER` and `ADMIN` roles with method-level security (`@PreAuthorize`)
- **Service Discovery** — All services auto-register with Eureka; gateway uses load-balanced routing (`lb://`)
- **Database per Service** — Each service has its own isolated MySQL database
- **Inter-service Communication** — Transaction service fetches user data from user-service using WebClient
- **Input Validation** — All request bodies validated with Jakarta Bean Validation
- **Global Exception Handling** — Consistent JSON error responses across all services
- **Unit Tests** — 61 tests with JaCoCo coverage reporting

---

## Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)
- Java 21+ (only needed if building locally without Docker)
- Maven 3.9+ (only needed if building locally without Docker)

---

## Getting Started

### Run with Docker (Recommended)

**1. Clone the repository**

```bash
git clone https://github.com/tejasbhosale17/microservices-backend.git
cd microservices-backend
```

**2. Build the JAR files**

```bash
mvn clean package -DskipTests
```

**3. Start the full stack**

```bash
docker compose up --build
```

This starts 6 containers: 2 MySQL databases + 4 Spring Boot services, in the correct dependency order.

**4. Verify everything is running**

Open http://localhost:8761 in your browser — you should see the Eureka dashboard with all services registered as **UP**.

**5. Stop the stack**

```bash
docker compose down
```

Add `-v` to also delete database data: `docker compose down -v`

---

## API Endpoints

All requests go through the **API Gateway at `http://localhost:8080`**.

### Auth (no token required)

| Method | Endpoint             | Description             |
| ------ | -------------------- | ----------------------- |
| `POST` | `/api/auth/register` | Register a new user     |
| `POST` | `/api/auth/login`    | Login and get JWT token |

### Users (token required)

| Method   | Endpoint          | Role  | Description         |
| -------- | ----------------- | ----- | ------------------- |
| `GET`    | `/api/users/{id}` | Any   | Get user by ID      |
| `GET`    | `/api/users`      | ADMIN | Get all users       |
| `PUT`    | `/api/users/{id}` | Any   | Update user profile |
| `DELETE` | `/api/users/{id}` | ADMIN | Delete a user       |

### Transactions (token required)

| Method | Endpoint                          | Description               |
| ------ | --------------------------------- | ------------------------- |
| `POST` | `/api/transactions`               | Create a transaction      |
| `GET`  | `/api/transactions`               | Get all transactions      |
| `GET`  | `/api/transactions/{id}`          | Get transaction by ID     |
| `GET`  | `/api/transactions/user/{userId}` | Get transactions by user  |
| `PUT`  | `/api/transactions/{id}/status`   | Update transaction status |

---

## Example Usage

**Register a user**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name": "Tejas", "email": "tejas@example.com", "password": "password123", "role": "USER"}'
```

**Login**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "tejas@example.com", "password": "password123"}'
```

Copy the `token` from the response.

**Create a transaction**

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer <your_token>" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "amount": 500.00, "type": "CREDIT", "description": "Salary"}'
```

**Transaction Types:** `CREDIT`, `DEBIT`  
**Transaction Statuses:** `PENDING`, `COMPLETED`, `FAILED`

---

## Postman Collection

A ready-to-use Postman collection is included: `Microservices-Backend.postman_collection.json`

**Import it:** Open Postman → Import → select the file.

The collection:

- Auto-saves the JWT token after login (no manual copy-paste)
- Auto-saves the transaction ID after creation
- Covers all 11 endpoints with example request bodies

---

## Running Tests

```bash
mvn test
```

**61 tests** across user-service and transaction-service.  
Tests use an **H2 in-memory database** — no MySQL needed to run tests.

**Generate coverage report:**

```bash
mvn clean test
# Report generated at: user-service/target/site/jacoco/index.html
#                      transaction-service/target/site/jacoco/index.html
```

---

## Project Structure

```
microservices-backend/
├── docker-compose.yml              # Full stack docker configuration
├── pom.xml                         # Parent POM (shared dependencies)
├── service-registry/               # Eureka server
├── api-gateway/                    # Spring Cloud Gateway + JWT filter
├── user-service/                   # User management + authentication
│   ├── src/main/java/
│   │   └── .../
│   │       ├── controller/         # AuthController, UserController
│   │       ├── service/            # AuthService, UserService
│   │       ├── security/           # JwtService, JwtAuthFilter, SecurityConfig
│   │       ├── entity/             # User, Role
│   │       ├── dto/                # Request/Response DTOs
│   │       ├── repository/         # UserRepository (JPA)
│   │       └── exception/          # GlobalExceptionHandler
│   └── src/test/                   # Unit tests
└── transaction-service/            # Transaction management
    ├── src/main/java/
    │   └── .../
    │       ├── controller/         # TransactionController
    │       ├── service/            # TransactionService
    │       ├── client/             # UserServiceClient (WebClient)
    │       ├── entity/             # Transaction, TransactionType, TransactionStatus
    │       ├── dto/                # Request/Response DTOs
    │       ├── repository/         # TransactionRepository (JPA)
    │       └── exception/          # GlobalExceptionHandler
    └── src/test/                   # Unit tests
```
