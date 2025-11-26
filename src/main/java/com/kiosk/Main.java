package com.kiosk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for Waste Disposal Kiosk.
 * 
 * User Story: Integrate UI with YOLO Machine Learning Model
 * Story #19: Feedback on correct disposal
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/disposal_message.fxml"));
        Parent root = loader.load();

        // Create scene (kiosk-sized)
        Scene scene = new Scene(root, 1024, 900);

        // Configure stage
        primaryStage.setTitle("Waste Disposal Kiosk - YOLO Integration");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        
        // Optional: Full screen for actual kiosk deployment
        // primaryStage.setFullScreen(true);
        
        primaryStage.show();
        
        System.out.println("[Main] Application started successfully");
    }

    @Override
    public void stop() {
        System.out.println("[Main] Application stopping...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
