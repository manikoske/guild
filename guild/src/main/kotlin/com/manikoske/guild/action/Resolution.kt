package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.CharacterState.WeaponAttackOutcome
import com.manikoske.guild.rules.Die

sealed interface Resolution {

    fun resolve(executor: CharacterState, target: CharacterState): CharacterState.Outcome

    sealed interface AttackResolution : Resolution {

        data class WeaponDamageResolution(
            val attackRollModifier: Int,
            val damageRollMultiplier: Int,
            val effectsOnHit: List<Effect>
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): WeaponAttackOutcome {
                return target.attackedBy(
                    attacker = executor,
                    attackRollModifier = attackRollModifier,
                    damageRollMultiplier = damageRollMultiplier,
                    effectsOnHit = effectsOnHit
                )
            }
        }

        data class SpellDamageResolution(
            val baseDifficultyClass: Int,
            val executorAttributeType: Attribute.Type,
            val targetAttributeType: Attribute.Type,
            val damage: Die.Dice,
            val effectsOnHit: List<Effect>
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {

                val result: MutableList<Event> = mutableListOf()

                val spellAttackDifficultyClass = SpellAttackDifficultyClass(
                    spellAttributeModifier = executor.character.attributeModifier(executorAttributeType),
                    spellDifficultyClass = baseDifficultyClass,
                    levelModifier = executor.character.levelModifier()
                )

                val spellDefenseRoll = SpellDefenseRoll(
                    spellAttributeModifier = target.character.attributeModifier(targetAttributeType),
                    levelModifier = target.character.levelModifier(),
                    roll = Die.Roll(Die.Dice.of(Die.d20))
                )

                if (spellAttackDifficultyClass.value >= spellDefenseRoll.value) {
                    result.add(Event.SpellAttackHit(spellAttackDifficultyClass = spellAttackDifficultyClass, spellDefenseRoll = spellDefenseRoll))

                    val spellDamageRoll = SpellDamageRoll(
                        spellAttributeModifier = executor.character.attributeModifier(executorAttributeType),
                        levelModifier = executor.character.levelModifier(),
                        roll = Die.Roll(damage)
                    )

                    result.add(Event.SpellDamageDealt(spellDamageRoll))
                    result.addAll(onDamageDealt(damageDealt = spellDamageRoll.value, target = target))
                    result.addAll(resolveEffect(effect))
                } else {
                    result.add(Event.SpellAttackMiss(spellAttackDifficultyClass = spellAttackDifficultyClass, spellDefenseRoll = spellDefenseRoll))
                }

                return result
            }
        }
    }

    sealed interface SupportResolution : Resolution {

        data class Healing(
            val executorAttributeType: Attribute.Type,
            val heal: Die.Dice
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {

                val healRoll = HealRoll(
                    healAttributeModifier = executor.character.attributeModifier(executorAttributeType),
                    levelModifier = executor.character.levelModifier(),
                    roll = Die.Roll(heal)
                )

                return listOf(Event.Healed(healRoll))
            }
        }

        data class ResourceBoost(
            val amount: Int
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {
                return listOf(Event.ResourcesGained(amount))
            }
        }


        data class RemoveEffect(
            val effect: Effect
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {
                return listOf(Event.EffectRemoved(effect))
            }

        }

        data class AddEffect(
            val effect: Effect
        ) : SupportResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {
                return listOf(Event.EffectAdded(effect))
            }

        }
    }




}