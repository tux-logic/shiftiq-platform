# IoT — Vehicles

**Base Path**: `/api/v1/vehicles`  
**Context**: IoT Domain  
**Description**: Gestión de vehículos de clientes, telemetría OBD2 y alertas DTC.

---

## `POST /api/v1/vehicles` — Registrar Vehículo

**Security**: `@PreAuthorize("isAuthenticated()")` — Caller debe ser `ROLE_CUSTOMER` o `ROLE_OWNER`  
**Description**: Registra vehículo y **vincula automáticamente al usuario autenticado** (obtiene `userId` del JWT principal).

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `plateNumber` | string | Sí | Placa (ej: `ABC-123`, formato Perú) |
| `brand` | string | Sí | Marca (ej: `Toyota`) |
| `model` | string | Sí | Modelo (ej: `Corolla Cross`) |
| `year` | integer | Sí | Año (ej: `2023`) |
| `vin` | string (17 chars) | No | VIN (17 caracteres alfanuméricos) |
| `photoUrl` | string (URL) | No | URL foto (Cloudinary) |

**Request Example**:
```json
{
  "plateNumber": "ABC-123",
  "brand": "Toyota",
  "model": "Corolla Cross",
  "year": 2023,
  "vin": "9BRBL3HE2P0123456",
  "photoUrl": "https://res.cloudinary.com/shiftiq/image/upload/v1728329482/vehicles/corolla.jpg"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "plateNumber": "ABC-123",
  "brand": "Toyota",
  "model": "Corolla Cross",
  "year": 2023,
  "vin": "9BRBL3HE2P0123456",
  "photoUrl": "https://res.cloudinary.com/shiftiq/image/upload/v1728329482/vehicles/corolla.jpg"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Placa duplicada, VIN inválido, campos faltantes |
| 403 | `ACCESS_DENIED` | Caller no es `ROLE_CUSTOMER` ni `ROLE_OWNER` |
| 409 | `CONFLICT` | Placa ya registrada |

### Important Considerations

- **Auto-vinculación**: `userId` se toma del `AuthenticatedPrincipal` del JWT — **no se envía en body**
- **Perfil requerido**: Caller debe tener perfil `Customer` o `Owner` en Core
- **Uso posterior**: Para vincular a sede → `POST /customer-registrations`; para OBD2 → `POST /obd2-device-registrations`

---

## `GET /api/v1/vehicles?branchId={branchId}&status={status}` — Listar Vehículos por Sede

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista vehículos de una sede **solo con `status=available-for-linking`** (vehículos listos para vincular escáner OBD2).

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `status` | string | Sí | **Solo valor válido: `available-for-linking`** |

### Success Response (200 OK)

```json
[
  {
    "id": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
    "plateNumber": "ABC-123",
    "brand": "Toyota",
    "model": "Corolla Cross",
    "year": 2023,
    "vin": "9BRBL3HE2P0123456",
    "photoUrl": "https://res.cloudinary.com/shiftiq/image/upload/v1728329482/vehicles/corolla.jpg"
  }
]
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Parámetros faltantes |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 422 | `UNPROCESSABLE_CONTENT` | `status` distinto de `available-for-linking` |

### Important Considerations

- **Solo un status**: Cualquier otro valor → 422 con mensaje `iot.error.vehicle.unsupportedStatusFilter`
- **Filtro lógico**: Retorna vehículos sin `Obd2DeviceRegistration` ACTIVE en esa sede
- **Uso**: Frontend "Asignar escáner" → lista vehículos disponibles

---

## `GET /api/v1/vehicles/{id}` — Detalle de Vehículo

**Security**: Validación compleja `validateVehicleAccess(vehicleId, authentication)`  
**Description**: Retorna vehículo. Acceso si:
1. Tiene `Obd2DeviceRegistration` ACTIVE → valida `isAuthorizedForBranch(branchId del registro)`
2. Sino, tiene `VehicleRegistration` ACTIVE (driver) → valida que `userId` del registro == caller
3. Sino → 403

### Success Response (200 OK)

`VehicleResource` completo.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Ni staff de sede OBD2 ni driver registrado |
| 404 | `NOT_FOUND` | Vehículo no existe |

---

## `PUT /api/v1/vehicles/{id}` — Actualizar Vehículo

**Security**: Misma validación `validateVehicleAccess` que GET  
**Description**: Actualiza datos del vehículo.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `plateNumber` | string | No | Nueva placa |
| `brand` | string | No | Nueva marca |
| `model` | string | No | Nuevo modelo |
| `year` | integer | No | Nuevo año |
| `vin` | string | No | Nuevo VIN |
| `photoUrl` | string | No | Nueva foto |

### Success Response (200 OK)

`VehicleResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Placa duplicada, VIN inválido |
| 403 | `ACCESS_DENIED` | Sin acceso (ver GET) |
| 404 | `NOT_FOUND` | Vehículo no existe |

---

## `DELETE /api/v1/vehicles/{id}` — Eliminar Vehículo

**Security**: Misma validación `validateVehicleAccess`  
**Description**: Soft delete + desactiva vinculos activos (OBD2 + driver).

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso |
| 404 | `NOT_FOUND` | Vehículo no existe |

---

## `GET /api/v1/vehicles/{vehicleId}/telemetry-snapshots` — Historial de Telemetría

**Security**: `validateVehicleAccess` + paginación  
**Description**: Lecturas OBD2 del vehículo (desde registro activo).

### Request

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | integer | No | 0 | Página |
| `size` | integer | No | 20 | Tamaño página |

### Success Response (200 OK)

```json
[
  {
    "id": "tel1tel2-tel3-tel4-tel5-tel6tel7tel8",
    "obd2DeviceRegistrationId": "reg1reg2-reg3-reg4-reg5-reg6reg7reg8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "rpm": 2450,
    "temperature": 89,
    "speedKmh": 62.5,
    "odometerKm": 24890,
    "fuelLevelPercent": 74.0,
    "createdAt": "2026-10-15T10:30:00Z"
  }
]
```

---

## `GET /api/v1/vehicles/{vehicleId}/dtc-alerts` — Historial de Alertas DTC

**Security**: `validateVehicleAccess` + paginación  
**Description**: Códigos de falla (Check Engine) detectados.

### Request

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | integer | No | 0 | Página |
| `size` | integer | No | 20 | Tamaño página |

### Success Response (200 OK)

```json
[
  {
    "id": "dtc1dtc2-dtc3-dtc4-dtc5-dtc6dtc7dtc8",
    "telemetrySnapshotId": "tel1tel2-tel3-tel4-tel5-tel6tel7tel8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "dtcCode": "P0300",
    "description": "Random/Multiple Cylinder Misfire Detected",
    "severity": "HIGH",
    "createdAt": "2026-10-15T10:30:00Z"
  }
]
```

### Important Considerations

- **Severidad**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **Analytics**: Cada DTC emite `DtcAlertTriggeredEvent` → `dtcAlertsCount++` en analytics
- **Frontend**: Mostrar en dashboard vehículo + alerta push si `HIGH`/`CRITICAL`

---

## `GET /api/v1/customers/{customerId}/vehicles` — Listar Vehículos de un Cliente

**Security**: `@PreAuthorize("isAuthenticated()")` + validación manual `isCurrentUser(customerId)`  
**Controller**: `CustomerVehiclesController` (base path `/api/v1/customers`)  
**Description**: Retorna vehículos con registro activo (`VehicleRegistration` ACTIVE) para un cliente. Valida que el caller sea el propio cliente.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `customerId` | UUID | Sí | Customer profile ID (path) |

### Success Response (200 OK)

```json
[
  {
    "id": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
    "plateNumber": "ABC-123",
    "brand": "Toyota",
    "model": "Corolla Cross",
    "year": 2023,
    "vin": "9BRBL3HE2P0123456",
    "photoUrl": "https://res.cloudinary.com/shiftiq/image/upload/v1728329482/vehicles/corolla.jpg"
  }
]
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | JWT inválido |
| 403 | `ACCESS_DENIED` | No es el propio cliente (`isCurrentUser`) |
| 404 | `NOT_FOUND` | Cliente no existe o sin vehículos activos |

### Important Considerations

- **Filtro**: Solo retorna vehículos con `VehicleRegistration` ACTIVE para ese customerId
- **Autorización**: `UserSecurityService.isCurrentUser(customerId)` — solo el propio cliente o ADMIN
- **Controller**: Está en `CustomerVehiclesController` (`/api/v1/customers`), NO en `VehiclesController`
- **Uso**: Frontend cliente → "Mis Vehículos" → lista sus vehículos registrados