package com.Tushar.lld.parkinglot;

public class ParkingSpot {

    private final int spotNumber;
    private final SpotSize spotSize;
    private Vehicle parkedVehicle;

    public ParkingSpot(int spotNumber, SpotSize spotSize) {
        if (spotNumber <= 0) {
            throw new IllegalArgumentException(
                    "Spot number must be positive"
            );
        }

        if (spotSize == null) {
            throw new IllegalArgumentException(
                    "Spot size cannot be null"
            );
        }

        this.spotNumber = spotNumber;
        this.spotSize = spotSize;
    }

    public boolean isAvailable() {
        return parkedVehicle == null;
    }

    public boolean canFit(Vehicle vehicle) {
        return isAvailable()
                && vehicle != null
                && vehicle.canFitIn(spotSize);
    }

    public void park(Vehicle vehicle) {
        if (!canFit(vehicle)) {
            throw new IllegalStateException(
                    "Vehicle cannot fit in this parking spot"
            );
        }

        parkedVehicle = vehicle;
    }

    public void removeVehicle() {
        parkedVehicle = null;
    }

    public int getSpotNumber() {
        return spotNumber;
    }

    public SpotSize getSpotSize() {
        return spotSize;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }
}