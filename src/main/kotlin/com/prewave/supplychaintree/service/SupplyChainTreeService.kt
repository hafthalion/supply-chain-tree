package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.api.dto.FetchTreeNode
import com.prewave.supplychaintree.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.exception.EdgeConflictException
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import com.prewave.supplychaintree.exception.TreeNotFoundException
import jakarta.validation.constraints.Min
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.validation.annotation.Validated
import java.util.stream.Stream
import kotlin.math.log10
import kotlin.math.max
import kotlin.streams.asStream

@Service
@Validated
class SupplyChainTreeService(
    private val repository: SupplyChainTreeRepository,
    private val transactionManager: PlatformTransactionManager,
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

        val deletedRows = repository.deleteEdge(fromNodeId, toNodeId)

        if (deletedRows == 0) {
            throw EdgeNotFoundException(fromNodeId, toNodeId)
        }
    }

    /**
     * Fetches all reachable tree edges from the repository and grouping all child edges to single element per each parent node.
     *
     * For this to work the repository has to return all child edges of any given node in sequence as rows coming directly after each other.
     * This method implements automatic stream closure after all the elements have been consumed to free the underlying resources without relying on the client.
     * Also requires and implements manual transaction management for the underlying stream to remain open until fully consumed.
     *
     * @return A sequence of nodes with child edges
     */
    @Throws(TreeNotFoundException::class)
    fun fetchTree(fromNodeId: Int): Stream<FetchTreeNode> {
        logger.info("Get tree from $fromNodeId")

        return streamWithTransaction({ repository.fetchReachableEdges(fromNodeId) }) { stream ->
            val edges = stream.iterator()

            if (!edges.hasNext()) {
                throw TreeNotFoundException(fromNodeId)
            }

            edges.foldAllChildEdgesIntoParentNodes(stream)
        }
    }

    private fun <T, R> streamWithTransaction(supplier: () -> Stream<T>, use: (Stream<T>) -> Stream<R>): Stream<R> {
        val tx = transactionManager.getTransaction(DefaultTransactionDefinition().apply { isReadOnly = true })

        try {
            return supplier().useClosingOnThrow { stream ->
                use(stream).onClose {
                    transactionManager.commit(tx)
                }
            }
        }
        catch (e: Throwable) {
            transactionManager.rollback(tx)
            throw e
        }
    }

    private inline fun <T : AutoCloseable, R> T.useClosingOnThrow(block: (T) -> R): R {
        return try {
            block(this)
        }
        catch (e: Throwable) {
            try {
                close()
            }
            catch (suppressed: Throwable) {
                e.addSuppressed(suppressed)
            }

            throw e
        }
    }

    @Transactional
    @Throws(EdgeConflictException::class)
    fun generateLargeTree(fromNodeId: Int, @Min(1) size: Int, @Min(1) arity: Int? = null) {
        logger.info("Generate large tree from $fromNodeId of size $size and arity $arity")

        val arityOrDefault = arity ?: max(log10(size.toDouble()).toInt(), 1)
        val fromToIdSequence = generateLargeTreeSequence(fromNodeId, size, arityOrDefault)

        repository.createEdges(fromToIdSequence)
    }

    private fun generateLargeTreeSequence(fromNodeId: Int, size: Int, arity: Int): Sequence<Pair<Int, Int>> =
        sequence {
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

    private fun Iterator<Pair<Int, Int>>.foldAllChildEdgesIntoParentNodes(source: AutoCloseable): Stream<FetchTreeNode> =
        sequence {
            source.use {
                var e = next()
                var fromNodeId = e.first
                var toNodeIds = mutableListOf(e.second)

                while (hasNext()) {
                    e = next()

                    if (e.first == fromNodeId) {
                        toNodeIds.add(e.second)
                    }
                    else {
                        // yield node when parent changed -> all child ids present
                        yield(FetchTreeNode(fromNodeId, toNodeIds))
                        fromNodeId = e.first
                        toNodeIds = mutableListOf(e.second)
                    }
                }

                // yield last parent node
                yield(FetchTreeNode(fromNodeId, toNodeIds))
            }
        }.asStreamClosingAlso(source) // delegate closing of the new stream to original source stream

    private fun <T> Sequence<T>.asStreamClosingAlso(source: AutoCloseable): Stream<T> =
        asStream().onClose { source.close() }
}
