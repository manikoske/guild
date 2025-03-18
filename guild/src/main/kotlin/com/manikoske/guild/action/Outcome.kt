package com.manikoske.guild.action

import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.PointOfView
import com.manikoske.guild.encounter.Target

sealed interface Outcome {

    fun resolve(
        pointOfView: PointOfView,
        target: Target,
        newPositionNodeId: Int,
        resourceCost: Int
    ): PointOfView

    fun commonResolution(
        executor: CharacterState,
        newPositionNodeId: Int,
        resourceCost: Int
    ) : CharacterState {
        return executor.moveTo(newPositionNodeId).spendResources(resourceCost).applyOverTimeEffects().tickEffects()
    }

    fun isValidTarget(executor: CharacterState, target: Target): Boolean


    data object OutcomeWithNoResolution : Outcome {

        override fun resolve(
            pointOfView: PointOfView,
            target: Target,
            newPositionNodeId: Int,
            resourceCost: Int
        ): PointOfView {
            return pointOfView.copy(taker = commonResolution(
                executor = pointOfView.taker,
                newPositionNodeId = newPositionNodeId,
                resourceCost = resourceCost
            ))
        }

        override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
            return true
        }
    }

    sealed interface OutcomeWithResolution : Outcome {

        val resolution: Resolution
        val selfResolution: Resolution.SupportResolution?


        override fun resolve(
            pointOfView: PointOfView,
            target: Target,
            newPositionNodeId: Int,
            resourceCost: Int
        ): PointOfView {

            val updatedPointOfView = target.applyResolution(pointOfView, resolution).let {
                if (selfResolution != null && selfResolution is Resolution) {
                    Target.Self(self = pointOfView.taker)
                        .applyResolution(it, selfResolution as Resolution.SupportResolution)
                } else {
                    it
                }
            }

            return updatedPointOfView.copy(
                taker = commonResolution(
                    executor = updatedPointOfView.taker,
                    newPositionNodeId = newPositionNodeId,
                    resourceCost = resourceCost
                )
            )
        }

    }

    sealed class SupportOutcome : OutcomeWithResolution {

        abstract override val resolution: Resolution.SupportResolution

        data class SelfSupport(
            override val resolution: Resolution.SupportResolution,
            override val selfResolution: Resolution.SupportResolution?
        ) : SupportOutcome() {
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Self
            }
        }

        sealed class SpellSupport : SupportOutcome() {
            abstract val range: Int
        }

        data class SingleSpellSupport(
            override val resolution: Resolution.SupportResolution,
            override val range: Int,
            override val selfResolution: Resolution.SupportResolution?
        ) : SpellSupport() {
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Single &&
                        target.scope == Target.Scope.Ally &&
                        target.range <= range
            }
        }

        data class DoubleSpellSupport(
            override val resolution: Resolution.SupportResolution,
            override val range: Int,
            override val selfResolution: Resolution.SupportResolution?
        ) : SpellSupport() {
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Double &&
                        target.scope == Target.Scope.Ally &&
                        target.range <= range
            }
        }

        data class NodeSpellSupport(
            override val resolution: Resolution.SupportResolution,
            override val range: Int,
            override val selfResolution: Resolution.SupportResolution?
        ) : SpellSupport() {
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Node &&
                        target.scope == Target.Scope.Ally &&
                        target.range <= range
            }
        }

    }

    sealed class AttackOutcome : OutcomeWithResolution {

        abstract override val resolution: Resolution.AttackResolution

        sealed class WeaponAttack : AttackOutcome()

        data class WeaponSingleAttack(
            override val resolution: Resolution.WeaponDamageResolution,
            override val selfResolution: Resolution.SupportResolution? = null,
        ) : WeaponAttack() {

            // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Single &&
                        target.range <= executor.character.arms().range() &&
                        target.scope == Target.Scope.Enemy
            }
        }

        data class WeaponDoubleAttack(
            override val resolution: Resolution.WeaponDamageResolution,
            override val selfResolution: Resolution.SupportResolution? = null,
        ) : WeaponAttack() {

            // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Double &&
                        target.range <= executor.character.arms().range() &&
                        target.scope == Target.Scope.Enemy
            }
        }

        data class WeaponNodeAttack(
            override val resolution: Resolution.WeaponDamageResolution,
            override val selfResolution: Resolution.SupportResolution? = null,
        ) : WeaponAttack() {
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Everyone &&
                        ((target.range == 0 && executor.character.arms().range() == 0) ||
                                target.range > 0 && executor.character.arms().range() > 0)
            }
        }

        sealed class SpellAttack : AttackOutcome() {
            abstract val range: Int
        }

        data class SpellSingleAttack(
            override val resolution: Resolution.SpellResolution,
            override val range: Int,
            override val selfResolution: Resolution.SupportResolution? = null,
        ) : SpellAttack() {

            // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Single &&
                        target.range <= range &&
                        target.scope == Target.Scope.Enemy
            }
        }

        data class SpellDoubleAttack(
            override val resolution: Resolution.SpellResolution,
            override val range: Int,
            override val selfResolution: Resolution.SupportResolution? = null,
        ) : SpellAttack() {

            // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Double &&
                        target.range <= range &&
                        target.scope == Target.Scope.Enemy
            }
        }

        data class SpellNodeAttack(
            override val resolution: Resolution.SpellResolution,
            override val range: Int,
            override val selfResolution: Resolution.SupportResolution? = null,
        ) : SpellAttack() {

            // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                return target is Target.Everyone &&
                        target.range <= range
            }
        }

    }




}