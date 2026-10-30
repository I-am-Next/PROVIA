# PROVIA
### A Microservices-Based Marketplace for Local Service Professionals

Provia is a service marketplace designed to connect customers with skilled local professionals, making it easier to discover service providers, request services, receive quotations, book jobs and make secure payments.

The platform is being developed around a microservices architecture, with a strong emphasis on service separation, reliable inter-service communication, event-driven workflows, payment security and customer trust.

One of Provia's key differentiators is its escrow-based payment model, which is designed to protect customers by holding payment until the relevant service milestones have been completed.

> **Project status:** Backend core implemented. Active development and testing are ongoing. Frontend integration, additional services, production hardening and deployment remain on the roadmap.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Core Features](#core-features)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Microservices](#microservices)
- [Inter-Service Communication](#inter-service-communication)
- [Event-Driven Architecture](#event-driven-architecture)
- [Payment Processing and Escrow](#payment-processing-and-escrow)
- [Security](#security)
- [Testing and Validation](#testing-and-validation)
- [Infrastructure and Development Environment](#infrastructure-and-development-environment)
- [Getting Started](#getting-started)
- [Environment Configuration](#environment-configuration)
- [Running the Infrastructure](#running-the-infrastructure)
- [Running the Microservices](#running-the-microservices)
- [Project Structure](#project-structure)
- [Development Roadmap](#development-roadmap)
- [Development and AI Assistance](#development-and-ai-assistance)
- [Project Attribution](#project-attribution)

---

## Project Overview

Finding reliable professionals for everyday services can be difficult. Customers need a convenient way to discover qualified providers, understand available services, agree on pricing and make payments with confidence.

Provia aims to address these challenges through a digital marketplace that connects customers with local professionals.

The platform supports three primary user groups:

- **Customers:** Discover professionals, request services, receive quotations, make bookings and payments, and rate completed services.
- **Service Providers:** Create professional profiles, offer services, respond to customer requests, submit quotations and receive payments.
- **Administrators:** Will have dedicated administrative capabilities for managing users, handling complaints and enforcing platform policies.

Provia is being developed as a distributed backend system in which individual business capabilities are separated into independently organised microservices.

The architecture is designed to support maintainability, scalability, fault isolation and reliable communication between services.

## Core Features

The implemented backend capabilities include:

- User authentication and identity management.
- Customer and provider management.
- Service categories and service catalog management.
- Provider and service search.
- Booking and quotation workflows.
- Payment processing through Paystack.
- Escrow management.
- Provider wallet transactions.
- Separate materials and labour release operations.
- Provider ratings.
- Event-driven notifications.
- Media upload and storage.
- API routing, authentication and request filtering.
- Asynchronous communication through Apache Kafka.
- Transactional outbox and idempotent event consumption.

Some additional platform capabilities are still under development.

---

## Technology Stack

| Category | Technology |
|---|---|
| Programming Language | Java 21 |
| Backend Framework | Spring Boot |
| Architecture | Microservices |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security, JWT |
| Database | PostgreSQL |
| Testing Database | H2 |
| Database Migrations | Flyway |
| Synchronous Communication | Spring Cloud OpenFeign |
| Asynchronous Communication | Apache Kafka |
| Message Reliability | Transactional Outbox and Inbox Patterns |
| Caching | Redis |
| Search and Indexing | Elasticsearch |
| Payment Integration | Paystack |
| Object Storage | AWS S3 |
| Local Object Storage / Development | MinIO |
| Containerisation | Docker |
| Container Orchestration for Local Infrastructure | Docker Compose |
| Build Tool | Maven |
| Unit Testing | JUnit and Mockito |
| Integration Testing | Testcontainers |
| API and Workflow Testing | Postman |
| Development Environment | IntelliJ IDEA |

---

## System Architecture

Provia follows a microservices architecture.

Each microservice is responsible for a specific business capability and maintains ownership of its application logic and data.

Each implemented microservice has its own PostgreSQL database rather than sharing a single application database.

This separation helps maintain clear service boundaries and reduces direct dependencies between business domains.

### High-level architecture

```text
                       CLIENT APPLICATION
                               |
                               |
                         API GATEWAY
                               |
          +--------------------+--------------------+
          |                    |                    |
          v                    v                    v
      AUTH SERVICE        USER SERVICE        PROVIDER SERVICE
          |                    |                    |
          v                    v                    v
       AUTH DB              USER DB             PROVIDER DB


          +--------------------+--------------------+
          |                    |                    |
          v                    v                    v
    SERVICE CATALOG       SEARCH SERVICE       BOOKING SERVICE
          |                    |                    |
          v                    v                    v
       CATALOG DB           SEARCH DB            BOOKING DB


          +--------------------+--------------------+
          |                    |                    |
          v                    v                    v
     PAYMENT SERVICE       RATING SERVICE     NOTIFICATION SERVICE
          |                    |                    |
          v                    v                    v
       PAYMENT DB            RATING DB         NOTIFICATION DB


                         MEDIA STORAGE SERVICE
                                  |
                                  v
                               MEDIA DB
                                  |
                                  v
                              AWS S3


                   APACHE KAFKA EVENT STREAMING
                                  |
                                  |
                   REDIS       ELASTICSEARCH

This is a conceptual representation of the major backend components and infrastructure, not a complete deployment topology.
Architectural principles
The system follows several important architectural principles:
1. Service ownership: Each microservice owns its business responsibilities and application data.
2. Database separation: Services maintain independent databases.
3. Loose coupling: Services communicate through defined APIs and asynchronous events.
4. Event-driven workflows: Important business events are communicated through Kafka.
5. Data consistency: Transactional outbox and inbox patterns help maintain reliable event processing.
6. Idempotency: Duplicate requests and event deliveries are handled with safeguards.
7. Separation of financial responsibilities: Payment Service owns financial operations, while Booking Service owns booking lifecycle management.

Microservices
The following services are included in the current public repository.
Microservice	Responsibility
Auth Service	Authentication, identity verification and JWT issuance.
User Service	Customer and user management.
Provider Service	Professional profiles and provider management.
Service Catalog	Service categories and catalog management.
Search Service	Search functionality and indexing of relevant marketplace data.
Booking Service	Booking lifecycle, quotations and booking workflow management.
Payment Service	Payment processing, escrow and wallet-related financial operations.
Rating Service	Provider ratings and rating management.
Notification Service	Event-driven notification processing.
Media Storage Service	Media upload and storage management.
API Gateway	Central API routing and request security.

Planned services
The following services are not yet part of the completed backend implementation:
- Admin Service
- Chat Service
- Analytics Service
- Pricing Service
They are planned extensions of the platform and should not be confused with the currently implemented services.

Inter-Service Communication
Provia uses two main communication mechanisms.
1. Synchronous communication
Spring Cloud OpenFeign is used for synchronous REST communication between microservices.

This is suitable when a service needs an immediate response from another service to complete an operation.
2. Asynchronous communication
Apache Kafka is used for asynchronous communication.

Services publish business events that other services can consume independently.

This reduces unnecessary direct dependencies and allows important business workflows to continue across service boundaries without requiring every participating service to be called synchronously.

Event-Driven Architecture
Kafka is an important part of Provia's backend architecture.

The system uses event-driven communication for workflows such as payment confirmation, escrow operations, booking updates and notifications.
Kafka topics
The main business event topics include:
Topic	Purpose
booking-events	Booking-related events.
quote-events	Quotation-related events.
payment-events	Payment lifecycle events.
escrow-events	Escrow-related events.
wallet-events	Wallet and financial transaction events.

Shared event conventions
Provia follows shared Kafka conventions across its relevant microservices.

These include:
- A shared EventEnvelope structure.
- Explicit event types and event versions.
- Stable Kafka message keys based on business aggregate IDs.
- Transactional outbox pattern for reliable event publication.
- Inbox and processed-event tracking for idempotent consumption.
- Separate consumer groups for independent consuming services.
- Retry handling for transient failures.
- Dead-letter handling for repeatedly failing messages.
- Explicit ownership of business state by the responsible microservice.
Transactional Outbox Pattern
The transactional outbox pattern is used to address the dual-write problem.

Instead of independently committing a business database transaction and publishing a Kafka event, the service records the business change and its corresponding outbox event in the same database transaction.

A separate publisher retrieves pending outbox records and publishes them to Kafka.

This helps prevent situations where a business operation succeeds but its corresponding event is never recorded for publication.
Idempotent Event Consumption
Kafka can deliver messages more than once.

To prevent duplicate processing from causing repeated business operations, consuming services use event identity and processed-event tracking.

This is particularly important for financial operations and booking state transitions.
State ownership
Each service updates only the business state it owns.

For example:
- Payment Service owns payment and escrow financial operations.
- Booking Service owns the booking lifecycle.
- Notification Service owns notification processing.
Booking state changes are based on validated business events rather than assuming that a successful HTTP response from another service proves that a financial operation has completed.

Payment Processing and Escrow
Payment processing and escrow are among the most important business capabilities in Provia.

The platform integrates Paystack for payment processing.

Bank transfer payments have not yet been implemented.
Payment workflow
The payment workflow follows this sequence:
1. A customer accepts a provider's quotation.
2. The booking enters the payment stage.
3. Payment Service creates a pending payment associated with the booking.
4. Payment Service initiates a Paystack transaction.
5. An authorization URL is returned for the customer to complete payment.
6. Paystack sends a webhook notification.
7. The webhook signature is validated.
8. The transaction is independently verified against the Paystack API.
9. The transaction reference, amount, currency and payment status are validated.
10. The payment is marked as completed.
11. An escrow record is created for the booking.
12. Payment and escrow events are recorded through the transactional outbox.
13. The events are published to Kafka.
14. Booking Service consumes and validates the payment event before updating its own booking state.
Escrow management
Provia uses escrow to provide an additional layer of financial assurance.

Customer payments are held against a booking and released through controlled financial operations.

The escrow model separates materials and labour release operations.
Materials release
Materials release is treated as a distinct financial operation.

The operation involves:
- Validating the release request.
- Checking the relevant escrow allocation.
- Recording the financial operation.
- Crediting the provider's wallet through a wallet transaction.
- Publishing the corresponding business event.
- Allowing Booking Service to update its own workflow based on the validated event.
Labour release
Labour release is handled separately from materials release.

It has its own financial operation and wallet transaction.

Separate operation identities and idempotency safeguards help prevent duplicate financial releases.
Financial reliability safeguards
The payment and escrow implementation includes:
- Idempotent payment attempts.
- Paystack webhook signature verification.
- Independent transaction verification.
- Payment reference validation.
- Amount validation.
- Currency validation.
- Payment status validation.
- Transactional outbox publication.
- Idempotent wallet transactions.
- Inbox and processed-event protection.
- Separate materials and labour release operations.
- Retry-safe release operations.
- Escrow balance tracking.
- Event-driven booking state synchronization.
The architecture maintains a clear separation between financial ownership and booking workflow ownership.

Payment Service owns the money. Booking Service owns the booking lifecycle. Kafka communicates validated business facts between services.

Security
Security is implemented through the authentication and API access architecture.

The Auth Service is responsible for issuing JWTs.

Spring Security and JWT-based resource-server validation are used across the relevant protected services.

The API Gateway provides centralised request handling and security-related functionality, including:
- API routing.
- JWT authentication.
- Authorization.
- Rate limiting.
- Request filtering.
- Redis-backed functionality.
Sensitive credentials and environment-specific configuration are maintained outside the source-controlled repository.

Testing and Validation
Provia has undergone testing at multiple levels.

The testing strategy includes unit tests, integration tests and end-to-end workflow testing.
Unit testing
A substantial number of unit tests have been implemented to validate individual application components, services and business logic.

Technologies include:
- JUnit
- Mockito
Integration testing
Several integration tests have been implemented to validate interactions between application components and selected infrastructure integrations.

Testcontainers is used for integration testing scenarios.

H2 is also used for testing database scenarios.

Additional integration tests will be added as development continues.
End-to-end testing
One of the most significant validation milestones has been testing the complete application workflows through Postman.

Rather than testing only isolated endpoints, the objective was to exercise the interaction between the microservices and verify that the expected business operations occurred.

The tested functionality includes:
- Search Service indexing and search functionality.
- Service Catalog operations.
- Payment processing and Paystack webhook handling.
- Notification consumers receiving and processing events.
- Rating Service functionality.
- Booking and related service workflows.
- API endpoint responses.
- Interactions between multiple microservices.
The end-to-end tests demonstrated that the tested endpoints returned valid responses and that the major workflows operated across the relevant services.

Testing remains ongoing. Additional unit, integration and end-to-end tests will be added to increase confidence in the correctness, reliability and behaviour of the system.

Infrastructure and Development Environment
Provia's backend infrastructure is managed locally using Docker Compose.

The microservices are run directly from IntelliJ IDEA during development, while their supporting infrastructure runs in Docker containers.
Infrastructure components
Component	Purpose
PostgreSQL	Persistent storage for microservices.
Apache Kafka	Asynchronous event communication.
ZooKeeper	Kafka coordination in the current local development setup.
Redis	Caching and supporting gateway functionality.
Elasticsearch	Search indexing and retrieval.
Kibana	Elasticsearch visualisation and management.
MinIO	Object storage used during early development and testing.
AWS S3	Integrated object storage for photos and documents.

Each microservice maintains its own PostgreSQL database.

The Docker Compose configuration creates the required local infrastructure containers, named volumes and shared backend network.
Local development ports
The current infrastructure configuration uses the following host ports:

Getting Started
The following instructions describe the local development setup.
Prerequisites
Install the following tools:
- Java 21
- IntelliJ IDEA
- Maven
- Docker Desktop
- Git
- Postman
Ensure Docker Desktop is running before starting the infrastructure.
1. Clone the repository
git clone https://github.com/I-am-Next/PROVIA.git


Navigate into the project:
cd PROVIA


2. Configure environment variables
Create a local environment file named:
.env.local


Use .env.example as the configuration template.

Copy the example file and provide your own local credentials.

PowerShell:
Copy-Item .env.example .env.local


Update the placeholders in .env.local with your actual local configuration.

Do not commit .env.local or any file containing real credentials.
3. Validate Docker Compose configuration
The project uses .env.local for local infrastructure configuration.

Validate the configuration with:
docker compose --env-file .env.local -f docker-compose.infrastructure.yml config --quiet


4. Start the infrastructure
Run:
docker compose --env-file .env.local -f docker-compose.infrastructure.yml up -d


Check the running containers:
docker compose -f docker-compose.infrastructure.yml ps


5. Open the project in IntelliJ IDEA
Open the root Provia project directory.

Import the Maven projects and allow IntelliJ IDEA to resolve their dependencies.

Configure the environment variables for each Spring Boot service using the appropriate values from .env.local.

For local development, the Spring Boot microservices run directly from IntelliJ IDEA while their supporting infrastructure runs in Docker.
6. Run the microservices
Start the required services individually using their Spring Boot application entry points in IntelliJ IDEA.

The recommended startup order is:
1. Auth Service.
2. User Service.
3. Provider Service.
4. Service Catalog.
5. Search Service.
6. Booking Service.
7. Payment Service.
8. Rating Service.
9. Notification Service.
10. Media Storage Service.
11. API Gateway.
Ensure the infrastructure dependencies are running and that the relevant service configuration is correct before starting each application.

Environment Configuration
The .env.example file contains the environment variable names and placeholder values required for local development.

The configuration includes:
- JWT configuration.
- PostgreSQL connection URLs.
- Database usernames and passwords.
- Internal service configuration.
- Paystack credentials.
- Kafka bootstrap servers.
- Redis connection settings.
- Elasticsearch URI.
- MinIO configuration.
- AWS S3 credentials.
Actual secrets must be supplied locally.

Never commit real credentials, private keys, API secrets or production environment files to GitHub.

Project Structure
The repository is organised around independently developed microservices.
PROVIA/
│
├── auth-service/
├── user-service/
├── provider-service/
├── service-catalog/
├── search-service/
├── booking-service/
├── payment-service-new/
├── rating-service/
├── notification-service/
├── media-storage/
├── api-gateway/
│
├── api-request/
├── contracts/
│
├── docker-compose.infrastructure.yml
├── .env.example
├── .gitignore
└── README.md


contracts contains shared event and contract definitions used by relevant services.

api-request contains API request resources used during development.

Development Roadmap
Provia's backend core has been implemented and tested through unit tests, selected integration tests and end-to-end workflows.

The next development stages include:
Backend expansion
- Complete Admin Service.
- Implement Chat Service and real-time messaging.
- Implement Analytics Service.
- Implement Pricing Service.
Frontend development
The frontend will be built using:
- Next.js
- TypeScript
- Tailwind CSS
- Shadcn UI
- TanStack Query
- Zustand
- React Hook Form
- Zod
The frontend will provide the customer and provider experiences and integrate with the existing backend APIs.
Production hardening
- Expand automated test coverage.
- Strengthen integration and end-to-end testing.
- Continue security and reliability reviews.
- Improve monitoring and observability.
- Review infrastructure and deployment requirements.
Deployment
Production deployment has not yet been completed. The deployment strategy will be determined as the application progresses toward production.

Development and AI Assistance
Provia was independently conceived and developed by its founder.

AI tools were used as development assistants throughout the project for activities such as:
- Code generation.
- Architectural exploration.
- Technical research.
- Design alternatives.
- Debugging assistance.
- Reviewing implementation approaches.
However, AI assistance did not replace the founder's responsibility for the project.

The founder retained responsibility for:
- Defining the product vision.
- Selecting the technologies.
- Establishing the system architecture.
- Evaluating and selecting suitable design approaches.
- Directing implementation.
- Reviewing and validating generated code.
- Integrating the microservices.
- Testing application workflows.
- Making technical and architectural decisions.
AI-generated code and design suggestions were evaluated against the requirements and long-term vision of Provia before being adopted.

Project Attribution
Founder | Sole Developer | Systems Architect | Backend Developer

Provia is an independently developed project.

The founder is responsible for the product vision, system architecture, backend implementation, technology selection, technical decisions, integration and validation.

The project is currently focused on backend development and will progress toward full-stack development as the frontend is integrated.

Repository
GitHub: https://github.com/I-am-Next/PROVIA

Provia