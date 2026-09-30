package com.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String distLocation = "file:C:/Users/35416/Desktop/FaceAttendanceGPSTotal/03-PC-Admin-Vue/dist/";
        
        registry.addResourceHandler("/**")
                .addResourceLocations(distLocation)
                .setCachePeriod(0);
    }
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/attendance/list").setViewName("forward:/index.html");
        registry.addViewController("/staff/list").setViewName("forward:/index.html");
        registry.addViewController("/location/set").setViewName("forward:/index.html");
    }
}