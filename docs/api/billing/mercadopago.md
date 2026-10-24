# Billing — Mercado Pago Integration

**Base Path**: `/api/v1/payments/mercadopago`  
**Context**: Billing Domain  
**Description**: Integración con Mercado Pago Checkout Pro — preferencias, webhooks, verificación.

---

## `POST /api/v1/payments/mercadopago/preferences` — Crear Preferencia de Pago

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `isAuthorizedForBranch(quote.branchId)`  
**Description**: Genera preferencia MP desde cotización `APPROVED`. Persiste `PaymentIntent` para webhook autónomo.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quoteId` | UUID | Sí | Cotización `APPROVED` (misma sede) |
| `type` | string (enum) | No | `RECEIPT` (default) o `INVOICE` |
| `customerDocumentType` | string (enum) | No | `DNI` (default) o `RUC` |
| `customerDocumentNumber` | string | No | Documento (default: `00000000`) |
| `customerName` | string | No | Nombre (default: `Cliente General`) |

**Request Example**:
```json
{
  "quoteId": "q1q2q3q4-1234-5678-90ab-cdef12345678",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "48765432",
  "customerName": "María Fernández"
}
```

### Success Response (201 CREATED)

```json
{
  "preferenceId": "123456789-abcdef-1234",
  "initPoint": "https://www.mercadopago.com.pe/checkout/v1/redirect?pref_id=123456789-abcdef-1234",
  "sandboxInitPoint": "https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref_id=123456789-abcdef-1234",
  "amount": 225.00,
  "currency": "PEN",
  "externalReference": "q1q2q3q4-1234-5678-90ab-cdef12345678"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | RUC requerido para Factura, documento inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Cotización no existe |
| 409 | `CONFLICT` | Cotización no `APPROVED`, ya facturada |
| 500 | `UNEXPECTED_ERROR` | Error MP API |

### Important Considerations

- **Validación Factura**: Si `type=INVOICE` → requiere `customerDocumentType=RUC` (11 dígitos), `customerName` obligatorio
- **PaymentIntent**: Se guarda `quoteId`, `type`, datos cliente, `amount` → usado por webhook si cliente no retorna
- **External Reference**: `quoteId` → permite reconciliación en webhook
- **Moneda**: Siempre `PEN` (soles peruanos)

---

## `POST /api/v1/payments/mercadopago/webhooks` — Webhook Oficial MP

**Security**: **Público** — Validación HMAC `x-signature` + Rate Limiter (60 req/min)  
**Description**: Notificaciones asíncronas de MP (pago aprobado, reembolso, chargeback). Emite voucher + concilia automáticamente.

### Headers Requeridos

| Header | Descripción |
|--------|-------------|
| `x-signature` | HMAC SHA256 (`ts,v1`) — validado con `mercadopago.webhook.secret` |
| `x-request-id` | ID único de notificación MP |

### Query Parameters

| Param | Descripción |
|-------|-------------|
| `id` / `data.id` | Payment ID (MP) |
| `type` / `topic` | Tipo notificación (`payment`, `merchant_order`) |

### Validaciones de Seguridad

1. **HMAC**: `x-signature` = `ts=timestamp,v1=hmac_sha256(manifest, secret)` — manifiesto: `id:{id};request-id:{x-request-id};ts:{ts};`
2. **Freshness**: `ts` no > 5 min diferencia con servidor
3. **Rate Limit**: 60 req/min por IP (exceso → 429)

### Procesamiento (Pago Aprobado)

```
1. Valida HMAC + freshness
2. Obtiene paymentId → consulta MP API `/v1/payments/{id}`
3. Si status = "approved" / "succeeded":
   a. externalReference = quoteId
   b. Si voucher ya existe y PAID → 200 OK (idempotente)
   c. Si voucher PENDING/EMISSION_FAILED → reintenta emisión (retry)
   d. Si NO existe voucher:
      - Busca PaymentIntent guardado en step 1 (preferences)
      - Valida monto MP == monto Intent
      - Emite voucher vía ProcessMercadoPagoCheckoutCommand
      - Si éxito → 200 OK
      - Si falla Factos → 503 (MP reintentará)
4. Si status = "refunded" / "charged_back" → Log WARNING (requiere reconciliación manual)
5. Otros status → 200 OK (ignora)
```

### Responses

| HTTP Code | Descripción |
|-----------|-------------|
| 200 OK | Procesado OK (cuerpo vacío) — MP considera atendido |
| 400 Bad Request | Monto MP ≠ Intent amount |
| 401 Unauthorized | HMAC inválido o timestamp > 5 min |
| 503 Service Unavailable | Sin secret, sin PaymentIntent, o emisión falló → MP reintenta |

### Important Considerations

- **Autonomía**: Webhook emite voucher **sin intervención del cliente** (si no volvió a la app)
- **Reintentos**: MP reintenta por ~72h con backoff exponencial ante 503
- **Idempotencia**: `externalReference` (quoteId) evita duplicados
- **Reconciliación**: `refunded`/`charged_back` → alerta manual (no auto-reversa voucher)
- **Config Requerida**: `mercadopago.webhook.secret` en env (obligatorio para producción)

---

## Flujo Completo MP (Frontend + Backend)

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant BE as Backend
    participant MP as Mercado Pago
    participant FA as Factos/SUNAT

    FE->>BE: POST /preferences {quoteId, type, customer...}
    BE->>FE: {preferenceId, initPoint, sandboxInitPoint}
    FE->>MP: Redirect usuario a initPoint
    MP->>Usuario: Checkout Pro (tarjeta, Yape, etc.)
    MP->>FE: Redirect back_url?payment_id=123&status=approved
    FE->>BE: POST /checkouts/mercadopago {quoteId, paymentId, ...}
    BE->>MP: GET /v1/payments/{paymentId} (verifica)
    MP-->>BE: status=approved, amount=225
    BE->>FA: Emite voucher (Factos)
    FA-->>BE: PDF URL, correlativo
    BE->>BE: Registra payment + actualiza voucher PAID
    BE-->>FE: VoucherResource {pdfUrl, status: PAID}
    
    alt Usuario NO retorna (cierra ventana)
        MP->>BE: POST /webhooks (x-signature, payment_id)
        BE->>MP: Verifica pago
        BE->>FA: Emite voucher autónomo
        BE->>BE: Registra payment
    end
```

---

## Configuración Requerida (Environment)

```bash
# Mercado Pago
MERCADOPAGO_ACCESS_TOKEN=APP_USR-xxxxxxxxxxxxxxxx
MERCADOPAGO_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxxxxx  # Obligatorio para webhook

# Factos (SUNAT)
FACTOS_API_URL=https://factos-gi5r.onrender.com
FACTOS_API_KEY=xxxxxxxxxxxxxxxx
```