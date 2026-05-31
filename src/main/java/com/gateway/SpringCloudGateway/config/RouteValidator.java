package com.gateway.SpringCloudGateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            Const.PREFIX+Const.JWT_AUTHENTICATION_CONTROLLER,
            Const.PREFIX+Const.RESET_PASSWORD,
            Const.PREFIX+Const.GENERATE_OTP
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));



}
