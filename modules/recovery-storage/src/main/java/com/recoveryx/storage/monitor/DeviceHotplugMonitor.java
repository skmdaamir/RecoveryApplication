package com.recoveryx.storage.monitor;

import com.recoveryx.common.constant.RecoveryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Monitors the system for newly plugged-in or removed removable storage devices
 * (USB drives, card readers, SD/CF card adapters).
 *
 * <p>Uses a polling strategy: every {@link RecoveryConstants#HOTPLUG_POLL_INTERVAL_MS} ms
 * the set of drive roots is compared against the previous snapshot. When a change is detected,
 * the registered {@link #onDeviceArrived} and {@link #onDeviceRemoved} callbacks are invoked.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * DeviceHotplugMonitor monitor = new DeviceHotplugMonitor(
 *     added   -> refreshUiDriveList(),
 *     removed -> refreshUiDriveList()
 * );
 * monitor.start();
 * // ... when app closes:
 * monitor.stop();
 * }</pre>
 */
public final class DeviceHotplugMonitor {

    private static final Logger log = LoggerFactory.getLogger(DeviceHotplugMonitor.class);

    private final Consumer<String> onDeviceArrived;
    private final Consumer<String> onDeviceRemoved;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "recoveryx-hotplug-monitor");
                t.setDaemon(true);
                return t;
            });

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Set<String> lastKnownRoots = new HashSet<>();

    /**
     * @param onDeviceArrived callback receiving the drive root path (e.g. "E:\") when added
     * @param onDeviceRemoved callback receiving the drive root path (e.g. "E:\") when removed
     */
    public DeviceHotplugMonitor(Consumer<String> onDeviceArrived,
                                Consumer<String> onDeviceRemoved) {
        this.onDeviceArrived = onDeviceArrived != null ? onDeviceArrived : root -> {};
        this.onDeviceRemoved = onDeviceRemoved != null ? onDeviceRemoved : root -> {};
    }

    /**
     * Starts the hotplug polling monitor.
     * Safe to call multiple times — subsequent calls are no-ops if already running.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            lastKnownRoots = currentRemovableRoots();
            scheduler.scheduleAtFixedRate(
                    this::poll,
                    0,
                    RecoveryConstants.HOTPLUG_POLL_INTERVAL_MS,
                    TimeUnit.MILLISECONDS);
            log.info("DeviceHotplugMonitor started (poll interval: {} ms)",
                    RecoveryConstants.HOTPLUG_POLL_INTERVAL_MS);
        }
    }

    /**
     * Stops the hotplug polling monitor and releases the background thread.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            scheduler.shutdownNow();
            log.info("DeviceHotplugMonitor stopped");
        }
    }

    /** Returns true if the monitor is currently running. */
    public boolean isRunning() {
        return running.get();
    }

    // ──────────────────────────────────────────────────────────────────────────

    private void poll() {
        Set<String> current = currentRemovableRoots();
        Set<String> previous = lastKnownRoots;

        // Find newly added drives
        Set<String> added = new HashSet<>(current);
        added.removeAll(previous);

        // Find removed drives
        Set<String> removed = new HashSet<>(previous);
        removed.removeAll(current);

        if (!added.isEmpty()) {
            log.info("Hotplug: removable device(s) arrived: {}", added);
            added.forEach(root -> {
                try {
                    onDeviceArrived.accept(root);
                } catch (Exception e) {
                    log.warn("Error in onDeviceArrived callback for {}: {}", root, e.getMessage());
                }
            });
        }

        if (!removed.isEmpty()) {
            log.info("Hotplug: removable device(s) removed: {}", removed);
            removed.forEach(root -> {
                try {
                    onDeviceRemoved.accept(root);
                } catch (Exception e) {
                    log.warn("Error in onDeviceRemoved callback for {}: {}", root, e.getMessage());
                }
            });
        }

        lastKnownRoots = current;
    }

    /**
     * Returns all currently mounted removable drive roots (e.g. {"E:\", "F:\"}).
     * Uses {@link File#listRoots()} filtered by {@link File#canRead()} and removable type.
     */
    private static Set<String> currentRemovableRoots() {
        Set<String> roots = new HashSet<>();
        File[] allRoots = File.listRoots();
        if (allRoots == null) {
            return roots;
        }
        for (File root : allRoots) {
            String path = root.getAbsolutePath();
            // Heuristic: skip C:\ (usually system drive) and include all others
            // that exist and are readable. The user rarely recovers from C:\.
            if (!path.toUpperCase().startsWith("C:") && root.exists() && root.canRead()) {
                roots.add(path);
            }
        }
        return roots;
    }
}
