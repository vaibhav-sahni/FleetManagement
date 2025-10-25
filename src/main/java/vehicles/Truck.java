package vehicles;

import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;
import exceptions.OverloadException;
import interfaces.CargoCarrier;
import interfaces.FuelConsumable;
import interfaces.Maintainable;

public class Truck extends LandVehicle implements FuelConsumable, CargoCarrier, Maintainable {
    private double fuelLevel;
    private double cargoCapacity;
    private double currentCargo;
    private boolean maintenanceNeeded;
    protected double lastMaintenanceMileage = 0;

    public Truck(String id, String model, double maxSpeed, int numWheels) throws InvalidOperationException {
        super(id, model, maxSpeed, numWheels);
        this.fuelLevel = 0;
        this.cargoCapacity = 5000.0; // kg
        this.currentCargo = 0.0;
        this.maintenanceNeeded = false;
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientFuelException {
        // Prevent movement if maintenance is required
        if (needsMaintenance()) throw new InvalidOperationException("Vehicle " + getID() + " requires maintenance and cannot move.");
        if (distance < 0) {
            throw new InvalidOperationException("Distance cannot be negative.");
        }
        double efficiency = calculateFuelEfficiency();
        if (currentCargo > cargoCapacity * 0.5) {
            efficiency *= 0.9;
        }
        double fuelNeeded = distance / efficiency;
        if (fuelNeeded > fuelLevel) {
            throw new InsufficientFuelException("Not enough fuel to move.");
        }
        fuelLevel -= fuelNeeded;
        addMileage(distance);
        if (getCurrentMileage() > 10000) maintenanceNeeded = true;
        System.out.printf("Truck %s moving for %.2f km. Fuel used: %.2f L%n",getID(),distance,fuelNeeded);

    }

    @Override
    public double calculateFuelEfficiency() {
        return 8.0; // km per liter
    }

    // FuelConsumable
    @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) throw new InvalidOperationException("Fuel amount must be positive.");
        fuelLevel += amount;
    }

    @Override
    public double getFuelLevel() {
        return fuelLevel;
    }

    @Override
    public double consumeFuel(double distance) throws InsufficientFuelException {
        double efficiency = calculateFuelEfficiency();
        if (currentCargo > cargoCapacity * 0.5) {
            efficiency *= 0.9;
        }
        double fuelNeeded = distance / efficiency;
        if (fuelNeeded > fuelLevel) {
            throw new InsufficientFuelException("Not enough fuel.");
        }
        fuelLevel -= fuelNeeded;
        return fuelNeeded;
    }

    // CargoCarrier
    @Override
    public void loadCargo(double weight) throws OverloadException {
        if (currentCargo + weight > cargoCapacity) {
            throw new OverloadException("Cargo overload.");
        }
        currentCargo += weight;
    }

    @Override
    public void unloadCargo(double weight) throws InvalidOperationException {
        if (weight > currentCargo) {
            throw new InvalidOperationException("Cannot unload more than current cargo.");
        }
        currentCargo -= weight;
    }

    @Override
    public double getCargoCapacity() {
        return cargoCapacity;
    }

    @Override
    public double getCurrentCargo() {
        return currentCargo;
    }

    // Maintainable
    @Override
    public void scheduleMaintenance() {
        maintenanceNeeded = true;
    }

    @Override
    public boolean needsMaintenance() {
        return maintenanceNeeded || (getCurrentMileage() - lastMaintenanceMileage >= 10000);
    }

    @Override
    public void performMaintenance() {
        lastMaintenanceMileage = getCurrentMileage();
        maintenanceNeeded = false;
        System.out.println("Truck " + getID() + " maintenance performed.");
    }
}
