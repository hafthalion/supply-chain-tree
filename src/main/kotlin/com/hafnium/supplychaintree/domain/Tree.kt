package com.hafnium.supplychaintree.domain

import com.hafnium.supplychaintree.domain.exception.TreeNotFoundException
import java.util.stream.Stream
import kotlin.streams.asStream

/**
 * A tree domain class responsible for processing its edges.
 */
class Tree(
    val rootNodeId: Int,
    val edges: Stream<TreeEdge>,
) {
    /**
     * Fold edges of same origin into tree nodes containing all direct child edges in a single element for easy streaming processing.
     * For this to work the edge stream has to return all child edges of any given node in sequence as rows coming directly after each other.
     */
    fun processNodes(consumer: (Stream<TreeNode>) -> Unit) {
        edges.use {
            consumer(edges.foldAllChildEdgesIntoParentNodes().asStream())
        }
    }

    private fun Stream<TreeEdge>.foldAllChildEdgesIntoParentNodes(): Sequence<TreeNode> = sequence {
        val edges = iterator()

        if (!edges.hasNext()) {
            throw TreeNotFoundException(rootNodeId)
        }

        var edge = edges.next()
        var node = TreeNode(edge)

        while (edges.hasNext()) {
            edge = edges.next()

            node = node.foldChild(edge).let {
                // yield previous parent node when the parent changed
                if (it.nodeChanged) yield(node)
                it.node
            }
        }

        // yield last parent node
        yield(node)
    }
}
