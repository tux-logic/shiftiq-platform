# Core — Branches & Subscriptions

**Base Paths**: `/api/v1/branches`  
**Context**: Core Domain

---

## `POST /api/v1/branches` — Crear Sede para un Taller

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForWorkshop(#resource.workshopId())")`  
**Description**: Crea una nueva sede (branch) asociada a un taller. Valida acceso al taller.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `workshopId` | UUID | Sí | Body | Taller padre (debe tener acceso) |
| `code` | string | Sí | Body | Código único (ej: `BR-SURCO-01`, max 20 chars) |
| `name` | string | Sí | Body | Nombre visible (ej: "Sede Principal Santiago de Surco") |
| `address` | string | Sí | Body | Dirección completa |
| `phone` | string | Sí | Body | Teléfono de contacto (ej: `+51987654321`) |

**Request Example**:
```json
{
  "workshopId": "b2c3d4e5-2222-3333-4444-555566667777",
  "code": "BR-SURCO-01",
  "name": "Sede Principal Santiago de Surco",
  "address": "Av. Primavera 1234, Santiago de Surco, Lima",
  "phone": "+51987654321"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "c3d4e5f6-3333-4444-5555-666677778888",
  "workshopId": "b2c3d4e5-2222-3333-4444-555566667777",
  "code": "BR-SURCO-01",
  "name": "Sede Principal Santiago de Surco",
  "address": "Av. Primavera 1234, Santiago de Surco, Lima",
  "phone": "+51987654321"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Código duplicado en taller, campos faltantes |
| 403 | `ACCESS_DENIED` | Sin acceso al taller (`isAuthorizedForWorkshop`) |
| 404 | `NOT_FOUND` | Taller no existe |
| 409 | `CONFLICT` | Código ya existe en ese taller |

### Important Considerations

- **Autorización**: `isAuthorizedForWorkshop(workshopId)` — ADMIN, OWNER del taller, BRANCH_MANAGER/ASSISTANT con sede en ese taller
- **Código único**: Por taller (case-insensitive)
- **Uso**: Requerido para citas, inventario, facturación, analytics

---

## `PUT /api/v1/branches/{branchId}` — Actualizar Sede

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `validateBranchAccess(branchId)`  
**Description**: Actualiza datos de la sede. Valida acceso a la sede.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `branchId` | UUID | Sí | Path | ID de la sede |
| `code` | string | No | Body | Nuevo código |
| `name` | string | No | Body | Nuevo nombre |
| `address` | string | No | Body | Nueva dirección |
| `phone` | string | No | Body | Nuevo teléfono |

**Request Example**:
```json
{
  "code": "BR-SURCO-01",
  "name": "Sede Surco Premium",
  "address": "Av. Primavera 1250, Santiago de Surco, Lima",
  "phone": "+51999888777"
}
```

### Success Response (200 OK)

`BranchResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Código duplicado en taller |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede (`isAuthorizedForBranch`) |
| 404 | `NOT_FOUND` | Sede no existe |
| 409 | `CONFLICT` | Código duplicado |

### Important Considerations

- **Acceso a sede**: `MultiTenancySecurityService.validateBranchAccess(branchId)` — valida `isAuthorizedForBranch` (ADMIN, OWNER del taller, o staff asignado a la sede)

---

## `GET /api/v1/branches/{branchId}` — Obtener Sede por ID

**Security**: Validación manual `validateBranchAccess(branchId)`  
**Description**: Retorna detalle de la sede.

### Success Response (200 OK)

```json
{
  "id": "c3d4e5f6-3333-4444-5555-666677778888",
  "workshopId": "b2c3d4e5-2222-3333-4444-555566667777",
  "code": "BR-SURCO-01",
  "name": "Sede Principal Santiago de Surco",
  "address": "Av. Primavera 1234, Santiago de Surco, Lima",
  "phone": "+51987654321"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede no existe |

---

## `GET /api/v1/branches?workshopId={workshopId}` — Listar Sedes de un Taller

**Security**: `@PreAuthorize("isAuthenticated()")` + filtro manual `isAuthorizedForBranch`  
**Description**: Lista sedes del taller, filtrando por acceso del caller.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `workshopId` | UUID | Sí | Query | Taller padre |

### Success Response (200 OK)

```json
[
  {
    "id": "c3d4e5f6-3333-4444-5555-666677778888",
    "workshopId": "b2c3d4e5-2222-3333-4444-555566667777",
    "code": "BR-SURCO-01",
    "name": "Sede Principal Santiago de Surco",
    "address": "Av. Primavera 1234, Santiago de Surco, Lima",
    "phone": "+51987654321"
  }
]
```

### Important Considerations

- **Filtrado post-query**: Obtiene todas las sedes del taller → filtra `isAuthorizedForBranch(branchId)` → retorna solo accesibles
- **Owner multi-sede**: Ve todas sus sedes; BRANCH_MANAGER solo las suyas

---

## `POST /api/v1/branches/{branchId}/subscriptions` — Asignar Suscripción a Sede

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Asigna plan de suscripción a la sede (simula pago). Requiere acceso a la sede.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `branchId` | UUID | Sí | Path | Sede destino |
| `planId` | UUID | Sí | Body | Plan de suscripción |
| `billingCycle` | string (enum) | Sí | Body | `MONTHLY` o `ANNUAL` |

**Request Example**:
```json
{
  "planId": "f1e2d3c4-9999-8888-7777-666655554444",
  "billingCycle": "MONTHLY"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "d4e5f6a7-4444-5555-6666-777788889999",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "planId": "f1e2d3c4-9999-8888-7777-666655554444",
  "billingCycle": "MONTHLY",
  "status": "ACTIVE",
  "startDate": "2026-10-01T00:00:00Z",
  "endDate": "2026-11-01T00:00:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Plan inválido, ciclo no soportado |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede o plan no existe |
| 409 | `CONFLICT` | Sede ya tiene suscripción activa |

### Important Considerations

- **Una suscripción activa**: No se puede crear otra si ya hay una `ACTIVE`
- **Planes**: Gestionados por ADMIN (no documentado aquí)
- **Facturación**: Integración real con Mercado Pago en Billing context

---

## `DELETE /api/v1/branches/{branchId}/subscription` — Cancelar Suscripción

**Security**: Validación manual `validateBranchAccess(branchId)`  
**Description**: Cancela la suscripción activa de la sede.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede no existe o sin suscripción activa |
| 409 | `CONFLICT` | No se puede cancelar (estado inválido) |

### Important Considerations

- **Efecto**: Suscripción pasa a `CANCELED`; sede pierde acceso a funcionalidades premium
- **Reactivación**: Requiere nuevo `POST /subscriptions`