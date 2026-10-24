# Proposal

## Why

The current API documentation in `docs/api-endpoints.md` is a single massive file (1400+ lines) that contains significant discrepancies with the actual implemented API. My analysis revealed:

- **Missing endpoints**: OTP endpoints, Identity Validations, Workshop Specialties, Profiles, and several Work Order Tasks/Product endpoints
- **Incorrect authorization rules**: Owners/Employees/Customers PUT/DELETE endpoints use `validateSelfAccess` (only ADMIN or self) but docs claim ROLE_OWNER bypass
- **Non-existent endpoints**: `GET /api/v1/customers/{customerId}/vehicles` documented but not implemented
- **Inconsistent structure**: Single flat file makes it hard for frontend developers to find relevant endpoints for their bounded context
- **No request/response examples for many endpoints**: Critical for frontend integration

This restructuring will align documentation with actual code, organize by bounded context (DDD), and provide complete request/response examples with important considerations.

## What Changes

- **RESTRUCTURE**: Split `docs/api-endpoints.md` into per-bounded-context markdown files under `docs/api/`
- **FIX**: Correct all endpoint discrepancies found during code analysis (authorization, missing endpoints, wrong paths)
- **ENHANCE**: Add complete request/response examples for every endpoint with realistic data
- **ADD**: Document important considerations per endpoint (validation rules, business logic, security notes)
- **REMOVE**: Delete the monolithic `api-endpoints.md` and any outdated/duplicate documentation
- **CREATE**: New index/navigation file for easy discovery

## Capabilities

### New Capabilities
- `api-documentation`: Comprehensive, accurate, and developer-friendly API documentation organized by bounded context with complete examples

### Modified Capabilities
- None (this is a documentation-only change, no runtime behavior changes)

## Impact

- **Files affected**: `docs/api-endpoints.md` (to be deleted), new `docs/api/**/*.md` files
- **No code changes**: Pure documentation restructuring
- **Frontend developers**: Primary beneficiaries - will have accurate, navigable docs
- **CI/CD**: No impact
- **Tests**: No impact

**Note**: This is a pure documentation change with no spec-level behavior modifications. The change will set `skip_specs: true` in its `.openspec.yaml`.