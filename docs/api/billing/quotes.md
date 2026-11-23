# Billing — Quotes

**Base Path**: `/api/v1/quotes`  
**Context**: Billing Domain  
**Description**: Cotizaciones generadas desde Órdenes de Trabajo. Flujo: `DRAFT` → `APPROVED` / `CANCELED`.

---

## Flujos de Operación en el Taller (Web & Mobile)

Ambos flujos parten de la recepción del vehículo (`POST /api/v1/appointments`) y la apertura de la Orden de Trabajo (`POST /api/v1/work-orders` con diagnóstico inicial y tareas):

1. **Servicio Estándar / Directo (cobro al finalizar):**
   - El mecánico ejecuta las tareas directamente (`start` → repuestos → `evidenceImages` → `complete`), mientras el cliente sigue el avance en solo lectura (`GET /api/v1/work-orders/me`).
   - El taller finaliza la OT (`POST /api/v1/work-orders/{id}/complete` → `COMPLETED`) y recién genera la cotización (`POST /api/v1/quotes`).
   - El cliente aprueba la cotización (`POST /api/v1/quotes/{id}/approvals`) y paga en línea (`POST /api/v1/payments/sessions`) o presencial en caja (`POST /api/v1/checkouts`).

2. **Servicio con Presupuesto Previo (autorización antes de ejecutar):**
   - Tras la revisión inicial, el taller carga las tareas/repuestos estimados en la OT y genera la cotización (`POST /api/v1/quotes` en estado `DRAFT`).
   - Si surgen ajustes antes de aprobar, el taller sincroniza el monto con `PUT /api/v1/quotes/{id}/sync` (o cancela y vuelve a cotizar).
   - El cliente revisa el presupuesto (`GET /api/v1/quotes/me`) y lo aprueba (`POST /api/v1/quotes/{id}/approvals` → `APPROVED`) como autorización para iniciar el servicio *(el pago en línea permanece bloqueado hasta que la OT pase a `COMPLETED`)*.
   - El mecánico ejecuta las tareas, el taller finaliza la OT (`COMPLETED`) y recién entonces se habilita el pago para el cliente.

---

## `POST /api/v1/quotes` — Generar Cotización

**Security**: Validación manual `isAuthorizedForBranch(branchId)`  
**Description**: Crea cotización desde OT completada. Calcula subtotal (servicios + repuestos) y aplica descuento.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `workOrderId` | UUID | Sí | OT completada (misma sede) |
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `discountPercentage` | number (decimal) | No | Descuento % (0-100, default: 0) |

**Request Example**:
```json
{
  "workOrderId": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "discountPercentage": 10.0
}
```

### Success Response (201 CREATED)

```json
{
  "id": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "workOrderId": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "subtotalAmount": 250.00,
  "discountPercentage": 10.0,
  "totalAmount": 225.00,
  "status": "DRAFT"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | OT no completada, descuento inválido (>100) |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | OT/Sede no existe |
| 409 | `CONFLICT` | Ya existe cotización para esa OT |

### Important Considerations

- **Origen**: Solo OTs con `status = COMPLETED`
- **Cálculo**: `subtotalAmount` = suma tasks (servicios + productos) de la OT
- **Descuento**: Se aplica sobre subtotal → `totalAmount = subtotal * (1 - discount/100)`
- **Estado**: Nace `DRAFT` → editable hasta `APPROVED`
- **Re-cotización**: Solo se permite 1 cotización activa (no `CANCELED`) por OT; si una cotización en `DRAFT` se cancela (`POST /api/v1/quotes/{id}/cancellations`, permitido tanto al staff como al cliente dueño de la OT), el taller puede volver a cotizar la OT.

---

## `PUT /api/v1/quotes/{id}/sync` — Recalcular Cotización desde la Orden de Trabajo

**Security**: `Permission.QUOTE_CREATE` en la sede  
**Description**: Sincroniza `subtotalAmount` y `totalAmount` de una cotización en estado `DRAFT` con el `totalAmount` actual de su Orden de Trabajo (útil cuando se agregan tareas o repuestos después de emitir el presupuesto inicial).

### Success Response (200 OK)

Retorna `QuoteResource` recalculado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin permiso en la sede |
| 404 | `NOT_FOUND` | Cotización u OT no existe |
| 409 | `CONFLICT` | Cotización no está en estado `DRAFT` (`INVALID_QUOTE_STATE`) |

---

## `GET /api/v1/quotes/{id}` — Detalle de Cotización

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Retorna cotización completa con desglose.

### Success Response (200 OK)

```json
{
  "id": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "workOrderId": "wo1wo2wo3-wo4-wo5-wo6wo7wo8wo9",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "subtotalAmount": 250.00,
  "discountPercentage": 10.0,
  "totalAmount": 225.00,
  "status": "DRAFT",
  "items": [
    {
      "type": "SERVICE",
      "name": "Cambio pastillas freno delanteras",
      "quantity": 1,
      "unitPrice": 80.00,
      "total": 80.00
    },
    {
      "type": "PRODUCT",
      "name": "Pastillas Freno Delanteras",
      "quantity": 1,
      "unitPrice": 180.00,
      "total": 180.00
    }
  ]
}
```

---

## `GET /api/v1/quotes/me` — Listar Mis Cotizaciones (Cliente — Solo Lectura)

**Security**: `@PreAuthorize("isAuthenticated()")`  
**Description**: Lista todas las cotizaciones asociadas a las órdenes de trabajo del cliente autenticado (incluyendo cotizaciones `APPROVED` listas para pago desde la app móvil).

### Success Response (200 OK)

Array de `QuoteResource`.

---

## `GET /api/v1/quotes?branchId={branchId}` — Listar Cotizaciones de Sede

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista cotizaciones de una sede.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |

### Success Response (200 OK)

Array de `QuoteResource` (resumen sin items).

---

## `PUT /api/v1/quotes/{id}` — Actualizar Descuento (Solo DRAFT)

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Actualiza `discountPercentage` y recalcula `totalAmount`. **Solo si `status = DRAFT`**.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `discountPercentage` | number (decimal) | Sí | Nuevo descuento % (0-100) |

**Request Example**:
```json
{
  "discountPercentage": 15.0
}
```

### Success Response (200 OK)

`QuoteResource` con `totalAmount` recalculado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Descuento > 100 o < 0 |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Cotización no es `DRAFT` (`INVALID_QUOTE_STATE`) |

---

## `POST /api/v1/quotes/{id}/approvals` — Aprobar Cotización

**Security**: Staff autorizado de la sede (`Permission.QUOTE_APPROVE`) o el **cliente dueño de la orden de trabajo**  
**Description**: `DRAFT` → `APPROVED`. Habilita el pago en línea (`POST /api/v1/payments/sessions` o `POST /api/v1/payments/mercadopago/preferences`) y el checkout presencial (`POST /api/v1/checkouts`).

### Success Response (200 OK)

```json
{
  "id": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "status": "APPROVED",
  "approvedAt": "2026-10-15T14:30:00Z"
  // ...resto de campos
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Ya `APPROVED` o `CANCELED` |

---

## `POST /api/v1/quotes/{id}/cancellations` — Cancelar Cotización

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: `DRAFT`/`APPROVED` → `CANCELED`. No se puede facturar una cotización cancelada.

### Success Response (200 OK)

`QuoteResource` con `status: "CANCELED"`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Ya `CANCELED` |

### Important Considerations

- **Estados válidos**: `DRAFT` → `APPROVED` → (facturar) | `DRAFT`/`APPROVED` → `CANCELED`
- **Irreversible**: `CANCELED` no vuelve a `DRAFT`
- **Facturación**: Solo `APPROVED` permite `POST /vouchers`