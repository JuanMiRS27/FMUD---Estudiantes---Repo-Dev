# API

Base mediante gateway: `http://localhost:8080/api`.

Todos los endpoints, excepto login y health, requieren `Authorization: Bearer <token>`.

## Auth

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/health`

## Estudiantes

- `POST /api/students` multipart: datos del estudiante y `photo` opcional. La foto se registra como documento activo `PHOTO`.
- `GET /api/students?page=0&size=12&search=&status=` lista paginada.
- `GET /api/students/{id}`
- `PUT /api/students/{id}` multipart.
- `PATCH /api/students/{id}/status` con `{ "status": "ACTIVE|INACTIVE" }`.

`PATCH /status` requiere `ADMIN`.

## Hoja de vida

- `GET /api/students/{id}/resume`

Devuelve estudiante, matriculas, documentos activos e historial.

## Matriculas

- `POST /api/students/{id}/enrollments` con `{ "periodCode": "2026-1", "program": "Programa Infantil", "status": "ENROLLED" }`.
- `GET /api/students/{id}/enrollments`
- `PUT /api/students/{id}/enrollments/{enrollmentId}` para actualizar periodo, programa o estado.

Estados permitidos: `ENROLLED`, `WITHDRAWN`, `COMPLETED`. Un estudiante solo puede tener una matricula por `periodCode`.

## Documentos

- `POST /api/students/{id}/documents` multipart: `documentType`, `displayName`, `description`, `file`.
- `GET /api/students/{id}/documents`
- `GET /api/students/{id}/documents/{documentId}`
- `GET /api/students/{id}/documents/{documentId}/download`
- `PUT /api/students/{id}/documents/{documentId}` multipart para reemplazar.
- `DELETE /api/students/{id}/documents/{documentId}`.

`DELETE` requiere `ADMIN`.

Tipos permitidos: `PHOTO`, `IDENTITY_DOCUMENT`, `CIVIL_REGISTRY`, `STUDY_CERTIFICATE`, `HEALTH_AFFILIATION`, `SIGNED_RESUME` y `OTHER`.

## Historial

- `GET /api/students/{id}/history`

Combina eventos directos del estudiante y eventos documentales obtenidos mediante `document_history_events -> student_documents -> students`.
