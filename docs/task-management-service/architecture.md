# Task Management Service
Responsible for task creation, deletion, completion and updating task data. 

---

## Interactions

```mermaid
flowchart TB
    subgraph boundary [System Boundary]
        TMS[("📋 Task Management Service<br><i>Handles task creation, deletion,<br>completion and updating</i>")]
    end

    User["👤 User<br/><i>End user of the system</i>"]
    Client["📱 Client Application<br/><i>Web/Mobile App</i>"]
    Gateway["🚪 Gateway"]
    Kafka["Kafka"]
    MS["🔔 Notification Service<br/><i>Schedules emails for<br>results for the day</i>"]

    User -->|"Uses"| Client
    Client --> |"REST API"|Gateway
    Gateway --> |"JSON/Form-encoded"| TMS
    TMS -->|"Publishes to<br/>TASK_TOPIC"| Kafka
    MS --> |"Consumes TASK_TOPIC"| Kafka

    style TMS fill:#1168bd,stroke:#0b4884,color:#fff
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
        bigint user_id PK
        varchar username UK
    }
    
    USERS ||..|{ TASKS : has
    
    TASKS {
        bigint task_id PK "Primary Key, Auto-increment"
        bigint owner_id FK "Not Null, Foreign Key to users.user_id"
        text title "Not Null, Indexed"
        text content
        task_status status  "Not Null, Indexed, Default"
        timestamptz finished_at "Indexed, Check status = DONE"
    }
    
    TASKS }|..|| TASK_STATUS : has
    TASK_STATUS {
        enum_key CREATED
        enum_key CANCELLED
        enum_key IN_BACKLOG
        enum_key BLOCKED
        enum_key DONE
    }
```
All indexes are composite with `owner_id`.

---

## Use case flows

### List Tasks
Get all tasks assigned to user. Paginated

```postgresql
SELECT * FROM tasks
WHERE owner_id = :ownerId
OFFSET :offset LIMIT :limit
```

```mermaid
---
title: Flow for listing all tasks
---
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Task Management Service API
    participant Security as Auth Module
    participant DB as Database
    
    C->>API: GET /api/tasks/
    API->>Security: Validate JWT
    alt Unauthenticated
        Security-->>API: Validation failed
        API-->>C: 401 Unauthorized
    else Authenticated
        Security-->>API: Validated JWT
        API->>DB: Find all tasks by JWT's username
        DB-->>API: Returns list of users. Can be empty
        API-->>C: 200 OK<br>["task1": {...}...]
    end

    opt Server Error
        API-->>C: 500 Internal Server Error<br/>{"message": "error_message"}
    end
```

### Add Task

```mermaid
erDiagram
    USERS {
        bigint user_id PK
        varchar username UK
    }
    
    USERS ||..|{ TASKS : adds
    
    TASKS {
        text title
        text content
    }
```

```mermaid
---
title: Flow for adding tasks
---
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Task Management Service API
    participant Security as Auth Module
    participant V as Validation
    participant DB as Database
    participant MQ as Message Queue
    
    C->>API: POST /api/tasks/<br>{title,content}
    API->>Security: Validate JWT
    alt Unauthenticated
        Security-->>API: Validation failed
        API-->>C: 401 Unauthorized
    else Authenticated
        Security-->>API: Validated JWT
        API->>V: Validate title and content
        alt Validation failed
            V-->>API: `title` is null or empty
            API-->>C: 400 Bad Request<br>{"message": "Title shouldn't<br>be null"}
        else Validation passed
            V-->>API: 
            API->>DB: Insert new task with user_id from local `users` schema
            DB-->>API: Returns inserted new task's DB data
            
            API--)MQ: Send TaskCreated message to task_events topic
            MQ--)API: Acknowledges message
            
            API-->>C: 201 Created<br>{"id": ...}
        end
    end
    
    
    opt Server Error
        API-->>C: 500 Internal Server Error<br/>{"message": "error_message"}
    end
```

```mermaid
---
title: Kafka record for task_events topic
---
erDiagram
    TaskCreated {
        bigint owner_id
        bigint task_id
        text title
    }
```

### Task Deletion

```mermaid
erDiagram
    "Request Body" {
        bigint task_id
    }
```
```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as Task Management Service API
    participant Security as Auth Module
    participant DB as Database
    participant MQ as Message Queue

    C->>API: DELETE /api/tasks/{id}
    API->>Security: Validate JWT
    alt Unauthenticated
        Security-->>API: Validation failed
        API-->>C: 401 Unauthorized
    else Authenticated
        Security-->>API: Validated JWT
        API->>DB: Delete task with task_id for owner_id
        alt Task doesn't exist
            DB-->>API: Returns 0 as modified rows
            API-->>C: 404 Not Found<br>{"message": "Task with ID %s was not found"}
        else Task exists
            DB-->>API: Returns 1 as modified rows
            API--)MQ: Send TaskDeleted message to task_events topic
            MQ--)API: Acknowledges message
            API-->>C: 204 No Content
        end
    end
    
    
    opt Server Error
    API-->>C: 500 Internal Server Error<br/>{"message": "error_message"}
    end
```
```mermaid
---
title: Kafka record for tasks_events topic
---
erDiagram
    TaskDeleted {
        bigint task_id
    }
```

### Task Update

This action can update any part of the task, including its status. All properties except task_id are nullable, but if all are null, then the request is invalid. At least one nullable property should be set.
The request body itself can omit null values.
```mermaid
erDiagram
    rb["Request Body"] {
        bigint task_id "Not Null"
        varchar title "Nullable"
        varchar content "Nullable"
        task_status new_status "Nullable"
    }

    rb }|..|| TASK_STATUS : has
    TASK_STATUS {
        enum_key CREATED
        enum_key CANCELLED
        enum_key IN_BACKLOG
        enum_key BLOCKED
        enum_key DONE
    }
```
```mermaid
---
title: Flow for updating tasks
---
sequenceDiagram
        autonumber
    participant C as Client
    participant API as Task Management Service API
    participant Security as Auth Module
    participant V as Validation
    participant DB as Database
    participant MQ as Message Queue
    
    C->>API: POST /api/tasks/<br>{title,content}
    API->>Security: Validate JWT
    alt Unauthenticated
        Security-->>API: Validation failed
        API-->>C: 401 Unauthorized
    else Authenticated
        Security-->>API: Validated JWT
        API->>V: Validate title and content
        alt Validation failed
            V-->>API: Either `title` is empty (but is not null)<br>or empty or all parameters are null
            API-->>C: 400 Bad Request<br>{"message": "Title shouldn't<br>be null"}
        else Validation passed
            V-->>API: Continues
            API->>DB: Update task with task_id and user_id from local `users` schema
            alt Task doesn't exist
                DB->>API: Returns 0 as number of modified rows
                API->>C: 404 Not Found<br>{"message": "Task with ID %s was not found"}
            else Task exists
                DB-->>API: Returns updated task's DB data
                
                API--)MQ: Send TaskUpdated message to task_events topic
                MQ--)API: Acknowledges message
                
                API-->>C: 200 OK<br>{"id": ...}
            end
        end
    end
    
    
    opt Server Error
        API-->>C: 500 Internal Server Error<br/>{"message": "error_message"}
    end
```
```mermaid
---
title: Kafka record for tasks_events topic
---
erDiagram
    TaskUpdate {
        bigint task_id "Not Null"
        varchar title "Nullable"
        varchar content "Nullable"
        boolean is_completed "Default — false"
    }
```