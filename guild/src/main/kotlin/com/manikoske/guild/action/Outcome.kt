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

            val savingThrow: SavingThrow

            data class WeaponDamage(
                val damageRoll: () -> Int,
                val attackRollBonusModifier: Int,
                val damageRollMultiplier: Int
            ) : HarmfulResolution {
                override val savingThrow: SavingThrow
                    get() = SavingThrow.ArmorClassSavingThrow(attackRollBonusModifier)

            }

            data class AvoidableDamage(
                val baseDifficultyClass: Int,
                val executorAttributeType: Attribute.Type,
                val targetAttributeType: Attribute.Type,
                val damageRoll: () -> Int
            ) : HarmfulResolution {
                override val savingThrow: SavingThrow
                    get() = SavingThrow.DifficultyClassSavingThrow(
                        baseDifficultyClass,
                        executorAttributeType,
                        targetAttributeType
                    )
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