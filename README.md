# Chatbot Service

Repository: https://github.com/Sukeerth17/Technobuild-Chatbot.git

## Overview
`chatbot-service` is a Spring Boot microservice that powers secure enterprise chat, SQL Q&A, RAG document ingestion, moderation, and audit-ready observability.

## Tech Stack
- Java 17, Spring Boot 3.2.x
- Spring Web, WebFlux, Validation, Security, Actuator
- Spring Data JPA, PostgreSQL (+ pgvector for KB), Flyway
- Redis (history cache, TTL storage)
- Apache Kafka (async orchestration)
- Ollama (chat/sql/embedding models)
- Python AI service (parse/chunk/embed helpers)
- Resilience4j, Bucket4j, Prometheus/Micrometer, OpenTelemetry
- JUnit 5, Mockito, Spring Boot Test, Embedded Kafka

## Prerequisites
- Java 17
- Maven (or use `./mvnw`)
- Docker + Docker Compose
- Ollama (required for LLM inference)
- Python 3.10+

## LLM Requirement
Yes. This service requires a local LLM runtime via Ollama and will not be fully ready without the required models.

Required models:
- `llama3.1:8b-instruct-q4_K_M` (chat)
- `qwen3:8b` (SQL)
- `nomic-embed-text` (embeddings)

## Install Ollama
### macOS
Option 1 (Homebrew):
```bash
brew install --cask ollama
```

Option 2:
- Download and install from https://ollama.com/download

### Windows
Option 1 (winget):
```powershell
winget install Ollama.Ollama
```

Option 2:
- Download and install from https://ollama.com/download

After install, start Ollama:
- macOS/Linux:
  ```bash
  ollama serve
  ```
- Windows (PowerShell):
  ```powershell
  ollama serve
  ```

Download required models:
```bash
ollama pull llama3.1:8b-instruct-q4_K_M
ollama pull qwen3:8b
ollama pull nomic-embed-text
```

## Quick Start
1. Clone this repository.
2. Copy `.env.example` to `.env` and fill real values.
3. Start infrastructure:
   ```bash
   docker-compose up -d
   ```
4. Ensure Ollama is running and required models are downloaded.
5. Start Python AI service.
   If your Python AI service is in a sibling folder (`techno_build_bot-main`):
   ```bash
   cd ../techno_build_bot-main
   python -m pip install -r requirements.txt
   python -m uvicorn app:app --reload --port 8000
   ```
6. Start chatbot service:
   ```bash
   cd /path/to/chatbot-backend
   ./mvnw spring-boot:run --spring.profiles.active=dev
   ```
7. Verify readiness:
   ```bash
   curl http://localhost:8085/ready
   ```

## API Endpoints
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/chat` | SSE chat endpoint for user prompts |
| `GET` | `/api/chat/conversations` | List current user conversations |
| `GET` | `/api/chat/conversations/{sessionId}/messages` | Get messages in a conversation |
| `DELETE` | `/api/chat/conversations/{sessionId}` | Delete one conversation |
| `DELETE` | `/api/chat/history` | Delete all user conversation history |
| `POST` | `/api/feedback` | Submit feedback |
| `POST` | `/api/docs/upload` | Upload document for ingestion |
| `GET` | `/api/docs/status/{jobId}` | Track ingestion job status |
| `GET` | `/api/docs/list` | Admin list uploaded documents |
| `DELETE` | `/api/docs/{documentId}` | Delete one ingested document |
| `POST` | `/api/docs/{documentId}/re-embed` | Re-embed a document |
| `GET` | `/health` | Dependency health summary |
| `GET` | `/ready` | Readiness status for runtime dependencies |

## Environment Variables
| Name | Required | Description |
|---|---|---|
| `CHATBOT_DB_URL` | Yes | JDBC URL for chatbot DB (PostgreSQL) |
| `CHATBOT_DB_USER` | Yes | DB username for chatbot DB |
| `CHATBOT_DB_PASS` | Yes | DB password for chatbot DB |
| `BUSINESS_DB_URL` | Yes | JDBC URL for business readonly DB |
| `BUSINESS_DB_USER` | Yes | DB username for business DB |
| `BUSINESS_DB_PASS` | Yes | DB password for business DB |
| `REDIS_HOST` | Yes | Redis hostname |
| `REDIS_PORT` | Yes | Redis port |
| `KAFKA_BOOTSTRAP` | Yes | Kafka bootstrap server(s) |
| `JWT_SECRET` | Yes | Shared JWT signing secret |
| `OLLAMA_BASE_URL` | Yes | Ollama base URL |
| `PYTHON_AI_BASE_URL` | Yes | Python AI service base URL |
| `SERVER_PORT` | No | Service port (default 8085) |
| `SPRING_PROFILES_ACTIVE` | No | Active Spring profile (`dev`, `prod`) |

## Architecture
The service follows a 13-layer architecture: Controllers -> DTOs -> Security -> Validation/Guards -> Routing -> Orchestration -> Services -> Kafka Producers/Consumers -> Persistence Repositories -> Entity Model -> External AI Clients -> Observability/Audit -> Health/Readiness controls.

## Running Tests
```bash
./mvnw test
```

## Troubleshooting
- `Database authentication failed`: verify PostgreSQL user/password and DB name (`chatbot_db`).
- `Flyway validation failed`: run repair or reset the target schema history after failed migrations.
- `Kafka connection refused`: ensure `docker-compose up -d` is running and `localhost:9092` is reachable.
- `/ready` shows `DOWN` for Ollama: start `ollama serve` and pull required models.
- Python AI errors: ensure `uvicorn app:app --reload --port 8000` is running and dependencies are installed.
