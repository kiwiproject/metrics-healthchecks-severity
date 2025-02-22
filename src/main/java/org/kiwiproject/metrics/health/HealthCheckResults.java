package org.kiwiproject.metrics.health;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.metrics.health.HealthStatus.isValidCombination;

import com.codahale.metrics.health.HealthCheck;
import lombok.experimental.UtilityClass;

/**
 * Utilities to create {@link HealthCheck.Result} or {@link HealthCheck.ResultBuilder} instances with a
 * {@link HealthStatus} to indicate the severity. Methods that do not have a severity argument will provide
 * a default value.
 *
 * @apiNote All methods throw IllegalArgumentException if given null arguments or invalid combination
 * of (healthy, severity).
 */
@UtilityClass
public class HealthCheckResults {

    /**
     * The name of the health check detail that will be used for the severity.
     */
    public static final String SEVERITY_DETAIL = "severity";

    /**
     * Create a new Result with no message and default severity.
     *
     * @return a healthy result
     */
    public static HealthCheck.Result newHealthyResult() {
        return newHealthyResultBuilder().build();
    }

    /**
     * Create a new Result with no message and the given severity.
     *
     * @param severity the severity to use
     * @return a healthy result
     */
    public static HealthCheck.Result newHealthyResult(HealthStatus severity) {
        return newHealthyResultBuilder(severity).build();
    }

    /**
     * Create a new Result with the given message and default severity.
     *
     * @param message the message to use
     * @return a healthy result
     */
    public static HealthCheck.Result newHealthyResult(String message) {
        return newHealthyResultBuilder()
                .withMessage(message)
                .build();
    }

    /**
     * Create a new Result with the given severity and message.
     *
     * @param severity the severity to use
     * @param message  the message to use
     * @return a healthy result
     */
    public static HealthCheck.Result newHealthyResult(HealthStatus severity, String message) {
        return newHealthyResultBuilder(severity)
                .withMessage(message)
                .build();
    }

    /**
     * Create a new Result with the given interpolated message and default severity.
     *
     * @param messageTemplate the message template to use
     * @param args            the message arguments
     * @return a healthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newHealthyResult(String messageTemplate, Object... args) {
        return newHealthyResultBuilder()
                .withMessage(messageTemplate, args)
                .build();
    }

    /**
     * Create a new Result with the given severity, interpolated message and default severity.
     *
     * @param severity        the severity to use
     * @param messageTemplate the message template to use
     * @param args            the message arguments
     * @return a healthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newHealthyResult(HealthStatus severity, String messageTemplate, Object... args) {
        return newHealthyResultBuilder(severity)
                .withMessage(messageTemplate, args)
                .build();
    }

    /**
     * Create a new Result with the given message and default severity.
     *
     * @param message the message to use
     * @return an unhealthy result
     */
    public static HealthCheck.Result newUnhealthyResult(String message) {
        return newUnhealthyResultBuilder()
                .withMessage(message)
                .build();
    }

    /**
     * Create a new Result with the given severity and message.
     *
     * @param severity the severity to use
     * @param message  the message to use
     * @return an unhealthy result
     */
    public static HealthCheck.Result newUnhealthyResult(HealthStatus severity, String message) {
        return newUnhealthyResultBuilder(severity)
                .withMessage(message)
                .build();
    }

    /**
     * Create a new Result with the given interpolated message and default severity.
     *
     * @param messageTemplate the message template to use
     * @param args            the message arguments
     * @return an unhealthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newUnhealthyResult(String messageTemplate, Object... args) {
        return newUnhealthyResultBuilder()
                .withMessage(messageTemplate, args)
                .build();
    }

    /**
     * Create a new Result with the given severity, interpolated message and default severity.
     *
     * @param severity        the severity to use
     * @param messageTemplate the message template to use
     * @param args            the message arguments
     * @return an unhealthy result
     * @implNote Uses {Kink HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newUnhealthyResult(HealthStatus severity, String messageTemplate, Object... args) {
        return newUnhealthyResultBuilder(severity)
                .withMessage(messageTemplate, args)
                .build();
    }

    /**
     * Create a new Result with the given error and default severity.
     *
     * @param error the Throwable to use
     * @return an unhealthy result
     */
    public static HealthCheck.Result newUnhealthyResult(Throwable error) {
        return newUnhealthyResultBuilder(error).build();
    }

    /**
     * Create a new Result with the given severity and error.
     *
     * @param severity the severity to use
     * @param error    the Throwable to use
     * @return an unhealthy result
     */
    public static HealthCheck.Result newUnhealthyResult(HealthStatus severity, Throwable error) {
        return newUnhealthyResultBuilder(severity, error).build();
    }

    /**
     * Create a new Result with the given error and message with default severity.
     *
     * @param error   the Throwable to use
     * @param message the message to use
     * @return an unhealthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newUnhealthyResult(Throwable error, String message) {
        return newUnhealthyResultBuilder(error)
                .withMessage(message)
                .build();
    }

    /**
     * Create a new Result with the given severity, error, and message.
     *
     * @param severity the severity to use
     * @param error    the Throwable to use
     * @param message  the message to use
     * @return an unhealthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newUnhealthyResult(HealthStatus severity, Throwable error, String message) {
        return newUnhealthyResultBuilder(severity, error)
                .withMessage(message)
                .build();
    }

    /**
     * Create a new Result with the given error and interpolated message.
     *
     * @param error           the Throwable to use
     * @param messageTemplate the message template
     * @param args            the message arguments
     * @return an unhealthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String, Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newUnhealthyResult(Throwable error, String messageTemplate, Object... args) {
        return newUnhealthyResultBuilder(error)
                .withMessage(messageTemplate, args)
                .build();
    }

    /**
     * Create a new Result with the given severity, error, and interpolated message.
     *
     * @param severity        the severity to use
     * @param error           the Throwable to use
     * @param messageTemplate the message template
     * @param args            the message arguments
     * @return an unhealthy result
     * @implNote Uses {@link HealthCheck.ResultBuilder#withMessage(String,Object...)} under the covers, so uses its
     * message formatting
     * @see HealthCheck.ResultBuilder#withMessage(String)
     */
    public static HealthCheck.Result newUnhealthyResult(HealthStatus severity,
                                                        Throwable error,
                                                        String messageTemplate,
                                                        Object... args) {
        return newUnhealthyResultBuilder(severity, error)
                .withMessage(messageTemplate, args)
                .build();
    }

    /**
     * Create a ResultBuilder with given severity.
     *
     * @param healthy  is it healthy?
     * @param severity the severity to use
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newResultBuilder(boolean healthy, HealthStatus severity) {
        checkSeverity(severity);
        checkValidCombination(healthy, severity);
        var resultBuilder = newEmptyResultBuilder(healthy);
        return addSeverity(severity, resultBuilder);
    }

    private static HealthCheck.ResultBuilder newEmptyResultBuilder(boolean healthy) {
        if (healthy) {
            return HealthCheck.Result.builder().healthy();
        }
        return HealthCheck.Result.builder().unhealthy();
    }

    /**
     * Create a ResultBuilder with OK (if healthy) or WARN (if not healthy) severity.
     *
     * @param healthy is it healthy?
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newResultBuilder(boolean healthy) {
        if (healthy) {
            return newHealthyResultBuilder();
        }
        return newUnhealthyResultBuilder();
    }

    /**
     * Create a healthy ResultBuilder with OK severity.
     *
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newHealthyResultBuilder() {
        return newHealthyResultBuilder(HealthStatus.OK);
    }

    /**
     * Create a healthy ResultBuilder with given severity.
     *
     * @param severity the severity to use
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newHealthyResultBuilder(HealthStatus severity) {
        checkSeverity(severity);
        checkValidCombination(true, severity);

        var resultBuilder = HealthCheck.Result.builder().healthy();
        return addSeverity(severity, resultBuilder);
    }

    /**
     * Create an unhealthy ResultBuilder with WARN severity.
     *
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newUnhealthyResultBuilder() {
        return newUnhealthyResultBuilder(HealthStatus.WARN);
    }

    /**
     * Create an unhealthy ResultBuilder with given severity.
     *
     * @param severity the severity to use
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newUnhealthyResultBuilder(HealthStatus severity) {
        checkSeverity(severity);
        checkValidCombination(false, severity);

        var resultBuilder = HealthCheck.Result.builder().unhealthy();
        return addSeverity(severity, resultBuilder);
    }

    /**
     * Create an unhealthy ResultBuilder with CRITICAL severity.
     *
     * @param error the exception causing unhealthiness
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newUnhealthyResultBuilder(Throwable error) {
        checkError(error);

        var resultBuilder = HealthCheck.Result.builder().unhealthy(error);
        return addSeverity(HealthStatus.CRITICAL, resultBuilder);
    }

    /**
     * Create an unhealthy ResultBuilder with CRITICAL severity and the given message.
     *
     * @param error   the exception causing unhealthiness
     * @param message the message
     * @return a new result builder
     * @implNote HealthCheck.Result.unhealthy(Throwable) sets the message to the exception's message. This overrides
     * with the given message, and call order matters such that {@code withMessage} must be called after
     * {@code unhealthy(error)}
     */
    public static HealthCheck.ResultBuilder newUnhealthyResultBuilder(Throwable error, String message) {
        checkError(error);

        var resultBuilder = HealthCheck.Result.builder().unhealthy(error).withMessage(message);
        return addSeverity(HealthStatus.CRITICAL, resultBuilder);
    }

    /**
     * Create an unhealthy ResultBuilder with given severity.
     *
     * @param severity the severity to use
     * @param error    the error to set
     * @return a new result builder
     */
    public static HealthCheck.ResultBuilder newUnhealthyResultBuilder(HealthStatus severity, Throwable error) {
        checkError(error);
        checkSeverity(severity);
        checkValidCombination(false, severity);

        var resultBuilder = HealthCheck.Result.builder().unhealthy(error);
        return addSeverity(severity, resultBuilder);
    }

    /**
     * Create an unhealthy ResultBuilder with given severity and message.
     *
     * @param severity the severity to use
     * @param error    the error to set
     * @param message  the message
     * @return a new result builder
     * @implNote HealthCheck.Result.unhealthy(Throwable) sets the message to the exception's message. This overrides
     * with the given message, and call order matters such that {@code withMessage} must be called after
     * {@code unhealthy(error)}
     */
    public static HealthCheck.ResultBuilder newUnhealthyResultBuilder(HealthStatus severity,
                                                                      Throwable error,
                                                                      String message) {
        checkError(error);
        checkSeverity(severity);
        checkValidCombination(false, severity);

        var resultBuilder = HealthCheck.Result.builder().unhealthy(error).withMessage(message);
        return addSeverity(severity, resultBuilder);
    }

    private static void checkValidCombination(boolean healthy, HealthStatus severity) {
        checkArgument(isValidCombination(healthy, severity),
                "Invalid combination (healthy, severity): (%s, %s)", healthy, severity);
    }

    private static void checkError(Throwable error) {
        checkArgumentNotNull(error, "error cannot be null");
    }

    /**
     * Adds the given severity to the builder.
     *
     * @param severity the severity to use
     * @param builder  the builder to mutate
     * @return the builder for additional configuration
     */
    public static HealthCheck.ResultBuilder addSeverity(HealthStatus severity, HealthCheck.ResultBuilder builder) {
        checkBuilder(builder);
        checkSeverity(severity);
        return builder.withDetail(SEVERITY_DETAIL, severity.name());
    }

    /**
     * Add the given severity to the builder and creates the final Result.
     *
     * @param severity the severity to use
     * @param builder  the builder to mutate
     * @return a final health check Result
     */
    public static HealthCheck.Result resultWithSeverity(HealthStatus severity, HealthCheck.ResultBuilder builder) {
        return addSeverity(severity, builder).build();
    }

    private static void checkBuilder(HealthCheck.ResultBuilder builder) {
        checkArgumentNotNull(builder, "builder cannot be null");
    }

    private static void checkSeverity(HealthStatus severity) {
        checkArgumentNotNull(severity, "severity cannot be null");
    }
}
