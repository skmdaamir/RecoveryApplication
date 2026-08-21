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
 * Spring-aware FXML loader that delegates controller creation
 * to the Spring application context.
 */
@Component
public class SpringFxmlLoader {

    private static final Logger log = LoggerFactory.getLogger(SpringFxmlLoader.class);

    private final ApplicationContext applicationContext;

    public SpringFxmlLoader(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(
                applicationContext,
                "applicationContext");
    }

    /**
     * Loads an FXML file and returns its root node.
     */
    public Parent load(String resourcePath) {
        try {
            FXMLLoader loader = createLoader(resourcePath);
            return loader.load();

        } catch (IOException | RuntimeException ex) {
            log.error(
                    "Failed to load FXML: {} - Cause: {}",
                    resourcePath,
                    ex.getMessage(),
                    ex);

            throw new IllegalStateException(
                    "Failed to load FXML: "
                            + resourcePath
                            + " ("
                            + ex.getMessage()
                            + ")",
                    ex);
        }
    }

    /**
     * Loads an FXML file and returns both its root node
     * and its Spring-managed controller.
     */
    public <T> LoadedView<T> loadWithController(
            String resourcePath,
            Class<T> controllerType) {
        try {
            FXMLLoader loader = createLoader(resourcePath);

            Parent root = loader.load();
            Object controller = loader.getController();

            if (controller == null) {
                throw new IllegalStateException(
                        "No controller was created for FXML: "
                                + resourcePath);
            }

            if (!controllerType.isInstance(controller)) {
                throw new IllegalStateException(
                        "Incorrect controller for FXML: "
                                + resourcePath
                                + ". Expected: "
                                + controllerType.getName()
                                + ", actual: "
                                + controller.getClass().getName());
            }

            return new LoadedView<>(
                    root,
                    controllerType.cast(controller));

        } catch (IOException | RuntimeException ex) {
            log.error(
                    "Failed to load FXML with controller: {} - Cause: {}",
                    resourcePath,
                    ex.getMessage(),
                    ex);

            throw new IllegalStateException(
                    "Failed to load FXML with controller: "
                            + resourcePath
                            + " ("
                            + ex.getMessage()
                            + ")",
                    ex);
        }
    }

    private FXMLLoader createLoader(String resourcePath)
            throws IOException {
        URL resource = getClass().getResource(resourcePath);

        if (resource == null) {
            String classpathPath = resourcePath.startsWith("/")
                    ? resourcePath.substring(1)
                    : resourcePath;

            resource = getClass()
                    .getClassLoader()
                    .getResource(classpathPath);
        }

        if (resource == null) {
            throw new IOException(
                    "FXML resource not found: " + resourcePath);
        }

        FXMLLoader loader = new FXMLLoader(resource);

        /*
         * The FXML file must contain fx:controller.
         * Spring creates that controller through this factory.
         */
        loader.setControllerFactory(applicationContext::getBean);

        return loader;
    }

    /**
     * Contains an FXML root node and its controller.
     *
     * Java 21 supports nested record classes.
     */
    public record LoadedView<T>(
            Parent root,
            T controller) {
    }
}