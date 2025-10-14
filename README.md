FleetManagement - Assignment 2

Overview
This project is a Fleet Management system that stores vehicles (Car, Truck, Bus, Airplane, CargoShip) and provides operations to add/remove, start journeys, refuel, load/unload cargo, manage passengers, sort/query, and persist fleet data to CSV.

How to compile and run
- This is a Maven project. From project root run:

```bash
mvn compile
mvn exec:java -Dexec.mainClass=Main
```

Or compile/run with javac/java (simple):
```bash
cd src/main/java
javac Main.java
java Main
```

Collections used
- ArrayList<Vehicle> (in `fleet.FleetManager`) to store fleet dynamically and allow iteration, sorting, and removals.
- HashSet<String> (modelSet) to maintain distinct vehicle model names and prevent duplicates.
- TreeSet<String> is used on-demand to present an alphabetically ordered set of distinct models.

File I/O / Persistence
- Fleet is saved to `fleetdata.csv` in CSV format by `fleet.Persistence.saveFleet(...)`.
- Loader `fleet.Persistence.loadFleet()` reads `fleetdata.csv`, skips comment lines, and gracefully handles malformed lines by logging and continuing.
- A sample `fleetdata.csv` is included in the repository root.

Sample run
- Start program and choose menu options 7 (Save Fleet) and 8 (Load Fleet) to persist/load data.
- Use the search and sort options to view sorted lists and fastest/slowest vehicles.

Notes
- The project builds with Maven; ensure you have Java 17+ and Maven installed.
- Persistence uses relative file path `fleetdata.csv` (working directory) so ensure you run the program from the project root or adjust the path.

Design rationale
- ArrayList chosen for main storage because we need ordered, indexable, and resizable collection with efficient iteration. HashSet is used to demonstrate uniqueness handling for model names, and TreeSet (on demand) for automatic alphabetical ordering of models.

Contact
For questions, inspect the source files under `src/main/java`.
