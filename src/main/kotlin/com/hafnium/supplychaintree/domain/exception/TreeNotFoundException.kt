package com.hafnium.supplychaintree.domain.exception

import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.server.ResponseStatusException

class TreeNotFoundException(fromNodeId: Int) :
    ResponseStatusException(NOT_FOUND, "Tree starting from $fromNodeId does not exist")
