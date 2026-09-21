package com.Tushar.lld.parkinglot.pricing;

/**
 * Charges ₹300 per started day.
 *
 * <p>Partial days are rounded up (ceiling division), so 25 hours
 * counts as 2 days → ₹600. Suitable for commuters who park overnight.
 */
public class DailyPricingStrategy implements PricingStrategy {

    private static final double RATE_PER_DAY    = 300.0;
    private static final long   MINUTES_PER_DAY = 24 * 60L;

    @Override
    public double calculate(long durationMinutes) {

        if (durationMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }

        long daysCharged =
                (durationMinutes + MINUTES_PER_DAY - 1)
                / MINUTES_PER_DAY;

        return daysCharged * RATE_PER_DAY;
    }
}
