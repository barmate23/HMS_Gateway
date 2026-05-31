//package com.gateway.SpringCloudGateway.config;
//
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//public class PathUpdatingFilter implements WebFilter {
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
////        String updatedPath = "/new/path"; // Replace this with your desired updated path
//        String originalPath = exchange.getRequest().getPath().pathWithinApplication().value();
//        String prefixToRemove = "/sms";
//
//        String url = removePrefix(originalPath, prefixToRemove);
////         Create a new ServerWebExchange with the updated path
//        ServerWebExchange updatedExchange = exchange.mutate()
//                .request(request -> request
//                        .path(url)
//                        .build())
//                .build();
//        System.out.println("@@@@@@@@@@@@@@@@FIrst filter for update path  "+ url);
////         Continue processing the request with the updated path
//        return chain.filter(exchange);
//    }
//    private static String removePrefix(String original, String prefix) {
//        if (original.startsWith(prefix)) {
//            return original.substring(prefix.length());
//        }
//        return original;
//    }
//}
