package com.recoveryx.ui.controller.drive;

import com.recoveryx.core.model.scan.ScanMode;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.ui.shell.ApplicationShell;
import com.recoveryx.ui.viewmodel.drive.DeviceItemViewModel;
import com.recoveryx.ui.viewmodel.drive.DriveSelectionViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * Spring-managed controller for the Drive Selection screen.
 * Navigates to the Scan Results view on scan initiation.
 */
@Component
public class DriveSelectionController {

    @FXML private TableView<DeviceItemViewModel> deviceTable;
    @FXML private TableColumn<DeviceItemViewModel, String> nameColumn;
    @FXML private TableColumn<DeviceItemViewModel, String> pathColumn;
    @FXML private TableColumn<DeviceItemViewModel, String> typeColumn;
    @FXML private TableColumn<DeviceItemViewModel, String> fsColumn;
    @FXML private TableColumn<DeviceItemViewModel, String> sizeColumn;
    @FXML private TableColumn<DeviceItemViewModel, String> healthColumn;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button refreshButton;
    @FXML private Button quickScanButton;
    @FXML private Label selectedDriveLabel;

    private final DriveSelectionViewModel viewModel;
    private final ApplicationShell applicationShell;

    public DriveSelectionController(DriveSelectionViewModel viewModel,
                                    ApplicationShell applicationShell) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        this.applicationShell = Objects.requireNonNull(applicationShell, "applicationShell");
    }

    @FXML
    public void initialize() {
        // Bind columns
        nameColumn.setCellValueFactory(data -> data.getValue().displayNameProperty());
        pathColumn.setCellValueFactory(data -> data.getValue().devicePathProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().deviceTypeProperty());
        fsColumn.setCellValueFactory(data -> data.getValue().fileSystemProperty());
        sizeColumn.setCellValueFactory(data -> data.getValue().sizeTextProperty());
        healthColumn.setCellValueFactory(data -> data.getValue().accessibilityTextProperty());

        deviceTable.setItems(viewModel.getDevices());
        deviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                viewModel.setSelectedDevice(newVal));

        // Bind status/progress
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        progressIndicator.progressProperty().bind(viewModel.progressProperty());
        progressIndicator.visibleProperty().bind(viewModel.loadingProperty());
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());

        selectedDriveLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            DeviceItemViewModel item = viewModel.getSelectedDevice();
            if (item == null || item.getDisplayName() == null) return "No drive selected";
            return "Selected: " + item.getDisplayName() + " | " + item.getSizeText();
        }, viewModel.selectedDeviceProperty()));

        refreshButton.disableProperty().bind(viewModel.loadingProperty());
        quickScanButton.disableProperty().bind(viewModel.quickScanAllowedProperty().not());

        viewModel.loadDevices();
    }

    @FXML
    private void onRefresh() {
        viewModel.refreshDevices();
    }

    @FXML
    private void onQuickScan() {
        launchScan(ScanMode.QUICK);
    }

    private void launchScan(ScanMode mode) {
        DeviceItemViewModel selected = viewModel.getSelectedDevice();
        if (selected == null || selected.getDevice() == null) {
            showError("No Drive Selected", "Please select a drive before scanning.");
            return;
        }
        if (!selected.getDevice().isReadable()) {
            showError("Drive Not Accessible", "The selected drive cannot be read.");
            return;
        }

        // Ask for destination
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Recovery Destination Folder");
        String defaultDest = "D:" + File.separator + "Recovered Files";
        File defaultDir = new File(defaultDest);
        if (!defaultDir.exists()) {
            defaultDir = new File(System.getProperty("user.home") + File.separator + "Recovered Files");
            defaultDir.mkdirs();
        }
        chooser.setInitialDirectory(defaultDir.exists() ? defaultDir : new File(System.getProperty("user.home")));

        File dest = chooser.showDialog(deviceTable.getScene().getWindow());
        String destinationPath = dest != null ? dest.getAbsolutePath() : defaultDest;

        ScanRequest request = ScanRequest.builder()
                .storageDevice(selected.getDevice())
                .scanMode(mode)
                .includeDeletedEntries(true)
                .includeRawSignatureScan(mode == ScanMode.DEEP)
                .build();

        // Navigate to scan results view
        applicationShell.showScanResults(request, destinationPath);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}