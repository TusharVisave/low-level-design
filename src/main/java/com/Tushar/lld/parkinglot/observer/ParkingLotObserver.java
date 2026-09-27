package com.Tushar.lld.parkinglot.observer;

import com.Tushar.lld.parkinglot.ParkingLot;

/**
 * Observer interface for listening to capacity state transitions in a {@link ParkingLot}.
 *
 * <p>Implements the GoF Observer pattern to decouple the parking lot domain logic
 * from downstream listeners such as physical entrance display boards, notification
 * services, or telemetry monitors.
 */
public interface ParkingLotObserver {

    /**
     * Invoked when the parking lot transitions from having available spots to being
     * completely full (zero available spots remaining).
     *
     * @param parkingLot the parking lot instance that reached full capacity
     */
    void onParkingLotFull(ParkingLot parkingLot);

    /**
     * Invoked when the parking lot transitions from being completely full to having
     * at least one available spot vacated.
     *
     * @param parkingLot the parking lot instance that now has available capacity
     */
    void onParkingLotAvailable(ParkingLot parkingLot);
}
