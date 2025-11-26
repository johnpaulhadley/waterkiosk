
package com.kiosk.controllers;

import com.kiosk.models.WasteCategory;
import com.kiosk.services.MessageDisplayService;
import com.kiosk.services.YoloAdapter;
import com.kiosk.services.CameraService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the disposal message display with YOLO integration.
 * 
 * User Story: Integrate the UI with the YOLO Machine Learning Model
 * "As a traveler I want the kiosk UI to display the model's live classification 
 * result with clear icon + text, so that I can quickly dispose of my item in 
 * the correct bin."
 * 
 * Tasks Implemented:
 * - Task #412: Wire the inference bridge - Load exported YOLO model, expose results to UI
 * - Task #413: Implement UI states (Scanning, Classified, LowConfidence)
 * - Task #414: Map YOLO classification to disposal instructions
 * 
 * Story #19: Feedback on correct disposal
 * - Display disposal instruction message
 * - After disposal, show green checkmark confirmation if correct
 */
public class DisposalMessageController {

    // Main message display components
    @FXML
    private VBox messageContainer;

    @FXML
    private Label iconLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label instructionLabel;

    // Confirmation display components (Story #19)
    @FXML
    private VBox confirmationContainer;

    @FXML
    private Label checkmarkLabel;

    @FXML
    private Label confirmationLabel;

    // Scanning overlay components (Task #413)
    @FXML
    private StackPane scanningOverlay;

    @FXML
    private Label scanningLabel;

    @FXML
    private Label scanningMessageLabel;
    
    // Model toggle buttons
    @FXML
    private Button chrisModelButton;

    @FXML
    private Button josephModelButton;
    
    // Camera feed components
    @FXML
    private VBox cameraContainer;
    
    @FXML
    private ImageView cameraFeedView;
    
    @FXML
    private VBox cameraErrorOverlay;
    
    @FXML
    private Label cameraErrorMessage;
    
    // Camera service
    private CameraService cameraService;

    // Track selected model
    private String selectedModel = "Chris";

    // Services
    private MessageDisplayService displayService;
    private YoloAdapter yoloAdapter;
    
    // State tracking
    private WasteCategory lastRecommendedCategory;
    private Timeline detectionLoop;
    private boolean isScanning = false;

    public DisposalMessageController() {
        this.displayService = new MessageDisplayService();
    }

    @FXML
    public void initialize() {
        // Hide all UI components initially
        messageContainer.setVisible(false);
        confirmationContainer.setVisible(false);
        
        // Initialize scanning overlay if it exists in FXML
        if (scanningOverlay != null) {
            scanningOverlay.setVisible(false);
        }

        // Task #412: Initialize YOLO adapter
        try {
            yoloAdapter = new YoloAdapter();
            System.out.println("[Controller] YoloAdapter initialized successfully");
        } catch (Exception e) {
            System.err.println("[Controller] Failed to initialize YoloAdapter: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Initialize camera service and start feed
        cameraService = new CameraService();
        startCameraFeed();
    }

    // ==================== CAMERA FEED ====================

    /**
     * Start the live camera feed.
     * Acceptance: Live video feed with < 500ms delay
     */
    @FXML
    private void startCameraFeed() {
        if (cameraService == null) {
            cameraService = new CameraService();
        }
        
        // Hide error overlay
        if (cameraErrorOverlay != null) {
            cameraErrorOverlay.setVisible(false);
        }
        
        // Start feed with error callback
        cameraService.startFeed(cameraFeedView, this::showCameraError);
        
        System.out.println("[Controller] Camera feed started");
    }

    /**
     * Stop the live camera feed.
     */
    @FXML
    private void stopCameraFeed() {
        if (cameraService != null) {
            cameraService.stopFeed();
        }
        
        // Clear the image
        if (cameraFeedView != null) {
            cameraFeedView.setImage(null);
        }
        
        System.out.println("[Controller] Camera feed stopped");
    }

    /**
     * Show camera error overlay.
     * Acceptance: Display error message if camera fails
     */
    private void showCameraError() {
        if (cameraErrorOverlay != null) {
            cameraErrorOverlay.setVisible(true);
        }
        
        if (cameraErrorMessage != null) {
            cameraErrorMessage.setText("Camera not authorized or disconnected.\nCheck System Settings > Privacy > Camera");
        }
        
        System.err.println("[Controller] Camera error - displaying error overlay");
    }

    /**
     * Check if camera feed is active.
     */
    public boolean isCameraFeedActive() {
        return cameraService != null && cameraService.isRunning();
    }

    // ==================== YOLO DETECTION ====================

    /**
     * Task #412 & #413: Start YOLO detection loop.
     */
    @FXML
    private void startScanning() {
        if (yoloAdapter == null) {
            System.err.println("[Controller] Cannot start scanning - YoloAdapter not initialized");
            return;
        }

        if (isScanning) {
            System.out.println("[Controller] Already scanning");
            return;
        }

        isScanning = true;
        showScanningOverlay();

        detectionLoop = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            try {
                WasteCategory detectedCategory = yoloAdapter.detectCategory();

                if (detectedCategory != null) {
                    System.out.println("[Controller] Detection: " + detectedCategory);
                    stopScanning();
                    showDisposalInstructions(detectedCategory);
                    displayService.scheduleAction(3.5, this::startScanning);
                }
            } catch (Exception e) {
                System.err.println("[Controller] Detection error: " + e.getMessage());
            }
        }));
        
        detectionLoop.setCycleCount(Timeline.INDEFINITE);
        detectionLoop.play();
        
        System.out.println("[Controller] Scanning started");
    }

    /**
     * Stop YOLO detection loop.
     */
    @FXML
    private void stopScanning() {
        if (detectionLoop != null) {
            detectionLoop.stop();
            detectionLoop = null;
        }
        
        isScanning = false;
        hideScanningOverlay();
        
        System.out.println("[Controller] Scanning stopped");
    }

    /**
     * Task #413: Show scanning overlay.
     */
    private void showScanningOverlay() {
        if (scanningOverlay != null) {
            if (scanningLabel != null) {
                scanningLabel.setText("Scanning...");
            }
            if (scanningMessageLabel != null) {
                scanningMessageLabel.setText("Hold item in front of camera");
            }
            scanningOverlay.setVisible(true);
        }
    }

    /**
     * Task #413: Hide scanning overlay.
     */
    private void hideScanningOverlay() {
        if (scanningOverlay != null) {
            scanningOverlay.setVisible(false);
        }
    }

    /**
     * Task #414: Display disposal instructions based on YOLO classification.
     */
    private void showDisposalInstructions(WasteCategory category) {
        lastRecommendedCategory = category;
        
        hideScanningOverlay();
        
        iconLabel.setText(category.getIcon());
        categoryLabel.setText(category.getCategoryName());
        instructionLabel.setText(category.getInstruction());
        
        messageContainer.setVisible(true);
        
        displayService.displayMessage(category, this::hideDisposalInstructions);
        
        System.out.println("[Controller] Showing disposal instruction: " + category);
    }

    /**
     * Hide the disposal message.
     */
    private void hideDisposalInstructions() {
        messageContainer.setVisible(false);
    }

    /**
     * Display a disposal message for a given waste category.
     */
    public void showDisposalMessage(WasteCategory category) {
        if (isScanning) {
            stopScanning();
        }
        showDisposalInstructions(category);
    }

    /**
     * Story #19: Validate disposal and show confirmation.
     */
    public void validateDisposal(WasteCategory disposedCategory) {
        if (lastRecommendedCategory != null && 
            lastRecommendedCategory == disposedCategory) {
            showConfirmation();
        } else {
            System.out.println("[Controller] Incorrect disposal detected");
        }
    }

    /**
     * Story #19: Show green checkmark confirmation.
     */
    private void showConfirmation() {
        checkmarkLabel.setText("✓");
        confirmationLabel.setText("Correct Disposal!");
        
        confirmationContainer.setVisible(true);
        
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.3), checkmarkLabel);
        scaleIn.setFromX(0.0);
        scaleIn.setFromY(0.0);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.play();
        
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), confirmationLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        displayService.displayConfirmation(this::hideConfirmation);
        
        System.out.println("[Controller] Showing confirmation");
    }

    /**
     * Hide the confirmation message.
     */
    private void hideConfirmation() {
        confirmationContainer.setVisible(false);
    }

    /**
     * Manually hide message.
     */
    public void dismissMessage() {
        displayService.cancelMessage();
        hideDisposalInstructions();
        hideConfirmation();
        
        if (isScanning) {
            stopScanning();
        }
    }

    /**
     * Cleanup resources when controller is destroyed.
     */
    public void shutdown() {
        if (detectionLoop != null) {
            detectionLoop.stop();
        }
        
        if (cameraService != null) {
            cameraService.stopFeed();
        }
        
        if (yoloAdapter != null) {
            yoloAdapter.close();
        }
        
        System.out.println("[Controller] Shutdown complete");
    }

    // ==================== MODEL TOGGLE ====================

    /**
     * Select Chris's YOLO model.
     */
    @FXML
    private void selectChrisModel() {
        selectedModel = "Chris";
        updateModelButtons();
        System.out.println("[Controller] Selected model: Chris");
    }

    /**
     * Select Joseph's ML model.
     */
    @FXML
    private void selectJosephModel() {
        selectedModel = "Joseph";
        updateModelButtons();
        System.out.println("[Controller] Selected model: Joseph");
    }

    /**
     * Update button styles to show which model is selected.
     */
    private void updateModelButtons() {
        if (chrisModelButton != null && josephModelButton != null) {
            if ("Chris".equals(selectedModel)) {
                chrisModelButton.getStyleClass().removeAll("model-button");
                chrisModelButton.getStyleClass().add("model-button-selected");
                josephModelButton.getStyleClass().removeAll("model-button-selected");
                josephModelButton.getStyleClass().add("model-button");
            } else {
                josephModelButton.getStyleClass().removeAll("model-button");
                josephModelButton.getStyleClass().add("model-button-selected");
                chrisModelButton.getStyleClass().removeAll("model-button-selected");
                chrisModelButton.getStyleClass().add("model-button");
            }
        }
    }

    /**
     * Get currently selected model name.
     */
    public String getSelectedModel() {
        return selectedModel;
    }

    // ==================== Test Methods ====================

    @FXML
    private void testRecycle() {
        showDisposalMessage(WasteCategory.RECYCLE);
    }

    @FXML
    private void testTrash() {
        showDisposalMessage(WasteCategory.TRASH);
    }

    @FXML
    private void testCompost() {
        showDisposalMessage(WasteCategory.COMPOST);
    }

    @FXML
    private void testHazardous() {
        showDisposalMessage(WasteCategory.HAZARDOUS);
    }

    @FXML
    private void testCorrectRecycle() {
        showDisposalMessage(WasteCategory.RECYCLE);
        displayService.scheduleAction(3.5, () -> validateDisposal(WasteCategory.RECYCLE));
    }

    @FXML
    private void testCorrectTrash() {
        showDisposalMessage(WasteCategory.TRASH);
        displayService.scheduleAction(3.5, () -> validateDisposal(WasteCategory.TRASH));
    }

    @FXML
    private void testCorrectCompost() {
        showDisposalMessage(WasteCategory.COMPOST);
        displayService.scheduleAction(3.5, () -> validateDisposal(WasteCategory.COMPOST));
    }

    @FXML
    private void testCorrectHazardous() {
        showDisposalMessage(WasteCategory.HAZARDOUS);
        displayService.scheduleAction(3.5, () -> validateDisposal(WasteCategory.HAZARDOUS));
    }

    @FXML
    private void testIncorrectDisposal() {
        showDisposalMessage(WasteCategory.RECYCLE);
        displayService.scheduleAction(3.5, () -> validateDisposal(WasteCategory.TRASH));
    }

    @FXML
    private void testYoloDetection() {
        if (yoloAdapter == null) {
            System.err.println("[Controller] YoloAdapter not initialized");
            return;
        }
        
        WasteCategory mockDetection = yoloAdapter.simulateDetection("bottle");
        if (mockDetection != null) {
            showDisposalInstructions(mockDetection);
        }
    }
}