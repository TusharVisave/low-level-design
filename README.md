# Low-Level Design — Multi-Floor Parking Lot System

A clean, modular, and extensible Java-based Low-Level Design (LLD) implementation of a multi-floor parking lot system.

This project demonstrates core Object-Oriented Programming (OOP) principles, SOLID architecture, design patterns (Strategy, Factory, Observer), comprehensive unit testing, and rigorous concurrency and scale analysis.

---

## Table of Contents

1. [Problem Statement](#problem-statement)
2. [Domain Model & Class Architecture](#domain-model--class-architecture)
3. [Class Diagram](#class-diagram)
4. [Design Patterns & Principles](#design-patterns--principles)
5. [End-to-End Workflow](#end-to-end-workflow)
6. [Scalability & Bottleneck Analysis](#scalability--bottleneck-analysis)
   - [What Breaks at 10,000 Spots?](#what-breaks-at-10000-spots)
   - [What Breaks at 50,000 Spots Across a City?](#what-breaks-at-50000-spots-across-a-city)
7. [Project Structure](#project-structure)
8. [Build and Test Execution](#build-and-test-execution)

---

## Problem Statement

Design an in-memory low-level parking lot management system capable of:

- Managing multiple parking floors, each containing parking spots of varying sizes (`BIKE`, `COMPACT`, `LARGE`).
- Supporting multiple vehicle types (`BIKE`, `CAR`, `TRUCK`) with strict spot compatibility:
  - `Bike` $\rightarrow$ fits in `BIKE`, `COMPACT`, `LARGE`.
  - `Car` $\rightarrow$ fits in `COMPACT`, `LARGE`.
  - `Truck` $\rightarrow$ fits only in `LARGE`.
- Allocating the nearest available, compatible parking spot to an incoming vehicle (lowest floor first, lowest spot number on that floor).
- Generating a `Ticket` upon entry that encapsulates vehicle details, assigned spot, entry timestamp, and an assigned pricing policy.
- Processing vehicle exits: freeing the assigned spot, stamping exit time, and computing the payable fee based on parking duration and the configured pricing strategy.
- Supporting pluggable pricing models (hourly, daily, monthly, weekend) without modifying existing ticket or lot logic.
- Centralizing vehicle instantiation through a dedicated factory.
- Notifying real-time subscribers (e.g. entrance display boards, operations telemetry) when the lot reaches full capacity or vacates spots, using the Observer pattern.

---

## Domain Model & Class Architecture

The system is structured into domain entities, pricing strategies, and capacity observers:

### Core Domain
- **`ParkingLot`**: Orchestrates high-level operations (`parkVehicle`, `exitVehicle`, `findNearestAvailableSpot`). Manages an immutable list of `ParkingFloor` instances, tracks overall capacity, and maintains an observer registry.
- **`ParkingFloor`**: Represents a single physical level with an indexed collection of `ParkingSpot` objects.
- **`ParkingSpot`**: Encapsulates an individual parking bay with a spot number, `SpotSize`, and its current occupancy status (`parkedVehicle`).
- **`Vehicle`** (Abstract): Root vehicle abstraction defining `licensePlate` and `SpotSize` requirements. Subclassed by concrete types:
  - `Bike` (requires `BIKE`)
  - `Car` (requires `COMPACT`)
  - `Truck` (requires `LARGE`)
- **`VehicleFactory`**: Provides a single creation point to instantiate vehicles safely via `VehicleType` and license plate string.
- **`Ticket`**: Represents an active or completed parking session. Encapsulates vehicle, spot reference, entry/exit timestamps, and the chosen `PricingStrategy`.

### Pricing Strategies (Strategy Pattern)
- **`PricingStrategy`** (Interface): Contract defining `double calculate(long durationMinutes)`.
- **`HourlyPricingStrategy`**: Rounds duration up to the nearest hour with flat rate per hour.
- **`DailyPricingStrategy`**: Rounds duration up to the nearest full day with a 24-hour rate.
- **`MonthlyPricingStrategy`**: Flat fee covering up to 30 days, billing additional 30-day blocks for overflow.
- **`WeekendPricingStrategy`**: Applies surge pricing (higher hourly rate) during weekends or promotional peak periods.

### Capacity Observers (Observer Pattern)
- **`ParkingLotObserver`** (Interface): Contract defining `void onParkingLotFull(ParkingLot)` and `void onParkingLotAvailable(ParkingLot)`.
- **`DisplayBoard`**: Real-time electronic entrance display board switching between `AVAILABLE` and `FULL` status and showing available spot counts.
- **`CapacityAlertService`**: Telemetry and alerting observer tracking full/available state transition histories and alert metrics.

---

## Class Diagram

### Structural Overview
```text
                               ┌────────────────────────────────┐
                               │           ParkingLot           │
                               ├────────────────────────────────┤
                               │ - parkingFloors: List<Floor>   │
                               │ - observers: List<Observer>    │
                               ├────────────────────────────────┤
                               │ + parkVehicle(v, strategy)     │
                               │ + exitVehicle(ticket)          │
                               │ + findNearestAvailableSpot(v)  │
                               │ + addObserver(observer)        │
                               │ + removeObserver(observer)     │
                               │ + isFull(): boolean            │
                               │ + getAvailableSpotsCount(): int│
                               └───────┬──────────────┬─────────┘
                                       │ 1..*         │ 0..* notifies
                                       ▼              ▼
                               ┌────────────────┐   «interface»  ┌───────────────────────────────────────────┐
                               │  ParkingFloor  │                │            ParkingLotObserver             │
                               ├────────────────┤                ├───────────────────────────────────────────┤
                               │ - floorNumber  │                │ + onParkingLotFull(lot: ParkingLot)       │
                               │ - spots: List  │                │ + onParkingLotAvailable(lot: ParkingLot)  │
                               ├────────────────┤                └─────────────────────┬─────────────────────┘
                               │ + isFull()     │                                      │
                               └───────┬────────┘                   ┌──────────────────┴──────────────────┐
                                       │ 1..* contains              ▼                                     ▼
                                       ▼                ┌───────────────────────┐             ┌───────────────────────┐
                               ┌────────────────┐       │     DisplayBoard      │             │ CapacityAlertService  │
                               │  ParkingSpot   │       ├───────────────────────┤             ├───────────────────────┤
                               ├────────────────┤       │ - status: DisplayStatus│            │ - alertHistory: List  │
                               │ - spotNumber   │       │ - message: String     │             │ - fullAlertCount: int │
                               │ - spotSize     │       └───────────────────────┘             └───────────────────────┘
                               │ - parkedVehicle│
                               ├────────────────┤
                               │ + isAvailable()│
                               │ + canFit(v)    │
                               │ + park(v)      │
                               │ + remove()     │
                               └───────┬────────┘
                                       │ 0..1 parks
                                       ▼
                               ┌────────────────┐
                               │Vehicle (Abstr.)│
                               ├────────────────┤
                               │ - licensePlate │
                               │ - spotSize     │
                               └───────┬────────┘
                                       │
                      ┌────────────────┼────────────────┐
                      ▼                ▼                ▼
            ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
            │       Bike       │ │       Car        │ │      Truck       │
            └──────────────────┘ └──────────────────┘ └──────────────────┘


        ┌────────────────────────────────────────────────────────┐
        │                         Ticket                         │
        ├────────────────────────────────────────────────────────┤
        │ - vehicle: Vehicle                                     │
        │ - spot: ParkingSpot                                    │
        │ - pricingStrategy: PricingStrategy                     │
        │ - entryTime: LocalDateTime                             │
        │ - exitTime: LocalDateTime                              │
        ├────────────────────────────────────────────────────────┤
        │ + calculateFee(): double                               │
        │ ~ recordExit(exitTime): void                           │
        └───────────────────────────┬────────────────────────────┘
                                    │ delegates fee computation
                                    ▼
       «interface»  ┌───────────────────────────────────────────┐
                    │              PricingStrategy              │
                    ├───────────────────────────────────────────┤
                    │ + calculate(durationMinutes: long): double│
                    └─────────────────────┬─────────────────────┘
                                          │
            ┌──────────────────┬──────────┴───────────┬──────────────────┐
            ▼                  ▼                      ▼                  ▼
┌───────────────────────┐ ┌────────────────────┐ ┌───────────────────────┐ ┌─────────────────────────┐
│ HourlyPricingStrategy │ │ DailyPricingStrategy│ │MonthlyPricingStrategy │ │ WeekendPricingStrategy  │
└───────────────────────┘ └────────────────────┘ └───────────────────────┘ └─────────────────────────┘


                             ┌──────────────────────────────────────┐
                             │            VehicleFactory            │
                             ├──────────────────────────────────────┤
                             │ + createVehicle(type, plate): Vehicle│
                             └──────────────────┬───────────────────┘
                                                │ instantiates
                                                ▼
                                    Bike / Car / Truck
```

---

## Design Patterns & Principles

### 1. Strategy Pattern (`PricingStrategy`)
- **Where**: `Ticket` delegates fee calculation to an injected `PricingStrategy` instance.
- **Why**: Different parking customers use different billing models (hourly shoppers, daily commuters, monthly pass holders, surge weekend visitors). Instead of hardcoding conditional `switch`/`if-else` blocks inside `Ticket` or `ParkingLot`, fee calculation is encapsulated into separate strategy classes.
- **SOLID Compliance**:
  - **Open/Closed Principle (OCP)**: New pricing schemes (e.g., EV charging fee, festival surge, VIP discounts) can be added by implementing `PricingStrategy` without touching existing tested classes.
  - **Single Responsibility Principle (SRP)**: `Ticket` tracks session state; `PricingStrategy` calculates monetary charge.

### 2. Factory Pattern (`VehicleFactory`)
- **Where**: `VehicleFactory.createVehicle(VehicleType, String)`.
- **Why**: Clients should not be tightly coupled to concrete subclasses (`new Car(...)`, `new Bike(...)`). The factory encapsulates vehicle creation, input validation, and decouples calling layers from concrete class constructors.
- **SOLID Compliance**:
  - **Dependency Inversion Principle (DIP)**: Clients depend on the `Vehicle` abstraction rather than concrete implementations.

### 3. Observer Pattern (`ParkingLotObserver`)
- **Where**: `ParkingLot` acts as the event publisher and maintains a registry of `ParkingLotObserver` instances. Subscribed components implement `ParkingLotObserver` (e.g., `DisplayBoard`, `CapacityAlertService`).
- **Why**: Arriving drivers and facility operations teams require instant visibility into lot occupancy. When the lot becomes full, entrance displays must promptly indicate `FULL`, and when spots become available, displays must update with vacancy counts. Directly embedding display rendering or telemetry logging into `ParkingLot` would violate SRP and create tight coupling to hardware and external services.
- **State Transition Triggers**:
  - **`AVAILABLE -> FULL`**: When `parkVehicle` occupies the last vacant spot across all floors (`isFull() == true`), `notifyParkingLotFull()` broadcasts `onParkingLotFull(this)` to all registered observers.
  - **`FULL -> AVAILABLE`**: When `exitVehicle` frees a spot from an otherwise completely full lot (`wasFull && !isFull()`), `notifyParkingLotAvailable()` broadcasts `onParkingLotAvailable(this)` to all observers.
- **SOLID Compliance**:
  - **Open/Closed Principle (OCP)**: New observers (e.g., SMS alerts, toll gate barrier controllers, Prometheus telemetry exporters) can subscribe without changing existing lot code.
  - **Single Responsibility Principle (SRP)**: `ParkingLot` focuses strictly on parking orchestration; observers handle display formatting, event counting, or alerting.
  - **Loose Coupling / DIP**: `ParkingLot` depends only on the `ParkingLotObserver` abstraction, remaining agnostic to concrete observer implementations.

### 4. Separation of Concerns & Information Expert
- `ParkingSpot` is the expert on whether a vehicle fits (`spot.canFit(vehicle)`) and mutates its own state (`park()`, `removeVehicle()`).
- `ParkingFloor` iterates over its own spots, computes floor-level occupancy (`isFull()`, `getAvailableSpotsCount()`), and delegates spot compatibility to each `ParkingSpot`.
- `ParkingLot` coordinates multi-floor operations, overall capacity tracking, and state transition dispatching without micromanaging individual spot state.

---

## End-to-End Workflow

### Vehicle Entry (`ParkingLot.parkVehicle`)
1. Client creates a vehicle via `VehicleFactory.createVehicle(VehicleType.CAR, "KA-01-HH-1234")`.
2. Client calls `parkingLot.parkVehicle(vehicle, pricingStrategy)`.
3. `ParkingLot` iterates through its floors:
   - Floor 0 searches its spots sequentially for `canFit(vehicle)`.
   - If no spot is free on Floor 0, search advances to Floor 1, and so on.
4. The first compatible spot is allocated via `spot.park(vehicle)`.
5. `ParkingLot` checks if the lot reached capacity (`isFull()`). If true, it triggers `observer.onParkingLotFull(this)` across all registered observers (e.g. updating the entrance `DisplayBoard` to `FULL`).
6. A `Ticket` is minted containing vehicle metadata, spot reference, pricing strategy, and entry timestamp (`LocalDateTime.now()`).

### Vehicle Exit (`ParkingLot.exitVehicle`)
1. Driver presents `Ticket` to `parkingLot.exitVehicle(ticket)`.
2. `ParkingLot` records `wasFull = isFull()`.
3. `ParkingLot` calls `ticket.recordExit(LocalDateTime.now())`.
4. The assigned spot is freed via `ticket.getSpot().removeVehicle()`.
5. If the lot was full prior to exit (`wasFull && !isFull()`), `ParkingLot` notifies all registered observers via `observer.onParkingLotAvailable(this)` (e.g. updating the entrance `DisplayBoard` to `AVAILABLE` with spot count).
6. Fee calculation is delegated to `ticket.calculateFee()`, which queries `pricingStrategy.calculate(durationMinutes)`.
7. Total payable amount in rupees is returned.

---

## Scalability & Bottleneck Analysis

### What Breaks at 10,000 Spots?

In the current single-facility implementation, several assumptions break under high volume:

#### 1. Linear Nearest-Spot Search ($O(F \times S)$)
- **Problem**: When 10,000 spots are distributed across floors, `findNearestAvailableSpot` executes a sequential scan over every floor and spot:
  ```java
  for (ParkingFloor floor : parkingFloors) {
      for (ParkingSpot spot : parkingSpots) {
          if (spot.canFit(vehicle)) return spot;
      }
  }
  ```
  In worst-case scenarios (e.g. lot nearly full, or looking for a `TRUCK` spot on the highest floor), this performs up to 10,000 iterations per entry request. Under 50 vehicles entering per minute, entry throughput stalls.
- **Solution**:
  - Maintain an indexed lookup or **Min-Heap / PriorityQueue** per vehicle size:
    ```java
    Map<SpotSize, PriorityQueue<ParkingSpot>> availableSpotsBySize;
    ```
    Where spots are ordered by `(floorNumber, spotNumber)`.
  - Finding the nearest spot becomes an $O(1)$ peek or $O(\log N)$ poll operation. When a vehicle leaves, its spot is inserted back in $O(\log N)$.

#### 2. Concurrency & Race Conditions (Double Booking)
- **Problem**: The current classes (`ParkingLot`, `ParkingFloor`, `ParkingSpot`) have no synchronization primitives (`synchronized`, `ReentrantLock`, `AtomicReference`).
- If two entry gates simultaneously attempt to park cars of the same size, both threads may identify the same available spot `spot.canFit(vehicle) == true` and execute `spot.park(vehicle)` concurrently. One vehicle will overwrite the other, or corrupt internal state.
- **Solution**:
  - Spot-level atomic updates via Compare-And-Swap (`AtomicReference<Vehicle> parkedVehicle`) or optimistic locking with version numbers.
  - Floor-level striping or thread-safe concurrent queues for available spots.

#### 3. In-Memory State & Volatility
- **Problem**: The entire state lives in RAM. An unhandled JVM crash, deployment restart, or power outage destroys all active tickets and spot allocations.
- **Solution**: Persist state transitions in an ACID database (PostgreSQL) using transactional isolation (`SELECT ... FOR UPDATE`), or an append-only event store (Event Sourcing).

---

### What Breaks at 50,000 Spots Across a City?

Scaling from a single multi-floor garage to a city-wide smart parking platform (e.g., municipal parking across 200 facilities) introduces distributed systems challenges:

```text
               ┌────────────────────────────────────────────────────────┐
               │              Global API Gateway / Router               │
               └───────────┬────────────────────────────────┬───────────┘
                           │                                │
                           ▼                                ▼
               ┌───────────────────────┐        ┌───────────────────────┐
               │ Facility Service: NYC │        │ Facility Service: SFO │
               └───────────┬───────────┘        └───────────┬───────────┘
                           │                                │
             ┌─────────────┴─────────────┐            ┌─────┴─────┐
             ▼                           ▼            ▼           ▼
        Entry Gate 1                Entry Gate 2   Gate 1      Gate 2
        (IoT Sensor)                (IoT Sensor)
```

#### 1. Physical Distance vs. Numerical Distance
- In a single building, floor 0 spot 1 is objectively closer than floor 3 spot 40.
- Across a city, "nearest" is no longer an integer spot ID; it is geospatial ($L_2$ Euclidean distance or driving duration via Google Maps API). Allocations require **Geospatial Indexing** (e.g., Uber H3 Hexagonal Hierarchical Spatial Index, S2 Geometry, or PostGIS R-Tree).

#### 2. Network Latency & Offline Gate Resilience (CAP Theorem)
- If an entry gate in a basement garage loses connectivity to the central cloud server, vehicles cannot be blocked from entering.
- **Architecture**: Edge-computing micro-controllers at each facility run an autonomous, local `FacilityParkingAgent`. Each facility manages its own internal allocations independently, syncing aggregate availability to the central cloud asynchronously (Eventual Consistency via Kafka / RabbitMQ).

#### 3. Distributed Locking & High Contention
- A single centralized database handling 50,000 spots across thousands of concurrent mobile reservations and entry sensors faces catastrophic lock contention.
- **Solution**: Partition (shard) the database by `facility_id` or `city_zone`. Distributed locks (via Redis Redlock) are only needed at the local facility level, never globally.

---

## Project Structure

```text
low-level-design/
├── src/
│   ├── main/
│   │   ├── java/com/Tushar/lld/
│   │   │   ├── LowLevelDesignApplication.java
│   │   │   └── parkinglot/
│   │   │       ├── Bike.java
│   │   │       ├── Car.java
│   │   │       ├── Truck.java
│   │   │       ├── Vehicle.java
│   │   │       ├── VehicleType.java
│   │   │       ├── VehicleFactory.java
│   │   │       ├── SpotSize.java
│   │   │       ├── ParkingSpot.java
│   │   │       ├── ParkingFloor.java
│   │   │       ├── ParkingLot.java
│   │   │       ├── Ticket.java
│   │   │       ├── observer/
│   │   │       │   ├── ParkingLotObserver.java
│   │   │       │   ├── DisplayBoard.java
│   │   │       │   └── CapacityAlertService.java
│   │   │       └── pricing/
│   │   │           ├── PricingStrategy.java
│   │   │           ├── HourlyPricingStrategy.java
│   │   │           ├── DailyPricingStrategy.java
│   │   │           ├── MonthlyPricingStrategy.java
│   │   │           └── WeekendPricingStrategy.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/Tushar/lld/
│           ├── LowLevelDesignApplicationTests.java
│           └── parkinglot/
│               ├── ParkingLotTest.java
│               ├── TicketTest.java
│               ├── VehicleFactoryTest.java
│               ├── observer/
│               │   └── ParkingLotObserverTest.java
│               └── pricing/
│                   ├── PricingStrategyTest.java
│                   └── WeekendPricingStrategyTest.java
├── pom.xml
└── README.md
```

---

## Build and Test Execution

The project uses Maven with Java 21.

### Prerequisites
- JDK 21+
- Apache Maven or included `./mvnw` wrapper

### Run All Unit Tests
```bash
./mvnw clean test
```

### Build JAR Package
```bash
./mvnw clean package
```