# API inicial

Base local mediante gateway: `http://localhost:8080/api`.

## POST /api/auth/login

Solicitud:

```json
{
  "email": "secretario@fmud.local",
  "password": "Cambiar123!"
}
```

Respuesta:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "name": "Usuario Secretaría",
    "email": "secretario@fmud.local",
    "role": "SECRETARIO"
  }
}
```

## GET /api/auth/me

Requiere `Authorization: Bearer <token>`.

## GET /api/health

Retorna estado básico del gateway.

## Error estándar

```json
{
  "timestamp": "2026-07-21T13:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Los datos enviados no son válidos.",
  "path": "/api/auth/login",
  "details": []
}
```
