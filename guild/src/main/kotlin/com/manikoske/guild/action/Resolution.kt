package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.rules.Die
import com.manikoske.guild.rules.Rollable

sealed interface Resolution {

    fun resolve(executor: CharacterState, target: CharacterState): List<Event>

    sealed interface AttackResolution : Resolution

    data class WeaponDamageResolution(
        val attackRollModifier: Int,
        val damageRollMultiplier: Int,
    ) : AttackResolution {

        override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {

            val armorClass = ArmorClass(
                armorModifier = target.character.armorClassArmorModifier(),
                armsModifier = target.character.armorClassArmsModifier(),
                levelModifier = target.character.levelModifier(),
                armorAttributeModifier = target.character.armorLimitedDexterityModifier()
            )

            val weaponAttackRoll = WeaponAttackRoll(
                weaponAttributeModifier = executor.character.weaponAttributeModifier(),
                weaponAttackModifier = executor.character.weaponAttackModifier(),
                actionAttackModifier = attackRollModifier,
                levelModifier = target.character.levelModifier(),
                rollDie = Die.d20
            )


            return if (executor.character.weaponAttackRoll(attackRollModifier) > target.character.armorClass()) {
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


    data class ArmorClass(
        val armorModifier: Int,
        val armsModifier: Int,
        val levelModifier: Int,
        val armorAttributeModifier: Int
    )

    data class WeaponAttackRoll(
        val weaponAttributeModifier: Int,
        val weaponAttackModifier : Int,
        val actionAttackModifier: Int,
        val levelModifier: Int,
        val rollDie : Die,
    )

    data class WeaponAttackDamage(
        val weaponAttributeModifier: Int,
        val weaponAttackModifier : Int,
        val actionAttackModifier: Int,
        val levelModifier: Int,
        val rollDies : Dies,
    )

}