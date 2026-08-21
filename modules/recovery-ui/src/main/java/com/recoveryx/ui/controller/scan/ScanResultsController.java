package com.recoveryx.ui.controller.scan;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.ui.viewmodel.scan.RecoverableFileViewModel;
import com.recoveryx.ui.viewmodel.scan.ScanResultsViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.DirectoryChooser;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the Scan Results screen.
 * Binds the table, progress bar, filters, and recovery action.
 */
@Component
public class ScanResultsController {

    @FXML private Label scanTitleLabel;
    @FXML private Label fileCountLabel;
    @FXML private Button backButton;
    @FXML private Button cancelScanButton;
    @FXML private Button recoverButton;

    @FXML private javafx.scene.layout.VBox progressSection;
    @FXML private Label progressLabel;
    @FXML private Label progressPercentLabel;
    @FXML private ProgressBar scanProgressBar;
    @FXML private Label progressDetailLabel;

    @FXML private CheckBox selectAllCheckBox;
    @FXML private Button filterAllBtn;
    @FXML private Button filterPhotosBtn;
    @FXML private Button filterDocsBtn;
    @FXML private Button filterVideoBtn;
    @FXML private Button filterOtherBtn;
    @FXML private Label selectedCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label destinationLabel;

    @FXML private TableView<RecoverableFileViewModel> resultsTable;
    @FXML private TableColumn<RecoverableFileViewModel, Boolean> selectColumn;
    @FXML private TableColumn<RecoverableFileViewModel, String> nameColumn;
    @FXML private TableColumn<RecoverableFileViewModel, String> extensionColumn;
    @FXML private TableColumn<RecoverableFileViewModel, String> sizeColumn;
    @FXML private TableColumn<RecoverableFileViewModel, String> deletedDateColumn;
    @FXML private TableColumn<RecoverableFileViewModel, String> pathColumn;
    @FXML private TableColumn<RecoverableFileViewModel, String> chanceColumn;

    private final ScanResultsViewModel viewModel;
    private Runnable onBackCallback;
    private String defaultDestination = System.getProperty("user.home") + File.separator + "Recovered Files";

    public ScanResultsController(ScanResultsViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    }

    @FXML
    public void initialize() {
        // Checkbox column
        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);
        resultsTable.setEditable(true);

        // Data columns
        nameColumn.setCellValueFactory(d -> d.getValue().nameProperty());
        extensionColumn.setCellValueFactory(d -> d.getValue().extensionProperty());
        sizeColumn.setCellValueFactory(d -> d.getValue().sizeTextProperty());
        deletedDateColumn.setCellValueFactory(d -> d.getValue().deletedDateTextProperty());
        pathColumn.setCellValueFactory(d -> d.getValue().originalPathProperty());
        chanceColumn.setCellValueFactory(d -> d.getValue().recoveryChanceTextProperty());

        // Wire table to filtered list
        resultsTable.setItems(viewModel.getFilteredFiles());

        // Progress bindings
        scanProgressBar.progressProperty().bind(viewModel.progressPercentProperty());
        progressDetailLabel.textProperty().bind(viewModel.progressDetailProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // File count label
        viewModel.totalFoundProperty().addListener((obs, o, n) ->
                fileCountLabel.setText(n.intValue() + " file(s) found"));

        // Selected count label
        viewModel.selectedCountProperty().addListener((obs, o, n) -> {
            selectedCountLabel.setText(n.intValue() + " selected");
            recoverButton.setDisable(n.intValue() == 0);
        });

        // Progress percent label
        viewModel.progressPercentProperty().addListener((obs, o, n) ->
                progressPercentLabel.setText(String.format("%.0f%%", n.doubleValue() * 100)));

        // Hide progress section once scan finishes
        viewModel.scanningProperty().addListener((obs, o, n) -> {
            progressSection.setVisible(n);
            progressSection.setManaged(n);
            cancelScanButton.setDisable(!n);
        });

        destinationLabel.setText("Destination: " + defaultDestination);
    }

    /** Called from DriveSelectionController to initiate a scan */
    public void startScan(ScanRequest request, String destinationPath) {
        if (destinationPath != null && !destinationPath.isBlank()) {
            defaultDestination = destinationPath;
        }
        destinationLabel.setText("Destination: " + defaultDestination);
        scanTitleLabel.setText("Scanning: " + request.getStorageDevice().getDevicePath());
        viewModel.startScan(request);
    }

    /** Set the back-navigation callback */
    public void setOnBack(Runnable callback) {
        this.onBackCallback = callback;
    }

    @FXML
    private void onBack() {
        viewModel.cancelScan();
        if (onBackCallback != null) onBackCallback.run();
    }

    @FXML
    private void onCancelScan() {
        viewModel.cancelScan();
    }

    @FXML
    private void onSelectAll() {
        viewModel.selectAll(selectAllCheckBox.isSelected());
    }

    @FXML private void onFilterAll() { viewModel.clearFilter(); setActiveFilter(filterAllBtn); }
    @FXML private void onFilterPhotos() { viewModel.filterByCategory(FileCategory.IMAGE); setActiveFilter(filterPhotosBtn); }
    @FXML private void onFilterDocs() { viewModel.filterByCategory(FileCategory.DOCUMENT); setActiveFilter(filterDocsBtn); }
    @FXML private void onFilterVideo() { viewModel.filterByCategory(FileCategory.VIDEO); setActiveFilter(filterVideoBtn); }
    @FXML private void onFilterOther() {
        viewModel.applyFilter(f -> f.getCategory() != FileCategory.IMAGE
                && f.getCategory() != FileCategory.DOCUMENT
                && f.getCategory() != FileCategory.VIDEO);
        setActiveFilter(filterOtherBtn);
    }

    private void setActiveFilter(Button active) {
        for (Button b : List.of(filterAllBtn, filterPhotosBtn, filterDocsBtn, filterVideoBtn, filterOtherBtn)) {
            b.getStyleClass().remove("filter-button-active");
            b.getStyleClass().add("filter-button");
        }
        active.getStyleClass().remove("filter-button");
        active.getStyleClass().add("filter-button-active");
    }

    @FXML
    private void onRecoverSelected() {
        List<RecoverableFileViewModel> selected = viewModel.getSelectedFiles();
        if (selected.isEmpty()) {
            showInfo("No Files Selected", "Please select at least one file to recover.");
            return;
        }

        // Ask user to confirm destination
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Recovery Destination Folder");
        File initial = new File(defaultDestination);
        if (!initial.exists()) initial.mkdirs();
        chooser.setInitialDirectory(initial.exists() ? initial : new File(System.getProperty("user.home")));

        File dest = chooser.showDialog(resultsTable.getScene().getWindow());
        if (dest == null) return;

        String destPath = dest.getAbsolutePath();
        destinationLabel.setText("Destination: " + destPath);

        // Recover in background
        int[] counts = {0, 0};
        Thread t = new Thread(() -> {
            for (RecoverableFileViewModel fvm : selected) {
                try {
                    String src = fvm.getFile().currentPath();
                    if (src == null || src.isBlank()) { counts[1]++; continue; }
                    Path srcPath = Paths.get(src);
                    Path target = Paths.get(destPath, fvm.getFile().name());
                    Files.copy(srcPath, target, StandardCopyOption.REPLACE_EXISTING);
                    counts[0]++;
                } catch (Exception ex) {
                    counts[1]++;
                }
            }
            javafx.application.Platform.runLater(() -> {
                showInfo("Recovery Complete",
                        counts[0] + " file(s) recovered successfully to:\n" + destPath
                        + (counts[1] > 0 ? "\n\n" + counts[1] + " file(s) could not be recovered." : ""));
            });
        }, "recoveryx-recover-thread");
        t.setDaemon(true);
        t.start();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
