package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState

sealed interface Outcome {

    data object NoOutcome : Outcome

    data class SelfOutcome(
        val resolution: Resolution.BeneficialResolution
    ) : Outcome {

        fun resolve(self: CharacterState) : CharacterState {
            return when (resolution) {
                is Resolution.BeneficialResolution.AddEffect -> resolution.resolve(self)
                is Resolution.BeneficialResolution.Healing -> resolution.resolve(self, self)
                is Resolution.BeneficialResolution.RemoveEffect -> resolution.resolve(self)
                is Resolution.BeneficialResolution.ResourceBoost -> resolution.resolve(self)
            }
        }
    }

    data class BeneficialOutcome(
        val resolution: Resolution.BeneficialResolution,
    ) : Outcome {

        data class BeneficialSphere(override val range: Int) : Targeting.Sphere {
            override val scope: Targeting.Scope
                get() = Targeting.Scope.Ally

        }

    }

    data class BeneficialOutcome(
        val resolution: Resolution.BeneficialResolution,
    ) : Outcome {

        data class BeneficialSphere(override val range: Int) : Targeting.Sphere {
            override val scope: Targeting.Scope
                get() = Targeting.Scope.Ally

        }

    }



    sealed interface Targeting {

        val range: Int
        val arity: Arity
        val scope: Scope


        enum class Arity {
            Single, Double, Triple, Node, Battleground
        }

        enum class Scope {
            Ally, Enemy, Self, Everyone, EveryoneElse
        }

        sealed interface Sphere : Targeting {

            override val arity: Arity
                get() = Arity.Node
        }


    }



    sealed interface Resolution {

        sealed interface HarmfulResolution {

            val onHitSelfEffect : Effect?
            val onHitTargetEffect : Effect?


            data class WeaponResolution(
                val attackRollBonusModifier: Int,
                val damageRollMultiplier: Int,
                override val onHitSelfEffect : Effect?,
                override val onHitTargetEffect : Effect?
            ) : HarmfulResolution {

                // TODO wtf to return
                fun resolve(attacker: CharacterState, target: CharacterState): CharacterState {

                    if (attacker.character.weaponAttackRoll(attackRollBonusModifier) > target.character.armorClass()) {
                        target.takeDamage(attacker.character.weaponDamageRoll(damageRollMultiplier))
                        if (onHitSelfEffect != null) attacker.addEffect(onHitSelfEffect)
                        if (onHitTargetEffect != null) target.addEffect(onHitTargetEffect)
                    }

                }

            }

            data class SpellResolution(
                val baseDifficultyClass: Int,
                val executorAttributeType: Attribute.Type,
                val targetAttributeType: Attribute.Type,
                val damageRoll: () -> Int,
                override val onHitSelfEffect : Effect?,
                override val onHitTargetEffect : Effect?
            ) : HarmfulResolution {

                // TODO wtf to return
                fun resolve(attacker: CharacterState, target: CharacterState): CharacterState {

                    if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + attacker.character.difficultyClassBonus(executorAttributeType)) {

                        target.takeDamage(attacker.character.attributeRoll(executorAttributeType, damageRoll))
                        if (onHitSelfEffect != null) attacker.addEffect(onHitSelfEffect)
                        if (onHitTargetEffect != null) target.addEffect(onHitTargetEffect)
                    }

                }
            }

            data class AddEffect(
                val baseDifficultyClass: Int,
                val executorAttributeType: Attribute.Type,
                val targetAttributeType: Attribute.Type,
                val effect: Effect
            ) : HarmfulResolution {
                override val savingThrow: SavingThrow
                    get() = SavingThrow.DifficultyClassSavingThrow(
                        baseDifficultyClass,
                        executorAttributeType,
                        targetAttributeType
                    )
            }
        }

        sealed interface BeneficialResolution {

            data class Healing(
                val healingRoll: () -> Int
            ) : BeneficialResolution {
                fun resolve(healer: CharacterState, target: CharacterState): CharacterState {
                    return target.heal(
                        healer.character.attributeRoll(Attribute.Type.wisdom, healingRoll)
                    )
                }
            }

            data class ResourceBoost(
                val amount: Int
            ) : BeneficialResolution {
                fun resolve(target: CharacterState) : CharacterState {
                    return target.gainResources(amount)
                }
            }


            data class RemoveEffect(
                val effect: Effect
            ) : BeneficialResolution {
                fun resolve(target: CharacterState) : CharacterState {
                    return target.removeEffect(effect)
                }

            }

            data class AddEffect(
                val effect: Effect
            ) : BeneficialResolution {

                fun resolve(target: CharacterState) : CharacterState {
                    return target.addEffect(effect)
                }

            }
        }

    }




}