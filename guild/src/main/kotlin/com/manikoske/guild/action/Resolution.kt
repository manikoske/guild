package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Effect
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Rules

sealed interface Resolution {

    fun resolve(executor: CharacterState, target: CharacterState): Event.ResolutionEvent

    sealed interface AttackResolution : Resolution {

        data class WeaponDamageResolution(
            val attackRollModifier: Int,
            val damageRollMultiplier: Int,
            val effectsOnHit: List<Effect>
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): Event.WeaponAttackEvent {
                return Rules.weaponAttackBy(
                    executor = executor,
                    target = target,
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
            val damage: Dice,
            val effectsOnHit: List<Effect>
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): Event.SpellAttackEvent {
                return Rules.weaponAttackBy(
                    executor = executor,
                    target = target,
                    baseDifficultyClass = baseDifficultyClass,
                    executorAttributeType = executorAttributeType,
                    targetAttributeType = targetAttributeType,
                    damage = damage,
                    effectsOnHit = effectsOnHit
                )
            }
        }
    }

    sealed interface SupportResolution : Resolution {

        data class Healing(
            val executorAttributeType: Attribute.Type,
            val heal: Dice
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): Event.Healed {
                return Rules.healBy(executor = executor, target = target, executorAttributeType = executorAttributeType, heal = heal)
            }
        }

        data class ResourceBoost(
            val amount: Int
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): Event.ResourceBoosted {
                return Rules.boostResources(target = target, amount = amount)
            }
        }


        data class RemoveEffect(
            val effect: Effect
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): Event.EffectRemoved {
                return Rules.removeEffect(target = target, effect = effect)
            }
        }

        data class AddEffect(
            val effect: Effect
        ) : SupportResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): Event.EffectAdded {
                return Rules.addEffect(target = target, effect = effect)
            }
        }

    }




}