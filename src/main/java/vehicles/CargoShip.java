package vehicles;

import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;
import exceptions.OverloadException;
import interfaces.CargoCarrier;
import interfaces.FuelConsumable;
import interfaces.Maintainable;

public class CargoShip extends WaterVehicle implements CargoCarrier, Maintainable,FuelConsumable {
    private final double cargoCapacity = 50000.0;
    private double currentCargo = 0.0;
    private boolean maintenanceNeeded = false;
    private Double fuelLevel = 0.0; 
    protected double lastMaintenanceMileage = 0;

    public CargoShip(String id, String model, double maxSpeed, boolean hasSail) throws InvalidOperationException {
        super(id, model, maxSpeed, hasSail);
        if (hasSail) {
            fuelLevel = null;
        }; 
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientFuelException {
        if (distance < 0) throw new InvalidOperationException("Negative distance");
        double eff = calculateFuelEfficiency();
        if (eff == 0) {
            // sail-powered — no fuel consumption
            addMileage(distance);
            if (getCurrentMileage() > 10000) maintenanceNeeded = true;
            System.out.printf("CargoShip %s: Sailing with cargo for %.2f km (no fuel).%n", getID(), distance);
            return;
        }
        double needed = distance / eff;
        if (fuelLevel == null || needed > fuelLevel) throw new InsufficientFuelException("Not enough fuel for cargo ship " + getID());
        fuelLevel -= needed;
        addMileage(distance);
        if (getCurrentMileage() > 10000) maintenanceNeeded = true;
        System.out.printf("CargoShip %s: Sailing with cargo for %.2f km. Fuel used: %.2f L%n", getID(), distance, needed);
    }

    @Override public double calculateFuelEfficiency() {
        if (hasSail()) return 0.0;
        else return 4.0;
    }

    // CargoCarrier
    @Override public void loadCargo(double weight) throws OverloadException, InvalidOperationException {
        if (weight < 0) throw new InvalidOperationException("Invalid weight");
        if (currentCargo + weight > cargoCapacity) throw new OverloadException("Ship overload");
        currentCargo += weight;
    }
    @Override public void unloadCargo(double weight) throws InvalidOperationException {
        if (weight < 0 || weight > currentCargo) throw new InvalidOperationException("Invalid unload weight");
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
        System.out.println("CargoShip " + getID() + " maintenance performed.");
    }

  @Override
    public void refuel(double amount) throws InvalidOperationException {
        if (hasSail()) throw new InvalidOperationException("This ship is sail-powered, cannot refuel.");
        if (amount <= 0) throw new InvalidOperationException("Refuel amount must be > 0");
        fuelLevel += amount;
    }

    @Override
    public double getFuelLevel() {
        return hasSail() ? 0.0 : fuelLevel;
    }

    @Override
    public double consumeFuel(double distance) throws InsufficientFuelException, InvalidOperationException {
        if (hasSail()) throw new InvalidOperationException("This ship is sail-powered.");
        double eff = calculateFuelEfficiency();
        double needed = distance / eff;
        if (needed > fuelLevel) throw new InsufficientFuelException("Not enough fuel for cargo ship " + getID());
        fuelLevel -= needed;
        return needed;
    }
}

