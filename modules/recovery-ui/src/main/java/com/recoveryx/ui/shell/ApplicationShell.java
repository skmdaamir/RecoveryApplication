package com.recoveryx.ui.shell;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Root desktop shell responsible for presenting the primary application window.
 */
@Component
@ConditionalOnProperty(
        prefix = "recoveryx.ui",
        name = "shell-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ApplicationShell {

    private final BorderPane rootLayout;

    public ApplicationShell() {
        this.rootLayout = new BorderPane();
        this.rootLayout.setPrefSize(1280, 800);
        
        // Let's explicitly center and format the label text inside the BorderPane layout
        Label mainLabel = new Label("RecoveryX Pro - Enterprise Recovery Platform");
        BorderPane.setAlignment(mainLabel, Pos.CENTER);
        this.rootLayout.setCenter(mainLabel);
    }

    /**
     * Exposes the root layout pane to be attached to the JavaFX scene.
     *
     * @return the primary BorderPane container
     */
    public BorderPane getView() {
        return this.rootLayout;
    }
}