package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.TreeNode
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeNotFoundException
import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.util.stream.Stream
import kotlin.streams.asStream

@ExtendWith(MockitoExtension::class)
class SupplyChainTreeServiceTest {
    private val repository = mock<SupplyChainTreeRepository>()

    private val service = SupplyChainTreeService(repository)

    @Test
    fun `should create edge in repository`() {
        whenever(repository.createEdge(any(), any())).then {}

        service.createEdge(1, 2)

        verify(repository).createEdge(1, 2)
    }

    @Test
    fun `should fail when creating duplicate edge in repository`() {
        whenever(repository.createEdge(any(), any())).doThrow(EdgeAlreadyExistsException::class)

        assertThatThrownBy {
            service.createEdge(1, 2)
        }.isInstanceOf(EdgeAlreadyExistsException::class.java)

        verify(repository).createEdge(1, 2)
    }

    @Test
    fun `should delete edge from repository`() {
        whenever(repository.deleteEdge(any(), any())).then {}

        service.deleteEdge(1, 2)

        verify(repository).deleteEdge(1, 2)
    }

    @Test
    fun `should fail when deleting unknown edge from repository`() {
        whenever(repository.deleteEdge(any(), any())).doThrow(EdgeNotFoundException::class)

        assertThatThrownBy {
            service.deleteEdge(1, 2)
        }.isInstanceOf(EdgeNotFoundException::class.java)

        verify(repository).deleteEdge(1, 2)
    }

    @Test
    fun `should fetch tree structure`() {
        whenever(repository.fetchReachableEdges(any())).thenReturn(
            Stream.of(TreeEdge(1, 20), TreeEdge(1, 3), TreeEdge(1, 4), TreeEdge(20, 5), TreeEdge(20, 6), TreeEdge(5, 7)))

        service.fetchTree(1) { tree ->
            verify(repository).fetchReachableEdges(1)
            assertThat(tree).hasSize(3)
                .containsExactly(
                    TreeNode(1, listOf(20, 3, 4)),
                    TreeNode(20, listOf(5, 6)),
                    TreeNode(5, listOf(7)),
                )
        }
    }

    @Test
    fun `should fail when fetching unknown tree`() {
        whenever(repository.fetchReachableEdges(any())).thenReturn(Stream.of())

        assertThatThrownBy {
            service.fetchTree(1) {}
        }.isInstanceOf(TreeNotFoundException::class.java)

        verify(repository).fetchReachableEdges(1)
    }

    @Test
    fun `should generate large tree`() {
        doNothing().whenever(repository).createEdges(any())

        service.generateLargeTree(1, 6, 2)

        argumentCaptor {
            verify(repository).createEdges(capture())
            assertThat(firstValue.asStream()).hasSize(6)
        }
    }
}