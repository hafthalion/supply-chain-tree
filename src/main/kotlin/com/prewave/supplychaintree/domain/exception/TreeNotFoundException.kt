package com.prewave.supplychaintree.domain.exception

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.web.ErrorResponseException

class TreeNotFoundException(fromNodeId: Int) :
    ErrorResponseException(NOT_FOUND, ProblemDetail.forStatusAndDetail(NOT_FOUND, "Tree starting from $fromNodeId does not exist"), null)
