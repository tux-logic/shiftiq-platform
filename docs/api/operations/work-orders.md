# Operations — Work Orders

**Base Path**: `/api/v1/work-orders`  
**Context**: Operations Domain  
**Description**: Órdenes de trabajo (OT) — ciclo de vida completo desde cita hasta facturación.

---

## `POST /api/v1/work-orders` — Crear Orden de Trabajo

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")`  
**Description**: Abre OT desde cita agendada. Registra km inicial + fotos recepción. Estado inicial: `OPEN`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `appointmentId` | UUID | Sí | Cita origen (debe estar `SCHEDULED`/`CONFIRMED`) |
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `vehicleId` | UUID | Sí | Vehículo (debe coincidir con cita) |
| `customerId` | UUID | Sí | Cliente (debe coincidir con cita) |
| `diagnosticSummary` | string | Sí | Diagnóstico inicial del cliente |
| `mileageIn` | integer | Sí | Kilometraje entrada (odómetro) |
| `entryInspectionImages` | string[] (URLs) | No | Fotos recepción (Cloudinary URLs) |

**Request Example**:
```json
{
  "appointmentId": "appt1appt2-appt3-appt4-appt5appt6appt7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "diagnosticSummary": "Cliente reporta vibración en volante al frenar a 80 km/h",
  "mileageIn": 45200,
  "entryInspectionImages": [
    "https://res.cloudinary.com/shiftiq/image/upload/v1/inspections/front-bumper.jpg",
    "https://res.cloudinary.com/shiftiq/image/upload/v1/inspections/dashboard-mileage.jpg"
  ]
}
```

### Success Response (201 CREATED)

```json
{
  "id": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
  "appointmentId": "appt1appt2-appt3-appt4-appt5appt6appt7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "internalNumber": 1042,
  "formattedNumber": "OT-2026-001042",
  "status": "OPEN",
  "diagnosticSummary": "Cliente reporta vibración en volante al frenar a 80 km/h",
  "mileageIn": 45200,
  "totalAmount": 0.00,
  "tasks": [],
  "entryInspectionImages": [
    "https://res.cloudinary.com/shiftiq/image/upload/v1/inspections/front-bumper.jpg",
    "https://res.cloudinary.com/shiftiq/image/upload/v1/inspections/dashboard-mileage.jpg"
  ],
  "createdAt": "2026-10-15T10:30:00Z",
  "updatedAt": "2026-10-15T10:30:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Cita inválida, km no numérico, IDs no coinciden |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cita/Vehículo/Cliente/Sede no existe |
| 409 | `CONFLICT` | Cita ya tiene OT, o cita `CANCELLED`/`COMPLETED` |

### Important Considerations

- **Numeración**: `internalNumber` secuencial por sede; `formattedNumber` = `OT-{AÑO}-{internalNumber:06d}`
- **Cita origen**: Debe estar `SCHEDULED` o `CONFIRMED`; al crear OT, cita pasa a `IN_PROGRESS` (implícito)
- **Stock**: No reserva stock al crear OT (se reserva al agregar productos a tasks)
- **Analytics**: No emite evento aún (solo al completar)

---

## `GET /api/v1/work-orders` — Listar Órdenes de Trabajo

**Security**: Validación manual según filtros  
**Description**: Lista OTs con filtro obligatorio `branchId` O `vehicleId`.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | No* | Filtrar por sede (requiere acceso) |
| `vehicleId` | UUID | No* | Filtrar por vehículo |

*Al menos uno requerido (branchId XOR vehicleId). Si ninguno → 400.

### Success Response (200 OK)

```json
[
  {
    "id": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
    "appointmentId": "appt1appt2-appt3-appt4-appt5appt6appt7",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
    "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
    "internalNumber": 1042,
    "formattedNumber": "OT-2026-001042",
    "status": "OPEN",
    "diagnosticSummary": "Vibración al frenar",
    "mileageIn": 45200,
    "totalAmount": 0.00,
    "createdAt": "2026-10-15T10:30:00Z"
  }
]
```

### Important Considerations

- **Filtro branchId**: Valida `isAuthorizedForBranch(branchId)`
- **Filtro vehicleId**: Busca OTs → filtra post-query `isAuthorizedForBranch(ot.branchId)`
- **Sin paginación**: Retorna todas (considerar para volúmenes altos)

---

## `GET /api/v1/work-orders/{id}` — Detalle Completo de OT

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: **Endpoint principal** — retorna OT con tareas, mecánicos, repuestos, fotos, montos.

### Success Response (200 OK)

```json
{
  "id": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
  "appointmentId": "appt1appt2-appt3-appt4-appt5appt6appt7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "internalNumber": 1042,
  "formattedNumber": "OT-2026-001042",
  "status": "OPEN",
  "diagnosticSummary": "Vibración al frenar",
  "mileageIn": 45200,
  "totalAmount": 1250.00,
  "tasks": [
    {
      "id": "task1task2-task3-task4-task5task6task7",
      "serviceId": "srv1srv2-srv3-srv4-srv5-srv6srv7srv8",
      "serviceName": "Cambio pastillas freno delanteras",
      "description": "Rectificado discos + pastillas",
      "status": "COMPLETED",
      "assignedMechanicId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
      "mechanicName": "Luis Mendoza",
      "evidenceImages": ["https://..."],
      "products": [
        {
          "id": "prod1prod2-prod3-prod4-prod5prod6prod7",
          "productId": "prod1prod2-prod3-prod4-prod5prod6prod7",
          "productName": "Pastillas Freno Delanteras",
          "quantity": 1,
          "unitPrice": 180.00,
          "totalAmount": 180.00
        }
      ],
      "totalAmount": 260.00,
      "startedAt": "2026-10-15T11:00:00Z",
      "completedAt": "2026-10-15T13:30:00Z"
    }
  ],
  "entryInspectionImages": ["https://..."],
  "exitInspectionImages": [],
  "createdAt": "2026-10-15T10:30:00Z",
  "updatedAt": "2026-10-15T14:00:00Z"
}
```

---

## `PUT /api/v1/work-orders/{id}` — Actualizar Diagnóstico y Kilometraje

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Actualiza `diagnosticSummary` y `mileageIn` (km entrada).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `diagnosticSummary` | string | No | Nuevo diagnóstico |
| `mileageIn` | integer | No | Nuevo km entrada |

### Success Response (200 OK)

`WorkOrderResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | KM inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | OT no existe |

---

## `POST /api/v1/work-orders/{id}/complete` — Completar Orden de Trabajo

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Cierra OT. **Requisito**: Todas las tasks deben estar `COMPLETED`. Dispara `WorkOrderCompletedEvent` → Analytics + Billing.

### Success Response (200 OK)

`WorkOrderResource` con `status: "COMPLETED"`, `totalAmount` calculado, `completedAt` seteado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | OT no existe |
| 409 | `CONFLICT` | Hay tasks no `COMPLETED` (PENDING/DOING) |

### Important Considerations

- **Validación estricta**: `ALL tasks == COMPLETED` → sino 409
- **Eventos**: Emite `WorkOrderCompletedEvent` →
  - Analytics: `totalRevenue += totalAmount`, `completedWorkOrdersCount++`
  - Billing: Habilita `POST /quotes` para esta OT
- **Stock**: Libera reservas de productos en tasks completadas (ya no necesarias)
- **Irreversible**: No hay "descompletar" — usar `DELETE` para cancelar y liberar stock

---

## `DELETE /api/v1/work-orders/{id}` — Eliminar Orden de Trabajo

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Cancela y borra OT. **Libera inmediatamente todas las reservas de stock** en tareas.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | OT no existe |
| 409 | `CONFLICT` | OT ya `COMPLETED` (no se puede borrar, usar reabrir tasks) |

### Important Considerations

- **Soft delete**: Estado `DELETED` (no borrado físico)
- **Stock**: Libera reservas de `WorkOrderTaskProduct` en todas las tasks
- **Cita origen**: Vuelve a estado previo (`SCHEDULED`/`CONFIRMED`)
- **Irreversible**: No se puede recuperar — crear nueva OT desde cita