package com.Tushar.lld.parkinglot;

import com.Tushar.lld.parkinglot.pricing.PricingStrategy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a parking session from entry to exit.
 *
 * <p>A {@code Ticket} is issued when a vehicle parks and holds a reference
 * to the {@link PricingStrategy} chosen at entry time. When the vehicle exits,
 * {@link #calculateFee()} delegates the computation to the injected strategy —
 * there are no conditionals here. This is the Strategy pattern's payoff: the
 * ticket doesn't need to know <em>how</em> the fee is computed, only <em>who</em>
 * is responsible for computing it.
 */
public class Ticket {

    private final Vehicle         vehicle;
    private final ParkingSpot     spot;
    private final PricingStrategy pricingStrategy;
    private final LocalDateTime   entryTime;
    private       LocalDateTime   exitTime;

    public Ticket(
            Vehicle vehicle,
            ParkingSpot spot,
            PricingStrategy pricingStrategy,
            LocalDateTime entryTime
    ) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        if (spot == null) {
            throw new IllegalArgumentException("Spot cannot be null");
        }
        if (pricingStrategy == null) {
            throw new IllegalArgumentException(
                    "PricingStrategy cannot be null"
            );
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null");
        }

        this.vehicle         = vehicle;
        this.spot            = spot;
        this.pricingStrategy = pricingStrategy;
        this.entryTime       = entryTime;
    }

    /**
     * Calculates the fee by delegating to the injected strategy.
     *
     * <p>If the vehicle has not yet exited, the current time is used
     * (useful for "check current charge" UX). The strategy receives
     * the duration in minutes and returns the rupee amount — this
     * method contains zero if/else branching on pricing type.
     *
     * @return fee in rupees
     */
    public double calculateFee() {

        LocalDateTime end =
                (exitTime != null) ? exitTime : LocalDateTime.now();

        long durationMinutes =
                ChronoUnit.MINUTES.between(entryTime, end);

        return pricingStrategy.calculate(durationMinutes);
    }

    /** Records the exit time, called by {@link ParkingLot#exitVehicle(Ticket)}. */
    void recordExit(LocalDateTime exitTime) {
        if (this.exitTime != null) {
            throw new IllegalStateException(
                    "Vehicle has already exited"
            );
        }
        this.exitTime = exitTime;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }
}
