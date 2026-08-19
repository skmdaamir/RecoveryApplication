package com.recoveryx.ui;

import com.recoveryx.ui.bootstrap.PrimaryStageInitializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.CountDownLatch;

/**
 * Main entry point for RecoveryX Pro desktop application.
 * Combines Spring Boot lifecycle with JavaFX application lifecycle.
 */
@SpringBootApplication
public class RecoveryXApplication extends Application {

    private static ConfigurableApplicationContext applicationContext;
    private static CountDownLatch latch;

    public static void main(String[] args) {
        // Start Spring context first
        applicationContext = SpringApplication.run(RecoveryXApplication.class, args);

        // Then launch JavaFX
        latch = new CountDownLatch(1);
        launch(args);

        // When JavaFX exits, close Spring
        applicationContext.close();
    }

    @Override
    public void start(Stage stage) {
        // Let Spring finish wiring before touching JavaFX
        Platform.runLater(() -> {
            PrimaryStageInitializer initializer = applicationContext.getBean(PrimaryStageInitializer.class);
            initializer.showDriveSelection(stage);
            latch.countDown();
        });
try{
        latch.await();
}catch(InterruptedException e){
Thread.currentThread().interrupt();
//APplication is shutting down; exit JavaFX
Platform.exit();
}
    }

    @Override
    public void stop() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }
}