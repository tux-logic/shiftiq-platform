# Billing — Checkouts (Cobro Unificado)

**Base Path**: `/api/v1/checkouts`  
**Context**: Billing Domain  
**Description**: Flujo "todo en uno" — emite comprobante + registra pago completo en una transacción.

---

## `POST /api/v1/checkouts` — Checkout Directo Presencial

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: **Botón "Cobrar en Caja"** — emite voucher (Factos/SUNAT) + registra pago 100% en una transacción atómica.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quoteId` | UUID | Sí | Cotización `APPROVED` (misma sede) |
| `type` | string (enum) | Sí | `RECEIPT` (Boleta) o `INVOICE` (Factura) |
| `customerDocumentType` | string (enum) | Sí | `DNI` o `RUC` |
| `customerDocumentNumber` | string | Sí | 8 dígitos DNI / 11 dígitos RUC |
| `customerName` | string | Sí | Nombre cliente |
| `method` | string (enum) | Sí | `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER` |

**Request Example**:
```json
{
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "48765432",
  "customerName": "María Fernández",
  "method": "CASH"
}
```

### Success Response (201 CREATED)

`VoucherResource` con comprobante emitido + pago registrado → `status: "PAID"`, `totalPaid = totalAmount`.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | RUC/DNI inválido, método no soportado |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Cotización no `APPROVED`, ya facturada |
| 500 | `UNEXPECTED_ERROR` | Error Factos/SUNAT (transacción rollback) |

### Important Considerations

- **Atómico**: Voucher + Payment en misma transacción — si falla Factos, no se registra pago
- **Uso**: Caja física, POS local, pago inmediato
- **Rollback**: Si Factos falla → ningún pago registrado, voucher no emitido

---

## `POST /api/v1/checkouts/mercadopago` — Confirmar Pago Mercado Pago

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: **Callback post-pago MP** — frontend llama tras retorno exitoso de Checkout Pro. Verifica pago en MP API, emite voucher, registra pago.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quoteId` | UUID | Sí | Cotización `APPROVED` |
| `type` | string (enum) | Sí | `RECEIPT` o `INVOICE` |
| `customerDocumentType` | string (enum) | Sí | `DNI` o `RUC` |
| `customerDocumentNumber` | string | Sí | Documento cliente |
| `customerName` | string | Sí | Nombre cliente |
| `paymentId` | string | Sí | `payment_id` devuelto por MP en URL retorno |

**Request Example**:
```json
{
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "48765432",
  "customerName": "María Fernández",
  "paymentId": "98765432101"
}
```

### Success Response (200 OK / 201 CREATED)

`VoucherResource` emitido con `status: "PAID"`, `pdfUrl` Factos.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Datos inválidos, paymentId no existe en MP |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Pago MP no `approved`, monto no coincide, ya facturado |
| 500 | `UNEXPECTED_ERROR` | Error verificación MP / Factos |

### Important Considerations

- **Verificación real**: Llama `GET /v1/payments/{paymentId}` a MP API con access token
- **Monto**: Debe coincidir con `quote.totalAmount` (configurable tolerancia)
- **Idempotencia**: Si ya existe voucher para la quote → retorna existente (no duplica)
- **Flujo frontend**:
  1. `POST /payments/mercadopago/preferences` → `initPoint`
  2. Redirect a `initPoint` (web) / WebView (app)
  3. Usuario paga → MP redirect a `back_url` con `payment_id`
  4. Frontend llama `POST /checkouts/mercadopago` con `paymentId`
  5. Backend verifica + emite + responde voucher con PDF

---

## Flujo Comparativo

| Flujo | Endpoint | Uso | Transacción |
|-------|----------|-----|-------------|
| **Presencial** | `POST /checkouts` | Efectivo, tarjeta física, transferencia | Voucher + Payment atómico |
| **Mercado Pago** | `POST /checkouts/mercadopago` | Pago digital (web/app) | Verifica MP → Voucher + Payment |

**Ambos**: Requieren `quoteId` `APPROVED`, emiten comprobante SUNAT, registran pago 100%.