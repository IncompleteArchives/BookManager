package org.example.bookapp;

import org.example.bookapp.config.AppConfig;
import org.example.bookapp.console.ConsoleApplication;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BookApplication {
    public static void main(String[] args) {

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            ConsoleApplication application = context.getBean(ConsoleApplication.class);
            application.start();
        }
    }
}