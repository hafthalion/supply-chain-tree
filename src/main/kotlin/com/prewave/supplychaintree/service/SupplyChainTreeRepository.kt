package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeConflictException
import com.prewave.supplychaintree.domain.exception.EdgeNotFoundException
import com.prewave.supplychaintree.jooq.tables.records.EdgeRecord
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
            dsl.executeInsert(EdgeRecord(fromNodeId, toNodeId))
        }
        catch (e: DuplicateKeyException) {
            throw EdgeAlreadyExistsException(fromNodeId, toNodeId, e)
        }
    }

    @Throws(EdgeConflictException::class)
    fun createEdges(edges: Sequence<TreeEdge>) {
        try {
            edges.map { EdgeRecord(it.fromNodeId, it.toNodeId) }.chunked(batchSize).forEach { dsl.batchInsert(it).execute() }
        }
        catch (e: DuplicateKeyException) {
            throw EdgeConflictException(e)
        }
    }

    @Throws(EdgeNotFoundException::class)
    fun deleteEdge(fromNodeId: Int, toNodeId: Int) {
        if (dsl.executeDelete(EdgeRecord(fromNodeId, toNodeId)) == 0) {
            throw EdgeNotFoundException(fromNodeId, toNodeId)
        }
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
    fun fetchReachableEdges(fromNodeId: Int): Stream<TreeEdge> {
        val rq = name("rq")
        val cte = rq.fields(EDGE.FROM_ID.name, EDGE.TO_ID.name)
            .`as`(dsl.select(EDGE.FROM_ID, EDGE.TO_ID)
                .from(EDGE)
                .where(EDGE.FROM_ID.eq(fromNodeId))
                .unionAll(
                    select(EDGE.FROM_ID, EDGE.TO_ID).from(EDGE).join(rq).on(EDGE.FROM_ID.eq(field(rq.append(EDGE.TO_ID.name), Int::class.java)))))

        return dsl.withRecursive(cte)
            .selectFrom(cte)
            .fetchSize(batchSize)
            .fetchStreamInto(EdgeRecord::class.java)
            .map { TreeEdge(it.fromId, it.toId) }
    }

    private val batchSize = 5_000
}