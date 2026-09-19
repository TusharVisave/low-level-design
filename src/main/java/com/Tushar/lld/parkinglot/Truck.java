package com.Tushar.lld.parkinglot;

public class Truck extends Vehicle {

    public Truck(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public boolean canFitIn(SpotSize spotSize) {
        return spotSize == SpotSize.LARGE;
    }
}