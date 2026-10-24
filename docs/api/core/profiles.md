# Core — Profiles & Document Lookup

**Base Path**: `/api/v1/profiles`  
**Context**: Core Domain  
**Description**: Consultas transversales de perfiles y roles de usuario.

---

## `GET /api/v1/profiles/roles?userId={userId}` — Obtener Roles Activos del Usuario

**Security**: Validación manual `validateUserAccess(userId)`  
**Description**: Retorna lista de roles que tiene el usuario en perfiles Core (ej: `OWNER`, `EMPLOYEE`, `CUSTOMER`). Útil para redirección post-login.

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `userId` | UUID | Sí | Query | User ID (IAM) |

### Success Response (200 OK)

```json
[
  "ROLE_OWNER",
  "ROLE_EMPLOYEE"
]
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 403 | `ACCESS_DENIED` | No autorizado para ese usuario (`isAuthorizedForUser`) |
| 404 | `NOT_FOUND` | Usuario no tiene perfiles |

### Important Considerations

- **Fuente**: Consulta perfiles en Core (Owner, Employee, Customer) — no roles de IAM/JWT
- **Uso frontend**: Al login, llamar esto para saber a qué dashboard redirigir:
  - `ROLE_OWNER` → Dashboard dueño
  - `ROLE_EMPLOYEE` → App mecánico
  - `ROLE_CUSTOMER` → App cliente
  - Múltiples → Selector de perfil

---

## `GET /api/v1/profiles?documentNumber={documentNumber}` — Buscar Perfil por DNI

**Security**: Validación manual `validateUserAccess(userId del perfil encontrado)`  
**Description**: Búsqueda unificada por número de documento (DNI/RUC) across todos los perfiles (Owner, Employee, Customer).

### Request

| Parámetro | Tipo | Requerido | Ubicación | Descripción |
|-----------|------|-----------|-----------|-------------|
| `documentNumber` | string | Sí | Query | DNI (8 dígitos) o RUC (11 dígitos) |

### Success Response (200 OK)

```json
{
  "profileType": "EMPLOYEE",
  "userId": "e4f8b2d1-9a7c-4e8b-8c2d-1a2b3c4d5e6f",
  "firstName": "Luis",
  "lastName": "Mendoza",
  "documentType": "DNI",
  "documentNumber": "71234567",
  "phone": "+51912345678",
  "employeeId": "emp1emp2-emp3-emp4-emp5-emp6emp7emp8",
  "roles": ["ROLE_EMPLOYEE"]
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Formato documento inválido |
| 403 | `ACCESS_DENIED` | No autorizado para el usuario del perfil encontrado |
| 404 | `NOT_FOUND` | No hay perfil con ese documento |

### Important Considerations

- **Búsqueda global**: Recorre Owner, Employee, Customer por `documentNumber`
- **Retorna**: Tipo de perfil + datos combinados + roles IAM
- **Autorización**: `validateUserAccess(userId)` del perfil encontrado
- **Uso**: Registro rápido, verificación de existencia, autocompletado

---

## Related Endpoints

| Funcionalidad | Endpoint | Context |
|---------------|----------|---------|
| Validar DNI/RUC/CE oficial | `GET /api/v1/identity-validations/lookup` | Shared |
| Crear Owner profile | `POST /api/v1/owners` | Core |
| Crear Employee profile | `POST /api/v1/employees` | Core |
| Crear Customer profile | `POST /api/v1/customers` | Core |