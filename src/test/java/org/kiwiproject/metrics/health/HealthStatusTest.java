package org.kiwiproject.metrics.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.base.KiwiStrings;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@DisplayName("HealthStatus")
class HealthStatusTest {

    @Nested
    class HealthStatusFrom {

        @Nested
        class EdgeCases {

            @Test
            void
            shouldIgnore_MapThatContainsNonMapValue_WhenOtherValidMapsExist() {
                Map<String, Object> healthDetails = Map.of(
                        "database", Map.of("healthy", true),
                        "badThing", "this is NOT a map and will be ignored"
                );
                assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.OK);
            }

            @Test
            void shouldBe_OK_WhenGiven_MapWithStringHealthyValue() {
                Map<String, Object> healthDetails = Map.of(
                        "database", Map.of("healthy", "TRUE", "message", "Connection refused")
                );

                assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.OK);
            }

            @Test
            void shouldBe_WARN_WhenGiven_MapWithHealthyValue_WhoseToStringThrowsAnException() {
                Map<String, Object> healthDetails = Map.of(
                        "database", Map.of("healthy", new EvilToString())
                );

                assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
            }

            @ParameterizedTest
            @EnumSource(HealthStatus.class)
            void shouldAcceptHealthSeverityObject(HealthStatus status) {
                var healthy = switch (status) {
                    case OK, INFO -> true;
                    case WARN, CRITICAL, FATAL -> false;
                };

                Map<String, Object> healthDetails = Map.of(
                        "database", Map.of("healthy", healthy, "severity", status)
                );

                assertThat(HealthStatus.from(healthDetails)).isEqualTo(status);
            }
        }
    }

    private static class EvilToString {
        @Override
        public String toString() {
            throw new RuntimeException("I am evil");
        }
    }

    @Nested
    class InvalidArgument {

        @Test
        void shouldBe_CRITICAL_WhenGiven_ObjectThatIsNotMap() {
            Map<String, Object> healthDetails = Map.of(
                    "database", "this is definitely NOT a map"
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.CRITICAL);
        }

        @Test
        void shouldBe_CRITICAL_WhenGiven_NullMap() {
            assertThat(HealthStatus.from((Map<String, Object>) null)).isEqualTo(HealthStatus.CRITICAL);
        }

        @Test
        void shouldBe_CRITICAL_WhenGiven_EmptyMap() {
            assertThat(HealthStatus.from(Map.of())).isEqualTo(HealthStatus.CRITICAL);
        }

        @Test
        void shouldBe_WARN_WhenGiven_MapWithNoHealthyKey() {
            Map<String, Object> healthDetails = Map.of(
                    "database", Map.of("message", "Connection refused")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_WARN_WhenGiven_MapWithObjectHealthyValue() {
            Map<String, Object> healthDetails = Map.of(
                    "database", Map.of("healthy", new Object(), "message", "Connection refused")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_WARN_WhenGiven_AnInvalidSeverityLevel() {
            Map<String, Object> healthDetails = Map.of(
                    "database", Map.of("healthy", false, "severity", "FOO")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_WARN_WhenGiven_MapWithSeverityLevel() {
            Map<String, Object> healthDetails = Map.of(
                    "database", Map.of(
                            "healthy", false,
                            "severity", Map.of("level", "WARN")
                    )
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }
    }

    @Nested
    class WhenSingleEntryMap {

        @Test
        void shouldBe_OK_WhenHealthy() {
            Map<String, Object> healthDetails = Map.of(
                    "database", Map.of("healthy", true)
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.OK);
        }

        @Test
        void shouldBe_INFO_WhenHealthy_AndHas_INFOSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "something", Map.of("healthy", true, "severity", "INFO"));

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.INFO);
        }

        @Test
        void shouldBe_WARN_WhenNotHealthy_AndHasNoSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "database", Map.of("healthy", false, "message", "Connection refused")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_WARN_WhenNotHealthy_AndHasWARNSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "aThing", Map.of("healthy", false, "severity", "WARN")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_CRITICAL_WhenNotHealthy_AndHasCRITICALSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "aThing", Map.of("healthy", false, "severity", "CRITICAL"));

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.CRITICAL);
        }
    }

    @Nested
    class WhenMultiEntryMap {

        @Test
        void shouldBe_OK_WhenAllHealthy() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", true),
                    "database", Map.of("healthy", true),
                    "rottenTomato", Map.of("healthy", true, "message", "No errors in last 15 minutes")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.OK);
        }

        @Test
        void shouldBe_INFO_WhenAllHealthy_WithINFOSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", true),
                    "database", Map.of("healthy", true),
                    "rottenTomato", Map.of("healthy", true, "message", "No errors in last 15 minutes"),
                    "somethingElse", Map.of("healthy", true, "severity", "INFO")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.INFO);
        }

        @Test
        void shouldBe_WARN_WhenOneNotHealthy_AndNoSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", true),
                    "database", Map.of("healthy", false, "message", "Connection refused"),
                    "rottenTomato", Map.of("healthy", true, "message", "No errors in last 15 minutes")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_WARN_WhenOneNotHealthy_AndHasWARNSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", true),
                    "database", Map.of("healthy", true),
                    "rottenTomato", Map.of("healthy", true, "message", "No errors in last 15 minutes"),
                    "notThatImportant", Map.of("healthy", false, "severity", "WARN")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_CRITICAL_WhenOneNotHealthy_AndHasCRITICALSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", true),
                    "database", Map.of("healthy", true),
                    "rottenTomato", Map.of("healthy", true, "message", "No errors in last 15 minutes"),
                    "extremelyImportant", Map.of("healthy", false, "severity", "CRITICAL"));

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.CRITICAL);
        }

        @Test
        void shouldBe_WARN_WhenAllNotHealthy_ButNoneHaveSeverity() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", false),
                    "database", Map.of("healthy", false, "message", "Connection refused"),
                    "rottenTomato", Map.of("healthy", false, "message", "27 errors in last 15 minutes")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_WARN_WhenAllNotHealthy_WithHighestSeverityOfWARN() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", false),
                    "database", Map.of("healthy", false, "message", "Connection refused"),
                    "rottenTomato", Map.of("healthy", false, "message", "27 errors in last 15 minutes"),
                    "somewhatImportant", Map.of("healthy", false, "severity", "WARN"));

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.WARN);
        }

        @Test
        void shouldBe_CRITICAL_WhenAllNotHealthy_WithHighSeverityOfCRITICAL() {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", false),
                    "database", Map.of("healthy", false, "message", "Connection refused"),
                    "rottenTomato", Map.of("healthy", false, "message", "27 errors in last 15 minutes"),
                    "somewhatImportant", Map.of("healthy", false, "severity", "CRITICAL")
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(HealthStatus.CRITICAL);
        }

        @ParameterizedTest
        @CsvSource({
                "true,CRITICAL,CRITICAL",
                "true,FATAL,FATAL",
                "false,OK,WARN",
        })
        void shouldReturnExpectedHealthStatusFrom_WhenInvalidHealthyAndSeverityCombination(boolean healthy,
                                                                                           String severity,
                                                                                           HealthStatus expectedStatus) {
            Map<String, Object> healthDetails = Map.of(
                    "deadlocks", Map.of("healthy", true),
                    "something", Map.of("healthy", healthy, "severity", severity)
            );

            assertThat(HealthStatus.from(healthDetails)).isEqualTo(expectedStatus);
        }
    }

    @Nested
    class DefaultSeverityForValue {

        @Test
        void shouldBe_OK_ForHealthy() {
            assertThat(HealthStatus.defaultSeverityForValue(true)).isEqualTo(HealthStatus.OK);
        }

        @Test
        void shouldBe_WARN_ForUnhealthy() {
            assertThat(HealthStatus.defaultSeverityForValue(false)).isEqualTo(HealthStatus.WARN);
        }
    }

    @Nested
    class IsValidCombination {

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldAllowNullSeverity(boolean healthy) {
            assertThat(HealthStatus.isValidCombination(healthy, null)).isTrue();
            assertThat(HealthStatus.isInvalidCombination(healthy, null)).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
                "true, OK",
                "true, INFO",
                "true, WARN",
                "false, INFO",
                "false, WARN",
                "false, CRITICAL",
                "false, FATAL",
        })
        void shouldBeTrue_WhenValid(boolean healthy, HealthStatus severity) {
            assertThat(HealthStatus.isValidCombination(healthy, severity)).isTrue();
            assertThat(HealthStatus.isInvalidCombination(healthy, severity)).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
                "false, OK",
                "true, CRITICAL",
                "true, FATAL"})
        void shouldBeFalse_WhenNotValid(boolean healthy, HealthStatus severity) {
            assertThat(HealthStatus.isInvalidCombination(healthy, severity)).isTrue();
            assertThat(HealthStatus.isValidCombination(healthy, severity)).isFalse();
        }
    }

    @Nested
    class HighestSeverity {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowException_WhenGivenNullStatusList(List<HealthStatus> statuses) {
            assertThatThrownBy(() -> HealthStatus.highestSeverity(statuses))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @CsvSource({
                " 'OK, OK, OK', OK",
                " 'OK, INFO, OK', INFO",
                " 'OK, OK, WARN, INFO', WARN",
                " 'OK, OK, WARN, INFO, OK, CRITICAL, OK', CRITICAL",
                " 'OK, FATAL, WARN, INFO, OK, CRITICAL, OK', FATAL"
        })
        void shouldReturn_HighestSeverity_FromCollection(String statusCsv, HealthStatus expectedHighest) {
            var statuses = Arrays.stream(statusCsv.split(","))
                    .map(String::trim)
                    .map(HealthStatus::valueOf)
                    .toList();

            assertThat(HealthStatus.highestSeverity(statuses)).isEqualTo(expectedHighest);
        }
    }

    @Nested
    class Max {
        @ParameterizedTest
        @CsvSource({
                " '' , '' ",
                " '', OK",
                " OK , '' "
        })
        void shouldNotAllowNullArguments(String arg1, String arg2) {
            var status1 = Optional.ofNullable(KiwiStrings.blankToNull(arg1)).map(HealthStatus::valueOf).orElse(null);
            var status2 = Optional.ofNullable(KiwiStrings.blankToNull(arg2)).map(HealthStatus::valueOf).orElse(null);

            assertThatThrownBy(() -> HealthStatus.max(status1, status2))
                    .isExactlyInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @CsvSource({
                "OK,OK,OK",
                "OK,INFO,INFO",
                "INFO,INFO,INFO",
                "INFO,WARN,WARN",
                "WARN,WARN,WARN",
                "WARN,CRITICAL,CRITICAL",
                "CRITICAL,CRITICAL,CRITICAL",
                "CRITICAL,FATAL,FATAL"
        })
        void shouldReturnHigherStatus(HealthStatus status1, HealthStatus status2, HealthStatus expectedMax) {
            assertThat(HealthStatus.max(status1, status2)).isEqualTo(expectedMax);
            assertThat(HealthStatus.max(status2, status1)).isEqualTo(expectedMax);
        }
    }

    @Nested
    class HealthStatusComparators {

        @ParameterizedTest(name = "[{index}] {0} ; expecting min={2}, max={3}")
        @MethodSource("org.kiwiproject.metrics.health.HealthStatusTest#comparators")
        void shouldCompareBySeverity(String description,
                                     Comparator<HealthStatus> comparator,
                                     HealthStatus expectedMin,
                                     HealthStatus expectedMax) {

            var values = newArrayList(HealthStatus.values());
            Collections.shuffle(values);

            var max = values.stream().max(comparator).orElseThrow();
            var min = values.stream().min(comparator).orElseThrow();

            assertThat(min)
                    .describedAs("%s expected min=%s", description, expectedMin)
                    .isEqualTo(expectedMin);

            assertThat(max)
                    .describedAs("%s expected max=%s", description, expectedMax)
                    .isEqualTo(expectedMax);
        }

        @ParameterizedTest(name = "[{index}] statuses: {0} {1} {2} ; expected min={3}, max={4}")
        @CsvSource({
                "OK, OK, OK, OK, OK",
                "INFO, INFO, INFO, INFO, INFO",
                "WARN, WARN, WARN, WARN, WARN",
                "CRITICAL, CRITICAL, CRITICAL, CRITICAL, CRITICAL",
                "FATAL, FATAL, FATAL, FATAL, FATAL",
                "OK, INFO, FATAL, OK, FATAL",
                "OK, CRITICAL, WARN, OK, CRITICAL",
                "OK, OK, CRITICAL, OK, CRITICAL",
                "OK, WARN, OK, OK, WARN",
                "FATAL, OK, OK, OK, FATAL",
                "WARN, OK, OK, OK, WARN",
                "WARN, INFO, INFO, INFO, WARN"
        })
        void shouldCompareHealthStatusInStreamPipelines(HealthStatus status1,
                                                        HealthStatus status2,
                                                        HealthStatus status3,
                                                        HealthStatus expectedMin,
                                                        HealthStatus expectedMax) {
            var errors = newArrayList(
                    new WorkflowError(status1 + " status", status1),
                    new WorkflowError(status2 + " status", status2),
                    new WorkflowError(status3 + " status", status3)
            );
            Collections.shuffle(errors);

            var min = errors.stream()
                    .map(WorkflowError::healthStatus)
                    .min(HealthStatus.comparingSeverity())
                    .orElseThrow();

            assertThat(min).isEqualTo(expectedMin);

            var max = errors.stream()
                    .map(WorkflowError::healthStatus)
                    .max(HealthStatus.comparingSeverity())
                    .orElseThrow();

            assertThat(max).isEqualTo(expectedMax);
        }
    }

    static List<Arguments> comparators() {
        return List.of(
                Arguments.of("Comparator.naturalOrder()",
                        Comparator.naturalOrder(), HealthStatus.OK, HealthStatus.FATAL),

                Arguments.of("new HealthStatusComparator()",
                        new HealthStatusComparator(), HealthStatus.OK, HealthStatus.FATAL),

                Arguments.of("HealthStatus.comparingSeverity()",
                        HealthStatus.comparingSeverity(), HealthStatus.OK, HealthStatus.FATAL),

                Arguments.of("HealthStatus.comparingSeverity().reversed()",
                        HealthStatus.comparingSeverity().reversed(), HealthStatus.FATAL, HealthStatus.OK)
        );
    }

    record WorkflowError(String message, HealthStatus healthStatus) {
    }
}
