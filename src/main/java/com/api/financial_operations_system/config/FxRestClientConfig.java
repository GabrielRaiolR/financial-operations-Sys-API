package com.api.financial_operations_system.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class FxRestClientConfig {

    public static final String FRANKFURTER_REST_CLIENT = "frankfurterRestClient";

    @Bean
    @Qualifier(FRANKFURTER_REST_CLIENT)
    public RestClient frankfurterRestClient(
            @Value("${app.fx.frankfurter-base-url:https://api.frankfurter.dev/v1}") String baseUrl) {
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(Duration.ofSeconds(5));
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(Duration.ofSeconds(15));
        return RestClient
                .builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
