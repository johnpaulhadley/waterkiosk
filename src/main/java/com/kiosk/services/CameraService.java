package com.kiosk.services;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing live camera feed display.
 * User Story: Maintenance worker wants to see live camera view.
 * 
 * Acceptance Criteria:
 * - Live video feed in designated UI area
 * - Real-time updates with < 500ms delay
 * - Clearly visible detection area
 * - Continuous feed during operation
 * - Error message if camera fails
 */
public class CameraService {
    
    private VideoCapture camera;
    private ScheduledExecutorService executor;
    private ImageView imageView;
    private Runnable onCameraError;
    private boolean isRunning = false;
    
    // Target ~30 FPS for smooth video (33ms between frames)
    private static final int FRAME_DELAY_MS = 33;
    
    /**
     * Initialize camera service.
     */
    public CameraService() {
        // OpenCV should already be loaded by YoloAdapter
    }
    
    /**
     * Start the live camera feed.
     * 
     * @param imageView The ImageView to display the feed
     * @param onError Callback if camera fails
     */
    public void startFeed(ImageView imageView, Runnable onError) {
        this.imageView = imageView;
        this.onCameraError = onError;
        
        // Initialize camera
        camera = new VideoCapture(0);
        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        
        if (!camera.isOpened()) {
            System.err.println("[CameraService] Failed to open camera");
            if (onCameraError != null) {
                Platform.runLater(onCameraError);
            }
            return;
        }
        
        isRunning = true;
        System.out.println("[CameraService] Camera feed started");
        
        // Start frame capture thread
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::captureFrame, 0, FRAME_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Capture and display a single frame.
     */
    private void captureFrame() {
        if (!isRunning || camera == null || !camera.isOpened()) {
            return;
        }
        
        try {
            Mat frame = new Mat();
            
            if (camera.read(frame) && !frame.empty()) {
                // Convert Mat to JavaFX Image
                Image image = matToImage(frame);
                
                // Update ImageView on JavaFX thread
                Platform.runLater(() -> {
                    if (imageView != null) {
                        imageView.setImage(image);
                    }
                });
            }
            
            frame.release();
            
        } catch (Exception e) {
            System.err.println("[CameraService] Frame capture error: " + e.getMessage());
        }
    }
    
    /**
     * Convert OpenCV Mat to JavaFX Image.
     */
    private Image matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
    
    /**
     * Stop the camera feed.
     */
    public void stopFeed() {
        isRunning = false;
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }
        
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        
        System.out.println("[CameraService] Camera feed stopped");
    }
    
    /**
     * Check if camera is running.
     */
    public boolean isRunning() {
        return isRunning && camera != null && camera.isOpened();
    }
    
    /**
     * Check if camera is available.
     */
    public boolean isCameraAvailable() {
        if (camera == null) {
            camera = new VideoCapture(0);
        }
        boolean available = camera.isOpened();
        if (!available) {
            camera.release();
            camera = null;
        }
        return available;
    }
}