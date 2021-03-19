package org.kiwiproject.metrics.health;

import java.util.Comparator;

/**
 * A Comparator that compares {@link HealthStatus} objects by severity.
 */
class HealthStatusComparator implements Comparator<HealthStatus> {

    static final HealthStatusComparator INSTANCE = new HealthStatusComparator();

    @Override
    public int compare(HealthStatus status1, HealthStatus status2) {
        return Comparator.comparingInt(HealthStatus::getValue).compare(status1, status2);
    }
}
