package com.kiosk.controllers;

import com.kiosk.models.WasteCategory;
import com.kiosk.services.MessageDisplayService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Controller for the disposal message display.
 * Tasks #127-131: Handles icon integration, text rendering, layout, and styling.
 */
public class DisposalMessageController {

    @FXML
    private VBox messageContainer;

    @FXML
    private Label iconLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label instructionLabel;

    private MessageDisplayService displayService;

    public DisposalMessageController() {
        this.displayService = new MessageDisplayService();
    }

    @FXML
    public void initialize() {
        // Hide message initially
        messageContainer.setVisible(false);
    }

    /**
     * Display a disposal message for the given waste category.
     * Task #127: Integrates instruction text with icon display.
     * Task #128: Implements text rendering with appropriate styling.
     * 
     * @param category The waste category to display
     */
    public void showDisposalMessage(WasteCategory category) {
        // Show message within 1 second (actually instant for better UX)
        iconLabel.setText(category.getIcon());
        categoryLabel.setText(category.getCategoryName());
        instructionLabel.setText(category.getInstruction());
        
        messageContainer.setVisible(true);
        
        // Display for 3-5 seconds, then hide
        displayService.displayMessage(category, this::hideMessage);
    }

    /**
     * Hide the disposal message.
     */
    private void hideMessage() {
        messageContainer.setVisible(false);
    }

    /**
     * Manually hide message (for testing or user dismissal).
     */
    public void dismissMessage() {
        displayService.cancelMessage();
        hideMessage();
    }

    // Test methods for Task #131: Testing
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
}
