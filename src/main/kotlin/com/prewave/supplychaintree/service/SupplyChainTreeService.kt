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

    /**
     * Fetches all reachable tree edges from the repository and grouping them to single element per each node.
     * For this to work the repository has to return all child edges of any given node in sequence as rows coming directly after each other.
     */
    fun fetchTree(fromNodeId: Int): Sequence<FetchTreeNode> {
        logger.info("Get tree from $fromNodeId")

        val linearEdges: Iterator<Pair<Int, Int>> = repository.fetchReachableEdges(fromNodeId).iterator()

        if (!linearEdges.hasNext()) {
            throw TreeNotFoundException(fromNodeId)
        }

        val foldedEdges = sequence {
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

        return foldedEdges
    }
}
