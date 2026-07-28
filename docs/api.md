# API

Base mediante gateway: `http://localhost:8080/api`.

Todos los endpoints, excepto login y health, requieren `Authorization: Bearer <token>`.

## Auth

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/health`

## Estudiantes

- `POST /api/students` multipart: datos del estudiante y `photo` opcional.
- `GET /api/students?page=0&size=12&search=&status=` lista paginada.
- `GET /api/students/{id}`
- `PUT /api/students/{id}` multipart.
- `PATCH /api/students/{id}/status` con `{ "status": "ACTIVE|INACTIVE" }`.

`PATCH /status` requiere `ADMIN`.

## Hoja de vida

- `GET /api/students/{id}/resume`

Devuelve estudiante, documentos activos e historial.

## Documentos

- `POST /api/students/{id}/documents` multipart: `documentType`, `displayName`, `description`, `file`.
- `GET /api/students/{id}/documents`
- `GET /api/students/{id}/documents/{documentId}`
- `GET /api/students/{id}/documents/{documentId}/download`
- `PUT /api/students/{id}/documents/{documentId}` multipart para reemplazar.
- `DELETE /api/students/{id}/documents/{documentId}`.

`DELETE` requiere `ADMIN`.

Tipos iniciales sugeridos: Documento de identidad, Registro civil, Certificado de estudio, Afiliacion a salud, Hoja de vida firmada, Fotografia adicional y Otro. La API guarda el tipo como texto controlado por la aplicacion para permitir ampliar el catalogo.

## Historial

- `GET /api/students/{id}/history`

Registra creacion, edicion, cambio de estado, carga, reemplazo y eliminacion de documentos.
