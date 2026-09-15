# FMUD Hojas de Vida

MVP dockerizado para la Fundacion Manos Unidas de Dios, limitado a la gestion de hojas de vida de estudiantes.

## Alcance

El sistema permite registrar, consultar, editar y desactivar estudiantes, gestionar su hoja de vida individual, cargar fotografias, adjuntar/descargar/reemplazar/eliminar documentos segun permisos y consultar historial de cambios.

No incluye observador, asistencias, alimentacion, notas, boletines, enfermeria, citas medicas, pagos, evaluaciones, calificaciones, practicas, notificaciones ni reportes academicos.

## Roles

- `ADMIN`: Junta Administrativa. Puede gestionar estudiantes, documentos, desactivaciones y eliminacion de documentos.
- `SECRETARIO`: Secretaria. Puede consultar, registrar, editar, adjuntar, reemplazar y descargar documentos. No puede eliminar documentos ni desactivar estudiantes.

## Arranque con Docker

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Servicios:

- Frontend: `http://localhost:4200`
- API Gateway: `http://localhost:8080`
- Auth Service: `http://localhost:8081`
- Student Service: `http://localhost:8082`
- PostgreSQL: `localhost:5432`

Volumenes persistentes:

- `fmud-postgres-data`
- `fmud-student-photos`
- `fmud-student-documents`

## Arranque en Render Free

El login consulta `/api/health/ready` antes de enviar las credenciales. El gateway
comprueba en paralelo `/actuator/health` de autenticacion y estudiantes, incluida
su conexion a la base de datos. Mientras despiertan, el formulario muestra el
progreso y permite cancelar. Las comprobaciones se reintentan cada 3 segundos
despues de una respuesta temporal, durante un maximo de 4 minutos. No hay tareas
periodicas para mantener las instancias activas ni reintentos de contrasenas.

Configuracion usada en Render:

- Gateway: `GATEWAY_TIMEOUT_SECONDS=180`.
- Los tres servicios: `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1`. Prioriza el
  arranque con poca CPU; limita la optimizacion JIT posterior. Reevaluar esta
  opcion si se migra a instancias permanentes con mayor carga.
- Static site: rewrite de `/*` a `/index.html`.

El plan gratuito suspende servicios inactivos; esta preparacion tolera el
arranque, pero no garantiza acceso inmediato tras la suspension.

## Usuarios de desarrollo

- `admin@fmud.local` / `Cambiar123!`
- `secretario@fmud.local` / `Cambiar123!`

## Desarrollo local

```powershell
docker compose up postgres
.\mvnw.cmd -pl backend/auth-service spring-boot:run
.\mvnw.cmd -pl backend/student-service spring-boot:run
.\mvnw.cmd -pl backend/api-gateway spring-boot:run
cd frontend/fmud-web
npm install
npm start
```

## Verificacion

```powershell
.\mvnw.cmd test
cd frontend/fmud-web
npm test -- --watch=false --browsers=ChromeHeadless
npm run build
```

Variables principales:

- `STUDENT_PHOTO_STORAGE_PATH`
- `STUDENT_DOCUMENT_STORAGE_PATH`
- `MAX_PHOTO_SIZE_MB`
- `MAX_DOCUMENT_SIZE_MB`
- `JWT_SECRET`
- `POSTGRES_PASSWORD`
