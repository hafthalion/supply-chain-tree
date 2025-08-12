package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.repository.SupplyChainTreeRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Supply chain tree API for testing", description = "Private test API for performance testing of large trees")
@RestController
@RequestMapping("/test")
class SupplyChainTreeTestApi(
    private val repository: SupplyChainTreeRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(summary = "Generate a large supply chain test tree")
    @ApiResponse(responseCode = "200", description = "Successfully generated a test tree")
    @PostMapping("/tree/from/{fromNodeId}")
    fun generateLargeTestTree(
        @PathVariable @Parameter fromNodeId: Int,
        @Parameter size: Int,
        @Parameter arity: Int?,
    ) {
        logger.info("Generate large test tree from $fromNodeId of size $size and arity $arity")
        repository.createLargeTree(fromNodeId, size, arity)
    }
}
