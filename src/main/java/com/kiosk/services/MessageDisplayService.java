package com.kiosk.services;

import com.kiosk.models.WasteCategory;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * Service for managing disposal message display timing.
 * Story #19: Message displayed for 3 seconds, confirmation for 3 seconds.
 * Updated with scheduleAction and displayConfirmation methods.
 */
public class MessageDisplayService {
    
    // Story #19: Message is displayed for 3 seconds
    private static final double MESSAGE_DURATION_SECONDS = 3.0;
    private static final double CONFIRMATION_DURATION_SECONDS = 3.0;
    
    private PauseTransition messageTimer;
    private PauseTransition confirmationTimer;
    private PauseTransition scheduledActionTimer;
    private Runnable onMessageComplete;

    /**
     * Display a message for 3 seconds (Story #19 requirement).
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
        
        // Create new timer for 3 seconds
        messageTimer = new PauseTransition(Duration.seconds(MESSAGE_DURATION_SECONDS));
        messageTimer.setOnFinished(event -> {
            if (onMessageComplete != null) {
                onMessageComplete.run();
            }
        });
        
        messageTimer.play();
    }

    /**
     * Display confirmation message for 3 seconds (Story #19 requirement).
     * 
     * @param onComplete Callback when confirmation display is complete
     */
    public void displayConfirmation(Runnable onComplete) {
        // Cancel any existing confirmation timer
        if (confirmationTimer != null) {
            confirmationTimer.stop();
        }
        
        // Create new timer for 3 seconds
        confirmationTimer = new PauseTransition(Duration.seconds(CONFIRMATION_DURATION_SECONDS));
        confirmationTimer.setOnFinished(event -> {
            if (onComplete != null) {
                onComplete.run();
            }
        });
        
        confirmationTimer.play();
    }

    /**
     * Schedule an action to occur after a delay.
     * Used for testing and restarting detection loops.
     * 
     * @param delaySeconds Delay in seconds
     * @param action Action to execute
     */
    public void scheduleAction(double delaySeconds, Runnable action) {
        if (scheduledActionTimer != null) {
            scheduledActionTimer.stop();
        }
        
        scheduledActionTimer = new PauseTransition(Duration.seconds(delaySeconds));
        scheduledActionTimer.setOnFinished(event -> {
            if (action != null) {
                action.run();
            }
        });
        
        scheduledActionTimer.play();
    }

    /**
     * Cancel the current message display.
     */
    public void cancelMessage() {
        if (messageTimer != null) {
            messageTimer.stop();
        }
        if (confirmationTimer != null) {
            confirmationTimer.stop();
        }
        if (scheduledActionTimer != null) {
            scheduledActionTimer.stop();
        }
    }

    /**
     * Get the configured message duration in seconds.
     */
    public double getMessageDuration() {
        return MESSAGE_DURATION_SECONDS;
    }

    /**
     * Get the configured confirmation duration in seconds.
     */
    public double getConfirmationDuration() {
        return CONFIRMATION_DURATION_SECONDS;
    }
}

