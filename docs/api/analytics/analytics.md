# Analytics — KPIs & Dashboards

**Base Path**: `/api/v1/analytics`  
**Context**: Analytics Domain  
**Description**: Métricas diarias por sede (snapshots), rango histórico, y vista consolidada multi-sede.

---

## Arquitectura de Datos

- **`BranchAnalyticsSnapshot`**: Un registro por (sede, día calendario en `America/Lima`)
- **UPSERT Atómico**: `ON CONFLICT (branch_id, snapshot_date)` — sin race conditions
- **Delta Dispatcher**: Eventos → upsert; si falla BD → encola en `branch_analytics_pending_deltas`
- **Replay Job**: Cron cada 5 min (`analytics.replay-deltas.cron`), máx 10 reintentos, aplica + borra en misma transacción
- **Clamp ≥ 0**: Valores nunca negativos (revenue, contadores)
- **Zona horaria**: `analytics.zone-id=America/Lima` — día de negocio ≠ día UTC

---

## `GET /api/v1/analytics/branches/{branchId}/summary` — Resumen Ejecutivo del Día

**Security**: `hasPermissionForBranch(branchId, Permission.ANALYTICS_READ)` + `branchExists(branchId)`  
**Description**: KPIs del día actual (zona `America/Lima`) para una sede.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `branchId` | UUID | Sí | Sede (path) |

### Success Response (200 OK)

```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "totalRevenue": 1250.00,
  "completedWorkOrdersCount": 4,
  "totalAppointmentsCount": 7,
  "lowStockAlertsCount": 2,
  "dtcAlertsCount": 1
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | branchId no es UUID válido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede no existe (o eliminada) |
| 500 | `UNEXPECTED_ERROR` | Error consulta BD |

### Important Considerations

- **`totalRevenue`**: Ingreso **reconocido** al completar OT (no al cobrar). Reabrir OT revierte ingreso + contador (clamp ≥ 0)
- **Día de negocio**: `analytics.zone-id` (default `America/Lima`) — evento 02:00 UTC = día anterior en Lima
- **Consistencia**: UPSERT atómico por (sede, día) — sin read-modify-write

---

## `GET /api/v1/analytics/branches/{branchId}/financial` — Analítica Financiera Histórica

**Security**: `hasPermissionForBranch(branchId, Permission.ANALYTICS_READ)` + `branchExists(branchId)`  
**Description**: Serie histórica de snapshots diarios para gráficos (barras/líneas).

### Request

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `branchId` | UUID | Sí | — | Sede (path) |
| `startDate` | ISO date (`YYYY-MM-DD`) | No | Hoy - 30 días | Inicio rango |
| `endDate` | ISO date (`YYYY-MM-DD`) | No | Hoy | Fin rango |

### Success Response (200 OK)

```json
[
  {
    "id": "snap1snap2-snap3-snap4-snap5snap6snap7",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "snapshotDate": "2026-10-06",
    "totalRevenue": 950.00,
    "completedWorkOrdersCount": 3,
    "totalAppointmentsCount": 5,
    "lowStockAlertsCount": 2,
    "dtcAlertsCount": 0
  },
  {
    "id": "snap2snap3-snap4-snap5-snap6-snap7snap8snap9",
    "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
    "snapshotDate": "2026-10-07",
    "totalRevenue": 1250.00,
    "completedWorkOrdersCount": 4,
    "totalAppointmentsCount": 7,
    "lowStockAlertsCount": 2,
    "dtcAlertsCount": 1
  }
]
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | `startDate > endDate`, rango > 365 días, formato fecha inválido |
| 403 | `ACCESS_DENIED` | Sin acceso a la sede |
| 404 | `NOT_FOUND` | Sede no existe |
| 500 | `UNEXPECTED_ERROR` | Error consulta BD |

### Important Considerations

- **Rango máximo**: 365 días
- **Orden**: Ascendente por `snapshotDate`
- **Días sin datos**: No se incluyen (solo días con snapshot)
- **Frontend**: Útil para Chart.js / Recharts — series temporales

---

## `GET /api/v1/analytics/network/summary` — Resumen Consolidado Red Multi-Sede

**Security**: `@PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")`  
**Description**: Consolida KPIs del día de sedes visibles para el caller.

### Reglas de Visibilidad

| Rol | Sedes Agregadas |
|-----|-----------------|
| `ROLE_ADMIN` | **Todas** las sedes de la plataforma |
| `ROLE_OWNER` | **Solo sedes de sus talleres** (tenant isolation) |
| Otros | 403 Forbidden |

### Success Response (200 OK)

```json
{
  "totalActiveBranchesCount": 3,
  "totalNetworkRevenue": 4850.00,
  "totalNetworkCompletedWorkOrders": 14,
  "totalNetworkAppointments": 22,
  "totalNetworkDtcAlerts": 3
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No es ADMIN ni OWNER |
| 500 | `UNEXPECTED_ERROR` | Error consulta BD |

### Important Considerations

- **`totalActiveBranchesCount`**: Cuenta sedes del alcance del caller (no dadas de baja), incluidas sin actividad hoy
- **Otros campos**: Suman solo snapshots del día actual
- **Owner sin talleres**: Respuesta toda en ceros
- **Uso**: Dashboard dueño/admin — vista global

---

## Eventos que Alimentan Analytics

| Evento | Origen | Efecto en Snapshot |
|--------|--------|-------------------|
| `AppointmentCreated` | Fleet | `totalAppointmentsCount++` |
| `WorkOrderCompleted` | Operations | `totalRevenue += ot.totalAmount`, `completedWorkOrdersCount++` |
| `WorkOrderReopened` | Operations | `totalRevenue -= ot.totalAmount` (clamp ≥ 0), `completedWorkOrdersCount--` |
| `DtcAlertTriggered` | IoT | `dtcAlertsCount++` |
| `LowStockAlertTriggered` | Inventory | `lowStockAlertsCount` = recalculado desde inventario (no incremental) |

### Flujo Resiliente (Delta Dispatcher + Replay)

```
Domain Event → AnalyticsDeltaDispatcher
  → Intenta UPSERT BranchAnalyticsSnapshot
  → Si éxito: OK
  → Si fallo BD: INSERT branch_analytics_pending_deltas (delta + metadata)
  
ReplayPendingAnalyticsDeltasJob (cron 5 min)
  → SELECT pending deltas (ORDER BY created_at, LIMIT 100)
  → Por cada delta:
      → BEGIN TRANSACTION
      → UPSERT snapshot (aplica delta)
      → DELETE pending delta
      → COMMIT
      → Si fallo: incrementa attempt_count, si > max_attempts → alerta
```

### Important Considerations

- **Sin pérdida**: Cola persistente garantiza eventual consistency
- **Reintentos**: Máx 10 (config `analytics.replay-deltas.max-attempts`)
- **Orden**: FIFO por `created_at` — respeta causalidad
- **Monitoreo**: Alertar si `pending_deltas` crece o `attempt_count` alto

---

## `GET /api/v1/analytics/me/customer-summary` — Customer Self-Service Dashboard Summary

**Security**: `@PreAuthorize("isAuthenticated()")` (scoped strictly to the authenticated user's `Customer` profile)  
**Description**: Aggregates active vehicles, active DTC alerts, upcoming `PENDING` appointments, active work orders (`PENDING` / `IN_PROGRESS`), and `DRAFT` quotes for the authenticated customer via read-only ACL ports.

### Success Response (200 OK)

```json
{
  "customerId": "11111111-2222-3333-4444-555555555555",
  "activeVehiclesCount": 2,
  "activeDtcAlertsCount": 1,
  "upcomingAppointmentsCount": 1,
  "activeWorkOrdersCount": 1,
  "pendingQuotesCount": 1,
  "upcomingAppointments": [
    {
      "appointmentId": "aaaa1111-2222-3333-4444-555555555555",
      "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
      "vehicleId": "bbbb1111-2222-3333-4444-555555555555",
      "scheduledStart": "2026-11-15T10:00:00",
      "status": "PENDING"
    }
  ],
  "activeWorkOrders": [
    {
      "workOrderId": "cccc1111-2222-3333-4444-555555555555",
      "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
      "vehicleId": "bbbb1111-2222-3333-4444-555555555555",
      "internalNumber": 1042,
      "status": "IN_PROGRESS"
    }
  ],
  "pendingQuotes": [
    {
      "quoteId": "dddd1111-2222-3333-4444-555555555555",
      "workOrderId": "cccc1111-2222-3333-4444-555555555555",
      "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
      "totalAmount": 250.00,
      "status": "DRAFT"
    }
  ]
}
```

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 404 | `CUSTOMER_NOT_FOUND` | Authenticated user has no linked `Customer` profile |

---

## `GET /api/v1/analytics/me/employee-summary` — Employee Personal Work Queue Summary

**Security**: `@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'BRANCH_MANAGER', 'ASSISTANT', 'EMPLOYEE')")`  
**Description**: Aggregates assigned active work orders, pending/completed task counts for the authenticated employee, and active DTC alerts on vehicles in their active queue via read-only ACL ports.

### Success Response (200 OK)

```json
{
  "employeeId": "22222222-3333-4444-5555-666666666666",
  "branchIds": [
    "c3d4e5f6-3333-4444-5555-666677778888"
  ],
  "assignedWorkOrdersCount": 1,
  "pendingTasksCount": 2,
  "completedTasksCount": 5,
  "activeDtcAlertsCount": 1,
  "assignedWorkOrders": [
    {
      "workOrderId": "cccc1111-2222-3333-4444-555555555555",
      "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
      "vehicleId": "bbbb1111-2222-3333-4444-555555555555",
      "internalNumber": 1042,
      "status": "IN_PROGRESS",
      "assignedTasksCount": 2,
      "pendingTasksCount": 1
    }
  ]
}
```

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | Caller is `ROLE_USER` (customer) |
| 404 | `EMPLOYEE_NOT_FOUND` | Authenticated staff user has no linked `Employee` profile |
