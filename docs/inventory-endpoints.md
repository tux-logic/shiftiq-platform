# Inventory Bounded Context - Endpoints Documentation

Este documento detalla los endpoints REST expuestos por el Bounded Context de **Inventory** (Inventario) en la plataforma ShiftIQ.

---

## Products (Productos)
**Base URL:** `/api/v1/inventory/products`

Los productos representan repuestos y materiales del taller. Cada producto pertenece a una sucursal (`branchId`) y puede tener múltiples lotes (`batches`) que documentan entradas de stock.

### 1.1. Crear un Producto (Create Product)
Registra un nuevo producto en el inventario de una sucursal.

- **Método:** `POST`
- **Ruta:** `/api/v1/inventory/products`
- **Request Body:**
```json
{
  "branchId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "category": "PART",
  "name": "Filtro de aceite",
  "sku": "FLT-001",
  "description": "Filtro compatible con motores 1.6L",
  "salePrice": 45.50,
  "minimumStock": 5
}
```
- **Respuestas:**
  - `201 Created`: Producto creado exitosamente.
  - `400 Bad Request`: Errores de validación en el payload.
  - `409 Conflict`: Ya existe un producto con el mismo SKU en la sucursal.

### 1.2. Listar Productos por Sucursal
Obtiene el catálogo de productos activos de una sucursal.

- **Método:** `GET`
- **Ruta:** `/api/v1/inventory/products?branchId={branchId}`
- **Query Parameter:** `branchId` (UUID de la sucursal)
- **Respuestas:**
  - `200 OK`: Lista de productos (puede estar vacía si la sucursal no tiene productos).

### 1.3. Obtener Detalle de Producto
Recupera un producto con sus lotes asociados.

- **Método:** `GET`
- **Ruta:** `/api/v1/inventory/products/{productId}`
- **Respuestas:**
  - `200 OK`: Detalle completo del producto.
  - `404 Not Found`: El producto no existe.

### 1.4. Actualizar Producto
Modifica los datos básicos de un producto existente.

- **Método:** `PUT`
- **Ruta:** `/api/v1/inventory/products/{productId}`
- **Request Body:**
```json
{
  "name": "Filtro de aceite premium",
  "category": "PART",
  "sku": "FLT-001",
  "description": "Filtro de alta eficiencia",
  "salePrice": 52.00,
  "minimumStock": 8
}
```
- **Respuestas:**
  - `200 OK`: Producto actualizado.
  - `404 Not Found`: El producto no existe.
  - `409 Conflict`: El SKU ya está en uso por otro producto de la misma sucursal.

### 1.5. Eliminar Producto
Elimina lógicamente un producto y sus lotes asociados.

- **Método:** `DELETE`
- **Ruta:** `/api/v1/inventory/products/{productId}`
- **Respuestas:**
  - `204 No Content`: Producto eliminado.
  - `404 Not Found`: El producto no existe.
  - `409 Conflict`: El producto está asociado a órdenes de trabajo y no puede eliminarse.

### 1.6. Agregar Lote a Producto
Registra una entrada de stock mediante un nuevo lote.

- **Método:** `POST`
- **Ruta:** `/api/v1/inventory/products/{productId}/batches`
- **Request Body:**
```json
{
  "quantity": 20,
  "acquisitionCost": 28.75
}
```
- **Respuestas:**
  - `201 Created`: Lote registrado y stock actualizado.
  - `400 Bad Request`: Cantidad o costo inválidos.
  - `404 Not Found`: El producto no existe.

---

## Integración con Operations

El contexto Inventory escucha eventos de Operations para reservar y liberar stock:

- `ProductReservedEvent`: descuenta stock al reservar productos en una orden de trabajo.
- `ProductReservationCanceledEvent`: libera stock cuando se cancela una reserva.
- `WorkOrderPaidEvent`: no requiere acción adicional (el stock ya fue reservado previamente).
