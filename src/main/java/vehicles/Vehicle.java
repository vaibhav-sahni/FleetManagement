package vehicles;
import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;

public abstract class Vehicle implements Comparable<Vehicle>{
    private String id;
    private String model;
    private double maxSpeed;
    private double currentMileage;

    public Vehicle(String id, String model, double maxSpeed) throws InvalidOperationException{
        if(id == null){
            throw new InvalidOperationException("ID cannot be empty!");
        }
        this.id = id;
        this.model = model;
        this.maxSpeed = maxSpeed;
        this.currentMileage = 0.0;
    }

    public abstract void move(double distance) throws InvalidOperationException,InsufficientFuelException;
    public abstract double calculateFuelEfficiency();
    public abstract double estimateJourneyTime(double distance) throws InvalidOperationException;

    public void displayInfo(){
        System.out.printf("ID: %s, Model: %s, Max Speed: %.2f km/h, Current Mileage: %.2f km\n", id, model, maxSpeed, currentMileage);
    }
    
    public double getCurrentMileage(){
        return this.currentMileage;
    }

    public String getID(){
        return this.id;
    }
    

    public double getMaxSpeed(){
        return this.maxSpeed;
    }

    public String getModel() {
    return this.model;
    }
    public void addMileage(double d){ //cant be protected...needed in persistence.java
        if (d>0){
            this.currentMileage+=d;
        }
    }

    @Override
     public int compareTo(Vehicle other) {
        return Double.compare(this.calculateFuelEfficiency(), other.calculateFuelEfficiency());
    }
}
