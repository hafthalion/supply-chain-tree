package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

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
    @Disabled("Not implemented yet")
    fun `should fetch tree structure`() {
        TODO()
    }

}