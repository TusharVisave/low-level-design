package com.Tushar.lld.parkinglot.pricing;

/**
 * Charges ₹5000 per started month (30-day month assumed).
 *
 * <p>Partial months are rounded up (ceiling division), so 32 days
 * counts as 2 months → ₹10,000. Suitable for monthly pass holders.
 */
public class MonthlyPricingStrategy implements PricingStrategy {

    private static final double RATE_PER_MONTH    = 5000.0;
    private static final long   MINUTES_PER_MONTH = 30L * 24 * 60;

    @Override
    public double calculate(long durationMinutes) {

        if (durationMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }

        long monthsCharged =
                (durationMinutes + MINUTES_PER_MONTH - 1)
                / MINUTES_PER_MONTH;

        return monthsCharged * RATE_PER_MONTH;
    }
}
