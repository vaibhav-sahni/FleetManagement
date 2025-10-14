import java.util.List;
import java.util.Scanner;

import fleet.FleetManager;
import fleet.Persistence;
import vehicles.Airplane;
import vehicles.Bus;
import vehicles.Car;
import vehicles.CargoShip;
import vehicles.Truck;
import vehicles.Vehicle;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static FleetManager fleetManager = new FleetManager();

    private static void addVehicleCLI() {
        System.out.print("Enter vehicle type (Car, Truck, Bus, Airplane, CargoShip): ");
        String type = sc.next().trim();

        System.out.print("Enter ID: ");
        String id = sc.next().trim();
        System.out.print("Enter model: ");
        String model = sc.next().trim();
        System.out.println("Enter max speed (km/h): ");
        double maxSpeed = sc.nextDouble();

        try {
            Vehicle vehicle = switch (type) {
                case "Car" -> {
                    System.out.println("Enter number of wheels: ");
                    int numWheels = sc.nextInt();
                    yield new Car(id, model, maxSpeed, numWheels);
                }
                case "Truck" -> {
                    System.out.println("Enter number of wheels: ");
                    int numWheels = sc.nextInt();
                    yield new Truck(id, model, maxSpeed, numWheels);
                }
                case "Bus" -> {
                    System.out.println("Enter number of wheels: ");
                    int numWheels = sc.nextInt();
                    yield new Bus(id, model, maxSpeed, numWheels);
                }
                case "Airplane" -> {
                    System.out.println("Enter max altitude: ");
                    double maxAltitude = sc.nextDouble();
                    yield new Airplane(id, model, maxSpeed, maxAltitude);
                }
                case "CargoShip" -> {
                    System.out.print("Does it have a sail? (true/false): ");
                    boolean hasSail = Boolean.parseBoolean(sc.next().trim());
                    yield new CargoShip(id, model, maxSpeed, hasSail);
                }
                default -> {
                    System.out.println("Unknown vehicle type.");
                    yield null;
                }
            };

            if (vehicle != null) {
                fleetManager.addVehicle(vehicle);
                System.out.println(type + " added successfully!");
            }
        } catch (Exception e) {
            System.out.println("Error adding vehicle: " + e.getMessage());
        }
    }

    private static void removeVehicleCLI() {
        System.out.print("Enter vehicle ID to remove: ");
        String id = sc.next().trim();
        try {
            fleetManager.removeVehicle(id);
            System.out.println("Vehicle removed successfully.");
        } catch (Exception e) {
            System.out.println("Error removing vehicle: " + e.getMessage());
        }
    }

    private static void startAllJourneyCLI() {
        System.out.println("Enter distance: ");
        double distance = sc.nextDouble();
        fleetManager.startAllJourneys(distance);
    }

    private static void startJourneyCLI() {
        System.out.print("Enter vehicle ID to start journey: ");
        String id = sc.next().trim();
        System.out.println("Enter distance: ");
        double distance = sc.nextDouble();
        fleetManager.startJourney(id, distance);
    }

    private static void refuelAllCLI() {
        System.out.println("Enter fuel amt: ");
        double amount = sc.nextDouble();
        fleetManager.refuelAll(amount);
    }

    private static void searchByTypeCLI() {
        System.out.print("Enter class name to search (Car, Truck, Bus, Airplane, CargoShip): ");
        String type = sc.next().trim();
        try {
            Class<?> se = Class.forName("vehicles." + type);
            List<Vehicle> result = fleetManager.searchByType(se);
            if (result.isEmpty()) {
                System.out.println("No vehicles found of type " + type);
            } else {
                System.out.println("Found " + result.size() + " vehicles:");
                result.forEach(Vehicle::displayInfo);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Invalid class name.");
        }
    }

    private static void listMaintenanceCLI() {
        List<Vehicle> needMaint = fleetManager.getVehiclesNeedingMaintenance();
        if (needMaint.isEmpty()) {
            System.out.println("No vehicles currently need maintenance.");
        } else {
            System.out.println("Vehicles needing maintenance:");
            needMaint.forEach(Vehicle::displayInfo);
        }
    }

    private static void boardPassengersCLI() {
        System.out.print("Enter vehicle ID to board passengers: ");
        String id = sc.next().trim();
        System.out.print("Enter number of passengers to board: ");
        int numPassengers = sc.nextInt();
        fleetManager.addPassengers(id, numPassengers);
    }

    private static void unboardPassengersCLI() {
        System.out.print("Enter vehicle ID to unboard passengers: ");
        String id = sc.next().trim();
        System.out.print("Enter number of passengers to unboard: ");
        int numPassengers = sc.nextInt();
        fleetManager.removePassengers(id, numPassengers);
    }

    private static void displayPassengerStatusCLI() {
        System.out.print("Enter vehicle ID to display passenger status: ");
        String id = sc.next().trim();
        fleetManager.displayPassengerStatus(id);
    }

    private static void loadCargoCLI() {
        System.out.print("Enter vehicle ID to load cargo: ");
        String id = sc.next().trim();
        System.out.print("Enter cargo weight to load: ");
        double weight = sc.nextDouble();
        fleetManager.loadCargo(id, weight);
    }

    private static void unloadCargoCLI() {
        System.out.print("Enter vehicle ID to unload cargo: ");
        String id = sc.next().trim();
        System.out.print("Enter cargo weight to unload: ");
        double weight = sc.nextDouble();
        fleetManager.unloadCargo(id, weight);
    }
    private static void displayCargoStatusCLI() {
        System.out.print("Enter vehicle ID to display cargo status: ");
        String id = sc.next().trim();
        fleetManager.displayCargoStatus(id);
    }

    private static void refuelVehicleCLI() {
        System.out.print("Enter vehicle ID to refuel: ");
        String id = sc.next().trim();
        System.out.print("Enter fuel amount to add: ");
        double amount = sc.nextDouble();
        fleetManager.refuelVehicle(id, amount);
    }

    private static void listDistinctModelsCLI() {
        System.out.println("Distinct models (sorted):");
        var models = fleetManager.getDistinctModelsSorted();
        if (models.isEmpty()) System.out.println("No models available.");
        else models.forEach(System.out::println);
    }

    private static void showFastestSlowestCLI() {
        var fastest = fleetManager.getFastestVehicle();
        var slowest = fleetManager.getSlowestVehicle();
        if (fastest != null) {
            System.out.println("Fastest vehicle:");
            fastest.displayInfo();
        } else System.out.println("No vehicles available");
        if (slowest != null) {
            System.out.println("Slowest vehicle:");
            slowest.displayInfo();
        }
    }

    private static void displayFleetSortedByModelCLI() {
        System.out.println("Fleet sorted by model:");
        var list = fleetManager.getFleetSortedByModel();
        if (list.isEmpty()) System.out.println("No vehicles available");
        else list.forEach(Vehicle::displayInfo);
    }

    private static void displayFleetSortedBySpeedCLI() {
        System.out.println("Fleet sorted by speed (desc):");
        var list = fleetManager.getFleetSortedBySpeedDesc();
        if (list.isEmpty()) System.out.println("No vehicles available");
        else list.forEach(Vehicle::displayInfo);
    }

    
    public static void main(String[] args) {
        boolean exit = false;
        while(!exit){
            System.out.println("\n=== Fleet Management Menu ===");
            System.out.println("1. Add Vehicle");
            System.out.println("2. Remove Vehicle");
            System.out.println("3. Start ALL Journey");
            System.out.println("4. Refuel All");
            System.out.println("5. Perform Maintenance");
            System.out.println("6. Generate Report");
            System.out.println("7. Save Fleet");
            System.out.println("8. Load Fleet");
            System.out.println("9. Search by Type");
            System.out.println("10. List Vehicles Needing Maintenance");
            System.out.println("11. Board Passengers");
            System.out.println("12. Unboard Passengers");  
            System.out.println("13. Display Passenger Status");
            System.out.println("14. Load Cargo");
            System.out.println("15. Unload Cargo");
            System.out.println("16. Display Cargo Status");
            System.out.println("17. Refuel Vehicle");
            System.out.println("18. Start Journey for a Vehicle");
            System.out.println("19. Distinct Models (sorted)");
            System.out.println("20. Fastest & Slowest Vehicle");
            System.out.println("21. Display Fleet sorted by Model");
            System.out.println("22. Display Fleet sorted by Speed");
            System.out.println("23. Exit");


            System.out.println("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> addVehicleCLI();
                case 2 -> removeVehicleCLI();
                case 3 -> startAllJourneyCLI();
                case 4 -> refuelAllCLI();
                case 5 -> fleetManager.maintainAll();
                case 6 -> System.out.println(fleetManager.generateReport());
                case 7 -> Persistence.saveFleet(fleetManager.getAll());
                case 8 -> fleetManager.loadFleet();
                case 9 -> searchByTypeCLI();
                case 10 -> listMaintenanceCLI();
                case 11 -> boardPassengersCLI();
                case 12 -> unboardPassengersCLI();
                case 13 -> displayPassengerStatusCLI();
                case 14 -> loadCargoCLI();
                case 15 -> unloadCargoCLI();
                case 16 -> displayCargoStatusCLI();
                case 17 -> refuelVehicleCLI();
                case 18 -> startJourneyCLI();
                case 19 -> listDistinctModelsCLI();
                case 20 -> showFastestSlowestCLI();
                case 21 -> displayFleetSortedByModelCLI();
                case 22 -> displayFleetSortedBySpeedCLI();
                case 23 -> {
                    System.out.println("Exiting program...");
                    exit = true;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

}
