# Account Service
Responsible for authentication and authorization. Produces JWT-tokens and sends event to send authenticated-related emails.

---

## Interactions

```mermaid
flowchart TB
    subgraph boundary [System Boundary]
        AS[("🔐 Account Service<br/><i>Handles user registration,<br/>authentication & profile</i>")]
    end

    User["👤 User<br/><i>End user of the system</i>"]
    Client["📱 Client Application<br/><i>Web/Mobile App</i>"]
    Gateway["🚪 Gateway"]
    Kafka["Kafka"]
    MS["📧 Mail Service<br/><i>Sends registration<br/>confirmation emails</i>"]

    User -->|"Uses"| Client
    Client --> |"REST API"|Gateway
    Gateway --> |"JSON/Form-encoded"| AS
    AS -->|"Publishes to<br/>EMAIL_SENDING_TASKS"| Kafka
    MS --> |"Consumes EMAIL_SENDING_TASKS"| Kafka

    style AS fill:#1168bd,stroke:#0b4884,color:#fff
    style User fill:#08427b,stroke:#052e56,color:#fff
    style Gateway fill:#FFF2C5,stroke:#FFD2C6,color:#000000
    style Client fill:#438dd5,stroke:#2e6295,color:#fff
    style MS fill:#999999,stroke:#666666,color:#fff
```

---

## Database Schema

```mermaid
erDiagram
    USERS {
        bigint user_id PK "Primary Key, Auto-increment"
        varchar(100) username UK "Unique, Not Null, Indexed"
        varchar(255) email UK "Unique, Not Null, Indexed"
        varchar(128) password "Not Null, Hashed"
    }
```

---

## Use case flows

### Registration

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Account Service API
    participant V as Validation
    participant DB as Database
    participant MQ as Message Queue
    participant MS as Mail Service

    C->>+API: POST /api/auth/user<br/>{username, email, password}
    API->>+V: Validate request
    V-->>-API: Validation result

    alt Invalid Input
        API-->>C: 400 Bad Request<br/>{"message": "error_message"}
    else Valid Input
        API->>+DB: Check existing user
        DB-->>-API: Query result

        alt User Exists
            API-->>C: 409 Conflict<br/>{"message": "User already exists"}
        else User Not Exists
            API->>+DB: INSERT user
            DB-->>-API: User created
            API->>+MQ: Publish SendWelcomeEmail message to email_commands topic
            MQ-->>-API: Acknowledged
            MQ--)MS: Async: Send welcome email
            API-->>C: 201 Created<br/>Body: Authorization: Bearer <token>
        end
    end

    Note over API,DB: Already authenticated users<br/>receive 400 Bad Request

    opt Server Error
        API-->>C: 500 Internal Server Error<br/>{"message": "error_message"}
    end
```

### Logging in

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Account Service API
    participant AUTH as Auth Module
    participant DB as Database

    C->>+API: POST /auth/login<br/>{username, password}
    API->>+AUTH: Authenticate credentials
    AUTH->>+DB: Find user by username
    DB-->>-AUTH: User record

    alt User Not Found
        AUTH-->>API: Authentication failed
        API-->>C: 401 Unauthorized<br/>{"message": "Invalid credentials"}
    else User Found
        AUTH->>AUTH: Verify password hash
        alt Password Invalid
            AUTH-->>API: Authentication failed
            API-->>C: 401 Unauthorized<br/>{"message": "Invalid credentials"}
        else Password Valid
            AUTH->>AUTH: Generate JWT token
            AUTH-->>-API: Token generated
            API-->>-C: 200 OK<br/>Body: Authorization: Bearer <token>
        end
    end
```

### Get user info

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Account Service API
    participant AUTH as Auth Module
    participant DB as Database

    C->>+API: GET /api/auth/user<br/>Headers: Authorization: Bearer <token>
    API->>+AUTH: Validate JWT token

    alt No Token / Invalid Token
        AUTH-->>API: Token invalid
        API-->>C: 401 Unauthorized<br/>{"message": "Authentication required"}
    else Token Valid
        AUTH->>AUTH: Extract user ID from token
        AUTH->>+DB: Find user by ID
        DB-->>-AUTH: User record

        alt User Not Found
            AUTH-->>API: User not found
            API-->>C: 401 Unauthorized<br/>{"message": "User not found"}
        else User Found
            AUTH-->>-API: User data
            API-->>-C: 200 OK<br/>{"id": 1, "email": "user@example.com"}
        end
    end
```

### Logging out

Happens on frontend-side as authentication is stateless JWT-based