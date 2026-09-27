package com.Tushar.lld.parkinglot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParkingFloor {

    private final int floorNumber;
    private final List<ParkingSpot> parkingSpots;

    public ParkingFloor(
            int floorNumber,
            List<ParkingSpot> parkingSpots
    ) {
        if (floorNumber < 0) {
            throw new IllegalArgumentException(
                    "Floor number cannot be negative"
            );
        }

        if (parkingSpots == null || parkingSpots.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking floor must contain at least one spot"
            );
        }

        this.floorNumber = floorNumber;
        this.parkingSpots = new ArrayList<>(parkingSpots);
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getParkingSpots() {
        return Collections.unmodifiableList(parkingSpots);
    }

    public int getTotalSpots() {
        return parkingSpots.size();
    }

    public int getAvailableSpotsCount() {
        int count = 0;
        for (ParkingSpot spot : parkingSpots) {
            if (spot.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public boolean isFull() {
        return getAvailableSpotsCount() == 0;
    }

    public ParkingSpot findNearestAvailableSpot(Vehicle vehicle) {

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null"
            );
        }

        for (ParkingSpot spot : parkingSpots) {
            if (spot.canFit(vehicle)) {
                return spot;
            }
        }

        return null;
    }
}
