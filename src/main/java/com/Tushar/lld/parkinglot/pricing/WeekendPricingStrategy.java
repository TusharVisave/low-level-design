package com.Tushar.lld.parkinglot.pricing;

/**
 * Premium weekend rate: ₹80 per started hour.
 *
 * <p><strong>OCP demonstration</strong>: this class was added on Day 3
 * to prove the Open/Closed Principle at work. Adding this new pricing
 * rule required:
 * <ul>
 *   <li>Creating THIS file — open for extension</li>
 *   <li>Zero changes to {@link PricingStrategy}, {@link HourlyPricingStrategy},
 *       {@link DailyPricingStrategy}, or {@link MonthlyPricingStrategy}
 *       — closed for modification</li>
 * </ul>
 *
 * <p>If pricing had been implemented as an if/else chain in a single
 * {@code calculateFee(String type, long minutes)} method, adding this
 * rate would have forced a modification to existing, tested code.
 */
public class WeekendPricingStrategy implements PricingStrategy {

    private static final double RATE_PER_HOUR   = 80.0;
    private static final long   MINUTES_PER_HOUR = 60L;

    @Override
    public double calculate(long durationMinutes) {

        if (durationMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative"
            );
        }

        long hoursCharged =
                (durationMinutes + MINUTES_PER_HOUR - 1)
                / MINUTES_PER_HOUR;

        return hoursCharged * RATE_PER_HOUR;
    }
}
