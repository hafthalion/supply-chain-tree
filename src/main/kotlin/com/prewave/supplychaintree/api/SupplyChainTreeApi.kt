package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.repository.SupplyChainTreeRepository
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@OpenAPIDefinition(info = Info(title = "SupplyChainTree", version = "1.0"))
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

    @DeleteMapping("/edge/from/{fromNodeId}/to/{toNodeId}")
    fun deleteEdge(@PathVariable fromNodeId: Int, @PathVariable toNodeId: Int) {
        logger.info("Delete edge from $fromNodeId to $toNodeId")
        TODO()
    }

    @GetMapping("/tree/from/{fromNodeId}")
    fun getTree(@PathVariable fromNodeId: Int): Any {
        logger.info("Get tree from $fromNodeId")
        TODO()
    }
}
