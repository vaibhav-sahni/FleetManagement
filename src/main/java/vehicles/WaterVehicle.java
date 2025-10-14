package vehicles;
import exceptions.InvalidOperationException;

public abstract class WaterVehicle extends Vehicle {
    private boolean hasSail;
    public WaterVehicle(String id, String model, double maxSpeed, boolean hasSail) throws InvalidOperationException{
        super(id, model, maxSpeed);
        this.hasSail = hasSail;
    }

     public boolean hasSail() { 
        return hasSail; 
    }

    @Override
    public double estimateJourneyTime(double distance) throws InvalidOperationException {
        double base = distance/this.getMaxSpeed();
        return base * 1.15;
    }
}
