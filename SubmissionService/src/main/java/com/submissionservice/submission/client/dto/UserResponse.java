package com.submissionservice.submission.client.dto;

public class UserResponse {
    private Long id;
    private String keycloakId;
    private String username;

    public UserResponse() {
    }

    public UserResponse(Long id, String keycloakId, String username) {
        this.id = id;
        this.keycloakId = keycloakId;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

