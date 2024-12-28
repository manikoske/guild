package com.manikoske.guild.rules

import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Current
import kotlin.math.max

sealed interface Ability {

    object Abilities {
        private val abilities = listOf(
            MeleeWeaponAbility(
                name = "Basic Melee Attack",
                actionType = ActionType.normal,
                resourceCost = 0,
                selfEffect = listOf(),
                targetEffect = listOf(),
                classRestriction = { true },
                { inventory -> inventory.meleeMainHand != null },
                attackRollModifier = 0,
                damageRollMultiplier = 1,
                additionalDamageRoll = { 0 },
            ),
            MeleeWeaponAbility(
                name = "Basic Melee Off-hand Attack",
                actionType = ActionType.bonus,
                resourceCost = 0,
                selfEffect = listOf(),
                targetEffect = listOf(),
                classRestriction = { true },
                { inventory -> inventory.meleeOffHand != null },
                attackRollModifier = 0,
                damageRollMultiplier = 1,
                additionalDamageRoll = { 0 },
            ),

            RangedWeaponAbility(
                name = "Basic Ranged Attack",
                actionType = ActionType.normal,
                resourceCost = 0,
                selfEffect = listOf(),
                targetEffect = listOf(),
                classRestriction = { true },
                { inventory -> inventory.rangedWeapon!= null },
                attackRollModifier = 0,
                damageRollMultiplier = 1,
                additionalDamageRoll = { 0 },
            ),

            OffensiveSpellAbility(
                name = "Arcane Bolt",
                actionType = ActionType.normal,
                resourceCost = 0,
                selfEffect = listOf(),
                targetEffect = listOf(),
                classRestriction = { clazz -> clazz == Class.wizard },
                inventoryRestriction = { true },
                casterAttributeType = Attribute.Type.intelligence,
                targetAttributeType = Attribute.Type.dexterity,
                damageRoll = { Die.d10.roll(1)}
            ),

            DefensiveSpellAbility(
                name = "Heal",
                actionType = ActionType.normal,
                resourceCost = 0,
                selfEffect = listOf(),
                targetEffect = listOf(),
                classRestriction = { clazz -> clazz == Class.cleric },
                inventoryRestriction = { true },
                healingRoll = { Die.d6.roll(1)}
            ),
            DefensiveSpellAbility(
                name = "Disengage",
                actionType = ActionType.normal,
                resourceCost = 0,
                selfEffect = listOf(Effect.Effects.disengaged),
                targetEffect = listOf(),
                classRestriction = { true },
                inventoryRestriction = { true },
                healingRoll = { 0 }
            ),
            DefensiveSpellAbility(
                name = "Dash",
                actionType = ActionType.normal,
                resourceCost = 0,
                selfEffect = listOf(Effect.Effects.doubleMovement),
                targetEffect = listOf(),
                classRestriction = { true },
                inventoryRestriction = { true },
                healingRoll = { 0 }
            ),
            DefensiveSpellAbility(
                name = "Second Wind",
                actionType = ActionType.bonus,
                resourceCost = 0,
                selfEffect = listOf(Effect.Effects.doubleMovement),
                targetEffect = listOf(),
                classRestriction = { clazz -> clazz == Class.fighter },
                inventoryRestriction = { true },
                healingRoll = { Class.fighter.hpDie.roll(1) }
            )


        )
    }
    val name: String
    val actionType: ActionType
    val resourceCost: Int
    val selfEffect: List<Effect>
    val targetEffect: List<Effect>
    val classRestriction: (clazz: Class) -> Boolean
    val inventoryRestriction: (inventory: Inventory) -> Boolean

    fun execute(executor: Character, target: Character) {

    }

    fun canExecute(executor: Character) {

    }

    // vs AC only melee
    data class MeleeWeaponAbility(
        override val name: String,
        override val actionType: ActionType,
        override val resourceCost: Int,
        override val selfEffect: List<Effect>,
        override val targetEffect: List<Effect>,
        override val classRestriction: (clazz: Class) -> Boolean,
        override val inventoryRestriction: (inventory: Inventory) -> Boolean,
        val attackRollModifier: Int,
        val damageRollMultiplier: Int,
        val additionalDamageRoll: () -> Int,
    ): Ability

    // vs AC and range
    data class RangedWeaponAbility(
        override val name: String,
        override val actionType: ActionType,
        override val resourceCost: Int,
        override val selfEffect: List<Effect>,
        override val targetEffect: List<Effect>,
        override val classRestriction: (clazz: Class) -> Boolean,
        override val inventoryRestriction: (inventory: Inventory) -> Boolean,
        val attackRollModifier: Int,
        val damageRollMultiplier: Int,
        val additionalDamageRoll: () -> Int,
    ) : Ability

    // caster to target, with DC,
    data class OffensiveSpellAbility(
        override val name: String,
        override val actionType: ActionType,
        override val resourceCost: Int,
        override val selfEffect: List<Effect>,
        override val targetEffect: List<Effect>,
        override val classRestriction: (clazz: Class) -> Boolean,
        override val inventoryRestriction: (inventory: Inventory) -> Boolean,
        val casterAttributeType: Attribute.Type,
        val targetAttributeType: Attribute.Type,
        val damageRoll: () -> Int,
    ) : Ability

    // caster to target, without DC,
    data class DefensiveSpellAbility(
        override val name: String,
        override val actionType: ActionType,
        override val resourceCost: Int,
        override val selfEffect: List<Effect>,
        override val targetEffect: List<Effect>,
        override val classRestriction: (clazz: Class) -> Boolean,
        override val inventoryRestriction: (inventory: Inventory) -> Boolean,
        val healingRoll: () -> Int,
    ) : Ability


    enum class TargetType {
        self, multi, areaOfEffect, single
    }

    enum class ActionType {
        bonus,
        normal
    }

    sealed interface SavingThrow {

        fun save(executor: Character, target: Character): Boolean

        data class ArmorClassSavingThrow(): SavingThrow {

        }

        data object NoSave: SavingThrow {
            override fun save(executor: Character, target: Character): Boolean {
                return false
            }
        }

        data class DifficultyClassSavingThrow(
            val baseDifficulty: Int,
            val executorAttributeType: Attribute.Type,
            val targetAttributeType: Attribute.Type): SavingThrow
        {
            override fun save(executor: Character, target: Character): Boolean {
                return Die.d20.roll(1) >= baseDifficulty + executor.attribute(executorAttributeType).modifier()
            }
        }
    }

    sealed interface Outcome {

        fun apply(current: Current): Current

        data class Damage(val roll: () -> Int): Outcome {
            override fun apply(current: Current): Current {
                return current.copy(damageTaken = max(0, current.damageTaken + roll.invoke()))
            }
        }
        data class Healing(val roll: () -> Int): Outcome {
            override fun apply(current: Current): Current {
                return current.copy(damageTaken = max(0, current.damageTaken - roll.invoke()))
            }
        }

        data class ResourceBoost(val boost: Int) : Outcome {
            override fun apply(current: Current): Current {
                return current.copy(resourcesSpent = max(0, current.resourcesSpent - boost))
            }
        }

        data class ApplyEffect(val effect: Effect): Outcome {
            override fun apply(current: Current): Current {
                TODO("Not yet implemented")
            }

        }

    }


}