# Core — Operating Hours & Holiday Exceptions

**Context**: Core Domain  
**Description**: Configuración de horarios de atención semanales y excepciones por fecha (feriados, medios días, cierres puntuales) con herencia **Workshop (global) → Branch (override por sede)**.

---

## Orden de Precedencia (Resolución Efectiva)

Para una sede `branchId` y una fecha `date` (`YYYY-MM-DD`), la disponibilidad (`GET /api/v1/branches/{branchId}/availability`) y la validación de citas (`POST`/`PUT /api/v1/appointments`) resuelven el horario efectivo en este orden:

1. **Excepción de fecha de la Sede** (`branchId + date`)
2. **Excepción de fecha del Taller** (`workshopId + date`)
3. **Horario semanal de la Sede** (`branchId + dayOfWeek`)
4. **Horario semanal global del Taller** (`workshopId + dayOfWeek`)
5. **Fallback por defecto** (`open: true`, `08:30 - 18:00`, slots de `60` min) si el taller aún no configuró horarios.

---

## Horario Semanal Global del Taller

### `GET /api/v1/workshops/{workshopId}/operating-hours`
- **Security**: `validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE)`
- **Response**: Lista de los 7 días (`MONDAY` .. `SUNDAY`) con `open`, `openTime`, `closeTime`, `slotDurationMinutes` y `source` (`WORKSHOP` o `DEFAULT`).

### `PUT /api/v1/workshops/{workshopId}/operating-hours`
- **Security**: `validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE)`
- **Request**:
```json
{
  "days": [
    { "dayOfWeek": "MONDAY", "open": true, "openTime": "08:30", "closeTime": "18:00", "slotDurationMinutes": 60 },
    { "dayOfWeek": "SUNDAY", "open": false, "openTime": null, "closeTime": null, "slotDurationMinutes": 60 }
  ]
}
```

---

## Horario Semanal por Sede (Override)

### `GET /api/v1/branches/{branchId}/operating-hours`
- **Security**: `@PreAuthorize("isAuthenticated()")`
- **Response**: Horario semanal efectivo de la sede (`source`: `BRANCH`, `WORKSHOP` o `DEFAULT`).

### `PUT /api/v1/branches/{branchId}/operating-hours`
- **Security**: `validateBranchPermission(branchId, Permission.BRANCH_MANAGE)`
- **Description**: Define overrides propios para los días indicados sin afectar a otras sedes del taller.

### `DELETE /api/v1/branches/{branchId}/operating-hours`
- **Security**: `validateBranchPermission(branchId, Permission.BRANCH_MANAGE)`
- **Description**: Elimina los overrides de la sede para volver a heredar el horario global del taller.

---

## Excepciones por Fecha (Feriados / Días Especiales)

Cada taller o sede decide si cierra (`open: false`) o atiende en horario especial (`open: true`, ej. medio día `09:00 - 13:00`) en una fecha puntual.

- **Global del Taller** (`validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE)`):
  - `GET /api/v1/workshops/{workshopId}/operating-hours/exceptions`
  - `POST /api/v1/workshops/{workshopId}/operating-hours/exceptions`
  - `DELETE /api/v1/workshops/{workshopId}/operating-hours/exceptions/{exceptionId}`
- **Por Sede**:
  - `GET /api/v1/branches/{branchId}/operating-hours/exceptions` (`@PreAuthorize("isAuthenticated()")`)
  - `POST /api/v1/branches/{branchId}/operating-hours/exceptions` (`validateBranchPermission(branchId, Permission.BRANCH_MANAGE)`)
  - `DELETE /api/v1/branches/{branchId}/operating-hours/exceptions/{exceptionId}` (`validateBranchPermission(branchId, Permission.BRANCH_MANAGE)`)

**Request Example (`POST .../exceptions`)**:
```json
{
  "date": "2026-07-28",
  "open": false,
  "openTime": null,
  "closeTime": null,
  "reason": "Fiestas Patrias"
}
```
