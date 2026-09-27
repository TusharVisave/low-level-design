package com.Tushar.lld.parkinglot;

import com.Tushar.lld.parkinglot.observer.ParkingLotObserver;
import com.Tushar.lld.parkinglot.pricing.PricingStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParkingLot {

    private final List<ParkingFloor> parkingFloors;
    private final List<ParkingLotObserver> observers;

    public ParkingLot(List<ParkingFloor> parkingFloors) {
        this(parkingFloors, Collections.emptyList());
    }

    public ParkingLot(
            List<ParkingFloor> parkingFloors,
            List<ParkingLotObserver> observers
    ) {
        if (parkingFloors == null || parkingFloors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking lot must contain at least one floor"
            );
        }
        if (observers == null) {
            throw new IllegalArgumentException(
                    "Observers list cannot be null"
            );
        }

        this.parkingFloors = new ArrayList<>(parkingFloors);
        this.observers = new CopyOnWriteArrayList<>();
        for (ParkingLotObserver observer : observers) {
            addObserver(observer);
        }
    }

    /**
     * Registers a new observer to receive capacity state notifications.
     *
     * @param observer the observer to register
     */
    public void addObserver(ParkingLotObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /** Alias for {@link #addObserver(ParkingLotObserver)}. */
    public void registerObserver(ParkingLotObserver observer) {
        addObserver(observer);
    }

    /**
     * Unregisters an observer so it will no longer receive capacity notifications.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(ParkingLotObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observers.remove(observer);
    }

    /** Alias for {@link #removeObserver(ParkingLotObserver)}. */
    public void unregisterObserver(ParkingLotObserver observer) {
        removeObserver(observer);
    }

    /**
     * Returns an unmodifiable view of all registered observers.
     */
    public List<ParkingLotObserver> getObservers() {
        return Collections.unmodifiableList(observers);
    }

    /**
     * Parks the vehicle and issues a {@link Ticket} with the chosen
     * pricing strategy baked in.
     *
     * <p>If this parking operation causes the lot to reach maximum capacity,
     * registered observers are notified via {@link ParkingLotObserver#onParkingLotFull(ParkingLot)}.
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

        if (isFull()) {
            notifyParkingLotFull();
        }

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
     * <p>If the lot was previously full and this exit creates available capacity,
     * registered observers are notified via {@link ParkingLotObserver#onParkingLotAvailable(ParkingLot)}.
     *
     * @param ticket the ticket issued at entry
     * @return the fee in rupees
     */
    public double exitVehicle(Ticket ticket) {

        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }

        boolean wasFull = isFull();

        ticket.recordExit(LocalDateTime.now());
        ticket.getSpot().removeVehicle();

        if (wasFull && !isFull()) {
            notifyParkingLotAvailable();
        }

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

    /**
     * Computes the total spot capacity across all floors.
     *
     * @return total capacity in spots
     */
    public int getTotalCapacity() {
        int total = 0;
        for (ParkingFloor floor : parkingFloors) {
            total += floor.getTotalSpots();
        }
        return total;
    }

    /**
     * Computes the number of currently vacant spots across all floors.
     *
     * @return count of available spots
     */
    public int getAvailableSpotsCount() {
        int count = 0;
        for (ParkingFloor floor : parkingFloors) {
            count += floor.getAvailableSpotsCount();
        }
        return count;
    }

    /**
     * Computes the number of currently occupied spots across all floors.
     *
     * @return count of occupied spots
     */
    public int getOccupiedSpotsCount() {
        return getTotalCapacity() - getAvailableSpotsCount();
    }

    /**
     * Returns true if all spots across all floors are occupied.
     *
     * @return true if no vacant spots remain
     */
    public boolean isFull() {
        return getAvailableSpotsCount() == 0;
    }

    protected void notifyParkingLotFull() {
        for (ParkingLotObserver observer : observers) {
            observer.onParkingLotFull(this);
        }
    }

    protected void notifyParkingLotAvailable() {
        for (ParkingLotObserver observer : observers) {
            observer.onParkingLotAvailable(this);
        }
    }

    public List<ParkingFloor> getParkingFloors() {
        return Collections.unmodifiableList(
                parkingFloors
        );
    }
}