package com.recoveryx.ui.viewmodel.scan;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.core.domain.file.RecoverableFile;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.scanner.service.DefaultScanOrchestrationService;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * ViewModel for the Scan Results screen.
 * Runs the actual scan on a background thread and streams results to the UI.
 */
@Component
public class ScanResultsViewModel {

    private static final Logger log = LoggerFactory.getLogger(ScanResultsViewModel.class);

    private final DefaultScanOrchestrationService scanService;

    private final ObservableList<RecoverableFileViewModel> allFiles = FXCollections.observableArrayList();
    private final FilteredList<RecoverableFileViewModel> filteredFiles = new FilteredList<>(allFiles, f -> true);

    private final StringProperty statusMessage = new SimpleStringProperty("Starting scan...");
    private final StringProperty progressDetail = new SimpleStringProperty("Preparing...");
    private final DoubleProperty progressPercent = new SimpleDoubleProperty(0.0);
    private final BooleanProperty scanning = new SimpleBooleanProperty(false);
    private final IntegerProperty totalFound = new SimpleIntegerProperty(0);
    private final IntegerProperty selectedCount = new SimpleIntegerProperty(0);

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private String scanId;

    public ScanResultsViewModel(DefaultScanOrchestrationService scanService) {
        this.scanService = scanService;
    }

    public void startScan(ScanRequest request) {
        cancelled.set(false);
        scanning.set(true);
        allFiles.clear();
        totalFound.set(0);
        selectedCount.set(0);
        statusMessage.set("Scanning " + request.getStorageDevice().getDevicePath() + "...");
        progressDetail.set("Initializing scan engine...");
        progressPercent.set(0.0);

        Thread scanThread = new Thread(() -> {
            try {
                var progress = scanService.startScan(request, file -> {
                    if (!cancelled.get()) {
                        Platform.runLater(() -> addFile(file));
                    }
                });
                scanId = progress.message(); // use message as ID fallback

                // Poll progress until scan completes
                while (!cancelled.get()) {
                    var current = scanService.getProgress(scanId);
                    if (current != null) {
                        final double pct = current.percentComplete();
                        final String msg = current.message();
                        final long items = current.processedItems();
                        Platform.runLater(() -> {
                            progressPercent.set(pct / 100.0);
                            progressDetail.set(msg);
                            statusMessage.set("Scanned " + items + " items...");
                        });
                        var state = current.state();
                        if (state != null) {
                            String stateName = state.name();
                            if ("COMPLETED".equals(stateName) || "FAILED".equals(stateName) || "CANCELLED".equals(stateName)) {
                                break;
                            }
                        }
                    }
                    Thread.sleep(500);
                }

                Platform.runLater(() -> {
                    scanning.set(false);
                    progressPercent.set(1.0);
                    statusMessage.set("Scan complete — " + allFiles.size() + " recoverable file(s) found");
                    progressDetail.set("Done");
                });

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    scanning.set(false);
                    statusMessage.set("Scan interrupted");
                });
            } catch (Exception e) {
                log.error("Scan failed", e);
                Platform.runLater(() -> {
                    scanning.set(false);
                    statusMessage.set("Scan error: " + e.getMessage());
                    progressDetail.set("Error: " + e.getMessage());
                });
            }
        }, "recoveryx-scan-thread");
        scanThread.setDaemon(true);
        scanThread.start();
    }

    private void addFile(RecoverableFile file) {
        RecoverableFileViewModel vm = new RecoverableFileViewModel(file);
        vm.selectedProperty().addListener((obs, o, n) -> {
            int delta = n ? 1 : -1;
            selectedCount.set(selectedCount.get() + delta);
        });
        allFiles.add(vm);
        totalFound.set(allFiles.size());
    }

    public void cancelScan() {
        cancelled.set(true);
        if (scanId != null) {
            try { scanService.cancelScan(scanId); } catch (Exception ignored) {}
        }
        scanning.set(false);
        statusMessage.set("Scan cancelled");
    }

    public void selectAll(boolean select) {
        filteredFiles.forEach(f -> f.setSelected(select));
        selectedCount.set((int) filteredFiles.stream().filter(RecoverableFileViewModel::isSelected).count());
    }

    public void applyFilter(Predicate<RecoverableFileViewModel> predicate) {
        filteredFiles.setPredicate(predicate);
    }

    public void clearFilter() {
        filteredFiles.setPredicate(f -> true);
    }

    public void filterByCategory(FileCategory category) {
        filteredFiles.setPredicate(f -> f.getCategory() == category);
    }

    public List<RecoverableFileViewModel> getSelectedFiles() {
        return filteredFiles.stream().filter(RecoverableFileViewModel::isSelected).toList();
    }

    public ObservableList<RecoverableFileViewModel> getFilteredFiles() { return filteredFiles; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public StringProperty progressDetailProperty() { return progressDetail; }
    public DoubleProperty progressPercentProperty() { return progressPercent; }
    public BooleanProperty scanningProperty() { return scanning; }
    public IntegerProperty totalFoundProperty() { return totalFound; }
    public IntegerProperty selectedCountProperty() { return selectedCount; }
}
