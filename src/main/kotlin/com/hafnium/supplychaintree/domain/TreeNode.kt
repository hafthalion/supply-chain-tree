package com.hafnium.supplychaintree.domain

/**
 * A single tree node containing all its direct child edges/nodes.
 */
@ExposedCopyVisibility
data class TreeNode private constructor(
    val id: Int,
    private val _children: MutableSet<TreeEdge>,
) {
    val children: Set<TreeEdge> get() = _children

    constructor(edge: TreeEdge) : this(edge.fromNodeId, mutableSetOf(edge))
    constructor(id: Int) : this(id, mutableSetOf())

    fun addChild(edge: TreeEdge) {
        require(edge.fromNodeId == id) { "Edge $edge not going from this node $id" }

        _children.add(edge)
    }

    /**
     * Fold a child edge into this node when it has the same origin node id, or create a new node with the edge for different origin node id.
     */
    fun foldChild(edge: TreeEdge): TreeNodeFold {
        if (edge.fromNodeId == id) {
            addChild(edge)
            return TreeNodeFold(this, false)
        }
        else {
            return TreeNodeFold(TreeNode(edge), true)
        }
    }
}

data class TreeNodeFold(
    val node: TreeNode,
    val nodeChanged: Boolean,
)
