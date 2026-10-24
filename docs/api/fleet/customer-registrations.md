# Fleet — Customer Registrations

**Base Path**: `/api/v1/customer-registrations`  
**Context**: Fleet Domain  
**Description**: Vinculación de clientes a sedes (cliente puede estar en múltiples sedes).

---

## `POST /api/v1/customer-registrations` — Registrar Cliente en Sede

**Security**: `@PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId()))")`  
**Description**: Crea vinculación cliente ↔ sede. Estado inicial: `ACTIVE`.

### Request

| Campo | Tipo | Requerido | Ubicación | Descripción |
|-------|------|-----------|-----------|-------------|
| `branchId` | UUID | Sí | Body | Sede destino (caller debe tener acceso) |
| `customerId` | UUID | Sí | Body | Customer profile ID (Core) |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "reg1reg2-reg3-reg4-reg5-reg6reg7reg8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
  "status": "ACTIVE",
  "createdAt": "2026-10-15T10:30:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | UUIDs inválidos, cliente/sede no existen |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede (`isAuthorizedForBranch`) |
| 409 | `CONFLICT` | Cliente ya registrado en esa sede (ACTIVE) |

### Important Considerations

- **Autorización**: ADMIN ✓, o staff con acceso a la `branchId` (`isAuthorizedForBranch`)
- **Unicidad**: Un cliente solo puede tener un registro `ACTIVE` por sede
- **Uso**: Requerido para agendar citas (`POST /appointments` valida registro activo)

---

## `GET /api/v1/customer-registrations` — Listar Registros con Filtros

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual por filtro  
**Description**: Lista registros con filtros opcionales. Valida acceso según filtros usados.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `branchId` | UUID | No | Query | Filtrar por sede (requiere acceso a esa sede) |
| `status` | string (enum) | No | Query | `ACTIVE`, `INACTIVE` |
| `customerId` | UUID | No | Query | Filtrar por cliente |

### Success Response (200 OK)

```json
[
  {
    "id": "reg1reg2-reg3-reg4-reg5-reg6reg7reg8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "customerId": "cust1cust2-cust3-cust4-cust5-cust6cust7cu",
    "status": "ACTIVE",
    "createdAt": "2026-10-15T10:30:00Z"
  }
]
```

### Important Considerations

- **Filtro branchId**: Si se usa, valida `isAuthorizedForBranch(branchId)`
- **Filtro customerId**: Si se usa, busca registro → valida `isAuthorizedForBranch(registro.branchId)`
- **Sin filtros**: Retorna 400 (requiere al menos un filtro)

---

## `GET /api/v1/customer-registrations/{registrationId}` — Detalle de Vinculación

**Security**: Validación manual `isAuthorizedForBranch(registro.branchId)`  
**Description**: Retorna detalle de una vinculación por ID.

### Success Response (200 OK)

`CustomerRegistrationResource` completo.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede del registro |
| 404 | `NOT_FOUND` | Registro no existe |

---

## `PUT /api/v1/customer-registrations/{registrationId}` — Cambiar Estado

**Security**: Validación manual `isAuthorizedForBranch(registro.branchId)`  
**Description**: Actualiza estado del registro (ej: `ACTIVE` → `INACTIVE`).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `status` | string (enum) | Sí | `ACTIVE` o `INACTIVE` |

**Request Example**:
```json
{
  "status": "INACTIVE"
}
```

### Success Response (200 OK)

`CustomerRegistrationResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Estado inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Registro no existe |

---

## `DELETE /api/v1/customer-registrations/{registrationId}` — Desactivar Registro

**Security**: Validación manual `isAuthorizedForBranch(registro.branchId)`  
**Description**: Soft delete — pasa a `INACTIVE`.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Registro no existe |

### Important Considerations

- **Soft delete**: No borra, solo cambia estado a `INACTIVE`
- **Citas existentes**: No afecta citas ya agendadas (validan registro al crear)
- **Reactivación**: Usar `PUT` con `status: "ACTIVE"`