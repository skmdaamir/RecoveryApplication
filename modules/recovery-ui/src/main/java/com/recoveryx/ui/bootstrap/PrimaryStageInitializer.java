package com.recoveryx.ui.bootstrap;

import com.recoveryx.ui.util.SpringFxmlLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Objects;

/**
 * Initializes the primary application stage and loads the first screen.
 */
@Component
public class PrimaryStageInitializer {

    private static final String DRIVE_SELECTION_FXML =
            "/com/recoveryx/ui/fxml/drive/drive-selection-view.fxml";
    private static final String DRIVE_SELECTION_CSS =
            "/com/recoveryx/ui/css/drive-selection.css";

    private final SpringFxmlLoader springFxmlLoader;

    public PrimaryStageInitializer(SpringFxmlLoader springFxmlLoader) {
        this.springFxmlLoader = Objects.requireNonNull(springFxmlLoader, "springFxmlLoader");
    }

    public void showDriveSelection(Stage stage) {
        Objects.requireNonNull(stage, "stage");

        Parent root = springFxmlLoader.load(DRIVE_SELECTION_FXML);
        Scene scene = new Scene(root, 1280, 800);

        URL stylesheet = getClass().getResource(DRIVE_SELECTION_CSS);
        if (stylesheet == null) {
            throw new IllegalStateException("Stylesheet not found: " + DRIVE_SELECTION_CSS);
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());

        stage.setTitle("RecoveryX Pro");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}