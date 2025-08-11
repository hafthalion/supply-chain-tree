package com.prewave.supplychaintree.repository

import com.prewave.supplychaintree.exception.EdgeAlreadyExistsException
import com.prewave.supplychaintree.exception.EdgeNotFoundException
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository
import java.util.stream.Stream


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

    fun fetchEdges(fromNodeId: Int): Stream<Record> =
        dsl.select().from("edge").where(field("from_id").eq(fromNodeId))
            .fetchSize(batchSize).fetchStream()
}

