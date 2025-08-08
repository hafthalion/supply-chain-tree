package com.prewave.supplychaintree.api

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class SupplyChainTreeApiAdvice {
    @ExceptionHandler(Exception::class)
    fun exceptionHandling(e: Exception): ResponseEntity<Any> {
        val status = if (e is ErrorResponse) e.statusCode else INTERNAL_SERVER_ERROR

        return ResponseEntity.status(status).body(
            object {
                val status = status.value()
                val error = e.toString()
            }
        )
    }
}
