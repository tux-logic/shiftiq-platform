# Guía de Uso e Integración API REST - Servicio Factos

Documentación oficial para la integración del servicio de Facturación Electrónica **`factos`** con sistemas ERP, e-commerce y aplicaciones cliente en entorno local y producción en **Render.com**.

---

## 🌐 Endpoints Principales

| Entorno | Base URL | URL de Swagger UI |
| :--- | :--- | :--- |
| **Producción (Render)** | `https://factos-reva.onrender.com` | `https://factos-reva.onrender.com/swagger-ui.html` |
| **Desarrollo Local** | `http://localhost:8080` | `http://localhost:8080/swagger-ui.html` |

---

## 🔒 1. Autenticación y Seguridad

Todas las peticiones a la API REST (excepto los endpoints de documentación OpenAPI y la creación inicial de API Keys) requieren la cabecera HTTP **`X-API-KEY`**.

```http
X-API-KEY: tu_api_key_aqui
```

---

## 🛠️ 2. Flujo Completo de Integración con Ejemplos

### Paso 1: Generar una API Key de Acceso
Crea una clave de acceso API para tu sistema cliente o ERP.

* **Endpoint:** `POST /api/v1/api-keys`
* **Content-Type:** `application/json`

#### Ejemplo de Entrada (Request Body):
```json
{
  "clientName": "Sistema ERP Producción",
  "validDays": 365
}
```

#### Ejemplo de Salida (Response - HTTP 201 Created):
```json
{
  "keyValue": "aa1e22c0ea1b41adbd1ce571542213bb",
  "clientName": "Sistema ERP Producción",
  "expiresAt": "2027-09-01T02:27:02Z",
  "active": true
}
```

---

### Paso 2: Registrar la Empresa Emisora
Registra los datos de la empresa emisora que emitirá los comprobantes.

* **Endpoint:** `POST /api/v1/issuers`
* **Headers:** `X-API-KEY: aa1e22c0ea1b41adbd1ce571542213bb`

#### Ejemplo de Entrada (Request Body):
```json
{
  "ruc": "20123456789",
  "corporateName": "FACTOS TECH PERU S.A.C.",
  "address": "Av. Javier Prado Este 456, San Isidro, Lima",
  "ubigeo": "150131"
}
```

#### Ejemplo de Salida (Response - HTTP 200 OK):
```json
{
  "ruc": "20123456789",
  "corporateName": "FACTOS TECH PERU S.A.C.",
  "address": "Av. Javier Prado Este 456, San Isidro, Lima",
  "ubigeo": "150131"
}
```

---

### Paso 3: Consultar Catálogos Oficiales SUNAT
Consulta los elementos codificados de los catálogos SUNAT (ej. `CAT-01` Tipos de Comprobante, `CAT-02` Monedas, `CAT-06` Documentos de Identidad, `CAT-07` Afectación al IGV, `CAT-51` Tipos de Operación).

* **Endpoint:** `GET /api/v1/catalogs/CAT-01`
* **Headers:** `X-API-KEY: aa1e22c0ea1b41adbd1ce571542213bb`

#### Ejemplo de Salida (Response - HTTP 200 OK):
```json
[
  { "catalogCode": "CAT-01", "itemCode": "01", "description": "Factura Electrónica", "active": true },
  { "catalogCode": "CAT-01", "itemCode": "03", "description": "Boleta de Venta Electrónica", "active": true },
  { "catalogCode": "CAT-01", "itemCode": "07", "description": "Nota de Crédito Electrónica", "active": true },
  { "catalogCode": "CAT-01", "itemCode": "08", "description": "Nota de Débito Electrónica", "active": true }
]
```

---

### Paso 4: Emitir una Factura Electrónica
Emite una nueva Factura Electrónica. El servicio autocalcula la base imponible, IGV (18%), importe total y almacena automáticamente el PDF impreso en el bucket de **Cloudflare R2**.

* **Endpoint:** `POST /api/v1/comprobantes`
* **Headers:** `X-API-KEY: aa1e22c0ea1b41adbd1ce571542213bb`

#### Ejemplo de Entrada (Request Body):
```json
{
  "series": "F004",
  "correlative": "00000001",
  "cpeType": "01",
  "issueDate": "2026-08-31",
  "issuerRuc": "20123456789",
  "acquirerDocument": "20111222333",
  "acquirerName": "EMPRESA RENDER PRODUCCION S.A.C.",
  "currency": "PEN",
  "items": [
    {
      "code": "SERV-004",
      "description": "Despliegue Exitoso en Render Production",
      "quantity": 1,
      "unitPrice": 5900.00,
      "affectationType": "TAXABLE_ONEROUS"
    }
  ]
}
```

#### Ejemplo de Salida (Response - HTTP 201 Created):
```json
{
  "series": "F004",
  "correlative": "00000001",
  "cpeType": "01",
  "issueDate": "2026-08-31",
  "issuerRuc": "20123456789",
  "acquirerDocument": "20111222333",
  "acquirerName": "EMPRESA RENDER PRODUCCION S.A.C.",
  "status": "EMITTED",
  "totalTaxable": 5000.00,
  "totalIgv": 900.00,
  "totalAmount": 5900.00,
  "currency": "PEN",
  "pdfUrl": "/api/v1/rendering/pdf/F004/00000001",
  "items": [
    {
      "code": "SERV-004",
      "description": "Despliegue Exitoso en Render Production",
      "quantity": 1.0,
      "unitPrice": 5900.00,
      "affectationType": "TAXABLE_ONEROUS"
    }
  ]
}
```

---

### Paso 5: Consultar una Factura por Serie y Correlativo
Obtén los datos de una factura emitida anteriormente.

* **Endpoint:** `GET /api/v1/comprobantes/F004/00000001`
* **Headers:** `X-API-KEY: aa1e22c0ea1b41adbd1ce571542213bb`

#### Ejemplo de Salida (Response - HTTP 200 OK):
```json
{
  "series": "F004",
  "correlative": "00000001",
  "cpeType": "01",
  "issueDate": "2026-08-31",
  "issuerRuc": "20123456789",
  "acquirerDocument": "20111222333",
  "acquirerName": "EMPRESA RENDER PRODUCCION S.A.C.",
  "status": "EMITTED",
  "totalTaxable": 5000.00,
  "totalIgv": 900.00,
  "totalAmount": 5900.00,
  "currency": "PEN",
  "pdfUrl": "/api/v1/rendering/pdf/F004/00000001",
  "items": [
    {
      "code": "SERV-004",
      "description": "Despliegue Exitoso en Render Production",
      "quantity": 1.0,
      "unitPrice": 5900.00,
      "affectationType": "TAXABLE_ONEROUS"
    }
  ]
}
```

---

### Paso 6: Descargar / Visualizar el PDF Impreso de la Factura
Descarga el documento PDF impreso oficial con el Código QR de la SUNAT al pie del documento.

* **Endpoint:** `GET /api/v1/rendering/pdf/F004/00000001`
* **Headers:** `X-API-KEY: aa1e22c0ea1b41adbd1ce571542213bb`

#### Ejemplo de Salida (Response - HTTP 200 OK):
* **Headers de respuesta:**
  * `Content-Type: application/pdf`
  * `Content-Disposition: inline; filename=F004-00000001.pdf`
* **Body:** Archivo binario `.pdf` (Peso aproximado: ~2.95 KB).

---

### Paso 7: Generar Imagen PNG de Código QR SUNAT
Genera una imagen del código QR SUNAT a partir del texto de metadatos.

* **Endpoint:** `GET /api/v1/rendering/qr?content=20123456789|01|F004|00000001|900.00|5900.00|2026-08-31|6|20111222333|MOCK_SIGNATURE`
* **Headers:** `X-API-KEY: aa1e22c0ea1b41adbd1ce571542213bb`

#### Ejemplo of Salida (Response - HTTP 200 OK):
* **Headers de respuesta:** `Content-Type: image/png`
* **Body:** Imagen binaria PNG del código QR.

---

## ☁️ 3. Almacenamiento en Cloudflare R2

Cada comprobante PDF generado es persistido automáticamente en el bucket de **Cloudflare R2**:
* **Bucket:** `factos-bucket`
* **Clave de Archivo:** `facturas/{series}-{correlative}.pdf`

Al solicitar la descarga del PDF mediante `/api/v1/rendering/pdf/{series}/{correlative}`, el servicio verifica primero si el archivo existe en Cloudflare R2 para servirlo directamente desde la nube, evitando trabajo de renderizado innecesario.

---

## 🌐 4. Entorno de Pruebas e Interfaz Swagger UI

La documentación interactiva y consola de pruebas en vivo se encuentra disponible en:
* **Swagger UI Producción:** [https://factos-reva.onrender.com/swagger-ui.html](https://factos-reva.onrender.com/swagger-ui.html)
* **OpenAPI Docs JSON Producción:** [https://factos-reva.onrender.com/v3/api-docs](https://factos-reva.onrender.com/v3/api-docs)
