package com.manikoske.guild.encounter

sealed interface Target {

    object Targets {
        fun possibleTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            return singleTargets(range, allies, enemies) +
                    doubleTargets(range, allies, enemies) +
                    nodeTargets(range, allies, enemies) +
                    Self
        }

        private fun singleTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            return allies.map { SingleAlly(range = range, others = listOf(it)) } +
                    enemies.map { SingleEnemy(range = range, others = listOf(it)) }
        }

        private fun doubleTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            val result: MutableList<Target> = mutableListOf()
            if (allies.isNotEmpty()) {
                for (i in allies.indices) {
                    for (j in i + 1..<allies.size) {
                        result.add(DoubleAlly(range = range, others = listOf(allies[i], allies[j])))
                    }
                }
            }
            if (enemies.isNotEmpty()) {
                for (i in enemies.indices) {
                    for (j in i + 1..<enemies.size) {
                        result.add(DoubleEnemy(range = range, others = listOf(enemies[i], enemies[j])))
                    }
                }
            }
            return result
        }

        private fun nodeTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            val result: MutableList<Target> = mutableListOf()
            if (allies.isNotEmpty()) {
                result.add(NodeAlly(range = range, others = allies))
            }
            if (enemies.isNotEmpty()) {
                result.add(NodeEnemy(range = range, others = enemies))
            }
            if (allies.isNotEmpty() || enemies.isNotEmpty()) {
                result.add(NodeEveryone(range = range, others = allies + enemies))
            }
            return result
        }

    }

    val range: Int
    val others: List<CharacterState>


    data class SingleEnemy(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data class SingleAlly(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data class DoubleEnemy(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data class DoubleAlly(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data class NodeAlly(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data class NodeEnemy(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data class NodeEveryone(
        override val range: Int,
        override val others: List<CharacterState>
    ) : Target

    data object Self : Target {
        override val range: Int
            get() = 0
        override val others: List<CharacterState>
            get() = listOf()
    }

}