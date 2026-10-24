# Operations — Services Catalog

**Base Path**: `/api/v1/services`  
**Context**: Operations Domain  
**Description**: Catálogo de servicios ofertados por sede (mano de obra).

---

## `POST /api/v1/services` — Crear Servicio

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `isAuthorizedForBranch(branchId)`  
**Description**: Crea servicio en catálogo de una sede.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `name` | string | Sí | Nombre servicio (ej: "Alineación y Balanceo Computarizado") |
| `price` | number (decimal) | Sí | Precio en soles (ej: `80.00`) |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "name": "Alineación y Balanceo Computarizado",
  "price": 80.00
}
```

### Success Response (201 CREATED)

```json
{
  "id": "srv1srv2-srv3-srv4-srv5-srv6srv7srv8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "name": "Alineación y Balanceo Computarizado",
  "price": 80.00
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Precio negativo, nombre vacío |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede no existe |

---

## `GET /api/v1/services?branchId={branchId}` — Listar Servicios de la Sede

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista servicios activos de una sede.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |

### Success Response (200 OK)

```json
[
  {
    "id": "srv1srv2-srv3-srv4-srv5-srv6srv7srv8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "name": "Alineación y Balanceo Computarizado",
    "price": 80.00
  }
]
```

---

## `PUT /api/v1/services/{serviceId}` — Actualizar Servicio

**Security**: Validación manual `isAuthorizedForBranch(service.branchId)`  
**Description**: Actualiza nombre y/o precio del servicio.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `name` | string | No | Nuevo nombre |
| `price` | number | No | Nuevo precio |

**Request Example**:
```json
{
  "name": "Alineación 3D y Balanceo",
  "price": 95.00
}
```

### Success Response (200 OK)

`ServiceResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Precio negativo |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede del servicio |
| 404 | `NOT_FOUND` | Servicio no existe |

---

## `DELETE /api/v1/services/{serviceId}` — Eliminar Servicio

**Security**: Validación manual `isAuthorizedForBranch(service.branchId)`  
**Description**: Elimina servicio del catálogo.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Servicio no existe |
| 409 | `CONFLICT` | Servicio usado en Work Order Tasks activas |

### Important Considerations

- **Uso en OT**: No se puede eliminar si hay `WorkOrderTask` referenciándolo (incluso completadas)
- **Alternativa**: No hay soft delete — eliminar requiere limpiar referencias primero