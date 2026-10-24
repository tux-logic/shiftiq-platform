# Core — Customer Profiles

**Base Path**: `/api/v1/customers`  
**Context**: Core Domain  
**Auth Pattern**: Idéntico a Employees — POST/GET → `isAuthorizedForUser` (ADMIN, OWNER bypass, self) | PUT/DELETE → `validateSelfAccess` (solo ADMIN o self, **SIN bypass OWNER**)

---

## `POST /api/v1/customers` — Crear Registro de Cliente

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForUser(#resource.userId())")`  
**Description**: Crea perfil de cliente vinculado a User (IAM). Permite ADMIN, OWNER (bypass global), o el propio usuario.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Body | User ID (IAM) |
| `firstName` | string | Sí | Body | Nombres |
| `lastName` | string | Sí | Body | Apellidos |
| `businessName` | string | No | Body | Razón social (si cliente corporativo) |
| `documentType` | string (enum) | Sí | Body | `DNI`, `RUC`, `CE`, `PASSPORT` |
| `documentNumber` | string | Sí | Body | Número documento (único) |
| `phone` | string | Sí | Body | Teléfono (ej: `+51922334455`) |

**Request Example**:
```json
{
  "userId": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "firstName": "María",
  "lastName": "Fernández",
  "businessName": null,
  "documentType": "DNI",
  "documentNumber": "48765432",
  "phone": "+51922334455"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "userId": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "isCorporate": false,
  "firstName": "María",
  "lastName": "Fernández",
  "businessName": null,
  "documentType": "DNI",
  "documentNumber": "48765432",
  "phone": "+51922334455"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | DNI/RUC inválido, duplicado |
| 403 | `ACCESS_DENIED` | No autorizado para ese userId (`isAuthorizedForUser`) |
| 404 | `NOT_FOUND` | User no existe |
| 409 | `CONFLICT` | Documento ya registrado |

### Important Considerations

- **`isCorporate`**: Se deduce automáticamente (`true` si `businessName` presente y `documentType=RUC`)
- **Autorización POST**: `isAuthorizedForUser(userId)` — ADMIN ✓, OWNER ✓ (bypass), Self ✓
- **Vinculación a sede**: Perfil global; asignación a sede en Fleet (`/customer-registrations`)

---

## `GET /api/v1/customers/{customerId}` — Obtener Cliente por ID

**Security**: Validación manual `validateUserAccess(userId del customer)`  
**Description**: Retorna perfil de cliente. Usa `isAuthorizedForUser` (permite OWNER bypass).

### Success Response (200 OK)

```json
{
  "id": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "userId": "7b8f9e01-1234-4567-89ab-cdef01234567",
  "isCorporate": false,
  "firstName": "María",
  "lastName": "Fernández",
  "businessName": null,
  "documentType": "DNI",
  "documentNumber": "48765432",
  "phone": "+51922334455"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No autorizado (`isAuthorizedForUser`) |
| 404 | `NOT_FOUND` | Cliente no existe |

---

## `GET /api/v1/customers?userId={userId}` — Obtener Cliente por User ID

**Security**: Validación manual `validateUserAccess(userId)`  
**Description**: Busca perfil por User ID (IAM).

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Query | User ID (IAM) |

### Success Response (200 OK)

`CustomerResource`.

---

## `PUT /api/v1/customers/{customerId}` — Actualizar Cliente

**Security**: Validación manual `validateSelfAccess(userId del customer existente)`  
**Description**: Actualiza perfil de cliente. **Solo ADMIN o el propio usuario** — ROLE_OWNER **NO** bypass.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `firstName` | string | No | Nombres |
| `lastName` | string | No | Apellidos |
| `businessName` | string | No | Razón social |
| `documentType` | string | No | Tipo documento |
| `documentNumber` | string | No | Número documento (único) |
| `phone` | string | No | Teléfono |

### Success Response (200 OK)

`CustomerResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Documento duplicado, formato inválido |
| 403 | `ACCESS_DENIED` | **No es el propio usuario ni ADMIN** (`validateSelfAccess` — SIN bypass OWNER) |
| 404 | `NOT_FOUND` | Cliente no existe |
| 409 | `CONFLICT` | Documento ya en uso |

### Important Considerations

- **🔴 Igual que Employees**: PUT usa `validateSelfAccess` — **ROLE_OWNER NO puede actualizar clientes ajenos**
- **Cliente corporativo**: Si cambia `businessName` + `documentType=RUC`, `isCorporate` se recalcula

---

## `DELETE /api/v1/customers/{customerId}` — Eliminar Cliente

**Security**: Validación manual `validateSelfAccess(userId del customer existente)`  
**Description**: Elimina perfil de cliente. **Solo ADMIN o el propio usuario** — ROLE_OWNER **NO** bypass.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | **No es el propio usuario ni ADMIN** (SIN bypass OWNER) |
| 404 | `NOT_FOUND` | Cliente no existe |
| 409 | `CONFLICT` | Cliente tiene registros/pedidos activos (validar en Fleet/Billing) |

### Important Considerations

- **🔴 RESUMEN AUTORIZACIÓN (Igual que Employees)**:
  - **POST/GET**: `isAuthorizedForUser` → ADMIN ✓, OWNER ✓ (bypass global), Self ✓
  - **PUT/DELETE**: `validateSelfAccess` → ADMIN ✓, Self ✓, **OWNER ✗**
- **Propósito**: Cliente gestiona su propio perfil; ADMIN gestiona todos; OWNER solo lee/crea (no edita/borra ajenos)