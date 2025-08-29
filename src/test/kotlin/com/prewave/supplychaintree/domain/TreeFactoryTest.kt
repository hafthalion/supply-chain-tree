package com.prewave.supplychaintree.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TreeFactoryTest {
    private val treeFactory = TreeFactory()

    @Test
    fun `should generate tree`() {
        val tree = treeFactory.generateTree(rootNodeId = 1, size = 6, arity = 2)

        assertThat(tree.rootNodeId).isEqualTo(1)
        assertThat(tree.edges).hasSize(6)
            .containsExactly(
                TreeEdge(1, 2),
                TreeEdge(1, 3),
                TreeEdge(2, 4),
                TreeEdge(2, 5),
                TreeEdge(3, 6),
                TreeEdge(3, 7),
            )
    }
}