package com.Tushar.lld.parkinglot;

public class Car extends Vehicle {

    public Car(String licensePlate) {
        super(licensePlate);
    }

    @Override
    public boolean canFitIn(SpotSize spotSize) {
        return spotSize == SpotSize.COMPACT
                || spotSize == SpotSize.LARGE;
    }
}