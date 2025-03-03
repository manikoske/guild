package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.PointOfView
import com.manikoske.guild.rules.Rollable

sealed interface Outcome {

    fun resolve(executor: CharacterState, target: PointOfView.Target): OutcomeResult

    sealed interface OutcomeResult {
        data object TargetNotApplicable : OutcomeResult
        data class AppliedToTarget(val updatedCharacterStates: List<CharacterState>) : OutcomeResult
    }

    data object NoOutcome : Outcome {
        override fun resolve(executor: CharacterState, target: PointOfView.Target): OutcomeResult {
            return OutcomeResult.AppliedToTarget(updatedCharacterStates = listOf())
        }
    }


    sealed class SupportOutcome : Outcome {

        abstract val resolution: Resolution.SupportResolution
        abstract val afterCastSelfResolution: Resolution.SupportResolution?

        abstract fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean
        override fun resolve(executor: CharacterState, target: PointOfView.Target): OutcomeResult {
            return if (isValidTarget(executor, target)) {
                OutcomeResult.AppliedToTarget(
                    updatedCharacterStates = target.targetedCharacterStates().map { support(executor, it) }.flatten()
                )
            } else {
                OutcomeResult.TargetNotApplicable
            }
        }

        private fun support(executor: CharacterState, target: CharacterState): List<CharacterState> {

            return listOf(
                resolution.resolve(executor, target),
                afterCastSelfResolution?.resolve(executor, executor) ?: executor
            )

        }

        data class SelfSupport(
            override val resolution: Resolution.SupportResolution,
            override val afterCastSelfResolution: Resolution.SupportResolution?
        ) : SupportOutcome() {
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Self
            }
        }

        sealed class SpellSupport : SupportOutcome() {
            abstract val range: Int
        }

        data class SingleSpellSupport(
            override val resolution: Resolution.SupportResolution,
            override val range: Int,
            override val afterCastSelfResolution: Resolution.SupportResolution?
        ) : SpellSupport() {
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Single &&
                        target.scope == PointOfView.Target.Scope.Ally &&
                        target.range <= range
            }
        }

        data class DoubleSpellSupport(
            override val resolution: Resolution.SupportResolution,
            override val range: Int,
            override val afterCastSelfResolution: Resolution.SupportResolution?
        ) : SpellSupport() {
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Double &&
                        target.scope == PointOfView.Target.Scope.Ally &&
                        target.range <= range
            }
        }

        data class NodeSpellSupport(
            override val resolution: Resolution.SupportResolution,
            override val range: Int,
            override val afterCastSelfResolution: Resolution.SupportResolution?
        ) : SpellSupport() {
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Node &&
                        target.scope == PointOfView.Target.Scope.Ally &&
                        target.range <= range
            }
        }

    }

    sealed class AttackOutcome : Outcome {

        abstract val resolution: Resolution.AttackResolution
        abstract val afterAttackSelfResolution: Resolution.SupportResolution?
        abstract val onHitTargetResolution: Resolution.SpellResolution?

        abstract fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean

        override fun resolve(executor: CharacterState, target: PointOfView.Target): OutcomeResult {
            return if (isValidTarget(executor, target)) {
                OutcomeResult.AppliedToTarget(
                    updatedCharacterStates = target.targetedCharacterStates().map { attack(executor, it) }.flatten()
                )
            } else {
                OutcomeResult.TargetNotApplicable
            }
        }

        private fun attack(executor: CharacterState, target: CharacterState): List<CharacterState> {

            when (val result = resolution.resolve(executor, target)) {
                is Resolution.AttackResolution.Result.Hit -> {
                    val updatedTarget =
                        onHitTargetResolution.let {
                            if (it != null) {
                                when (val onHitResult = it.resolve(executor, result.hitTarget)) {
                                    is Resolution.AttackResolution.Result.Hit -> onHitResult.hitTarget
                                    Resolution.AttackResolution.Result.Miss -> result.hitTarget
                                }
                            } else result.hitTarget
                        }

                    val updatedExecutor =
                        afterAttackSelfResolution?.resolve(executor, executor) ?: executor

                    return listOf(updatedExecutor, updatedTarget)
                }

                Resolution.AttackResolution.Result.Miss ->
                    return listOf()
            }
        }

        sealed class WeaponAttack : AttackOutcome()

        data class WeaponSingleAttack(
            override val resolution: Resolution.WeaponDamageResolution,
            override val afterAttackSelfResolution: Resolution.SupportResolution? = null,
            override val onHitTargetResolution: Resolution.SpellResolution? = null
        ) : WeaponAttack() {

            // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Single &&
                        target.range <= executor.character.arms().range() &&
                        target.scope == PointOfView.Target.Scope.Enemy
            }
        }

        data class WeaponDoubleAttack(
            override val resolution: Resolution.WeaponDamageResolution,
            override val afterAttackSelfResolution: Resolution.SupportResolution? = null,
            override val onHitTargetResolution: Resolution.SpellResolution? = null
        ) : WeaponAttack() {

            // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Double &&
                        target.range <= executor.character.arms().range() &&
                        target.scope == PointOfView.Target.Scope.Enemy
            }
        }

        data class WeaponNodeAttack(
            override val resolution: Resolution.WeaponDamageResolution,
            override val afterAttackSelfResolution: Resolution.SupportResolution? = null,
            override val onHitTargetResolution: Resolution.SpellResolution? = null
        ) : WeaponAttack() {
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Node &&
                        ((target.range == 0 && executor.character.arms()
                            .range() == 0) || target.range > 0 && executor.character.arms().range() > 0) &&
                        target.scope == PointOfView.Target.Scope.Everyone
            }
        }

        sealed class SpellAttack : AttackOutcome() {
            abstract val range: Int
        }

        data class SpellSingleAttack(
            override val resolution: Resolution.SpellResolution,
            override val range: Int,
            override val afterAttackSelfResolution: Resolution.SupportResolution? = null,
            override val onHitTargetResolution: Resolution.SpellResolution? = null
        ) : SpellAttack() {

            // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Single &&
                        target.range <= range &&
                        target.scope == PointOfView.Target.Scope.Enemy
            }
        }

        data class SpellDoubleAttack(
            override val resolution: Resolution.SpellResolution,
            override val range: Int,
            override val afterAttackSelfResolution: Resolution.SupportResolution? = null,
            override val onHitTargetResolution: Resolution.SpellResolution? = null
        ) : SpellAttack() {

            // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Double &&
                        target.range <= range &&
                        target.scope == PointOfView.Target.Scope.Enemy
            }
        }

        data class SpellNodeAttack(
            override val resolution: Resolution.SpellResolution,
            override val range: Int,
            override val afterAttackSelfResolution: Resolution.SupportResolution? = null,
            override val onHitTargetResolution: Resolution.SpellResolution? = null
        ) : SpellAttack() {

            // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
            override fun isValidTarget(executor: CharacterState, target: PointOfView.Target): Boolean {
                return target is PointOfView.Target.Node &&
                        target.range <= range &&
                        target.scope == PointOfView.Target.Scope.Everyone
            }
        }

    }

    sealed interface Resolution {

        sealed interface AttackResolution : Resolution {
            fun resolve(attacker: CharacterState, target: CharacterState): Result

            sealed interface Result {
                data object Miss : Result
                data class Hit(val hitTarget: CharacterState) : Result
            }
        }

        data class WeaponDamageResolution(
            val attackRollBonusModifier: Int,
            val damageRollMultiplier: Int,
        ) : AttackResolution {

            override fun resolve(attacker: CharacterState, target: CharacterState): AttackResolution.Result {
                return if (attacker.character.weaponAttackRoll(attackRollBonusModifier) > target.character.armorClass()) {
                    AttackResolution.Result.Hit(
                        hitTarget = target.takeDamage(attacker.character.weaponDamageRoll(damageRollMultiplier))
                    )
                } else {
                    AttackResolution.Result.Miss
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

                override fun resolve(attacker: CharacterState, target: CharacterState): AttackResolution.Result {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + attacker.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        AttackResolution.Result.Hit(
                            hitTarget = target.takeDamage(
                                attacker.character.attributeRoll(
                                    executorAttributeType,
                                    damage
                                )
                            )
                        )
                    } else {
                        AttackResolution.Result.Miss
                    }
                }
            }

            data class SpellEffectResolution(
                override val baseDifficultyClass: Int,
                override val executorAttributeType: Attribute.Type,
                override val targetAttributeType: Attribute.Type,
                val effect: Effect
            ) : SpellResolution {

                override fun resolve(attacker: CharacterState, target: CharacterState): AttackResolution.Result {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + attacker.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        AttackResolution.Result.Hit(hitTarget = target.addEffect(effect))
                    } else {
                        AttackResolution.Result.Miss
                    }
                }
            }

        }


        sealed interface SupportResolution : Resolution {

            fun resolve(support: CharacterState, target: CharacterState): CharacterState

            data class Healing(
                val heal: Rollable.Heal
            ) : SupportResolution {
                override fun resolve(support: CharacterState, target: CharacterState): CharacterState {
                    return target.heal(
                        support.character.attributeRoll(Attribute.Type.wisdom, heal)
                    )
                }
            }

            data class ResourceBoost(
                val amount: Int
            ) : SupportResolution {
                override fun resolve(support: CharacterState, target: CharacterState): CharacterState {
                    return target.gainResources(amount)
                }
            }


            data class RemoveEffect(
                val effect: Effect
            ) : SupportResolution {
                override fun resolve(support: CharacterState, target: CharacterState): CharacterState {
                    return target.removeEffect(effect)
                }

            }

            data class AddEffect(
                val effect: Effect
            ) : SupportResolution {

                override fun resolve(support: CharacterState, target: CharacterState): CharacterState {
                    return target.addEffect(effect)
                }

            }
        }

    }


}