package com.Tushar.lld.parkinglot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParkingLotTest {

    @Test
    void shouldParkVehicleInMatchingSpot() {

        ParkingSpot compactSpot =
                new ParkingSpot(1, SpotSize.COMPACT);

        ParkingLot parkingLot =
                new ParkingLot(List.of(compactSpot));

        Vehicle car =
                new Car("MH15AB1234");

        parkingLot.parkVehicle(car);

        assertEquals(
                car,
                compactSpot.getParkedVehicle()
        );
    }

    @Test
    void shouldRejectVehicleForMismatchedSpot() {

        ParkingSpot compactSpot =
                new ParkingSpot(1, SpotSize.COMPACT);

        ParkingLot parkingLot =
                new ParkingLot(List.of(compactSpot));

        Vehicle truck =
                new Truck("MH15TR1234");

        assertThrows(
                IllegalStateException.class,
                () -> parkingLot.parkVehicle(truck)
        );
    }
}