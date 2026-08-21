package com.recoveryx.ui.viewmodel.scan;

import com.recoveryx.common.enumtype.FileCategory;
import com.recoveryx.core.domain.file.RecoverableFile;
import javafx.beans.property.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * ViewModel wrapping a single RecoverableFile for display in the results table.
 */
public class RecoverableFileViewModel {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final RecoverableFile file;

    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty extension = new SimpleStringProperty();
    private final StringProperty sizeText = new SimpleStringProperty();
    private final StringProperty deletedDateText = new SimpleStringProperty();
    private final StringProperty originalPath = new SimpleStringProperty();
    private final StringProperty recoveryChanceText = new SimpleStringProperty();

    public RecoverableFileViewModel(RecoverableFile file) {
        this.file = file;
        this.name.set(file.name());
        this.extension.set(file.extension().toUpperCase());
        this.sizeText.set(formatSize(file.fileSize()));
        this.deletedDateText.set(formatDate(file.deletedDate() != null ? file.deletedDate() : file.modifiedDate()));
        this.originalPath.set(file.originalPath() != null ? file.originalPath() : file.currentPath());
        this.recoveryChanceText.set(formatChance(file));
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String formatDate(Instant instant) {
        if (instant == null) return "Unknown";
        return DATE_FMT.format(instant);
    }

    private String formatChance(RecoverableFile f) {
        if (f.recoveryChance() == null) return "Unknown";
        return switch (f.recoveryChance()) {
            case HIGH -> "✅ High";
            case MEDIUM -> "⚠️ Medium";
            case LOW -> "❌ Low";
            default -> f.recoveryChance().name();
        };
    }

    public RecoverableFile getFile() { return file; }

    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }

    public StringProperty nameProperty() { return name; }
    public StringProperty extensionProperty() { return extension; }
    public StringProperty sizeTextProperty() { return sizeText; }
    public StringProperty deletedDateTextProperty() { return deletedDateText; }
    public StringProperty originalPathProperty() { return originalPath; }
    public StringProperty recoveryChanceTextProperty() { return recoveryChanceText; }

    public FileCategory getCategory() { return file.category(); }
}
