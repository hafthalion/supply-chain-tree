package com.hafnium.supplychaintree.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "An error response")
data class ErrorResponse(
    val timestamp: String,
    @Schema(description = "HTTP status code")
    val status: Int,
    val error: String,
    @Schema(description = "Error description")
    val message: String,
    val path: String,
)