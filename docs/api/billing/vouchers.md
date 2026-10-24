# Billing — Vouchers (Comprobantes SUNAT)

**Base Path**: `/api/v1/vouchers`  
**Context**: Billing Domain  
**Description**: Emisión de comprobantes electrónicos (Boleta/Factura) vía Factos → SUNAT. Flujo: `PENDING` → `PAID` / `EMISSION_FAILED` / `CANCELED`.

---

## `POST /api/v1/vouchers` — Emitir Comprobante (Boleta / Factura)

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Emite comprobante legal conectándose en tiempo real con Factos → SUNAT. Requiere cotización `APPROVED`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quoteId` | UUID | Sí | Cotización `APPROVED` (misma sede) |
| `type` | string (enum) | Sí | `RECEIPT` (Boleta) o `INVOICE` (Factura) |
| `customerDocumentType` | string (enum) | Sí | `DNI` o `RUC` |
| `customerDocumentNumber` | string | Sí | 8 dígitos DNI / 11 dígitos RUC |
| `customerName` | string | Sí | Nombre/Razón social cliente |

**Request Example** (Boleta):
```json
{
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "48765432",
  "customerName": "María Fernández"
}
```

**Request Example** (Factura):
```json
{
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "INVOICE",
  "customerDocumentType": "RUC",
  "customerDocumentNumber": "20549876123",
  "customerName": "Empresa Ejemplo S.A.C."
}
```

### Success Response (201 CREATED)

```json
{
  "id": "v1v2v3v4-9999-8888-7777-666655554444",
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "48765432",
  "customerName": "María Fernández",
  "totalAmount": 225.00,
  "status": "PENDING",
  "externalInvoiceId": "fct-05b7e3a9-1111-2222-3333-444455556666",
  "pdfUrl": "https://factos-gi5r.onrender.com/v1/invoices/fct-05b7e3a9/pdf",
  "payments": [],
  "totalPaid": 0.00
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | RUC no 11 dígitos, DNI no 8 dígitos, tipo/doc inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede de la cotización |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Cotización no `APPROVED`, ya facturada, o RUC inválido para Factura |
| 500 | `UNEXPECTED_ERROR` | Error Factos/SUNAT (logged, no internal details) |

### Important Considerations

- **Validación RUC**: Factura **requiere** `customerDocumentType=RUC` + 11 dígitos
- **Validación DNI**: Boleta acepta `DNI` (8 dígitos) o `RUC`
- **Correlativo**: Factos asigna serie/correlativo por sede/tipo (config en BD)
- **Estado inicial**: `PENDING` → Factos procesa async → `EMITTED` → `PAID` al recibir pago
- **PDF**: `pdfUrl` disponible inmediatamente (Factos genera PDF previo)

---

## `GET /api/v1/vouchers/{voucherId}` — Detalle de Comprobante

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Retorna comprobante con PDF, pagos, saldos.

### Success Response (200 OK)

```json
{
  "id": "v1v2v3v4-9999-8888-7777-666655554444",
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "48765432",
  "customerName": "María Fernández",
  "totalAmount": 225.00,
  "status": "PAID",
  "externalInvoiceId": "fct-05b7e3a9-1111-2222-3333-444455556666",
  "series": "B001",
  "correlative": "00001234",
  "fiscalNumber": "B001-00001234",
  "pdfUrl": "https://factos-gi5r.onrender.com/v1/invoices/fct-05b7e3a9/pdf",
  "payments": [
    {
      "id": "pay1pay2-pay3-pay4-pay5pay6pay7pay8",
      "amount": 225.00,
      "method": "CASH",
      "paidAt": "2026-10-15T15:00:00Z"
    }
  ],
  "totalPaid": 225.00,
  "balance": 0.00
}
```

---

## `GET /api/v1/vouchers?branchId={branchId}` — Listar Comprobantes de Sede

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista comprobantes emitidos en una sede.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |

### Success Response (200 OK)

Array de `VoucherResource` (resumen).

---

## `POST /api/v1/vouchers/{voucherId}/payments` — Registrar Pago en Comprobante

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Registra pago parcial o total. Si `totalPaid >= totalAmount` → `status = PAID`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `amount` | number (decimal) | Sí | Monto > 0 |
| `method` | string (enum) | Sí | `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER` |

**Request Example**:
```json
{
  "amount": 225.00,
  "method": "CASH"
}
```

### Success Response (201 CREATED)

`VoucherResource` actualizado con `status: "PAID"` si cubre total.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Monto <= 0, método inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Comprobante no existe |
| 409 | `CONFLICT` | Comprobante `CANCELED`, ya `PAID`, o monto excede deuda |

---

## `DELETE /api/v1/vouchers/{voucherId}/payments/{paymentId}` — Eliminar Pago

**Security**: Validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Elimina pago y recalcula estado (`PAID` → `PARTIALLY_PAID` → `PENDING`).

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Pago/Comprobante no existe |
| 409 | `CONFLICT` | Comprobante `CANCELED` |

### Important Considerations

- **Estados voucher**: `PENDING` → `PARTIALLY_PAID` → `PAID` / `CANCELED` / `EMISSION_FAILED`
- **Correlativo fiscal**: Incluye `series`, `correlative`, `fiscalNumber` en response
- **Factos**: Integración real — errores de SUNAT propagan como `EMISSION_FAILED` (reintentable)
- **Moneda**: Soles (PEN) — montos en decimal(10,2)