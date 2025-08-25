package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.TreeNode
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeConflictException
import com.prewave.supplychaintree.domain.exception.EdgeNotFoundException
import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.util.stream.Stream
import kotlin.math.log10
import kotlin.math.max
import kotlin.streams.asStream

@Service
@Validated
class SupplyChainTreeService(
    private val repository: SupplyChainTreeRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    @Throws(EdgeAlreadyExistsException::class)
    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Create edge from $fromNodeId to $toNodeId")

        repository.createEdge(fromNodeId, toNodeId)
    }

    @Transactional
    @Throws(EdgeNotFoundException::class)
    fun deleteEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Delete edge from $fromNodeId to $toNodeId")

        repository.deleteEdge(fromNodeId, toNodeId)
    }

    /**
     * Fetches all reachable tree edges from the repository and grouping all child edges to single element per each parent node.
     *
     * For this to work the repository has to return all child edges of any given node in sequence as rows coming directly after each other.
     * The tree node sequence has to be processed entirely in the consumer function so that underlying stream resources and transaction can be closed properly.
     *
     * @param fromNodeId Starting node id
     * @param consumer The consumer function that should process the tree nodes
     */
    @Transactional
    @Throws(TreeNotFoundException::class)
    fun fetchTree(fromNodeId: Int, consumer: (Stream<TreeNode>) -> Unit) {
        logger.info("Fetch and stream tree from $fromNodeId")

        repository.fetchReachableEdges(fromNodeId).use {
            val edges = it.iterator()

            if (!edges.hasNext()) {
                throw TreeNotFoundException(fromNodeId)
            }

            consumer(edges.foldAllChildEdgesIntoParentNodes().asStream())
        }
    }


    @Transactional
    @Throws(EdgeConflictException::class)
    fun generateLargeTree(fromNodeId: Int, @Min(1) size: Int, @Min(1) arity: Int? = null) {
        logger.info("Generate large tree from $fromNodeId of size $size and arity $arity")

        val arityOrDefault = arity ?: max(log10(size.toDouble()).toInt(), 1)
        val edges = generateLargeTreeSequence(fromNodeId, size, arityOrDefault)

        repository.createEdges(edges)
    }

    private fun generateLargeTreeSequence(fromNodeId: Int, size: Int, arity: Int): Sequence<TreeEdge> = sequence {
        val nodeIdDeque = ArrayDeque<Int>(size).apply { add(fromNodeId) }
        var toNodeId = fromNodeId + 1

        while (true) {
            val fromNodeId = nodeIdDeque.removeFirst()

            repeat(arity) {
                // yield node immediately when created
                yield(TreeEdge(fromNodeId, toNodeId))
                // add current nodeId to the queue to generate its child nodes later
                nodeIdDeque.addLast(toNodeId++)
            }
        }
    }.take(size)

    private fun Iterator<TreeEdge>.foldAllChildEdgesIntoParentNodes(): Sequence<TreeNode> = sequence {
        var e = next()
        var fromNodeId = e.fromNodeId
        var toNodeIds = mutableListOf(e.toNodeId)

        while (hasNext()) {
            e = next()

            if (e.fromNodeId == fromNodeId) {
                toNodeIds.add(e.toNodeId)
            }
            else {
                // yield node when parent changed -> all child ids present
                yield(TreeNode(fromNodeId, toNodeIds))
                fromNodeId = e.fromNodeId
                toNodeIds = mutableListOf(e.toNodeId)
            }
        }

        // yield last parent node
        yield(TreeNode(fromNodeId, toNodeIds))
    }
}
