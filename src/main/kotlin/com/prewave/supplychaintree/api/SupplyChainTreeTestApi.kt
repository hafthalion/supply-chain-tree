package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.service.SupplyChainTreeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SecurityScheme(name = "test-auth", type = SecuritySchemeType.HTTP, scheme = "basic", description = "Basic authentication for test endpoints. Use test/secret during local development.")
@SecurityRequirement(name = "test-auth")
@Tag(name = "Supply chain tree API for testing", description = "Private test API for performance testing of large trees.")
@RestController
@RequestMapping("/test")
@Profile("default")
class SupplyChainTreeTestApi(
    private val service: SupplyChainTreeService,
) {
    @Operation(summary = "Generate a large supply chain test tree")
    @ApiResponse(responseCode = "200", description = "Successfully generated a test tree", content = [Content()])
    @PostMapping("/tree/from/{fromNodeId}")
    fun generateLargeTestTree(
        @PathVariable @Parameter fromNodeId: Int,
        @Parameter size: Int,
        @Parameter arity: Int?,
    ) {
        service.generateLargeTree(fromNodeId, size, arity)
    }
}
