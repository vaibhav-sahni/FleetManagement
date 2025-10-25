package vehicles;

import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;
import exceptions.OverloadException;
import interfaces.FuelConsumable;
import interfaces.Maintainable;
import interfaces.PassengerCarrier;

public class Car extends LandVehicle implements FuelConsumable,PassengerCarrier,Maintainable {
    private double fuelLevel = 0.0;
    private final int passengerCapacity = 5;
    private int currentPassengers = 0;
    private boolean maintenanceNeeded = false;
    protected double lastMaintenanceMileage = 0;

    public Car(String id, String model, double maxSpeed, int numWheels) throws InvalidOperationException {
        super(id, model, maxSpeed, numWheels);
    }

    @Override
    public void move(double distance) throws InvalidOperationException, InsufficientFuelException {
        // Prevent movement if maintenance is required
        if (needsMaintenance()) throw new InvalidOperationException("Vehicle " + getID() + " requires maintenance and cannot move.");
        if (distance < 0) throw new InvalidOperationException("Distance must be > 0");
        double requiredFuel = distance / calculateFuelEfficiency();
        if (requiredFuel > fuelLevel) throw new InsufficientFuelException("Not enough fuel for car " + getID());
        fuelLevel -= requiredFuel;
        addMileage(distance);
        if (getCurrentMileage() > 10000) maintenanceNeeded = true;
        System.out.printf("Car %s: Driving on road for %.2f km. Fuel used: %.2f L\n", getID(), distance, requiredFuel);
    }

    @Override 
    public double calculateFuelEfficiency() { 
        return 15.0; 
    }
    

    //Methods of FuelConsumable
    @Override 
    public void refuel(double amount) throws InvalidOperationException {
        if (amount <= 0) throw new InvalidOperationException("Refuel amount must be > 0");
        this.fuelLevel += amount;
    }

    @Override
    public double getFuelLevel(){
        return this.fuelLevel;
    }

    @Override
    public double consumeFuel(double distance) throws InsufficientFuelException{
        double requiredFuel = distance / calculateFuelEfficiency();
        if (requiredFuel > fuelLevel) throw new InsufficientFuelException("Insufficient fuel");
        fuelLevel -= requiredFuel;
        return requiredFuel;
    }

    //Methods of PassengerCarrier
    @Override
    public void boardPassengers(int count) throws OverloadException{
        if (count < 0) return;
        if (currentPassengers + count > passengerCapacity) throw new OverloadException("Car overload");
        currentPassengers += count;
    }
    @Override 
    public void disembarkPassengers(int count) throws InvalidOperationException {
        if (count > currentPassengers) throw new InvalidOperationException("Cannot disembark more than the current number of Passengers.");
        currentPassengers -= count;
    }
    @Override 
    public int getPassengerCapacity() { 
        return passengerCapacity; 
    }
    @Override 
    public int getCurrentPassengers() { 
        return currentPassengers; 
    }

    //Methods of Maintainable
    @Override public void scheduleMaintenance() { 
        maintenanceNeeded = true; 
    }
    @Override public boolean needsMaintenance() { 
        return maintenanceNeeded || (getCurrentMileage() - lastMaintenanceMileage >= 10000);
    }
    @Override public void performMaintenance() {
        lastMaintenanceMileage = getCurrentMileage();
        maintenanceNeeded = false;
        System.out.println("Car " + getID() + " maintenance done.");
    }
}

