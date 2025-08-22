package com.prewave.supplychaintree.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.mockito.stubbing.OngoingStubbing
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.SimpleTransactionStatus
import java.io.IOException
import java.sql.SQLException
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class StreamFetcherTest {
    private val transactionManager = mock<PlatformTransactionManager>()

    private val streamFetcher = StreamFetcher(transactionManager)

    @Test
    fun `should fetch stream in transaction successfully`() {
        val tx = SimpleTransactionStatus()
        whenever(transactionManager.getTransaction(any())).thenReturn(tx)

        val stream = streamFetcher.fetchStreamInTransaction({ listOf(1, 2, 3).stream() }) {
            it.map(Int::toString)
        }

        assertThat(stream).containsExactly("1", "2", "3")
        verify(transactionManager).getTransaction(any())
        verify(transactionManager).rollback(tx)
    }

    @Test
    fun `should delegate close of returned new stream to the original fetched stream`() {
        val fetchStream = spy<Stream<Any>>(Stream.empty())
        whenever(transactionManager.getTransaction(any())).thenReturn(SimpleTransactionStatus())

        val stream = streamFetcher.fetchStreamInTransaction({ fetchStream }) { Stream.empty<Any>() }
        stream.close()

        verify(fetchStream).close()
    }

    @Test
    fun `should not close transaction before stream closed`() {
        whenever(transactionManager.getTransaction(any())).thenReturn(SimpleTransactionStatus())

        streamFetcher.fetchStreamInTransaction({ Stream.empty<Any>() }) { it }

        verify(transactionManager, never()).rollback(any())
        verify(transactionManager, never()).commit(any())
    }

    @Test
    fun `should close transaction when fetch fails`() {
        val tx = SimpleTransactionStatus()
        whenever(transactionManager.getTransaction(any())).thenReturn(tx)

        assertThatThrownBy {
            streamFetcher.fetchStreamInTransaction<Any, Any>({ throw SQLException("fetch error") }) { it }
        }.isInstanceOf(SQLException::class.java).hasMessage("fetch error")

        verify(transactionManager).rollback(tx)
        verify(transactionManager, never()).commit(tx)
    }

    @Test
    fun `should close transaction and fetched stream when usage fails`() {
        val fetchStream = mock<Stream<Any>>()
        val tx = SimpleTransactionStatus()
        whenever(transactionManager.getTransaction(any())).thenReturn(tx)

        assertThatThrownBy {
            streamFetcher.fetchStreamInTransaction<Any, Any>({ fetchStream }) { throw IOException("stream error") }
        }.isInstanceOf(IOException::class.java).hasMessage("stream error")

        verify(fetchStream).close()
        verify(transactionManager).rollback(tx)
        verify(transactionManager, never()).commit(tx)
    }
}

fun <R> wheneverFetchingStreamOf(streamFetcherMock: StreamFetcher) = whenever(streamFetcherMock.fetchStreamInTransaction<Any, R>(any(), any()))

fun <R> OngoingStubbing<R>.thenSimulateStreamFetcher() = doAnswer {
    class T
    val (fetch: () -> T, use: (T) -> R) = it
    use(fetch())
}
