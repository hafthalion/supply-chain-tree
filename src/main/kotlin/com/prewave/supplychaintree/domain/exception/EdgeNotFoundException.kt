package com.prewave.supplychaintree.domain.exception

import com.prewave.supplychaintree.domain.TreeEdge
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.server.ResponseStatusException

class EdgeNotFoundException(edge: TreeEdge) :
    ResponseStatusException(NOT_FOUND, "Tree edge from ${edge.fromNodeId} to ${edge.toNodeId} does not exist")
