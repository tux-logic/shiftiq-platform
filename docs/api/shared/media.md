# Shared — Media Upload (Cloudinary)

**Base Path**: `/api/v1/media`  
**Context**: Shared Kernel  
**Description**: Subida de imágenes a Cloudinary. Retorna URL segura (HTTPS, optimizada).

---

## `POST /api/v1/media/upload` — Subir Imagen

**Security**: `@PreAuthorize("isAuthenticated()")` — Bearer JWT requerido. Carpetas operativas requieren rol de staff (`ROLE_EMPLOYEE`, `ROLE_BRANCH_MANAGER`, `ROLE_OWNER`, `ROLE_ADMIN`).  
**Description**: Sube archivo a Cloudinary en carpeta autorizada. Retorna URL pública HTTPS.

### Request (multipart/form-data)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `file` | file (binary) | Sí | Imagen: JPEG, PNG, WEBP, GIF, SVG, AVIF (máx 10MB) |
| `folder` | string | No | Subcarpeta Cloudinary (default: `uploads`). Permitidas: `customers`, `employees`, `products`, `vehicles`, `workorders`, `uploads` |

### Success Response (201 CREATED)

```json
{
  "url": "https://res.cloudinary.com/shiftiq/image/upload/v1728329482/vehicles/sample-photo.jpg"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Archivo vacío, no es imagen, o carpeta destino no permitida |
| 401 | `UNAUTHORIZED` | JWT faltante/inválido |
| 403 | `FORBIDDEN` | Usuario no autorizado para subir a carpetas operativas de taller |
| 413 | `PAYLOAD_TOO_LARGE` | Archivo > 10MB |
| 500 | `UNEXPECTED_ERROR` | Error Cloudinary (credenciales, cuota, red) |

### Important Considerations

- **Formatos**: JPEG, PNG, WEBP, GIF, SVG, AVIF — otros → 400
- **Tamaño máx**: 10MB (config `spring.servlet.multipart.max-file-size`)
- **Optimización**: Cloudinary aplica `f_auto,q_auto` automático (formato óptimo, compresión inteligente)
- **URL devuelta**: HTTPS, CDN global, cacheable
- **Carpetas permitidas y control de acceso**:
  - `customers`: Avatares y fotos de perfil (disponible para todos los usuarios autenticados)
  - `uploads`: Subidas genéricas temporales (disponible para todos los usuarios autenticados)
  - `vehicles`: Fotos de vehículos (requiere rol de staff)
  - `products`: Catálogo de repuestos (requiere rol de staff)
  - `workorders`: Evidencias de órdenes de trabajo (requiere rol de staff)
  - `employees`: Fotos de credenciales o perfil técnico (requiere rol de staff)

### Ejemplo Frontend (JavaScript)

```javascript
async function uploadImage(file, folder = 'uploads') {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);
  
  const response = await fetch('/api/v1/media/upload', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    },
    body: formData
  });
  
  if (!response.ok) throw new Error('Upload failed');
  const { url } = await response.json();
  return url;
}
```

### Uso en Otros Contextos

| Contexto | Endpoint Relacionado | Uso |
|----------|---------------------|-----|
| Fleet | `POST /vehicles` | `photoUrl` en request |
| Operations | `POST /work-orders` | `entryInspectionImages[]` |
| Operations | `POST /work-orders/{id}/tasks` | `evidenceImages[]` |
| Inventory | `POST /products` | (no directo — usar Media + set URL manual) |
| Billing | — | Comprobantes generan PDF propio (Factos) |