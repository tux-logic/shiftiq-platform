package com.tuxlogic.shiftiq.platform.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class UnauthorizedRequestHandlerEntryPointTest {

    private final UnauthorizedRequestHandlerEntryPoint entryPoint = new UnauthorizedRequestHandlerEntryPoint();

    @Test
    @DisplayName("commence sets status 401 UNAUTHORIZED")
    void commenceSetsUnauthorized() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getErrorMessage()).isEqualTo("Unauthorized request detected");
    }
}
