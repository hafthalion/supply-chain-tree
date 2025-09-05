package com.hafnium.supplychaintree.domain

import kotlin.math.log10
import kotlin.math.max
import kotlin.streams.asStream

class TreeFactory {
    /**
     * Generate a tree starting with the [rootNodeId] with total tree size of [size] and optionally a given node-child [arity] for testing purposes.
     */
    fun generateTree(rootNodeId: Int, size: Int, arity: Int? = null): Tree {
        require(size > 0) { "Size must be greater than zero." }
        require(arity == null || arity > 0) { "Arity must be greater than zero." }

        val arityOrDefault = arity ?: max(log10(size.toDouble()).toInt(), 1)
        val edges = generateTreeSequence(rootNodeId, size, arityOrDefault)

        return Tree(rootNodeId, edges.asStream())
    }

    private fun generateTreeSequence(fromNodeId: Int, size: Int, arity: Int): Sequence<TreeEdge> = sequence {
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
}