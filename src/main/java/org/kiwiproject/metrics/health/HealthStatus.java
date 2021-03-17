package org.kiwiproject.metrics.health;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.collect.KiwiMaps.isNullOrEmpty;
import static org.kiwiproject.metrics.health.HealthCheckResults.SEVERITY_DETAIL;

import com.google.common.collect.Iterables;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * This enum is used to indicate the health status/severity for both a service (i.e. multiple running instances)
 * as well as the status of individual service instances.
 */
@Slf4j
public enum HealthStatus {

    /**
     * Everything is OK and healthy.
     * <p>
     * This should only be used when health checks return {@code healthy=true}.
     */
    OK(1),

    /**
     * This can be used to provide additional information that may help to diagnose a problem, or simply to communicate
     * some information that we might want to act upon.
     * <p>
     * This can be used whether health checks return {@code healthy=true} or {@code healthy=false}.
     */
    INFO(2),

    /**
     * Something is wrong or potentially wrong, or there is something concerning or that we need to take action on.
     * <p>
     * This can be used whether health checks return {@code healthy=true} or {@code healthy=false}.
     */
    WARN(3),

    /**
     * There is an actual problem that needs to be addressed and corrected quickly for the service to function properly.
     * <p>
     * This should only be used when health checks return {@code healthy=false}.
     */
    CRITICAL(4),

    /**
     * This level is reserved for the case when all instances of a service are down, and thus the system cannot
     * perform its services and functions. It needs to be corrected immediately.
     * <p>
     * This should only be used when there are no service instances.
     */
    FATAL(5);

    /**
     * Internal value used to compare severity (we do NOT want to rely on the ordinal of the enum constants).
     */
    @Getter(AccessLevel.PACKAGE)
    private final int value;

    HealthStatus(int value) {
        this.value = value;
    }

    /**
     * Given a map containing the results of all the health checks in a service instance (e.g. the JSON that is
     * returned by calling the {@code healthcheck} endpoint of an instance), determine the appropriate health status
     * by checking both the {@code healthy} flag (true or false) as well as the {@code severity} if present.
     *
     * @param healthDetails the health check results as a map of maps
     * @return the most appropriate {@link HealthStatus}
     * @implNote Expects a map of maps structured like: {@code string ->(string -> object)}. The map's keys
     * are the names of the individual checks (e.g. database, serverErrors, rottenTomato, etc.) while the values
     * are maps containing the health check result, which at a minimum should contain a boolean {@code healthy} and
     * can contain a {@code severity} whose values should be the exact names of this enum as a string, e.g. "INFO".
     */
    public static HealthStatus from(Map<String, Object> healthDetails) {
        if (isNullOrEmpty(healthDetails)) {
            return CRITICAL;
        }

        var healthStatuses = healthDetails.values()
                .stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(HealthStatus::determineOverallStatus)
                .collect(toSet());

        if (healthStatuses.isEmpty()) {
            return CRITICAL;
        }

        if (healthStatuses.size() == 1) {
            return Iterables.getOnlyElement(healthStatuses);
        }

        return highestSeverity(healthStatuses);
    }

    private static HealthStatus determineOverallStatus(Map<String, Object> map) {
        var healthy = getHealthyValue(map);
        var severity = getHealthStatusOrNull(map);
        return determineOverallStatus(healthy, severity);
    }

    // Assumes the map contains a "healthy" key with boolean value, otherwise returns false.
    private static boolean getHealthyValue(Map<String, Object> map) {
        try {
            var value = map.getOrDefault("healthy", Boolean.FALSE).toString();
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            LOG.warn("Something gave us a 'healthy' value that threw an exception on toString()");
            return false;
        }
    }

    // Suppress "Exception handlers should preserve the original exceptions" - we know the cause in this case is that
    // the severity value is null or not a valid enum constant, so the stack trace provides no additional help
    @SuppressWarnings("java:S1166")
    private static HealthStatus getHealthStatusOrNull(Map<String, Object> map) {
        if (!map.containsKey(SEVERITY_DETAIL)) {
            return null;
        }

        Object severityObj = map.get(SEVERITY_DETAIL);
        String severity = severityOrNull(severityObj);

        try {
            return Optional.ofNullable(severity)
                    .map(HealthStatus::valueOf)
                    .orElse(HealthStatus.WARN);
        } catch (Exception e) {
            LOG.error("Something gave us an invalid severity: {} (returning WARN). Health map: {}", severity, map);
            return WARN;
        }
    }

    private static String severityOrNull(Object severityObj) {
        if (severityObj instanceof String) {
            return (String) severityObj;
        }

        LOG.warn("Something gave us a severity that was not a String: {}", severityObj);
        return null;
    }

    private static HealthStatus determineOverallStatus(boolean healthy, @Nullable HealthStatus severity) {
        if (isInvalidCombination(healthy, severity)) {
            LOG.warn("Detected invalid (healthy, severity) combination: ({}, {})", healthy, severity);
            return HealthStatus.max(WARN, Optional.ofNullable(severity).orElse(WARN));
        }

        if (isNull(severity)) {
            return HealthStatus.from(healthy);
        }

        if (healthy) {
            return HealthStatus.max(OK, severity);
        }

        return severity;
    }

    /**
     * Return the default severity for the given healthy value.
     *
     * @param healthy true if healthy, false otherwise
     * @return the default HealthStatus
     */
    public static HealthStatus defaultSeverityForValue(boolean healthy) {
        return healthy ? OK : WARN;
    }

    /**
     * Is the given (healthy, severity) combination valid?
     *
     * @param healthy  true if healthy, false otherwise
     * @param severity the severity, if null the return value will always be true
     * @return true if the given (healthy, severity) combination is valid, otherwise false
     */
    public static boolean isValidCombination(boolean healthy, @Nullable HealthStatus severity) {
        return !isInvalidCombination(healthy, severity);
    }

    /**
     * Is the given (healthy, severity) combination invalid?
     *
     * @param healthy  true if healthy, false otherwise
     * @param severity the severity, if null the return value will always be false (i.e. it's a valid combination)
     * @return true if the given (healthy, severity) combination is NOT valid, otherwise false
     */
    public static boolean isInvalidCombination(boolean healthy, @Nullable HealthStatus severity) {
        if (isNull(severity)) {
            return false;
        }

        var healthyWithInvalidSeverity = healthy && (severity == CRITICAL || severity == FATAL);
        var unhealthyWithInvalidSeverity = !healthy && severity == OK;

        return healthyWithInvalidSeverity || unhealthyWithInvalidSeverity;
    }

    /**
     * Return {@link #OK} if {@code value} is {@code true}; {@link #WARN} otherwise (including if {@code null}).
     *
     * @param value a (nullable) Boolean value
     * @return {@link #OK} if {@code value} is {@code true}; {@link #WARN} otherwise
     */
    public static HealthStatus from(Boolean value) {
        return BooleanUtils.toBoolean(value) ? OK : WARN;
    }

    /**
     * Return the higher of the two given status values.
     *
     * @param status1 the first status
     * @param status2 the second status
     * @return the higher of the statuses
     * @throws IllegalArgumentException if either argument is null
     */
    public static HealthStatus max(HealthStatus status1, HealthStatus status2) {
        checkArgumentNotNull(status1);
        checkArgumentNotNull(status2);

        return compare(status1, status2) > 0 ? status1 : status2;
    }

    private static int compare(HealthStatus status1, HealthStatus status2) {
        return comparingSeverity().compare(status1, status2);
    }

    /**
     * Return the highest severity in the (non-null, non-empty) collection of status values.
     *
     * @param statuses a collection of {@link HealthStatus}
     * @return the highest severity of the given status values
     * @throws IllegalArgumentException if the status list is null or empty
     */
    public static HealthStatus highestSeverity(Collection<HealthStatus> statuses) {
        checkArgument(nonNull(statuses) && !statuses.isEmpty(), "statuses cannot be empty or null");

        return Collections.max(statuses, comparingSeverity());
    }

    /**
     * Return a {@link Comparator} that compares {@link HealthStatus} objects from lowest to highest severity.
     *
     * @return a comparator that orders from lowest to highest severity
     */
    public static Comparator<HealthStatus> comparingSeverity() {
        return HealthStatusComparator.INSTANCE;
    }
}
