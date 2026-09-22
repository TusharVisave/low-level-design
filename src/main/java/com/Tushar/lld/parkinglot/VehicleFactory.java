package com.Tushar.lld.parkinglot;

public class VehicleFactory {

    public Vehicle createVehicle(
            VehicleType vehicleType,
            String licensePlate
    ) {

        if (vehicleType == null) {
            throw new IllegalArgumentException(
                    "Vehicle type cannot be null"
            );
        }

        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException(
                    "License plate cannot be empty"
            );
        }

        return switch (vehicleType) {

            case BIKE ->
                    new Bike(licensePlate);

            case CAR ->
                    new Car(licensePlate);

            case TRUCK ->
                    new Truck(licensePlate);
        };
    }
}