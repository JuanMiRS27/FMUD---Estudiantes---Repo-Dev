# Arquitectura

El MVP conserva el monorepo con frontend Angular, API Gateway y microservicios Spring Boot solo donde son utiles para el alcance actual.

- `api-gateway`: entrada publica, CORS, validacion JWT y proxy hacia servicios internos.
- `auth-service`: autenticacion, usuarios de desarrollo, BCrypt, JWT, Flyway y PostgreSQL.
- `student-service`: gestion de estudiantes, hojas de vida, documentos, almacenamiento local desacoplado, auditoria, Flyway y PostgreSQL.
- `frontend/fmud-web`: Angular standalone, lazy loading, Reactive Forms, Signals, guards, interceptores, modo claro/oscuro y UI responsive.

## Hexagonal en student-service

- Dominio: `domain/model`, `domain/document`, `domain/history`.
- Aplicacion: comandos, DTOs, puertos de entrada/salida y casos de uso.
- Adaptadores web: controladores REST/multipart.
- Adaptadores de salida: JPA/PostgreSQL y almacenamiento local.
- Infraestructura: seguridad JWT, configuracion, excepciones.

Los controladores no contienen reglas de negocio y no se exponen entidades JPA.

## Datos y archivos

PostgreSQL usa una sola base `fmud` con schemas `auth` y `students`, gestionada con Flyway y `ddl-auto=validate`. Los archivos no se guardan como Base64: se almacenan en volumen Docker con claves fisicas unicas y se descargan mediante endpoint autorizado.

## Modelo entidad-relacion final

```mermaid
erDiagram
    AUTH_USERS ||--o{ STUDENT_DOCUMENTS : uploads
    AUTH_USERS ||--o{ STUDENT_HISTORY_EVENTS : acts
    AUTH_USERS ||--o{ DOCUMENT_HISTORY_EVENTS : acts
    STUDENTS ||--o{ ENROLLMENTS : has
    STUDENTS ||--o{ STUDENT_DOCUMENTS : owns
    STUDENTS ||--o{ STUDENT_HISTORY_EVENTS : has
    STUDENT_DOCUMENTS ||--o{ DOCUMENT_HISTORY_EVENTS : has

    AUTH_USERS {
        UUID id PK
        VARCHAR name
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR role
        BOOLEAN enabled
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    STUDENTS {
        UUID id PK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR document_number UK
        DATE birth_date
        VARCHAR birth_place
        VARCHAR address
        VARCHAR phone
        VARCHAR email
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    ENROLLMENTS {
        UUID id PK
        UUID student_id FK
        VARCHAR period_code
        VARCHAR program
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    STUDENT_DOCUMENTS {
        UUID id PK
        UUID student_id FK
        VARCHAR document_type
        VARCHAR display_name
        VARCHAR original_name
        VARCHAR storage_key
        VARCHAR content_type
        BIGINT size_bytes
        VARCHAR description
        VARCHAR status
        UUID uploaded_by_user_id FK
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    STUDENT_HISTORY_EVENTS {
        UUID id PK
        UUID student_id FK
        UUID actor_user_id FK
        VARCHAR action
        VARCHAR summary
        TIMESTAMPTZ created_at
    }

    DOCUMENT_HISTORY_EVENTS {
        UUID id PK
        UUID document_id FK
        UUID actor_user_id FK
        VARCHAR action
        VARCHAR summary
        TIMESTAMPTZ created_at
    }
```

## Seguridad

Los roles activos son `ADMIN` y `SECRETARIO`. Las restricciones se aplican en frontend para experiencia de usuario y en backend para control real.
