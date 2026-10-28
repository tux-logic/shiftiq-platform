# IAM — User Management Endpoints

**Base Path**: `/api/v1/users`  
**Context**: Identity & Access Management  
**Default Auth**: `@PreAuthorize("isAuthenticated()")` en clase controller

---

## `POST /api/v1/users` — Registrar Nuevo Usuario (Sign Up)

**Security**: `@PreAuthorize("permitAll()")` — Público  
**Description**: Registra una nueva cuenta de usuario. Requiere que el correo haya sido verificado previamente mediante código OTP (`/api/v1/authentication/otp/send` y `/api/v1/authentication/otp/verify`).

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `email` | string (email) | Sí | Body | Correo único verificado previamente por OTP |
| `password` | string | Sí | Body | Mín 8 caracteres |

**Request Example**:
```json
{
  "email": "carlos.cliente@gmail.com",
  "password": "Password123!"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "email": "carlos.cliente@gmail.com",
  "role": "ROLE_USER"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Correo no verificado con OTP (`iam.error.otp.verificationRequired`), formato inválido o password menor a 8 caracteres |
| 400 | `VALIDATION_ERROR` | Correo ya en uso (`iam.error.email.alreadyInUse`) |

### Important Considerations

- **Verificación OTP Obligatoria**: Antes de invocar este endpoint, el frontend debe solicitar el código mediante `POST /api/v1/authentication/otp/send` y validarlo mediante `POST /api/v1/authentication/otp/verify`.
- **Consumo de OTP**: Cada verificación OTP es de un solo uso; se consume automáticamente al crear la cuenta.
- **Sin tokens**: Este endpoint crea la cuenta y devuelve los datos del usuario, tras lo cual se realiza el login en `POST /api/v1/authentication/sessions` (o `/login`).

---

## `GET /api/v1/users/{userId}` — Obtener Usuario por ID

**Security**: `@PreAuthorize("isAuthenticated() and @userSecurityService.isCurrentUser(#userId)")`  
**Description**: Retorna datos básicos del usuario. Solo el propio usuario o ADMIN.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Path | ID del usuario |

### Success Response (200 OK)

```json
{
  "id": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "email": "carlos.cliente@gmail.com",
  "role": "ROLE_CUSTOMER"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Token inválido/expirado |
| 403 | `ACCESS_DENIED` | No es el propio usuario ni ADMIN |
| 404 | `NOT_FOUND` | Usuario no existe |

### Important Considerations

- **Autorización**: `UserSecurityService.isCurrentUser(userId)` valida que el JWT `sub` coincida con `userId` O tenga `ROLE_ADMIN`
- **Datos sensibles**: No expone password hash, refresh tokens, ni datos de perfil (esos están en Core)

---

## `GET /api/v1/users?email={email}` — Buscar Usuario por Email

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual en código  
**Description**: Busca usuario por email exacto. Requiere ser el propio usuario o ADMIN.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `email` | string (email) | Sí | Query | Email a buscar |

### Success Response (200 OK)

```json
{
  "id": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "email": "carlos.cliente@gmail.com",
  "role": "ROLE_CUSTOMER"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Email inválido |
| 403 | `ACCESS_DENIED` | No es el propio usuario ni ADMIN |
| 404 | `NOT_FOUND` | Usuario no existe |

### Important Considerations

- **Validación adicional**: Código verifica `userSecurityService.isCurrentUser(foundUserId)` antes de retornar
- **Uso**: Para "olvidé mi contraseña" o verificación de existencia

---

## `PUT /api/v1/users/{userId}/email` — Actualizar Email

**Security**: `@PreAuthorize("isAuthenticated() and @userSecurityService.isCurrentUser(#userId)")`  
**Description**: Actualiza el email del usuario y genera **nuevo par de tokens** (access + refresh) para el nuevo email.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Path | ID del usuario (debe coincidir con token) |
| `email` | string (email) | Sí | Body | Nuevo email (único, válido) |

**Request Example**:
```json
{
  "email": "nuevo.email@gmail.com"
}
```

### Success Response (200 OK)

```json
{
  "id": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "email": "nuevo.email@gmail.com",
  "role": "ROLE_CUSTOMER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "new-refresh-token-uuid",
  "accessTokenExpiresInSeconds": 900
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Email inválido, ya en uso |
| 401 | `UNAUTHORIZED` | Token inválido |
| 403 | `ACCESS_DENIED` | No es el propio usuario ni ADMIN |
| 404 | `NOT_FOUND` | Usuario no existe |

### Important Considerations

- **Nuevos tokens**: El response incluye **nuevo access token y refresh token** — frontend debe reemplazar ambos inmediatamente
- **Verificación OTP**: Si email verification habilitada, nuevo email requiere OTP antes de activarse
- **Revocation**: Refresh token anterior se revoca automáticamente
- **Unicidad**: Email debe ser único en todo el sistema

---

## `PUT /api/v1/users/{userId}/password` — Actualizar Contraseña

**Security**: `@PreAuthorize("isAuthenticated() and @userSecurityService.isCurrentUser(#userId)")`  
**Description**: Cambia la contraseña del usuario validando la actual.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Path | ID del usuario (debe coincidir con token) |
| `currentPassword` | string | Sí | Body | Contraseña actual |
| `newPassword` | string | Sí | Body | Nueva contraseña (mín 8 chars, 1 mayús, 1 número, 1 especial) |

**Request Example**:
```json
{
  "currentPassword": "Password123!",
  "newPassword": "NuevaPassword456!"
}
```

### Success Response (200 OK)

```json
{
  "message": "Password updated successfully"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Password actual incorrecta, nueva password débil |
| 401 | `UNAUTHORIZED` | Token inválido |
| 403 | `ACCESS_DENIED` | No es el propio usuario ni ADMIN |
| 404 | `NOT_FOUND` | Usuario no existe |

### Important Considerations

- **Validación actual**: Requiere contraseña actual correcta (previene cambio en sesión robada)
- **Revocation**: Todos los refresh tokens del usuario se revocan (logout global en todos los dispositivos)
- **Frontend**: Tras éxito, redirigir a login (tokens invalidados)