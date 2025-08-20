package com.prewave.supplychaintree.domain

data class TreeNode(
    val nodeId: Int,
    val childIds: List<Int>,
)
