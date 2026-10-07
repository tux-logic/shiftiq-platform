# Billing Bounded Context - Endpoints Documentation

Este documento detalla el uso de los endpoints REST expuestos por el Bounded Context de **Billing** (Facturación) en la plataforma Atelier. El contexto se divide en cuatro áreas principales: **Quotes** (Cotizaciones), **Vouchers** (Comprobantes de Pago: Boletas/Facturas), **Checkouts** (Flujos de Facturación e Integración Mercado Pago) y **Mercado Pago Payments** (Preferencias y Webhooks).

---

## 1. Quotes (Cotizaciones)
**Base URL:** `/api/v1/quotes`

Las cotizaciones representan el presupuesto inicial generado a partir de una orden de trabajo (Work Order). Pasan por un ciclo de vida: `DRAFT` -> `APPROVED` / `CANCELED`.

### 1.1. Crear una Cotización (Create Quote)
Genera una nueva cotización en estado `DRAFT` asociada a una orden de trabajo.

- **Método:** `POST`
- **Ruta:** `/api/v1/quotes`
- **Request Body:**
```json
{
  "workOrderId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "branchId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "discountPercentage": 10.5
}
```
- **Respuestas:**
  - `201 Created`: Cotización creada exitosamente.
  - `400 Bad Request`: Errores de validación (ej. descuento inválido).
  - `409 Conflict`: Ya existe una cotización para esta orden de trabajo.

### 1.2. Actualizar Descuento (Update Quote Discount)
Permite modificar el descuento de una cotización, siempre y cuando esté en estado `DRAFT`.

- **Método:** `PUT`
- **Ruta:** `/api/v1/quotes/{id}`
- **Path Variable:** `id` (UUID de la cotización)
- **Request Body:**
```json
{
  "discountPercentage": 15.0
}
```
- **Respuestas:**
  - `200 OK`: Cotización actualizada.
  - `409 Conflict`: La cotización no está en estado `DRAFT`.

### 1.3. Aprobar Cotización (Approve Quote)
Transiciona el estado de la cotización de `DRAFT` a `APPROVED`. Solo las cotizaciones aprobadas pueden ser facturadas (convertidas a Vouchers).

- **Método:** `POST`
- **Ruta:** `/api/v1/quotes/{id}/approvals`
- **Respuestas:**
  - `200 OK`: Cotización aprobada.

### 1.4. Cancelar Cotización (Cancel Quote)
Transiciona el estado de la cotización a `CANCELED`.

- **Método:** `POST`
- **Ruta:** `/api/v1/quotes/{id}/cancellations`
- **Respuestas:**
  - `200 OK`: Cotización cancelada.

### 1.5. Obtener Cotizaciones
- **Por ID:** `GET /api/v1/quotes/{id}`
- **Por Sucursal:** `GET /api/v1/quotes?branchId={branchId}`

---

## 2. Vouchers (Comprobantes)
**Base URL:** `/api/v1/vouchers`

Un Voucher representa un comprobante de pago electrónico (Boleta o Factura). Se generan a partir de cotizaciones `APPROVED`.

### 2.1. Generar Comprobante (Generate Voucher)
Crea un comprobante a partir de una cotización aprobada y lo envía a SUNAT vía Factos. Inicialmente se crea en estado `PENDING`.

- **Método:** `POST`
- **Ruta:** `/api/v1/vouchers`
- **Request Body:**
```json
{
  "quoteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "INVOICE", 
  "customerDocumentType": "RUC",
  "customerDocumentNumber": "20123456789",
  "customerName": "Empresa Cliente S.A.C."
}
```
*Nota: `type` puede ser `INVOICE` (Factura) o `RECEIPT` (Boleta).*

- **Respuestas:**
  - `201 Created`: Comprobante emitido correctamente.
  - `404 Not Found`: La cotización no existe.
  - `409 Conflict`: La cotización no está en estado `APPROVED` (`QUOTE_NOT_APPROVED`), ya fue facturada (`QUOTE_ALREADY_INVOICED`) o una emisión previa sigue en curso (`VOUCHER_EMISSION_IN_PROGRESS`).
  - `500 Internal Server Error`: Falla en la integración con Factos. El comprobante queda en `EMISSION_FAILED` conservando su correlativo fiscal, de modo que un reintento reutiliza ese número en lugar de consumir uno nuevo.

### 2.2. Agregar un Pago Parcial/Total (Add Payment)
Agrega un pago a un comprobante. Si la suma de los pagos alcanza el monto total, el comprobante pasa a `PAID`.

- **Método:** `POST`
- **Ruta:** `/api/v1/vouchers/{voucherId}/payments`
- **Request Body:**
```json
{
  "amount": 50.00,
  "method": "CASH"
}
```

### 2.3. Eliminar un Pago (Remove Payment)
- **Método:** `DELETE`
- **Ruta:** `/api/v1/vouchers/{voucherId}/payments/{paymentId}`

### 2.4. Obtener Comprobantes
- **Por ID:** `GET /api/v1/vouchers/{voucherId}`
- **Por Sucursal:** `GET /api/v1/vouchers?branchId={branchId}`

---

## 3. Checkouts & Mercado Pago Integrations
**Base URLs:** `/api/v1/checkouts`, `/api/v1/payments/mercadopago`

### 3.1. Flujo Completo de Checkout Directo (Process Checkout)
Genera el comprobante y registra el pago total en una sola transacción (efectivo/tarjeta manual).

- **Método:** `POST`
- **Ruta:** `/api/v1/checkouts`
- **Request Body:**
```json
{
  "quoteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "70123456",
  "customerName": "Juan Perez",
  "method": "CASH"
}
```

- **Respuestas:**
  - `201 Created`: Comprobante emitido y pago total registrado.
  - `404 Not Found`: La cotización no existe.
  - `409 Conflict`: La cotización no está en estado `APPROVED`, ya fue facturada (`QUOTE_ALREADY_INVOICED`) o una emisión previa sigue en curso (`VOUCHER_EMISSION_IN_PROGRESS`).
  - `500 Internal Server Error`: Falla en la integración con Factos; el comprobante queda en `EMISSION_FAILED` y un reintento reutiliza su correlativo fiscal.

### 3.2. Crear Preferencia de Mercado Pago (Create Mercado Pago Preference)
Genera la preferencia de cobro derivada directamente desde la Cotización aprobada. Opcionalmente captura los datos fiscales del comprobante (`type`, `customerDocumentType`, `customerDocumentNumber`, `customerName`) para persistir una intención de cobro (`PaymentIntent`) que permite emitir el CPE en SUNAT de manera autónoma si el cliente cierra la pestaña del navegador tras pagar.

- **Método:** `POST`
- **Ruta:** `/api/v1/payments/mercadopago/preferences`
- **Request Body:**
```json
{
  "quoteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "INVOICE",
  "customerDocumentType": "RUC",
  "customerDocumentNumber": "20601234567",
  "customerName": "Transportes Lima S.A.C."
}
```
> **Reglas de Validación Fiscal:**
> - Si `type` es `INVOICE` (Factura), `customerDocumentType` debe ser `RUC`, `customerDocumentNumber` debe constar exactamente de 11 dígitos numéricos, y `customerName` es obligatorio.
> - Si `type` no se especifica, por defecto es `RECEIPT` (Boleta).

- **Respuesta `201 Created`:**
```json
{
  "preferenceId": "123456789-abc-def",
  "initPoint": "https://www.mercadopago.com.pe/checkout/v1/redirect?pref_id=...",
  "sandboxInitPoint": "https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref_id=...",
  "amount": 150.00,
  "currency": "PEN",
  "externalReference": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

### 3.3. Confirmar Checkout con Mercado Pago (Process Mercado Pago Checkout)
Verifica la validez, monto, moneda y pertenencia del pago en Mercado Pago, emite el comprobante fiscal en SUNAT y registra el pago con prevención de ataques de replay.

- **Idempotencia y Recuperación:** Si el webhook de Mercado Pago ya emitió el comprobante previamente (por ejemplo, si el cliente demoró en regresar al frontend), este endpoint no genera error de conflicto ni duplica comprobantes en SUNAT: recupera y retorna exitosamente el `Voucher` ya emitido (`PAID`).
- **Recuperación ante fallos:** Si un intento previo quedó en `EMISSION_FAILED`, este endpoint reintenta la emisión conservando el mismo correlativo fiscal para evitar saltos en la numeración de SUNAT.

- **Método:** `POST`
- **Ruta:** `/api/v1/checkouts/mercadopago`
- **Request Body:**
```json
{
  "quoteId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "type": "RECEIPT",
  "customerDocumentType": "DNI",
  "customerDocumentNumber": "70123456",
  "customerName": "Juan Perez",
  "paymentId": "9988776655"
}
```

- **Respuestas:**
  - `201 Created`: Comprobante emitido y pago registrado.
  - `400 Bad Request`: El pago en Mercado Pago no es válido (estado, monto, moneda o referencia externa).
  - `404 Not Found`: La cotización o el pago no fueron encontrados.
  - `409 Conflict`: El comprobante ya existe (`QUOTE_ALREADY_INVOICED`), el pago ya fue consumido (`PAYMENT_ALREADY_CONSUMED`) o una emisión previa sigue en curso (`VOUCHER_EMISSION_IN_PROGRESS`).
  - `500 Internal Server Error`: Falla en la integración con Factos.

### 3.4. Webhooks / Notificaciones IPN de Mercado Pago
Endpoint expuesto para la recepción asíncrona de cambios de estado de pagos desde los servidores de Mercado Pago.

- **Seguridad HMAC Fail-Closed:** Valida la cabecera `x-signature` calculando el hash SHA-256 (`v1`) con el secreto configurado en `mercadopago.webhook.secret`. Si el secreto no está configurado, responde `503 Service Unavailable`.
- **Protección contra Ataques de Repetición (Replay Prevention):** Valida la frescura temporal de la firma (`ts`); las solicitudes con un desfase superior a 5 minutos (300 segundos) son rechazadas con `401 Unauthorized`.
- **Emisión Autónoma Asíncrona:** Si el pago es `approved` y el comprobante aún no fue emitido (el usuario cerró la pestaña), recupera la intención de facturación (`PaymentIntent`) y emite el comprobante en SUNAT de manera autónoma.
- **Reintentos Automáticos de Mercado Pago:** Si la emisión externa en Factos falla, el webhook responde con código `503 Service Unavailable`, indicando a Mercado Pago que reintente la entrega de la notificación según su política de backoff exponencial.
- **Respuestas:**
  - `200 OK`: Notificación procesada. Incluye pagos no aprobados, comprobantes ya `PAID`, comprobantes en `PENDING` recientes (aún en emisión) y estados sin acción pendiente.
  - `400 Bad Request`: El monto del pago no coincide con el registrado en la `PaymentIntent` asociada.
  - `401 Unauthorized`: Firma HMAC inválida o desfase temporal de la firma (`ts`) superior a 5 minutos.
  - `503 Service Unavailable`: HMAC sin secreto configurado; el comprobante está en `EMISSION_FAILED` o en `PENDING` obsoleto **sin una `PaymentIntent`** desde la que reconstruir la emisión; o bien la reconstrucción/la emisión falló. En todos los casos Mercado Pago reintentará la notificación.

- **Método:** `POST`
- **Ruta:** `/api/v1/payments/mercadopago/webhooks`
- **Query Params (opcionales):** `?type=payment&data.id=9988776655` o `?topic=payment&id=9988776655`
- **Headers:** `x-signature`, `x-request-id`

---

## 4. Consideraciones de Internacionalización (i18n)

Todos los endpoints que retornen errores de negocio o validación soportan **Internacionalización**. 

Para recibir los mensajes en el idioma deseado, debe enviar la cabecera HTTP:
`Accept-Language: es` (Para Español)
`Accept-Language: en` (Para Inglés)
