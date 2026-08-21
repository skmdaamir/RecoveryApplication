package com.recoveryx.ui.shell;

import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.ui.controller.scan.ScanResultsController;
import com.recoveryx.ui.util.SpringFxmlLoader;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Root desktop shell responsible for presenting the primary application window.
 * Supports navigation between the drive-selection and scan-results views.
 *
 * JavaFX controls and FXML views are initialized only after JavaFX startup.
 */
@Component
@ConditionalOnProperty(prefix = "recoveryx.ui", name = "shell-enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationShell {

    private static final Logger log = LoggerFactory.getLogger(ApplicationShell.class);

    private final SpringFxmlLoader fxmlLoader;

    private BorderPane rootLayout;
    private Stage primaryStage;
    private Parent driveSelectionView;

    public ApplicationShell(SpringFxmlLoader fxmlLoader) {
        this.fxmlLoader = Objects.requireNonNull(
                fxmlLoader,
                "fxmlLoader");

        /*
         * Do not create JavaFX controls or load FXML here.
         *
         * Spring may construct this bean before the JavaFX toolkit has
         * started, and FXML loading here causes a circular bean creation:
         *
         * ApplicationShell
         * -> FXML loader
         * -> DriveSelectionController
         */
    }

    /**
     * Initializes the JavaFX shell.
     *
     * Must be called from the JavaFX Application Thread after JavaFX startup.
     */
    public void initialize(Stage stage) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException(
                    "ApplicationShell.initialize must run on the JavaFX Application Thread");
        }

        this.primaryStage = Objects.requireNonNull(stage, "stage");

        if (this.rootLayout == null) {
            this.rootLayout = new BorderPane();
            this.rootLayout.setPrefSize(1280, 800);
        }

        showDriveSelection();
    }

    /**
     * Navigates to the drive-selection screen.
     */
    public void showDriveSelection() {
        requireInitialized();

        try {
            if (driveSelectionView == null) {
                driveSelectionView = fxmlLoader.load(
                        "/com/recoveryx/ui/fxml/drive/drive-selection-view.fxml");

                log.info("Loaded drive-selection-view.fxml successfully.");
            }

            rootLayout.setCenter(driveSelectionView);
            showRootLayout();

        } catch (Exception ex) {
            log.error(
                    "Failed to load drive-selection-view.fxml",
                    ex);

            showError(
                    "Error loading drive selection view",
                    ex);
        }
    }

    /**
     * Navigates to the scan-results screen and starts the scan.
     *
     * @param request     scan request
     * @param destination destination folder, or null for the default
     */
    public void showScanResults(
            ScanRequest request,
            String destination) {
        requireInitialized();

        Objects.requireNonNull(request, "request");

        try {
            SpringFxmlLoader.LoadedView<ScanResultsController> loadedView = fxmlLoader.loadWithController(
                    "/com/recoveryx/ui/fxml/scan/scan-results-view.fxml",
                    ScanResultsController.class);

            Parent resultsView = loadedView.root();
            ScanResultsController controller = loadedView.controller();

            controller.setOnBack(this::showDriveSelection);
            controller.startScan(request, destination);

            rootLayout.setCenter(resultsView);
            showRootLayout();

            log.info("Navigated to scan-results-view.fxml.");

        } catch (Exception ex) {
            log.error(
                    "Failed to load scan-results-view.fxml",
                    ex);

            showError(
                    "Error loading scan results view",
                    ex);
        }
    }

    /**
     * Returns the root layout to attach to a Scene.
     */
    public BorderPane getView() {
        requireInitialized();
        return rootLayout;
    }

    /**
     * Returns the primary stage after initialization.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Closes the primary window.
     */
    public void close() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }

    private void showRootLayout() {
        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(rootLayout));
        } else if (primaryStage.getScene().getRoot() != rootLayout) {
            primaryStage.getScene().setRoot(rootLayout);
        }

        primaryStage.setTitle("RecoveryX Pro");
        primaryStage.show();
    }

    private void showError(String title, Exception ex) {
        if (rootLayout == null) {
            log.error("{}: {}", title, ex.getMessage());
            return;
        }

        String message = ex.getMessage() == null
                ? ex.getClass().getSimpleName()
                : ex.getMessage();

        Label fallback = new Label(title + ": " + message);
        fallback.setWrapText(true);

        BorderPane.setAlignment(fallback, Pos.CENTER);
        rootLayout.setCenter(fallback);

        if (primaryStage != null) {
            showRootLayout();
        }
    }

    private void requireInitialized() {
        if (rootLayout == null || primaryStage == null) {
            throw new IllegalStateException(
                    "ApplicationShell has not been initialized. "
                            + "Call initialize(Stage) from the JavaFX Application Thread first.");
        }
    }
}