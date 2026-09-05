# AspireApps Linker

**AspireApps Linker** is a production-oriented URL shortening platform built with **Java 21, Spring Boot, Spring Cloud,
PostgreSQL, Redis, Kafka, Docker, and GitHub Actions**.

The project is implemented as a distributed microservices system with service discovery, an API Gateway, JWT-based
authentication, persistent link management, caching, asynchronous analytics, and containerized deployment.

---

## ✨ What is Linker?

Linker turns long URLs into short, shareable links.

The platform supports:

- Creating and managing shortened links
- Redirecting visitors through short links
- User registration and authentication
- JWT access tokens + refresh tokens
- Public and authenticated APIs
- Web pages rendered with Thymeleaf
- Redis-based short-link caching
- Asynchronous visit analytics through Kafka
- Link popularity tracking
- PostgreSQL persistence with Flyway migrations
- Service discovery with Eureka
- API routing through Spring Cloud Gateway
- Docker Compose based deployment
- GitHub Container Registry images and CI/CD workflows

---

## 🏗️ Architecture

```text
                         ┌──────────────────────┐
                         │       Clients        │
                         │ Browser / REST API   │
                         └──────────┬───────────┘
                                    │
                         ┌──────────▼───────────┐
                         │   Caddy / Cloudflare │
                         │   Public entrypoint  │
                         └──────────┬───────────┘
                                    │
                         ┌──────────▼───────────┐
                         │   Spring Cloud       │
                         │       Gateway        │
                         └──────┬───────┬───────┘
                                │       │
              ┌─────────────────┘       └─────────────────┐
              │                                           │
      ┌───────▼────────┐                         ┌────────▼────────┐
      │  User Service  │                         │  Links Service  │
      │ JWT / Users    │                         │ Short Links     │
      │ Refresh Tokens │                         │ Redis Cache     │
      └───────┬────────┘                         └────────┬────────┘
              │                                           │
              └──────────────────┬────────────────────────┘
                                 │
                         ┌───────▼────────┐
                         │     Eureka     │
                         │ Service        │
                         │ Discovery      │
                         └────────────────┘

                         ┌────────────────┐
                         │     Kafka      │
                         │ Async Events   │
                         └───────┬────────┘
                                 │
                         ┌───────▼────────┐
                         │    Analysis    │
                         │    Service     │
                         │ Analytics      │
                         └───────┬────────┘
                                 │
                         ┌───────▼────────┐
                         │   PostgreSQL   │
                         │ Persistent DBs  │
                         └────────────────┘
```

### Main components

| Service            | Responsibility                                           |
|--------------------|----------------------------------------------------------|
| `gateway-server`   | API Gateway, routing, JWT validation, web/API entrypoint |
| `eureka-server`    | Service discovery                                        |
| `user-service`     | Users, authentication, authorization, refresh tokens     |
| `links-service`    | Short-link creation, management and resolution           |
| `analysis-service` | Visit analytics and popularity processing                |
| `common`           | Shared DTOs, events and common infrastructure            |

Infrastructure:

- PostgreSQL 16
- Redis 8
- Apache Kafka 4.2.1
- Caddy
- Docker / Docker Compose
- GitHub Container Registry

---

## 🔐 Authentication

Linker uses a JWT-based authentication model.

### Access token

Access tokens contain the user's identity and authorization information and are used for authenticated requests.

The Gateway validates the token and propagates authenticated identity information to downstream services.

### Refresh token

Refresh tokens are:

- Persisted in PostgreSQL
- Stored as hashes rather than raw tokens
- Associated with a user
- Expirable
- Revocable
- Rotated during refresh

This allows the system to invalidate previously issued refresh tokens without storing reusable credentials in the
database.

### Password security

User passwords are hashed using **BCrypt**.

---

## 🔗 Short-link lifecycle

A simplified link lifecycle looks like this:

```text
Create Link
    │
    ▼
Links Service
    │
    ├── Persist metadata ──► PostgreSQL
    │
    ├── Cache link ────────► Redis
    │
    └── Publish event ─────► Kafka
                              │
                              ▼
                         Analysis Service
```

When a short link is visited:

```text
Visitor
   │
   ▼
Gateway
   │
   ▼
Links Service
   │
   ├── Redis lookup
   │
   ├── PostgreSQL fallback
   │
   └── Publish visit event
             │
             ▼
           Kafka
             │
             ▼
      Analysis Service
             │
             └── Update analytics / popularity
```

Analytics processing is asynchronous so the redirect path does not need to wait for analytics persistence.

---

## ⚡ Redis caching

The Links Service uses Redis to reduce database lookups during short-link resolution.

The cache uses TTL-based expiration and adjusts caching behavior according to link popularity.

This provides a simple cache-aside strategy:

```text
Request
  │
  ▼
Redis ───── hit ─────► Return link
  │
 miss
  │
  ▼
PostgreSQL
  │
  ▼
Cache result
  │
  ▼
Return link
```

Redis is configured with authentication and persistent storage in the deployment environment.

---

## 📨 Event-driven analytics

Kafka is used for asynchronous communication between services.

Current event topics include:

- `link-visit-topic`
- `popularity-response-topic`
- `link-registered-topic`
- `link-deleted-topic`
- `analytics-error-topic`

This keeps analytics processing decoupled from the main link-resolution flow.

The project also uses an **Outbox pattern** for reliable event publication around transactional operations.

---

## 🗄️ Database

The platform uses **PostgreSQL** with **Flyway** database migrations.

Each service owns its database schema logically, while the deployment uses a shared PostgreSQL server with separate
databases.

Examples of persisted data include:

- Users
- Refresh tokens
- Short links
- Link metadata
- Analytics data
- Outbox messages

Database schema changes are versioned through Flyway migrations.

---

## 🌐 Web + REST API

Linker supports both REST APIs and server-rendered web pages.

### Web interface

The web interface is implemented with **Thymeleaf**.

Public routing is exposed through:

```text
https://aspireapps.ir/linker/...
```

### REST API

API endpoints are exposed through:

```text
https://api.aspireapps.ir/linker/...
```

Internally, Gateway routes use the application's `/ir/aspireapps/linker/...` path structure.

---

## 📁 Project structure

```text
AspireApps_Linker/
│
├── common/
│
├── eureka-server/
│
├── gateway-server/
│
├── user-service/
│
├── links-service/
│
├── analysis-service/
│
├── postgres-init/
│
├── .github/
│   └── workflows/
│
├── docker-compose.prod.yml
└── pom.xml
```

The project is organized as a Maven multi-module Spring Boot application.

---

## 🛠️ Technology Stack

### Backend

- Java 21
- Spring Boot 3.5.x
- Spring Cloud
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Security
- Spring Data JPA
- Hibernate
- OpenFeign
- Thymeleaf

### Security

- JWT
- JJWT
- BCrypt
- Refresh-token rotation

### Data

- PostgreSQL 16
- Redis 8
- Flyway

### Messaging

- Apache Kafka 4.2.1
- Event-driven communication
- Outbox pattern

### Infrastructure

- Docker
- Docker Compose
- Caddy
- Cloudflare
- GitHub Container Registry

### CI/CD

Container images are built and published through GitHub Actions and deployed using Docker Compose.

---

## 🚀 Running the project

### Prerequisites

Install:

- Docker
- Docker Compose
- Git
- JDK 21 (for local Maven development)

### Build

From the project root:

```bash
./mvnw clean package
```

or:

```bash
mvn clean package
```

### Run the production-style stack

```bash
docker compose -f docker-compose.prod.yml up -d
```

Check running containers:

```bash
docker compose -f docker-compose.prod.yml ps
```

View logs:

```bash
docker compose -f docker-compose.prod.yml logs -f
```

Stop the stack:

```bash
docker compose -f docker-compose.prod.yml down
```

Persistent data is stored in Docker volumes for PostgreSQL, Kafka and Redis.

---

## 🩺 Health & service discovery

Services register themselves with Eureka.

The deployment waits for required infrastructure health checks before starting dependent services.

This allows the application stack to start in dependency order:

```text
PostgreSQL / Kafka / Redis
          │
          ▼
       Eureka
          │
          ▼
    Application Services
          │
          ▼
       Gateway
```

---

## 🔄 CI/CD

The project follows a branch-oriented development workflow.

Feature development is performed on `dev/feature/...` branches.

Completed service changes are transferred into their corresponding CI/CD microservice branches, for example:

```text
dev/feature/gateway-server
            │
            │ merge
            ▼
CI-CD/microservice/gateway-server
```

The CI/CD side builds container images and publishes them to GitHub Container Registry.

---

## 🧩 Design principles

The project focuses on practical distributed-system patterns rather than building a large framework around a simple URL
shortener.

Key design decisions include:

- Service boundaries based on business responsibility
- Database ownership per service
- API Gateway as the external entry point
- Eureka for service discovery
- JWT-based stateless access authentication
- Persistent and rotatable refresh tokens
- Redis for frequently accessed short links
- Kafka for asynchronous analytics
- Outbox pattern for reliable event publishing
- Flyway for version-controlled schema changes
- Docker Compose for reproducible deployment

---

## 📌 Project status

AspireApps Linker is an actively developed portfolio project focused on demonstrating practical backend and
distributed-system engineering skills.

The current implementation includes the core microservice architecture, authentication, link management, caching,
event-driven analytics, database migrations, containerized infrastructure and deployment workflow.

---

## 👨‍💻 Author

**Mohammad Momensafaei**

Backend Developer

Java • Spring Boot • Microservices • PostgreSQL • Redis • Kafka • Docker

---

## 📄 License

This project is currently maintained as a personal portfolio project.