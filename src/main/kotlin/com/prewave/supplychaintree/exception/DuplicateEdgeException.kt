package com.prewave.supplychaintree.exception

import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class DuplicateEdgeException(fromId: Int, toId: Int, cause: DuplicateKeyException) : ErrorResponseException(
    CONFLICT, ProblemDetail.forStatusAndDetail(CONFLICT, "Tree edge from $fromId to $toId already present"), cause
)
