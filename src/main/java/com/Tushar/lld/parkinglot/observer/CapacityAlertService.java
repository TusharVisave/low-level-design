package com.Tushar.lld.parkinglot.observer;

import com.Tushar.lld.parkinglot.ParkingLot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete observer representing an administrative alert and auditing service.
 *
 * <p>Tracks historical capacity notifications and log events for operations teams
 * and automated telemetry dashboards.
 */
public class CapacityAlertService implements ParkingLotObserver {

    private final List<String> alertHistory = new ArrayList<>();
    private int fullAlertCount = 0;
    private int availableAlertCount = 0;

    @Override
    public void onParkingLotFull(ParkingLot parkingLot) {
        fullAlertCount++;
        alertHistory.add("ALERT: Parking lot reached maximum capacity.");
    }

    @Override
    public void onParkingLotAvailable(ParkingLot parkingLot) {
        availableAlertCount++;
        int available = (parkingLot != null) ? parkingLot.getAvailableSpotsCount() : 0;
        alertHistory.add("INFO: Parking lot has available capacity (" + available + " spots).");
    }

    public int getFullAlertCount() {
        return fullAlertCount;
    }

    public int getAvailableAlertCount() {
        return availableAlertCount;
    }

    public List<String> getAlertHistory() {
        return Collections.unmodifiableList(alertHistory);
    }
}
