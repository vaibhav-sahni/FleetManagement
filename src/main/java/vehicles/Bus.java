package vehicles;

import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;
import exceptions.OverloadException;
import interfaces.CargoCarrier;
import interfaces.FuelConsumable;
import interfaces.Maintainable;
import interfaces.PassengerCarrier;

public class Bus extends LandVehicle implements FuelConsumable, PassengerCarrier, CargoCarrier, Maintainable {
    private double fuelLevel = 0.0;
    private final int passengerCapacity = 50;
    private int currentPassengers = 0;
    private final double cargoCapacity = 500.0;
    private double currentCargo = 0.0;
    private boolean maintenanceNeeded = false;
    protected double lastMaintenanceMileage = 0;
    
    public Bus(String id, String model, double maxSpeed, int numWheels) throws InvalidOperationException {
        super(id, model, maxSpeed,numWheels);
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientFuelException {
        // Prevent movement if maintenance is required
        if (needsMaintenance()) throw new InvalidOperationException("Vehicle " + getID() + " requires maintenance and cannot move.");
        if (distance < 0) throw new InvalidOperationException("Negative distance");
        double needed = distance / calculateFuelEfficiency();
        if (needed > fuelLevel) throw new InsufficientFuelException("Not enough fuel for bus " + getID());
        fuelLevel -= needed;
        addMileage(distance);
        if (getCurrentMileage() > 10000) maintenanceNeeded = true;
        System.out.printf("Bus %s: Transporting passengers and cargo for %.2f km. Fuel used: %.2f L%n", getID(), distance, needed);
    }

    @Override public double calculateFuelEfficiency() { 
        return 10.0; 
    }

    // FuelConsumable
    @Override public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) throw new InvalidOperationException("Refuel amount must be > 0");
        fuelLevel += amount;
    }
    @Override public double getFuelLevel() { 
        return fuelLevel; 
    }
    @Override public double consumeFuel(double distance) throws InsufficientFuelException {
        double need = distance / calculateFuelEfficiency();
        if (need > fuelLevel) throw new InsufficientFuelException("Insufficient fuel");
        fuelLevel -= need;
        return need;
    }

    // PassengerCarrier
    @Override public void boardPassengers(int count) throws OverloadException {
        if (count < 0) return;
        if (currentPassengers + count > passengerCapacity) throw new OverloadException("Bus overload");
        currentPassengers += count;
    }
    @Override public void disembarkPassengers(int count) throws InvalidOperationException {
        if (count > currentPassengers) throw new InvalidOperationException("Too many disembarking");
        currentPassengers -= count;
    }
    @Override public int getPassengerCapacity() { return passengerCapacity; }
    @Override public int getCurrentPassengers() { return currentPassengers; }

    // CargoCarrier
    @Override public void loadCargo(double weight) throws OverloadException, InvalidOperationException {
        if (weight <= 0) throw new InvalidOperationException("Weight must be > 0");
        if (currentCargo + weight > cargoCapacity) throw new OverloadException("Bus Overloaded!");
        currentCargo += weight;
    }
    @Override public void unloadCargo(double weight) throws InvalidOperationException {
        if (weight < 0 || weight > currentCargo) throw new InvalidOperationException("Unload weight more than actual!");
        currentCargo -= weight;
    }
    @Override public double getCargoCapacity() { 
        return cargoCapacity; 
    }
    @Override public double getCurrentCargo() { 
        return currentCargo; 
    }

    // Maintainable
    @Override public void scheduleMaintenance() { 
        maintenanceNeeded = true; 
    }
    @Override public boolean needsMaintenance() { 
        return maintenanceNeeded || (getCurrentMileage() - lastMaintenanceMileage >= 10000);
    }
    @Override public void performMaintenance() {
        lastMaintenanceMileage = getCurrentMileage();
        maintenanceNeeded = false;
        System.out.println("Bus " + getID() + " maintenance performed.");
    }
}
