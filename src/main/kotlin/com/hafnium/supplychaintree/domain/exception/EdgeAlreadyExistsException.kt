package com.hafnium.supplychaintree.domain.exception

import com.hafnium.supplychaintree.domain.TreeEdge
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.web.server.ResponseStatusException

class EdgeAlreadyExistsException(edge: TreeEdge, cause: Exception) :
    ResponseStatusException(CONFLICT, "Tree edge from ${edge.fromNodeId} to ${edge.toNodeId} already exists", cause)
