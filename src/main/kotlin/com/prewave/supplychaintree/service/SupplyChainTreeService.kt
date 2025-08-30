package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.TreeFactory
import com.prewave.supplychaintree.domain.TreeNode
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeConflictException
import com.prewave.supplychaintree.domain.exception.EdgeNotFoundException
import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

/**
 * The business logic entry point service.
 */
@Service
class SupplyChainTreeService(
    private val repository: SupplyChainTreeRepository,
    private val treeFactory: TreeFactory,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Transactional
    @Throws(EdgeAlreadyExistsException::class)
    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Create edge from $fromNodeId to $toNodeId")
        repository.createEdge(TreeEdge(fromNodeId, toNodeId))
    }

    @Transactional
    @Throws(EdgeNotFoundException::class)
    fun deleteEdge(fromNodeId: Int, toNodeId: Int) {
        logger.info("Delete edge from $fromNodeId to $toNodeId")
        repository.deleteEdge(TreeEdge(fromNodeId, toNodeId))
    }

    /**
     * Fetches all reachable tree edges from the repository and grouping all child edges to single element per each parent node.
     * For this to work the repository has to return all child edges of any given node in sequence as rows coming directly after each other.
     *
     * The tree node sequence has to be processed entirely in the consumer function so that underlying stream resources and transaction can be closed properly.
     *
     * @param fromNodeId Starting node id
     * @param consumer The consumer function that should process the tree nodes
     */
    @Transactional
    @Throws(TreeNotFoundException::class)
    fun fetchAndProcessTree(fromNodeId: Int, consumer: (Stream<TreeNode>) -> Unit) {
        logger.info("Fetch and process tree from $fromNodeId")
        repository.fetchTree(fromNodeId).processNodes(consumer)
    }


    /**
     * Generate a tree in the repository starting with the [fromNodeId] with total tree size of [size]
     * and optionally a given node-child [arity] for testing purposes.
     */
    @Transactional
    @Throws(EdgeConflictException::class)
    fun generateTree(fromNodeId: Int, size: Int, arity: Int? = null) {
        logger.info("Generate tree from $fromNodeId of size $size and arity $arity")
        repository.createTree(treeFactory.generateTree(fromNodeId, size, arity))
    }
}
