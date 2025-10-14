package vehicles;

import exceptions.InvalidOperationException;

public abstract class LandVehicle extends Vehicle{
    private int numWheels;
    public LandVehicle(String id, String model, double maxSpeed, int numWheels) throws InvalidOperationException{
        super(id,model,maxSpeed);
        this.numWheels = numWheels;
    }
    public int getNumWheels(){
        return numWheels;
    }
    @Override
    public double estimateJourneyTime(double distance) throws InvalidOperationException {
        if (distance < 0) throw new InvalidOperationException("Distance must be > 0");
        if (this.getMaxSpeed() <= 0) throw new InvalidOperationException("MaxSpeed must be > 0");
        double base = distance / this.getMaxSpeed();
        return base * 1.1;
    }
}


