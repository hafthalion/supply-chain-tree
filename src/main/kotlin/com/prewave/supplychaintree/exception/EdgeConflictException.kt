package com.prewave.supplychaintree.exception

import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class EdgeConflictException(cause: Exception) : ErrorResponseException(
    CONFLICT, ProblemDetail.forStatusAndDetail(CONFLICT, "An existing edge conflict occurred when generating a tree"), cause
)
