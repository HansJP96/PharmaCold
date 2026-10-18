package com.pharmacold.backend.infrastructure.config.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final List<AsyncExceptionHandlerStrategy> asyncExceptionHandlerStrategies;

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        asyncExceptionHandlerStrategies.stream()
                .filter(handler -> handler.supports(method, params))
                .findFirst()
                .ifPresentOrElse(
                        handlerStrategy -> handlerStrategy.handle(ex, method, params),
                        () -> log.error("Unhandled async exception in {}.{}(): {}",
                                method.getDeclaringClass().getSimpleName(),
                                method.getName(),
                                ex.getMessage()
                        )
                );
    }
}
