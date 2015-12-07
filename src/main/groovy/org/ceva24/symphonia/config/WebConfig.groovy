package org.ceva24.symphonia.config

import org.ceva24.symphonia.filter.RequestLoggingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

@Configuration
class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

        configurer.ignoreAcceptHeader true
    }

    @Bean
    RequestLoggingFilter requestLoggingFilter() {

        return new RequestLoggingFilter()
    }
}