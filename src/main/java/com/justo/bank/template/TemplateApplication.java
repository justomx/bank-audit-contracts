package com.justo.bank.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Service entry point.
 *
 * <p>INIT: {@code scripts/init-repo.sh} renames this class and its package.
 *
 * <p>Deliberately empty: cross-cutting configuration annotations
 * ({@code @EnableCaching}, {@code @EnableScheduling}, etc.) live in dedicated
 * classes under {@code config/}, not here. This way each concern is
 * locatable by its file name.
 */
@SpringBootApplication
public class TemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateApplication.class, args);
    }
}
