package org.kiwiproject.metrics.health;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.test.assertj.dropwizard.metrics.HealthCheckResultAssertions.assertThat;

import com.codahale.metrics.health.HealthCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("HealthCheckResults")
class HealthCheckResultsTest {

    private static final String SEVERITY_KEY = "severity";
    private static final String SEVERITY_CANNOT_BE_NULL = "severity cannot be null";
    private static final String BUILDER_CANNOT_BE_NULL = "builder cannot be null";
    private static final String ERROR_CANNOT_BE_NULL = "error cannot be null";

    @Nested
    class NewHealthyResult {

        @Nested
        class WithNoArgs {

            @Test
            void shouldHaveDefaultSeverity() {
                assertThat(HealthCheckResults.newHealthyResult())
                        .isHealthy()
                        .hasDetail(SEVERITY_KEY, "OK");
            }
        }

        @Nested
        class WithSeverity {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> HealthCheckResults.newHealthyResult((HealthStatus) null))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                var result = HealthCheckResults.newHealthyResult(HealthStatus.INFO);

                assertThat(result)
                        .isHealthy()
                        .hasDetail(SEVERITY_KEY, "INFO");
            }
        }

        @Nested
        class WithMessage {

            @Test
            void shouldHaveDefaultSeverity() {
                var result = HealthCheckResults.newHealthyResult("things are great!");

                assertThat(result)
                        .isHealthy()
                        .hasDetail(SEVERITY_KEY, "OK")
                        .hasMessage("things are great!");
            }
        }

        @Nested
        class WithSeverityAndMessage {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> HealthCheckResults.newHealthyResult(null, "a message"))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                var result = HealthCheckResults.newHealthyResult(HealthStatus.WARN, "certs expire soon!");

                assertThat(result)
                        .isHealthy()
                        .hasDetail(SEVERITY_KEY, "WARN")
                        .hasMessage("certs expire soon!");
            }
        }

        @Nested
        class WithMessageTemplate {

            @Test
            void shouldHaveDefaultSeverity() {
                var result = HealthCheckResults.newHealthyResult("There are %d foos and %d bars", 42, 24);

                assertThat(result)
                        .isHealthy()
                        .hasDetail(SEVERITY_KEY, "OK")
                        .hasMessage("There are 42 foos and 24 bars");
            }
        }

        @Nested
        class WithSeverityAndMessageTemplate {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newHealthyResult(
                                        (HealthStatus) null, "There are %d foos and %d bars", 42, 24))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                var result = HealthCheckResults.newHealthyResult(HealthStatus.INFO, "The bar is %s", "BAZ");

                assertThat(result)
                        .isHealthy()
                        .hasDetail(SEVERITY_KEY, "INFO")
                        .hasMessage("The bar is BAZ");
            }
        }
    }

    @Nested
    class NewUnhealthyResult {

        @Nested
        class WithMessage {
            @Test
            void shouldHaveDefaultSeverity() {
                var result = HealthCheckResults.newUnhealthyResult("things are no good...");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "WARN")
                        .hasMessage("things are no good...");
            }
        }

        @Nested
        class WithSeverityAndMessage {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult((HealthStatus) null, "a message"))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                var result = HealthCheckResults.newUnhealthyResult(HealthStatus.CRITICAL, "certs expire VERY soon!");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "CRITICAL")
                        .hasMessage("certs expire VERY soon!");
            }
        }

        @Nested
        class WithMessageTemplate {

            @Test
            void shouldHaveDefaultSeverity() {
                var result = HealthCheckResults.newUnhealthyResult("The %s is %s", "thingy", "not working");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "WARN")
                        .hasMessage("The thingy is not working");
            }
        }

        @Nested
        class WithSeverityAndMessageTemplate {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult(
                                        (HealthStatus) null, "There are %d problems", 42))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                assertThat(HealthCheckResults.newUnhealthyResult(HealthStatus
                        .CRITICAL, "%d errors", 42))
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "CRITICAL")
                        .hasMessage("42 errors");
            }
        }

        @Nested
        class WithError {
            @Test
            void shouldNotAllowNullError() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> HealthCheckResults.newUnhealthyResult((Throwable) null))
                        .withMessage(ERROR_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveDefaultSeverity() {
                assertThat(HealthCheckResults.newUnhealthyResult(new
                        RuntimeException("an oop")))
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "CRITICAL")
                        .hasErrorWithMessage("an oop");
            }
        }

        @Nested
        class WithSeverityAndError {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> HealthCheckResults.newUnhealthyResult(null, new RuntimeException()))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldNotAllowNullError() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() -> HealthCheckResults.newUnhealthyResult(HealthStatus.WARN, (Throwable) null))
                        .withMessage(ERROR_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                assertThat(HealthCheckResults.newUnhealthyResult(HealthStatus.WARN, new RuntimeException("a minor oop")))
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "WARN")
                        .hasErrorWithMessage("a minor oop");
            }
        }

        @Nested
        class WithErrorAndMessage {

            @Test
            void shouldNotAllowNullError() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult((Throwable) null, "The thing is messed up"))
                        .withMessage(ERROR_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveDefaultSeverity() {
                var result = HealthCheckResults.newUnhealthyResult(
                        new RuntimeException("an oop"), "The thing is REALLY messed up");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "CRITICAL")
                        .hasMessage("The thing is REALLY messed up")
                        .hasErrorWithMessage("an oop");
            }
        }

        @Nested
        class WithSeverityAndErrorAndMessage {

            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult(
                                        null, new RuntimeException(), "27 errors"))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldNotAllowNullError() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult(HealthStatus.WARN, null, "27errors"))
                        .withMessage(ERROR_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                var result = HealthCheckResults.newUnhealthyResult(
                        HealthStatus.WARN,
                        new RuntimeException("not that bad"),
                        "The thing has a minor problem");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "WARN")
                        .hasMessage("The thing has a minor problem")
                        .hasErrorWithMessage("not that bad");
            }
        }

        @Nested
        class WithErrorAndMessageTemplate {
            @Test
            void shouldNotAllowNullError() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult(
                                        (Throwable) null, "The thing is %s", "messed up"))
                        .withMessage(ERROR_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveDefaultSeverity() {
                var result = HealthCheckResults.newUnhealthyResult(
                        new RuntimeException("an oop"),
                        "The thing is %s",
                        "REALLY messed up");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "CRITICAL")
                        .hasMessage("The thing is REALLY messed up")
                        .hasErrorWithMessage("an oop");
            }
        }

        @Nested
        class WithSeverityAndErrorAndMessageTemplate {
            @Test
            void shouldNotAllowNullSeverity() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult(
                                        (HealthStatus) null, new RuntimeException(), "%d errors", 27))
                        .withMessage(SEVERITY_CANNOT_BE_NULL);
            }

            @Test
            void shouldNotAllowNullError() {
                assertThatIllegalArgumentException()
                        .isThrownBy(() ->
                                HealthCheckResults.newUnhealthyResult(
                                        HealthStatus.WARN, (Throwable) null, "%d errors", 27))
                        .withMessage(ERROR_CANNOT_BE_NULL);
            }

            @Test
            void shouldHaveGivenSeverity() {
                var result = HealthCheckResults.newUnhealthyResult(
                        HealthStatus.WARN,
                        new RuntimeException("not that bad"),
                        "The thing has %s",
                        "a minor problem");

                assertThat(result)
                        .isUnhealthy()
                        .hasDetail(SEVERITY_KEY, "WARN")
                        .hasMessage("The thing has a minor problem")
                        .hasErrorWithMessage("not that bad");
            }
        }
    }

    @Nested
    class NewResultBuilderWithSeverity {

        @Test
        void shouldNotAllowNullSeverity() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newResultBuilder(true, null))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowInvalidCombinations() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newResultBuilder(true, HealthStatus.CRITICAL))
                    .withMessage("Invalid combination (healthy, severity): (true, CRITICAL)");
        }

        @Test
        void shouldAddSeverityToHealthy() {
            var result = HealthCheckResults.newResultBuilder(true, HealthStatus.INFO).build();

            assertThat(result)
                    .isHealthy()
                    .hasDetail(SEVERITY_KEY, "INFO");
        }

        @Test
        void shouldAddSeverityToUnhealthy() {
            var result = HealthCheckResults.newResultBuilder(false, HealthStatus.INFO).build();

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "INFO");
        }
    }

    @Nested
    class NewResultBuilder {

        @Test
        void shouldHave_OK_Severity_ForHealthyArgument() {
            var result = HealthCheckResults.newResultBuilder(true).build();

            assertThat(result)
                    .isHealthy()
                    .hasDetail(SEVERITY_KEY, "OK");
        }

        @Test
        void shouldHave_WARN_Severity_ForHealthyArgument() {
            var result = HealthCheckResults.newResultBuilder(false).build();

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "WARN");
        }
    }

    @Nested
    class NewHealthyResultBuilder {

        @Test
        void shouldHave_OK_Severity() {
            var result = HealthCheckResults.newHealthyResultBuilder().build();

            assertThat(result)
                    .isHealthy()
                    .hasDetail(SEVERITY_KEY, "OK");
        }
    }

    @Nested
    class NewHealthyResultBuilderWithSeverity {

        @Test
        void shouldNotAllowNullSeverity() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newHealthyResultBuilder(null))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowInvalidCombinations() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newHealthyResultBuilder(HealthStatus.CRITICAL))
                    .withMessage("Invalid combination (healthy, severity): (true, CRITICAL)");
        }
    }

    @Nested
    class NewUnhealthyResultBuilder {

        @Test
        void shouldHave_WARN_Severity() {
            var result = HealthCheckResults.newUnhealthyResultBuilder().build();

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "WARN");
        }
    }

    @Nested
    class NewUnhealthyResultBuilderWithSeverity {

        @Test
        void shouldNotAllowNullSeverity() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newUnhealthyResultBuilder((HealthStatus) null))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowInvalidCombinations() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(HealthStatus.OK))
                    .withMessage("Invalid combination (healthy, severity): (false, OK)");
        }

        @Test
        void shouldAddSeverity() {
            var result = HealthCheckResults.newUnhealthyResultBuilder(HealthStatus.CRITICAL).build();

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "CRITICAL");
        }
    }

    @Nested
    class NewUnhealthyResultBuilderWithError {

        @Test
        void shouldNotAllowNullError() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newUnhealthyResultBuilder((Throwable) null))
                    .withMessage(ERROR_CANNOT_BE_NULL);
        }

        @Test
        void shouldHave_CRITICAL_Severity() {
            var result = HealthCheckResults.newUnhealthyResultBuilder(new RuntimeException("oopsy daisy")).build();

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "CRITICAL");
        }
    }

    @Nested
    class NewUnhealthyResultBuilderWithErrorAndMessage {

        @Test
        void shouldNotAllowNullError() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.newUnhealthyResultBuilder(null, "the message"))
                    .withMessage(ERROR_CANNOT_BE_NULL);
        }

        @Test
        void shouldHave_CRITICAL_Severity() {
            var result = HealthCheckResults.newUnhealthyResultBuilder(
                    new RuntimeException("oopsy daisy"), "the message").build();

            assertThat(result)
                    .isUnhealthy()
                    .hasMessage("the message")
                    .hasDetail(SEVERITY_KEY, "CRITICAL");
        }
    }

    @Nested
    class NewUnhealthyResultBuilderWithErrorAndSeverity {

        @Test
        void shouldNotAllowNullError() {

            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(HealthStatus.CRITICAL, null))
                    .withMessage(ERROR_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowNullSeverity() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(null, new RuntimeException("oop")))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowInvalidCombinations() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(HealthStatus.OK, new RuntimeException("oop")))
                    .withMessage("Invalid combination (healthy, severity): (false, OK)");
        }

        @Test
        void shouldAddSeverity() {
            var result = HealthCheckResults.newUnhealthyResultBuilder(
                    HealthStatus.WARN, new RuntimeException("not that bad")).build();

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "WARN");
        }
    }

    @Nested
    class NewUnhealthyResultBuilderWithErrorAndSeverityAndMessage {

        @Test
        void shouldNotAllowNullError() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(HealthStatus.CRITICAL, null, "the message"))
                    .withMessage(ERROR_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowNullSeverity() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(
                                    null, new RuntimeException("oop"), "the message"))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowInvalidCombinations() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() ->
                            HealthCheckResults.newUnhealthyResultBuilder(
                                    HealthStatus.OK, new RuntimeException("oop"), "the message"))
                    .withMessage("Invalid combination (healthy, severity): (false, OK)");
        }

        @Test
        void shouldAddSeverity() {
            var result = HealthCheckResults.newUnhealthyResultBuilder(
                    HealthStatus.WARN, new RuntimeException("not that bad"), "the message")
                    .build();

            assertThat(result)
                    .isUnhealthy()
                    .hasMessage("the message")
                    .hasDetail(SEVERITY_KEY, "WARN");
        }
    }

    @Nested
    class AddSeverity {

        @Test
        void shouldNotAllowNullBuilder() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.addSeverity(HealthStatus.WARN, null))
                    .withMessage(BUILDER_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowNullSeverity() {
            var builder = HealthCheck.Result.builder();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.addSeverity(null, builder))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldAddSeverityToBuilder() {
            var resultBuilder = HealthCheck.Result.builder().healthy();

            var result = HealthCheckResults.addSeverity(
                    HealthStatus.INFO,
                    resultBuilder)
                    .withMessage("things are OK, but might want to check the Foo")
                    .build();

            assertThat(result)
                    .isHealthy()
                    .hasDetail(SEVERITY_KEY, "INFO")
                    .hasMessageStartingWith("things are OK");
        }
    }

    @Nested
    class ResultWithSeverity {

        @Test
        void shouldNotAllowNullBuilder() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.resultWithSeverity(HealthStatus.WARN, null))
                    .withMessage(BUILDER_CANNOT_BE_NULL);
        }

        @Test
        void shouldNotAllowNullSeverity() {
            var builder = HealthCheck.Result.builder();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HealthCheckResults.resultWithSeverity(null, builder))
                    .withMessage(SEVERITY_CANNOT_BE_NULL);
        }

        @Test
        void shouldBuildResultWithSeverity() {
            var builder = HealthCheck.Result.builder().unhealthy();
            var result = HealthCheckResults.resultWithSeverity(HealthStatus.CRITICAL, builder);

            assertThat(result)
                    .isUnhealthy()
                    .hasDetail(SEVERITY_KEY, "CRITICAL");
        }
    }
}
