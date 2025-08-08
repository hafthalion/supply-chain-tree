package com.prewave.supplychaintree.repository

import com.prewave.supplychaintree.exception.DuplicateEdgeException
import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.table
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Repository

@Repository
class SupplyChainTreeRepository(
    private val dsl: DSLContext,
) {
    fun createEdge(fromId: Int, toId: Int) {
        try {
            //TODO Use jooq generator for type-safe access
            dsl.insertInto(table("edge"))
                .set(field("from_id"), fromId)
                .set(field("to_id"), toId)
                .execute()
        }
        catch (e: DuplicateKeyException) {
            throw DuplicateEdgeException(fromId, toId, e)
        }
    }
}
