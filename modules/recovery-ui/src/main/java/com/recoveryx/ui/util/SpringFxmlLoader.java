package com.recoveryx.ui.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(SpringFxmlLoader.class);

    private final ApplicationContext applicationContext;

    public SpringFxmlLoader(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext");
    }

    public Parent load(String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                // Try classloader as fallback
                resource = getClass().getClassLoader().getResource(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
            }
            if (resource == null) {
                throw new IllegalArgumentException("FXML resource not found: " + resourcePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            loader.setControllerFactory(applicationContext::getBean);
            return loader.load();
        } catch (Exception ex) {
            log.error("Failed to load FXML: {} - Cause: {}", resourcePath, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to load FXML: " + resourcePath + " (" + ex.getMessage() + ")", ex);
        }
    }
}