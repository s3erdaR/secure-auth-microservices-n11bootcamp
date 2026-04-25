package com.n11bootcamp.notification_service.event;

public class UserRegisteredEvent {

    private String username;
    private String email;

    public UserRegisteredEvent() {
    }

    public UserRegisteredEvent(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
