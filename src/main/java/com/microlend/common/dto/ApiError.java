package com.microlend.common.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API Error Response")
public class ApiError {

    @Schema(description = "Timestamp when the error occurred", example = "2026-09-13T10:00:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP Status code", example = "400")
    private int status;
    
    @Schema(description = "HTTP Status error description", example = "Bad Request")
    private String error;
    
    @Schema(description = "Detailed error message", example = "Invalid input provided")
    private String message;
    
    @Schema(description = "Path of the request that caused the error", example = "/api/resource")
    private String path;

    public ApiError() {
    }

    public ApiError(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
