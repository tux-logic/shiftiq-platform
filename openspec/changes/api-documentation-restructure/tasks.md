# Tasks

## 1. Setup & Structure

- [x] 1.1 Create `docs/api/` directory structure per bounded context and verify structure matches design.md
- [x] 1.2 Create `docs/api/index.md` with navigation table linking all context files
- [x] 1.3 Create endpoint documentation template file for reference (internal use)

## 2. IAM Context Documentation

- [x] 2.1 Document `docs/api/iam/authentication.md` with all auth endpoints:
  - `POST /api/v1/authentication/sessions` (login)
  - `POST /api/v1/authentication/sessions/google` (Google OAuth)
  - `POST /api/v1/authentication/sessions/refresh` (refresh token)
  - `DELETE /api/v1/authentication/sessions` (logout)
  - `POST /api/v1/authentication/password-recoveries` (forgot password)
  - `POST /api/v1/authentication/password-resets` (reset password)
  - `POST /api/v1/authentication/otp/send` (send OTP)
  - `POST /api/v1/authentication/otp/verify` (verify OTP)
  - Verify: each endpoint has request/response examples, auth requirements, error codes

- [x] 2.2 Document `docs/api/iam/users.md` with user management endpoints:
  - `POST /api/v1/users` (sign up)
  - `GET /api/v1/users/{userId}` (get by ID)
  - `GET /api/v1/users?email={email}` (get by email)
  - `PUT /api/v1/users/{userId}/email` (update email)
  - `PUT /api/v1/users/{userId}/password` (update password)
  - Verify: correct @PreAuthorize expressions documented, response types match code

## 3. Core Context Documentation

- [x] 3.1 Document `docs/api/core/workshops.md`:
  - `POST /api/v1/workshops` (create)
  - `PUT /api/v1/workshops/{workshopId}` (update)
  - `GET /api/v1/workshops/{workshopId}` (get by ID)
  - `GET /api/v1/workshops?ownerId={ownerId}` (list by owner)
  - `POST /api/v1/workshops/{workshopId}/specialties` (create specialty)
  - `GET /api/v1/workshops/{workshopId}/specialties` (list specialties)
  - `GET /api/v1/workshops/{workshopId}/specialties/{specialtyId}` (get specialty)
  - `PUT /api/v1/workshops/{workshopId}/specialties/{specialtyId}` (update specialty)
  - `DELETE /api/v1/workshops/{workshopId}/specialties/{specialtyId}` (deactivate specialty)
  - `GET /api/v1/workshops/{workshopId}/specialties/{specialtyId}/history` (specialty history)
  - Verify: workshop-scoped authorization documented correctly

- [x] 3.2 Document `docs/api/core/branches.md`:
  - `POST /api/v1/branches` (create)
  - `PUT /api/v1/branches/{branchId}` (update)
  - `GET /api/v1/branches/{branchId}` (get by ID)
  - `GET /api/v1/branches?workshopId={workshopId}` (list by workshop)
  - `POST /api/v1/branches/{branchId}/subscriptions` (assign subscription)
  - `DELETE /api/v1/branches/{branchId}/subscription` (cancel subscription)
  - Verify: branch-scoped authorization, subscription flow

- [x] 3.3 Document `docs/api/core/owners.md`:
  - `POST /api/v1/owners` (create) - **validateSelfAccess only**
  - `GET /api/v1/owners/{ownerId}` (get by ID) - **validateSelfAccess only**
  - `GET /api/v1/owners?userId={userId}` (get by userId) - **validateSelfAccess only**
  - `PUT /api/v1/owners/{ownerId}` (update) - **validateSelfAccess only**
  - `DELETE /api/v1/owners/{ownerId}` (delete) - **validateSelfAccess only**
  - Verify: **Critical** - document that only ADMIN or self can access (NO ROLE_OWNER bypass)

- [x] 3.4 Document `docs/api/core/employees.md`:
  - `POST /api/v1/employees` (create) - isAuthorizedForUser (ADMIN/OWNER/self)
  - `GET /api/v1/employees/{employeeId}` (get by ID) - isAuthorizedForUser
  - `GET /api/v1/employees?userId={userId}` (get by userId) - isAuthorizedForUser
  - `GET /api/v1/employees?documentNumber={documentNumber}` (get by DNI)
  - `PUT /api/v1/employees/{employeeId}` (update) - **validateSelfAccess only**
  - `DELETE /api/v1/employees/{employeeId}` (delete) - **validateSelfAccess only**
  - Verify: POST/GET allow OWNER bypass, PUT/DELETE require self/ADMIN

- [x] 3.5 Document `docs/api/core/customers.md`:
  - Same pattern as employees - verify authorization rules match

- [x] 3.6 Document `docs/api/core/profiles.md`:
  - `GET /api/v1/profiles/roles?userId={userId}` (get roles)
  - `GET /api/v1/profiles?documentNumber={documentNumber}` (get by DNI)

## 4. Fleet Context Documentation

- [x] 4.1 Document `docs/api/fleet/customer-registrations.md`:
  - `POST /api/v1/customer-registrations` (create)
  - `GET /api/v1/customer-registrations` (list with filters)
  - `GET /api/v1/customer-registrations/{registrationId}` (get by ID)
  - `PUT /api/v1/customer-registrations/{registrationId}` (update status)
  - `DELETE /api/v1/customer-registrations/{registrationId}` (deactivate)

- [x] 4.2 Document `docs/api/fleet/employee-registrations.md`:
  - `POST /api/v1/employee-registrations` (create with hierarchy validation)
  - `POST /api/v1/employee-registrations/request-join` (mechanic request join)
  - `POST /api/v1/employee-registrations/{id}/approve` (approve request)
  - `POST /api/v1/employee-registrations/{id}/reject` (reject request)
  - `GET /api/v1/employee-registrations` (list with filters)
  - `GET /api/v1/employee-registrations/{id}` (get by ID)
  - `PUT /api/v1/employee-registrations/{id}` (update specialty/salary)
  - `DELETE /api/v1/employee-registrations/{id}` (deactivate)
  - **Critical**: Document `canManageStaff` hierarchy matrix per role

- [x] 4.3 Document `docs/api/fleet/appointments.md`:
  - `POST /api/v1/appointments` (create)
  - `GET /api/v1/appointments` (list with filters)
  - `GET /api/v1/appointments/{appointmentId}` (get by ID)
  - `PUT /api/v1/appointments/{appointmentId}` (update)
  - `DELETE /api/v1/appointments/{appointmentId}` (cancel)

## 5. IoT Context Documentation

- [x] 5.1 Document `docs/api/iot/vehicles.md`:
  - `POST /api/v1/vehicles` (register - auto-links to authenticated user)
  - `GET /api/v1/vehicles?branchId={branchId}&status=available-for-linking` (list)
  - `GET /api/v1/vehicles/{id}` (get by ID - complex access validation)
  - `PUT /api/v1/vehicles/{id}` (update)
  - `DELETE /api/v1/vehicles/{id}` (delete)
  - `GET /api/v1/vehicles/{vehicleId}/telemetry-snapshots` (telemetry history)
  - `GET /api/v1/vehicles/{vehicleId}/dtc-alerts` (DTC alerts history)
  - **Note**: `GET /api/v1/customers/{customerId}/vehicles` does NOT exist - do not document

- [x] 5.2 Document `docs/api/iot/obd2-devices.md`:
  - `POST /api/v1/obd2-devices` (register device)
  - `GET /api/v1/obd2-devices?branchId={branchId}&status=available` (list)
  - `GET /api/v1/obd2-devices/{id}` (get by ID)
  - `PUT /api/v1/obd2-devices/{id}` (update)
  - `DELETE /api/v1/obd2-devices/{id}` (soft delete with validation)
  - `GET /api/v1/obd2-devices/{deviceId}/telemetry-snapshots/latest` (live reading)
  - `GET /api/v1/obd2-devices/{deviceId}/telemetry-snapshots` (history)

- [x] 5.3 Document `docs/api/iot/obd2-device-registrations.md`:
  - `POST /api/v1/obd2-device-registrations` (link device to vehicle)
  - `PATCH /api/v1/obd2-device-registrations/{id}` (update status)
  - `GET /api/v1/obd2-device-registrations?branchId={branchId}&status={status}` (list)
  - `GET /api/v1/obd2-device-registrations/{id}/telemetry-snapshots` (telemetry)
  - `GET /api/v1/obd2-device-registrations/{id}/dtc-alerts` (DTC alerts)

- [x] 5.4 Document `docs/api/iot/telemetry-batches.md`:
  - `POST /api/v1/telemetry-batches` (ingest batch with DTC codes)

## 6. Operations Context Documentation

- [x] 6.1 Document `docs/api/operations/services.md`:
  - `POST /api/v1/services` (create)
  - `GET /api/v1/services?branchId={branchId}` (list)
  - `PUT /api/v1/services/{serviceId}` (update)
  - `DELETE /api/v1/services/{serviceId}` (delete)

- [x] 6.2 Document `docs/api/operations/work-orders.md`:
  - `POST /api/v1/work-orders` (create from appointment)
  - `GET /api/v1/work-orders` (list by branchId or vehicleId)
  - `GET /api/v1/work-orders/{id}` (get detail with tasks/products)
  - `PUT /api/v1/work-orders/{id}` (update diagnostic/mileage)
  - `POST /api/v1/work-orders/{id}/complete` (complete - all tasks must be COMPLETED)
  - `DELETE /api/v1/work-orders/{id}` (delete - releases stock reservations)

- [x] 6.3 Document `docs/api/operations/work-order-tasks.md`:
  - `POST /api/v1/work-orders/{id}/tasks` (add task)
  - `PUT /api/v1/work-orders/{id}/tasks/{taskId}` (update task details)
  - `DELETE /api/v1/work-orders/{id}/tasks/{taskId}` (remove task)
  - `POST /api/v1/work-order-tasks/{taskId}/assign-mechanic` (assign mechanic)
  - `POST /api/v1/work-order-tasks/{taskId}/start` (start task)
  - `POST /api/v1/work-order-tasks/{taskId}/complete` (complete task)
  - `POST /api/v1/work-order-tasks/{taskId}/reopen` (reopen task)
  - `POST /api/v1/work-order-tasks/{taskId}/products` (add product - reserves stock)
  - `PUT /api/v1/work-order-tasks/{taskId}/products/{productId}` (update quantity)
  - `DELETE /api/v1/work-order-tasks/{taskId}/products/{productId}` (remove product)
  - **Verify**: These endpoints exist in WorkOrderTasksController

## 7. Inventory Context Documentation

- [x] 7.1 Document `docs/api/inventory/products.md`:
  - `POST /api/v1/inventory/products` (create product)
  - `GET /api/v1/inventory/products?branchId={branchId}` (list with filters)
  - `GET /api/v1/inventory/products/branch/{branchId}` (catalog endpoint)
  - `GET /api/v1/inventory/products/{productId}` (detail with batches)
  - `PUT /api/v1/inventory/products/{productId}` (update)
  - `POST /api/v1/inventory/products/{productId}/batches` (add/adjust stock)
  - `DELETE /api/v1/inventory/products/{productId}` (delete)

## 8. Billing Context Documentation

- [x] 8.1 Document `docs/api/billing/quotes.md`:
  - `POST /api/v1/quotes` (create from work order)
  - `GET /api/v1/quotes/{id}` (get by ID)
  - `GET /api/v1/quotes?branchId={branchId}` (list)
  - `PUT /api/v1/quotes/{id}` (update discount - DRAFT only)
  - `POST /api/v1/quotes/{id}/approvals` (approve)
  - `POST /api/v1/quotes/{id}/cancellations` (cancel)

- [x] 8.2 Document `docs/api/billing/vouchers.md`:
  - `POST /api/v1/vouchers` (emit boleta/factura via Factos)
  - `GET /api/v1/vouchers/{voucherId}` (get with PDF URL)
  - `GET /api/v1/vouchers?branchId={branchId}` (list)
  - `POST /api/v1/vouchers/{voucherId}/payments` (add payment)
  - `DELETE /api/v1/vouchers/{voucherId}/payments/{paymentId}` (remove payment)

- [x] 8.3 Document `docs/api/billing/checkouts.md`:
  - `POST /api/v1/checkouts` (presencial - voucher + full payment)
  - `POST /api/v1/checkouts/mercadopago` (verify MP payment + emit voucher)

- [x] 8.4 Document `docs/api/billing/mercadopago.md`:
  - `POST /api/v1/payments/mercadopago/preferences` (create preference from quote)
  - `POST /api/v1/payments/mercadopago/webhooks` (async webhook - HMAC validated)

## 9. Analytics Context Documentation

- [x] 9.1 Document `docs/api/analytics/analytics.md`:
  - `GET /api/v1/analytics/branches/{branchId}/summary` (daily KPIs)
  - `GET /api/v1/analytics/branches/{branchId}/financial` (historical range)
  - `GET /api/v1/analytics/network/summary` (multi-branch consolidated)
  - Document: UPSERT atomic, delta replay job, timezone handling

## 10. Shared Context Documentation

- [x] 10.1 Document `docs/api/shared/health.md`:
  - `GET /` (root ping)
  - `GET /health` (health check)

- [x] 10.2 Document `docs/api/shared/media.md`:
  - `POST /api/v1/media/upload` (Cloudinary upload)

- [x] 10.3 Document `docs/api/shared/identity-validations.md`:
  - `GET /api/v1/identity-validations/lookup?type={type}&number={number}` (RENIEC/SUNAT/Migraciones)

## 11. Cleanup & Verification

- [x] 11.1 Delete `docs/api-endpoints.md` (monolithic file)
- [x] 11.2 Verify `docs/auth-and-roles-flow.md` has no broken internal links to deleted file
- [x] 11.3 Verify all documented endpoints exist in controller code (grep validation)
- [x] 11.4 Verify no duplicate/conflicting documentation across files
- [x] 11.5 Commit changes following conventional commits (one commit per context or squash)

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied
- Verify the archived result