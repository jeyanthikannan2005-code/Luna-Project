package com.lunette.gifts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${lunette.upload.dir:./data/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Expose the uploads directory under /uploads/**
        File uploadFolder = Paths.get(uploadDir).toFile();
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String uploadPath = uploadFolder.toURI().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);

        // Standard classpath static resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
