package com.gateway.gateway_service.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gateway.gateway_service.security.AuthenticationFilter;

@Configuration
public class GatewayConfig {
    @Value("${auth.baseuri}")
    public String AUTH_URI;

    @Value("${customer.uri}")
    public String CUSTOMER_URI;

    @Autowired
    AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")                                           
                        .filters(f -> f.filter(authenticationFilter))                      
                        .uri(AUTH_URI))
                .route("customer-service", r -> r.path("/api/**")                       
                        .filters(f -> f.filter(authenticationFilter))                      
                        .uri(CUSTOMER_URI))
                        .build();           
    }  
}
