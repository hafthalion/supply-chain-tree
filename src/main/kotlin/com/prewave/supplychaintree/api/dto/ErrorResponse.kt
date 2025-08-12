package com.prewave.supplychaintree.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "An error response")
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String,
)