package com.hafnium.supplychaintree.api.dto

import com.hafnium.supplychaintree.domain.TreeNode
import io.swagger.v3.oas.annotations.media.Schema

/**
 * The stream element that is retuned by the [com.hafnium.supplychaintree.api.SupplyChainTreeApi.fetchTree] API method,
 * meaning that from the node id [id] there are tree edges to the child node ids [to].
 */
@Schema(description = "One element in the tree structure stream representing the edges from one node to all child nodes")
data class FetchTreeNode(
    @Schema(description = "ID of this node")
    val id: Int,
    @Schema(description = "IDs of all direct child nodes")
    val to: List<Int>,
) {
    constructor(node: TreeNode) : this(node.id, node.children.map { it.toNodeId })
}