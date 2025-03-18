package com.manikoske.guild.encounter

import com.manikoske.guild.action.Outcome
import com.manikoske.guild.action.Resolution

sealed interface Target {
    enum class Scope {
        Ally, Enemy
    }

    private fun merge(characterStates: List<CharacterState>, updatedCharacterState: CharacterState): List<CharacterState> {
        return characterStates.map { if (it.character.id == updatedCharacterState.character.id) updatedCharacterState else it }
    }

    fun updateWith(
        pointOfView: PointOfView,
        scope: Scope,
        targets: List<CharacterState>,
        resolution: Resolution
    ): PointOfView {
        return targets.fold(pointOfView) { updatedPointOfView, target ->
            val updatedCharacterState = resolution.resolve(updatedPointOfView.taker, target)
            when (scope) {
                Scope.Ally ->
                    updatedPointOfView.copy(allies = merge(updatedPointOfView.allies, updatedCharacterState))
                Scope.Enemy ->
                    updatedPointOfView.copy(enemies = merge(updatedPointOfView.enemies, updatedCharacterState))
            }
        }
    }

    object Targets {
        fun possibleTargets(range: Int, allies: List<CharacterState>, enemies: List<CharacterState>): List<Target> {
            return singleTargets(Scope.Ally, range, allies) +
                    singleTargets(Scope.Enemy, range, enemies) +
                    doubleTargets(Scope.Ally, range, allies) +
                    doubleTargets(Scope.Enemy, range, enemies) +
                    nodeTargets(Scope.Ally, range, allies) +
                    nodeTargets(Scope.Enemy, range, enemies) +
                    everyoneTargets(range, allies, enemies)
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

        private fun nodeTargets(scope: Scope, range: Int, targets: List<CharacterState>): List<Node> {
            return if (targets.isNotEmpty()) {
                listOf(Node(scope = scope, range, targets = targets))
            } else {
                listOf()
            }
        }

        private fun everyoneTargets(
            range: Int,
            allies: List<CharacterState>,
            enemies: List<CharacterState>
        ): List<Everyone> {
            return if (allies.isNotEmpty() || enemies.isNotEmpty()) {
                listOf(Everyone(range = range, allies = allies, enemies = enemies))
            } else {
                listOf()
            }
        }
    }

    fun applyResolution(pointOfView: PointOfView, resolution: Resolution): PointOfView

    data class Single(
        val scope: Scope,
        val range: Int,
        val single: CharacterState
    ) : Target {
        override fun applyResolution(pointOfView: PointOfView, resolution: Resolution): PointOfView {
            return updateWith(pointOfView, scope, listOf(single), resolution)
        }
    }

    data class Double(
        val scope: Scope,
        val range: Int,
        val first: CharacterState,
        val second: CharacterState,
    ) : Target {
        override fun applyResolution(pointOfView: PointOfView, resolution: Resolution): PointOfView {
            return updateWith(pointOfView, scope, listOf(first, second), resolution)
        }
    }

    data class Node(
        val scope: Scope,
        val range: Int,
        val targets: List<CharacterState>
    ) : Target {
        override fun applyResolution(pointOfView: PointOfView, resolution: Resolution): PointOfView {
            return updateWith(pointOfView, scope, targets, resolution)
        }
    }

    data class Everyone(
        val range: Int,
        val allies: List<CharacterState>,
        val enemies: List<CharacterState>
    ) : Target {
        override fun applyResolution(pointOfView: PointOfView, resolution: Resolution): PointOfView {
            return updateWith(updateWith(pointOfView, Scope.Enemy, enemies, resolution), Scope.Ally, allies, resolution)
        }
    }

    data class Self(val self: CharacterState) : Target {
        override fun applyResolution(pointOfView: PointOfView, resolution: Resolution): PointOfView {
            return pointOfView.copy(taker = resolution.resolve(pointOfView.taker, pointOfView.taker))
        }
    }

}