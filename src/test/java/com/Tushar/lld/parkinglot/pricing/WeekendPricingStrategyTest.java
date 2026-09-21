package com.Tushar.lld.parkinglot.pricing;

import com.Tushar.lld.parkinglot.ParkingFloor;
import com.Tushar.lld.parkinglot.ParkingLot;
import com.Tushar.lld.parkinglot.ParkingSpot;
import com.Tushar.lld.parkinglot.SpotSize;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeekendPricingStrategyTest {

    @Test
    void shouldAddWeekendPricingWithoutChangingExistingStrategies() {
        PricingStrategy weekendStrategy = new PricingStrategy() {
            @Override
            public double calculate(long durationMinutes) {
                long hours = durationMinutes / 60;
                return hours * 30;
            }
        };

        ParkingSpot spot = new ParkingSpot(1, SpotSize.COMPACT);
        ParkingFloor floor = new ParkingFloor(0, List.of(spot));
        ParkingLot parkingLot = new ParkingLot(List.of(floor));

        double result = parkingLot.calculateParkingFee(weekendStrategy, 4);

        assertEquals(120, result);
    }

    @Test
    void shouldCalculateFeeUsingWeekendPricingStrategy() {
        WeekendPricingStrategy weekendStrategy = new WeekendPricingStrategy();

        // 4 hours (240 mins) at ₹80/hr
        assertEquals(320.0, weekendStrategy.calculate(240));
    }
}