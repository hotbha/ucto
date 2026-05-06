package com.ucto.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class OAuthRequest {

    @NotBlank
    private String provider; // "google" (MVP only; Facebook deferred to Phase 2)

    @NotBlank
    private String token;    // OAuth access token from frontend

    public OAuthRequest() {}

    public OAuthRequest(String provider, String token) {
        this.provider = provider;
        this.token = token;
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
