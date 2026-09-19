package com.Tushar.lld.parkinglot;

public class Bike extends Vehicle {

    public Bike(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public boolean canFitIn(SpotSize spotSize) {
        return true;
    }
}