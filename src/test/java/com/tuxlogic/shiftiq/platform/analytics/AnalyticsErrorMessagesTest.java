package com.tuxlogic.shiftiq.platform.analytics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the i18n contract of the analytics bounded context: every message key raised
 * by the analytics code must exist in both message bundles, otherwise the global
 * exception handler falls back to a generic message and logs an unknown key warning.
 */
class AnalyticsErrorMessagesTest {

    private static final List<String> ANALYTICS_KEYS = List.of(
            "analytics.error.retrievalFailed",
            "analytics.error.branchId.required",
            "analytics.error.snapshotId.required",
            "analytics.error.snapshotDate.required",
            "analytics.error.snapshotTimestamps.required",
            "analytics.error.startDate.required",
            "analytics.error.endDate.required",
            "analytics.error.invalidDateRange",
            "analytics.error.dateRangeTooLarge",
            "analytics.error.branchScope.required");

    @Test
    @DisplayName("every analytics error key exists in the default bundle")
    void keysExistInDefaultBundle() {
        var bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);

        assertThat(ANALYTICS_KEYS).allSatisfy(key ->
                assertThat(bundle.containsKey(key))
                        .as("missing key %s in messages.properties", key)
                        .isTrue());
    }

    @Test
    @DisplayName("every analytics error key exists in the spanish bundle")
    void keysExistInSpanishBundle() {
        var bundle = ResourceBundle.getBundle("messages", Locale.of("es"));

        assertThat(ANALYTICS_KEYS).allSatisfy(key ->
                assertThat(bundle.containsKey(key))
                        .as("missing key %s in messages_es.properties", key)
                        .isTrue());
    }

    @Test
    @DisplayName("keys resolve to a non blank message in both locales")
    void keysResolveToNonBlankMessages() {
        for (var locale : List.of(Locale.ENGLISH, Locale.of("es"))) {
            var bundle = ResourceBundle.getBundle("messages", locale);
            for (var key : ANALYTICS_KEYS) {
                assertThat(bundle.getString(key)).as("%s in %s", key, locale).isNotBlank();
            }
        }
    }
}
