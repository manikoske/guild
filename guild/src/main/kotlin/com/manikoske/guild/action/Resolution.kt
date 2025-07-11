package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.rules.Die

sealed interface Resolution {

    fun resolve(executor: CharacterState, target: CharacterState): Event.ResolutionEvent

    sealed interface AttackResolution : Resolution {

        data class WeaponDamageResolution(
            val attackRollModifier: Int,
            val damageRollMultiplier: Int,
            val effectsOnHit: List<Effect>
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): Event.WeaponAttackEvent {
                return target.attackBy(
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

            override fun resolve(executor: CharacterState, target: CharacterState): Event.SpellAttackEvent {
                return target.attackBy(
                    attacker = executor,
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
            val heal: Die.Dice
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): Event.Healed {
                return target.healBy(healer = executor, executorAttributeType = executorAttributeType, heal = heal)
            }
        }

        data class ResourceBoost(
            val amount: Int
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): Event.ResourceBoosted {
                return target.boostResources(amount)
            }
        }


        data class RemoveEffect(
            val effect: Effect
        ) : SupportResolution {
            override fun resolve(executor: CharacterState, target: CharacterState): Event.EffectRemoved {
                return target.removeEffect(effect)
            }
        }

        data class AddEffect(
            val effect: Effect
        ) : SupportResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): Event.EffectAdded {
                return target.addEffect(effect)
            }
        }

    }




}