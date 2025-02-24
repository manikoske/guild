package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState

sealed interface Outcome {

    data object NoOutcome : Outcome

    sealed interface BeneficialOutcome : Outcome {

        val resolution: Resolution.BeneficialResolution

        data class SelfOutcome(
            override val resolution: Resolution.BeneficialResolution
        ) : BeneficialOutcome {

            fun resolve(self: CharacterState): CharacterState {
                return when (resolution) {
                    is Resolution.BeneficialResolution.AddEffect -> resolution.resolve(self)
                    is Resolution.BeneficialResolution.Healing -> resolution.resolve(self, self)
                    is Resolution.BeneficialResolution.RemoveEffect -> resolution.resolve(self)
                    is Resolution.BeneficialResolution.ResourceBoost -> resolution.resolve(self)
                }
            }
        }
    }

    sealed interface HarmfulOutcome : Outcome {

        // TODO add self and target resolutions on hit

        data class WeaponStrikeAttack (
            val resolution: Resolution.WeaponDamageResolution
        ) : HarmfulOutcome {

            fun resolve(attacker: CharacterState, target: CharacterState): CharacterState {
                when (val result = resolution.resolve(attacker, target)) {
                    is Resolution.HarmfulResolution.Result.Hit -> TODO()
                    Resolution.HarmfulResolution.Result.Miss -> TODO()
                }
            }
        }
    }

    sealed interface Resolution {

        sealed interface HarmfulResolution : Resolution {
            fun resolve(attacker: CharacterState, target: CharacterState) : Result

            sealed interface Result {
                data object Miss : Result
                data class Hit(val hitTarget : CharacterState) : Result
            }
        }

        data class WeaponDamageResolution(
            val attackRollBonusModifier: Int,
            val damageRollMultiplier: Int,
            val bonusDamageRoll: () -> Int = { 0 }
        ) : HarmfulResolution {

            override fun resolve(attacker: CharacterState, target: CharacterState): HarmfulResolution.Result {
                return if (attacker.character.weaponAttackRoll(attackRollBonusModifier) > target.character.armorClass()) {
                    HarmfulResolution.Result.Hit(
                        hitTarget = target.takeDamage(
                            attacker.character.weaponDamageRoll(damageRollMultiplier) + bonusDamageRoll.invoke()
                        )
                    )
                } else {
                    HarmfulResolution.Result.Miss
                }
            }
        }

        sealed interface SpellResolution : HarmfulResolution {
            val baseDifficultyClass: Int
            val executorAttributeType: Attribute.Type
            val targetAttributeType: Attribute.Type

            data class SpellDamageResolution(
                override val baseDifficultyClass: Int,
                override val executorAttributeType: Attribute.Type,
                override val targetAttributeType: Attribute.Type,
                val damageRoll: () -> Int,
            ) : SpellResolution {

                override fun resolve(attacker: CharacterState, target: CharacterState): HarmfulResolution.Result {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + attacker.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        HarmfulResolution.Result.Hit(
                            hitTarget = target.takeDamage(attacker.character.attributeRoll(executorAttributeType, damageRoll))
                        )
                    } else {
                        HarmfulResolution.Result.Miss
                    }
                }
            }

            data class SpellEffectResolution(
                override val baseDifficultyClass: Int,
                override val executorAttributeType: Attribute.Type,
                override val targetAttributeType: Attribute.Type,
                val effect: Effect
            ) : SpellResolution {

                override fun resolve(attacker: CharacterState, target: CharacterState): HarmfulResolution.Result {

                    return if (target.character.difficultyClassRoll(targetAttributeType) >=
                        baseDifficultyClass + attacker.character.difficultyClassBonus(executorAttributeType)
                    ) {
                        HarmfulResolution.Result.Hit( hitTarget = target.addEffect(effect))
                    } else {
                        HarmfulResolution.Result.Miss
                    }
                }
            }

        }


        sealed interface BeneficialResolution : Resolution {

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
                fun resolve(target: CharacterState): CharacterState {
                    return target.gainResources(amount)
                }
            }


            data class RemoveEffect(
                val effect: Effect
            ) : BeneficialResolution {
                fun resolve(target: CharacterState): CharacterState {
                    return target.removeEffect(effect)
                }

            }

            data class AddEffect(
                val effect: Effect
            ) : BeneficialResolution {

                fun resolve(target: CharacterState): CharacterState {
                    return target.addEffect(effect)
                }

            }
        }

    }


}