package com.prewave.supplychaintree.exception

import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class EdgeAlreadyExistsException(fromId: Int, toId: Int, cause: Exception?) : ErrorResponseException(
    CONFLICT, ProblemDetail.forStatusAndDetail(CONFLICT, "Tree edge from $fromId to $toId already exists"), cause
)
