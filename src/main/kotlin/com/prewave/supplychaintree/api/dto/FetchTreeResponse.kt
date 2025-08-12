package com.prewave.supplychaintree.api.dto

/**
 * The stream element that is retuned by the [com.prewave.supplychaintree.api.SupplyChainTreeApi.fetchTree] API method,
 * meaning that from the node id [from] there are tree edges to the child node ids [to].
 */
data class FetchTreeResponse(
    val from: Int,
    val to: List<Int>,
)
