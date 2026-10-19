package com.pharmacold.backend.infrastructure.config.client.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class AuditInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("- [REQUEST] {} -> <<{}>> -", request.getMethod(), request.getURI());
        ClientHttpResponse response = execution.execute(request, body);
        log.info("- [RESPONSE] {} -> <<{}>> Status({}) -", request.getMethod(), request.getURI(), response.getStatusCode());
        return response;
    }
}
