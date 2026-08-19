package com.recoveryx.ui.controller.drive;

import com.recoveryx.ui.viewmodel.drive.DeviceItemViewModel;
import com.recoveryx.ui.viewmodel.drive.DriveSelectionViewModel;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Spring-managed controller for the Drive Selection screen.
 * Uses MVVM: all UI logic delegates to DriveSelectionViewModel.
 */
@Component
public class DriveSelectionController {

    @FXML
    private TableView<DeviceItemViewModel> deviceTable;

    @FXML
    private TableColumn<DeviceItemViewModel, String> nameColumn;

    @FXML
    private TableColumn<DeviceItemViewModel, String> pathColumn;

    @FXML
    private TableColumn<DeviceItemViewModel, String> typeColumn;

    @FXML
    private TableColumn<DeviceItemViewModel, String> fsColumn;

    @FXML
    private TableColumn<DeviceItemViewModel, String> sizeColumn;

    @FXML
    private TableColumn<DeviceItemViewModel, String> healthColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button refreshButton;

    @FXML
    private Button quickScanButton;

    @FXML
    private Label selectedDriveLabel;

    private final DriveSelectionViewModel viewModel;

    public DriveSelectionController(DriveSelectionViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    }

    @FXML
    public void initialize() {
        // Bind columns to view model properties
        nameColumn.setCellValueFactory(data ->
                data.getValue().displayNameProperty());
        pathColumn.setCellValueFactory(data ->
                data.getValue().devicePathProperty());
        typeColumn.setCellValueFactory(data ->
                data.getValue().deviceTypeProperty());
        fsColumn.setCellValueFactory(data ->
                data.getValue().fileSystemProperty());
        sizeColumn.setCellValueFactory(data ->
                data.getValue().sizeTextProperty());
        healthColumn.setCellValueFactory(data ->
                data.getValue().accessibilityTextProperty());

        // Wire table to ViewModel list and selection
        deviceTable.setItems(viewModel.getDevices());
        deviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                viewModel.setSelectedDevice(newVal));

        // Bind status, progress, and selection summary
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        progressIndicator.progressProperty().bind(viewModel.progressProperty());
        progressIndicator.visibleProperty().bind(viewModel.loadingProperty());
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());

        selectedDriveLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            DeviceItemViewModel item = viewModel.getSelectedDevice();
            if (item == null || item.getDisplayName() == null) {
                return "No drive selected";
            }
            return "Selected: " + item.getDisplayName() + " | " + item.getSizeText();
        }, viewModel.selectedDeviceProperty()));

        // Bind button states
        refreshButton.disableProperty().bind(viewModel.loadingProperty());
        quickScanButton.disableProperty().bind(viewModel.quickScanAllowedProperty().not());

        // Initial load
        viewModel.loadDevices();
    }

    @FXML
    private void onRefresh() {
        viewModel.refreshDevices();
    }

    @FXML
    private void onQuickScan() {
        try {
            viewModel.startQuickScan();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quick Scan Created");
            alert.setHeaderText("Scan session created successfully");
            alert.setContentText("Session ID: " + viewModel.getActiveSessionId());
            alert.showAndWait();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Quick Scan Error");
            alert.setHeaderText("Unable to create quick scan session");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}