package com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects;

/**
 * Atomic, backend-owned permission catalog for hybrid workshop RBAC/PBAC.
 */
public enum Permission {
    WORKORDER_READ("View work orders and diagnostics", true),
    WORKORDER_EXECUTE("Create, update, and execute work order tasks", true),
    WORKORDER_DELETE("Delete work orders", true),

    INVENTORY_READ("View products and stock batches", true),
    INVENTORY_MANAGE("Create, update, delete products and manage stock batches", true),

    SERVICE_MANAGE("Create, update, and delete workshop services", true),

    QUOTE_READ("View quotes", true),
    QUOTE_CREATE("Create and update draft quotes", true),
    QUOTE_APPROVE("Approve quotes", true),
    QUOTE_CANCEL("Cancel quotes", true),

    VOUCHER_READ("View billing vouchers", true),
    CHECKOUT_PROCESS("Generate vouchers, process checkouts, and record payments", true),

    APPOINTMENT_MANAGE("Create, update, and cancel branch appointments", true),

    STAFF_READ("View branch employee registrations", true),
    STAFF_MANAGE("Create, update, approve, reject, and deactivate staff registrations", true),

    SPECIALTY_MANAGE("Create, update, and deactivate workshop specialties", true),

    BRANCH_READ("View branch details", true),
    BRANCH_MANAGE("Create and update branches and manage branch subscriptions", true),

    MEDIA_UPLOAD("Upload media assets for workshop operations", true),

    ROLE_MANAGE("Create, update, deactivate, and assign custom workshop roles", true),

    PLATFORM_ADMIN("Platform-wide administrative privileges (not assignable by workshops)", false);

    private final String description;
    private final boolean assignableByWorkshop;

    Permission(String description, boolean assignableByWorkshop) {
        this.description = description;
        this.assignableByWorkshop = assignableByWorkshop;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAssignableByWorkshop() {
        return assignableByWorkshop;
    }
}
