package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.rules.Die

sealed interface Resolution {

    fun resolve(executor: CharacterState, target: CharacterState): List<Event>

    sealed interface AttackResolution : Resolution {

        fun onDamageDealt(damageDealt : Int, target: CharacterState): List<Event> {

            val result: MutableList<Event> = mutableListOf()

            if (damageDealt >= target.currentHitPoints()) {
                result.add(Event.EffectAdded(Effect.ActionForcingEffect.Dying(0)))
            }

            target.effects.all()
                .filter { it.removeOnDamageTaken() }
                .forEach { effectRemovedOnDamage -> result.add(Event.EffectRemoved(effectRemovedOnDamage)) }

            return result
        }

        fun resolveEffect(effect: Effect?) : List<Event> {
            return if (effect != null) listOf(Event.EffectAdded(effect)) else listOf()
        }


        data class WeaponDamageResolution(
            val attackRollModifier: Int,
            val damageRollMultiplier: Int,
            val effectsOnHit: List<Effect>
        ) : AttackResolution {

            override fun resolve(executor: CharacterState, target: CharacterState): List<Event> {

                val result: MutableList<Event> = mutableListOf()

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
                    roll = Die.Roll(Die.Dice.of(Die.d20))
                )

                if (weaponAttackRoll.value >= armorClass.value) {
                    result.add(Event.WeaponAttackHit(weaponAttackRoll = weaponAttackRoll, armorClass = armorClass))

                    val weaponDamageRoll = WeaponDamageRoll(
                        weaponAttributeModifier = executor.character.weaponAttributeModifier(),
                        actionDamageMultiplier = damageRollMultiplier,
                        levelModifier = executor.character.levelModifier(),
                        roll = Die.Roll(executor.character.weaponDamage())
                    )
                    result.add(Event.WeaponDamageDealt(weaponDamageRoll = weaponDamageRoll))
                    result.addAll(onDamageDealt(damageDealt = weaponDamageRoll.value, target = target))
                    result.addAll(resolveEffect(effectsOnHit))
                } else {
                    result.add(Event.WeaponAttackMiss(weaponAttackRoll = weaponAttackRoll, armorClass = armorClass))
                }
                return result
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