# IoT — OBD2 Devices

**Base Path**: `/api/v1/obd2-devices`  
**Context**: IoT Domain  
**Description**: Gestión de dispositivos físicos OBD2 (dongles) por sede.

---

## `POST /api/v1/obd2-devices` — Registrar Dispositivo OBD2

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())")`  
**Description**: Registra escáner OBD2 físico en sede. Estado inicial: `AVAILABLE`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `macAddress` | string (MAC) | Sí | MAC del dispositivo (ej: `00:1B:44:11:3A:B7`) |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "macAddress": "00:1B:44:11:3A:B7"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "macAddress": "00:1B:44:11:3A:B7",
  "status": "AVAILABLE"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | MAC inválido, sede inexistente |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 409 | `CONFLICT` | MAC ya registrado |

---

## `GET /api/v1/obd2-devices?branchId={branchId}&status={status}` — Listar Dispositivos

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista dispositivos de una sede. Filtro opcional `status=available` para no vinculados.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `status` | string | No | `available` = solo `AVAILABLE` (sin vincular) |

### Success Response (200 OK)

```json
[
  {
    "id": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "macAddress": "00:1B:44:11:3A:B7",
    "status": "AVAILABLE"
  }
]
```

---

## `GET /api/v1/obd2-devices/{id}` — Detalle de Dispositivo

**Security**: Validación manual `isAuthorizedForBranch(device.branchId)`  
**Description**: Retorna detalle del dispositivo.

### Success Response (200 OK)

```json
{
  "id": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "macAddress": "00:1B:44:11:3A:B7",
  "status": "AVAILABLE"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede del dispositivo |
| 404 | `NOT_FOUND` | Dispositivo no existe |

---

## `PUT /api/v1/obd2-devices/{id}` — Actualizar Dispositivo

**Security**: Validación manual `isAuthorizedForBranch(device.branchId)`  
**Description**: Actualiza MAC address del dispositivo.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `macAddress` | string (MAC) | Sí | Nueva MAC |

### Success Response (200 OK)

`Obd2DeviceResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | MAC inválido, duplicado |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Dispositivo no existe |

---

## `DELETE /api/v1/obd2-devices/{id}` — Eliminar Dispositivo (Soft Delete)

**Security**: Validación manual `isAuthorizedForBranch(device.branchId)`  
**Description**: Soft delete con validaciones de estado.

### Success Response (204 NO CONTENT)

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Estado inválido para eliminar (ej: tiene registro ACTIVE) |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Dispositivo no existe |
| 409 | `CONFLICT` | Dispositivo vinculado a vehículo (registro ACTIVE) |

### Important Considerations

- **Validación previa**: No se puede eliminar si tiene `Obd2DeviceRegistration` ACTIVE
- **Soft delete**: Estado pasa a `DELETED` (no `AVAILABLE`)

---

## `GET /api/v1/obd2-devices/{deviceId}/telemetry-snapshots/latest` — Última Lectura en Vivo

**Security**: Validación manual `isAuthorizedForBranch(device.branchId)`  
**Description**: **Endpoint clave para dashboard en vivo** — última telemetría del dispositivo.

### Success Response (200 OK)

```json
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
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Dispositivo no existe o sin lecturas |

### Important Considerations

- **Tiempo real**: Frontend debe pollear cada 5-10s para dashboard en vivo
- **Registro activo**: Requiere `Obd2DeviceRegistration` ACTIVE para tener lecturas
- **Campos**: `rpm`, `temperature` (°C), `speedKmh`, `odometerKm`, `fuelLevelPercent` (%)

---

## `GET /api/v1/obd2-devices/{deviceId}/telemetry-snapshots` — Historial de Telemetría

**Security**: Validación manual `isAuthorizedForBranch(device.branchId)` + paginación  
**Description**: Historial completo ordenado descendente por fecha.

### Request

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | integer | No | 0 | Página |
| `size` | integer | No | 20 | Tamaño página |

### Success Response (200 OK)

Array de `TelemetrySnapshotResource` (mismo formato que `/latest`).