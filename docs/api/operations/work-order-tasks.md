# Operations — Work Order Tasks & Products

**Base Paths**: `/api/v1/work-orders/{id}/tasks`, `/api/v1/work-order-tasks/{taskId}/*`  
**Context**: Operations Domain  
**Description**: Gestión de tareas mecánicas, asignación de personal, repuestos (con reserva de stock).

---

## `POST /api/v1/work-orders/{id}/tasks` — Agregar Tarea a la OT

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Agrega tarea a OT existente. Estado inicial de la tarea: `PENDING`. Si la orden estaba `PENDING`, pasa a `IN_PROGRESS`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `serviceId` | UUID | Sí | Servicio del catálogo (misma sede que OT) |
| `assignedMechanicId` | UUID | Sí | ID del mecánico. Puede ser el `id` de `employee_registrations` o el `id` de `employees` (perfil Core) — el backend lo resuelve automáticamente a la registración activa de la sede. |
| `description` | string | Sí | Descripción técnica del trabajo (10 - 1000 caracteres) |
| `evidenceImages` | string[] (URLs) | No | Fotos evidencia (Cloudinary) |

**Request Example**:
```json
{
  "serviceId": "f287fa2e-43d7-4de6-901f-5ffaa85ca527",
  "assignedMechanicId": "651ba739-427e-4fda-8ae0-c1bf6c4f8610",
  "description": "Rectificado de discos y alineación dinamica",
  "evidenceImages": [
    "https://res.cloudinary.com/shiftiq/image/upload/v1/tasks/laser.jpg"
  ]
}
```

### Success Response (201 CREATED)

Retorna la orden de trabajo actualizada (`WorkOrderResource`) con el arreglo de tareas actualizado y el nuevo `totalAmount`.

```json
{
  "id": "8279b895-82a9-454a-b67b-5e9ef5855223",
  "appointmentId": "14d73f73-77d4-4cae-8047-816717c6ac4c",
  "branchId": "bcf5acc6-5745-48bd-a133-611181b5ced0",
  "vehicleId": "091fd95d-3012-4ed5-adab-40e88bf3c26b",
  "customerId": "dd0d563d-51c0-4302-84ad-09c71bdf9303",
  "internalNumber": 1,
  "formattedNumber": "MOL-01-000001",
  "status": "IN_PROGRESS",
  "diagnosticSummary": "Diagnóstico inicial",
  "mileageIn": 45300,
  "totalAmount": 150.00,
  "tasks": [
    {
      "id": "7b6ab543-674a-4598-83da-3b1c70dd113b",
      "serviceId": "f287fa2e-43d7-4de6-901f-5ffaa85ca527",
      "branchId": "bcf5acc6-5745-48bd-a133-611181b5ced0",
      "assignedMechanicId": "651ba739-427e-4fda-8ae0-c1bf6c4f8610",
      "status": "PENDING",
      "description": "Rectificado de discos y alineación dinamica",
      "price": 150.00,
      "startedAt": null,
      "completedAt": null,
      "products": [],
      "evidenceImages": [
        "https://res.cloudinary.com/shiftiq/image/upload/v1/tasks/laser.jpg"
      ],
      "createdAt": "2026-11-04T19:48:42Z"
    }
  ],
  "entryInspectionImages": [],
  "createdAt": "2026-11-04T18:06:21Z",
  "updatedAt": "2026-11-04T19:48:42Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Servicio no existe, mecánico no especificado o descripción inválida |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede de la OT |
| 404 | `NOT_FOUND` | OT no existe |
| 409 | `CONFLICT` | OT no admite adición de tareas en su estado actual (ej: `COMPLETED`) |

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
**Description**: Asigna o reasigna el empleado/mecánico responsable de la tarea (`assignedMechanicId` apunta al `id` de `employee_registrations`). Acepta indistintamente el ID de registración o el ID de perfil de empleado `employees.id`, resolviéndolo automáticamente.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `mechanicId` | UUID | Sí | Query | ID del mecánico: ID de registración (`employee_registrations`) o ID de perfil (`employees.id`) activo en la sede |

### Success Response (200 OK)

Retorna la orden de trabajo (`WorkOrderResource`) con la tarea asignada al nuevo mecánico.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Empleado no activo en la sede |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Task o Empleado no existe |

---

## `POST /api/v1/work-order-tasks/{taskId}/start` — Iniciar Tarea

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Cambia estado de la tarea `PENDING` → `DOING` y setea `startedAt = now()`.

### Success Response (200 OK)

Retorna `WorkOrderResource` actualizado con la tarea en estado `DOING` y `startedAt` poblado.

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
**Description**: Cambia estado `DOING` → `COMPLETED` y setea `completedAt = now()`. Si todas las tareas de la orden están completadas, la orden pasa automáticamente a `COMPLETED`.

### Success Response (200 OK)

Retorna `WorkOrderResource` actualizado con la tarea en `COMPLETED`.

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
**Description**: Revierte la tarea de `COMPLETED` → `DOING`. Limpia `completedAt` y regresa la orden de trabajo a `IN_PROGRESS` si estaba completada.

### Success Response (200 OK)

Retorna `WorkOrderResource` con la tarea de vuelta en `DOING` y `completedAt: null`.

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
  "productId": "bcc1523d-e945-4fcc-8634-748f67ee170d",
  "quantity": 1
}
```

### Success Response (201 CREATED)

Retorna `WorkOrderResource` actualizado con el nuevo producto registrado dentro del arreglo `products` de la tarea correspondiente y el monto total recalculado.

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
- **Montos**: El `price` de la tarea y el `totalAmount` de la OT se recalculan automáticamente
- **Validación stock**: Suma de `availableQuantity` across lotes ≥ quantity

---

## `PUT /api/v1/work-order-tasks/{taskId}/products/{productId}` — Modificar Cantidad de Repuesto

**Security**: Validación manual `isAuthorizedForBranch(ot.branchId)`  
**Description**: Actualiza cantidad reservada. Ajusta reserva de stock (incrementa o decrementa).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quantity` | integer | Sí | Nueva cantidad (> 0) |

### Success Response (200 OK)

Retorna `WorkOrderResource` con los montos actualizados.

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