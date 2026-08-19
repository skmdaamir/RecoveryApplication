package com.recoveryx.ui.app;

import com.recoveryx.ui.bootstrap.SpringContextBridge;
import com.recoveryx.ui.shell.ApplicationShell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.Scene;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Primary JavaFX application lifecycle for RecoveryX Pro.
 */
public final class RecoveryXFxApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryXFxApplication.class);

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        LOGGER.info("Initializing RecoveryX Pro JavaFX application");
        this.applicationContext = SpringContextBridge.start(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting RecoveryX Pro primary stage");
    try {
       // 1. Fetch your ApplicationShell from the Spring IoC Context
            ApplicationShell applicationShell = applicationContext.getBean(ApplicationShell.class);

            // 2. Extract the view container and attach it to a new active scene graph
            Scene scene = new Scene(applicationShell.getView(), 1280, 800);
            primaryStage.setScene(scene);

            // 3. Configure window boundaries and title attributes
            primaryStage.setTitle("RecoveryX Pro - Enterprise Recovery Platform");
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(720);
            
            // 4. Render layout structures to physical display hardware
            primaryStage.centerOnScreen();
            primaryStage.show();
        
    } catch (Exception e) {
        LOGGER.error("Critical failure during JavaFX Stage startup", e);
    }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping RecoveryX Pro");
        if (applicationContext != null) {
            applicationContext.close();
        }
        Platform.exit();
    }
}