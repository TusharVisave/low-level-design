package com.Tushar.lld.parkinglot.pricing;

/**
 * Strategy pattern contract for parking fee calculation.
 *
 * <p>Each concrete implementation encapsulates one pricing algorithm.
 * Adding a new rate (e.g. weekend, EV, VIP) requires only a new class
 * that implements this interface — no existing code changes needed.
 * This is the Open/Closed Principle formalised as a named pattern.
 *
 * @see HourlyPricingStrategy
 * @see DailyPricingStrategy
 * @see MonthlyPricingStrategy
 * @see WeekendPricingStrategy
 */
public interface PricingStrategy {

    /**
     * Calculates the parking fee for the given duration.
     *
     * @param durationMinutes total parked time in minutes (must be >= 0)
     * @return the fee in rupees (>= 0)
     */
    double calculate(long durationMinutes);
}
