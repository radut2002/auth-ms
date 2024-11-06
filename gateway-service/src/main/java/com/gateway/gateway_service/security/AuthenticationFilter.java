package com.gateway.gateway_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private RouterValidator routerValidator;
    
    @Autowired
    private WebClient  webClient;

    @Value("${auth.uri}")
    public String AUTH_URI;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (routerValidator.isSecured.test(request)) {
            try {
                    return webClient.post()
                    .uri(AUTH_URI)
                    .headers((httpHeaders -> httpHeaders.putAll(exchange.getRequest().getHeaders())))
                    .retrieve()
                    .onStatus(HttpStatus::isError, response -> {
                        return Mono.defer(() -> setErrorResponse(exchange.getResponse()).setComplete().then(Mono.empty()));                        
                    })                                     
                    .bodyToMono(Void.class)
                    .then(chain.filter(exchange));
                                                         
            } catch (Exception e) {
                return this.onError(exchange, e.getLocalizedMessage(), HttpStatus.UNAUTHORIZED);
            }
        }
        return chain.filter(exchange);
    }

    private ServerHttpResponse setErrorResponse(ServerHttpResponse serverHttpResponse) {
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);        
        return serverHttpResponse;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
}