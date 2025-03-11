package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.PointOfView
import com.manikoske.guild.encounter.Target
import com.manikoske.guild.rules.Rollable

sealed interface Outcome {

    fun resolve(
        pointOfView: PointOfView,
        target: Target,
        newPositionNodeId: Int,
        resourceCost: Int
    ): PointOfView

    fun isValidTarget(executor: CharacterState, target: Target): Boolean


    data object OutcomeWithNoResolution : Outcome {

        override fun resolve(
            pointOfView: PointOfView,
            target: Target,
            newPositionNodeId: Int,
            resourceCost: Int
        ): PointOfView {
            return pointOfView
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
                taker = updatedPointOfView.taker
                    .moveTo(newPositionNodeId)
                    .spendResources(resourceCost)
                    .applyOverTimeEffects()
                    .tickEffects()
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

    sealed interface Resolution {

        fun resolve(executor: CharacterState, target: CharacterState): CharacterState

        sealed interface AttackResolution : Resolution

        data class WeaponDamageResolution(
            val attackRollBonusModifier: Int,
            val damageRollMultiplier: Int,
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {
                return if (executor.character.weaponAttackRoll(attackRollBonusModifier) > target.character.armorClass()) {
                    target.takeDamage(executor.character.weaponDamageRoll(damageRollMultiplier))
                } else {
                    target
                }
            }
        }

        sealed interface SpellResolution : AttackResolution {
            val baseDifficultyClass: Int
            val executorAttributeType: Attribute.Type
            val targetAttributeType: Attribute.Type

            data class SpellDamageResolution(
                override val baseDifficultyClass: Int,
                override val executorAttributeType: Attribute.Type,
                override val targetAttributeType: Attribute.Type,
                val damage: Rollable.Damage,
            ) : SpellResolution {

                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + executor.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        target.takeDamage(executor.character.attributeRoll(executorAttributeType, damage))
                    } else {
                        target
                    }
                }
            }

            data class SpellDamageAndEffectResolution(
                override val baseDifficultyClass: Int,
                override val executorAttributeType: Attribute.Type,
                override val targetAttributeType: Attribute.Type,
                val damage: Rollable.Damage,
                val effect: Effect
            ) : SpellResolution {

                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + executor.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        target
                            .takeDamage(executor.character.attributeRoll(executorAttributeType, damage))
                            .addEffect(effect)
                    } else {
                        target
                    }
                }
            }

            data class SpellEffectResolution(
                override val baseDifficultyClass: Int,
                override val executorAttributeType: Attribute.Type,
                override val targetAttributeType: Attribute.Type,
                val effect: Effect
            ) : SpellResolution {

                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + executor.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        target.addEffect(effect)
                    } else {
                        target
                    }
                }
            }

        }


        sealed interface SupportResolution : Resolution {

            data class Healing(
                val heal: Rollable.Heal
            ) : SupportResolution {
                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {
                    return target.heal(
                        executor.character.attributeRoll(Attribute.Type.wisdom, heal)
                    )
                }
            }

            data class ResourceBoost(
                val amount: Int
            ) : SupportResolution {
                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {
                    return target.gainResources(amount)
                }
            }


            data class RemoveEffect(
                val effect: Effect
            ) : SupportResolution {
                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {
                    return target.removeEffect(effect)
                }

            }

            data class AddEffect(
                val effect: Effect
            ) : SupportResolution {

                override fun resolve(executor: CharacterState, target: CharacterState): CharacterState {
                    return target.addEffect(effect)
                }

            }
        }

    }


}