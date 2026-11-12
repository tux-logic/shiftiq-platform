# Operations — Work Orders

**Base Path**: `/api/v1/work-orders`  
**Context**: Operations Domain  
**Description**: Órdenes de trabajo (OT) — ciclo de vida completo desde cita hasta facturación.

---

## `POST /api/v1/work-orders` — Crear Orden de Trabajo

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")`  
**Description**: Abre OT desde cita agendada. Registra km inicial + fotos recepción. Estado inicial: `PENDING`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `appointmentId` | UUID | Sí | Cita origen (debe existir en la sede) |
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
  "internalNumber": 1,
  "formattedNumber": "MOL-01-000001",
  "status": "PENDING",
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
| 409 | `CONFLICT` | Cita ya tiene OT, o cita cancelada |

### Important Considerations

- **Numeración**: `internalNumber` secuencial por sede; `formattedNumber` = `{branchCode}-{internalNumber:06d}` (ej: `MOL-01-000001`)
- **Estados**: `PENDING` → `IN_PROGRESS` → `COMPLETED` → `PAID`
- **Stock**: No reserva stock al crear OT (se reserva al agregar productos a tasks)
- **Analytics**: Emite evento al completar la orden

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
    "internalNumber": 1,
    "formattedNumber": "MOL-01-000001",
    "status": "IN_PROGRESS",
    "diagnosticSummary": "Vibración al frenar",
    "mileageIn": 45200,
    "totalAmount": 200.00,
    "tasks": [ ... ],
    "entryInspectionImages": [ "https://..." ],
    "createdAt": "2026-10-15T10:30:00Z",
    "updatedAt": "2026-10-15T11:00:00Z"
  }
]
```

### Important Considerations

- **Filtro branchId**: Valida `isAuthorizedForBranch(branchId)`
- **Filtro vehicleId**: Busca OTs del vehículo y valida que el caller tenga acceso a la sede de cada OT
- **Sin paginación**: Retorna todas las órdenes asociadas con sus tareas y repuestos

---

## `GET /api/v1/work-orders/{id}` — Detalle Completo de OT

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: **Endpoint principal** — retorna OT con tareas, mecánicos asignados, repuestos, fotos y montos consolidados.

### Success Response (200 OK)

```json
{
  "id": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
  "appointmentId": "appt1appt2-appt3-appt4-appt5appt6appt7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "internalNumber": 1,
  "formattedNumber": "MOL-01-000001",
  "status": "IN_PROGRESS",
  "diagnosticSummary": "Vibración al frenar",
  "mileageIn": 45200,
  "totalAmount": 200.00,
  "tasks": [
    {
      "id": "task1task2-task3-task4-task5task6task7",
      "serviceId": "srv1srv2-srv3-srv4-srv5-srv6srv7srv8",
      "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
      "assignedMechanicId": "ereg1ereg2-ereg3-ereg4-ereg5ereg6ereg7",
      "status": "DOING",
      "description": "Rectificado discos + pastillas",
      "price": 200.00,
      "startedAt": "2026-10-15T11:00:00Z",
      "completedAt": null,
      "products": [
        {
          "id": "wp1wp2wp3-wp4-wp5-wp6-wp7wp8wp9wp0",
          "productId": "prod1prod2-prod3-prod4-prod5prod6prod7",
          "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
          "quantity": 1,
          "unitPrice": 50.00,
          "totalAmount": 50.00,
          "createdAt": "2026-10-15T11:15:00Z"
        }
      ],
      "evidenceImages": ["https://..."],
      "createdAt": "2026-10-15T10:45:00Z"
    }
  ],
  "entryInspectionImages": ["https://..."],
  "createdAt": "2026-10-15T10:30:00Z",
  "updatedAt": "2026-10-15T11:15:00Z"
}
```

---

## `PUT /api/v1/work-orders/{id}` — Actualizar Diagnóstico y Kilometraje

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Actualiza `diagnosticSummary` y `mileageIn` (km entrada).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `diagnosticSummary` | string (5-2000 chars) | Sí | Resumen del diagnóstico técnico inicial |
| `mileageIn` | integer | Sí | Kilometraje de ingreso del vehículo |
| `entryInspectionImages` | string[] (URLs) | No | URLs de fotos tomadas durante la inspección visual inicial |

**Request Example**:
```json
{
  "diagnosticSummary": "Mantenimiento preventivo general, cambio de aceite y pastillas de freno delanteras",
  "mileageIn": 45200,
  "entryInspectionImages": [
    "https://res.cloudinary.com/shiftiq/image/upload/v1234/workorders/inspection1.jpg"
  ]
}
```

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