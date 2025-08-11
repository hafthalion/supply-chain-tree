package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.repository.SupplyChainTreeRepository
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.stream.Stream

@OpenAPIDefinition(info = Info(title = "Supply chain tree API", version = "1.0", summary = "A simple API to manage supply chain tree structure"))
@Tag(name = "Supply chain tree API")
@RestController
@RequestMapping("/api")
class SupplyChainTreeApi(
    private val repository: SupplyChainTreeRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(summary = "Create new supply chain tree edge")
    @ApiResponse(responseCode = "200", description = "Successfully created the tree edge")
    @ApiResponse(responseCode = "409", description = "The tree edge already exists")
    @PostMapping("/edge/from/{fromNodeId}/to/{toNodeId}")
    fun createEdge(
        @PathVariable @Parameter fromNodeId: Int,
        @PathVariable @Parameter toNodeId: Int,
    ) {
        logger.info("Create edge from $fromNodeId to $toNodeId")
        repository.createEdge(fromNodeId, toNodeId)
    }

    @Operation(summary = "Delete an existing supply chain tree edge")
    @ApiResponse(responseCode = "200", description = "Successfully deleted the tree edge")
    @ApiResponse(responseCode = "404", description = "The tree edge does not exist")
    @DeleteMapping("/edge/from/{fromNodeId}/to/{toNodeId}")
    fun deleteEdge(
        @PathVariable fromNodeId: Int,
        @PathVariable toNodeId: Int,
    ) {
        logger.info("Delete edge from $fromNodeId to $toNodeId")
        repository.deleteEdge(fromNodeId, toNodeId)
    }

    @Operation(summary = "Fetch the whole supply chain tree")
    @ApiResponse(responseCode = "200", description = "Successfully fetched the tree hierarchy")
    @ApiResponse(responseCode = "404", description = "The tree with that starting node does not exist")
    @GetMapping("/tree/from/{fromNodeId}")
    fun fetchTree(@PathVariable fromNodeId: Int): Stream<String> {
        logger.info("Get tree from $fromNodeId")
        return repository.fetchReachableEdges(fromNodeId)
            .map { "${it.getValue("from_id")}->${it.getValue("to_id")}" }
    }
}
