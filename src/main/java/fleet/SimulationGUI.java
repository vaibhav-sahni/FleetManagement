package fleet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * Simple Swing GUI to control and observe the Simulation.
 * - Start unsynchronised or synchronised
 * - Pause / Resume / Stop
 * - Show highway counter and per-vehicle status
 * - Refuel a selected vehicle
 *
 * This GUI updates every second using a Swing Timer and calls into the
 * Simulation API implemented in `fleet.Simulation`.
 */
public class SimulationGUI {

    private final Simulation sim = new Simulation();
    private final JFrame frame = new JFrame("Fleet Highway Simulator");
    private final DefaultTableModel tableModel = new DefaultTableModel(new Object[] {"ID","Mileage","Fuel","Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable statusTable = new JTable(tableModel);
    private final JLabel highwayLabel = new JLabel("Highway Distance: 0");
    private final JLabel elapsedLabel = new JLabel("Elapsed: 0s");
    private final JComboBox<String> vehicleSelector = new JComboBox<>();
    private final JSpinner refuelSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.1, 10000.0, 1.0));

    private Timer updateTimer;

    public SimulationGUI() {
        buildUI();
    }

    private void buildUI() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            // If the requested look and feel is not available, fall back to the default.
            System.err.println("Warning: unable to set look and feel: " + ex.getMessage());
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 520));
        frame.setMinimumSize(new Dimension(1000, 520));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JButton startUnsync = new JButton("Start (unsynchronised)");
        JButton startSync = new JButton("Start (synchronised)");
        JButton pause = new JButton("Pause");
        JButton resume = new JButton("Resume");
        JButton stop = new JButton("Stop");

        // Refuel controls moved to their own panel to ensure visibility
        JButton refuel = new JButton("Refuel Selected");

        JButton reset = new JButton("Reset");
        reset.setEnabled(true);
        reset.setFocusable(false);

        controlPanel.add(startUnsync);
        controlPanel.add(startSync);
        controlPanel.add(pause);
        controlPanel.add(resume);
        controlPanel.add(stop);
        controlPanel.add(reset);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        highwayLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        infoPanel.add(highwayLabel);
        infoPanel.add(elapsedLabel);
        topPanel.add(infoPanel, BorderLayout.SOUTH);

        statusTable.setFillsViewportHeight(true);
        // set preferred column widths for consistent formatting
        if (statusTable.getColumnModel().getColumnCount() >= 4) {
            statusTable.getColumnModel().getColumn(0).setPreferredWidth(140);
            statusTable.getColumnModel().getColumn(1).setPreferredWidth(80);
            statusTable.getColumnModel().getColumn(2).setPreferredWidth(80);
            statusTable.getColumnModel().getColumn(3).setPreferredWidth(240);
        }

        JScrollPane scroll = new JScrollPane(statusTable);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);

        // Refuel panel (separate so controls remain visible)
        JPanel refuelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        refuelPanel.setBorder(BorderFactory.createTitledBorder("Refuel Vehicle"));
        refuelPanel.add(new JLabel("Refuel amt:"));
        refuelPanel.add(refuelSpinner);
        refuelPanel.add(new JLabel("Vehicle:"));
        refuelPanel.add(vehicleSelector);
        refuelPanel.add(refuel);
        // Per-vehicle controls: pause/resume
        JButton pauseVehicleBtn = new JButton("Pause Vehicle");
        JButton resumeVehicleBtn = new JButton("Resume Vehicle");
        refuelPanel.add(pauseVehicleBtn);
        refuelPanel.add(resumeVehicleBtn);
        // add a backup reset button to ensure visibility
        JButton resetBottom = new JButton("Reset");
        resetBottom.setEnabled(true);
        resetBottom.setFocusable(false);
        refuelPanel.add(resetBottom);
        frame.getContentPane().add(refuelPanel, BorderLayout.SOUTH);

        // Actions
        startUnsync.addActionListener(e -> {
            sim.startSimulation(false);
            JOptionPane.showMessageDialog(frame, "Started simulation (unsynchronised)");
            startUpdater();
        });

        startSync.addActionListener(e -> {
            sim.startSimulation(true);
            JOptionPane.showMessageDialog(frame, "Started simulation (synchronised)");
            startUpdater();
        });

        pause.addActionListener(e -> sim.pauseSimulation());
        resume.addActionListener(e -> sim.resumeSimulation());
        stop.addActionListener(e -> {
            sim.stopSimulation();
            stopUpdater();
        });

        ActionListener resetAction = e -> {
            sim.resetSimulation();
            refreshStatus();
            JOptionPane.showMessageDialog(frame, "Simulation reset: highway distance and time set to 0, vehicles refuelled.");
        };
        reset.addActionListener(resetAction);
        resetBottom.addActionListener(resetAction);

        refuel.addActionListener(e -> {
            String id = (String) vehicleSelector.getSelectedItem();
            if (id == null) {
                JOptionPane.showMessageDialog(frame, "Select a vehicle first.");
                return;
            }
            double amt = ((Number) refuelSpinner.getValue()).doubleValue();
            sim.refuelVehicle(id, amt);
        });

        // Pause/Resume individual vehicles
        pauseVehicleBtn.addActionListener(e -> {
            String id = (String) vehicleSelector.getSelectedItem();
            if (id == null) {
                JOptionPane.showMessageDialog(frame, "Select a vehicle first.");
                return;
            }
            sim.pauseVehicle(id);
            refreshStatus();
        });

        resumeVehicleBtn.addActionListener(e -> {
            String id = (String) vehicleSelector.getSelectedItem();
            if (id == null) {
                JOptionPane.showMessageDialog(frame, "Select a vehicle first.");
                return;
            }
            sim.resumeVehicle(id);
            refreshStatus();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    private void startUpdater() {
        if (updateTimer != null && updateTimer.isRunning()) return;
        // populate immediately then start regular updates
        refreshStatus();
        updateTimer = new Timer(1000, e -> refreshStatus());
        updateTimer.start();
    }

    private void stopUpdater() {
        if (updateTimer != null) updateTimer.stop();
        updateTimer = null;
        // clear GUI indicators (optional)
        SwingUtilities.invokeLater(() -> {
            highwayLabel.setText("Highway Distance: " + sim.getHighwayDistance());
            elapsedLabel.setText("Elapsed: " + sim.getElapsedSeconds() + "s");
            tableModel.setRowCount(0);
            vehicleSelector.removeAllItems();
        });
    }

    private void refreshStatus() {
        java.util.List<String[]> rows = sim.getVehicleTableSnapshot();
        SwingUtilities.invokeLater(() -> {
            // preserve the user's selection across refreshes so they can click
            // pause/resume reliably without the timer overwriting the choice.
            String selected = (String) vehicleSelector.getSelectedItem();

            tableModel.setRowCount(0);
            vehicleSelector.removeAllItems();
            for (String[] r : rows) {
                tableModel.addRow(r);
                vehicleSelector.addItem(r[0]);
            }

            // restore selection if it still exists
            if (selected != null) {
                vehicleSelector.setSelectedItem(selected);
            }

            highwayLabel.setText("Highway Distance: " + sim.getHighwayDistance());
            elapsedLabel.setText("Elapsed: " + sim.getElapsedSeconds() + "s");
        });
    }

    public void show() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    public static void main(String[] args) {
        SimulationGUI gui = new SimulationGUI();
        gui.show();
    }
}
