package com.Tushar.lld.parkinglot.observer;

import com.Tushar.lld.parkinglot.ParkingLot;

/**
 * Concrete observer representing an electronic display board installed at the
 * parking lot entrance.
 *
 * <p>Updates its display message and status in real time as the parking lot
 * becomes full or vacates spots, guiding arriving motorists.
 */
public class DisplayBoard implements ParkingLotObserver {

    public enum DisplayStatus {
        AVAILABLE,
        FULL
    }

    private DisplayStatus status;
    private String message;

    public DisplayBoard() {
        this.status = DisplayStatus.AVAILABLE;
        this.message = "Welcome! Parking spots available.";
    }

    @Override
    public void onParkingLotFull(ParkingLot parkingLot) {
        this.status = DisplayStatus.FULL;
        this.message = "PARKING LOT FULL: No spots available.";
    }

    @Override
    public void onParkingLotAvailable(ParkingLot parkingLot) {
        this.status = DisplayStatus.AVAILABLE;
        int available = (parkingLot != null) ? parkingLot.getAvailableSpotsCount() : 0;
        this.message = "Welcome! " + available + " spot(s) available.";
    }

    public DisplayStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFull() {
        return status == DisplayStatus.FULL;
    }
}
