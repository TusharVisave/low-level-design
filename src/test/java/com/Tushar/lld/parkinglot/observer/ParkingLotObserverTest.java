package com.Tushar.lld.parkinglot.observer;

import com.Tushar.lld.parkinglot.Car;
import com.Tushar.lld.parkinglot.ParkingFloor;
import com.Tushar.lld.parkinglot.ParkingLot;
import com.Tushar.lld.parkinglot.ParkingSpot;
import com.Tushar.lld.parkinglot.SpotSize;
import com.Tushar.lld.parkinglot.Ticket;
import com.Tushar.lld.parkinglot.Vehicle;
import com.Tushar.lld.parkinglot.pricing.HourlyPricingStrategy;
import com.Tushar.lld.parkinglot.pricing.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParkingLotObserverTest {

    private PricingStrategy pricingStrategy;

    @BeforeEach
    void setUp() {
        pricingStrategy = new HourlyPricingStrategy();
    }

    private ParkingLot createSingleSpotLot() {
        ParkingSpot spot = new ParkingSpot(1, SpotSize.COMPACT);
        ParkingFloor floor = new ParkingFloor(0, List.of(spot));
        return new ParkingLot(List.of(floor));
    }

    private ParkingLot createMultiFloorLot(int spotsPerFloor, int floorsCount) {
        List<ParkingFloor> floors = new ArrayList<>();
        int spotCounter = 1;
        for (int f = 0; f < floorsCount; f++) {
            List<ParkingSpot> spots = new ArrayList<>();
            for (int s = 0; s < spotsPerFloor; s++) {
                spots.add(new ParkingSpot(spotCounter++, SpotSize.COMPACT));
            }
            floors.add(new ParkingFloor(f, spots));
        }
        return new ParkingLot(floors);
    }

    @Test
    @DisplayName("Should notify DisplayBoard when lot transitions to full")
    void shouldNotifyDisplayBoardWhenParkingLotBecomesFull() {
        ParkingLot lot = createSingleSpotLot();
        DisplayBoard displayBoard = new DisplayBoard();
        lot.addObserver(displayBoard);

        assertEquals(DisplayBoard.DisplayStatus.AVAILABLE, displayBoard.getStatus());
        assertFalse(displayBoard.isFull());

        Vehicle car = new Car("KA-01-AB-1234");
        lot.parkVehicle(car, pricingStrategy);

        assertTrue(lot.isFull());
        assertEquals(0, lot.getAvailableSpotsCount());
        assertEquals(DisplayBoard.DisplayStatus.FULL, displayBoard.getStatus());
        assertTrue(displayBoard.isFull());
        assertTrue(displayBoard.getMessage().contains("FULL"));
    }

    @Test
    @DisplayName("Should notify DisplayBoard when spot is vacated from full lot")
    void shouldNotifyDisplayBoardWhenSpotVacatedFromFullLot() {
        ParkingLot lot = createSingleSpotLot();
        DisplayBoard displayBoard = new DisplayBoard();
        lot.addObserver(displayBoard);

        Vehicle car = new Car("KA-01-AB-1234");
        Ticket ticket = lot.parkVehicle(car, pricingStrategy);

        assertTrue(displayBoard.isFull());

        lot.exitVehicle(ticket);

        assertFalse(lot.isFull());
        assertEquals(1, lot.getAvailableSpotsCount());
        assertEquals(DisplayBoard.DisplayStatus.AVAILABLE, displayBoard.getStatus());
        assertFalse(displayBoard.isFull());
        assertTrue(displayBoard.getMessage().contains("1 spot(s) available"));
    }

    @Test
    @DisplayName("Should not notify available when vehicle exits a lot that was not full")
    void shouldNotNotifyAvailableIfLotWasNotFullBeforeExit() {
        ParkingLot lot = createMultiFloorLot(2, 1); // 2 spots total
        CapacityAlertService alertService = new CapacityAlertService();
        lot.addObserver(alertService);

        Vehicle car1 = new Car("KA-01-AB-1111");
        Ticket ticket1 = lot.parkVehicle(car1, pricingStrategy);

        // Lot has 1 spot left, never became full
        assertEquals(0, alertService.getFullAlertCount());
        assertEquals(0, alertService.getAvailableAlertCount());

        lot.exitVehicle(ticket1);

        // Still should not trigger available notification because it was never full
        assertEquals(0, alertService.getAvailableAlertCount());
        assertEquals(2, lot.getAvailableSpotsCount());
    }

    @Test
    @DisplayName("Should not send duplicate full notification when rejected on already-full lot")
    void shouldNotSendDuplicateFullNotificationOnRejectedEntry() {
        ParkingLot lot = createSingleSpotLot();
        CapacityAlertService alertService = new CapacityAlertService();
        lot.addObserver(alertService);

        Vehicle car1 = new Car("KA-01-AB-1111");
        lot.parkVehicle(car1, pricingStrategy);
        assertEquals(1, alertService.getFullAlertCount());

        Vehicle car2 = new Car("KA-01-AB-2222");
        assertThrows(IllegalStateException.class, () -> lot.parkVehicle(car2, pricingStrategy));

        // Alert count should remain 1
        assertEquals(1, alertService.getFullAlertCount());
    }

    @Test
    @DisplayName("Should broadcast full and available events to multiple observers")
    void shouldBroadcastToMultipleObservers() {
        ParkingLot lot = createSingleSpotLot();
        DisplayBoard displayBoard = new DisplayBoard();
        CapacityAlertService alertService = new CapacityAlertService();

        lot.addObserver(displayBoard);
        lot.registerObserver(alertService);

        assertEquals(2, lot.getObservers().size());

        Vehicle car = new Car("KA-01-AB-1234");
        Ticket ticket = lot.parkVehicle(car, pricingStrategy);

        assertEquals(DisplayBoard.DisplayStatus.FULL, displayBoard.getStatus());
        assertEquals(1, alertService.getFullAlertCount());

        lot.exitVehicle(ticket);

        assertEquals(DisplayBoard.DisplayStatus.AVAILABLE, displayBoard.getStatus());
        assertEquals(1, alertService.getAvailableAlertCount());
        assertEquals(2, alertService.getAlertHistory().size());
    }

    @Test
    @DisplayName("Should stop sending notifications after observer is removed")
    void shouldStopSendingNotificationsAfterObserverRemoved() {
        ParkingLot lot = createSingleSpotLot();
        CapacityAlertService alertService = new CapacityAlertService();
        lot.addObserver(alertService);

        lot.removeObserver(alertService);
        assertFalse(lot.getObservers().contains(alertService));

        Vehicle car = new Car("KA-01-AB-1234");
        Ticket ticket = lot.parkVehicle(car, pricingStrategy);
        lot.exitVehicle(ticket);

        assertEquals(0, alertService.getFullAlertCount());
        assertEquals(0, alertService.getAvailableAlertCount());
    }

    @Test
    @DisplayName("Should prevent duplicate observer registrations")
    void shouldPreventDuplicateObserverRegistrations() {
        ParkingLot lot = createSingleSpotLot();
        CapacityAlertService alertService = new CapacityAlertService();

        lot.addObserver(alertService);
        lot.addObserver(alertService); // Duplicate add

        assertEquals(1, lot.getObservers().size());

        Vehicle car = new Car("KA-01-AB-1234");
        lot.parkVehicle(car, pricingStrategy);

        assertEquals(1, alertService.getFullAlertCount());
    }

    @Test
    @DisplayName("Should transition to full only after all spots across all floors are filled")
    void shouldTransitionToFullAcrossMultipleFloors() {
        ParkingLot lot = createMultiFloorLot(1, 2); // 2 floors, 1 spot each = 2 spots
        DisplayBoard displayBoard = new DisplayBoard();
        lot.addObserver(displayBoard);

        Vehicle car1 = new Car("KA-01-AB-1111");
        Vehicle car2 = new Car("KA-01-AB-2222");

        Ticket t1 = lot.parkVehicle(car1, pricingStrategy);
        assertFalse(lot.isFull());
        assertEquals(1, lot.getAvailableSpotsCount());
        assertEquals(DisplayBoard.DisplayStatus.AVAILABLE, displayBoard.getStatus());

        Ticket t2 = lot.parkVehicle(car2, pricingStrategy);
        assertTrue(lot.isFull());
        assertEquals(0, lot.getAvailableSpotsCount());
        assertEquals(DisplayBoard.DisplayStatus.FULL, displayBoard.getStatus());

        // Exit car 1
        lot.exitVehicle(t1);
        assertFalse(lot.isFull());
        assertEquals(1, lot.getAvailableSpotsCount());
        assertEquals(DisplayBoard.DisplayStatus.AVAILABLE, displayBoard.getStatus());

        // Exit car 2 (was not full anymore, so no extra available transition)
        lot.exitVehicle(t2);
        assertEquals(2, lot.getAvailableSpotsCount());
        assertEquals(DisplayBoard.DisplayStatus.AVAILABLE, displayBoard.getStatus());
    }

    @Test
    @DisplayName("Should accurately report capacity metrics on lot and floors")
    void shouldAccuratelyReportCapacityMetrics() {
        ParkingLot lot = createMultiFloorLot(2, 2); // 4 spots total
        assertEquals(4, lot.getTotalCapacity());
        assertEquals(4, lot.getAvailableSpotsCount());
        assertEquals(0, lot.getOccupiedSpotsCount());
        assertFalse(lot.isFull());

        ParkingFloor floor0 = lot.getParkingFloors().get(0);
        assertEquals(2, floor0.getTotalSpots());
        assertEquals(2, floor0.getAvailableSpotsCount());
        assertFalse(floor0.isFull());

        Vehicle car1 = new Car("KA-01-AB-1111");
        Vehicle car2 = new Car("KA-01-AB-2222");
        lot.parkVehicle(car1, pricingStrategy);
        lot.parkVehicle(car2, pricingStrategy);

        // Floor 0 is now full
        assertEquals(0, floor0.getAvailableSpotsCount());
        assertTrue(floor0.isFull());

        // Lot still has floor 1 free
        assertEquals(2, lot.getAvailableSpotsCount());
        assertEquals(2, lot.getOccupiedSpotsCount());
        assertFalse(lot.isFull());
    }

    @Test
    @DisplayName("Should accept initial observers via constructor")
    void shouldAcceptInitialObserversInConstructor() {
        ParkingSpot spot = new ParkingSpot(1, SpotSize.COMPACT);
        ParkingFloor floor = new ParkingFloor(0, List.of(spot));
        DisplayBoard displayBoard = new DisplayBoard();

        ParkingLot lot = new ParkingLot(List.of(floor), List.of(displayBoard));
        assertTrue(lot.getObservers().contains(displayBoard));

        lot.parkVehicle(new Car("KA-01-AB-1234"), pricingStrategy);
        assertTrue(displayBoard.isFull());
    }

    @Test
    @DisplayName("Should validate null inputs for observer management")
    void shouldValidateNullInputsForObserverManagement() {
        ParkingLot lot = createSingleSpotLot();

        assertThrows(IllegalArgumentException.class, () -> lot.addObserver(null));
        assertThrows(IllegalArgumentException.class, () -> lot.removeObserver(null));
        assertThrows(IllegalArgumentException.class, () -> new ParkingLot(lot.getParkingFloors(), null));
    }

    @Test
    @DisplayName("Should safely handle observer unregistering during notification callback")
    void shouldSafelyHandleObserverUnregisteringDuringCallback() {
        ParkingLot lot = createSingleSpotLot();

        ParkingLotObserver selfRemovingObserver = new ParkingLotObserver() {
            @Override
            public void onParkingLotFull(ParkingLot parkingLot) {
                parkingLot.removeObserver(this);
            }

            @Override
            public void onParkingLotAvailable(ParkingLot parkingLot) {
            }
        };

        DisplayBoard displayBoard = new DisplayBoard();
        lot.addObserver(selfRemovingObserver);
        lot.addObserver(displayBoard);

        // Should not throw ConcurrentModificationException
        lot.parkVehicle(new Car("KA-01-AB-1234"), pricingStrategy);

        assertFalse(lot.getObservers().contains(selfRemovingObserver));
        assertTrue(lot.getObservers().contains(displayBoard));
        assertTrue(displayBoard.isFull());
    }
}
