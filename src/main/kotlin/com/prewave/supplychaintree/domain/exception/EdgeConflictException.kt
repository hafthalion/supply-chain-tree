package com.prewave.supplychaintree.domain.exception

import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.web.server.ResponseStatusException

class EdgeConflictException(cause: Exception) :
    ResponseStatusException(CONFLICT, "A conflict with an unspecified existing edge occurred when creating a tree", cause)
