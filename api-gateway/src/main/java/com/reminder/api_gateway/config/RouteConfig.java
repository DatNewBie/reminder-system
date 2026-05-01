package com.reminder.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class RouteConfig {

    @Value("${services.auth.url:http://localhost:8084}")
    private String authServiceUrl;

    @Value("${services.reminder.url:http://localhost:8081}")
    private String reminderServiceUrl;

    @Value("${services.scheduler.url:http://localhost:8082}")
    private String schedulerServiceUrl;

    @Value("${services.notification.url:http://localhost:8083}")
    private String notificationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {
        return route()
                .route(RequestPredicates.path("/api/auth/**"), http(authServiceUrl))
                .route(RequestPredicates.path("/api/reminders/**"), http(reminderServiceUrl))
                .route(RequestPredicates.path("/api/scheduler/**"), http(schedulerServiceUrl))
                .route(RequestPredicates.path("/api/notifications/**"), http(notificationServiceUrl))
                .build();
    }
}
