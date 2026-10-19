package com.pharmacold.backend.infrastructure.config.async;

import com.pharmacold.backend.domain.event.TrackableNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
public class NotificationAsyncExceptionHandlerStrategy implements AsyncExceptionHandlerStrategy {

    @Override
    public boolean supports(Method method, Object[] params) {
        if (params == null || params.length == 0) return false;
        return Arrays.stream(params).anyMatch(event -> event instanceof TrackableNotification);
    }

    @Override
    public void handle(Throwable ex, Method method, Object... params) {
        for (Object param : params) {
            if (param instanceof TrackableNotification notification) {
                log.error(
                        "--- NOTIFICATION ASYNC FAILURE | Method: {}.{}() | Context: [{}] | Error: {} ---",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        notification.getContextSummary(),
                        ex.getMessage()
                );
                return;
            }
        }
    }
}
