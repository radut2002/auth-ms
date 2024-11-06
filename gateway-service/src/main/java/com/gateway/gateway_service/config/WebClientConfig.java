package com.gateway.gateway_service.config;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;


@Configuration
public class WebClientConfig {

    @Value("${auth.baseuri}")
    public String AUTH_BASEURI;

    @Bean
    public WebClient getWebClient() throws SSLException {

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()                
                .codecs(configurer -> configurer.defaultCodecs()
                        .maxInMemorySize(1048576)) // Set buffer size to 1 MB                        
                .build();

        // Disable ssl verification
        SslContext context = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(context))                
                // .proxyWithSystemProperties() // Use JVM level System proxy
                .responseTimeout(Duration.ofSeconds(30))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30,
                                TimeUnit.SECONDS))
                                
                        .addHandlerLast(new WriteTimeoutHandler(30)));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        

        return WebClient
                .builder()
                .baseUrl(AUTH_BASEURI)
                .exchangeStrategies(exchangeStrategies)
                .clientConnector(connector)                
                .build();
    }

        
 /*    @Bean
        public WebClient webClient() {
            return WebClient.builder().build();
        }   */
}