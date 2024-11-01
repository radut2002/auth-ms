package com.gateway.gateway_service.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {

    @Autowired
    private RouterValidator routerValidator;
    

    @Value("${jwt.prefix}")
    public String TOKEN_PREFIX;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isSecured.test(request)) {
            if (this.IsCookieWithTokenExist(request, TOKEN_PREFIX))
                return this.onError(exchange, String.format("Cookie %s is missing in request", TOKEN_PREFIX), HttpStatus.UNAUTHORIZED);

            final String token = this.getCookieValue(request, TOKEN_PREFIX);
            if (token != null ) {
                final DecodedJWT validatedJWT;
                      
                try {
                    validatedJWT = JWT.decode(token);               
                } catch (JWTDecodeException e) {
                    return this.onError(exchange, e.getMessage(), HttpStatus.UNAUTHORIZED);
                }     
                
                if (!isJWTExpired(validatedJWT)) {
                    this.populateRequestWithHeaders(exchange);
                }
            }            
        }
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private void populateRequestWithHeaders(ServerWebExchange exchange) {        
        exchange.getRequest().mutate()
            .header(HttpHeaders.SET_COOKIE, getCookieValue(exchange.getRequest(), TOKEN_PREFIX)).build();
    }

    private boolean isJWTExpired(DecodedJWT decodedJWT) {
        Date expiresAt = decodedJWT.getExpiresAt();
        return expiresAt.before(new Date());
    }

    private String getCookieValue(ServerHttpRequest req, String cookieName) {
        return req.getCookies()
                .getFirst(cookieName)
                .getValue();
    }

    private boolean  IsCookieWithTokenExist(ServerHttpRequest req, String cookieName) {
        var cookie =  req.getCookies()
        .getFirst(cookieName);
       return cookie == null;
    }
}