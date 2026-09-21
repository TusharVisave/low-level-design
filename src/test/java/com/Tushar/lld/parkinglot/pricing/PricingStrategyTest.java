package com.Tushar.lld.parkinglot.pricing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for all PricingStrategy implementations.
 *
 * <p>The final test — {@code addingWeekendStrategyRequiresNoChangesToExistingStrategies}
 * — is the explicit OCP tie-in: it proves that WeekendPricingStrategy was dropped in
 * alongside the three original strategies without altering any of them. Each strategy
 * computes its result independently; none knows the others exist.
 */
class PricingStrategyTest {

    // ─────────────────────────────────────────────────────────────
    // Hourly
    // ─────────────────────────────────────────────────────────────

    @Test
    void hourlyRate_lessThanOneHour_chargesOneHour() {

        PricingStrategy strategy = new HourlyPricingStrategy();

        // 30 min → ceil(0.5) = 1 hour → ₹50
        assertEquals(50.0, strategy.calculate(30));
    }

    @Test
    void hourlyRate_exactHour_chargesOneHour() {

        PricingStrategy strategy = new HourlyPricingStrategy();

        // 60 min → exactly 1 hour → ₹50
        assertEquals(50.0, strategy.calculate(60));
    }

    @Test
    void hourlyRate_partialHour_roundsUp() {

        PricingStrategy strategy = new HourlyPricingStrategy();

        // 90 min → ceil(1.5) = 2 hours → ₹100
        assertEquals(100.0, strategy.calculate(90));
    }

    @Test
    void hourlyRate_zeroDuration_chargesZero() {

        PricingStrategy strategy = new HourlyPricingStrategy();

        assertEquals(0.0, strategy.calculate(0));
    }

    @Test
    void hourlyRate_negativeDuration_throwsException() {

        PricingStrategy strategy = new HourlyPricingStrategy();

        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.calculate(-1)
        );
    }

    // ─────────────────────────────────────────────────────────────
    // Daily
    // ─────────────────────────────────────────────────────────────

    @Test
    void dailyRate_lessThanOneDay_chargesOneDay() {

        PricingStrategy strategy = new DailyPricingStrategy();

        // 6 hours = 360 min → ceil(0.25 day) = 1 day → ₹300
        assertEquals(300.0, strategy.calculate(360));
    }

    @Test
    void dailyRate_exactDay_chargesOneDay() {

        PricingStrategy strategy = new DailyPricingStrategy();

        // 24 hours = 1440 min → 1 day → ₹300
        assertEquals(300.0, strategy.calculate(1440));
    }

    @Test
    void dailyRate_partialDay_roundsUp() {

        PricingStrategy strategy = new DailyPricingStrategy();

        // 25 hours = 1500 min → ceil(1.04 days) = 2 days → ₹600
        assertEquals(600.0, strategy.calculate(1500));
    }

    // ─────────────────────────────────────────────────────────────
    // Monthly
    // ─────────────────────────────────────────────────────────────

    @Test
    void monthlyRate_lessThanOneMonth_chargesOneMonth() {

        PricingStrategy strategy = new MonthlyPricingStrategy();

        // 15 days = 21600 min → ceil(0.5 month) = 1 month → ₹5000
        assertEquals(5000.0, strategy.calculate(15L * 24 * 60));
    }

    @Test
    void monthlyRate_exactMonth_chargesOneMonth() {

        PricingStrategy strategy = new MonthlyPricingStrategy();

        // 30 days = 43200 min → 1 month → ₹5000
        assertEquals(5000.0, strategy.calculate(30L * 24 * 60));
    }

    @Test
    void monthlyRate_partialMonth_roundsUp() {

        PricingStrategy strategy = new MonthlyPricingStrategy();

        // 32 days → ceil(1.067 months) = 2 months → ₹10,000
        assertEquals(10000.0, strategy.calculate(32L * 24 * 60));
    }

    // ─────────────────────────────────────────────────────────────
    // Weekend (OCP proof)
    // ─────────────────────────────────────────────────────────────

    @Test
    void weekendRate_lessThanOneHour_chargesOneHour() {

        PricingStrategy strategy = new WeekendPricingStrategy();

        // 30 min → 1 hour → ₹80
        assertEquals(80.0, strategy.calculate(30));
    }

    @Test
    void weekendRate_partialHour_roundsUp() {

        PricingStrategy strategy = new WeekendPricingStrategy();

        // 90 min → 2 hours → ₹160
        assertEquals(160.0, strategy.calculate(90));
    }

    // ─────────────────────────────────────────────────────────────
    // OCP explicit proof
    // ─────────────────────────────────────────────────────────────

    /**
     * Demonstrates that WeekendPricingStrategy was added without modifying any
     * existing strategy.
     *
     * <p>All four strategies compute their fees independently from the same
     * duration input. No strategy inspects or calls another. The PricingStrategy
     * interface is the only shared contract, and it was never modified.
     *
     * <p>This is OCP in action: the pricing system was open for extension
     * (WeekendPricingStrategy is a new class) and closed for modification
     * (HourlyPricingStrategy, DailyPricingStrategy, MonthlyPricingStrategy
     * were untouched — verify with `git diff HEAD~1 -- src/main/java/...pricing/`).
     */
    @Test
    void addingWeekendStrategyRequiresNoChangesToExistingStrategies() {

        long ninetyMinutes = 90L;

        PricingStrategy hourly  = new HourlyPricingStrategy();
        PricingStrategy daily   = new DailyPricingStrategy();
        PricingStrategy monthly = new MonthlyPricingStrategy();
        PricingStrategy weekend = new WeekendPricingStrategy();

        // Each strategy computes independently — no coupling, no shared state
        assertEquals(100.0,  hourly.calculate(ninetyMinutes),  "Hourly: 2h × ₹50");
        assertEquals(300.0,  daily.calculate(ninetyMinutes),   "Daily: 1 day × ₹300");
        assertEquals(5000.0, monthly.calculate(ninetyMinutes), "Monthly: 1 month × ₹5000");
        assertEquals(160.0,  weekend.calculate(ninetyMinutes), "Weekend: 2h × ₹80");
    }
}
