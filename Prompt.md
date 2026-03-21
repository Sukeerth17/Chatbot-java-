MASTER PROJECT PROMPT — AI Chatbot Microservice
================================================

CONTEXT
-------
I am building a production-grade AI chatbot microservice that plugs into an 
existing enterprise application. The existing project is built with React 
frontend, Java Spring Boot backend, and MySQL database following a microservices 
architecture. The chatbot is a brand new standalone microservice that integrates 
with the existing system.

COMPANY
-------
Company name   : Technobuild (package: in.technobuild)
Project name   : chatbot-service
Port           : 8085

TECH STACK — COMPLETE
---------------------
Language            : Java 17
Framework           : Spring Boot 3.2.3
Build tool          : Maven
Database            : MySQL 8 (two schemas — chatbot_db and existing business_db)
Cache               : Redis 7 (chat history, semantic cache, rate limiting)
Message broker      : Apache Kafka 3.6 (KRaft mode — no ZooKeeper)
AI models           : Ollama (local, localhost:11434)
                       - Llama 3.1 8B Q4_K_M  → chat and RAG answers
                       - SQLCoder 7B Q4_K_M   → SQL generation only
                       - nomic-embed-text      → embeddings for RAG
Python AI service   : FastAPI (localhost:8000)
                       - /embed               → text to vector
                       - /rerank              → cross-encoder reranking
                       - /chunk               → document chunking
                       - /confidence          → hallucination detection
                       - /parse-pdf           → PDF text extraction
Infrastructure      : AWS g4dn.xlarge (NVIDIA T4 GPU, 16GB VRAM, ap-south-1)
Migrations          : Flyway
Circuit breaker     : Resilience4j
Rate limiting       : Bucket4j
Token counting      : jTokkit
Observability       : Micrometer + Prometheus + OpenTelemetry
Containerisation    : Docker + Docker Compose
CI/CD               : GitHub Actions


MAVEN DEPENDENCIES — ALREADY ADDED TO POM.XML
----------------------------------------------
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-actuator
- spring-boot-starter-validation
- spring-boot-starter-webflux       (WebClient for Ollama + Python calls)
- spring-kafka
- spring-boot-starter-data-redis
- mysql-connector-j
- flyway-core
- flyway-mysql
- jjwt-api 0.11.5
- jjwt-impl 0.11.5
- jjwt-jackson 0.11.5
- resilience4j-spring-boot3 2.1.0
- bucket4j-core 8.7.0
- tika-core 2.9.1
- tika-parsers-standard-package 2.9.1
- jtokkit 0.6.1
- micrometer-registry-prometheus
- micrometer-tracing-bridge-otel
- opentelemetry-exporter-otlp 1.32.0
- lombok
- spring-boot-starter-test
- spring-kafka-test
- spring-security-test


COMPLETE FILE STRUCTURE
-----------------------
src/main/java/in/technobuild/chatbot/

  ChatbotApplication.java

  controller/
    ChatController.java           POST /api/chat — receives message, publishes to Kafka, opens SSE
    DocumentController.java       POST /api/docs/upload — admin document ingestion
    FeedbackController.java       POST /api/feedback — thumbs up/down per message
    HealthController.java         GET /health, GET /ready — all dependency checks

  kafka/
    producer/
      ChatRequestProducer.java    publishes to chat.requests topic
      SqlRequestProducer.java     publishes to sql.requests topic
      IngestionProducer.java      publishes to document.ingestion topic
    consumer/
      ChatConsumer.java           reads chat.requests, calls Llama, streams via SSE
      SqlConsumer.java            reads sql.requests, calls SQLCoder, validates, executes
      IngestionConsumer.java      reads document.ingestion, chunks and embeds documents
      ModelHealthConsumer.java    reads model.health.events from watchdog
    model/
      ChatRequestEvent.java       Kafka message payload for chat
      SqlRequestEvent.java        Kafka message payload for SQL
      IngestionEvent.java         Kafka message payload for document jobs

  service/
    ChatOrchestrationService.java  main flow coordinator
    ModelRouterService.java        decides chat path vs SQL path by intent detection
    ConversationService.java       load and save Redis history, trim to 8 messages
    RagService.java                embed query, hybrid search, rerank, return top 3 chunks
    PromptBuilderService.java      assembles system prompt + docs + history + question
    OllamaService.java             calls Ollama /api/chat with WebClient, streams tokens
    SqlGenerationService.java      calls SQLCoder via Ollama for SQL generation
    SqlValidationService.java      5 guards: SELECT-only, LIMIT 100, whitelist, EXPLAIN, parameterize
    SqlExecutionService.java       executes validated SQL on business_db with readonly user
    PiiScrubberService.java        masks Aadhaar, phone numbers, email, PAN before processing
    RateLimiterService.java        Bucket4j per-user rate limiting using Redis
    TokenCounterService.java       jTokkit accurate token counting for context trimming
    SemanticCacheService.java      Redis cache for repeated questions using embedding similarity
    FeedbackService.java           save ratings, flag bad answers for weekly review
    DocumentIngestionService.java  parse PDF/DOCX with Tika, chunk, embed, store in MySQL
    HallucinationGuardService.java keyword cross-check between answer and retrieved chunks
    WatchdogService.java           ping Ollama every 30s, auto-restart on failure, publish health events
    CostTrackerService.java        track daily token usage per user, enforce 50k daily limit

  client/
    PythonAiClient.java            WebClient wrapper for all Python FastAPI endpoints

  sse/
    SseEmitterRegistry.java        ConcurrentHashMap<String, SseEmitter> keyed by messageId
    SseEventPublisher.java         pushes tokens to correct SseEmitter as Kafka consumer receives them

  security/
    JwtAuthFilter.java             OncePerRequestFilter, validates JWT on every request
    JwtTokenProvider.java          validates token, extracts userId and role
    UserPrincipal.java             current authenticated user object

  entity/
    Conversation.java              one conversation session per user
    Message.java                   one message in a conversation (role: USER or ASSISTANT)
    VectorChunk.java               RAG document chunk with embedding stored as JSON
    Document.java                  ingested document metadata and status
    Feedback.java                  thumbs up/down per message with rating
    AuditLog.java                  append-only audit trail (never update or delete)
    TokenUsage.java                daily token budget tracking per user
    SqlAuditLog.java               every generated and executed SQL query logged

  repository/
    ConversationRepository.java
    MessageRepository.java
    VectorChunkRepository.java     includes native query for cosine similarity search
    DocumentRepository.java
    FeedbackRepository.java
    AuditLogRepository.java
    TokenUsageRepository.java
    SqlAuditLogRepository.java

  dto/
    request/
      ChatRequestDto.java          { message, sessionId }
      FeedbackRequestDto.java      { messageId, rating, comment }
      DocumentUploadDto.java       { file, category, audience }
    response/
      ChatResponseDto.java         { messageId, sessionId, status }
      HealthResponseDto.java       { mysql, redis, kafka, ollama, python }
      IngestionStatusDto.java      { jobId, status, chunksProcessed }

  exception/
    GlobalExceptionHandler.java    @ControllerAdvice catches all exceptions, returns friendly messages
    OllamaUnavailableException.java
    RateLimitExceededException.java
    TokenBudgetExceededException.java
    SqlValidationException.java

  config/
    KafkaConfig.java               5 topics, producer factory, consumer factory, KRaft settings
    RedisConfig.java               RedisTemplate, StringRedisTemplate, connection factory
    SecurityConfig.java            JWT filter chain, permit /health and /ready, secure everything else
    OllamaConfig.java              WebClient bean for Ollama with 120s timeout and circuit breaker
    PythonAiConfig.java            WebClient bean for Python service with 30s timeout
    DataSourceConfig.java          two DataSources: chatbot_db (read-write) and business_db (read-only)
    Resilience4jConfig.java        circuit breaker: 5 failures -> OPEN, wait 60s, half-open test

src/main/resources/
  application.yml
  application-dev.yml
  application-prod.yml
  db/migration/
    V1__create_conversations.sql
    V2__create_messages.sql
    V3__create_vector_chunks.sql
    V4__create_documents.sql
    V5__create_feedback.sql
    V6__create_audit_log.sql
    V7__create_token_usage.sql
    V8__create_sql_audit_log.sql

src/test/java/in/technobuild/chatbot/
  controller/ChatControllerTest.java
  service/ModelRouterServiceTest.java
  service/SqlValidationServiceTest.java
  service/PiiScrubberServiceTest.java
  kafka/ChatConsumerTest.java

Dockerfile
docker-compose.yml
.env.example


KAFKA TOPICS
------------
chat.requests        partitioned by user_id, retention 7 days
sql.requests         partitioned by user_id, retention 7 days
chat.responses       partitioned by user_id, retention 30 days
document.ingestion   partitioned by doc_id,  retention 30 days
model.health.events  no partition key,       retention 1 day


HOW KAFKA + SSE WORKS TOGETHER
-------------------------------
1. User sends POST /api/chat
2. ChatController creates a SseEmitter, stores it in SseEmitterRegistry keyed by messageId
3. ChatController publishes ChatRequestEvent to Kafka chat.requests topic
4. ChatController returns the SSE stream to the browser (connection stays open)
5. ChatConsumer picks up the event from Kafka
6. ChatConsumer looks up the SseEmitter from registry using messageId
7. ChatConsumer calls Ollama which streams tokens
8. For each token received, ChatConsumer calls SseEventPublisher.send(messageId, token)
9. SseEventPublisher pushes each token through the SseEmitter to the browser
10. When generation is complete, ChatConsumer closes the SseEmitter and publishes to chat.responses


HOW THE RAG PIPELINE WORKS
---------------------------
1. PythonAiClient.embed(userQuestion) → vector float array
2. VectorChunkRepository.findTopSimilar(vector, 20) → top 20 candidates from MySQL
3. BM25 full-text search on same question → top 20 keyword matches from MySQL
4. Merge both lists using Reciprocal Rank Fusion
5. PythonAiClient.rerank(question, mergedChunks, 3) → top 3 most relevant chunks
6. HallucinationGuardService checks relevance score > 0.72, otherwise reject
7. PromptBuilderService assembles: system prompt + top 3 chunks + history + question
8. OllamaService sends to Llama 3.1 8B and streams response


HOW SQL GENERATION WORKS
-------------------------
1. ModelRouterService detects SQL intent (keywords: show, list, how many, total, count, 
   find all, what is the, give me the)
2. SqlGenerationService calls SQLCoder 7B with schema + question only (NO history)
3. SqlValidationService runs 5 guards:
   a. Must start with SELECT — block UPDATE, DELETE, DROP, INSERT, TRUNCATE, ALTER
   b. Auto-append LIMIT 100 if not present
   c. Table name must be in whitelist defined in application.yml
   d. Run EXPLAIN — reject if estimated rows > 10000
   e. Use PreparedStatement with parameterized values — never string concatenation
4. SqlExecutionService runs validated SQL on business_db readonly user
5. Results passed back to Llama 3.1 8B for formatting into plain English
6. Every query logged to sql_audit_log table


SECURITY FLOW
-------------
1. Every request hits JwtAuthFilter first
2. JWT secret is the same secret used by all other microservices in the project
3. Token validated, userId and role extracted into UserPrincipal
4. UserPrincipal stored in SecurityContextHolder
5. All services access current user via SecurityContextHolder.getContext().getAuthentication()
6. PiiScrubberService runs on every user message BEFORE any processing
7. RateLimiterService checks per-user rate (configurable, default 20 messages/minute)
8. CostTrackerService checks daily token budget (default 50000 tokens/day per user)
9. InjectionGuard checks message for prompt injection patterns before publishing to Kafka


SYSTEM PROMPT STRATEGY
-----------------------
The system prompt is assembled dynamically in PromptBuilderService every request:

Section 1: Static rules (never changes) — loaded from application.yml or a constants file
Section 2: User context — injected: userId, userName, userRole, currentDateTime
Section 3: Retrieved documents — [DOCUMENTS] ... [END DOCUMENTS]
Section 4: SQL results if any — [DATA] ... [END DATA]
Section 5: Conversation history — last 8 messages from Redis
Section 6: Current question — User: {message} \n\nAssistant:

Token budget per request:
  System prompt static: ~500 tokens
  User context:         ~50 tokens
  Documents (3 chunks): ~1500 tokens
  History (8 messages): ~800 tokens
  User question:        ~100 tokens
  Total input:          ~2950 tokens (safe for Llama 3.1 8B 4k effective context)
  Reserved for output:  ~400 tokens (max_tokens setting in Ollama request)


HALLUCINATION PREVENTION
------------------------
System prompt rule: answer only from provided documents and data
Confidence check: keyword overlap between answer and retrieved chunks must be > 35%
If confidence < 35%: prepend "Note: This answer may need verification." to response
If no relevant chunks found: return "I don't have that information in my knowledge base."
Temperature for Llama chat: 0.1 (low randomness, more factual)
Temperature for SQLCoder: 0.0 (fully deterministic)


OLLAMA CONFIGURATION
--------------------
Chat model:      llama3.1:8b-instruct-q4_K_M
SQL model:       sqlcoder:7b-q4_K_M
Embed model:     nomic-embed-text
Base URL:        http://localhost:11434
Endpoint:        /api/chat (NOT /api/generate — chat endpoint applies correct template)
keep_alive:      300 seconds for Llama, 60 seconds for SQLCoder
max_tokens:      400 for chat responses, 200 for SQL generation
num_parallel:    2 (set via OLLAMA_NUM_PARALLEL=2 environment variable)
Timeout:         120 seconds read timeout, 10 seconds connect timeout
Circuit breaker: after 5 failures -> OPEN, wait 60s, try 1 request in HALF_OPEN


PYTHON AI SERVICE — ACTUAL STATE
---------------------------------
Framework      : FastAPI
AI framework   : CrewAI with Ollama
Model          : qwen3:8b (via Ollama)
Port           : 8000 (uvicorn)
Database       : pymysql (direct connection)
Cache          : Redis (session history)
Base URL       : http://localhost:8000

EXISTING ENDPOINTS (already built):
  GET  /health                     → returns {"status": "200 OK"}
  POST /getResponse                → takes prompt + uuid, returns SQL result rows
  GET  /removeSession/{uuid}       → deletes Redis session

EXISTING AGENTS (CrewAI in system_prompt.py):
  Agent 1: SchemaSelectorAgent
    - Takes schema_info + details + user_request
    - Returns strict JSON: {database, tables, columns}
    - Model: ollama/qwen3:8b

  Agent 2: SQLGeneratorAgent
    - Takes output from Agent 1 + user_request
    - Returns one valid MySQL SELECT query
    - Model: ollama/qwen3:8b

EXISTING FILES:
  app.py           FastAPI app, endpoints, Redis session management
  db.py            pymysql connection, execute_query, get_schema_info
  system_prompt.py CrewAI agents and tasks
  llm_result.py    Direct Ollama API calls (fallback, not main path)
  extract_data.py  Reads .xlsx and .csv files into dict list
  requirements.txt All Python dependencies

WHAT PYTHON CURRENTLY DOES:
  1. Receives user prompt + session uuid
  2. Loads schema from MySQL information_schema
  3. Loads table details from Excel files (CTM_table.xlsx, inventory_table_details.xlsx)
  4. Runs CrewAI 2-agent pipeline: schema select → SQL generate
  5. Executes SQL on MySQL
  6. Returns raw rows to caller
  7. Stores conversation in Redis by uuid

WHAT PYTHON NEEDS TO ADD FOR JAVA INTEGRATION:
  - POST /embed     → embed text using nomic-embed-text via Ollama
  - POST /rerank    → rerank chunks using cross-encoder
  - POST /chunk     → chunk documents with sentence-aware splitting
  - POST /confidence → check if answer is grounded in retrieved chunks
  - POST /parse-pdf  → extract text from PDF using PyMuPDF
  - GET  /ready      → full health check including Ollama + Redis + MySQL

IMPORTANT NOTES ABOUT EXISTING CODE:
  - db.py has hardcoded credentials — must be moved to .env before production
  - app.py creates MySQL connection at startup globally — should be per-request
  - extract_data.py reads Excel files at runtime on every request — should be cached
  - Redis stores raw SQL and prompts — no encryption
  - No JWT validation — Java handles auth, Python is internal only
  - CORS is open (*) — acceptable since Python is internal microservice only
  - CrewAI uses qwen3:8b not Llama 3.1 — this is the SQL generation model
  - Llama 3.1 8B is used by Java for chat/RAG answers
  - SQLCoder 7B is the backup option — currently qwen3:8b handles SQL generation

DATABASES PYTHON CONNECTS TO:
  host: 3.108.61.207
  databases: CTM, inventorymulti2
  user: sumukh (needs readonly user for production)


DATABASE SCHEMAS
----------------
chatbot_db (read-write, managed by this service):
  conversations, messages, vector_chunks, documents, 
  feedback, audit_log, token_usage, sql_audit_log

business_db (read-only, existing database):
  Accessed only by SqlExecutionService using a readonly MySQL user
  Tables accessible are defined in a whitelist in application.yml
  This service NEVER writes to business_db
  This service NEVER modifies existing tables


ENVIRONMENT VARIABLES NEEDED
-----------------------------
CHATBOT_DB_URL          jdbc:mysql://localhost:3306/chatbot_db
CHATBOT_DB_USER         chatbot_user
CHATBOT_DB_PASS         (secret)
BUSINESS_DB_URL         jdbc:mysql://localhost:3306/your_existing_db
BUSINESS_DB_USER        chatbot_readonly
BUSINESS_DB_PASS        (secret)
REDIS_HOST              localhost
REDIS_PORT              6379
KAFKA_BOOTSTRAP         localhost:9092
JWT_SECRET              (same secret as all other microservices)
OLLAMA_BASE_URL         http://localhost:11434
PYTHON_AI_BASE_URL      http://localhost:8000


CODING STANDARDS
----------------
- All classes use @Slf4j from Lombok for logging
- All entities use @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor from Lombok
- All services use constructor injection — never @Autowired on fields
- All controller methods return ResponseEntity<?>
- All exceptions are caught in GlobalExceptionHandler — no try-catch in controllers
- All database access goes through repositories — no native SQL in services except vector search
- All sensitive config values come from environment variables — nothing hardcoded
- Every public service method has a log.info at entry and log.error on exception
- Flyway migrations are numbered sequentially V1, V2, V3 — never modify an existing migration
- application-dev.yml overrides for local development (H2 or local MySQL)
- application-prod.yml overrides for production (connection pool sizes, log levels)


BUILD AND RUN ORDER
-------------------
Stage 1: Config + Security + HealthController
         Goal: app starts, /health returns 200
         Files: ChatbotApplication, all config classes, security classes, HealthController

Stage 2: Entities + Repositories + Flyway migrations
         Goal: app starts, Flyway creates all 8 tables in MySQL automatically

Stage 3: Kafka setup — topics, producers, consumers (empty shells)
         Goal: messages flow through Kafka topics without errors

Stage 4: Ollama + Python client + RAG pipeline
         Goal: basic chat message goes in, Llama response comes out via SSE

Stage 5: SQL generation + validation + execution
         Goal: data questions trigger SQLCoder, result formatted by Llama

Stage 6: All remaining services
         Goal: rate limiting, PII scrubbing, cost tracking, feedback, watchdog all working

Stage 7: Exception handling, DTOs, tests, Docker, CI/CD
         Goal: production-hardened, all edge cases handled


HOW TO USE THIS PROMPT WITH ANY AI
-----------------------------------
When asking any AI (ChatGPT, Claude, Gemini, Copilot, Cursor) to write code for 
this project, start your message with:

"Using the project context below, [your specific request]"
Then paste this entire prompt.

Example requests:
"Using the project context below, write ChatConsumer.java"
"Using the project context below, write the Flyway migration V1__create_conversations.sql"
"Using the project context below, write application.yml"
"Using the project context below, write OllamaService.java with circuit breaker"
"Using the project context below, write SqlValidationService.java with all 5 guards"
"Using the project context below, write the docker-compose.yml for local development"


WHAT NOT TO ASK ANY AI TO CHANGE
----------------------------------
Do not ask any AI to change the package name in.technobuild.chatbot
Do not ask any AI to change the port to anything other than 8085
Do not ask any AI to switch from Maven to Gradle
Do not ask any AI to upgrade Spring Boot beyond 3.2.3 without checking compatibility
Do not ask any AI to add a new dependency without updating this master prompt first
Do not ask any AI to change the JWT approach — it must reuse the same secret as existing services
Do not ask any AI to give Ollama a timeout less than 120 seconds


CURRENT STATUS
--------------
Completed:
  - pom.xml with all dependencies (Spring Boot 3.2.3)
  - Spring Initializr project structure generated
  - Master system prompt for the AI chatbot written

Next step:
  Stage 1 — write all config classes and get the app to start cleanly
  Start with: application.yml, then KafkaConfig.java, RedisConfig.java,
  DataSourceConfig.java, SecurityConfig.java, OllamaConfig.java,
  PythonAiConfig.java, Resilience4jConfig.java, HealthController.java
