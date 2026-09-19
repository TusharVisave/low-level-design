package com.Tushar.lld.parkinglot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParkingLot {

    private final List<ParkingSpot> parkingSpots;

    public ParkingLot(List<ParkingSpot> parkingSpots) {
        if (parkingSpots == null || parkingSpots.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parking lot must contain at least one spot"
            );
        }

        this.parkingSpots = new ArrayList<>(parkingSpots);
    }

    public void parkVehicle(Vehicle vehicle) {

        if (vehicle == null) {
            throw new IllegalArgumentException(
                    "Vehicle cannot be null"
            );
        }

        for (ParkingSpot spot : parkingSpots) {

            if (spot.canFit(vehicle)) {
                spot.park(vehicle);
                return;
            }
        }

        throw new IllegalStateException(
                "No suitable parking spot available"
        );
    }

    public List<ParkingSpot> getParkingSpots() {
        return Collections.unmodifiableList(parkingSpots);
    }
}