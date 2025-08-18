package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.api.dto.FetchTreeNode
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import com.prewave.supplychaintree.exception.TreeNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.log10
import kotlin.math.max

@Service
class SupplyChainTreeService(
    private val repository: SupplyChainTreeRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Create edge from $fromNodeId to $toNodeId")

        repository.createEdge(fromNodeId, toNodeId)
    }

    @Throws(EdgeNotFoundException::class)
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
    @Throws(TreeNotFoundException::class)
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

    fun generateLargeTree(fromNodeId: Int, size: Int, arity: Int? = null) {
        logger.info("Generate large tree from $fromNodeId of size $size and arity $arity")

        val arityOrDefault = arity ?: max(log10(size.toDouble()).toInt(), 1)
        val fromToIdSequence = generateLargeTreeSequence(fromNodeId, size, arityOrDefault)

        repository.createEdges(fromToIdSequence)
    }

    private fun generateLargeTreeSequence(fromNodeId: Int, size: Int, arity: Int): Sequence<Pair<Int, Int>> = sequence {
        val nodeIdDeque = ArrayDeque<Int>(size).apply { add(fromNodeId) }
        var toNodeId = fromNodeId + 1

        while (true) {
            val fromNodeId = nodeIdDeque.removeFirst()

            repeat(arity) {
                yield(fromNodeId to toNodeId) // yield node immediately when created
                nodeIdDeque.addLast(toNodeId++) // add current nodeId to the queue to generate its child nodes later
            }
        }
    }.take(size)
}
