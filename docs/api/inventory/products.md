# Inventory — Products & Stock

**Base Path**: `/api/v1/inventory/products`  
**Context**: Inventory Domain  
**Description**: Gestión de productos/repuestos, lotes (batches), stock y alertas.

---

## `POST /api/v1/inventory/products` — Crear Producto / Repuesto

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")`  
**Description**: Crea producto en inventario de una sede. Stock inicial: 0.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `category` | string | Sí | Categoría (ej: "Filtros", "Frenos", "Aceites") |
| `name` | string | Sí | Nombre (ej: "Filtro de Aceite Sintético Premium") |
| `sku` | string | Sí | SKU único en sede (ej: `FLT-OIL-001`) |
| `description` | string | No | Descripción |
| `salePrice` | number (decimal) | Sí | Precio venta soles (ej: `45.00`) |
| `minimumStock` | integer | Sí | Stock mínimo para alerta (ej: `5`) |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "category": "Filtros",
  "name": "Filtro de Aceite Sintético Premium",
  "sku": "FLT-OIL-001",
  "description": "Filtro de alto flujo para motores de 4 cilindros",
  "salePrice": 45.00,
  "minimumStock": 5
}
```

### Success Response (201 CREATED)

```json
{
  "id": "prod1prod2-prod3-prod4-prod5prod6prod7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "category": "Filtros",
  "name": "Filtro de Aceite Sintético Premium",
  "sku": "FLT-OIL-001",
  "description": "Filtro de alto flujo para motores de 4 cilindros",
  "salePrice": 45.00,
  "minimumStock": 5,
  "currentStock": 0,
  "lowStockAlert": true
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | SKU duplicado, precio negativo, stock mínimo negativo |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede no existe |
| 409 | `CONFLICT` | SKU ya existe en la sede |

---

## `GET /api/v1/inventory/products?branchId={branchId}` — Listar Productos con Filtros

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista productos de una sede con filtros opcionales.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `name` | string | No | Filtro nombre parcial (case-insensitive) |
| `category` | string | No | Filtro categoría exacta |
| `lowStockOnly` | boolean | No | `true` = solo productos con `currentStock <= minimumStock` |

### Success Response (200 OK)

```json
[
  {
    "id": "prod1prod2-prod3-prod4-prod5prod6prod7",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "category": "Filtros",
    "name": "Filtro de Aceite Sintético Premium",
    "sku": "FLT-OIL-001",
    "description": "Filtro de alto flujo para motores de 4 cilindros",
    "salePrice": 45.00,
    "minimumStock": 5,
    "currentStock": 20,
    "lowStockAlert": false
  }
]
```

---

## `GET /api/v1/inventory/products/branch/{branchId}` — Catálogo de Productos

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: **Endpoint simplificado** para selectores/dropdowns en app móvil. Mismos filtros que endpoint principal.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (path) |
| `name` | string | No | Filtro nombre |
| `category` | string | No | Filtro categoría |
| `lowStockOnly` | boolean | No | Solo stock bajo |

### Success Response (200 OK)

Mismo formato que listado principal.

---

## `GET /api/v1/inventory/products/{productId}` — Detalle del Producto con Lotes

**Security**: Validación manual `isAuthorizedForBranch(product.branchId)`  
**Description**: Retorna producto + todos sus lotes (historial de entradas).

### Success Response (200 OK)

```json
{
  "id": "prod1prod2-prod3-prod4-prod5prod6prod7",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "category": "Filtros",
  "name": "Filtro de Aceite Sintético Premium",
  "sku": "FLT-OIL-001",
  "description": "Filtro de alto flujo para motores de 4 cilindros",
  "salePrice": 45.00,
  "minimumStock": 5,
  "currentStock": 20,
  "lowStockAlert": false,
  "batches": [
    {
      "batchId": "BATCH-2026-10-A",
      "initialQuantity": 20,
      "availableQuantity": 20,
      "acquisitionCost": 22.50,
      "createdAt": "2026-10-05T10:00:00Z"
    }
  ]
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Producto no existe |

---

## `PUT /api/v1/inventory/products/{productId}` — Actualizar Información de Producto

**Security**: Validación manual `isAuthorizedForBranch(product.branchId)`  
**Description**: Actualiza datos básicos (no stock — para stock usar `/batches`).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `name` | string | No | Nuevo nombre |
| `category` | string | No | Nueva categoría |
| `sku` | string | No | Nuevo SKU (único en sede) |
| `description` | string | No | Nueva descripción |
| `salePrice` | number | No | Nuevo precio venta |
| `minimumStock` | integer | No | Nuevo stock mínimo |

### Success Response (200 OK)

`ProductResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | SKU duplicado, precio negativo |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Producto no existe |
| 409 | `CONFLICT` | SKU duplicado |

---

## `POST /api/v1/inventory/products/{productId}/batches` — Ingreso de Lote / Ajuste de Stock

**Security**: Validación manual `isAuthorizedForBranch(product.branchId)`  
**Description**: Añade stock (compra) o ajusta (negativo). Crea lote FIFO.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `quantity` | integer | Sí | **Positivo** = ingreso compra; **Negativo** = ajuste/salida |
| `acquisitionCost` | number (decimal) | Sí* | Costo unitario compra (requerido si quantity > 0) |

*Si `quantity < 0`, `acquisitionCost` se ignora (puede enviar 0).

**Request Example** (Ingreso compra):
```json
{
  "quantity": 15,
  "acquisitionCost": 24.00
}
```

**Request Example** (Ajuste salida):
```json
{
  "quantity": -3,
  "acquisitionCost": 0
}
```

### Success Response (200 OK / 201 CREATED)

```json
{
  "message": "Stock batch updated successfully",
  "currentStock": 35
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Cantidad 0, costo negativo en ingreso |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Producto no existe |
| 409 | `CONFLICT` | Stock resultante negativo (`INSUFFICIENT_STOCK`) |

### Important Considerations

- **FIFO**: Ingresos crean lote nuevo; salidas consumen del lote más antiguo con `availableQuantity > 0`
- **Stock actual**: `currentStock = sum(availableQuantity)` across lotes
- **Alerta stock bajo**: `lowStockAlert = (currentStock <= minimumStock)` — se recalcula automáticamente
- **Costo promedio**: No se mantiene — cada lote tiene su `acquisitionCost`
- **Uso en OT**: Al agregar producto a task, reserva de `availableQuantity` (FIFO)

---

## `DELETE /api/v1/inventory/products/{productId}` — Eliminar Producto

**Security**: Validación manual `isAuthorizedForBranch(product.branchId)`  
**Description**: Elimina producto y **todos sus lotes**.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Producto no existe |
| 409 | `CONFLICT` | Producto referenciado en `WorkOrderTaskProduct` activas (`PRODUCT_IN_USE`) |

### Important Considerations

- **Cascada**: Borra lotes + historial
- **Protección**: No permite borrar si hay reservas activas en OTs
- **Alternativa**: Poner `minimumStock = 0` y `salePrice = 0` para "desactivar" sin borrar