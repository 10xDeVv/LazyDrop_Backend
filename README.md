# LazyDrop Server

LazyDrop is a real-time file sharing and collaboration platform. This repository contains the backend server built with Spring Boot, providing RESTful APIs and WebSocket support for session management, file transfers, and real-time notifications.

## 🚀 Overview

The LazyDrop Server acts as the central hub for:
- Managing **Drop Sessions**: Temporary or persistent rooms for sharing.
- **File Orchestration**: Integrating with Supabase for secure file storage and signed URLs.
- **Real-time Collaboration**: Using WebSockets (STOMP) to notify participants of new files, notes, or join/leave events.
- **Subscription & Billing**: Managing user tiers (Pro, Plus) via Stripe integration.
- **Identity & Security**: Authenticating users and guests using Supabase JWTs.

## 🛠️ Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.1
- **Build Tool**: Maven
- **Database**: PostgreSQL (JPA/Hibernate)
- **Security**: Spring Security with OAuth2 Resource Server (Supabase JWT)
- **Real-time**: Spring WebSocket (STOMP)
- **Storage**: Supabase Storage
- **Payments**: Stripe

## 📋 Requirements

- Java 21 or higher
- Maven 3.9+
- PostgreSQL database
- Supabase account (for Auth and Storage)
- Stripe account (for payments)

## ⚙️ Setup & Run

### 1. Clone the repository
```bash
git clone <repository-url>
cd Server
```

### 2. Configure Environment Variables
Create a `.env` file or set the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/lazydrop` |
| `DB_USER` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | - |
| `SUPABASE_URL` | Your Supabase project URL | - |
| `SUPABASE_ANON_KEY` | Supabase anonymous key | - |
| `SUPABASE_JWT_SECRET` | Supabase JWT secret (for token validation) | - |
| `SUPABASE_SERVICE_KEY` | Supabase service role key | - |
| `SUPABASE_BUCKET_NAME` | Supabase storage bucket name | - |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed origins | - |
| `APP_FRONTEND_URL` | URL of the frontend application | `http://localhost:3000` |
| `STRIPE_TEST_SECRET_KEY`| Stripe secret key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret | - |
| `STRIPE_PRICE_PRO` | Stripe Price ID for Pro plan | - |
| `STRIPE_PRICE_PLUS` | Stripe Price ID for Plus plan | - |

### 3. Build the application
```bash
./mvnw clean install
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080/api/v1`.

## 📜 Scripts

- `./mvnw clean install`: Build the project and run tests.
- `./mvnw spring-boot:run`: Start the application in development mode.
- `./mvnw test`: Run unit and integration tests.

## 📁 Project Structure

```text
src/main/java/com/lazydrop
├── auth/               # Identity resolution and authentication logic
├── common/             # Shared exceptions, DTOs, and utilities
├── config/             # Application-wide configurations (Stripe, AppConfig)
├── modules/            # Domain-driven modules
│   ├── billing/        # Stripe payment processing and webhooks
│   ├── session/        # Core session management
│   │   ├── core/       # Session creation, lookup, QR generation
│   │   ├── file/       # File upload/download orchestration
│   │   ├── note/       # Shared notes within sessions
│   │   └── participant/# Session membership and settings
│   ├── subscription/   # User tier and plan management
│   ├── user/           # User profile and guest management
│   └── websocket/      # WebSocket configuration and notification service
├── security/           # Spring Security configuration and JWT filters
└── utility/            # General helpers (e.g., Code generation)
```

## 🧪 Testing & Quality Assurance

### 📬 Postman API Suite
To ensure the reliability of the LazyDrop ecosystem, we use a structured Postman Collection to "battle-test" our endpoints.

#### **1. Environment Setup**
Set up a Postman Environment with the following variables:
- `baseUrl`: `http://localhost:8080/api/v1`
- `jwt_token`: Your Supabase Auth Token (Bearer).
- `session_id`: UUID of an active drop session.
- `session_code`: The 6-character short code for a session.

#### **2. Collection Structure**
| Folder | Focus | Key Scenarios to Test |
|--------|-------|-----------------------|
| **01. Auth & Identity** | Authentication | Guest cookie generation; JWT resolution; Anonymous to Authenticated upgrade. |
| **02. Drop Sessions** | Lifecycle | Create session; Fetch by code; QR code generation; Session termination. |
| **03. Participants** | Membership | Joining via code; Leaving session; Updating nickname; Participant limit enforcement. |
| **04. File Sharing** | Orchestration | 2-Step Upload (Get Signed URL -> Confirm); Signed Download URLs; File count limits. |
| **05. Subscriptions**| Billing | Checkout session creation; Plan status verification; Stripe Portal redirection. |
| **06. Real-time** | WebSockets | Connect to `/ws` (STOMP); Subscribe to `/topic/session/{id}`; Verify event payloads. |

---

### 🛡️ Battle Testing (Edge Cases)
Don't just test the "happy path". A robust system handles the unexpected:

1.  **Plan Limit Breaches**:
    *   **File Size**: Attempt to upload a 100MB file on a 50MB limit plan.
    *   **Participant Cap**: Try to join a "Full" session.
    *   **Session Bloat**: Attempt to create a 6th session when the plan limit is 5.
2.  **Concurrency & Race Conditions**:
    *   **Double Join**: Rapidly click "Join" to see if duplicate participants are created.
    *   **Simultaneous Upload**: Two users confirming the same file path (if path generation isn't unique).
3.  **Security Isolation**:
    *   **Cross-Session Access**: Use a token from Session A to try and download a file from Session B.
    *   **Unauthorized Close**: Try to `DELETE` a session as a regular participant (only owner should allowed).
4.  **Resilience**:
    *   **Invalid Tokens**: Use expired or malformed JWTs.
    *   **Orphaned Files**: Simulate a user getting an upload URL but never calling `/confirm`.

---

### 🧑‍💻 Senior Engineer Insights & Concerns

As a Senior Engineer, these are the architectural "gotchas" we must monitor:

*   **Non-Atomic File Uploads**: Our 2-step process (Cloud Storage then Metadata Confirm) can lead to **Orphaned Objects** in Supabase if the second step fails. We need a background "Sweeper" service to clean up unconfirmed files older than 24h.
*   **Idempotency is Key**: Ensure that `/confirm` and `/join` operations are idempotent. In a distributed system, retries are common; the server must handle "already confirmed" or "already joined" gracefully without returning 500s.
*   **WebSocket Backpressure**: In very active sessions (e.g., 20+ people rapidly adding notes), the message volume on `/topic/session/{id}` can spike. Monitor the STOMP broker performance and consider message throttling if latency increases.
*   **Stripe Webhook Security**: Never trust the payload of a billing webhook without verifying the `Stripe-Signature`. This is our source of truth for revenue-impacting plan changes.
*   **Database Contention**: Under high load, joining/leaving sessions involves row-level locks on the `participants` table. Ensure we have proper indexing on `(session_id, user_id)` to keep these operations fast and avoid deadlocks.
*   **Observability**: Look out for a high volume of `PlanLimitExceededException`. While technically a "success" (the system blocked it), a high frequency might indicate a confusing UI or a bot trying to scrape the service.

---

## 📘 Design Documentation (API & Resources)

This section provides a comprehensive guide for the frontend team to integrate with the backend.

### Authentication
The server uses **Supabase JWTs**.
- All authenticated requests must include the header: `Authorization: Bearer <JWT_TOKEN>`.
- Guests are also supported and resolved via JWT or internal session identification.

### Resources

#### 1. Drop Session (`/sessions`)
A session is a workspace where files and notes are shared.
- **Create Session**: `POST /sessions` -> Returns `DropSessionResponse`.
- **Get by Code**: `GET /sessions/code/{code}` -> Find a session using its short code.
- **Get by ID**: `GET /sessions/{sessionId}`
- **Get QR Code**: `GET /sessions/{sessionId}/qr` -> Returns a Base64 encoded QR code.
- **Delete Session**: `DELETE /sessions/{sessionId}` -> Closes the session.
- **My Active Sessions**: `GET /sessions/active`

#### 2. Participants (`/sessions/{sessionId}/participants`)
Manages users within a session.
- **Join**: `POST /sessions/{sessionId}/participants`
- **Leave**: `DELETE /sessions/{sessionId}/participants`
- **List**: `GET /sessions/{sessionId}/participants`
- **Get My Settings**: `GET /sessions/{sessionId}/participants/me/settings`
- **Update My Settings**: `PATCH /sessions/{sessionId}/participants/me/settings` -> (e.g., update nickname).

#### 3. Files (`/sessions/{sessionId}/files`)
Handles file metadata and provides signed URLs for Supabase Storage.
- **Get Upload URL**: `POST /sessions/{sessionId}/files/upload-url`
    - Body: `{ "fileName": String, "contentType": String, "fileSize": Long }`
    - Returns: Signed URL for direct upload to Supabase.
- **Confirm Upload**: `POST /sessions/{sessionId}/files/confirm`
    - Body: `{ "fileName": String, "storagePath": String, "fileSize": Long, "contentType": String }`
- **Get Download URL**: `GET /sessions/{sessionId}/files/{fileId}/download`
- **Mark Downloaded**: `POST /sessions/{sessionId}/files/{fileId}/mark-downloaded`
- **List Files**: `GET /sessions/{sessionId}/files`

#### 4. Notes (`/sessions/{sessionId}/notes`)
Shared text snippets.
- **Create Note**: `POST /sessions/{sessionId}/notes` -> Body: `{ "content": String }`
- **List Notes**: `GET /sessions/{sessionId}/notes?limit=50`

#### 5. Subscriptions (`/subscriptions`)
- **Get Status**: `GET /subscriptions`
- **Checkout**: `POST /subscriptions/checkout` -> Body: `{ "plan": "PRO" | "PLUS" }` -> Returns Stripe Checkout URL.
- **Cancel**: `POST /subscriptions/cancel`
- **Reactivate**: `POST /subscriptions/reactivate`
- **Billing Portal**: `POST /subscriptions/portal` -> Returns Stripe Customer Portal URL.

### Real-time Notifications (WebSockets)
- **Endpoint**: `/ws` (supports SockJS)
- **Topic**: `/topic/session/{sessionId}`
- **Events**:
    - `PEER_JOIN`: A new participant joined.
    - `PEER_LEAVE`: A participant left.
    - `FILE_UPLOADED`: A new file is available.
    - `NOTE_CREATED`: A new note was added.
    - `SESSION_CLOSED`: The session has been terminated.

Honest Critique (The 1/10 Points You Lost)
Minor issues:

No file preview - Users download before seeing content
No typing indicators - Chat could feel more "live"
No drag-drop UI mention - Should be in docs for frontend team

## ⚖️ License
[TODO: Insert License Info, e.g., MIT]
