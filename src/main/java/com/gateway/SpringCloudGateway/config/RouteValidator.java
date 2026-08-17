package com.gateway.SpringCloudGateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/hmsUserService/v1/auth/",
            "/api/hmsUserService/v1/licenses/activate",
            "/api/hmsUserService/v1/licenses/status",
            "/api/hmsUserService/v1/licenses/validate",
            "/api/hmsUserService/v1/licenses/onboard-client",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs/",
            "/actuator/health"
    );

    public static final List<String> licenseExemptEndpoints = List.of(
            "/api/hmsUserService/v1/auth/",
            "/api/hmsUserService/v1/licenses/"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));

    public Predicate<ServerHttpRequest> isLicenseExempt =
            request -> licenseExemptEndpoints
                    .stream()
                    .anyMatch(uri -> request.getURI().getPath().startsWith(uri));
}
