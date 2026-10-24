# Billing — Quotes

**Base Path**: `/api/v1/quotes`  
**Context**: Billing Domain  
**Description**: Cotizaciones generadas desde Órdenes de Trabajo. Flujo: `DRAFT` → `APPROVED` → `CANCELED`.

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

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: `DRAFT` → `APPROVED`. Habilita emisión de comprobante (`POST /vouchers`) y pago.

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