# IoT — OBD2 Device Registrations (Vinculación Escáner ↔ Vehículo)

**Base Path**: `/api/v1/obd2-device-registrations`  
**Context**: IoT Domain  
**Description**: Vincula un dispositivo OBD2 físico a un vehículo en una sede específica.

---

## `POST /api/v1/obd2-device-registrations` — Vincular OBD2 a Vehículo

**Security**: Validación manual `isAuthorizedForBranch(branchId)` + validaciones de estado  
**Description**: Crea vinculación escáner ↔ vehículo. Estado inicial: `ACTIVE`.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `obd2DeviceId` | UUID | Sí | Dispositivo OBD2 (debe estar `AVAILABLE`) |
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `vehicleId` | UUID | Sí | Vehículo (debe estar en la sede, sin OBD2 activo) |

**Request Example**:
```json
{
  "obd2DeviceId": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8"
}
```

### Success Response (201 CREATED)

```json
{
  "id": "reg1reg2-reg3-reg4-reg5-reg6reg7reg8",
  "obd2DeviceId": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
  "status": "ACTIVE",
  "createdAt": "2026-10-15T10:30:00Z"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Dispositivo no `AVAILABLE`, vehículo ya vinculado, sede inválida |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Dispositivo/Vehículo/Sede no existe |
| 409 | `CONFLICT` | Dispositivo ya vinculado, o vehículo ya tiene OBD2 activo |

### Important Considerations

- **Exclusividad**: Un dispositivo OBD2 solo puede tener 1 registro ACTIVE; un vehículo solo 1 OBD2 ACTIVE
- **Validaciones**: `obd2Device.status == AVAILABLE`, `vehicle` sin registro ACTIVE
- **Efecto**: Dispositivo pasa a `LINKED`; vehículo puede recibir telemetría

---

## `PATCH /api/v1/obd2-device-registrations/{id}` — Desvincular / Cambiar Estado

**Security**: Validación manual `isAuthorizedForBranch(registration.branchId)`  
**Description**: Cambia estado del registro (ej: `ACTIVE` → `INACTIVE` para desvincular).

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

`Obd2DeviceRegistrationResource` actualizado.

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Estado inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Registro no existe |

### Important Considerations

- **Desvinculación**: `INACTIVE` → dispositivo vuelve a `AVAILABLE`, vehículo libre para otro OBD2
- **Telemetría histórica**: Se conserva (consultable por registrationId)

---

## `GET /api/v1/obd2-device-registrations?branchId={branchId}&status={status}` — Listar Vinculaciones

**Security**: `@PreAuthorize("isAuthenticated() and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)")`  
**Description**: Lista vinculaciones de una sede filtradas por estado.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (caller debe tener acceso) |
| `status` | string | Sí | `ACTIVE`, `INACTIVE` |

### Success Response (200 OK)

```json
[
  {
    "id": "reg1reg2-reg3-reg4-reg5-reg6reg7reg8",
    "obd2DeviceId": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "vehicleId": "veh1veh2-veh3-veh4-veh5-veh6veh7veh8",
    "status": "ACTIVE",
    "createdAt": "2026-10-15T10:30:00Z"
  }
]
```

---

## `GET /api/v1/obd2-device-registrations/{id}/telemetry-snapshots` — Telemetría de la Vinculación

**Security**: Validación manual `isAuthorizedForBranch(registration.branchId)` + paginación  
**Description**: Lecturas capturadas bajo esta vinculación específica.

### Request

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | integer | No | 0 | Página |
| `size` | integer | No | 20 | Tamaño página |

### Success Response (200 OK)

Array de `TelemetrySnapshotResource`.

---

## `GET /api/v1/obd2-device-registrations/{id}/dtc-alerts` — Alertas DTC de la Vinculación

**Security**: Validación manual `isAuthorizedForBranch(registration.branchId)` + paginación  
**Description**: Códigos de falla detectados durante esta vinculación.

### Request

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `page` | integer | No | 0 | Página |
| `size` | integer | No | 20 | Tamaño página |

### Success Response (200 OK)

Array de `DtcAlertResource`.