package com.tuxlogic.shiftiq.platform.iam.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iam.application.internal.queryservices.WorkshopRoleQueryServiceImpl;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleAuditLogRepository;
import com.tuxlogic.shiftiq.platform.iam.domain.repositories.WorkshopRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionsControllerTest {

    @Test
    @DisplayName("GET /api/v1/permissions returns assignable permissions and excludes PLATFORM_ADMIN")
    void listAssignablePermissionsExcludesPlatformAdmin() {
        var queryService = new WorkshopRoleQueryServiceImpl(
                Mockito.mock(WorkshopRoleRepository.class),
                Mockito.mock(WorkshopRoleAuditLogRepository.class)
        );
        var controller = new PermissionsController(queryService);

        var response = controller.listAssignablePermissions();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody())
                .noneMatch(p -> p.code().equals(Permission.PLATFORM_ADMIN.name()))
                .anyMatch(p -> p.code().equals(Permission.INVENTORY_MANAGE.name()));
    }
}
