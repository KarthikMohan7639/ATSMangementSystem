package com.ats;

import com.ats.controller.ATSController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for ATS Management System
 */
public class ATSApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("ATS Management System - Resume & CV Processing");
        
        ATSController controller = new ATSController(primaryStage);
        VBox root = controller.createUI();
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
