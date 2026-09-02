# Inventory Bounded Context - Endpoints Documentation

Este documento detalla los endpoints REST expuestos por el Bounded Context de **Inventory** (Inventario) en la plataforma ShiftIQ.

---

## Products (Productos)
**Base URL:** `/api/v1/inventory/products`

Los productos representan repuestos y materiales del taller. Cada producto pertenece a una sucursal (`branchId`) y puede tener múltiples lotes (`batches`) que documentan entradas y ajustes de stock.

### 1.1. Crear un Producto (Create Product)
- **Método:** `POST`
- **Ruta:** `/api/v1/inventory/products`
- **Respuestas:** `201 Created`, `400 Bad Request`, `409 Conflict` (SKU duplicado)

### 1.2. Listar Productos por Sucursal (TS009 / TS013)
- **Método:** `GET`
- **Rutas equivalentes:**
  - `/api/v1/inventory/products?branchId={branchId}`
  - `/api/v1/inventory/products/branch/{branchId}`
- **Query params opcionales:**
  - `name`: búsqueda parcial por nombre (`?name=Filtro`)
  - `category`: filtro exacto por categoría (`?category=PART`)
  - `lowStockOnly`: `true` para productos en alerta de stock mínimo
- **Respuestas:** `200 OK` (lista, puede estar vacía)

### 1.3. Obtener Detalle de Producto
- **Método:** `GET`
- **Ruta:** `/api/v1/inventory/products/{productId}`
- **Respuestas:** `200 OK`, `404 Not Found`

### 1.4. Actualizar Producto
- **Método:** `PUT`
- **Ruta:** `/api/v1/inventory/products/{productId}`
- **Respuestas:** `200 OK`, `404 Not Found`, `409 Conflict`

### 1.5. Eliminar Producto (TS004)
- **Método:** `DELETE`
- **Ruta:** `/api/v1/inventory/products/{productId}`
- **Respuestas:** `204 No Content`, `404 Not Found`, `409 Conflict` (producto en uso)

### 1.6. Entradas y Ajustes de Stock (TS011 / US009)
- **Método:** `POST`
- **Ruta:** `/api/v1/inventory/products/{productId}/batches`
- **Request Body:**
```json
{
  "quantity": 20,
  "acquisitionCost": 28.75
}
```
- **Cantidad positiva:** registra una entrada de stock.
- **Cantidad negativa:** ajuste de salida de almacén (deduce stock existente).
- **Respuestas:**
  - `201 Created`: movimiento aplicado.
  - `400 Bad Request`: cantidad cero, datos inválidos o stock insuficiente en ajuste negativo.
  - `404 Not Found`: producto inexistente.

### 1.7. Campo `lowStockAlert` (TS012 / US010)
Los recursos de producto incluyen `lowStockAlert: true|false`. Se actualiza automáticamente cuando el stock actual cae por debajo o igual al `minimumStock`, y se limpia cuando un nuevo lote eleva el stock por encima del umbral.

Un job programado (`MinimumStockAlertEvaluationJob`) reevalúa periódicamente todos los productos.

---

## Integración con Operations (TS010)

- `ProductReservedEvent`: descuenta stock al reservar productos en una orden de trabajo.
- `ProductReservationCanceledEvent`: libera stock cuando se cancela una reserva.
- `WorkOrderPaidEvent`: no requiere acción adicional.

---

## Eventos de dominio Inventory

- `LowStockAlertTriggeredEvent`: emitido cuando un producto entra en alerta de reposición.
- `LowStockAlertClearedEvent`: emitido cuando el stock supera nuevamente el umbral mínimo.
