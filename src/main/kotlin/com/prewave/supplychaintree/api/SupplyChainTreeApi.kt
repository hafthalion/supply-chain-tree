package com.prewave.supplychaintree.api

import com.fasterxml.jackson.databind.ObjectWriter
import com.prewave.supplychaintree.api.dto.ErrorResponse
import com.prewave.supplychaintree.api.dto.FetchTreeNode
import com.prewave.supplychaintree.service.SupplyChainTreeService
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

@OpenAPIDefinition(info = Info(title = "Supply chain tree API", version = "1.0", summary = "A simple API to manage supply chain tree structure"))
@Tag(name = "Supply chain tree API", description = "Public API to manage supply chain tree structure")
@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_VALUE])
class SupplyChainTreeApi(
    private val service: SupplyChainTreeService,
    private val objectWriter: ObjectWriter,
) {
    @Operation(summary = "Create new supply chain tree edge")
    @ApiResponse(responseCode = "200", description = "Successfully created the tree edge", content = [Content()])
    @ApiResponse(responseCode = "409", description = "The tree edge already exists",
        content = [Content(mediaType = "application/json", schema = Schema(ErrorResponse::class))])
    @PostMapping("/edge/from/{fromNodeId}/to/{toNodeId}")
    fun createEdge(
        @PathVariable @Parameter fromNodeId: Int,
        @PathVariable @Parameter toNodeId: Int,
    ) {
        return service.createEdge(fromNodeId, toNodeId)
    }

    @Operation(summary = "Delete an existing supply chain tree edge")
    @ApiResponse(responseCode = "200", description = "Successfully deleted the tree edge", content = [Content()])
    @ApiResponse(responseCode = "404", description = "The tree edge does not exist",
        content = [Content(mediaType = "application/json", schema = Schema(ErrorResponse::class))])
    @DeleteMapping("/edge/from/{fromNodeId}/to/{toNodeId}")
    fun deleteEdge(
        @PathVariable fromNodeId: Int,
        @PathVariable toNodeId: Int,
    ) {
        return service.deleteEdge(fromNodeId, toNodeId)
    }

    @Operation(summary = "Fetch the whole supply chain tree structure", description = """
Fetch the whole supply chain tree structure starting with the fromNodeId.
The node elements are streamed in chunks and in the tree hierarchy order, meaning that node ID references are forward only,
allowing effective processing on the client side, i.e. processed elements can be forgotten.
        """)
    @ApiResponse(responseCode = "200", description = "Successfully fetched the tree structure",
        content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(FetchTreeNode::class)))])
    @ApiResponse(responseCode = "404", description = "The tree with that starting node does not exist",
        content = [Content(mediaType = "application/json", schema = Schema(ErrorResponse::class))])
    @GetMapping("/tree/from/{fromNodeId}")
    fun fetchTree(@PathVariable fromNodeId: Int): ResponseEntity<StreamingResponseBody> {
        val response = ResponseEntity.ok().contentType(APPLICATION_JSON)

        return response.body(StreamingResponseBody { output ->
            service.fetchAndProcessTree(fromNodeId) { nodes ->
                objectWriter.writeValue(output, nodes.map(::FetchTreeNode))
            }
        })
    }
}
