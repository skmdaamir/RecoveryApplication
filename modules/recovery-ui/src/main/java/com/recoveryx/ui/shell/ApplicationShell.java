package com.recoveryx.ui.shell;

import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.ui.controller.scan.ScanResultsController;
import com.recoveryx.ui.util.SpringFxmlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Root desktop shell responsible for presenting the primary application window.
 * Supports navigation between the drive-selection and scan-results views.
 */
@Component
@ConditionalOnProperty(
        prefix = "recoveryx.ui",
        name = "shell-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ApplicationShell {

    private static final Logger log = LoggerFactory.getLogger(ApplicationShell.class);

    private final BorderPane rootLayout;
    private final SpringFxmlLoader fxmlLoader;

    // Cached drive selection view (re-used when navigating back)
    private Parent driveSelectionView;

    public ApplicationShell(SpringFxmlLoader fxmlLoader) {
        this.fxmlLoader = fxmlLoader;
        this.rootLayout = new BorderPane();
        this.rootLayout.setPrefSize(1280, 800);
        showDriveSelection();
    }

    /** Navigate to drive selection (home) screen */
    public void showDriveSelection() {
        try {
            if (driveSelectionView == null) {
                driveSelectionView = fxmlLoader.load("/com/recoveryx/ui/fxml/drive/drive-selection-view.fxml");
                log.info("Loaded drive-selection-view successfully.");
            }
            rootLayout.setCenter(driveSelectionView);
        } catch (Exception e) {
            log.error("Failed to load drive-selection-view.fxml: {}", e.getMessage(), e);
            Label fallback = new Label("Error loading drive selection view: " + e.getMessage());
            BorderPane.setAlignment(fallback, Pos.CENTER);
            rootLayout.setCenter(fallback);
        }
    }

    /**
     * Navigate to the scan results screen and immediately start a scan.
     *
     * @param request     the ScanRequest to execute
     * @param destination destination folder for recovered files (or null for default)
     */
    public void showScanResults(ScanRequest request, String destination) {
        try {
            // Load a fresh scan-results view each time (new scan = new controller instance)
            Parent resultsView = fxmlLoader.load("/com/recoveryx/ui/fxml/scan/scan-results-view.fxml");

            // Get the controller so we can pass the request and hook up the back button
            ScanResultsController controller = (ScanResultsController)
                    resultsView.getProperties().get("fx:controller");

            // FXMLLoader stores the controller in user data
            if (controller == null && resultsView.getUserData() instanceof ScanResultsController c) {
                controller = c;
            }

            // Fallback: retrieve from Spring context via loader
            // (Spring factory creates the bean — just call startScan via reflection-free approach)
            // The loader's controllerFactory already wired the Spring bean; we need a reference.
            // We use a workaround: store the controller in rootLayout userData after load.
            if (controller == null) {
                Object ud = rootLayout.getUserData();
                if (ud instanceof ScanResultsController c) controller = c;
            }

            rootLayout.setCenter(resultsView);

            // Wire back button and start scan through controller if we have it
            if (controller != null) {
                final ApplicationShell shell = this;
                controller.setOnBack(shell::showDriveSelection);
                controller.startScan(request, destination);
            }

            log.info("Navigated to scan-results-view.");
        } catch (Exception e) {
            log.error("Failed to load scan-results-view.fxml: {}", e.getMessage(), e);
        }
    }

    /**
     * Exposes the root layout pane to be attached to the JavaFX scene.
     */
    public BorderPane getView() {
        return this.rootLayout;
    }
}