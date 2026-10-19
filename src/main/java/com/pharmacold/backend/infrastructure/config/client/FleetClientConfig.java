package com.pharmacold.backend.infrastructure.config.client;

import com.pharmacold.backend.infrastructure.client.FleetLogisticsClient;
import com.pharmacold.backend.infrastructure.config.client.interceptor.AuditInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@RequiredArgsConstructor
@Configuration
public class FleetClientConfig {

    private final AuditInterceptor auditInterceptor;

    @Bean
    public RestClient fleetRestClient(
            @Value("${api-clients.fleet.webhook.base-url}") String baseUrl,
            @Value("${api-clients.fleet.webhook.connect-timeout-ms}") int connectTimeout,
            @Value("${api-clients.fleet.webhook.read-timeout-ms}") int readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestInterceptor(auditInterceptor)
                .build();

    }

    @Bean
    public FleetLogisticsClient fleetLogisticsClient(@Qualifier("fleetRestClient") RestClient fleetRestClient) {
        HttpServiceProxyFactory serviceProxyFactory = HttpServiceProxyFactory.
                builderFor(RestClientAdapter.create(fleetRestClient))
                .build();
        return serviceProxyFactory.createClient(FleetLogisticsClient.class);
    }
}
