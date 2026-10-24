# Core — Employee Profiles

**Base Path**: `/api/v1/employees`  
**Context**: Core Domain  
**Auth Pattern**: POST/GET → `isAuthorizedForUser` (ADMIN, OWNER bypass, self) | PUT/DELETE → `validateSelfAccess` (solo ADMIN o self, **SIN bypass OWNER**)

---

## `POST /api/v1/employees` — Crear Registro de Empleado

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForUser(#resource.userId())")`  
**Description**: Crea perfil de empleado vinculado a User (IAM). Permite ADMIN, OWNER (bypass global), o el propio usuario.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Body | User ID (IAM) |
| `firstName` | string | Sí | Body | Nombres |
| `lastName` | string | Sí | Body | Apellidos |
| `documentType` | string (enum) | Sí | Body | `DNI`, `RUC`, `CE`, `PASSPORT` |
| `documentNumber` | string | Sí | Body | Número documento (único, 8 dígitos DNI) |
| `phone` | string | Sí | Body | Teléfono (ej: `+51912345678`) |

**Request Example**:
```json
{
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Luis",
  "lastName": "Mendoza",
  "documentType": "DNI",
  "documentNumber": "71234567",
  "phone": "+51912345678"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Luis",
  "lastName": "Mendoza",
  "documentType": "DNI",
  "documentNumber": "71234567",
  "phone": "+51912345678"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | DNI inválido (no 8 dígitos), duplicado, userId inválido |
| 403 | `ACCESS_DENIED` | No autorizado para ese userId (`isAuthorizedForUser`) |
| 404 | `NOT_FOUND` | User no existe |
| 409 | `CONFLICT` | Documento ya registrado |

### Important Considerations

- **Autorización POST**: `isAuthorizedForUser(userId)` permite:
  - ADMIN (cualquiera)
  - ROLE_OWNER (bypass global — perfiles sin sede)
  - El propio usuario (self)
- **User previo**: Debe existir en IAM (rol típico `ROLE_EMPLOYEE` o `ROLE_USER`)
- **Vinculación a sede**: Este perfil es **global**; la asignación a sede se hace en Fleet (`/employee-registrations`)

---

## `GET /api/v1/employees/{employeeId}` — Obtener Empleado por ID

**Security**: Validación manual `validateUserAccess(userId del employee)`  
**Description**: Retorna perfil de empleado. Usa `isAuthorizedForUser` (permite OWNER bypass).

### Success Response (200 OK)

```json
{
  "id": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Luis",
  "lastName": "Mendoza",
  "documentType": "DNI",
  "documentNumber": "71234567",
  "phone": "+51912345678"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No autorizado para ese usuario (`isAuthorizedForUser`) |
| 404 | `NOT_FOUND` | Empleado no existe |

---

## `GET /api/v1/employees?userId={userId}` — Obtener Empleado por User ID

**Security**: Validación manual `validateUserAccess(userId)`  
**Description**: Busca perfil por User ID (IAM).

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Query | User ID (IAM) |

### Success Response (200 OK)

`EmployeeResource`.

---

## `GET /api/v1/employees?documentNumber={documentNumber}` — Buscar por DNI

**Security**: Validación manual `validateUserAccess(userId del employee encontrado)`  
**Description**: Busca empleado por número de documento (DNI/RUC).

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `documentNumber` | string | Sí | Query | DNI (8 dígitos) o RUC (11 dígitos) |

### Success Response (200 OK)

`EmployeeResource`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No autorizado para ese empleado (`isAuthorizedForUser`) |
| 404 | `NOT_FOUND` | No existe empleado con ese documento |

---

## `PUT /api/v1/employees/{employeeId}` — Actualizar Empleado

**Security**: Validación manual `validateSelfAccess(userId del employee existente)`  
**Description**: Actualiza perfil de empleado. **Solo ADMIN o el propio usuario** — ROLE_OWNER **NO** tiene bypass aquí.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `firstName` | string | No | Nombres |
| `lastName` | string | No | Apellidos |
| `documentType` | string | No | Tipo documento |
| `documentNumber` | string | No | Número documento (único) |
| `phone` | string | No | Teléfono |

### Success Response (200 OK)

`EmployeeResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | DNI duplicado, formato inválido |
| 403 | `ACCESS_DENIED` | **No es el propio usuario ni ADMIN** (`validateSelfAccess` — SIN bypass OWNER) |
| 404 | `NOT_FOUND` | Empleado no existe |
| 409 | `CONFLICT` | Documento ya en uso |

### Important Considerations

- **🔴 DIFERENCIA CLAVE vs POST/GET**: PUT usa `validateSelfAccess` — **ROLE_OWNER NO puede actualizar empleados ajenos**
- **Propósito**: Un empleado solo puede editar su propio perfil; ADMIN puede editar cualquiera

---

## `DELETE /api/v1/employees/{employeeId}` — Eliminar Empleado

**Security**: Validación manual `validateSelfAccess(userId del employee existente)`  
**Description**: Elimina perfil de empleado. **Solo ADMIN o el propio usuario** — ROLE_OWNER **NO** bypass.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | **No es el propio usuario ni ADMIN** (SIN bypass OWNER) |
| 404 | `NOT_FOUND` | Empleado no existe |
| 409 | `CONFLICT` | Empleado tiene registros activos en Fleet (debe desactivarlos primero) |

### Important Considerations

- **Cascada**: No elimina si hay `EmployeeRegistration` activos → 409
- **User (IAM)**: No se elimina el User
- **🔴 RESUMEN AUTORIZACIÓN**:
  - **POST/GET**: `isAuthorizedForUser` → ADMIN ✓, OWNER ✓ (bypass), Self ✓
  - **PUT/DELETE**: `validateSelfAccess` → ADMIN ✓, Self ✓, **OWNER ✗**