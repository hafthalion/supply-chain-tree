package com.prewave.supplychaintree.domain.exception

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class EdgeNotFoundException(fromNodeId: Int, toNodeId: Int) :
    ErrorResponseException(NOT_FOUND, ProblemDetail.forStatusAndDetail(NOT_FOUND, "Tree edge from $fromNodeId to $toNodeId does not exist"), null)
