package fleet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;
import exceptions.OverloadException;
import interfaces.CargoCarrier;
import interfaces.FuelConsumable;
import interfaces.Maintainable;
import interfaces.PassengerCarrier;
import vehicles.Vehicle;

public class FleetManager {
    private List<Vehicle> fleet = new ArrayList<>();

    // Keep a HashSet of model names for uniqueness demonstration (distinct models)
    // TreeSet views can be created on demand for sorted order.
    private java.util.Set<String> modelSet = new java.util.HashSet<>();

    public void addVehicle(Vehicle v) throws InvalidOperationException{
        for(Vehicle x : fleet){
            if(x.getID().equals(v.getID())){
                throw new InvalidOperationException("Vehicle ID must be unique");
            }
        }
        fleet.add(v);
        if (v.getModel() != null) modelSet.add(v.getModel());
    }

    public void removeVehicle(String id) throws InvalidOperationException{
        Iterator<Vehicle> it = fleet.iterator();
        while(it.hasNext()){
            if(it.next().getID().equals(id)){
                it.remove();
                System.out.printf("Vehicle with ID %s removed.\n", id);
                return;
            }
        }
        throw new InvalidOperationException(String.format("Vehicle with ID %s not found",id));
    }

    public void startAllJourneys(double distance){
        for (Vehicle v : fleet){
            try {
                v.move(distance);
            } catch (InsufficientFuelException e) {
                System.out.println("Insufficient fuel in " + v.getID() + ": " + e.getMessage());
            } catch (InvalidOperationException e) {
                System.out.println("Invalid operation in " + v.getID() + ": " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error occured while moving " + v.getID() + ": " + e.getMessage());
            }
        }
    }

    public void startJourney(String id, double distance){
        for (Vehicle v : fleet){
            if (v.getID().equals(id)){
                try {
                    v.move(distance);
                    System.out.printf("Vehicle %s moved %.2f km.\n", id, distance);
                    return;
                } catch (InsufficientFuelException e) {
                    System.out.println("Insufficient fuel in " + v.getID() + ": " + e.getMessage());
                    return;
                } catch (InvalidOperationException e) {
                    System.out.println("Invalid operation in " + v.getID() + ": " + e.getMessage());
                    return;
                } catch (Exception e) {
                    System.out.println("Error occured while moving " + v.getID() + ": " + e.getMessage());
                    return;
                }
            }
        }
        System.out.printf("Vehicle with ID %s not found.\n", id);
    }

    public void refuelAll(double amount) { //Included in CLI but not in Fleetmanager documentation
    for (Vehicle v : fleet) {
        if (v instanceof FuelConsumable f) {
            try {
                f.refuel(amount);
            } catch (InvalidOperationException e) {
                System.out.println("Refuel failed for " + v.getID() + ": " + e.getMessage());
            }
        }
    }
}
    public void refuelVehicle(String id, double amount){  
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof FuelConsumable f) {
                try {
                    f.refuel(amount);
                    System.out.println("Refueled " + id + " with " + amount + " liters.");
                    return;
                } catch (InvalidOperationException e) {
                    System.out.println("Refuel failed for " + v.getID() + ": " + e.getMessage());
                    return;
                }
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or is not fuel consumable.");

    }

    public void loadCargo(String id, double weight){
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof CargoCarrier c) {
                try {
                    c.loadCargo(weight);
                    System.out.println("Loaded " + weight + " kg into " + id);
                    return;
                } catch (InvalidOperationException e) {
                    System.out.println("Load cargo failed for " + v.getID() + ": " + e.getMessage());
                    return;
                } catch (OverloadException e) {
                    System.out.println("Overload error for " + v.getID() + ": " + e.getMessage());
                    return;
                    
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or not of cargocarrier type.");
    }
    }

    public void unloadCargo(String id, double weight){
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof CargoCarrier c) {
                try {
                    c.unloadCargo(weight);
                    System.out.println("Unloaded " + weight + " kg from " + id);
                    return;
                } catch (InvalidOperationException e) {
                    System.out.println("Unload cargo failed for " + v.getID() + ": " + e.getMessage());
                    return;
                }
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or not of cargocarrier type.");
    }

    public void displayCargoStatus(String id){
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof CargoCarrier c) {
                System.out.printf("Vehicle %s: Current Cargo = %.2f kg, Capacity = %.2f kg\n", id, c.getCurrentCargo(), c.getCargoCapacity());
                return;
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or not of cargocarrier type.");
    }

    public void addPassengers(String id, int count){
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof PassengerCarrier p) {
                try {
                    p.boardPassengers(count);
                    System.out.println("Boarded " + count + " passengers into " + id);
                    return;
                } catch (OverloadException e) {
                    System.out.println("Overload error for " + v.getID() + ": " + e.getMessage());
                    return;
                }
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or not of PassengerCarrier type.");
    }

    public void removePassengers(String id, int count){
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof PassengerCarrier p) {
                try {
                    p.disembarkPassengers(count);
                    System.out.println("Disembarked " + count + " passengers from " + id);
                    return;
                } catch (InvalidOperationException e) {
                    System.out.println("Disembark failed for " + v.getID() + ": " + e.getMessage());
                    return;
                }
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or not of PassengerCarrier type.");
    }

    public void displayPassengerStatus(String id){
        for (Vehicle v : fleet) {
            if (v.getID().equals(id) && v instanceof PassengerCarrier p) {
                System.out.printf("Vehicle %s: Current Passengers = %d, Capacity = %d\n", id, p.getCurrentPassengers(), p.getPassengerCapacity());
                return;
            }
        }
        System.out.println("Vehicle with ID " + id + " not found or not of PassengerCarrier type.");
    }

    double getTotalFuelConsumption(double distance){
        double total = 0.0;
        for(Vehicle v: fleet){
            if (v instanceof FuelConsumable){
                double eff = v.calculateFuelEfficiency();
                if (eff>0) total+= distance/eff;
            }
        }
        return total;
    }

    public void maintainAll() {
        for (Vehicle v : fleet) {
            if (v instanceof Maintainable m) {
                if (m.needsMaintenance()) m.performMaintenance();
            }
        }
    }

    public List<Vehicle> searchByType(Class<?> type) {
        List<Vehicle> out = new ArrayList<>();
        for (Vehicle v : fleet){
         if (type.isInstance(v)) out.add(v);
        }
        return out;
    }

    public void sortFleetByEfficiency(){
        Collections.sort(fleet);
    }

    public List<Vehicle> getVehiclesNeedingMaintenance() {
        List<Vehicle> list = new ArrayList<>();
        for (Vehicle v : fleet) {
            if (v instanceof Maintainable m) {
                if (m.needsMaintenance()) list.add(v);
            }
        }
        return list;
    }

     public void loadFleet() {
        fleet = Persistence.loadFleet(); // overwrite with loaded list
        // Rebuild model set after loading
        modelSet.clear();
        for (Vehicle v : fleet) if (v.getModel() != null) modelSet.add(v.getModel());
    }

    public List<Vehicle> getAll() {
        // Return a defensive copy to avoid external modification
        return new ArrayList<>(fleet);
    }

    /**
     * Return a sorted list (by model name) of distinct model names using a TreeSet.
     */
    public java.util.List<String> getDistinctModelsSorted() {
        java.util.TreeSet<String> t = new java.util.TreeSet<>(modelSet);
        return new java.util.ArrayList<>(t);
    }

    /**
     * Return the vehicle with maximum maxSpeed. Returns null if fleet empty.
     */
    public Vehicle getFastestVehicle() {
        if (fleet.isEmpty()) return null;
        return Collections.max(fleet, java.util.Comparator.comparingDouble(Vehicle::getMaxSpeed));
    }

    /**
     * Return the vehicle with minimum maxSpeed. Returns null if fleet empty.
     */
    public Vehicle getSlowestVehicle() {
        if (fleet.isEmpty()) return null;
        return Collections.min(fleet, java.util.Comparator.comparingDouble(Vehicle::getMaxSpeed));
    }

    /**
     * Sort fleet in-place by model name (lexicographical) and return a copy of sorted list.
     */
    public java.util.List<Vehicle> getFleetSortedByModel() {
        java.util.List<Vehicle> copy = new java.util.ArrayList<>(fleet);
        copy.sort(java.util.Comparator.comparing(Vehicle::getModel, java.util.Comparator.nullsFirst(String::compareTo)));
        return copy;
    }

    /**
     * Sort fleet by maxSpeed and return a new list (fastest first).
     */
    public java.util.List<Vehicle> getFleetSortedBySpeedDesc() {
        java.util.List<Vehicle> copy = new java.util.ArrayList<>(fleet);
        copy.sort(java.util.Comparator.comparingDouble(Vehicle::getMaxSpeed).reversed());
        return copy;
    }

    public String generateReport(){
        StringBuilder sb = new StringBuilder();
        sb.append("----FLEET REPORT----\n");
        sb.append("Total vehicles: ").append(fleet.size()).append("\n");

        Map<String, Integer> vtypes = new LinkedHashMap<>();
        double totalEff = 0.0;
        int effCount = 0;
        double totalMileage = 0.0;

        for (Vehicle v : fleet) {
            String key = v.getClass().getSimpleName(); //Turns Class.class to Class
            vtypes.put(key, vtypes.getOrDefault(key, 0) + 1); //Increment the count

            double eff = v.calculateFuelEfficiency();
            if (eff > 0) { 
                totalEff += eff; 
                effCount++; 
            }
            totalMileage += v.getCurrentMileage();
        }
        sb.append("Count by types: \n");
        for (var e : vtypes.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n"); // Type : count
        }

        sb.append("Average fuel efficiency: ")
          .append(effCount > 0 ? String.format("%.2f km/l", totalEff / effCount) : "N/A")
          .append("\n");
        sb.append("Total mileage: ").append(String.format("%.2f km", totalMileage)).append("\n");

        List<Vehicle> needMaint = getVehiclesNeedingMaintenance();
        sb.append("Vehicles needing maintenance: ").append(needMaint.size()).append("\n");
        return sb.toString();

    }

}
