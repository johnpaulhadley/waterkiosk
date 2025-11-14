package com.kiosk.services;

import com.kiosk.models.WasteCategory;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Service for managing disposal message display timing.
 * Handles the 3-5 second display requirement and 1 second response time.
 */
public class MessageDisplayService {
    
    private static final double MESSAGE_DURATION_SECONDS = 4.0; // Middle of 3-5 second range
    private PauseTransition messageTimer;
    private Runnable onMessageComplete;

    /**
     * Display a message for the specified duration.
     * 
     * @param category The waste category to display
     * @param onComplete Callback when message display is complete
     */
    public void displayMessage(WasteCategory category, Runnable onComplete) {
        this.onMessageComplete = onComplete;
        
        // Cancel any existing timer
        if (messageTimer != null) {
            messageTimer.stop();
        }
        
        // Create new timer for MESSAGE_DURATION_SECONDS
        messageTimer = new PauseTransition(Duration.seconds(MESSAGE_DURATION_SECONDS));
        messageTimer.setOnFinished(event -> {
            if (onMessageComplete != null) {
                onMessageComplete.run();
            }
        });
        
        messageTimer.play();
    }

    /**
     * Cancel the current message display.
     */
    public void cancelMessage() {
        if (messageTimer != null) {
            messageTimer.stop();
        }
    }

    /**
     * Get the configured message duration in seconds.
     */
    public double getMessageDuration() {
        return MESSAGE_DURATION_SECONDS;
    }
}
