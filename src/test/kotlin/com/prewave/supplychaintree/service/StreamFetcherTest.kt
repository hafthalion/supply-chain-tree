package com.prewave.supplychaintree.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.mockito.stubbing.OngoingStubbing
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.SimpleTransactionStatus

@ExtendWith(MockitoExtension::class)
class StreamFetcherTest {
    private val transactionManager = mock<PlatformTransactionManager>()

    private val streamFetcher = StreamFetcher(transactionManager)

    @Test
    fun `should fetch stream in transaction successfully`() {
        val tx = SimpleTransactionStatus()
        whenever(transactionManager.getTransaction(any())).thenReturn(tx)
        whenever(transactionManager.rollback(any())).then {}

        val stream = streamFetcher.fetchStreamInTransaction({ listOf(1, 2, 3).stream() }) {
            it.map(Int::toString)
        }

        assertThat(stream).containsExactly("1", "2", "3")
        verify(transactionManager).getTransaction(any())
        verify(transactionManager).rollback(tx)
    }

    @Test
    fun `should not close transaction before stream closed`() {
        whenever(transactionManager.getTransaction(any())).thenReturn(SimpleTransactionStatus())
        whenever(transactionManager.rollback(any())).then {}

        streamFetcher.fetchStreamInTransaction({ listOf(1).stream() }) { it }

        verify(transactionManager, never()).rollback(SimpleTransactionStatus())
    }
}

fun <R> wheneverFetchingStreamOf(streamFetcherMock: StreamFetcher) = whenever(streamFetcherMock.fetchStreamInTransaction<Any, R>(any(), any()))

fun <R> OngoingStubbing<R>.thenSimulateStreamFetcher() = doAnswer {
    class T
    val (fetch: () -> T, use: (T) -> R) = it
    use(fetch())
}
