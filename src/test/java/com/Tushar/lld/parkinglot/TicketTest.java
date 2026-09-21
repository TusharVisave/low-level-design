package com.Tushar.lld.parkinglot;

import com.Tushar.lld.parkinglot.pricing.DailyPricingStrategy;
import com.Tushar.lld.parkinglot.pricing.HourlyPricingStrategy;
import com.Tushar.lld.parkinglot.pricing.PricingStrategy;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link Ticket} — verifying that fee calculation delegates
 * entirely to the injected {@link PricingStrategy}.
 *
 * <p>These tests make the delegation visible: the same ticket setup with
 * two different strategies produces two different fees, proving that no
 * hard-coded rate logic lives inside {@code Ticket} itself.
 */
class TicketTest {

    private static final ParkingSpot SPOT =
            new ParkingSpot(1, SpotSize.COMPACT);

    private static final Vehicle CAR =
            new Car("MH01TEST01");

    @Test
    void calculateFee_delegatesToInjectedStrategy() {

        PricingStrategy hourly = new HourlyPricingStrategy();

        // Entry 2 hours ago, no exit recorded yet
        LocalDateTime entryTime =
                LocalDateTime.now().minusMinutes(120);

        Ticket ticket = new Ticket(
                CAR, SPOT, hourly, entryTime
        );

        // 120 min → 2 hours → ₹100
        assertEquals(100.0, ticket.calculateFee(), 0.01);
    }

    @Test
    void ticketWithDifferentStrategies_givesDifferentFees() {

        LocalDateTime entryTime =
                LocalDateTime.now().minusMinutes(90);

        Ticket hourlyTicket = new Ticket(
                CAR, SPOT,
                new HourlyPricingStrategy(),
                entryTime
        );

        // Need a different spot to avoid reuse conflict in state
        ParkingSpot spot2 = new ParkingSpot(2, SpotSize.COMPACT);

        Ticket dailyTicket = new Ticket(
                new Car("MH01TEST02"), spot2,
                new DailyPricingStrategy(),
                entryTime
        );

        // 90 min = 2 hours at ₹50 = ₹100
        assertEquals(100.0, hourlyTicket.calculateFee(), 0.01);

        // 90 min = 1 day at ₹300 = ₹300
        assertEquals(300.0, dailyTicket.calculateFee(), 0.01);
    }

    @Test
    void exitVehicle_freesSpot() {

        ParkingSpot spot = new ParkingSpot(3, SpotSize.COMPACT);
        ParkingFloor floor = new ParkingFloor(0, List.of(spot));
        ParkingLot parkingLot = new ParkingLot(List.of(floor));

        Vehicle car = new Car("MH01EXIT01");
        PricingStrategy hourly = new HourlyPricingStrategy();

        Ticket ticket = parkingLot.parkVehicle(car, hourly);

        assertNotNull(ticket);
        assertNotNull(ticket.getEntryTime());

        // Spot is occupied immediately after parking
        assertEquals(car, spot.getParkedVehicle());

        parkingLot.exitVehicle(ticket);

        // Spot is freed after exit
        assertNull(spot.getParkedVehicle());

        // Exit time is recorded
        assertNotNull(ticket.getExitTime());
    }

    @Test
    void exitVehicle_chargesCorrectFeeForKnownDuration() {

        // Construct a Ticket with a known entry time (120 minutes ago)
        // to avoid depending on wall-clock precision inside parkVehicle()
        ParkingSpot spot = new ParkingSpot(4, SpotSize.COMPACT);
        Vehicle car = new Car("MH01EXIT02");

        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(120);

        Ticket ticket = new Ticket(
                car, spot,
                new HourlyPricingStrategy(),
                entryTime
        );

        // Manually park so the spot isn't null-state only
        spot.park(car);
        ticket.recordExit(LocalDateTime.now());
        spot.removeVehicle();

        // 120 min = 2 hours → ₹100
        assertEquals(100.0, ticket.calculateFee(), 1.0); // 1-minute tolerance
    }
}
