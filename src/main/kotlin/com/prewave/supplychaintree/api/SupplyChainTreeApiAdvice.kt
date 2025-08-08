package com.prewave.supplychaintree.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class SupplyChainTreeApiAdvice {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(Exception::class)
    fun exceptionHandling(e: Exception): ResponseEntity<Any> {
        if (e is ErrorResponse) {
            logger.warn("Error response", e)
            return ResponseEntity.status(e.statusCode).body(SupplyChainTreeApiError(e.statusCode.value(), e.body.detail))
        } else {
            logger.error("Unknown exception", e)
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(SupplyChainTreeApiError(INTERNAL_SERVER_ERROR.value(), e.message))
        }
    }
}

data class SupplyChainTreeApiError(
    val status: Int,
    val error: String?,
)