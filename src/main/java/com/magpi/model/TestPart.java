package com.magpi.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a part being tested with its measurements and status
 */
public class TestPart {
    private int partNumber;
    private String partDescription;
    private LocalDateTime testTime;
    private List<Measurement> headshotMeasurements;
    private List<Measurement> coilshotMeasurements;
    private String status; // "Accept" or "Reject"
    
    public TestPart(int partNumber, String partDescription) {
        this.partNumber = partNumber;
        this.partDescription = partDescription;
        this.testTime = LocalDateTime.now();
        this.headshotMeasurements = new ArrayList<>();
        this.coilshotMeasurements = new ArrayList<>();
        this.status = "";
    }
    
    public void addHeadshotMeasurement(Measurement measurement) {
        headshotMeasurements.add(measurement);
    }
    
    public void addCoilshotMeasurement(Measurement measurement) {
        coilshotMeasurements.add(measurement);
    }
    
    public int getPartNumber() {
        return partNumber;
    }
    
    public String getPartDescription() {
        return partDescription;
    }
    
    public LocalDateTime getTestTime() {
        return testTime;
    }
    
    public List<Measurement> getHeadshotMeasurements() {
        return headshotMeasurements;
    }
    
    public List<Measurement> getCoilshotMeasurements() {
        return coilshotMeasurements;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Gets a specific headshot measurement by index
     * @param index The index of the measurement to retrieve (0-based)
     * @return The measurement or null if out of bounds
     */
    public Measurement getHeadshotMeasurement(int index) {
        if (index >= 0 && index < headshotMeasurements.size()) {
            return headshotMeasurements.get(index);
        }
        return null;
    }
    
    /**
     * Gets a specific coilshot measurement by index
     * @param index The index of the measurement to retrieve (0-based)
     * @return The measurement or null if out of bounds
     */
    public Measurement getCoilshotMeasurement(int index) {
        if (index >= 0 && index < coilshotMeasurements.size()) {
            return coilshotMeasurements.get(index);
        }
        return null;
    }
    
    /**
     * Gets the highest current value from headshot measurements
     */
    public double getHighestHeadshotCurrent() {
        return headshotMeasurements.stream()
                .mapToDouble(Measurement::getCurrent)
                .max()
                .orElse(0);
    }
    
    /**
     * Gets the highest current value from coilshot measurements
     */
    public double getHighestCoilshotCurrent() {
        return coilshotMeasurements.stream()
                .mapToDouble(Measurement::getCurrent)
                .max()
                .orElse(0);
    }
} 