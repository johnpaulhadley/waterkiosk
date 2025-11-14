package com.kiosk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for Waste Disposal Kiosk.
 * 
 * Acceptance Criteria:
 * - Message appears within 1 second ✓
 * - Message is readable from 2 meters away ✓ (56px+ fonts)
 * - Message lasts 3–5 seconds ✓ (configured for 4 seconds)
 * - Feedback from 5 users confirms clarity (testing required)
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/disposal_message.fxml"));
        Parent root = loader.load();

        // Create scene (kiosk-sized)
        Scene scene = new Scene(root, 1024, 768);

        // Configure stage
        primaryStage.setTitle("Waste Disposal Kiosk");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        
        // Optional: Full screen for actual kiosk deployment
        // primaryStage.setFullScreen(true);
        
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
