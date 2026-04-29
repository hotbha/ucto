package com.ucto.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank
    private String phoneNumber;

    public OtpRequest() {}

    public OtpRequest(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
