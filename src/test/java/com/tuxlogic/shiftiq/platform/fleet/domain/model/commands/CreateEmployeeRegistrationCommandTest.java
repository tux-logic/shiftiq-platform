package com.tuxlogic.shiftiq.platform.fleet.domain.model.commands;

import com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.EmployeeId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateEmployeeRegistrationCommandTest {

    private final EmployeeId employeeId = new EmployeeId(UUID.randomUUID());
    private final BranchId branchId = new BranchId(UUID.randomUUID());

    @Test
    @DisplayName("role defaults to ROLE_EMPLOYEE when the request omits it")
    void roleDefaultsToEmployee() {
        var command = new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "GENERAL_MECHANIC", "Mecánica General", new BigDecimal("1000"), null);

        assertThat(command.role()).isEqualTo("ROLE_EMPLOYEE");

        var withoutRole = new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "GENERAL_MECHANIC", "Mecánica General", new BigDecimal("1000"));

        assertThat(withoutRole.role()).isEqualTo("ROLE_EMPLOYEE");
    }

    @Test
    @DisplayName("role accepts the staff allow-list with or without the ROLE_ prefix")
    void roleAcceptsStaffAllowList() {
        assertThat(new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "ELECTRICIAN", "Electricidad", new BigDecimal("1000"), "assistant").role())
                .isEqualTo("ROLE_ASSISTANT");
        assertThat(new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "ELECTRICIAN", "Electricidad", new BigDecimal("1000"), "ROLE_BRANCH_MANAGER").role())
                .isEqualTo("ROLE_BRANCH_MANAGER");
        assertThat(new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "ELECTRICIAN", "Electricidad", new BigDecimal("1000"), "employee").role())
                .isEqualTo("ROLE_EMPLOYEE");
    }

    @Test
    @DisplayName("privileged roles outside the staff allow-list are rejected")
    void privilegedRolesAreRejected() {
        assertThatThrownBy(() -> new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "ELECTRICIAN", "Electricidad", new BigDecimal("1000"), "ROLE_ADMIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fleet.error.resource.role.invalid");

        assertThatThrownBy(() -> new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "ELECTRICIAN", "Electricidad", new BigDecimal("1000"), "ROLE_OWNER"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new CreateEmployeeRegistrationCommand(
                employeeId, branchId, "ELECTRICIAN", "Electricidad", new BigDecimal("1000"), "ROLE_HACKER"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
