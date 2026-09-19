package com.Tushar.lld.parkinglot;


public abstract class Vehicle {

    private final String licensePlate;

    protected Vehicle(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException(
                    "License plate cannot be empty"
            );
        }

        this.licensePlate = licensePlate;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public abstract boolean canFitIn(SpotSize spotSize);
}