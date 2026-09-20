package com.Tushar.lld.parkinglot;


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

    public void parkVehicle(Vehicle vehicle) {

        ParkingSpot nearestSpot =
                findNearestAvailableSpot(vehicle);

        if (nearestSpot == null) {
            throw new IllegalStateException(
                    "No suitable parking spot available"
            );
        }

        nearestSpot.park(vehicle);
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