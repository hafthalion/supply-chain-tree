package com.prewave.supplychaintree.api

import com.prewave.supplychaintree.api.dto.FetchTreeResponse
import com.prewave.supplychaintree.repository.SupplyChainTreeRepository
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.stream.Stream
import kotlin.streams.asStream

@OpenAPIDefinition(
    info = Info(
        title = "Supply chain tree API",
        version = "1.0",
        summary = "A simple API to manage supply chain tree structure"
    )
)
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

    @Operation(summary = "Fetch the whole supply chain tree hierarchy")
    @ApiResponse(responseCode = "200", description = "Successfully fetched the tree hierarchy",
        content = [Content(mediaType = "application/json", array = ArraySchema(schema = Schema(FetchTreeResponse::class)))])
    @ApiResponse(responseCode = "404", description = "The tree with that starting node does not exist", content = [Content()])
    @GetMapping("/tree/from/{fromNodeId}")
    fun fetchTree(@PathVariable fromNodeId: Int): Stream<Any> {
        logger.info("Get tree from $fromNodeId")

        val linearEdges: Iterator<Pair<Int, Int>> = repository.fetchReachableEdges(fromNodeId).iterator()

        val foldedEdges = sequence {
            if (linearEdges.hasNext()) {
                var e = linearEdges.next()
                var fromNodeId = e.first
                var toNodeIds = mutableListOf(e.second)

                while (linearEdges.hasNext()) {
                    e = linearEdges.next()

                    if (e.first == fromNodeId) {
                        toNodeIds.add(e.second)
                    } else {
                        yield(FetchTreeResponse(fromNodeId, toNodeIds))
                        fromNodeId = e.first
                        toNodeIds = mutableListOf(e.second)
                    }
                }

                yield(FetchTreeResponse(fromNodeId, toNodeIds))
            }
        }

        return foldedEdges.asStream()
    }
}
