package fleet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.InsufficientFuelException;
import exceptions.InvalidOperationException;
import interfaces.FuelConsumable;
import vehicles.Bus;
import vehicles.Car;
import vehicles.Truck;
import vehicles.Vehicle;

public class Simulation implements Runnable {
    // Shared highway distance counter (used to demonstrate race conditions).
    // This value is updated by each vehicle thread as they travel.
    // By default updates are unsynchronised to make the race visible,
    // and can be switched to a synchronized increment strategy.
    public static int highwayDistance = 0; // Step 1: unsynchronised
    // Lock objects used by different synchronization strategies.
    private static final Object highwayLock = new Object();
    private static final ReentrantLock highwayReLock = new ReentrantLock();

    // Use thread-safe collections to make iteration from the GUI safe
    // while lifecycle operations (start/stop/reset) modify the lists.
    private final List<Thread> threads = new CopyOnWriteArrayList<>();
    private final List<VehicleTask> tasks = new CopyOnWriteArrayList<>();
    private final Map<String, String> vehicleStatus = new ConcurrentHashMap<>();

    // control flags
    // `running` indicates whether a simulation session is active
    private volatile boolean running = false;
    // `useSynchronizedIncrement` toggles between unsynchronised and synchronised counter updates
    private volatile boolean useSynchronizedIncrement = false; // toggle for demonstrating race condition
    // `globallyPaused` is true when the simulation has been paused (time tracking uses this)
    private volatile boolean globallyPaused = false;

    // time tracking
    // `startTimeMillis` holds the last resume/start timestamp (ms) when running
    private volatile long startTimeMillis = 0;
    // `accumulatedMillis` stores elapsed time across pause/resume cycles
    private volatile long accumulatedMillis = 0; // accumulated while paused/stopped

    public Simulation() {
        // Initialize a default set of vehicles so the GUI can display them
        // before the simulation is started. prepareVehicles sets up the
        // tasks list but does not start any threads.
        try {
            prepareVehicles();
            // mark prepared vehicles as Ready (threads not running yet)
            for (VehicleTask t : tasks) {
                vehicleStatus.put(t.getVehicle().getID(), "Ready");
            }
        } catch (InvalidOperationException e) {
            System.err.println("Error preparing initial vehicles: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // default run behaviour: start simulation with unsynchronised counter
        startSimulation(false);
    }

    // start simulation; provide whether to use synchronization for highway counter
    public void startSimulation(boolean useSync) {
        // ensure any previous run is stopped and state is cleared
        stopSimulation(); // stop any existing run first
        useSynchronizedIncrement = useSync;
        running = true;
        threads.clear();
        tasks.clear();
        vehicleStatus.clear();

        // reset timing and pause state
        accumulatedMillis = 0;
        startTimeMillis = System.currentTimeMillis();
        globallyPaused = false;

        try {
            // create and initialise vehicles (their tasks are added to `tasks`)
            prepareVehicles();

            // start one thread per vehicle task
            for (VehicleTask t : tasks) {
                Thread th = new Thread(t);
                threads.add(th);
                th.start();
            }

        } catch (InvalidOperationException e) {
            System.err.println("Error creating vehicles: " + e.getMessage());
        }
    }

    // prepare vehicles but do not start threads; used by startSimulation and resetSimulation
    private void prepareVehicles() throws InvalidOperationException {
        threads.clear();
        tasks.clear();
        vehicleStatus.clear();

        Car car = new Car("Car-A", "Sedan", 120.0, 4);
        Bus bus = new Bus("Bus-A", "CityBus", 80.0, 6);
        Truck truck = new Truck("Truck-A", "Hauler", 100.0, 8);

        // initial fuel levels (set sensible defaults for demo)
        if (car instanceof FuelConsumable) ((FuelConsumable) car).refuel(50.0);
        if (bus instanceof FuelConsumable) ((FuelConsumable) bus).refuel(100.0);
        if (truck instanceof FuelConsumable) ((FuelConsumable) truck).refuel(200.0);

        addVehicle(car);
        addVehicle(bus);
        addVehicle(truck);
    }

    // reset simulation: stop, reset counters/time and recreate/refuel vehicles (does not start threads)
    public void resetSimulation() {
        stopSimulation();
        highwayDistance = 0;
        accumulatedMillis = 0;
        startTimeMillis = 0;
        globallyPaused = false;
        try {
            prepareVehicles();
            // set statuses to Ready
            // Mark freshly prepared vehicles as Ready (threads not yet started)
            for (VehicleTask t : tasks) vehicleStatus.put(t.getVehicle().getID(), "Ready");
        } catch (InvalidOperationException e) {
            System.err.println("Error preparing vehicles on reset: " + e.getMessage());
        }
    }

    public void addVehicle(Vehicle v) {
        VehicleTask t = new VehicleTask(v);
        tasks.add(t);
        vehicleStatus.put(v.getID(), "Running");
    }

    public void pauseSimulation() {
        if (!globallyPaused) {
            if (startTimeMillis > 0) accumulatedMillis += System.currentTimeMillis() - startTimeMillis;
            globallyPaused = true;
        }
        for (VehicleTask t : tasks) t.pause();
    }

    public void resumeSimulation() {
        if (globallyPaused) {
            startTimeMillis = System.currentTimeMillis();
            globallyPaused = false;
        }
        for (VehicleTask t : tasks) t.resume();
    }

    public void stopSimulation() {
        // finalize elapsed time
        if (!globallyPaused && startTimeMillis > 0) {
            accumulatedMillis += System.currentTimeMillis() - startTimeMillis;
        }
        startTimeMillis = 0;
        globallyPaused = false;
        running = false;
        for (VehicleTask t : tasks) t.stop();
        for (Thread th : threads) {
            if (th != null && th.isAlive()) th.interrupt();
        }
        threads.clear();
        tasks.clear();
    }

    public void refuelVehicle(String vehicleId, double amount) {
        for (VehicleTask t : tasks) {
            if (t.getVehicle().getID().equals(vehicleId)) {
                // delegate fuel update to the vehicle's task. Do NOT change
                // global simulation state (running/paused) here -- refuelling
                // should not automatically resume or pause the simulation.
                t.refuel(amount);
                return;
            }
        }
    }

    // Pause a specific vehicle by ID. This leaves the rest of the simulation
    // unchanged and only affects the matching vehicle's task.
    public void pauseVehicle(String vehicleId) {
        for (VehicleTask t : tasks) {
            if (t.getVehicle().getID().equals(vehicleId)) {
                t.pause();
                vehicleStatus.put(vehicleId, "Paused");
                return;
            }
        }
    }

    // Resume a specific vehicle by ID. This will wake the vehicle's task if it
    // was paused; it does not change global pause state.
    public void resumeVehicle(String vehicleId) {
        for (VehicleTask t : tasks) {
            if (t.getVehicle().getID().equals(vehicleId)) {
                t.resume();
                vehicleStatus.put(vehicleId, "Running");
                return;
            }
        }
    }

    public int getHighwayDistance() {
        return highwayDistance;
    }

    // elapsed time in seconds
    public long getElapsedSeconds() {
        long elapsed = accumulatedMillis;
        if (startTimeMillis > 0 && !globallyPaused) {
            elapsed += System.currentTimeMillis() - startTimeMillis;
        }
        return elapsed / 1000;
    }

    // return table-friendly snapshot: {id, mileage, fuel, status}
    public java.util.List<String[]> getVehicleTableSnapshot() {
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        for (VehicleTask t : tasks) {
            String id = t.getVehicle().getID();
            String mileage = String.format("%.1f", t.getVehicle().getCurrentMileage());
            String fuel = "-";
            if (t.getVehicle() instanceof FuelConsumable) {
                fuel = String.format("%.1f", ((FuelConsumable) t.getVehicle()).getFuelLevel());
            }
            String status = t.getStatus();
            rows.add(new String[] { id, mileage, fuel, status });
        }
        return rows;
    }

    public Map<String, String> getVehicleStatusSnapshot() {
        Map<String, String> snap = new ConcurrentHashMap<>();
        for (VehicleTask t : tasks) {
            snap.put(t.getVehicle().getID(), t.getStatus());
        }
        return snap;
    }

    // unsynchronised increment (used to demonstrate race condition)
    private void incrementHighwayDistanceUnsync(int delta) {
        // unsynchronised increment used to demonstrate lost updates under concurrency
        highwayDistance += delta;
    }

    // synchronized increment using synchronized block
    private void incrementHighwayDistanceSyncBlock(int delta) {
        // synchronized increment using a monitor lock to make update atomic
        synchronized (highwayLock) { // lock on shared object
            highwayDistance += delta;
        }
    }

    // choose increment method based on configuration
    private void incrementHighwayDistance(int delta) {
        if (!useSynchronizedIncrement) {
            incrementHighwayDistanceUnsync(delta);
        } else {
            // choose one of the sync strategies; using synchronized block here
            incrementHighwayDistanceSyncBlock(delta);
        }
    }

    // Inner class representing the per-vehicle runnable
    private class VehicleTask implements Runnable {
        private final Vehicle vehicle;
        private volatile boolean taskRunning = true;
        private volatile boolean paused = false;
        private final Object pauseLock = new Object();

        private volatile String status = "Initialized";

        public VehicleTask(Vehicle v) {
            this.vehicle = v;
            this.status = "Running";
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public String getStatus() {
            return status;
        }

        public void stop() {
            taskRunning = false;
            resume();
        }

        public void pause() {
            paused = true;
            status = "Paused";
        }

        public void resume() {
            synchronized (pauseLock) {
                paused = false;
                pauseLock.notifyAll();
            }
            status = "Running";
        }

        public void refuel(double amount) {
            if (vehicle instanceof FuelConsumable) {
                try {
                    ((FuelConsumable) vehicle).refuel(amount);
                    // Do NOT change pause/resume state here -- refuelling must not alter
                    // whether the simulation or this vehicle is paused or running.
                    // Do not modify the `status` string here to avoid overwriting
                    // an explicit paused/out-of-fuel state; GUI will pick up the
                    // updated fuel level from the vehicle object when refreshed.
                } catch (InvalidOperationException e) {
                    System.err.println("Refuel failed: " + e.getMessage());
                }
            }
        }

        @Override
        public void run() {
            while (taskRunning) {
                // pause handling
                synchronized (pauseLock) {
                    while (paused) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                // simulate 1 km per second
                try {
                    // If vehicle consumes fuel
                    if (vehicle instanceof FuelConsumable) {
                        try {
                            ((FuelConsumable) vehicle).consumeFuel(1.0);
                            vehicle.addMileage(1.0);
                            incrementHighwayDistance(1);
                            status = String.format("Running (mileage=%.1f, fuel=%.1f)", vehicle.getCurrentMileage(), ((FuelConsumable) vehicle).getFuelLevel());
                        } catch (InsufficientFuelException | InvalidOperationException e) {
                            status = "OutOfFuel";
                            // wait until refuel called
                            synchronized (pauseLock) {
                                paused = true;
                            }
                        }
                    } else {
                        // non fuel consumable: just add mileage
                        vehicle.addMileage(1.0);
                        incrementHighwayDistance(1);
                        status = String.format("Running (mileage=%.1f)", vehicle.getCurrentMileage());
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

}
