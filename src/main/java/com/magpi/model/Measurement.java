package com.magpi.model;

/**
 * Represents a single measurement reading from the meters
 */
public class Measurement {
    private double current;
    private double duration;
    private String meterType; // "Headshot" or "Coilshot"
    
    public Measurement(String meterType, double current, double duration) {
        this.meterType = meterType;
        this.current = current;
        this.duration = duration;
    }
    
    public double getCurrent() {
        return current;
    }
    
    public double getDuration() {
        return duration;
    }
    
    public String getMeterType() {
        return meterType;
    }
    
    @Override
    public String toString() {
        return String.format("%s: Current=%.2f, Duration=%.3f", meterType, current, duration);
    }
} 