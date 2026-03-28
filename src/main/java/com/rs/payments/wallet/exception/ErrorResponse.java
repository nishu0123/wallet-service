package com.rs.payments.wallet.exception;

import java.time.LocalDateTime;

/**
 * The Blue Phase: Standardizing the API response.
 * Using a record ensures the error structure is immutable and consistent.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {}