# Fleet — Employee Registrations (Hierarchical Onboarding)

**Base Path**: `/api/v1/employee-registrations`  
**Context**: Fleet Domain  
**Critical**: Jerarquía estricta de autorización vía `MultiTenancySecurityService.canManageStaff(targetRole, branchId)`

---

## Matriz de Autorización `canManageStaff`

| Rol Caller | Puede dar de alta | Condición |
|------------|-------------------|-----------|
| `ROLE_ADMIN` | Cualquier (`BRANCH_MANAGER`, `ASSISTANT`, `EMPLOYEE`) | Sin restricción |
| `ROLE_OWNER` | `ROLE_BRANCH_MANAGER` | **Solo talleres multi-sede** (debe delegar) |
| `ROLE_OWNER` | `ROLE_ASSISTANT`, `ROLE_EMPLOYEE` | **Solo talleres mono-sede** (owner asume gerencia) |
| `ROLE_BRANCH_MANAGER` | `ROLE_ASSISTANT`, `ROLE_EMPLOYEE` | Solo en sus sedes asignadas |
| `ROLE_ASSISTANT` | `ROLE_EMPLOYEE` | Solo en sus sedes asignadas |
| `ROLE_EMPLOYEE` | *Nadie* | — |

**Validación**: `@PreAuthorize("isAuthenticated()")` + `multiTenancySecurityService.validateStaffManagement(targetRole, branchId)`

---

## `POST /api/v1/employee-registrations` — Crear Registro de Personal (Onboarding Jerárquico)

**Security**: `validateStaffManagement(targetRole, branchId)`  
**Description**: Da de alta personal en sede. Valida jerarquía, especialidad en catálogo del taller, y asigna rol/sedes en IAM.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `employeeId` | UUID | Sí | Employee profile ID (Core) |
| `branchId` | UUID | Sí | Sede destino (caller debe tener acceso + jerarquía) |
| `speciality` | string | Sí | Código especialidad (debe existir en catálogo activo del taller) |
| `specialityName` | string | Sí | Nombre visible de la especialidad |
| `salary` | number (decimal) | Sí | Salario mensual (soles) |
| `role` | string | No | `ROLE_BRANCH_MANAGER`, `ROLE_ASSISTANT`, `ROLE_EMPLOYEE` (default: `ROLE_EMPLOYEE`). Acepta con/sin prefijo `ROLE_` |

**Request Example** (Owner creando Gerente en multi-sede):
```json
{
  "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "speciality": "GENERAL_MECHANIC",
  "specialityName": "Gerencia General de Sede Norte",
  "salary": 4500.00,
  "role": "ROLE_BRANCH_MANAGER"
}
```

**Request Example** (Asistente creando Mecánico):
```json
{
  "employeeId": "emp2emp3-emp4-emp5-emp6-emp7emp8emp9",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "speciality": "ELECTRICIAN",
  "specialityName": "Técnico Electricista y Diagnóstico",
  "salary": 3000.00,
  "role": "ROLE_EMPLOYEE"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "ereg1ereg2-ereg3-ereg4-ereg5-ereg6ereg7er",
  "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "speciality": "GENERAL_MECHANIC",
  "specialityName": "Gerencia General de Sede Norte",
  "salary": 4500.00,
  "role": "ROLE_BRANCH_MANAGER",
  "status": "ACTIVE",
  "createdAt": "2026-10-15T10:30:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Especialidad no en catálogo (`fleet.error.employeeRegistration.specialtyNotInCatalog`), rol no permitido (`ADMIN`, `OWNER`), datos inválidos |
| 403 | `ACCESS_DENIED` | Jerarquía violada (`canManageStaff` false), sin acceso a sede |
| 404 | `NOT_FOUND` | Employee/Sede no existe |
| 409 | `CONFLICT` | Employee ya tiene registro ACTIVE en esa sede |

### Important Considerations

- **Especialidad válida**: Debe existir en `WorkshopSpecialty` del taller dueño de la sede (`active=true`)
- **Rol en IAM**: Se asigna **realmente** al User en IAM:
  - `user.role` = rol del registro (si no degrada jerarquía)
  - `user.branchIds` = sedes de registros ACTIVE
- **No degradación**: Si user ya tiene `ROLE_BRANCH_MANAGER` y se registra como `EMPLOYEE`, **conserva `BRANCH_MANAGER`** (log WARN)
- **JWT refresh**: Usuario debe re-logear o refrescar sesión para ver nuevo rol/sedes en token
- **Roles rechazados**: `ROLE_ADMIN`, `ROLE_OWNER` → 400

---

## `POST /api/v1/employee-registrations/request-join` — Mecánico Solicita Unirse (Request-Join)

**Security**: `@PreAuthorize("isAuthenticated()")` + caller debe tener `ROLE_EMPLOYEE`  
**Description**: Mecánico postula a sede. Estado: `PENDING_APPROVAL`. **No otorga permisos ni sedes hasta aprobación**.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `employeeId` | UUID | Sí | Employee profile ID (del caller) |
| `branchId` | UUID | Sí | Sede destino |
| `speciality` | string | Sí | Código especialidad (catálogo activo del taller) |
| `specialityName` | string | Sí | Nombre especialidad |
| `salary` | number | Sí | Salario pretendido |

**Request Example**:
```json
{
  "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "speciality": "MOTOR_DIESEL",
  "specialityName": "Especialista en Motores Diésel e Inyección",
  "salary": 2500.00
}
```

### Success Response (201 CREATED)

```json
{
  "id": "ereg2ereg3-ereg4-ereg5-ereg6-ereg7ereg8er",
  "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "speciality": "MOTOR_DIESEL",
  "specialityName": "Especialista en Motores Diésel e Inyección",
  "salary": 2500.00,
  "status": "PENDING_APPROVAL",
  "createdAt": "2026-10-15T10:30:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Especialidad no en catálogo, caller no es `ROLE_EMPLOYEE` |
| 403 | `ACCESS_DENIED` | Caller no tiene rol `ROLE_EMPLOYEE` |
| 409 | `CONFLICT` | Ya tiene solicitud pendiente o registro activo en esa sede |

---

## `POST /api/v1/employee-registrations/{id}/approve` — Aprobar Mecánico

**Security**: `validateStaffManagement("ROLE_EMPLOYEE", registration.branchId)`  
**Description**: Aprueba solicitud `PENDING_APPROVAL` → `ACTIVE`. Asigna sede + rol `ROLE_EMPLOYEE` en IAM (sin degradar).

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `id` | UUID | Sí | Registration ID (path) |

### Success Response (200 OK)

```json
{
  "id": "ereg2ereg3-ereg4-ereg5-ereg6-ereg7ereg8er",
  "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "speciality": "MOTOR_DIESEL",
  "specialityName": "Especialista en Motores Diésel e Inyección",
  "salary": 2500.00,
  "role": "ROLE_EMPLOYEE",
  "status": "ACTIVE",
  "createdAt": "2026-10-15T10:30:00Z",
  "approvedAt": "2026-10-15T14:00:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No autorizado para aprobar (jerarquía/sede) |
| 404 | `NOT_FOUND` | Registro no existe |
| 409 | `CONFLICT` | Registro no está `PENDING_APPROVAL` |

### Important Considerations

- **Quién aprueba**: `ROLE_OWNER` (dueño del taller) o `ROLE_BRANCH_MANAGER` de esa sede
- **Efecto IAM**: `user.branchIds` += branchId; `user.role` = `ROLE_EMPLOYEE` (si no degrada)
- **Notificación**: Frontend debe guiar a usuario a refrescar sesión (`/sessions/refresh`)

---

## `POST /api/v1/employee-registrations/{id}/reject` — Rechazar Solicitud

**Security**: `isAuthorizedForBranch(registration.branchId)`  
**Description**: Rechaza solicitud `PENDING_APPROVAL` → `REJECTED`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `reason` | string | No | Motivo del rechazo |

**Request Example**:
```json
{
  "reason": "La sede ya no cuenta con vacantes para el turno solicitado"
}
```

### Success Response (200 OK)

`EmployeeRegistrationResource` con `status: "REJECTED"`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Registro no existe |
| 409 | `CONFLICT` | Registro no está `PENDING_APPROVAL` |

---

## `GET /api/v1/employee-registrations` — Listar Registros

**Security**: Validación manual según filtros  
**Description**: Lista con filtros. Valida acceso a sede si se filtra por `branchId`.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | No | Filtrar por sede (requiere acceso) |
| `status` | string | No | `ACTIVE`, `PENDING_APPROVAL`, `REJECTED`, `INACTIVE` |
| `employeeId` | UUID | No | Filtrar por empleado |

### Success Response (200 OK)

Array de `EmployeeRegistrationResource`.

---

## `GET /api/v1/employee-registrations/{id}` — Detalle de Registro

**Security**: Validación manual `isAuthorizedForBranch(registration.branchId)`  
**Description**: Detalle por ID.

### Success Response (200 OK)

`EmployeeRegistrationResource` completo.

---

## `PUT /api/v1/employee-registrations/{id}` — Actualizar Especialidad / Salario

**Security**: Validación manual `isAuthorizedForBranch(registration.branchId)`  
**Description**: Actualiza `speciality`, `specialityName`, `salary`. **No cambia rol ni estado**.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `speciality` | string | No | Nuevo código especialidad (catálogo activo) |
| `specialityName` | string | No | Nuevo nombre |
| `salary` | number | No | Nuevo salario |

### Success Response (200 OK)

`EmployeeRegistrationResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Especialidad no en catálogo |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Registro no existe |

---

## `DELETE /api/v1/employee-registrations/{id}` — Dar de Baja (Soft Delete)

**Security**: Validación manual `isAuthorizedForBranch(registration.branchId)`  
**Description**: Pasa a `INACTIVE`. Recalcula sedes del user en IAM (si no quedan registros ACTIVE, pierde `branchIds`).

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Registro no existe |

### Important Considerations

- **Sincronización IAM**: `ExternalIamService.setBranches(userId, activeBranchIds)` — si `activeBranchIds` vacío, user pierde acceso a datos operativos
- **Rol en IAM**: **No se degrada** al dar de baja — user conserva su rol máximo histórico
- **Reactivación**: Crear nuevo registro o `PUT` status `ACTIVE` (si existe)

---

## `GET /api/v1/branches/{branchId}/employees/{employeeId}/history` — Historial de Carrera del Empleado

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Controller**: `StaffCareerHistoryController`  
**Description**: Retorna historial cronológico de snapshots de carrera, ajustes de especialidad y revisiones salariales de un empleado en una sede.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (path, caller debe tener acceso) |
| `employeeId` | UUID | Sí | EmployeeRegistration ID (path) |

### Success Response (200 OK)

```json
[
  {
    "id": "hist1hist2-hist3-hist4-hist5hist6hist7",
    "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "speciality": "GENERAL_MECHANIC",
    "specialityName": "Mecánica General",
    "salary": 2500.00,
    "role": "ROLE_EMPLOYEE",
    "status": "ACTIVE",
    "changedAt": "2026-10-15T10:30:00Z",
    "changeReason": "Promoción a Mecánico Senior"
  }
]
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Empleado/Sede no existe |

### Important Considerations

- **Fuente**: Tabla `staff_career_history` (poblada por triggers/eventos en cambios de registro)
- **Orden**: Cronológico ascendente por `changedAt`
- **Uso**: Auditoría, revisión salarial, compliance laboral
- **Autorización**: `isAuthorizedForBranch(branchId)` — ADMIN, OWNER del taller, o staff asignado a la sede