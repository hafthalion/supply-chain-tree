package com.prewave.supplychaintree.service

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.util.stream.Stream

@Component
class StreamFetcher(
    private val transactionManager: PlatformTransactionManager,
) {
    fun <T, R> fetchStreamInTransaction(fetch: () -> Stream<T>, use: (Stream<T>) -> Stream<R>): Stream<R> {
        val tx = transactionManager.getTransaction(DefaultTransactionDefinition().apply {
            isReadOnly = true
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        })

        try {
            return fetch().useClosingOnThrow { stream ->
                use(stream).onClose {
                    // delegate closing of the new stream to original source stream
                    stream.close()
                    //TODO we cannot distinguish between commit and rollback here, rollback safer and ok for read-only queries for now
                    transactionManager.rollback(tx)
                }
            }
        }
        catch (e: Throwable) {
            transactionManager.rollback(tx)
            throw e
        }
    }

    private inline fun <T : AutoCloseable, R> T.useClosingOnThrow(block: (T) -> R): R {
        return try {
            block(this)
        }
        catch (e: Throwable) {
            try {
                close()
            }
            catch (suppressed: Throwable) {
                e.addSuppressed(suppressed)
            }

            throw e
        }
    }
}