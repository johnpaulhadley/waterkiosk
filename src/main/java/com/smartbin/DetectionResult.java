package com.smartbin;

import org.opencv.core.Rect;

/**
 * Detection result from YOLO model.
 * Returned by YoloBridge.detect()
 */
public class DetectionResult {
    
    private final String label;
    private final float confidence;
    private final Rect boundingBox;

    public DetectionResult(String label, float confidence, Rect boundingBox) {
        this.label = label;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public String getLabel() {
        return label;
    }

    public float getConfidence() {
        return confidence;
    }

    public Rect getBoundingBox() {
        return boundingBox;
    }

    public String getConfidencePercentage() {
        return String.format("%.0f%%", confidence * 100);
    }

    public boolean isConfident(double threshold) {
        return confidence >= threshold;
    }

    public String getStatusMessage(double threshold) {
        if (confidence < threshold) {
            return String.format("Low confidence (%s). Please try again.", getConfidencePercentage());
        }
        return String.format("%s detected (%s). Place in %s bin.", 
            capitalize(label), 
            getConfidencePercentage(),
            getBinType());
    }

    public String getBinInstruction() {
        return "Place in " + getBinType() + " bin.";
    }

    private String getBinType() {
        String lower = label.toLowerCase();
        if (lower.contains("bottle") || lower.contains("can")) {
            return "recycle";
        }
        if (lower.contains("food")) {
            return "compost";
        }
        if (lower.contains("battery")) {
            return "hazardous";
        }
        return "trash";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public String toString() {
        return String.format("DetectionResult{label='%s', confidence=%.2f}", label, confidence);
    }
}