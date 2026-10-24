# Shared — Media Upload (Cloudinary)

**Base Path**: `/api/v1/media`  
**Context**: Shared Kernel  
**Description**: Subida de imágenes a Cloudinary. Retorna URL segura (HTTPS, optimizada).

---

## `POST /api/v1/media/upload` — Subir Imagen

**Security**: `@PreAuthorize("isAuthenticated()")` — Bearer JWT requerido  
**Description**: Sube archivo a Cloudinary en carpeta organizada. Retorna URL pública HTTPS.

### Request (multipart/form-data)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `file` | file (binary) | Sí | Imagen: JPEG, PNG, WEBP, GIF, SVG, AVIF (máx 10MB) |
| `folder` | string | No | Subcarpeta Cloudinary (default: `uploads`). Ej: `vehicles`, `inspections`, `products`, `avatars`, `workorders` |

### Success Response (201 CREATED)

```json
{
  "url": "https://res.cloudinary.com/shiftiq/image/upload/v1728329482/vehicles/sample-photo.jpg"
}
```

### Error Responses

| HTTP Code | Error Code | Descripción |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Archivo vacío, no es imagen, formato no soportado |
| 401 | `UNAUTHORIZED` | JWT faltante/inválido |
| 413 | `PAYLOAD_TOO_LARGE` | Archivo > 10MB |
| 500 | `UNEXPECTED_ERROR` | Error Cloudinary (credenciales, cuota, red) |

### Important Considerations

- **Formatos**: JPEG, PNG, WEBP, GIF, SVG, AVIF — otros → 400
- **Tamaño máx**: 10MB (config `spring.servlet.multipart.max-file-size`)
- **Optimización**: Cloudinary aplica `f_auto,q_auto` automático (formato óptimo, compresión inteligente)
- **URL devuelta**: HTTPS, CDN global, cacheable
- **Carpetas recomendadas**:
  - `vehicles` — fotos vehículos cliente
  - `inspections` — fotos recepción OT (antes/después)
  - `products` — fotos repuestos
  - `avatars` — fotos perfil usuario
  - `workorders` — evidencias tareas
  - `uploads` — default (genérico)

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