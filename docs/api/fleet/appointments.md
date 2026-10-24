# Fleet — Appointments

**Base Path**: `/api/v1/appointments`  
**Context**: Fleet Domain  
**Description**: Agendamiento de citas de taller. Valida acceso a sede, cliente y vehículo.

---

## `POST /api/v1/appointments` — Agendar Nueva Cita

**Security**: `@PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId()))")`  
**Description**: Crea cita para cliente + vehículo en sede. Emite `AppointmentCreatedEvent` → Analytics.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `customerId` | UUID | Sí | Customer profile (Core) |
| `vehicleId` | UUID | Sí | Vehicle ID (IoT) — debe tener registro activo en la sede |
| `scheduledStart` | ISO 8601 datetime | Sí | Inicio (ej: `2026-10-15T09:00:00`) — futuro |
| `scheduledEnd` | ISO 8601 datetime | No | Fin (default: `scheduledStart` + 1 hora) |
| `notes` | string | No | Observaciones (max 500 chars) |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "scheduledStart": "2026-10-15T09:00:00",
  "scheduledEnd": "2026-10-15T10:00:00",
  "notes": "Mantenimiento preventivo 20,000 km y revisión frenos"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "appt1appt2-appt3-appt4-appt5appt6appt7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "scheduledStart": "2026-10-15T09:00:00",
  "scheduledEnd": "2026-10-15T10:00:00",
  "status": "SCHEDULED",
  "notes": "Mantenimiento preventivo 20,000 km y revisión frenos"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Fechas pasadas, UUIDs inválidos, vehículo sin registro en sede |
| 403 | `ACCESS_DENIED` | Sin acceso a `branchId` |
| 404 | `NOT_FOUND` | Cliente/Vehículo/Sede no existe |
| 409 | `CONFLICT` | Solapamiento de cita para mismo vehículo/hora |

### Important Considerations

- **Validaciones**: `vehicleId` debe tener `CustomerRegistration` ACTIVE en `branchId` + `VehicleRegistration` ACTIVE (vinculado a cliente)
- **Estado inicial**: `SCHEDULED` →可转 `CONFIRMED` → `IN_PROGRESS` → `COMPLETED`/`CANCELLED`
- **Analytics**: Emite `AppointmentCreatedEvent` → `AnalyticsDeltaDispatcher` → `BranchAnalyticsSnapshot.totalAppointmentsCount++` (UPSERT atómico)
- **Duración**: Default 1 hora si no se envía `scheduledEnd`

---

## `GET /api/v1/appointments` — Listar Citas con Filtros

**Security**: Validación manual según filtros usados  
**Description**: Lista citas con filtros combinados. Valida acceso a sede si se usa `branchId`.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | No | Filtrar por sede (requiere `isAuthorizedForBranch`) |
| `status` | string (enum) | No | `SCHEDULED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `customerId` | UUID | No | Filtrar por cliente |
| `vehicleId` | UUID | No | Filtrar por vehículo |

**Combinaciones válidas**:
- `branchId` + `status` → lista por sede y estado (valida acceso a sede)
- `branchId` solo → lista por sede (valida acceso)
- `customerId` solo → lista por cliente (filtra por sedes accesibles del caller)
- `vehicleId` solo → lista por vehículo (filtra por sedes accesibles)
- Sin `branchId` + sin `customerId` + sin `vehicleId` → **400 Bad Request**

### Success Response (200 OK)

```json
[
  {
    "id": "appt1appt2-appt3-appt4-appt5appt6appt7",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
    "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
    "scheduledStart": "2026-10-15T09:00:00",
    "scheduledEnd": "2026-10-15T10:00:00",
    "status": "SCHEDULED",
    "notes": "Mantenimiento preventivo 20,000 km"
  }
]
```

### Important Considerations

- **Filtro customerId/vehicleId**: Retorna citas de **todas las sedes** → filtra post-query `isAuthorizedForBranch(cita.branchId)` → solo muestra accesibles
- **Paginación**: No implementada (retorna todas) — considerar para volúmenes altos

---

## `GET /api/v1/appointments/{appointmentId}` — Detalle de Cita

**Security**: Validación manual `isAuthorizedForBranch(cita.branchId)`  
**Description**: Retorna cita completa por ID.

### Success Response (200 OK)

`AppointmentResource` con todos los campos.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede de la cita |
| 404 | `NOT_FOUND` | Cita no existe |

---

## `PUT /api/v1/appointments/{appointmentId}` — Modificar Cita

**Security**: Validación manual `isAuthorizedForBranch(citaExistente.branchId)` + `isAuthorizedForBranch(nuevoBranchId)` si cambia  
**Description**: Actualiza cita completa. Valida acceso a sede actual y nueva (si cambia).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (puede cambiar si caller tiene acceso a ambas) |
| `customerId` | UUID | Sí | Cliente |
| `vehicleId` | UUID | Sí | Vehículo |
| `scheduledStart` | ISO 8601 | Sí | Nuevo inicio |
| `scheduledEnd` | ISO 8601 | No | Nuevo fin (default +1h) |
| `status` | string (enum) | Sí | Nuevo estado |
| `notes` | string | No | Nuevas notas |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "scheduledStart": "2026-10-16T14:00:00",
  "scheduledEnd": "2026-10-16T16:00:00",
  "status": "CONFIRMED",
  "notes": "Cliente reprogramó para la tarde"
}
```

### Success Response (200 OK)

`AppointmentResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Fechas inválidas, solapamiento |
| 403 | `ACCESS_DENIED` | Sin acceso a sede actual o nueva |
| 404 | `NOT_FOUND` | Cita no existe |
| 409 | `CONFLICT` | Solapamiento en nueva fecha/hora |

### Important Considerations

- **Cambio de sede**: Requiere acceso a **ambas** sedes (actual y nueva)
- **Cambio de estado**: Validaciones de transición (ej: no `COMPLETED` → `SCHEDULED`)
- **Analytics**: Cambios de estado emiten eventos correspondientes

---

## `DELETE /api/v1/appointments/{appointmentId}` — Cancelar Cita

**Security**: Validación manual `isAuthorizedForBranch(cita.branchId)`  
**Description**: Soft delete — estado `CANCELLED`. Libera slot horario.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cita no existe |
| 409 | `CONFLICT` | Cita ya `COMPLETED` (no se puede cancelar) |

### Important Considerations

- **No borrado físico**: Estado `CANCELLED` para auditoría
- **Work Order**: Si ya se creó OT desde esta cita, no se permite cancelar (409)
- **Analytics**: `totalAppointmentsCount` no decrementa (snapshot histórico)