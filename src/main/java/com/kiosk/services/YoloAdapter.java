package com.kiosk.services;

import com.kiosk.models.WasteCategory;
import com.smartbin.DetectionResult;
import com.smartbin.yolo.YoloBridge;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.List;

public class YoloAdapter {
    
    // Load OpenCV native library
    static {
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("[YoloAdapter] OpenCV loaded successfully");
        } catch (Exception e) {
            System.err.println("[YoloAdapter] Failed to load OpenCV: " + e.getMessage());
        }
    }
    
    private YoloBridge bridge;
    private Mat frame;
    private static final double CONFIDENCE_THRESHOLD = 0.5;
    
    /**
     * Initialize YOLO adapter with model.
     */
    public YoloAdapter() {
        try {
            bridge = new YoloBridge("model/YOLO/best.onnx");
            frame = new Mat();
            
            if (!bridge.isCameraOpen()) {
                System.err.println("[YoloAdapter] Warning: Camera not opened");
            }
            
            System.out.println("[YoloAdapter] Initialized successfully");
        } catch (Exception e) {
            System.err.println("[YoloAdapter] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            bridge = null;
            frame = null;
        }
    }
    
    /**
     * Detect object and map to waste category.
     * Returns null if no detection or confidence too low.
     * 
     * Task #414: Maps YOLO classification categories to disposal instructions
     */
    public WasteCategory detectCategory() {
        if (bridge == null || frame == null) {
            return null;
        }
        
        try {
            // Read frame from webcam (pass Mat to readFrame)
            boolean frameRead = bridge.readFrame(frame);
            
            if (!frameRead || frame.empty()) {
                return null; // No frame available
            }
            
            // Run YOLO detection (pass Mat to detect)
            List<DetectionResult> detections = bridge.detect(frame);
            
            if (detections == null || detections.isEmpty()) {
                return null; // No detections
            }
            
            // Get best detection
            DetectionResult best = bridge.getBestResult(detections);
            
            if (best == null || best.getConfidence() < CONFIDENCE_THRESHOLD) {
                return null; // Low confidence
            }
            
            // Map YOLO label to WasteCategory
            return mapLabelToCategory(best.getLabel());
            
        } catch (Exception e) {
            System.err.println("[YoloAdapter] Detection error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Task #414: Map YOLO classification label to WasteCategory.
     * 
     * Adjust these mappings based on your YOLO model's actual class labels.
     */
    private WasteCategory mapLabelToCategory(String label) {
        if (label == null) {
            return WasteCategory.TRASH;
        }
        
        String lower = label.toLowerCase();
        
        // Recyclable items
        if (lower.contains("bottle") || 
            lower.contains("can") || 
            lower.contains("aluminum") ||
            lower.contains("plastic") ||
            lower.contains("paper") ||
            lower.contains("cardboard") ||
            lower.contains("glass")) {
            System.out.println("[YoloAdapter] Mapped '" + label + "' to RECYCLE");
            return WasteCategory.RECYCLE;
        }
        
        // Compostable items  
        if (lower.contains("food") || 
            lower.contains("organic") ||
            lower.contains("compost") ||
            lower.contains("banana") ||
            lower.contains("apple")) {
            System.out.println("[YoloAdapter] Mapped '" + label + "' to COMPOST");
            return WasteCategory.COMPOST;
        }
        
        // Hazardous items
        if (lower.contains("battery") || 
            lower.contains("electronic") ||
            lower.contains("hazard") ||
            lower.contains("chemical")) {
            System.out.println("[YoloAdapter] Mapped '" + label + "' to HAZARDOUS");
            return WasteCategory.HAZARDOUS;
        }
        
        // Default to trash
        System.out.println("[YoloAdapter] Mapped '" + label + "' to TRASH (default)");
        return WasteCategory.TRASH;
    }
    
    /**
     * Release resources (camera, model).
     */
    public void close() {
        if (frame != null && !frame.empty()) {
            frame.release();
        }
        
        if (bridge != null) {
            try {
                bridge.close();
                System.out.println("[YoloAdapter] Closed successfully");
            } catch (Exception e) {
                System.err.println("[YoloAdapter] Error closing: " + e.getMessage());
            }
        }
    }
    
    /**
     * For testing: simulate a detection
     */
    public WasteCategory simulateDetection(String itemType) {
        System.out.println("[YoloAdapter] Simulating detection: " + itemType);
        return mapLabelToCategory(itemType);
    }
}


































