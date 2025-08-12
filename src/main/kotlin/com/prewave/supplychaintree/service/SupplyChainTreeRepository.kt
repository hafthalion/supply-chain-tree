package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.exception.EdgeAlreadyExistsException
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.math.log10

//TODO Use jooq generator for type-safe access
//TODO Refactor logic into service component
@Repository
class SupplyChainTreeRepository(
    private val dsl: DSLContext,
) {
    private val batchSize = 10_000

    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        try {
            dsl.insertInto(DSL.table("edge"))
                .set(DSL.field("from_id"), fromNodeId)
                .set(DSL.field("to_id"), toNodeId)
                .execute()
        } catch (e: DuplicateKeyException) {
            throw EdgeAlreadyExistsException(fromNodeId, toNodeId, e)
        }
    }

    fun deleteEdge(fromNodeId: Int, toNodeId: Int): Int {
        val deletedRows = dsl.deleteFrom(DSL.table("edge"))
            .where(
                DSL.field("from_id").eq(fromNodeId)
                    .and(DSL.field("to_id").eq(toNodeId))
            ).execute()

        return deletedRows
    }

    /**
     * Fetch all the reachable edges from a given node.
     * For this a recursive SQL query is used at the moment as an easy and also surprisingly effective solution.
     *
     * The method has to return all child edges of any given node in sequence as rows coming directly after each other
     * for the streaming algorithm to be able to work.
     *
     * Used SQL query in the form of:
     *
     * WITH RECURSIVE rq(from_id, to_id) AS (
     *     SELECT from_id, to_id
     *     FROM edge e
     *     WHERE from_id = 100000
     *     UNION ALL
     *     SELECT e.from_id, e.to_id
     *     FROM rq INNER JOIN edge e ON rq.to_id = e.from_id
     * )
     * SELECT from_id, to_id
     * FROM rq;
     */
    //TODO Turn off autocommit to keep db cursor open
    fun fetchReachableEdges(fromNodeId: Int): Stream<Pair<Int, Int>> {
        return dsl.withRecursive(DSL.name("rq"), DSL.name("from_id"), DSL.name("to_id"))
            .`as`(
                DSL.select(DSL.field("from_id"), DSL.field("to_id"))
                    .from(DSL.table("edge"))
                    .where(DSL.field("from_id").eq(fromNodeId))
                    .unionAll(
                        DSL.select(DSL.field(DSL.name("e", "from_id")), DSL.field(DSL.name("e", "to_id")))
                            .from(DSL.name("rq"))
                            .join(DSL.table("edge").`as`("e"))
                            .on(DSL.field(DSL.name("rq", "to_id")).eq(DSL.field(DSL.name("e", "from_id"))))
                    )
            )
            .select()
            .from(DSL.name("rq"))
            .fetchSize(batchSize)
            .fetchStream()
            .map { it.getValue(0, Int::class.java) to it.getValue(1, Int::class.java) }
    }

    fun hasDirectEdges(fromNodeId: Int): Boolean =
        dsl.fetchExists(
            DSL.select()
            .from(DSL.table("edge"))
            .where(DSL.field(DSL.name("from_id")).eq(fromNodeId))
        )

    fun generateLargeTree(fromNodeId: Int, size: Int, arity: Int? = null) {
        val insert = dsl.insertInto(DSL.table("edge"))
            .columns(DSL.field("from_id"), DSL.field("to_id"))
            .values(0, 0) // param placeholders

        generateTreeSequence(fromNodeId, size, arity ?: log10(size.toDouble()).toInt())
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