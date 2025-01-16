package com.manikoske.guild.encounter

data class PointOfView(
    val self: Int,
    val enemies: List<Int>,
    val allies: List<Int>,
    val everyone: List<Int>,
    val everyoneElse: List<Int>,
    val vantageNodes: List<VantageNode>,
) {

    data class VantageNode(
        val nodeId: Int,
        val requiredNormalMovement: Int,
        val requiredSpecialMovement: Int,
        val targetNodes: List<TargetNode>,
    )

    data class TargetNode(
        val nodeId: Int,
        val characterIds: List<Int>,
        val range: Int,

    )
}
