package com.magpi.util;

import java.io.*;
import java.util.*;

/**
 * Manages persistent storage of operator names, part descriptions, and part-specific parameters
 */
public class PersistentLibrary {
    private static final String LIBRARY_DIR = System.getProperty("user.home") + "/MagPi/Library";
    private static final String OPERATORS_FILE = LIBRARY_DIR + "/operators.txt";
    private static final String PARTS_FILE = LIBRARY_DIR + "/parts.txt";
    private static final String PARAMETERS_FILE = LIBRARY_DIR + "/parameters.txt";
    private static final String PARAMETER_HISTORY_FILE = LIBRARY_DIR + "/parameter_history.txt";


    private static PersistentLibrary instance;
    private Set<String> operators;
    private Set<String> partDescriptions;
    private Map<String, PartParameters> partParameters;
    private Map<String, List<PartParameters>> partParameterHistory;

    private PersistentLibrary() {
        loadLibrary();
    }

    public static PersistentLibrary getInstance() {
        if (instance == null) {
            instance = new PersistentLibrary();
        }
        return instance;
    }

    private void loadLibrary() {
        // Create library directory if it doesn't exist
        new File(LIBRARY_DIR).mkdirs();

        // Load operators
        operators = new HashSet<>();
        loadFromFile(OPERATORS_FILE, operators);

        // Load part descriptions
        partDescriptions = new HashSet<>();
        loadFromFile(PARTS_FILE, partDescriptions);

        // Load part parameters
        partParameters = new HashMap<>();
        loadParameters();

        // Load parameter history
        partParameterHistory = new HashMap<>();
        loadParameterHistory();
    }

    private void loadFromFile(String filename, Set<String> set) {
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    set.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadParameters() {
        File file = new File(PARAMETERS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String partDesc = parts[0].trim();
                        double headshot = Double.parseDouble(parts[1].trim());
                        double coilshot = Double.parseDouble(parts[2].trim());
                        partParameters.put(partDesc, new PartParameters(headshot, coilshot));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadParameterHistory() {
        File file = new File(PARAMETER_HISTORY_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        String partDesc = parts[0].trim();
                        double headshot = Double.parseDouble(parts[1].trim());
                        double coilshot = Double.parseDouble(parts[2].trim());

                        if (!partParameterHistory.containsKey(partDesc)) {
                            partParameterHistory.put(partDesc, new ArrayList<>());
                        }
                        partParameterHistory.get(partDesc).add(new PartParameters(headshot, coilshot));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToFile(String filename, Set<String> set) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (String item : set) {
                writer.println(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveParameters() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PARAMETERS_FILE))) {
            for (Map.Entry<String, PartParameters> entry : partParameters.entrySet()) {
                writer.printf("%s,%.2f,%.2f%n",
                        entry.getKey(),
                        entry.getValue().headshotThreshold,
                        entry.getValue().coilshotThreshold);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveParameterHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PARAMETER_HISTORY_FILE))) {
            for (Map.Entry<String, List<PartParameters>> entry : partParameterHistory.entrySet()) {
                String partDesc = entry.getKey();
                for (PartParameters params : entry.getValue()) {
                    writer.printf("%s,%.2f,%.2f%n",
                            partDesc,
                            params.headshotThreshold,
                            params.coilshotThreshold);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Operator methods
    public void addOperator(String operator) {
        operators.add(operator);
        saveToFile(OPERATORS_FILE, operators);
    }

    public List<String> getOperators() {
        return new ArrayList<>(operators);
    }

    // Part description methods
    public void addPartDescription(String description) {
        partDescriptions.add(description);
        saveToFile(PARTS_FILE, partDescriptions);
    }

    public List<String> getPartDescriptions() {
        return new ArrayList<>(partDescriptions);
    }

    // Parameter methods
    public void savePartParameters(String partDescription, double headshotThreshold, double coilshotThreshold) {
        // Update current parameters
        partParameters.put(partDescription, new PartParameters(headshotThreshold, coilshotThreshold));
        saveParameters();

        // Add to history
        if (!partParameterHistory.containsKey(partDescription)) {
            partParameterHistory.put(partDescription, new ArrayList<>());
        }
        partParameterHistory.get(partDescription).add(new PartParameters(headshotThreshold, coilshotThreshold));
        saveParameterHistory();
    }

    public PartParameters getPartParameters(String partDescription) {
        return partParameters.get(partDescription);
    }

    public List<PartParameters> getPartParameterHistory(String partDescription) {
        List<PartParameters> history = partParameterHistory.get(partDescription);
        if (history == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    public static class PartParameters {
        private final double headshotThreshold;
        private final double coilshotThreshold;

        public PartParameters(double headshotThreshold, double coilshotThreshold) {
            this.headshotThreshold = headshotThreshold;
            this.coilshotThreshold = coilshotThreshold;
        }

        public double getHeadshotThreshold() {
            return headshotThreshold;
        }

        public double getCoilshotThreshold() {
            return coilshotThreshold;
        }

        @Override
        public String toString() {
            return String.format("Headshot: %.2f, Coilshot: %.2f", headshotThreshold, coilshotThreshold);
        }
    }
}