# ShiftIQ Platform

## Descripción general
ShiftIQ Platform es una aplicación Java 26 (Spring Boot 4) basada en monolito Maven que gestiona operaciones críticas para talleres mecánicos: agendamiento de citas, control de flotas, inventario, cotizaciones, facturación y operaciones de IAM (gestión de usuarios, autenticación y autorización).

La arquitectura sigue principios de **Domain-Driven Design (DDD)** con **Contextos acotados (Bounded Contexts)** independientes que se comunican a través de eventos de dominio integrados en un kernel compartido. El proyecto está organizado en los siguientes contextos:

- **Core**: Perfiles, talleres y sucursales
- **IAM**: Autenticación, autorización y gestión de usuarios
- **Fleet**: Citas, vehículos, empleados y clientes por sucursal
- **Operations**: Órdenes de trabajo, servicios y tareas
- **Billing**: Cotizaciones, pagos y vouchers
- **Inventory**: Productos y alertas de stock
- **IoT**: Dispositivos Obd2 y registraciones
- **Shared**: Kernel compartido con utilidades, seguridad y eventos de integración

## Arquitectura técnica

### Stack tecnológico
- **Java 27** (JDK 27) / **Maven 3.9.16**
- **Spring Boot 4** con Jackson, Spring Security, Spring Data JPA
- **PostgreSQL** como base de datos principal
- **Flyway** para migraciones de esquema (baseline-on-migrate=true)
- **Testcontainers** para tests de integración (Postgres container)
- **Lombok 1.18.48** (commit 1: fix build for JDK 26)
- **JJWT** (JSON Web Token) para autenticación

### Arquitectura de seguridad (commits 8, 11, 14, 17)
- **Autenticación JWT** con access tokens de 15 minutos y refresh tokens de 7 días
- **Refresh token rotation**: cada intercambio consume el token anterior (single-use)
- **Revocation server-side**: los tokens pueden ser revocados explícitamente mediante `DELETE /sessions`
- **Audiencia (audience) diferenciada**: access tokens llevan audiencia `shiftiq-users`, refresh tokens `shiftiq-refresh`, por lo que un refresh token nunca puede usarse como Bearer token en las peticiones normales
- **Validación de secreto al arranque**: el `authorization.jwt.secret` debe decodificarse a al menos 32 bytes (256 bits) para firmar con HS256, lanzado `IllegalStateException` en startup si no cumple
- **Multi-tenancy fail-closed**: la validación de acceso a ramas y usuarios niega explícitamente cuando el identificador falta (`null`), evitando bypass de aislamiento por omisión de campos

### Endpoints de autenticación (commit 12)
- `POST /api/v1/authentication/sessions` (login) → devuelve `token` (access) + `refreshToken` + `accessTokenExpiresInSeconds` (15 min)
- `POST /api/v1/authentication/sessions/refresh` (refresh) → intercambia refresh token por nuevos tokens (rotación single-use)
- `DELETE /api/v1/authentication/sessions` (logout) → revoca el refresh token indicado
- `POST /api/v1/authentication/google-sign-in` → autenticación federada

### Patrón de error global (commit 4)
- Todas las excepciones no manejadas son traducidas a respuestas HTTP consistentes:
  - 400 Bad Request (validación, tipos de datos incorrectos)
  - 401 Unauthorized (token inválido/no autenticado)
  - 403 Forbidden (sin permisos multi-tenancy)
  - 404 Not Found (recurso inexistente)
  - 405 Method Not Allowed / 415 Unsupported Media Type
  - 409 Conflict (violaciones de integridad, locking optimista)
  - 422 Business Rule Violation
  - 500 Internal Server Error (errores verdaderos, sin fugas de internals)
- **Never leaks internal exceptions**: los mensajes `ex.getMessage()` nunca alcanzan al cliente; se registran en servidor y el cliente recibe mensajes localizados genéricos
- **i18n keys**: se añadieron las claves `error.*` y `validation.*` a ambos bundles (`messages.properties` y `messages_es.properties`)

### Migración con Flyway (commit 3)
- `V1__baseline.sql`: reproduce el esquema hand-written (28 tablas, FKs, triggers, índices)
- `baseline-on-migrate=true`, `baseline-version=1`: en BD existente Flyway baselinea en versión 1 y salta V1
- `spring.jpa.hibernate.ddl-auto=validate`: en startup Hibernate valida que las entidades coinciden con el esquema; cualquier drift rompe el arranque
- `docs/atelier-schema.sql` pasada a rol de "referencia solamente"

### Eventos de dominio cross-Context (commit 10)
- `EmployeeRegistrationApprovedEvent` migrado desde `fleet.domain.model.events` a `shared.domain.model.events`
- Evento de integración con UUIDs planos (sin depender de value objects de contexto)
- Publicado por Fleet, consumido por IAM para otorgar acceso a la rama

### Capa de seguridad y multi-tenancy (commits 8, 14)
- **AuthenticatedPrincipal** port en `shared` rompe la dependencia circular `shared ↔ iam`
- `UserDetailsImpl` ahora implementa `AuthenticatedPrincipal` con métodos `getId()`, `hasRole(String)`, `hasBranch(UUID)`
- **Valores nulos denegados por defecto**: `isAuthorizedForBranch(null)` → false, `isAuthorizedForUser(null)` → false
- **Controladores**: `BranchesController`, `AppointmentsController`, `CustomerRegistrationsController`, `EmployeeRegistrationsController` usan la sobrecarga `isAuthorizedForBranch(BranchId)` que es nula-safe

### Testing (55 tests verdes)
- `GlobalExceptionHandlerTest`: 8 tests cubriendo los nuevos handlers y el comportamiento "no leak"
- Tests de integración con Testcontainers (pending Docker)
- Tests unitarios por capa (command services, query services, controladores)
- Cobertura: 47 tests previos + 8 del handler = 55 total, 0 failures, 0 errors

## Guía de desarrollo rápida

### Levantamiento local (sin Docker)
```bash
# 1. JDK 27 y Maven 3.9.16
# 2. Variables de entorno obligatorias:
export JWT_SECRET=$(openssl rand -base64 32)  # mínimo 32 bytes Base64

# 3. Compilar y test (válida que el secreto tenga 32+ bytes):
./mvnw -B test

# 4. Si se tiene Docker corriendo:
docker compose up -d
# Las apps se conectan a postgres-db en puerto 5432

# 5. Para correr un test específico:
./mvnw -B test -Dtest=GlobalExceptionHandlerTest
```

### Flujo de autención del frontend
1. **Login**: POST `/api/v1/authentication/sessions` con `email`/`password`
   - Respuesta: `token` (usar en `Authorization: Bearer`) + `refreshToken` (guardar en almacenamiento persistente) + `accessTokenExpiresInSeconds` (900 = 15 min)
2. **Peticiones normales**: enviar `Authorization: Bearer <token>` 
   - Si la API responde **401**, llamar a `POST /sessions/refresh` con el `refreshToken` guardado
   - Reemplazar ambos tokens y reintentar la petición original
   - Si el refresh también falla (**401**), la sesión terminó: pedir credenciales de nuevo
3. **Logout**: `DELETE /api/v1/authentication/sessions` con el `refreshToken` → 204 No Content

### Convenios de código
- **Lombok 1.18.48**: versión obligatoria para JDK 27 (commit 1)
- **Sin fugas de información**: los handlers global capturan y loguean en servidor, never expose internals al cliente
- **Fallos cerrados**: validaciones de multi-tenancy niegan cuando falta el identificador
- **Nombres de paquetes**: `shared` nunca depende de `iam` ni de otros contextos; `iam` depende de `shared` y su propio modelo; `fleet` y `iot` dependen de `shared` y de sus propias aplicaciones services, nunca de repositorios de otros contextos
- **Events**: los eventos cross-context viven en `shared.domain.model.events` con identificadores UUID planos

### Convenios de commit (cumplidos en esta rama)
- Commits estilo convencional en inglés (ej: `fix(build): upgrade lombok to 1.18.48 for jdk 26 support`)
- No se modifica `LICENSE.md` (se queda vacío por decisión)
- Se deletaron `public/fonts/**` y `.vscode/launch.json` (commit 2)
- No se hace push a menos que el usuario lo indique
- Cada commit deja el proyecto `green`: `./mvnw -B test` exitoso

## Próximos pasos (pendientes de validación con Docker)
- Tests de integración con Testcontainers (Postgres container)
- Validación end-to-end del flujo de refresh token y logout
- Despliegue con `docker compose up -d` en entorno local