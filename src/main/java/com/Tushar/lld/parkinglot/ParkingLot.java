package com.Tushar.lld.parkinglot;

import com.Tushar.lld.parkinglot.pricing.PricingStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParkingLot {

    private final List<ParkingFloor> parkingFloors;

    public ParkingLot(List<ParkingFloor> parkingFloors) {

        if (parkingFloors == null || parkingFloors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking lot must contain at least one floor"
            );
        }

        this.parkingFloors =
                new ArrayList<>(parkingFloors);
    }

    /**
     * Parks the vehicle and issues a {@link Ticket} with the chosen
     * pricing strategy baked in.
     *
     * <p>The strategy is injected here — not hard-coded — so the caller
     * decides the rate plan (hourly, daily, monthly, weekend, etc.).
     * {@code ParkingLot} itself remains closed for modification when new
     * rate plans are added.
     *
     * @param vehicle         the vehicle to park
     * @param pricingStrategy the rate plan for this session
     * @return a ticket representing this parking session
     * @throws IllegalStateException if no suitable spot is available
     */
    public Ticket parkVehicle(
            Vehicle vehicle,
            PricingStrategy pricingStrategy
    ) {

        ParkingSpot nearestSpot =
                findNearestAvailableSpot(vehicle);

        if (nearestSpot == null) {
            throw new IllegalStateException(
                    "No suitable parking spot available"
            );
        }

        nearestSpot.park(vehicle);

        return new Ticket(
                vehicle,
                nearestSpot,
                pricingStrategy,
                LocalDateTime.now()
        );
    }

    /**
     * Exits a parked vehicle: frees the spot, records the exit time,
     * and returns the fee.
     *
     * @param ticket the ticket issued at entry
     * @return the fee in rupees
     */
    public double exitVehicle(Ticket ticket) {

        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }

        ticket.recordExit(LocalDateTime.now());
        ticket.getSpot().removeVehicle();

        return ticket.calculateFee();
    }

    /**
     * Calculates the parking fee for a given pricing strategy and duration in hours.
     *
     * @param pricingStrategy the rate plan to use
     * @param hours           parking duration in hours
     * @return the calculated parking fee
     */
    public double calculateParkingFee(
            PricingStrategy pricingStrategy,
            int hours
    ) {
        if (pricingStrategy == null) {
            throw new IllegalArgumentException("PricingStrategy cannot be null");
        }
        if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
        return pricingStrategy.calculate((long) hours * 60);
    }

    public ParkingSpot findNearestAvailableSpot(
            Vehicle vehicle
    ) {

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null"
            );
        }

        for (ParkingFloor floor : parkingFloors) {

            ParkingSpot spot =
                    floor.findNearestAvailableSpot(vehicle);

            if (spot != null) {
                return spot;
            }
        }

        return null;
    }

    public List<ParkingFloor> getParkingFloors() {
        return Collections.unmodifiableList(
                parkingFloors
        );
    }
}