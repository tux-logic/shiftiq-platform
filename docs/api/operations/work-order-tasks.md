# Operations — Work Order Tasks & Products

**Base Paths**: `/api/v1/work-orders/{id}/tasks`, `/api/v1/work-order-tasks/{taskId}/*`  
**Context**: Operations Domain  
**Description**: Gestión de tareas mecánicas, asignación de personal, repuestos (con reserva de stock).

---

## `POST /api/v1/work-orders/{id}/tasks` — Agregar Tarea a la OT

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Agrega tarea a OT existente. Estado inicial: `PENDING`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `serviceId` | UUID | Sí | Servicio del catálogo (misma sede que OT) |
| `description` | string | Sí | Descripción técnica |
| `evidenceImages` | string[] (URLs) | No | Fotos evidencia (Cloudinary) |

**Request Example**:
```json
{
  "serviceId": "srv1srv2-srv3-srv4-srv5-srv6srv7srv8",
  "description": "Rectificado de discos y cambio de pastillas delanteras",
  "evidenceImages": [
    "https://res.cloudinary.com/shiftiq/image/upload/v1/tasks/disco-desgastado.jpg"
  ]
}
```

### Success Response (201 CREATED)

```json
{
  "id": "task1task2-task3-task4-task5task6task7",
  "serviceId": "srv1srv2-srv3-srv4-srv5-srv6srv7srv8",
  "serviceName": "Cambio pastillas freno delanteras",
  "description": "Rectificado de discos y cambio de pastillas delanteras",
  "status": "PENDING",
  "evidenceImages": ["https://..."],
  "products": [],
  "totalAmount": 0.00,
  "createdAt": "2026-10-15T11:00:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Servicio no existe o distinta sede |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede de la OT |
| 404 | `NOT_FOUND` | OT no existe |
| 409 | `CONFLICT` | OT no está `OPEN` (ej: `COMPLETED`) |

---

## `PUT /api/v1/work-orders/{id}/tasks/{taskId}` — Actualizar Detalles de Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Actualiza servicio, mecánico asignado, descripción, fotos.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `serviceId` | UUID | No | Nuevo servicio (misma sede) |
| `assignedMechanicId` | UUID | No | EmployeeRegistration ID (debe estar ACTIVE en la sede) |
| `description` | string | No | Nueva descripción |
| `evidenceImages` | string[] | No | Nuevas fotos (reemplaza) |

### Success Response (200 OK)

`WorkOrderTaskResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Servicio/empleado inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | OT o Task no existe |

---

## `DELETE /api/v1/work-orders/{id}/tasks/{taskId}` — Eliminar Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Elimina tarea y **libera reservas de stock** de sus productos.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | OT o Task no existe |
| 409 | `CONFLICT` | Task tiene estado no eliminable (verificar) |

### Important Considerations

- **Liberación stock**: Inmediata — `WorkOrderTaskProduct` reservas → `availableQuantity++` en lotes
- **Montos**: `totalAmount` de OT se recalcula (resta monto de task eliminada)

---

## `POST /api/v1/work-order-tasks/{taskId}/assign-mechanic` — Asignar Mecánico

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Asigna empleado responsable de la tarea.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `mechanicId` | UUID | Sí | EmployeeRegistration ID (ACTIVE en la sede) |

### Success Response (200 OK)

`WorkOrderTaskResource` con `assignedMechanicId` y `mechanicName`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Empleado no ACTIVE en la sede |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task o Empleado no existe |

---

## `POST /api/v1/work-order-tasks/{taskId}/start` — Iniciar Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Cambia estado `PENDING` → `DOING`, setea `startedAt = now()`.

### Success Response (200 OK)

`WorkOrderTaskResource` con `status: "DOING"`, `startedAt`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Task no está `PENDING` |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task no existe |
| 409 | `CONFLICT` | Ya está `DOING` o `COMPLETED` |

---

## `POST /api/v1/work-order-tasks/{taskId}/complete` — Finalizar Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Cambia estado `DOING` → `COMPLETED`, setea `completedAt = now()`. **Requerido para completar OT**.

### Success Response (200 OK)

`WorkOrderTaskResource` con `status: "COMPLETED"`, `completedAt`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Task no está `DOING` |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task no existe |
| 409 | `CONFLICT` | Ya `COMPLETED` |

---

## `POST /api/v1/work-order-tasks/{taskId}/reopen` — Reabrir Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Revierte `COMPLETED` → `DOING` (para ajustes). Limpia `completedAt`.

### Success Response (200 OK)

`WorkOrderTaskResource` con `status: "DOING"`, `completedAt: null`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Task no está `COMPLETED` |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task no existe |

---

## `POST /api/v1/work-order-tasks/{taskId}/products` — Agregar Repuesto a Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Asocia producto del inventario a la tarea. **Verifica stock y genera reserva inmediata**.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `productId` | UUID | Sí | Producto (misma sede que OT) |
| `quantity` | integer | Sí | Cantidad (> 0) |

**Request Example**:
```json
{
  "productId": "prod1prod2-prod3-prod4-prod5prod6prod7",
  "quantity": 2
}
```

### Success Response (201 CREATED)

```json
{
  "id": "wp1wp2wp3-wp4-wp5-wp6-wp7wp8wp9wp0",
  "productId": "prod1prod2-prod3-prod4-prod5prod6prod7",
  "productName": "Pastillas Freno Delanteras",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "quantity": 2,
  "unitPrice": 180.00,
  "totalAmount": 360.00,
  "createdAt": "2026-10-15T11:15:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Cantidad <= 0, producto distinta sede |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task o Producto no existe |
| 409 | `CONFLICT` | **Stock insuficiente** (`INSUFFICIENT_STOCK`), producto ya en task |

### Important Considerations

- **Reserva inmediata**: `ProductBatch.availableQuantity -= quantity` (FIFO por lotes)
- **Precio**: Se toma `Product.salePrice` al momento de agregar (no cambia si precio cambia después)
- **Montos**: `task.totalAmount += unitPrice * quantity`; `ot.totalAmount` se recalcula
- **Validación stock**: Suma `availableQuantity` across lotes ≥ quantity

---

## `PUT /api/v1/work-order-tasks/{taskId}/products/{productId}` — Modificar Cantidad de Repuesto

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Actualiza cantidad reservada. Ajusta reserva de stock (incrementa/decrementa).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quantity` | integer | Sí | Nueva cantidad (> 0) |

### Success Response (200 OK)

`WorkOrderTaskProductResource` actualizado con nuevos montos.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Cantidad <= 0 |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task/Product no existe en task |
| 409 | `CONFLICT` | Stock insuficiente para incremento |

---

## `DELETE /api/v1/work-order-tasks/{taskId}/products/{productId}` — Quitar Repuesto de Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Elimina producto de la tarea. **Libera reserva de stock** (devuelve a lotes FIFO).

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task/Product no existe en task |

### Important Considerations

- **Liberación FIFO**: `availableQuantity += quantity` en lotes (inverso a reserva)
- **Montos**: `task.totalAmount -= unitPrice * quantity`; `ot.totalAmount` recalculado
- **Completar OT**: Requiere 0 productos en tasks `PENDING`/`DOING` (o todos `COMPLETED`)