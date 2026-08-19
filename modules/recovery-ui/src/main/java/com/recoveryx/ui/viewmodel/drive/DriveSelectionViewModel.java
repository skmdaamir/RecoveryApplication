package com.recoveryx.ui.viewmodel.drive;

import com.recoveryx.core.model.device.StorageDevice;
import com.recoveryx.core.model.scan.ScanMode;
import com.recoveryx.core.model.scan.ScanRequest;
import com.recoveryx.core.model.scan.ScanSession;
import com.recoveryx.core.service.DeviceDiscoveryService;
import com.recoveryx.core.service.ScanOrchestrationService;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Objects;

/**
 * MVVM ViewModel for the Drive Selection screen.
 * Handles device discovery, selection, and quick scan session creation.
 */
public class DriveSelectionViewModel {

    private final DeviceDiscoveryService deviceDiscoveryService;
    private final ScanOrchestrationService scanOrchestrationService;

    private final ObservableList<DeviceItemViewModel> devices = FXCollections.observableArrayList();
    private final ObjectProperty<DeviceItemViewModel> selectedDevice = new SimpleObjectProperty<>();

    private final StringProperty statusMessage = new SimpleStringProperty("Ready");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final DoubleProperty progress = new SimpleDoubleProperty(-1.0);

    private final ObjectProperty<ScanSession> activeSession = new SimpleObjectProperty<>();
    private final StringProperty lastError = new SimpleStringProperty("");

    private final javafx.beans.binding.BooleanBinding quickScanAllowed;

    public DriveSelectionViewModel(DeviceDiscoveryService deviceDiscoveryService,
                                   ScanOrchestrationService scanOrchestrationService) {
        this.deviceDiscoveryService = Objects.requireNonNull(deviceDiscoveryService, "deviceDiscoveryService");
        this.scanOrchestrationService = Objects.requireNonNull(scanOrchestrationService, "scanOrchestrationService");

        this.quickScanAllowed = Bindings.createBooleanBinding(
                () -> !loading.get()
                        && selectedDevice.get() != null
                        && selectedDevice.get().getDevice() != null
                        && selectedDevice.get().getDevice().isReadable(),
                loading, selectedDevice
        );
    }

    public ObservableList<DeviceItemViewModel> getDevices() {
        return devices;
    }

    public ObjectProperty<DeviceItemViewModel> selectedDeviceProperty() {
        return selectedDevice;
    }

    public DeviceItemViewModel getSelectedDevice() {
        return selectedDevice.get();
    }

    public void setSelectedDevice(DeviceItemViewModel selectedDevice) {
        this.selectedDevice.set(selectedDevice);
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public javafx.beans.binding.BooleanBinding quickScanAllowedProperty() {
        return quickScanAllowed;
    }

    public ObjectProperty<ScanSession> activeSessionProperty() {
        return activeSession;
    }

    public StringProperty lastErrorProperty() {
        return lastError;
    }

    public String getActiveSessionId() {
        ScanSession session = activeSession.get();
        return session == null ? "" : session.getSessionId();
    }

    public void loadDevices() {
        if (loading.get()) {
            return;
        }

        Task<List<DeviceItemViewModel>> task = new Task<>() {
            @Override
            protected List<DeviceItemViewModel> call() {
                updateMessage("Discovering storage devices...");
                updateProgress(-1, 1);

                List<StorageDevice> discovered = deviceDiscoveryService.discoverDevices();

                updateMessage("Preparing device list...");
                return discovered.stream()
                        .map(DeviceItemViewModel::new)
                        .toList();
            }
        };

        statusMessage.bind(task.messageProperty());
        progress.bind(task.progressProperty());
        loading.set(true);
        lastError.set("");

        task.setOnSucceeded(event -> {
            statusMessage.unbind();
            progress.unbind();

            devices.setAll(task.getValue());

            if (devices.isEmpty()) {
                selectedDevice.set(null);
                statusMessage.set("No readable storage devices detected");
            } else {
                if (selectedDevice.get() == null) {
                    selectedDevice.set(devices.get(0));
                }
                statusMessage.set("Detected " + devices.size() + " storage device(s)");
            }

            progress.set(-1.0);
            loading.set(false);
        });

        task.setOnFailed(event -> {
            statusMessage.unbind();
            progress.unbind();

            Throwable ex = task.getException();
            lastError.set(ex == null ? "Unknown error while loading devices" : ex.getMessage());
            statusMessage.set("Failed to discover devices");
            progress.set(0.0);
            loading.set(false);
        });

        task.setOnCancelled(event -> {
            statusMessage.unbind();
            progress.unbind();
            statusMessage.set("Device discovery cancelled");
            progress.set(0.0);
            loading.set(false);
        });

        Thread thread = new Thread(task, "recoveryx-device-discovery");
        thread.setDaemon(true);
        thread.start();
    }

    public void refreshDevices() {
        loadDevices();
    }

    public ScanSession startQuickScan() {
        DeviceItemViewModel selected = selectedDevice.get();
        if (selected == null || selected.getDevice() == null) {
            throw new IllegalStateException("No device selected");
        }
        if (!selected.getDevice().isReadable()) {
            throw new IllegalStateException("Selected device is not readable");
        }

        ScanRequest request = ScanRequest.builder()
                .storageDevice(selected.getDevice())
                .scanMode(ScanMode.QUICK)
                .includeDeletedEntries(true)
                .includeRawSignatureScan(false)
                .build();

        ScanSession session = scanOrchestrationService.createSession(request);
        activeSession.set(session);
        statusMessage.set("Quick scan session created: " + session.getSessionId());
        return session;
    }
}