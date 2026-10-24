# Core — Owner Profiles

**Base Path**: `/api/v1/owners`  
**Context**: Core Domain  
**Critical Auth Note**: **Todas las operaciones requieren `validateSelfAccess`** — solo ADMIN o el **mismo usuario** (NO bypass de ROLE_OWNER)

---

## `POST /api/v1/owners` — Crear Registro de Dueño

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForSelf(#resource.userId())")`  
**Description**: Crea perfil de dueño vinculado a un User (IAM). Solo el propio usuario o ADMIN.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Body | User ID (IAM) — debe ser el caller |
| `firstName` | string | Sí | Body | Nombres |
| `lastName` | string | Sí | Body | Apellidos |
| `documentType` | string (enum) | Sí | Body | `DNI`, `RUC`, `CE`, `PASSPORT` |
| `documentNumber` | string | Sí | Body | Número de documento (único) |
| `phone` | string | Sí | Body | Teléfono (ej: `+51987112233`) |

**Request Example**:
```json
{
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Raúl",
  "lastName": "Castro",
  "documentType": "DNI",
  "documentNumber": "45678901",
  "phone": "+51987112233"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "o1o2o3o4-1111-2222-3333-444455556666",
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Raúl",
  "lastName": "Castro",
  "documentType": "DNI",
  "documentNumber": "45678901",
  "phone": "+51987112233"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | DNI/RUC inválido, documento duplicado, userId no coincide |
| 403 | `ACCESS_DENIED` | Caller no es el `userId` ni ADMIN (`validateSelfAccess`) |
| 404 | `NOT_FOUND` | User no existe |
| 409 | `CONFLICT` | Documento ya registrado |

### Important Considerations

- **🔴 CRÍTICO**: `validateSelfAccess(userId)` — **ROLE_OWNER NO tiene bypass** para crear/ver/editar/borrar OTRO owner
- **User previo**: El `userId` debe existir en IAM con rol `ROLE_OWNER` (o se asigna al crear)
- **Unicidad**: Un User solo puede tener un Owner profile
- **Uso**: Requerido para crear talleres (`POST /workshops` requiere `ownerId`)

---

## `GET /api/v1/owners/{ownerId}` — Obtener Dueño por ID

**Security**: Validación manual `validateSelfAccess(userId del owner)`  
**Description**: Retorna perfil de dueño. Solo el propio usuario o ADMIN.

### Success Response (200 OK)

```json
{
  "id": "o1o2o3o4-1111-2222-3333-444455556666",
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Raúl",
  "lastName": "Castro",
  "documentType": "DNI",
  "documentNumber": "45678901",
  "phone": "+51987112233"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No es el propio usuario ni ADMIN |
| 404 | `NOT_FOUND` | Owner no existe |

---

## `GET /api/v1/owners?userId={userId}` — Obtener Dueño por User ID

**Security**: Validación manual `validateSelfAccess(userId)`  
**Description**: Busca owner profile por User ID (IAM). Solo el propio usuario o ADMIN.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Query | User ID (IAM) |

### Success Response (200 OK)

`OwnerResource` (igual que GET by ID).

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No es el propio usuario ni ADMIN |
| 404 | `NOT_FOUND` | No hay owner profile para ese userId |

---

## `PUT /api/v1/owners/{ownerId}` — Actualizar Dueño

**Security**: Validación manual `validateSelfAccess(userId del owner existente)`  
**Description**: Actualiza datos del perfil de dueño. Solo el propio usuario o ADMIN.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `firstName` | string | No | Nombres |
| `lastName` | string | No | Apellidos |
| `documentType` | string | No | Tipo documento |
| `documentNumber` | string | No | Número documento (único) |
| `phone` | string | No | Teléfono |

### Success Response (200 OK)

`OwnerResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Documento duplicado, formato inválido |
| 403 | `ACCESS_DENIED` | **No es el propio usuario ni ADMIN** (ROLE_OWNER NO bypass) |
| 404 | `NOT_FOUND` | Owner no existe |
| 409 | `CONFLICT` | Documento ya en uso |

---

## `DELETE /api/v1/owners/{ownerId}` — Eliminar Dueño

**Security**: Validación manual `validateSelfAccess(userId del owner existente)`  
**Description**: Elimina perfil de dueño. Solo el propio usuario o ADMIN.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | **No es el propio usuario ni ADMIN** (ROLE_OWNER NO bypass) |
| 404 | `NOT_FOUND` | Owner no existe |
| 409 | `CONFLICT` | Owner tiene talleres asociados (debe eliminarlos primero) |

### Important Considerations

- **Cascada**: No se permite eliminar si tiene talleres → error 409
- **User (IAM)**: No se elimina el User, solo el Owner profile
- **🔴 REPETICIÓN**: **PUT y DELETE usan `validateSelfAccess` — ROLE_OWNER NO puede gestionar otros owners**