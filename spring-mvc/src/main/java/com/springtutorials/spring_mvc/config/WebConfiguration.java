package com.springtutorials.spring_mvc.config;

import com.springtutorials.spring_mvc.interceptor.ControllerRequestInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerRequestInterceptor());
    }

    public ControllerRequestInterceptor controllerRequestInterceptor() {
        return new ControllerRequestInterceptor();
    }
}
