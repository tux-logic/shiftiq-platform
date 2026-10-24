# Core — Workshops & Specialties

**Base Paths**: `/api/v1/workshops`, `/api/v1/workshops/{workshopId}/specialties`  
**Context**: Core Domain (Workshops, Branches, Profiles)

---

## `POST /api/v1/workshops` — Crear Taller

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForOwnerProfile(#resource.ownerId())")`  
**Description**: Crea un nuevo taller asociado a un owner. Siembra 5 especialidades base automáticamente.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `ownerId` | UUID | Sí | Body | Owner profile ID (debe pertenecer al caller) |
| `businessName` | string | Sí | Body | Razón social (ej: "Taller Mecánico Hermanos Castro S.A.C.") |
| `brandName` | string | Sí | Body | Nombre comercial (ej: "Castro Motors") |
| `taxId` | string (11 dígitos) | Sí | Body | RUC (11 dígitos) |
| `mileageIntervalConfig` | integer | No | Body | Intervalo km para alertas (default: 5000) |

**Request Example**:
```json
{
  "ownerId": "a1b2c3d4-1111-2222-3333-444455556666",
  "businessName": "Taller Mecánico Hermanos Castro S.A.C.",
  "brandName": "Castro Motors",
  "taxId": "20549876123",
  "mileageIntervalConfig": 5000
}
```

### Success Response (201 CREATED)

```json
{
  "id": "b2c3d4e5-2222-3333-4444-555566667777",
  "ownerId": "a1b2c3d4-1111-2222-3333-444455556666",
  "businessName": "Taller Mecánico Hermanos Castro S.A.C.",
  "brandName": "Castro Motors",
  "taxId": "20549876123",
  "mileageIntervalConfig": 5000
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | RUC inválido (no 11 dígitos), datos faltantes |
| 403 | `ACCESS_DENIED` | Caller no autorizado para ese `ownerId` (validateOwnerProfileAccess) |
| 409 | `CONFLICT` | RUC ya registrado |

### Important Considerations

- **Autorización**: `MultiTenancySecurityService.validateOwnerProfileAccess(ownerId)` — **solo ADMIN o el propio owner** (NO bypass de ROLE_OWNER)
- **Especialidades base**: Se siembran automáticamente: `GENERAL_MECHANIC`, `ELECTRICIAN`, `BODYWORK_PAINT`, `DIAGNOSTIC`, `TIRE_ALIGNMENT` (idempotente)
- **Owner previo**: El `ownerId` debe existir en Core (`/api/v1/owners`)

---

## `PUT /api/v1/workshops/{workshopId}` — Actualizar Taller

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `validateOwnerProfileAccess`  
**Description**: Actualiza datos del taller. Valida que el caller sea owner del taller.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `workshopId` | UUID | Sí | Path | ID del taller |
| `businessName` | string | No | Body | Nueva razón social |
| `brandName` | string | No | Body | Nuevo nombre comercial |
| `taxId` | string (11 dígitos) | No | Body | Nuevo RUC |
| `mileageIntervalConfig` | integer | No | Body | Nuevo intervalo km |

**Request Example**:
```json
{
  "businessName": "Castro Motors Premium S.A.C.",
  "brandName": "Castro Motors Pro",
  "taxId": "20549876123",
  "mileageIntervalConfig": 7500
}
```

### Success Response (200 OK)

`WorkshopResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | RUC inválido |
| 403 | `ACCESS_DENIED` | No es owner del taller ni ADMIN |
| 404 | `NOT_FOUND` | Taller no existe |
| 409 | `CONFLICT` | RUC ya en uso por otro taller |

### Important Considerations

- **Validación owner**: Busca taller existente → `validateOwnerProfileAccess(ownerId)` → actualiza
- **RUC único**: No puede duplicar RUC de otro taller

---

## `GET /api/v1/workshops/{workshopId}` — Obtener Taller por ID

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual  
**Description**: Retorna detalle del taller. Valida acceso al owner.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `workshopId` | UUID | Sí | Path | ID del taller |

### Success Response (200 OK)

```json
{
  "id": "b2c3d4e5-2222-3333-4444-555566667777",
  "ownerId": "a1b2c3d4-1111-2222-3333-444455556666",
  "businessName": "Taller Mecánico Hermanos Castro S.A.C.",
  "brandName": "Castro Motors",
  "taxId": "20549876123",
  "mileageIntervalConfig": 5000
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No autorizado para el owner del taller |
| 404 | `NOT_FOUND` | Taller no existe |

---

## `GET /api/v1/workshops?ownerId={ownerId}` — Listar Talleres del Dueño

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `validateOwnerProfileAccess(ownerId)`  
**Description**: Lista todos los talleres de un owner.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `ownerId` | UUID | Sí | Query | Owner profile ID |

### Success Response (200 OK)

```json
[
  {
    "id": "b2c3d4e5-2222-3333-4444-555566667777",
    "ownerId": "a1b2c3d4-1111-2222-3333-444455556666",
    "businessName": "Taller Mecánico Hermanos Castro S.A.C.",
    "brandName": "Castro Motors",
    "taxId": "20549876123",
    "mileageIntervalConfig": 5000
  }
]
```

---

## `POST /api/v1/workshops/{workshopId}/specialties` — Crear Especialidad

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")`  
**Description**: Agrega especialidad técnica al catálogo del taller. Usada al dar de alta personal.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `workshopId` | UUID | Sí | Path | Taller al que pertenece |
| `name` | string | Sí | Body | Nombre visible (ej: "Inyección Electrónica y Calibración") |
| `code` | string | Sí | Body | Código único en taller (ej: `INYECCION_ELECTRONICA`, UPPER_SNAKE_CASE) |
| `description` | string | No | Body | Descripción técnica |

**Request Example**:
```json
{
  "name": "Inyección Electrónica y Calibración",
  "code": "INYECCION_ELECTRONICA",
  "description": "Limpieza y diagnóstico de inyectores, sensores MAF/MAP y cuerpos de aceleración"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "e4f5a6b7-8888-9999-0000-111122223333",
  "workshopId": "b2c3d4e5-2222-3333-4444-555566667777",
  "name": "Inyección Electrónica y Calibración",
  "code": "INYECCION_ELECTRONICA",
  "description": "Limpieza y diagnóstico de inyectores, sensores MAF/MAP y cuerpos de aceleración",
  "active": true,
  "createdAt": "2026-10-07T12:00:00Z",
  "updatedAt": "2026-10-07T12:00:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Código duplicado en taller, nombre vacío |
| 403 | `ACCESS_DENIED` | Sin acceso al taller (no owner/manager/assistant asignado) |
| 404 | `NOT_FOUND` | Taller no existe |

### Important Considerations

- **Código único**: Por taller, case-insensitive
- **Validación en onboarding**: Al crear `EmployeeRegistration`, el `speciality` debe existir y estar `active=true` en este catálogo
- **Autorización**: `isAuthorizedForWorkshop` permite ADMIN, OWNER del taller, BRANCH_MANAGER/ASSISTANT con sede en ese taller

---

## `GET /api/v1/workshops/{workshopId}/specialties` — Listar Especialidades

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")`  
**Description**: Lista especialidades del taller, opcionalmente solo activas.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `workshopId` | UUID | Sí | Path | Taller |
| `activeOnly` | boolean | No | Query | Default `true` — solo activas |

### Success Response (200 OK)

```json
[
  {
    "id": "e4f5a6b7-8888-9999-0000-111122223333",
    "workshopId": "b2c3d4e5-2222-3333-4444-555566667777",
    "name": "Mecánica General",
    "code": "GENERAL_MECHANIC",
    "description": "Mantenimiento preventivo, correctivo y reparación de motores",
    "active": true,
    "createdAt": "2026-10-07T12:00:00Z",
    "updatedAt": "2026-10-07T12:00:00Z"
  }
]
```

---

## `GET /api/v1/workshops/{workshopId}/specialties/{specialtyId}` — Obtener Especialidad

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")`  
**Description**: Detalle de una especialidad. Valida que pertenezca al taller.

### Success Response (200 OK)

`WorkshopSpecialtyResource` completo.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso al taller |
| 404 | `NOT_FOUND` | Especialidad no existe o no pertenece al taller |

---

## `PUT /api/v1/workshops/{workshopId}/specialties/{specialtyId}` — Actualizar Especialidad

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")`  
**Description**: Actualiza nombre y descripción (no código).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `name` | string | No | Nuevo nombre |
| `description` | string | No | Nueva descripción |

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Nombre vacío |
| 403 | `ACCESS_DENIED` | Sin acceso al taller |
| 404 | `NOT_FOUND` | Especialidad no existe en ese taller |

---

## `DELETE /api/v1/workshops/{workshopId}/specialties/{specialtyId}` — Desactivar Especialidad

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")`  
**Description**: Desactiva (soft delete) — no se puede asignar a nuevo personal. Existentes conservan referencia.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso al taller |
| 404 | `NOT_FOUND` | Especialidad no existe en ese taller |

---

## `GET /api/v1/workshops/{workshopId}/specialties/{specialtyId}/history` — Historial de Revisiones

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#workshopId)")`  
**Description**: Auditoría cronológica de cambios en la especialidad.

### Success Response (200 OK)

```json
[
  {
    "id": "rev-1",
    "specialtyId": "e4f5a6b7-8888-9999-0000-111122223333",
    "fieldName": "name",
    "oldValue": "Mecánica General",
    "newValue": "Mecánica General y Motores",
    "changedBy": "user-uuid",
    "changedAt": "2026-10-10T15:30:00Z"
  }
]
```

### Important Considerations

- **Auditoría**: Cada cambio en nombre/descripción/active genera registro
- **Uso**: Trazabilidad para compliance y debugging