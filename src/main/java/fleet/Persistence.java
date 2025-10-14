package fleet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import vehicles.Airplane;
import vehicles.Bus;
import vehicles.Car;
import vehicles.CargoShip;
import vehicles.Truck;
import vehicles.Vehicle;

public class Persistence{
    private static final String FILE_NAME = "fleet.csv";

    public static void saveFleet(List<Vehicle> fleet){ //dont hv to create objects to access this method
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
        for (Vehicle v : fleet) {
            switch (v) {
                case Car c -> writer.printf("Car,%s,%s,%.2f,%d,%.2f,%.2f,%b%n",
                            c.getID(), c.getModel(), c.getMaxSpeed(),
                            c.getNumWheels(), c.getFuelLevel(),
                            c.getCurrentMileage(), c.needsMaintenance());
                    case Truck t -> writer.printf("Truck,%s,%s,%.2f,%d,%.2f,%.2f,%.2f,%b%n",
                            t.getID(), t.getModel(), t.getMaxSpeed(),
                            t.getNumWheels(), t.getFuelLevel(),
                            t.getCurrentCargo(), t.getCurrentMileage(),
                            t.needsMaintenance());
                    case Bus b -> writer.printf("Bus,%s,%s,%.2f,%d,%.2f,%d,%d,%.2f,%b%n",
                            b.getID(), b.getModel(), b.getMaxSpeed(),
                            b.getNumWheels(), b.getFuelLevel(),
                            b.getPassengerCapacity(), b.getCurrentPassengers(),
                            b.getCurrentMileage(), b.needsMaintenance());
                    case Airplane a -> writer.printf("Airplane,%s,%s,%.2f,%.2f,%.2f,%d,%d,%.2f,%.2f,%b%n",
                            a.getID(), a.getModel(), a.getMaxSpeed(),
                            a.getMaxAltitude(), a.getFuelLevel(),
                            a.getPassengerCapacity(), a.getCurrentPassengers(),
                            a.getCurrentCargo(), a.getCurrentMileage(),
                            a.needsMaintenance());
                    case CargoShip s -> writer.printf("CargoShip,%s,%s,%.2f,%b,%.2f,%.2f,%.2f,%b%n",
                            s.getID(), s.getModel(), s.getMaxSpeed(),
                            s.hasSail(), s.getFuelLevel(),
                            s.getCurrentCargo(), s.getCurrentMileage(),
                            s.needsMaintenance());
                default -> {
                    continue;
                }
            }
        }
        System.out.println("Fleet saved to " + FILE_NAME);
        }
        catch (IOException e){
            System.out.println("Error saving fleet: " + e.getMessage());
        }
    }

public static List<Vehicle> loadFleet() {
    List<Vehicle> fleet = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            String type = parts[0]; // vehicle type
            try {
                switch (type) {
                    case "Car" -> {
                        //Car,id,model,maxSpeed,numWheels,fuelLevel,currentMileage,needsMaintenance
                        Car c = new Car(parts[1], parts[2],
                                Double.parseDouble(parts[3]),   // maxSpeed
                                Integer.parseInt(parts[4]));    // numWheels
                        if (Double.parseDouble(parts[5]) > 0.0) c.refuel(Double.parseDouble(parts[5])); // fuelLevel
                        c.addMileage(Double.parseDouble(parts[6])); // currentMileage
                        if (Boolean.parseBoolean(parts[7])) c.scheduleMaintenance(); // needsMaintenance
                        fleet.add(c);
                    }
                    case "Truck" -> {
                        //Truck,id,model,maxSpeed,numWheels,fuelLevel,currentCargo,currentMileage,needsMaintenance
                        Truck t = new Truck(parts[1], parts[2],
                                Double.parseDouble(parts[3]),   // maxSpeed
                                Integer.parseInt(parts[4]));    // numWheels
                        if (Double.parseDouble(parts[5]) > 0.0) t.refuel(Double.parseDouble(parts[5])); // fuelLevel
                        t.loadCargo(Double.parseDouble(parts[6])); // currentCargo
                        t.addMileage(Double.parseDouble(parts[7])); // currentMileage
                        if (Boolean.parseBoolean(parts[8])) t.scheduleMaintenance(); // needsMaintenance
                        fleet.add(t);
                    }
                    case "Bus" -> {
                        //Bus,id,model,maxSpeed,numWheels,fuelLevel,passengerCapacity,currentPassengers,currentMileage,needsMaintenance
                        Bus b = new Bus(parts[1], parts[2],
                                Double.parseDouble(parts[3]),   // maxSpeed
                                Integer.parseInt(parts[4]));    // numWheels
                        if (Double.parseDouble(parts[5]) > 0.0) b.refuel(Double.parseDouble(parts[5])); // fuelLevel
                        b.boardPassengers(Integer.parseInt(parts[7])); //currentPassengers
                        b.addMileage(Double.parseDouble(parts[8])); // currentMileage
                        if (Boolean.parseBoolean(parts[9])) b.scheduleMaintenance(); // needsMaintenance
                        fleet.add(b);
                    }
                    case "Airplane" -> {
                        //Airplane,id,model,maxSpeed,maxAltitude,fuelLevel,passengerCapacity,currentPassengers,currentCargo,currentMileage,needsMaintenance
                        Airplane a = new Airplane(parts[1], parts[2],
                                Double.parseDouble(parts[3]),   // maxSpeed
                                Double.parseDouble(parts[4]));  // maxAltitude
                        if (Double.parseDouble(parts[5]) > 0.0) a.refuel(Double.parseDouble(parts[5])); // fuelLevel 
                        a.boardPassengers(Integer.parseInt(parts[7])); // currentPassengers
                        a.loadCargo(Double.parseDouble(parts[8])); // currentCargo
                        a.addMileage(Double.parseDouble(parts[9])); // currentMileage
                        if (Boolean.parseBoolean(parts[10])) a.scheduleMaintenance(); // needsMaintenance
                        fleet.add(a);
                    }
                    case "CargoShip" -> {
                        //CargoShip,id,model,maxSpeed,hasSail,fuelLevel,currentCargo,currentMileage,needsMaintenance
                        CargoShip s = new CargoShip(parts[1], parts[2],
                                Double.parseDouble(parts[3]),   // maxSpeed
                                Boolean.parseBoolean(parts[4])); // hasSail
                        if (!s.hasSail()) {
                            if (Double.parseDouble(parts[5]) > 0.0) s.refuel(Double.parseDouble(parts[5])); // fuelLevel (only if no sail)
                        }
                        s.loadCargo(Double.parseDouble(parts[6])); // currentCargo
                        s.addMileage(Double.parseDouble(parts[7])); // currentMileage
                        if (Boolean.parseBoolean(parts[8])) s.scheduleMaintenance(); // needsMaintenance
                        fleet.add(s);
                    }
                    default -> System.out.println("Unknown type: " + type);
                }
            } catch (Exception e) {
                System.out.println("Error restoring vehicle from line: " + line);
            }
        }
    } catch (IOException e) {
        System.out.println("Error loading fleet: " + e.getMessage());
    }
    return fleet;
}




}
