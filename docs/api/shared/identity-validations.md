# Shared — Identity Validations (RENIEC / SUNAT / Migraciones)

**Base Path**: `/api/v1/identity-validations`  
**Context**: Shared Kernel  
**Description**: Validación oficial de documentos peruanos vía Factiliza. Autocompletado de formularios.

---

## `GET /api/v1/identity-validations/lookup?type={type}&number={number}` — Consultar Documento

**Security**: `@PreAuthorize("isAuthenticated()")` — Bearer JWT requerido  
**Description**: Consulta RENIEC (DNI), SUNAT (RUC) o Migraciones (CE) vía Factiliza. Retorna datos oficiales para autocompletado.

### Request

| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `type` | string (enum) | Sí | `DNI`, `RUC`, `CE` (o `CEE`) |
| `number` | string | Sí | Número documento: DNI=8 dígitos, RUC=11 dígitos, CE=7-12 chars |

### Success Response — DNI (200 OK)

```json
{
  "type": "DNI",
  "documentNumber": "48765432",
  "firstName": "María",
  "lastName": "Fernández",
  "fullName": "María Fernández",
  "verified": true,
  "source": "RENIEC"
}
```

### Success Response — RUC (200 OK)

```json
{
  "type": "RUC",
  "documentNumber": "20549876123",
  "businessName": "EMPRESA EJEMPLO S.A.C.",
  "tradeName": "Ejemplo Corp",
  "address": "AV. PRIMAVERA 1234, SANTIAGO DE SURCO, LIMA",
  "condition": "HABIDO",
  "status": "ACTIVO",
  "verified": true,
  "source": "SUNAT"
}
```

### Success Response — CE (200 OK)

```json
{
  "type": "CE",
  "documentNumber": "001234567",
  "firstName": "Juan",
  "lastName": "Pérez",
  "fullName": "Juan Pérez",
  "nationality": "VENEZUELA",
  "verified": true,
  "source": "MIGRACIONES"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Tipo inválido, número formato incorrecto (DNI≠8, RUC≠11, CE<7/>12) |
| 401 | `UNAUTHORIZED` | JWT faltante/inválido |
| 404 | `NOT_FOUND` | Documento no encontrado en registro oficial |
| 500 | `UNEXPECTED_ERROR` | Error Factiliza (timeout, credenciales, cuota) |
| 503 | `SERVICE_UNAVAILABLE` | Factiliza no configurado (`factiliza.api.token` vacío) |

### Important Considerations

- **Validación estricta**:
  - `DNI`: `^[0-9]{8}$` exactamente 8 dígitos
  - `RUC`: `^[0-9]{11}$` exactamente 11 dígitos
  - `CE`/`CEE`: longitud 7-12 caracteres alfanuméricos
- **Fuentes oficiales**:
  - `DNI` → RENIEC (personas naturales)
  - `RUC` → SUNAT (empresas/personas jurídicas)
  - `CE` → Migraciones (carné extranjería)
- **Cache**: No cachea — consulta en vivo a Factiliza cada request
- **Rate Limit**: Factiliza tiene límites — evitar spam (frontend: debounce 500ms)
- **Uso frontend**: Autocompletar formularios registro (Owner, Employee, Customer)
- **Config requerida**: `factiliza.api.url`, `factiliza.api.token` en env

### Ejemplo Flujo Frontend

```javascript
// Al escribir DNI en campo (onBlur o debounce)
async function autocompletarDNI(dni) {
  if (!/^\d{8}$/.test(dni)) return;
  
  try {
    const response = await fetch(`/api/v1/identity-validations/lookup?type=DNI&number=${dni}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (response.ok) {
      const data = await response.json();
      // Rellenar campos: firstName, lastName, etc.
      setFormValues({ firstName: data.firstName, lastName: data.lastName });
    } else if (response.status === 404) {
      showWarning('DNI no encontrado en RENIEC');
    }
  } catch (e) {
    console.error('Error validando DNI', e);
  }
}
```

### Configuración Factiliza

```bash
FACTILIZA_API_URL=https://api.factiliza.com
FACTILIZA_API_TOKEN=xxxxxxxxxxxxxxxx  # Token Factiliza (obligatorio)
```

### Notas de Integración

- **Factiliza**: Agregador oficial Perú (RENIEC + SUNAT + Migraciones)
- **Cuota**: Planes mensuales — monitor `X-RateLimit-Remaining` en headers
- **Errores comunes**:
  - `404`: Documento no existe en registro (no = error técnico)
  - `503`: Token no configurado o Factiliza caído
  - `500`: Error interno Factiliza (reintentar con backoff)