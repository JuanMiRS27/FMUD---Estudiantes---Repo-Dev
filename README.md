# FMUD Platform

MVP dockerizado para la Fundación Manos Unidas de Dios.

## Requisitos

- Docker Desktop con Docker Compose v2.
- Para desarrollo local fuera de Docker: Java `25`, Maven Wrapper incluido y Node `26.x` o `24.15+` para Angular 22.

## Arranque completo con Docker

Prepare variables locales:

```powershell
Copy-Item .env.example .env
```

Desde la raíz del repositorio:

```powershell
docker compose up --build
```

Comando explícito equivalente:

```powershell
docker compose -f infrastructure/docker-compose.yml --env-file .env up --build
```

Detener:

```powershell
docker compose down
```

Eliminar contenedores, red y volúmenes de desarrollo:

```powershell
docker compose down -v
```

## Servicios y URLs

- Frontend Nginx: `http://localhost:4200`
- API Gateway: `http://localhost:8080`
- Auth Service: `http://localhost:8081`
- Student Service: `http://localhost:8082`
- PostgreSQL: `localhost:5432`

La comunicación interna usa la red `fmud-network` y nombres de servicio: `postgres`, `auth-service`, `student-service`, `api-gateway`.

## Logs y reconstrucción

```powershell
docker compose logs -f api-gateway
docker compose logs -f auth-service
docker compose up --build auth-service
```

## Healthchecks

- PostgreSQL: `pg_isready`.
- Microservicios: `/actuator/health`.
- Frontend: petición HTTP a Nginx.

Ver estado:

```powershell
docker compose ps
```

## Desarrollo local mixto

Levantar solo PostgreSQL:

```powershell
docker compose up postgres
```

Luego ejecutar localmente:

```powershell
.\mvnw.cmd -pl backend/auth-service spring-boot:run
.\mvnw.cmd -pl backend/api-gateway spring-boot:run
.\mvnw.cmd -pl backend/student-service spring-boot:run
cd frontend/fmud-web
npm install
npm start
```

## Usuarios iniciales de desarrollo

Se crean al iniciar `auth-service` con perfil `dev` o `docker` si no existen:

- `secretario@fmud.local` / `Cambiar123!`
- `admin@fmud.local` / `Cambiar123!`

Estas credenciales son solo de desarrollo y deben cambiarse para producción.

## Pruebas y build local

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
cd frontend/fmud-web
npm test
npm run build
```

## Solución de problemas

- Si `frontend` no inicia, revise que el puerto `4200` no esté ocupado.
- Si `auth-service` falla al iniciar, revise `docker compose logs -f auth-service`; Flyway reportará errores de migración y conexión a PostgreSQL.
- Si el login devuelve `SERVICE_UNAVAILABLE`, revise que `api-gateway` pueda resolver `auth-service:8081` dentro de `fmud-network`.
- Si desea recrear bases desde cero en desarrollo, ejecute `docker compose down -v` y luego `docker compose up --build`.

## Limitaciones de la primera fase

No incluye hojas de vida, matrículas, documentos, prácticas, evaluaciones, calificaciones, reportes, notificaciones ni gestión completa de usuarios desde la interfaz.
