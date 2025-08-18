package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.api.dto.FetchTreeNode
import com.prewave.supplychaintree.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import com.prewave.supplychaintree.exception.TreeNotFoundException
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
        doNothing().whenever(repository).createEdge(any(), any())

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
        whenever(repository.deleteEdge(any(), any())).thenReturn(1)

        service.deleteEdge(1, 2)

        verify(repository).deleteEdge(1, 2)
    }

    @Test
    fun `should fail when deleting unknown edge from repository`() {
        whenever(repository.deleteEdge(any(), any())).thenReturn(0)

        assertThatThrownBy {
            service.deleteEdge(1, 2)
        }.isInstanceOf(EdgeNotFoundException::class.java)

        verify(repository).deleteEdge(1, 2)
    }

    @Test
    fun `should fetch tree structure`() {
        whenever(repository.fetchReachableEdges(any())).thenReturn(
            Stream.of(1 to 20, 1 to 3, 1 to 4, 20 to 5, 20 to 6, 5 to 7)
        )

        val tree = service.fetchTree(1).asStream()

        verify(repository).fetchReachableEdges(1)
        assertThat(tree).hasSize(3).containsExactly(
            FetchTreeNode(1, listOf(20, 3, 4)),
            FetchTreeNode(20, listOf(5, 6)),
            FetchTreeNode(5, listOf(7)),
        )
    }

    @Test
    fun `should fail when fetching unknown tree`() {
        whenever(repository.fetchReachableEdges(any())).thenReturn(Stream.of())

        assertThatThrownBy {
            service.fetchTree(1)
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