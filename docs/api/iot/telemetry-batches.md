# IoT — Telemetry Batches (Ingesta Masiva)

**Base Path**: `/api/v1/telemetry-batches`  
**Context**: IoT Domain  
**Description**: Ingesta de lotes de telemetría desde gateways IoT / emuladores. Procesa snapshots + códigos DTC en una transacción.

---

## `POST /api/v1/telemetry-batches` — Ingestar Lote de Telemetría

**Security**: `@PreAuthorize("isAuthenticated()")` — Requiere token (gateway/emulador autenticado)  
**Description**: Recibe ráfaga de datos de sensores + DTCs. Procesa cada snapshot: guarda telemetría, detecta/guarda DTCs, emite eventos para analytics.

### Request

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `obd2DeviceId` | UUID | Sí | Dispositivo OBD2 origen (debe tener registro ACTIVE) |
| `snapshots` | array | Sí | Array de lecturas (mín 1, máx 100 por lote) |

**Snapshot Item**:

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `rpm` | integer | Sí | RPM motor (0-8000+) |
| `temperature` | integer | Sí | Temp. refrigerante °C (-40 a 150) |
| `speedKmh` | number | Sí | Velocidad km/h (0-300+) |
| `odometerKm` | number | Sí | Odómetro km (acumulativo, no decrece) |
| `fuelLevelPercent` | number | Sí | Nivel combustible % (0.0-100.0) |
| `createdAt` | ISO 8601 datetime | Sí | Timestamp de la lectura (ej: `2026-10-15T10:30:00Z`) |
| `dtcCodes` | array | No | Array de códigos DTC detectados en esta lectura |

**DTC Code Item**:

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `dtcCode` | string | Sí | Código OBD2 (ej: `P0300`, `P0420`, `U0100`) |
| `description` | string | Sí | Descripción legible |
| `severity` | string (enum) | Sí | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |

**Request Example**:
```json
{
  "obd2DeviceId": "obd1obd2-obd3-obd4-obd5-obd6obd7obd8",
  "snapshots": [
    {
      "rpm": 2200,
      "temperature": 91,
      "speedKmh": 55.4,
      "odometerKm": 25100,
      "fuelLevelPercent": 68.5,
      "createdAt": "2026-10-15T10:30:00Z",
      "dtcCodes": [
        {
          "dtcCode": "P0420",
          "description": "Catalyst System Efficiency Below Threshold (Bank 1)",
          "severity": "MEDIUM"
        }
      ]
    },
    {
      "rpm": 1800,
      "temperature": 90,
      "speedKmh": 0,
      "odometerKm": 25100,
      "fuelLevelPercent": 68.5,
      "createdAt": "2026-10-15T10:31:00Z",
      "dtcCodes": []
    }
  ]
}
```

### Success Response (201 CREATED)

```json
{
  "message": "Telemetry batch ingested successfully"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | DeviceId inválido, snapshots vacíos, campos requeridos faltantes, odometer decrece |
| 401 | `UNAUTHORIZED` | Token inválido/expirado |
| 403 | `ACCESS_DENIED` | Device no tiene registro ACTIVE en sede accesible |
| 404 | `NOT_FOUND` | Dispositivo OBD2 no existe |
| 422 | `UNPROCESSABLE_CONTENT` | Validación de negocio fallida (ej: RPM fuera de rango) |

### Important Considerations

- **Validación odometer**: Debe ser ≥ último odometer guardado para ese vehicle/registration (no decrece)
- **Validación RPM**: Rango típico 0-8000 (configurable por vehículo)
- **Procesamiento DTC**: Cada `dtcCode` único por snapshot → crea `DtcAlert` + emite `DtcAlertTriggeredEvent` → Analytics `dtcAlertsCount++`
- **Analytics**: Cada snapshot válido → `AppointmentCreatedEvent` no (solo citas), pero telemetría alimenta dashboards
- **Idempotencia**: No garantizada — mismo lote enviado 2 veces = 2 lecturas guardadas
- **Throughput**: Máx 100 snapshots/lote; para volúmenes altos, enviar lotes concurrentes
- **Autenticación**: Gateway/emulador debe tener user con rol adecuado (típicamente `ROLE_EMPLOYEE` o service account)

### Flujo Interno (Para Debugging)

```
POST /telemetry-batches
  → Valida deviceId + registro ACTIVE + acceso a sede
  → Por cada snapshot:
      → Valida rangos (RPM, temp, speed, odometer, fuel)
      → Guarda TelemetrySnapshot (linked to Obd2DeviceRegistration)
      → Por cada dtcCode:
          → Guarda DtcAlert (linked to snapshot)
          → Emite DtcAlertTriggeredEvent → AnalyticsDeltaDispatcher
  → Retorna 201
```

### Códigos DTC Comunes (Referencia)

| Código | Significado | Severidad Típica |
|--------|-------------|------------------|
| `P0300` | Random/Multiple Cylinder Misfire | HIGH |
| `P0420` | Catalyst Efficiency Below Threshold | MEDIUM |
| `P0171` | System Too Lean (Bank 1) | MEDIUM |
| `P0442` | Evap Leak Detected (Small) | LOW |
| `U0100` | Lost Communication with ECM | CRITICAL |
| `C1234` | Chassis codes (ABS, etc.) | Variable |