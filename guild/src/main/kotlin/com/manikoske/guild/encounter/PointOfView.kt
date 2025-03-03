package com.manikoske.guild.encounter

data class PointOfView(
    val taker: CharacterState,
    val vantageNodes: List<VantageNode>,
) {

    data class VantageNode(
        val nodeId: Int,
        val requiredNormalMovement: Int,
        val requiredSpecialMovement: Int,
        val targets: List<Target>,
    )

    sealed interface Target {
        enum class Scope {
            Ally, Enemy, Everyone
        }

        object Targets {
            fun possibleTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>) : List<Target> {
                return singleTargets(Scope.Ally, range, allies) +
                        singleTargets(Scope.Enemy, range, enemies) +
                        doubleTargets(Scope.Ally, range, allies) +
                        doubleTargets(Scope.Enemy, range, enemies) +
                        nodeTargets(Scope.Ally, range, allies) +
                        nodeTargets(Scope.Enemy, range, enemies) +
                        nodeTargets(Scope.Everyone, range, allies + enemies)
            }

            private fun singleTargets(scope: Scope, range: Int, targets: List<CharacterState>): List<Single> {
                return targets.map { Single(scope = scope, range = range, single = it) }
            }

            private fun doubleTargets(scope: Scope, range: Int, targets: List<CharacterState>): List<Double> {
                val result: MutableList<Double> = mutableListOf()
                if (targets.isNotEmpty()) {
                    for (i in targets.indices) {
                        for (j in i + 1..<targets.size) {
                            result.add(Double(scope = scope, range = range, first = targets[i], second = targets[j]))
                        }
                    }
                }
                return result
            }

            private fun nodeTargets(scope: Scope, range: Int, targets: List<CharacterState>) : List<Node> {
                if (targets.isNotEmpty()) {
                    return listOf(Node(scope = scope, range, targets = targets))
                } else {
                    return listOf()
                }
            }

            private fun tripleTarget(targets: List<Int>): List<List<Int>> {
                val result: MutableList<List<Int>> = mutableListOf()
                if (targets.isNotEmpty()) {
                    for (i in targets.indices) {
                        for (j in i + 1..<targets.size) {
                            for (k in j + 1..<targets.size) {
                                result.add(listOf(targets[i], targets[j], targets[k]))
                            }
                        }
                    }
                }
                return result
            }
        }

        fun targetedCharacterStates() : List<CharacterState>

        data class Single(
            val scope: Scope,
            val range: Int,
            val single: CharacterState
        ) : Target {
            override fun targetedCharacterStates(): List<CharacterState> {
                return listOf(single)
            }
        }

        data class Double(
            val scope: Scope,
            val range: Int,
            val first: CharacterState,
            val second: CharacterState,
        ) : Target {
            override fun targetedCharacterStates(): List<CharacterState> {
                return listOf(first, second)
            }
        }

        data class Node(
            val scope: Scope,
            val range: Int,
            val targets: List<CharacterState>
        ) : Target {
            override fun targetedCharacterStates(): List<CharacterState> {
                return targets
            }
        }

        data class Self(val self: CharacterState) : Target {
            override fun targetedCharacterStates(): List<CharacterState> {
                return listOf(self)
            }
        }

    }
}
