# IAM — Authentication Endpoints

**Base Path**: `/api/v1/authentication`  
**Context**: Identity & Access Management

---

## `POST /api/v1/authentication/sessions` (alias: `/api/v1/authentication/login`) — Iniciar Sesión (Login)

**Security**: Público  
**Description**: Autentica a un usuario mediante correo y contraseña (disponible tanto en `/sessions` como en el alias `/login`). Devuelve Access Token (15 min) + Refresh Token (7 días, rotación single-use).

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `email` | string (email) | Sí | Body | Correo electrónico registrado |
| `password` | string | Sí | Body | Contraseña (mín 8 chars, 1 mayús, 1 número, 1 especial) |

**Request Example**:
```json
{
  "email": "juan.mecanico@shiftiq.com",
  "password": "Password123!"
}
```

### Success Response (200 OK)

```json
{
  "id": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "email": "juan.mecanico@shiftiq.com",
  "role": "ROLE_EMPLOYEE",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d8c7b6a5-4321-4f9e-8d2a-112233445566",
  "accessTokenExpiresInSeconds": 900
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Email inválido, password faltante |
| 401 | `UNAUTHORIZED` | Credenciales incorrectas |

### Important Considerations

- **JWT Claims**: El access token incluye `sub` (userId), `role`, `branchIds` (array), `aud: "shiftiq-users"`
- **Refresh Token**: Audience `shiftiq-refresh` — **nunca** usar como Bearer token en peticiones normales
- **Rotación**: Cada refresh consume el token anterior (single-use). Guardar el nuevo refreshToken devuelto
- **Frontend**: Implementar interceptor HTTP que en 401 llame a `/sessions/refresh` automáticamente

---

## `POST /api/v1/authentication/sessions/google` — Login con Google OAuth

**Security**: Público  
**Description**: Autentica o sincroniza usuario vía Google Sign-In (ID Token). Si no existe, crea cuenta con `ROLE_USER`.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `idToken` | string (JWT) | Sí | Body | ID Token emitido por Google Identity Services |

**Request Example**:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1NiI..."
}
```

### Success Response (200 OK)

Igual estructura que login normal (`AuthenticatedUserResource`).

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | idToken faltante o inválido |
| 401 | `UNAUTHORIZED` | Token de Google expirado o no verificado |

### Important Considerations

- **Auto-registro**: Usuario nuevo obtiene `ROLE_USER` por defecto
- **Sincronización**: Si el email ya existe, actualiza datos de Google (nombre, foto)
- **Requisito**: `google.client.id` configurado en entorno

---

## `POST /api/v1/authentication/sessions/refresh` — Refrescar Sesión

**Security**: Público  
**Description**: Intercambia refresh token vigente por nuevo access token + nuevo refresh token (rotación single-use).

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `refreshToken` | string (UUID) | Sí | Body | Refresh token obtenido en login/refresh anterior |

**Request Example**:
```json
{
  "refreshToken": "d8c7b6a5-4321-4f9e-8d2a-112233445566"
}
```

### Success Response (200 OK)

Nuevo `AuthenticatedUserResource` con tokens rotados.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | refreshToken faltante |
| 401 | `UNAUTHORIZED` | Refresh token expirado, revocado, ya usado, o usuario deshabilitado |

### Important Considerations

- **Rotación obligatoria**: El refresh token anterior **queda invalidado** tras uso exitoso
- **Flujo frontend**: En 401 → llamar refresh → reemplazar ambos tokens → reintentar petición original
- **Si refresh falla (401)**: Sesión terminada → redirigir a login

---

## `DELETE /api/v1/authentication/sessions` — Cerrar Sesión (Logout)

**Security**: Público (requiere refreshToken en body)  
**Description**: Revoca el refresh token indicado en el servidor (invalida sesión).

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `refreshToken` | string (UUID) | Sí | Body | Refresh token a revocar |

**Request Example**:
```json
{
  "refreshToken": "d8c7b6a5-4321-4f9e-8d2a-112233445566"
}
```

### Success Response (204 NO CONTENT)

Sin body.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | refreshToken faltante |
| 401 | `UNAUTHORIZED` | Token ya revocado o inválido |

### Important Considerations

- **Revocation server-side**: El token se marca como revocado en BD; no basta con borrarlo del cliente
- **Access token**: Sigue válido hasta su expiración (15 min) — para invalidación inmediata, usar lista negra (no implementado)

---

## `POST /api/v1/authentication/password-recoveries` — Solicitar Recuperación de Contraseña

**Security**: Público  
**Description**: Envía email con enlace/token de recuperación al correo indicado.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `email` | string (email) | Sí | Body | Correo del usuario |

**Request Example**:
```json
{
  "email": "usuario@ejemplo.com"
}
```

### Success Response (200 OK / 204 NO CONTENT)

Sin body (siempre 2xx para no revelar si email existe).

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Email inválido |

### Important Considerations

- **Anti-enumeración**: Siempre responde 2xx aunque el email no exista
- **Email**: Usa Brevo (config `brevo.api.key`) con template de recuperación
- **Token**: Expira en 1 hora (configurable), single-use

---

## `POST /api/v1/authentication/password-resets` — Restablecer Contraseña

**Security**: Público  
**Description**: Aplica cambio de contraseña usando token recibido por email.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `token` | string | Sí | Body | Token de recuperación (del email) |
| `newPassword` | string | Sí | Body | Nueva contraseña (mín 8 chars, 1 mayús, 1 número, 1 especial) |

**Request Example**:
```json
{
  "token": "token-recibido-por-correo",
  "newPassword": "NuevaPasswordSegura2026!"
}
```

### Success Response (200 OK / 204 NO CONTENT)

```json
{
  "message": "Password updated successfully"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Token faltante, password débil |
| 401 | `UNAUTHORIZED` | Token expirado, ya usado, o inválido |

### Important Considerations

- **Single-use**: Token se invalida tras uso exitoso
- **Sesiones existentes**: Se revocan todos los refresh tokens del usuario (logout global)

---

## `POST /api/v1/authentication/otp/send` — Enviar Código OTP

**Security**: Público  
**Description**: Valida entregabilidad y envía código OTP (6 dígitos) vía Brevo al email indicado. Para verificación de email en registro o 2FA.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `email` | string (email) | Sí | Body | Correo destino |

**Request Example**:
```json
{
  "email": "nuevo@usuario.com"
}
```

### Success Response (200 OK)

Sin body.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Email inválido |
| 429 | `RATE_LIMITED` | Límite de envíos excedido (configurable) |

### Important Considerations

- **Entregabilidad**: Usa EmailVerify.io para validar email antes de enviar
- **Expiración**: Código OTP expira en 10 minutos (configurable)
- **Almacenamiento**: Se guarda en BD con hash (no en texto plano)

---

## `POST /api/v1/authentication/otp/verify` — Verificar Código OTP

**Security**: Público  
**Description**: Verifica el código OTP enviado contra el registro activo.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `email` | string (email) | Sí | Body | Correo al que se envió OTP |
| `code` | string (6 dígitos) | Sí | Body | Código recibido |

**Request Example**:
```json
{
  "email": "nuevo@usuario.com",
  "code": "123456"
}
```

### Success Response (200 OK)

Sin body (o información de verificación exitosa).

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Email/código faltante, formato inválido |
| 401 | `UNAUTHORIZED` | Código incorrecto, expirado, o ya usado |
| 404 | `NOT_FOUND` | No hay OTP pendiente para ese email |

### Important Considerations

- **Single-use**: Código se invalida tras verificación exitosa
- **Intentos**: Máximo 3 intentos por código (configurable), luego requiere reenvío
- **Uso**: Requerido en `POST /api/v1/users` (sign up) si email verification está habilitado