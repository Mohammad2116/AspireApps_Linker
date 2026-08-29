# AspireApps_Linker

AspireApps_Linker is a link-shortener service implemented as a microservices system. This repository is the project starter and will be expanded over time.

## Overview
This project implements a URL shortening platform using a microservice architecture. Core components include:

- Service Discovery: Eureka
- API Gateway: Spring Cloud Gateway
- Messaging: Apache Kafka
- Primary datastore: PostgreSQL
- Cache: Redis
- Built with: Java + Spring Boot (Spring Cloud)

## Architecture (high level)
- Gateway: single entry point for clients; routes requests to backend services.
- Registry (Eureka): services register themselves so they can discover each other.
- Shortener Service: core service that creates and resolves short links, persists metadata to PostgreSQL, caches lookups in Redis.
- Analytics / Events: events (clicks, redirects) emitted to Kafka for asynchronous processing and later analytics.
- (Optional) Config server, Auth service, Monitoring and logging (to be added).

## Quick start (local / development)
1. Ensure Docker and Docker Compose are installed.
2. Start dependent services (Postgres, Redis, Kafka, Eureka, Gateway) — a `docker-compose.yml` will be added soon.
3. Build and run individual Spring Boot services via Maven/Gradle:
   - mvn -T 1C clean package
   - java -jar service/target/service.jar
4. We'll add a ready-to-run Docker Compose and Kubernetes manifests in the next updates.

## Next steps / TODO
- Scaffold modules: gateway, registry, shortener-service, analytics-service, config-server
- Add Docker Compose for local dev
- Add database migrations (Flyway/Liquibase)
- Add integration tests and CI pipeline
- Add instructions for deploying to Kubernetes

## Notes
This is an initial startup README. We'll update it as project structure, modules, and deployment details are added.
# AspireApps_Linker
AspireApps_Linker is a link-shortener service implemented as a microservices system. 

 
