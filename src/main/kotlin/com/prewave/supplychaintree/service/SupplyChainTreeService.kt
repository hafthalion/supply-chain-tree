package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.api.dto.FetchTreeNode
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import com.prewave.supplychaintree.exception.TreeNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SupplyChainTreeService(
    private val repository: SupplyChainTreeRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Create edge from $fromNodeId to $toNodeId")

        repository.createEdge(fromNodeId, toNodeId)
    }

    fun deleteEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Delete edge from $fromNodeId to $toNodeId")

        val deletedRows = repository.deleteEdge(fromNodeId, toNodeId)

        if (deletedRows == 0) {
            throw EdgeNotFoundException(fromNodeId, toNodeId)
        }
    }

    fun fetchTree(fromNodeId: Int): Sequence<FetchTreeNode> {
        if (!repository.hasDirectEdges(fromNodeId)) {
            throw TreeNotFoundException(fromNodeId)
        }

        logger.info("Get tree from $fromNodeId")

        var fromNodeId1 = fromNodeId
        val linearEdges: Iterator<Pair<Int, Int>> = repository.fetchReachableEdges(fromNodeId1).iterator()

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
                        yield(FetchTreeNode(fromNodeId, toNodeIds))
                        fromNodeId = e.first
                        toNodeIds = mutableListOf(e.second)
                    }
                }

                yield(FetchTreeNode(fromNodeId, toNodeIds))
            }
        }

        return foldedEdges
    }
}
