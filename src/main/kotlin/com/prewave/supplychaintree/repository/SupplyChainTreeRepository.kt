package com.prewave.supplychaintree.repository

import com.prewave.supplychaintree.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.table
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.math.log10


//TODO Use jooq generator for type-safe access
@Repository
class SupplyChainTreeRepository(
    private val dsl: DSLContext,
) {
    private val batchSize = 10_000

    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        try {
            dsl.insertInto(table("edge"))
                .set(field("from_id"), fromNodeId)
                .set(field("to_id"), toNodeId)
                .execute()
        } catch (e: DuplicateKeyException) {
            throw EdgeAlreadyExistsException(fromNodeId, toNodeId, e)
        }
    }

    fun deleteEdge(fromNodeId: Int, toNodeId: Int) {
        val deletedRows = dsl.deleteFrom(table("edge"))
            .where(
                field("from_id").eq(fromNodeId)
                    .and(field("to_id").eq(toNodeId))
            ).execute()

        if (deletedRows == 0) {
            throw EdgeNotFoundException(fromNodeId, toNodeId)
        }
    }

    fun fetchEdges(): Stream<Record> =
        dsl.select().from(table("edge")).fetchSize(batchSize).fetchStream()

    /**
    WITH RECURSIVE rq(from_id, to_id) AS (
        SELECT from_id, to_id
        FROM edge e
        WHERE from_id = 100000
        UNION ALL
        SELECT e.from_id, e.to_id
        FROM rq INNER JOIN edge e ON rq.to_id = e.from_id
    )
    SELECT from_id, to_id
    FROM rq;
     */
    fun fetchEdges(fromNodeId: Int): Stream<Record> {
        return dsl.withRecursive(name("cte"), name("from_id"), name("to_id"))
            .`as`(
                select(field("from_id"), field("to_id"))
                    .from(table("edge"))
                    .where(field("from_id").eq(fromNodeId))
                    .unionAll(
                        select(field(name("e", "from_id")), field(name("e", "to_id")))
                            .from(name("rq"))
                            .join(table("edge").`as`("e"))
                            .on(field(name("rq", "to_id")).eq(field(name("e", "from_id"))))
                    )
            )
            .select()
            .from(name("rq"))
            .fetchSize(batchSize)
            .fetchStream()
    }

    fun createLargeTree(fromNodeId: Int, size: Int, arity: Int = log10(size.toDouble()).toInt()) {
        val insert = dsl.insertInto(table("edge"))
            .columns(field("from_id"), field("to_id"))
            .values(0, 0) // param placeholders

        generateTreeSequence(fromNodeId, size, arity)
            .chunked(batchSize)
            .forEach { chunk ->
                val batch = dsl.batch(insert)
                chunk.forEach { batch.bind(it.first, it.second) }
                batch.execute()
            }
    }

    private fun generateTreeSequence(fromNodeId: Int, size: Int, arity: Int): Sequence<Pair<Int, Int>> = sequence {
        val nodeIdQueue = ArrayDeque<Int>(size).apply { add(fromNodeId) }
        var toNodeId = fromNodeId + 1

        while (true) {
            val fromNodeId = nodeIdQueue.removeFirst()

            repeat(arity) {
                yield(fromNodeId to toNodeId) // yield node immediately when created
                nodeIdQueue.addLast(toNodeId++) // add current nodeId to the queue to generate its child nodes later
            }
        }
    }.take(size)
}

