package com.pharmacold.backend.infrastructure.config.async;

import java.lang.reflect.Method;

public interface AsyncExceptionHandlerStrategy {

    boolean supports(Method method, Object[] objects);

    void handle(Throwable ex, Method method, Object... params);
}
