package com.pharmacold.backend.application.notification;

public interface NotificationStrategy<T> {

    void send(T event);
}
