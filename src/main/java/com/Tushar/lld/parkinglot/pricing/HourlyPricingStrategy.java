package com.Tushar.lld.parkinglot.pricing;

/**
 * Charges ₹50 per started hour.
 *
 * <p>Partial hours are rounded up (ceiling division), so 90 minutes
 * counts as 2 hours → ₹100. This mirrors typical metered parking.
 */
public class HourlyPricingStrategy implements PricingStrategy {

    private static final double RATE_PER_HOUR = 50.0;
    private static final long   MINUTES_PER_HOUR = 60L;

    @Override
    public double calculate(long durationMinutes) {

        if (durationMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }

        // Ceiling division: bill for every started hour
        long hoursCharged =
                (durationMinutes + MINUTES_PER_HOUR - 1)
                / MINUTES_PER_HOUR;

        return hoursCharged * RATE_PER_HOUR;
    }
}
