# Droply Server

> **Real-time collaborative file sharing platform built with Spring Boot**
> Originally developed as a production SaaS product, now open-sourced as a portfolio project demonstrating full-stack backend engineering with modern Java.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📖 About

Droply is a real-time file sharing and collaboration platform that enables users to create temporary "drop sessions" for seamless file exchange. This repository contains the complete backend implementation featuring:

- **Real-time collaboration** via WebSocket (STOMP protocol)
- **Secure file orchestration** using Supabase Storage with signed URLs
- **Subscription management** integrated with Stripe
- **JWT-based authentication** via Supabase Auth
- **Production-grade reliability** with idempotent operations and scheduled cleanup jobs

This project showcases enterprise-level backend architecture, from handling webhook idempotency to implementing plan enforcement and real-time notifications—all the challenges you'd face building a real SaaS product.

---

## 🎯 Key Features

### Core Functionality
- **Drop Sessions**: Create temporary or persistent collaboration rooms with shareable codes/QR codes
- **File Management**: Two-phase upload system (signed URL generation + confirmation) preventing orphaned files
- **Real-time Notifications**: WebSocket-based live updates for participants
- **Note Sharing**: Quick text sharing within sessions
- **Participant Management**: Role-based access (owner/participant) with per-user settings

### Backend Engineering Highlights
- **Idempotent Operations**: Retry-safe endpoints for file confirmations and participant joins
- **Plan Enforcement**: Server-side subscription limits (file size, session count, participant limits)
- **Webhook Reliability**: Stripe webhook processing with deduplication and retry logic
- **Scheduled Jobs**: Automated cleanup of expired sessions and unconfirmed uploads
- **Security**: JWT validation, CORS enforcement, Stripe signature verification

---

## 🏗️ Architecture

### System Design

```
┌─────────────────┐
│   Client Apps   │
│  (Web/Mobile)   │
└────────┬────────┘
         │
         ├─────── REST API ─────────┐
         └───── WebSocket ──────────┤
                                    │
         ┌──────────────────────────▼──────────────────────┐
         │         Spring Boot Application                  │
         │                                                   │
         │  ┌────────────────────────────────────────────┐  │
         │  │          Controller Layer                  │  │
         │  │  Sessions │ Files │ Notes │ Subscriptions │  │
         │  └──────────────────┬───────────────────────┘  │
         │                     │                           │
         │  ┌──────────────────▼───────────────────────┐  │
         │  │          Service Layer                   │  │
         │  │  Business Logic │ Validation │ Security  │  │
         │  └──────────────────┬───────────────────────┘  │
         │                     │                           │
         │  ┌──────────────────▼───────────────────────┐  │
         │  │      Repository Layer (JPA/Hibernate)    │  │
         │  └──────────────────┬───────────────────────┘  │
         └────────────────────┬┘───────────────────────────┘
                              │
         ┌────────────────────▼────────────────────┐
         │          PostgreSQL Database            │
         └─────────────────────────────────────────┘

External Services:
├── Supabase (Auth + Storage)
└── Stripe (Billing + Webhooks)
```

### Module Structure

The codebase follows a **domain-driven modular architecture**:

```
com.lazydrop.modules/
├── session/
│   ├── core/          # Session lifecycle management
│   ├── file/          # File upload/download orchestration
│   ├── note/          # Text note sharing
│   └── participant/   # Participant management
├── billing/           # Stripe webhook processing
├── subscription/      # Subscription & plan management
├── user/              # User identity & profiles
├── storage/           # Supabase Storage client
└── websocket/         # Real-time notifications
```

Each module is self-contained with its own:
- **Controllers** (REST endpoints)
- **Services** (business logic)
- **Repositories** (data access)
- **Models** (JPA entities)
- **DTOs** (request/response contracts)
- **Mappers** (entity ↔ DTO conversion)

---

## 🛠️ Technology Stack

| Layer | Technologies |
|-------|-------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.1 |
| **Security** | Spring Security + OAuth2 Resource Server |
| **Database** | PostgreSQL (via JPA/Hibernate) |
| **Migrations** | Flyway |
| **Real-time** | Spring WebSocket (STOMP) |
| **Storage** | Supabase Storage (signed URLs) |
| **Payments** | Stripe (webhooks, subscriptions) |
| **Build Tool** | Maven 3.9+ |
| **Deployment** | Docker, Heroku-ready |

### Key Dependencies
- **Lombok** - Reduce boilerplate
- **Jackson** - JSON serialization
- **OkHttp** - HTTP client for Supabase
- **ZXing** - QR code generation
- **Spring Actuator** - Health checks

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 14+
- Supabase account (free tier works)
- Stripe account (test mode)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/droply-server.git
cd droply-server
```

2. **Set up environment variables**

Create a `.env` file or export these variables:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/droply
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_db_password

# Supabase
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your_anon_key
SUPABASE_JWT_SECRET=your_jwt_secret
SUPABASE_SERVICE_KEY=your_service_role_key
SUPABASE_BUCKET_NAME=drop-files

# CORS & Frontend
CORS_ALLOWED_ORIGINS=http://localhost:3000
APP_FRONTEND_URL=http://localhost:3000

# Stripe
STRIPE_TEST_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_PRICE_PRO=price_...
STRIPE_PRICE_PLUS=price_...
```

3. **Build the project**
```bash
./mvnw clean install
```

4. **Run database migrations**
```bash
# Migrations run automatically on startup via Flyway
```

5. **Start the server**
```bash
./mvnw spring-boot:run
```

Server will be available at `http://localhost:8080/api/v1`

### Docker Deployment

```bash
docker build -t droply-server .
docker run -p 8080:8080 --env-file .env droply-server
```

---

## 📚 API Documentation

### Sessions

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sessions` | POST | Create new drop session |
| `/sessions/{id}` | GET | Get session details |
| `/sessions/code/{code}` | GET | Find session by join code |
| `/sessions/{id}` | DELETE | End session (owner only) |
| `/sessions/{id}/qr` | GET | Generate QR code for session |
| `/sessions/active` | GET | List user's active sessions |

### Files

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sessions/{id}/files/upload-url` | POST | Get signed upload URL |
| `/sessions/{id}/files/confirm` | POST | Confirm file upload (idempotent) |
| `/sessions/{id}/files/{fileId}/download` | GET | Get signed download URL |
| `/sessions/{id}/files` | GET | List session files |

### Participants

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sessions/{id}/participants` | POST | Join session |
| `/sessions/{id}/participants` | DELETE | Leave session |
| `/sessions/{id}/participants` | GET | List participants |
| `/sessions/{id}/participants/me/settings` | PATCH | Update user settings |

### Notes

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sessions/{id}/notes` | POST | Create note |
| `/sessions/{id}/notes` | GET | List session notes |

### Subscriptions

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/subscriptions` | GET | Get current subscription |
| `/subscriptions/checkout` | POST | Create Stripe checkout session |
| `/subscriptions/cancel` | POST | Cancel subscription |
| `/subscriptions/reactivate` | POST | Reactivate cancelled subscription |
| `/subscriptions/portal` | POST | Get Stripe billing portal URL |

### Webhooks

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/webhooks/stripe` | POST | Stripe webhook handler |

---

## 🔐 Security

### Authentication Flow
1. User authenticates via Supabase Auth (frontend)
2. Frontend receives JWT from Supabase
3. JWT included in `Authorization: Bearer <token>` header
4. Backend validates JWT using Supabase public key
5. User identity extracted to `UserPrincipal`

### Authorization
- **Owner-only operations**: Delete session, manage participant settings
- **Participant operations**: Upload files, create notes, leave session
- **Plan enforcement**: Server-side limits on file size, session count, participant limits

### Data Security
- File access via time-limited signed URLs (5-minute expiry)
- Stripe webhook signature verification
- CORS restrictions enforced
- No sensitive data in logs (JWTs, API keys redacted)

---

## 💡 Engineering Highlights

### 1. Two-Phase File Upload
**Problem**: Users might request upload URLs but never upload, creating orphaned database records.

**Solution**: Two-phase process
- Phase 1: Generate signed URL, create pending file record
- Phase 2: User uploads to Supabase, calls `/confirm` endpoint
- Idempotent confirmation prevents duplicate processing
- Scheduled job cleans up unconfirmed uploads after 1 hour

### 2. Webhook Idempotency
**Problem**: Stripe may retry webhook deliveries, causing duplicate processing.

**Solution**:
- Store webhook event ID in database
- Check for duplicates before processing
- Track processing status (pending/processed/failed)
- Retry failed webhooks with exponential backoff

### 3. Plan Enforcement
**Problem**: Users could bypass frontend limits by calling APIs directly.

**Solution**:
- All plan limits validated server-side
- Checks performed before resource creation
- Graceful error responses with upgrade prompts
- Plan limits: file size, session count, participants per session

### 4. Session Lifecycle Management
**Problem**: Abandoned sessions waste resources and confuse users.

**Solution**:
- Scheduled job runs every 5 minutes
- Expires sessions past their time limit
- Marks inactive sessions (all participants disconnected for 30+ minutes)
- Owner can manually end sessions
- Graceful cleanup with participant notifications

### 5. Real-time Synchronization
**Problem**: Multiple participants need instant updates on file uploads, notes, joins/leaves.

**Solution**:
- WebSocket STOMP protocol
- Topic-based subscriptions (`/topic/session/{sessionId}`)
- Typed message system (FILE_UPLOADED, PARTICIPANT_JOINED, etc.)
- Automatic reconnection handling on frontend

---

## 🧪 Testing

### Running Tests
```bash
./mvnw test
```

### Test Coverage
- **Unit tests**: Service layer business logic
- **Controller tests**: MockMvc for endpoint validation
- **Security tests**: JWT validation, authorization
- **Edge cases**: Plan limits, idempotency, expired sessions

### Future Testing Enhancements
- Integration tests with Testcontainers
- Load testing for high-participant sessions
- Contract testing for API stability

---

## 📊 Database Schema

### Core Entities

```sql
users
├── id (UUID, PK)
├── supabase_user_id (UUID)
├── email
├── guest (boolean)
└── created_at

drop_session
├── id (UUID, PK)
├── owner_id (FK → users.id)
├── code (unique 6-char)
├── created_at
├── expires_at
├── ended_at
├── status (ACTIVE/EXPIRED/ENDED)
└── end_reason

drop_session_participants
├── id (UUID, PK)
├── drop_session_id (FK)
├── user_id (FK)
├── role (OWNER/PARTICIPANT)
├── joined_at
├── disconnected_at
└── auto_download (boolean)

drop_file
├── id (UUID, PK)
├── drop_session_id (FK)
├── uploader (FK → participants.id)
├── storage_path
├── original_name
├── size_bytes
└── created_at

subscriptions
├── id (UUID, PK)
├── user_id (FK)
├── stripe_customer_id
├── stripe_subscription_id
├── plan_code (FREE/PLUS/PRO)
├── status
└── current_period_end
```

---

## 🔧 Configuration

### Application Properties

Key configurations in `application.yml`:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 15
      minimum-idle: 2
  jpa:
    hibernate:
      ddl-auto: none  # Flyway handles schema
  flyway:
    enabled: true
    baseline-on-migrate: true

supabase:
  url: ${SUPABASE_URL}
  jwt-secret: ${SUPABASE_JWT_SECRET}
  bucket-name: ${SUPABASE_BUCKET_NAME}

stripe:
  webhook-secret: ${STRIPE_WEBHOOK_SECRET}
  success-url: ${app.frontend-url}/checkout/success
  cancel-url: ${app.frontend-url}/checkout/cancel
```

---

## 📈 Performance Considerations

### Database Optimization
- **Connection pooling**: HikariCP with tuned pool sizes
- **Indexed queries**: Foreign keys, unique constraints on hot paths
- **Lazy loading**: JPA relationships optimized for N+1 prevention

### File Handling
- **No file storage on server**: All files in Supabase Storage
- **Signed URLs**: Offload bandwidth to Supabase CDN
- **Streaming downloads**: No memory buffering

### Scalability
- **Stateless design**: Horizontal scaling ready
- **WebSocket sticky sessions**: Load balancer configuration required
- **Database migrations**: Zero-downtime with Flyway

---

## 🚧 Known Limitations

- WebSocket connections require sticky sessions in multi-instance deployments
- File upload size limited by Supabase Storage (5GB default)
- No file virus scanning (would require additional service)
- Guest user cleanup not implemented (accounts persist)

---

## 🤝 Contributing

While this is primarily a portfolio project, suggestions and feedback are welcome! Feel free to:
- Open issues for bugs or enhancement ideas
- Fork and experiment with the codebase
- Use this as a reference for your own projects

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🎓 Learning Outcomes

Building this project provided hands-on experience with:

✅ **Spring Boot ecosystem** - Security, WebSocket, JPA, Actuator
✅ **RESTful API design** - Resource modeling, versioning, error handling
✅ **Database design** - Schema normalization, migrations, query optimization
✅ **Real-time systems** - WebSocket protocol, message broadcasting
✅ **Payment integration** - Stripe subscriptions, webhook handling
✅ **Cloud services** - Supabase Auth/Storage, external API integration
✅ **Security** - JWT validation, CORS, signature verification
✅ **Production patterns** - Idempotency, scheduled jobs, error handling
✅ **DevOps** - Docker, environment configuration, logging

---

## 📧 Contact

Built by [Your Name]
🔗 [LinkedIn](https://linkedin.com/in/yourprofile) | 💻 [GitHub](https://github.com/yourusername) | 🌐 [Portfolio](https://yourwebsite.com)

---

<div align="center">
  <sub>Originally built as a production SaaS product. Open-sourced to showcase full-stack backend engineering with modern Java and Spring Boot.</sub>
</div>
