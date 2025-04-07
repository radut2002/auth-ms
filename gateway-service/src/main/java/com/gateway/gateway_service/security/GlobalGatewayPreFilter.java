package com.gateway.gateway_service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class GlobalGatewayPreFilter extends AbstractGatewayFilterFactory<GlobalGatewayPreFilter.Config> {
    
    @Autowired
    private WebClient  webClient;
    
    @Value("${auth.uri}")
    public String AUTH_URI;

    public GlobalGatewayPreFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {     
        return (exchange, chain) ->  webClient.post()
        .uri(AUTH_URI)
        .headers((httpHeaders -> httpHeaders.putAll(exchange.getRequest().getHeaders())))
        .retrieve()
        .onStatus(HttpStatus::isError, response -> {
            return Mono.defer(() -> setErrorResponse(exchange.getResponse()).setComplete().then(Mono.empty()));
        })
        .bodyToMono(Void.class)
        .then(chain.filter(exchange));    
    }

    private ServerHttpResponse setErrorResponse(ServerHttpResponse serverHttpResponse) {
        serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
        return serverHttpResponse;
    }

    public static class Config {
        private String name;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}