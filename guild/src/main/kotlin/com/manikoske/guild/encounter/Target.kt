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
            return allies.map { SingleAlly(range = range, characterStates = listOf(it)) } +
                    enemies.map { SingleEnemy(range = range, characterStates = listOf(it)) }
        }

        private fun doubleTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            val result: MutableList<Target> = mutableListOf()
            if (allies.isNotEmpty()) {
                for (i in allies.indices) {
                    for (j in i + 1..<allies.size) {
                        result.add(DoubleAlly(range = range, characterStates = listOf(allies[i], allies[j])))
                    }
                }
            }
            if (enemies.isNotEmpty()) {
                for (i in enemies.indices) {
                    for (j in i + 1..<enemies.size) {
                        result.add(DoubleEnemy(range = range, characterStates = listOf(enemies[i], enemies[j])))
                    }
                }
            }
            return result
        }

        private fun nodeTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            val result: MutableList<Target> = mutableListOf()
            if (allies.isNotEmpty()) {
                result.add(NodeAlly(range = range, characterStates = allies))
            }
            if (enemies.isNotEmpty()) {
                result.add(NodeEnemy(range = range, characterStates = enemies))
            }
            if (allies.isNotEmpty() || enemies.isNotEmpty()) {
                result.add(NodeEveryone(range = range, characterStates = allies + enemies))
            }
            return result
        }

    }

    sealed interface Other : Target {
        val range: Int
        val characterStates: List<CharacterState>
    }

    data class SingleEnemy(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data class SingleAlly(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data class DoubleEnemy(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data class DoubleAlly(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data class NodeAlly(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data class NodeEnemy(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data class NodeEveryone(
        override val range: Int,
        override val characterStates: List<CharacterState>
    ) : Other

    data object Self : Target

}