package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.Tree
import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.TreeFactory
import com.prewave.supplychaintree.domain.TreeNode
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeNotFoundException
import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class SupplyChainTreeServiceTest {
    private val repository = mock<SupplyChainTreeRepository>()
    private val treeFactory = mock<TreeFactory>()

    private val service = SupplyChainTreeService(repository, treeFactory)

    @Test
    fun `should create edge in repository`() {
        whenever(repository.createEdge(any())).then {}

        service.createEdge(1, 2)

        verify(repository).createEdge(TreeEdge(1, 2))
    }

    @Test
    fun `should fail when creating duplicate edge in repository`() {
        whenever(repository.createEdge(any())).doThrow(EdgeAlreadyExistsException::class)

        assertThatThrownBy {
            service.createEdge(1, 2)
        }.isInstanceOf(EdgeAlreadyExistsException::class.java)

        verify(repository).createEdge(TreeEdge(1, 2))
    }

    @Test
    fun `should delete edge from repository`() {
        whenever(repository.deleteEdge(any())).then {}

        service.deleteEdge(1, 2)

        verify(repository).deleteEdge(TreeEdge(1, 2))
    }

    @Test
    fun `should fail when deleting unknown edge from repository`() {
        whenever(repository.deleteEdge(any())).doThrow(EdgeNotFoundException::class)

        assertThatThrownBy {
            service.deleteEdge(1, 2)
        }.isInstanceOf(EdgeNotFoundException::class.java)

        verify(repository).deleteEdge(TreeEdge(1, 2))
    }

    @Test
    fun `should fetch and process tree structure`() {
        whenever(repository.fetchTree(any())).thenReturn(
            Tree(1, Stream.of(TreeEdge(1, 20), TreeEdge(1, 3), TreeEdge(1, 4), TreeEdge(20, 5), TreeEdge(20, 6), TreeEdge(5, 7))))

        service.fetchAndProcessTree(1) { nodes ->
            verify(repository).fetchTree(1)
            assertThat(nodes).hasSize(3)
                .containsExactly(
                    TreeNode(1).withChildren(20, 3, 4),
                    TreeNode(20).withChildren(5, 6),
                    TreeNode(5).withChildren(7),
                )
        }
    }

    @Test
    fun `should fail when fetching unknown tree`() {
        whenever(repository.fetchTree(any())).thenThrow(TreeNotFoundException(1))

        assertThatThrownBy {
            service.fetchAndProcessTree(1) {}
        }.isInstanceOf(TreeNotFoundException::class.java)

        verify(repository).fetchTree(1)
    }

    @Test
    fun `should generate tree`() {
        val tree = Tree(1, Stream.of(TreeEdge(1, 2)))
        whenever(repository.createTree(any())).then {}
        whenever(treeFactory.generateTree(any(), any(), any())).thenReturn(tree)

        service.generateTree(1, 2, 3)

        verify(treeFactory).generateTree(1, 2, 3)
        verify(repository).createTree(tree)
    }
}

private fun TreeNode.withChildren(vararg children: Int): TreeNode {
    children.forEach {
        addChild(TreeEdge(id, it))
    }

    return this
}
