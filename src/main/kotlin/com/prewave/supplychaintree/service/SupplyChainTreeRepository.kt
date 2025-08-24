package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeConflictException
import com.prewave.supplychaintree.jooq.tables.references.EDGE
import org.jooq.DSLContext
import org.jooq.impl.DSL.*
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
class SupplyChainTreeRepository(
    private val dsl: DSLContext,
) {
    @Throws(EdgeAlreadyExistsException::class)
    fun createEdge(fromNodeId: Int, toNodeId: Int) {
        try {
            dsl.newRecord(EDGE).apply {
                fromId = fromNodeId
                toId = toNodeId
                insert()
            }
        }
        catch (e: DuplicateKeyException) {
            throw EdgeAlreadyExistsException(fromNodeId, toNodeId, e)
        }
    }

    fun deleteEdge(fromNodeId: Int, toNodeId: Int): Int {
        val deletedRows = dsl.deleteFrom(EDGE).where(EDGE.FROM_ID.eq(fromNodeId).and(EDGE.TO_ID.eq(toNodeId))).execute()

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
    //TODO Use jooq type-safe access in with query?
    fun fetchReachableEdges(fromNodeId: Int): Stream<TreeEdge> {
        return dsl.withRecursive(name("rq"), name("from_id"), name("to_id"))
            .`as`(select(field("from_id"), field("to_id")).from(table("edge"))
                .where(field("from_id").eq(fromNodeId))
                .unionAll(select(field(name("e", "from_id")), field(name("e", "to_id"))).from(name("rq"))
                    .join(table("edge").`as`("e"))
                    .on(field(name("rq", "to_id")).eq(field(name("e", "from_id"))))))
            .select()
            .from(name("rq"))
            .fetchSize(batchSize)
            .fetchStream()
            .map { TreeEdge(it.getValue(0, Int::class.java), it.getValue(1, Int::class.java)) }
    }

    @Throws(EdgeConflictException::class)
    fun createEdges(edges: Sequence<TreeEdge>) {
        edges.chunked(batchSize).forEach { chunk ->
            try {
                dsl.batchInsert(chunk.map {
                    dsl.newRecord(EDGE).apply {
                        fromId = it.fromNodeId
                        toId = it.toNodeId
                    }
                }).execute()
            }
            catch (e: DuplicateKeyException) {
                throw EdgeConflictException(e)
            }
        }
    }

    private val batchSize = 1_000
}