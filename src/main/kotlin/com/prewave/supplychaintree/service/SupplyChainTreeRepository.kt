package com.prewave.supplychaintree.service

import com.prewave.supplychaintree.domain.Tree
import com.prewave.supplychaintree.domain.TreeEdge
import com.prewave.supplychaintree.domain.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.domain.exception.EdgeConflictException
import com.prewave.supplychaintree.domain.exception.EdgeNotFoundException
import com.prewave.supplychaintree.domain.exception.TreeNotFoundException
import com.prewave.supplychaintree.jooq.tables.records.EdgeRecord
import com.prewave.supplychaintree.jooq.tables.references.EDGE
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.select
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.util.stream.Stream
import kotlin.streams.asSequence

@Repository
class SupplyChainTreeRepository(
    private val dsl: DSLContext,
) {
    @Throws(EdgeAlreadyExistsException::class)
    fun createEdge(edge: TreeEdge) {
        try {
            dsl.executeInsert(edge.toEdgeRecord())
        }
        catch (e: DuplicateKeyException) {
            throw EdgeAlreadyExistsException(edge, e)
        }
    }

    @Throws(EdgeNotFoundException::class)
    fun deleteEdge(edge: TreeEdge) {
        if (dsl.executeDelete(edge.toEdgeRecord()) == 0) {
            throw EdgeNotFoundException(edge)
        }
    }

    @Throws(EdgeConflictException::class)
    fun createTree(tree: Tree) {
        try {
            tree.edges.use { edges ->
                edges.asSequence().map { it.toEdgeRecord() }.chunked(batchSize).forEach {
                    dsl.batchInsert(it).execute()
                }
            }
        }
        catch (e: DuplicateKeyException) {
            throw EdgeConflictException(e)
        }
    }

    /**
     * Fetch tree with all the reachable edges from a given node.
     * The client is responsible for closing the included stream to free DB resources.
     *
     * The method has to return all child edges of any given node in sequence as rows coming directly after each other
     * for the streaming algorithm to be able to work.
     *
     * For this a recursive SQL query is used at the moment as an easy and also surprisingly effective solution.
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
    @Throws(TreeNotFoundException::class)
    fun fetchTree(fromNodeId: Int): Tree {
        val count = dsl.selectCount().from(EDGE).where(EDGE.FROM_ID.eq(fromNodeId)).fetchSingle()

        if (count.get(0, Int::class.java) == 0) {
            throw TreeNotFoundException(fromNodeId)
        }

        return Tree(fromNodeId, fetchReachableEdges(fromNodeId))
    }

    private fun fetchReachableEdges(fromNodeId: Int): Stream<TreeEdge> {
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
            .map { it.toTreeEdge() }
    }

    private val batchSize = 5_000

    private fun TreeEdge.toEdgeRecord() = EdgeRecord(fromNodeId, toNodeId)

    private fun EdgeRecord.toTreeEdge() = TreeEdge(fromId, toId)
}