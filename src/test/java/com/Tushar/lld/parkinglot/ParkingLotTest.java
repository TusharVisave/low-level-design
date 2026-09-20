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

        ParkingFloor floor =
                new ParkingFloor(
                        0,
                        List.of(compactSpot)
                );

        ParkingLot parkingLot =
                new ParkingLot(List.of(floor));

        Vehicle car =
                new Car("MH15AB1234");

        parkingLot.parkVehicle(car);

        assertEquals(
                car,
                compactSpot.getParkedVehicle()
        );
    }

    @Test
    void shouldRejectVehicleWhenNoMatchingSpotExists() {

        ParkingSpot compactSpot =
                new ParkingSpot(1, SpotSize.COMPACT);

        ParkingFloor floor =
                new ParkingFloor(
                        0,
                        List.of(compactSpot)
                );

        ParkingLot parkingLot =
                new ParkingLot(List.of(floor));

        Vehicle truck =
                new Truck("MH15TR1234");

        assertThrows(
                IllegalStateException.class,
                () -> parkingLot.parkVehicle(truck)
        );
    }

    @Test
    void shouldFindNearestAvailableSpotAcrossFloors() {

        ParkingSpot floorZeroSpot =
                new ParkingSpot(1, SpotSize.COMPACT);

        ParkingSpot floorOneSpot =
                new ParkingSpot(2, SpotSize.COMPACT);

        ParkingFloor floorZero =
                new ParkingFloor(
                        0,
                        List.of(floorZeroSpot)
                );

        ParkingFloor floorOne =
                new ParkingFloor(
                        1,
                        List.of(floorOneSpot)
                );

        ParkingLot parkingLot =
                new ParkingLot(
                        List.of(floorZero, floorOne)
                );

        Vehicle car =
                new Car("MH15NEAR01");

        ParkingSpot result =
                parkingLot.findNearestAvailableSpot(car);

        assertEquals(
                floorZeroSpot,
                result
        );
    }

    @Test
    void shouldUseNextFloorWhenNearestSpotIsOccupied() {

        ParkingSpot nearestSpot =
                new ParkingSpot(1, SpotSize.COMPACT);

        ParkingSpot nextFloorSpot =
                new ParkingSpot(2, SpotSize.COMPACT);

        ParkingFloor floorZero =
                new ParkingFloor(
                        0,
                        List.of(nearestSpot)
                );

        ParkingFloor floorOne =
                new ParkingFloor(
                        1,
                        List.of(nextFloorSpot)
                );

        ParkingLot parkingLot =
                new ParkingLot(
                        List.of(floorZero, floorOne)
                );

        nearestSpot.park(
                new Car("MH15OCCUPIED")
        );

        Vehicle car =
                new Car("MH15NEXT01");

        ParkingSpot result =
                parkingLot.findNearestAvailableSpot(car);

        assertEquals(
                nextFloorSpot,
                result
        );
    }
}