package com.recoveryx.ui.app;

import com.recoveryx.ui.bootstrap.SpringContextBridge;
import com.recoveryx.ui.shell.ApplicationShell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

/**
 * Primary JavaFX application lifecycle for RecoveryX Pro.
 */
public final class RecoveryXFxApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryXFxApplication.class);

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        LOGGER.info("Initializing RecoveryX Pro JavaFX application");

        this.applicationContext = SpringContextBridge.start(
                getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Starting RecoveryX Pro primary stage");

        try {
            Objects.requireNonNull(
                    applicationContext,
                    "Spring application context has not been initialized");

            ApplicationShell applicationShell = applicationContext.getBean(ApplicationShell.class);

            /*
             * This must happen before getView().
             *
             * initialize(Stage) creates the BorderPane, loads the
             * drive-selection FXML, and attaches the scene.
             */
            applicationShell.initialize(primaryStage);

            primaryStage.setTitle(
                    "RecoveryX Pro - Enterprise Recovery Platform");
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(720);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception ex) {
            LOGGER.error(
                    "Critical failure during JavaFX Stage startup",
                    ex);

            Platform.exit();
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping RecoveryX Pro");

        if (applicationContext != null) {
            applicationContext.close();
            applicationContext = null;
        }
    }
}