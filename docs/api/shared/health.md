# Shared — Health & Root Endpoints

**Base Paths**: `/`, `/health`  
**Context**: Shared Kernel  
**Description**: Endpoints públicos para health checks y conectividad.

---

## `GET /` — Ping de la Raíz

**Security**: Público  
**Description**: Verifica que la aplicación está corriendo y accesible. Usado por load balancers, monitoreo básico.

### Success Response (200 OK)

```json
{
  "status": "UP",
  "app": "ShiftIQ Platform API",
  "docs": "/swagger-ui.html"
}
```

---

## `GET /health` — Health Check

**Security**: Público  
**Description**: Health check estándar para orquestadores (Render, Kubernetes, Docker). Retorna estado básico.

### Success Response (200 OK)

```json
{
  "status": "UP"
}
```

### Important Considerations

- **Sin dependencias**: No verifica BD, Factos, MP, etc. — solo que el proceso Spring Boot está vivo
- **Orquestadores**: Render/K8s usan este endpoint para readiness/liveness probes
- **Extensión**: Para health checks profundos (DB, servicios externos), implementar custom actuator endpoint

---

## Swagger UI

**URL**: `/swagger-ui/index.html` (o `/swagger-ui.html`)  
**Descripción**: Documentación interactiva OpenAPI 3.0 generada desde anotaciones `@Operation`, `@Tag`, `@Parameter` en controllers.

### Acceso

- **Local**: `http://localhost:8080/swagger-ui/index.html`
- **Producción**: `https://shiftiq-platform.onrender.com/swagger-ui/index.html`

### Important Considerations

- **Sincronización**: Swagger se genera del código — siempre actualizado
- **Try It Out**: Requiere JWT válido para endpoints protegidos
- **Auth en Swagger**: Usar botón "Authorize" → pegar `Bearer <token>`