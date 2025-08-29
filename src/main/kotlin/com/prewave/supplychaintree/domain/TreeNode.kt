package com.prewave.supplychaintree.domain

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
        _children.add(edge.toNodeId)
    }

    fun foldChildOrCreateNew(edge: TreeEdge): TreeNodeFold {
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
    val parentChanged: Boolean,
)
