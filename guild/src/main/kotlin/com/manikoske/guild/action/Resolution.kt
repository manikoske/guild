package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.rules.Rollable

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