package com.Tushar.lld.parkinglot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleFactoryTest {

    private final VehicleFactory vehicleFactory =
            new VehicleFactory();

    @Test
    void shouldCreateBike() {

        Vehicle vehicle =
                vehicleFactory.createVehicle(
                        VehicleType.BIKE,
                        "MH15BIKE01"
                );

        assertInstanceOf(Bike.class, vehicle);

        assertEquals(
                "MH15BIKE01",
                vehicle.getLicensePlate()
        );
    }

    @Test
    void shouldCreateCar() {

        Vehicle vehicle =
                vehicleFactory.createVehicle(
                        VehicleType.CAR,
                        "MH15CAR01"
                );

        assertInstanceOf(Car.class, vehicle);

        assertEquals(
                "MH15CAR01",
                vehicle.getLicensePlate()
        );
    }

    @Test
    void shouldCreateTruck() {

        Vehicle vehicle =
                vehicleFactory.createVehicle(
                        VehicleType.TRUCK,
                        "MH15TRUCK01"
                );

        assertInstanceOf(Truck.class, vehicle);

        assertEquals(
                "MH15TRUCK01",
                vehicle.getLicensePlate()
        );
    }

    @Test
    void shouldRejectNullVehicleType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleFactory.createVehicle(
                        null,
                        "MH15CAR01"
                )
        );
    }

    @Test
    void shouldRejectEmptyLicensePlate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleFactory.createVehicle(
                        VehicleType.CAR,
                        ""
                )
        );
    }

    @Test
    void shouldRejectNullLicensePlate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleFactory.createVehicle(
                        VehicleType.CAR,
                        null
                )
        );
    }
}