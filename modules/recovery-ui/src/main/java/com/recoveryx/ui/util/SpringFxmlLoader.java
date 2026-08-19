package com.recoveryx.ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Spring-aware FXML loader that delegates controller creation to the Spring application context.
 */
@Component
public class SpringFxmlLoader {

    private final ApplicationContext applicationContext;

    public SpringFxmlLoader(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
    }

    public Parent load(String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                throw new IllegalArgumentException("FXML resource not found: " + resourcePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            return loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load FXML: " + resourcePath, ex);
        }
    }
}