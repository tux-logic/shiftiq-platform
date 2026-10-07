# 🔐 ShiftIQ Platform — Flujo de Autenticación, Jerarquía de Roles y Gestión de Personal

Guía completa de arquitectura y desarrollo sobre el ciclo de vida de autenticación, el sistema de roles jerárquicos y los flujos de vinculación de personal en **ShiftIQ Platform**.

---

## 📑 Tabla de Contenidos

1. [Visión General de Arquitectura](#-1-visión-general-de-arquitectura)
2. [Catálogo de Roles y Matriz de Jerarquía](#-2-catálogo-de-roles-y-matriz-de-jerarquía)
3. [Flujo de Inicio de Sesión y Manejo de Tokens (IAM)](#-3-flujo-de-inicio-de-sesión-y-manejo-de-tokens-iam)
   - [Inicio de Sesión Clásico (Email & Password)](#31-inicio-de-sesión-clásico)
   - [Inicio de Sesión con Google (OAuth2)](#32-inicio-de-sesión-con-google)
   - [Renovación de Sesión (Refresh Token)](#33-renovación-de-sesión-refresh-token)
   - [Cierre de Sesión (Revocación)](#34-cierre-de-sesión-revocación)
4. [Flujo Jerárquico de Contratación y Onboarding de Personal](#-4-flujo-jerárquico-de-contratación-y-onboarding-de-personal)
   - [Principio Rector y Filosofía del Modelo Jerárquico](#41-principio-rector-y-filosofía-del-modelo-jerárquico)
   - [Guía Operativa Paso a Paso: Agregado por Jerarquía en Taller Multi-Sede](#42-guía-operativa-paso-a-paso-agregado-por-jerarquía-en-taller-multi-sede)
   - [Guía Operativa en Taller Mono-Sede](#43-guía-operativa-en-taller-mono-sede-1-sola-sede)
   - [Diagrama de Secuencia Técnico de Validación](#44-diagrama-de-secuencia-técnico-de-validación)
   - [Matriz de Infracciones de Jerarquía y Respuestas HTTP](#45-matriz-de-infracciones-de-jerarquía-y-respuestas-http)
   - [Flujo de Postulación Autónoma (Request-Join & Aprobación)](#46-flujo-de-postulación-autónoma-request-join--aprobación)
5. [Aislamiento Multi-Tenant y Seguridad por Sede](#-5-aislamiento-multi-tenant-y-seguridad-por-sede)
   - [Scoping de Cuentas (`AccountBranchScopeResolver`)](#51-scoping-de-cuentas-accountbranchscoperesolver)
   - [Protección Contra Degradación de Privilegios](#52-protección-contra-degradación-de-privilegios)
   - [Sincronización Dinámica al Dar de Baja Personal](#53-sincronización-dinámica-al-dar-de-baja-personal)
6. [Integración con el Catálogo Dinámico de Especialidades](#-6-integración-con-el-catálogo-dinámico-de-especialidades)
7. [Buenas Prácticas para Clientes Frontend (Web y Mobile)](#-7-buenas-prácticas-para-clientes-frontend-web-y-mobile)

---

## 🏛️ 1. Visión General de Arquitectura

El sistema de seguridad de ShiftIQ implementa un modelo **Role-Based Access Control (RBAC)** combinado con **Tenancy Scoping** (aislamiento por Taller y Sedes):

* **Contexto IAM (`com.tuxlogic.shiftiq.platform.iam`)**: Administra credenciales, emite JWTs y resguarda los atributos de autenticación de cada cuenta (`role` y `branchIds`).
* **Contexto Core (`com.tuxlogic.shiftiq.platform.core`)**: Administra los talleres (`Workshop`), sedes (`Branch`), perfiles de personal (`Employee`) y el catálogo configurable de especialidades (`WorkshopSpecialty`).
* **Contexto Fleet (`com.tuxlogic.shiftiq.platform.fleet`)**: Gestiona los contratos y vinculaciones operativas de personal en sedes (`EmployeeRegistration`), orquestando la asignación real de roles a través de puertos de salida ACL (`ExternalIamService`).
* **Capa Compartida (`com.tuxlogic.shiftiq.platform.shared.infrastructure.security`)**: [`MultiTenancySecurityService`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/shared/infrastructure/security/MultiTenancySecurityService.java) intercepta y valida que cada operación respete la jerarquía de mando y el aislamiento territorial de sedes.

```
       ┌────────────────────────┐
       │   Cliente (Web / App)   │
       └───────────┬────────────┘
                   │ 1. POST /api/v1/authentication/sessions
                   ▼
       ┌────────────────────────┐       2. Emite JWT con claims
       │      Contexto IAM      │───────►  (id, role, branchIds)
       └───────────┬────────────┘
                   │
                   │ 3. Peticiones con Bearer <token>
                   ▼
┌───────────────────────────────────────────────┐
│         MultiTenancySecurityService           │
│  - ¿El llamador tiene la sede asignada?       │
│  - ¿Su rango jerárquico permite dar el alta?  │
└──────────────────────┬────────────────────────┘
                       │ Petición Autorizada
                       ▼
┌───────────────────────────────────────────────┐
│          Fleet / Core Controllers             │
│  - Valida especialidad en catálogo de taller   │
│  - Crea EmployeeRegistration                  │
│  - Actualiza IAM: branches & role sin degradar │
└───────────────────────────────────────────────┘
```

---

## 👑 2. Catálogo de Roles y Matriz de Jerarquía

Cada cuenta de usuario posee un rol único principal definido en el enum [`Roles`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/iam/domain/model/valueobjects/Roles.java):

| Rol | Rango (`hierarchyRank`) | Descripción y Responsabilidad |
| :--- | :---: | :--- |
| **`ROLE_ADMIN`** | **4** | Administrador global de la plataforma ShiftIQ. Acceso irrestricto sin filtrado de sedes. |
| **`ROLE_OWNER`** | **3** | Dueño o representante legal del taller/empresa. Gestiona talleres, sedes y personal superior. |
| **`ROLE_BRANCH_MANAGER`** | **2** | Gerente de Sede. Responsable administrativo y operativo asignado a una o más sedes específicas. |
| **`ROLE_ASSISTANT`** | **1** | Asistente de Sede / Recepcionista. Gestiona citas, recepción y alta de técnicos en su sede. |
| **`ROLE_EMPLOYEE`** | **0** | Personal técnico/operativo (Mecánico, Electricista, Planchador, Diagnóstico, etc.). |
| **`ROLE_USER`** | **0** | Cliente particular o usuario base registrado en la app móvil antes de ser contratado. |

### Matriz de Autorización para Gestión de Personal (`canManageStaff`)

La política definida en [`MultiTenancySecurityService.canManageStaff`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/shared/infrastructure/security/MultiTenancySecurityService.java#L256-L303) aplica las siguientes reglas estrictas:

| Rol del Llamador | Rol que Puede Dar de Alta | Restricción Territorial | Condición de Negocio |
| :--- | :--- | :--- | :--- |
| **`ROLE_ADMIN`** | Cualquier rol (`BRANCH_MANAGER`, `ASSISTANT`, `EMPLOYEE`) | Cualquiera | Modo soporte/administración global. |
| **`ROLE_OWNER`** | **`ROLE_BRANCH_MANAGER`** | Sedes de su taller | **Taller Multi-Sede** (debe delegar en gerentes). |
| **`ROLE_OWNER`** | **`ROLE_ASSISTANT`** o **`ROLE_EMPLOYEE`** | Sedes de su taller | **Taller Mono-Sede** (el dueño asume el rol de gerente). |
| **`ROLE_BRANCH_MANAGER`** | **`ROLE_ASSISTANT`** o **`ROLE_EMPLOYEE`** | Solo en sus sedes asignadas | Debe pertenecer a la sede indicada. |
| **`ROLE_ASSISTANT`** | **`ROLE_EMPLOYEE`** exclusivamente | Solo en sus sedes asignadas | Alta de mecánicos, electricistas, etc. |
| **`ROLE_EMPLOYEE`** | *Ninguno* | N/A | No tiene privilegios para dar de alta personal. |

---

## 🔑 3. Flujo de Inicio de Sesión y Manejo de Tokens (IAM)

### 3.1. Inicio de Sesión Clásico

Permite autenticar a un usuario mediante sus credenciales (correo electrónico y contraseña).

* **Endpoint:** `POST /api/v1/authentication/sessions`
* **Acceso:** Público
* **Request Body:**
```json
{
  "email": "gerente.norte@taller-rapido.com",
  "password": "Password123!"
}
```
* **Respuesta Exitosa (`200 OK`):**
```json
{
  "id": "7f8b8941-8f55-46f3-9d11-536cf29d1c7a",
  "email": "gerente.norte@taller-rapido.com",
  "role": "ROLE_BRANCH_MANAGER",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey...",
  "refreshToken": "84d56f61-b4d2-43cf-a0e2-66b97621c0ea",
  "accessTokenExpiresInSeconds": 900
}
```

> **Contenido del Access Token (JWT):**
> El JWT emitido incluye en sus claims el identificador del usuario (`sub`), su rol activo (`role`) y el conjunto de identificadores de sedes asignadas (`branchIds`). Esto permite a la plataforma validar accesos de forma stateless a nivel de red sin consultar la base de datos en cada petición.

---

### 3.2. Inicio de Sesión con Google

Permite autenticación federada mediante OAuth2 ID Tokens emitidos por Google Identity Services. Si el usuario no existe en la base de datos, se auto-registra con el rol por defecto `ROLE_USER`.

* **Endpoint:** `POST /api/v1/authentication/sessions/google`
* **Acceso:** Público
* **Request Body:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMy... (Google JWT)"
}
```
* **Respuesta Exitosa (`200 OK`):** Retorna `AuthenticatedUserResource`.

---

### 3.3. Renovación de Sesión (Refresh Token)

Cuando el token de acceso expira (`401 Unauthorized`), el cliente frontend renueva las credenciales automáticamente usando el `refreshToken` sin obligar al usuario a escribir su clave nuevamente.

* **Endpoint:** `POST /api/v1/authentication/sessions/refresh`
* **Acceso:** Público
* **Request Body:**
```json
{
  "refreshToken": "84d56f61-b4d2-43cf-a0e2-66b97621c0ea"
}
```
* **Respuesta Exitosa (`200 OK`):** Retorna un nuevo par `token` y `refreshToken`.

> [!IMPORTANT]
> **Actualización de Roles en Sesión Activa:**
> Debido a que las autoridades (`role` y `branchIds`) se firman dentro del JWT en el momento del login, **cuando un empleado recibe un nuevo rol o una nueva sede en el backend**, deberá refrescar su sesión o iniciar sesión de nuevo para que el frontend reciba el token actualizado con los nuevos permisos.

---

### 3.4. Cierre de Sesión (Revocación)

Invalida la sesión activa y elimina el refresh token de la base de datos para impedir renovaciones posteriores.

* **Endpoint:** `DELETE /api/v1/authentication/sessions`
* **Request Body:**
```json
{
  "refreshToken": "84d56f61-b4d2-43cf-a0e2-66b97621c0ea"
}
```
* **Respuesta Exitosa (`204 NO CONTENT`)**

---

## 👥 4. Flujo Jerárquico de Contratación y Onboarding de Personal

### 4.1. Principio Rector y Filosofía del Modelo Jerárquico

En los talleres automotrices modernos, la administración del personal sigue una **cadena de mando delegada**:

1. **La cuenta padre (`ROLE_OWNER`)** no debe microgestionar decenas de técnicos distribuidos en múltiples sedes. Su responsabilidad es designar al responsable de cada sede: el **Gerente de Sede (`ROLE_BRANCH_MANAGER`)**.
2. **El Gerente de Sede (`ROLE_BRANCH_MANAGER`)** toma posesión administrativa de su sede y designa a su brazo operativo: el **Asistente de Sede (`ROLE_ASSISTANT`)**.
3. **El Asistente de Sede (`ROLE_ASSISTANT`)** es la persona en el piso de recepción y taller que gestiona el día a día; por ello, es quien se encarga de dar de alta al personal técnico y operativo (**Mecánicos**, **Electricistas**, **Planchadores**, **Técnicos de Diagnóstico**, etc. con `ROLE_EMPLOYEE`).
4. **En talleres con una sola sede (Mono-Sede)**, no existe una estructura gerencial compleja: el propio **Dueño (`ROLE_OWNER`)** asume el papel de gestor directo de la sede y puede dar de alta directamente a su Asistente (`ROLE_ASSISTANT`) o a los técnicos (`ROLE_EMPLOYEE`).
5. **Los empleados técnicos (`ROLE_EMPLOYEE`)** están estrictamente bloqueados de dar de alta o administrar a otros miembros del personal.

```mermaid
graph TD
    subgraph MultiSede ["Escenario Multi-Sede (> 1 sede en el consorcio)"]
        O1["👑 Dueño del Taller (ROLE_OWNER)<br/>(Cuenta Padre)"]
        GM1["👔 Gerente de Sede Norte (ROLE_BRANCH_MANAGER)"]
        GM2["👔 Gerente de Sede Sur (ROLE_BRANCH_MANAGER)"]
        AS1["📋 Asistente / Recepción Sede Norte (ROLE_ASSISTANT)"]
        AS2["📋 Asistente / Recepción Sede Sur (ROLE_ASSISTANT)"]
        T1["🔧 Mecánicos, Electricistas, Planchadores<br/>(ROLE_EMPLOYEE)"]
        T2["🔧 Mecánicos, Electricistas, Planchadores<br/>(ROLE_EMPLOYEE)"]

        O1 -->|1. Agrega| GM1
        O1 -->|1. Agrega| GM2
        GM1 -->|2. Agrega| AS1
        GM2 -->|2. Agrega| AS2
        AS1 -->|3. Agrega| T1
        AS2 -->|3. Agrega| T2
    end

    subgraph MonoSede ["Escenario Mono-Sede (1 sola sede física)"]
        O2["👑 Dueño del Taller (ROLE_OWNER)<br/>(Asume rol de Gerente)"]
        AS_M["📋 Asistente (ROLE_ASSISTANT)"]
        T_M["🔧 Mecánicos y Técnicos (ROLE_EMPLOYEE)"]

        O2 -->|Agrega directamente| AS_M
        O2 -.->|O agrega directamente| T_M
        AS_M -->|Agrega operativamente| T_M
    end
```

---

### 4.2. Guía Operativa Paso a Paso: Agregado por Jerarquía en Taller Multi-Sede

A continuación se muestra el ciclo completo de peticiones REST ejecutadas para dar de alta a toda la cadena de mando.

#### 📌 Pre-requisito Común: Cuenta de Usuario y Perfil de Empleado
Antes de que una persona pueda ser vinculada a una sede, debe contar con su cuenta base en IAM y su perfil en el Core:
1. El usuario se registra en la plataforma: `POST /api/v1/authentication/sign-up` (obtiene un `userId` con rol `ROLE_USER`).
2. Se crea su ficha de datos personales: `POST /api/v1/employees` con el `userId`, DNI, nombre y teléfono (obtiene un `employeeId`).

---

#### 🥇 Paso 1: La Cuenta Padre (Dueño) Agrega al Gerente de Sede
* **Quién ejecuta:** Dueño del Taller (`ROLE_OWNER`).
* **Cabecera HTTP:** `Authorization: Bearer <token_del_dueño>`.
* **Endpoint:** `POST /api/v1/employee-registrations`
* **Request Body:**
```json
{
  "employeeId": "77777777-1111-2222-3333-444444444444",
  "branchId": "b1111111-0000-0000-0000-000000000001",
  "speciality": "GENERAL_MECHANIC",
  "specialityName": "Gerencia General de Sede Norte",
  "salary": 4500.00,
  "role": "ROLE_BRANCH_MANAGER"
}
```
* **Efecto en el sistema:**
  1. Se valida que el llamador sea el dueño del taller al que pertenece `branchId`.
  2. Se valida que el rol objetivo sea `ROLE_BRANCH_MANAGER`.
  3. Se crea el registro `EmployeeRegistration` en estado `ACTIVE`.
  4. En IAM, la cuenta de usuario se actualiza:
     - `user.role` = `ROLE_BRANCH_MANAGER`.
     - `user.branchIds` = `["b1111111-0000-0000-0000-000000000001"]`.

---

#### 🥈 Paso 2: El Gerente de Sede Agrega a su Asistente
* **Quién ejecuta:** El Gerente de Sede (`ROLE_BRANCH_MANAGER`).
* **Credenciales:** El gerente inicia sesión en `POST /api/v1/authentication/sessions` y utiliza su nuevo token emitido.
* **Cabecera HTTP:** `Authorization: Bearer <token_del_gerente>`.
* **Endpoint:** `POST /api/v1/employee-registrations`
* **Request Body:**
```json
{
  "employeeId": "88888888-2222-3333-4444-555555555555",
  "branchId": "b1111111-0000-0000-0000-000000000001",
  "speciality": "GENERAL_MECHANIC",
  "specialityName": "Asistente Administrativa y Recepción",
  "salary": 2200.00,
  "role": "ROLE_ASSISTANT"
}
```
* **Efecto en el sistema:**
  1. [`MultiTenancySecurityService`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/shared/infrastructure/security/MultiTenancySecurityService.java) comprueba que el token pertenezca a la sede `b1111111-0000-0000-0000-000000000001`.
  2. Comprueba que un `ROLE_BRANCH_MANAGER` tiene autorización para asignar `ROLE_ASSISTANT`.
  3. En IAM, la cuenta del asistente recibe:
     - `user.role` = `ROLE_ASSISTANT`.
     - `user.branchIds` = `["b1111111-0000-0000-0000-000000000001"]`.

---

#### 🥉 Paso 3: El Asistente de Sede Agrega al Personal Técnico
* **Quién ejecuta:** El Asistente de Sede (`ROLE_ASSISTANT`).
* **Credenciales:** El asistente inicia sesión y usa su token `Bearer <token_del_asistente>`.
* **Endpoint:** `POST /api/v1/employee-registrations`
* **Request Body para Mecánico General:**
```json
{
  "employeeId": "99999999-3333-4444-5555-666666666666",
  "branchId": "b1111111-0000-0000-0000-000000000001",
  "speciality": "GENERAL_MECHANIC",
  "specialityName": "Mecánico Automotriz de Motores",
  "salary": 2800.00,
  "role": "ROLE_EMPLOYEE"
}
```
* **Request Body para Electricista Automotriz:**
```json
{
  "employeeId": "aaaaaaaa-4444-5555-6666-777777777777",
  "branchId": "b1111111-0000-0000-0000-000000000001",
  "speciality": "ELECTRICIAN",
  "specialityName": "Técnico Electricista y Diagnóstico de Sensores",
  "salary": 3000.00,
  "role": "ROLE_EMPLOYEE"
}
```
* **Request Body para Planchado y Pintura:**
```json
{
  "employeeId": "bbbbbbbb-5555-6666-7777-888888888888",
  "branchId": "b1111111-0000-0000-0000-000000000001",
  "speciality": "BODYWORK_PAINT",
  "specialityName": "Maestro Planchador y Pintor al Horno",
  "salary": 3100.00,
  "role": "ROLE_EMPLOYEE"
}
```
* **Efecto en el sistema:**
  1. Se verifica que `GENERAL_MECHANIC`, `ELECTRICIAN` y `BODYWORK_PAINT` pertenezcan al catálogo activo del taller.
  2. Se verifica que el llamador (`ROLE_ASSISTANT`) solo esté intentando dar de alta `ROLE_EMPLOYEE`.
  3. Las cuentas IAM de los técnicos quedan configuradas con `ROLE_EMPLOYEE` y vinculadas exclusivamente a la sede Norte.

---

### 4.3. Guía Operativa en Taller Mono-Sede (1 sola sede)

Cuando el taller cuenta con una sola sede (`countBranchesForWorkshop <= 1`):
* El Dueño (`ROLE_OWNER`) tiene potestad directa sobre la sede y no está obligado a crear un Gerente.
* El Dueño puede invocar directamente `POST /api/v1/employee-registrations` con:
  * `role: "ROLE_ASSISTANT"` para incorporar a su asistente.
  * O `role: "ROLE_EMPLOYEE"` para incorporar directamente a los mecánicos.
* Una vez registrado el asistente, este asume la tarea de seguir agregando más personal técnico.

---

### 4.4. Diagrama de Secuencia Técnico de Validación

```mermaid
sequenceDiagram
    autonumber
    actor Jefe as Autoridad (Dueño / Gerente / Asistente)
    participant API as EmployeeRegistrationsController
    participant Sec as MultiTenancySecurityService
    participant Fleet as EmployeeRegistrationCommandService
    participant Core as ExternalCoreService
    participant IAM as ExternalIamService

    Jefe->>API: POST /api/v1/employee-registrations { employeeId, branchId, speciality, salary, role }
    API->>Sec: validateStaffManagement(role, branchId)
    alt Llamador no tiene acceso a la sede o rol no permitido en jerarquía
        Sec-->>API: throws AccessDeniedException
        API-->>Jefe: 403 FORBIDDEN
    else Jerarquía y sede válidas
        Sec-->>API: Autorizado OK
    end

    API->>Fleet: handle(CreateEmployeeRegistrationCommand)
    Fleet->>Core: existsBranchById(branchId) & existsEmployeeById(employeeId)
    Fleet->>Core: findWorkshopIdForBranch(branchId)
    Fleet->>Core: existsActiveWorkshopSpecialty(workshopId, speciality)
    alt Especialidad no existe o está inactiva en el taller
        Core-->>Fleet: false
        Fleet-->>API: Result.failure(SPECIALTY_NOT_IN_CATALOG)
        API-->>Jefe: 400 BAD REQUEST ("fleet.error.employeeRegistration.specialtyNotInCatalog")
    else Especialidad activa OK
        Core-->>Fleet: true
    end

    Fleet->>Fleet: repository.save(registration)
    Fleet->>Core: findUserIdByEmployeeId(employeeId)
    Fleet->>IAM: setBranches(userId, activeBranches)
    Fleet->>IAM: assignRole(userId, role)
    Note over IAM: Comprueba no degradar privilegios (canBeReplacedBy)<br/>y actualiza la cuenta User en base de datos.
    IAM-->>Fleet: OK

    Fleet-->>API: Result.success(registration)
    API-->>Jefe: 201 CREATED (EmployeeRegistrationResource)
```

---

### 4.5. Matriz de Infracciones de Jerarquía y Respuestas HTTP

El sistema rechaza de forma estricta cualquier intento de saltarse la cadena de mando o quebrantar la seguridad multi-tenant:

| Escenario de Intento No Permitido | Emisor | Rol Solicitado | Código HTTP | Razón / Mensaje |
| :--- | :--- | :--- | :---: | :--- |
| Asistente intenta dar de alta a un Gerente | `ROLE_ASSISTANT` | `ROLE_BRANCH_MANAGER` | **`403 FORBIDDEN`** | Un asistente solo puede dar de alta personal técnico (`ROLE_EMPLOYEE`). |
| Asistente intenta dar de alta a otro Asistente | `ROLE_ASSISTANT` | `ROLE_ASSISTANT` | **`403 FORBIDDEN`** | Solo el Gerente (o Dueño en mono-sede) puede nombrar asistentes. |
| Técnico intenta dar de alta a un compañero | `ROLE_EMPLOYEE` | `ROLE_EMPLOYEE` | **`403 FORBIDDEN`** | Los mecánicos/técnicos no tienen permisos de gestión de personal. |
| Gerente intenta dar de alta personal en una sede ajena | `ROLE_BRANCH_MANAGER` | `ROLE_EMPLOYEE` | **`403 FORBIDDEN`** | El gerente no pertenece ni tiene asignada esa sede (`!isAuthorizedForBranch`). |
| Dueño en taller multi-sede intenta dar de alta un técnico sin delegar | `ROLE_OWNER` | `ROLE_EMPLOYEE` | **`403 FORBIDDEN`** | En multi-sede se exige delegar: el dueño debe nombrar al `ROLE_BRANCH_MANAGER`. |
| Solicitud con rol reservado de plataforma | Cualquier usuario | `ROLE_ADMIN` u `ROLE_OWNER` | **`400 BAD REQUEST`** | El campo `role` solo acepta roles de personal de sede. |
| Especialidad no existe en el catálogo del taller | Cualquier autoridad | Especialidad no registrada | **`400 BAD REQUEST`** | `fleet.error.employeeRegistration.specialtyNotInCatalog`. |

---

### 4.6. Flujo de Postulación Autónoma (Request-Join & Aprobación)

Este flujo se utiliza cuando un mecánico registrado en la aplicación móvil solicita unirse por iniciativa propia a una sede:

```
1. Mecánico (ROLE_EMPLOYEE)
   POST /api/v1/employee-registrations/request-join
   { branchId, employeeId, speciality, specialityName, salary }
               │
               ▼
   Estado: PENDING_APPROVAL (No se otorgan permisos ni acceso aún)
               │
2. Encargado de Sede (ROLE_BRANCH_MANAGER / ROLE_OWNER)
   POST /api/v1/employee-registrations/{id}/approve
               │
               ▼
   Estado: ACTIVE
   - La cuenta de usuario recibe la sede en user.branchIds
   - Se le asigna formalmente el rol ROLE_EMPLOYEE en IAM
```

---

## 🛡️ 5. Aislamiento Multi-Tenant y Seguridad por Sede

### 5.1. Scoping de Cuentas (`AccountBranchScopeResolver`)

Para evitar que un Gerente de Sede o un Asistente consulte o manipule perfiles de empleados o clientes de otros talleres o sedes no autorizadas:

1. El componente [`AccountBranchScopeResolverImpl`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/iam/infrastructure/security/AccountBranchScopeResolverImpl.java) recupera las sedes del usuario objetivo (`targetUser.getBranchIds()`).
2. [`MultiTenancySecurityService.sharesBranchWith`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/shared/infrastructure/security/MultiTenancySecurityService.java#L117-L129) evalúa la intersección entre las sedes del llamador y las sedes del usuario consultado:
   $$\text{callerBranches} \cap \text{targetBranches} \neq \emptyset$$
3. Si no comparten al menos una sede, la plataforma arroja **`403 FORBIDDEN`**.

---

### 5.2. Protección Contra Degradación de Privilegios

La lógica de negocio implementada en [`Roles.canBeReplacedBy`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/iam/domain/model/valueobjects/Roles.java#L33-L35) e [`UserCommandServiceImpl.handle(AssignRoleToUserCommand)`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/iam/application/internal/commandservices/UserCommandServiceImpl.java#L163-L180) protege la integridad de las cuentas:

* Si un usuario ya es **`ROLE_BRANCH_MANAGER`** (rango 2) y posteriormente se le registra en otra sede con el rol por defecto **`ROLE_EMPLOYEE`** (rango 0):
  * **El rol NO se degrada**: La cuenta conserva `ROLE_BRANCH_MANAGER`.
  * **La nueva sede SÍ se agrega**: La nueva sede se incorpora a su conjunto de sedes autorizadas.
  * Se registra una traza de auditoría informativa (`WARN`) en el log del servidor.

---

### 5.3. Sincronización Dinámica al Dar de Baja Personal

Cuando se da de baja una vinculación de personal mediante `DELETE /api/v1/employee-registrations/{id}`:

1. El registro en Fleet pasa a estado `INACTIVE`.
2. El servicio consulta todas las vinculaciones activas restantes para ese empleado mediante `findActiveBranchIdsByEmployeeId(employeeId)`.
3. Se invoca [`ExternalIamService.setBranches`](file:///home/aldo/Proyectos/University/Mobile-Applications/shiftiq-platform/src/main/java/com/tuxlogic/shiftiq/platform/fleet/application/outboundservices/ExternalIamService.java#L23) para actualizar `User.replaceBranches()`.
4. Si el usuario ya no cuenta con ningún contrato activo en ninguna sede, su conjunto de sedes queda vacío (`[]`), perdiendo de inmediato el acceso a los datos operativos del taller.

---

## 🔧 6. Integración con el Catálogo Dinámico de Especialidades

Cada taller gestiona un catálogo propio y dinámico de especialidades técnicas que alimenta el alta de mecánicos:

* **Siembra Inicial Automática:** Al registrar un taller (`POST /api/v1/workshops`), se siembran de forma automática e idempotente 5 especialidades base:
  1. `GENERAL_MECHANIC` — *Mecánica General*
  2. `ELECTRICIAN` — *Electricidad y Electrónica*
  3. `BODYWORK_PAINT` — *Planchado y Pintura*
  4. `DIAGNOSTIC` — *Diagnóstico Computarizado*
  5. `TIRE_ALIGNMENT` — *Alineación y Suspensión*
* **Gestión por Taller:** El dueño y los gerentes de sede pueden consultar, crear, modificar o desactivar especialidades mediante los endpoints `/api/v1/workshops/{workshopId}/specialties`.
* **Validación en Onboarding:** Cualquier intento de registrar a un empleado o enviar un `request-join` con una especialidad que no esté activa en el catálogo del taller del que depende la sede es rechazado con:
  * **Código HTTP:** `400 Bad Request`
  * **Clave de Error:** `fleet.error.employeeRegistration.specialtyNotInCatalog`

---

## 📱 7. Buenas Prácticas para Clientes Frontend (Web y Mobile)

1. **Almacenamiento Seguro de Tokens:**
   * En Web: almacenar el `refreshToken` en una cookie `HttpOnly` o en memoria segura del cliente.
   * En Mobile (Flutter/React Native): almacenar el `token` y `refreshToken` en almacenamiento seguro (`flutter_secure_storage` / Keychain / Keystore).
2. **Interceptor HTTP (Axios / Dio):**
   * Configurar un interceptor que capture respuestas con código `401 Unauthorized`.
   * Encolar las peticiones fallidas, invocar `POST /api/v1/authentication/sessions/refresh` con el `refreshToken`, y reintentar las peticiones originales con el nuevo `token`.
3. **Refresco tras Asignación de Roles:**
   * Si una pantalla administrativa cambia el rol o las sedes de un usuario que tiene sesión iniciada, instruir a la aplicación para renovar la sesión vía `/sessions/refresh` para sincronizar los nuevos claims de inmediato.
4. **Validación Previa en Formularios:**
   * Al registrar un empleado, consultar previamente `GET /api/v1/workshops/{workshopId}/specialties?activeOnly=true` para poblar el dropdown de especialidades con opciones válidas.
   * Limitar las opciones del selector de rol según el rol del usuario conectado (por ejemplo, ocultar la opción `ROLE_BRANCH_MANAGER` si el usuario en sesión es un `ROLE_ASSISTANT`).
