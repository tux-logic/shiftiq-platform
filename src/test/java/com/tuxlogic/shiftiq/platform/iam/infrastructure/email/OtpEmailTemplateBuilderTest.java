package com.tuxlogic.shiftiq.platform.iam.infrastructure.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpEmailTemplateBuilderTest {

    @Test
    void buildOtpEmailHtml_ShouldContainOtpCodeAndExpiration() {
        String html = OtpEmailTemplateBuilder.buildOtpEmailHtml("482910", 10);

        assertNotNull(html);
        assertTrue(html.contains("482910"));
        assertTrue(html.contains("10 minutos"));
        assertTrue(html.contains("ShiftIQ Platform"));
        assertTrue(html.contains("<!DOCTYPE html>"));
    }
}
