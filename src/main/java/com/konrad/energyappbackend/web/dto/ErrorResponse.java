package com.konrad.energyappbackend.web.dto;

import java.time.LocalDateTime;

/**
 * Standard error response format for API errors.
 *
 * @param timestamp when the error occurred
 * @param status HTTP status code
 * @param error error type/title
 * @param message detailed error message
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
    public ErrorResponse(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message);
    }
}