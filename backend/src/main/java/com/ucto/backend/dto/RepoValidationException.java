package com.ucto.backend.dto;

/**
 * Exception for repo configuration validation failures.
 */
public class RepoValidationException extends RuntimeException {
    public RepoValidationException(String message) {
        super(message);
    }
}
