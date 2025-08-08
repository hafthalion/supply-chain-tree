package com.prewave.supplychaintree.api

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class SupplyChainTreeApi {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/edge/from/{fromNodeId}/to/{toNodeId}")
    fun createEdge(@PathVariable fromNodeId: Int, @PathVariable toNodeId: Int) {
        logger.info("Create edge from $fromNodeId to $toNodeId")
        TODO()
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
