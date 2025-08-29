package com.prewave.supplychaintree.domain

import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import java.util.stream.Stream
import kotlin.streams.asStream

class Tree(
    val rootNodeId: Int,
    val edges: Stream<TreeEdge>,
) {
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

            node = node.foldChildOrCreateNew(edge).let {
                // yield previous parent node when the parent changed
                if (it.parentChanged) yield(node)
                it.node
            }
        }

        // yield last parent node
        yield(node)
    }
}
