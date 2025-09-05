package com.hafnium.supplychaintree.api

import com.hafnium.supplychaintree.api.dto.ErrorResponse
import com.hafnium.supplychaintree.service.SupplyChainTreeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Min
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SecurityScheme(name = "test-auth", type = SecuritySchemeType.HTTP, scheme = "basic",
    description = "Basic authentication for test endpoints. Use test/secret during local development.")
@SecurityRequirement(name = "test-auth")
@Tag(name = "Supply chain tree API for testing", description = "Private test API for performance testing of large trees during development profile.")
@RestController
@RequestMapping("/test", produces = [APPLICATION_JSON_VALUE])
@Validated
@Profile("default")
class SupplyChainTreeTestApi(
    private val service: SupplyChainTreeService,
) {
    @Operation(summary = "Generate a supply chain test tree of given size")
    @ApiResponse(responseCode = "200", description = "Successfully generated a test tree", content = [Content()])
    @ApiResponse(responseCode = "409", description = "A conflict with an existing tree edge occurred",
        content = [Content(mediaType = APPLICATION_JSON_VALUE, schema = Schema(ErrorResponse::class))])
    @PostMapping("/tree/from/{fromNodeId}")
    fun generateTestTree(
        @PathVariable @Parameter fromNodeId: Int,
        @Parameter @Min(1) size: Int,
        @Parameter @Min(1) arity: Int?,
    ) {
        service.generateTree(fromNodeId, size, arity)
    }
}
