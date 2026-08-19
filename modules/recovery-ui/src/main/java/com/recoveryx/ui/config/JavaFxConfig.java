package com.recoveryx.ui.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;

@SpringBootApplication(exclude = {
    WebMvcAutoConfiguration.class
})
public class JavaFxConfig {
    //Configuration class with no additional beans required at this phase
}
