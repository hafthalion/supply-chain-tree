package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.repository.SupplyChainTreeRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@Tag(name = "Supply chain tree API for testing")
@RestController
@RequestMapping("/test")
class SupplyChainTreeTestApi(
    private val repository: SupplyChainTreeRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(summary = "Create a large supply chain test tree")
    @ApiResponse(responseCode = "200", description = "Successfully created the test tree")
    @PostMapping("/tree/from/{fromNodeId}")
    fun createLargeTestTree(
        @PathVariable @Parameter fromNodeId: Int,
        @Parameter size: Int,
    ) {
        logger.info("Create large test tree from $fromNodeId of size $size")
        repository.createLargeTree(fromNodeId, size)
    }
}
