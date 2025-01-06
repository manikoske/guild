package com.manikoske.guild.ability

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Class
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.Die

sealed interface Ability {

    object Abilities {

        private val noClassRestriction = listOf(Class.Fighter, Class.Rogue, Class.Ranger, Class.Cleric, Class.Wizard)

        val abilities = listOf(

            MeleeWeaponAbility(
                name = "Basic Melee Attack",
                resourceCost = 0,
                arity = TargetType.Arity.Single,
                classRestriction = noClassRestriction,
            ),
            RangedWeaponAbility(
                name = "Basic Ranged Attack",
                resourceCost = 0,
                arity = TargetType.Arity.Single,
                classRestriction = noClassRestriction,
            ),
            SelfAbility(
                name = "Disengage",
                resourceCost = 0,
                movement = Movement.SpecialMovement(nodes = 1),
                classRestriction = noClassRestriction,
                effect = Effect.NoEffect
            ),
            SelfAbility(
                name = "Dash",
                resourceCost = 0,
                movement = Movement.NormalMovement(nodes = 2),
                classRestriction = noClassRestriction,
                effect = Effect.NoEffect
            ),

            MeleeWeaponAbility(
                name = "Shield Bash",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.OneHandedWeaponAndShield },
                damageRollMultiplier = 0,
                onHit = TriggeredAbility.TargetTriggeredAbility(
                    effect = Effect.ApplyStatus(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.strength,
                        targetAttributeType = Attribute.Type.constitution,
                        status = Status.Stunned(roundsLeft = 1)
                    )
                )
            ),
            MeleeWeaponAbility(
                name = "Cleave",
                resourceCost = 1,
                arity = TargetType.Arity.Double,
                classRestriction = listOf(Class.Fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.TwoHandedWeapon },
                attackRollBonusModifier = -2
            ),
            MeleeWeaponAbility(
                name = "Dual attack",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.DualWeapon },
                attackRollBonusModifier = 2
            ),
            MeleeWeaponAbility(
                name = "Heavy Blow",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                attackRollBonusModifier = -2,
                damageRollMultiplier = 2,
            ),
            MeleeWeaponAbility(
                name = "Whirlwind",
                resourceCost = 2,
                arity = TargetType.Arity.Area,
                classRestriction = listOf(Class.Fighter),
                attackRollBonusModifier = -2,
            ),
            MeleeWeaponAbility(
                name = "Charge",
                resourceCost = 1,
                movement = Movement.NormalMovement(nodes = 2),
                arity = TargetType.Arity.Area,
                classRestriction = listOf(Class.Fighter),
                attackRollBonusModifier = -2,
            ),
            SelfAbility(
                name = "Second Wind",
                resourceCost = 1,
                classRestriction = listOf(Class.Fighter),
                effect =
                Effect.Healing(
                    healingRoll = { Die.d4.roll(1) }
                )
            ),


            MeleeWeaponAbility(
                name = "Rend",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Rogue),
                onHit = TriggeredAbility.TargetTriggeredAbility(
                    effect = Effect.ApplyStatus(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.dexterity,
                        targetAttributeType = Attribute.Type.constitution,
                        status = Status.DamageOverTime(
                            name = "Bleeding",
                            roundsLeft = 3,
                            damageRoll = { Die.d4.roll(1) }
                        )
                    )
                )
            ),
            MeleeWeaponAbility(
                name = "Slice and Dice",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Rogue),
                onHit = TriggeredAbility.SelfTriggeredAbility(
                    effect = Effect.ResourceBoost(
                        amount = 2
                    )
                )
            ),
            MeleeWeaponAbility(
                name = "Shadow Step",
                resourceCost = 2,
                movement = Movement.SpecialMovement(nodes = 2),
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Rogue)
            ),

            MeleeWeaponAbility(
                name = "Holy Strike",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Cleric),
                onHit = TriggeredAbility.TargetTriggeredAbility(
                    effect = Effect.DirectDamage(
                        damageRoll = { Die.d4.roll(1) }
                    )
                )
            ),
            SpellAbility(
                name = "Divine heal",
                resourceCost = 1,
                targetType = TargetType(range = 1, arity = TargetType.Arity.Single),
                classRestriction = listOf(Class.Cleric),
                effect = Effect.Healing(
                    healingRoll = { Die.d8.roll(1) }
                ),
                onSuccess = TriggeredAbility.SelfTriggeredAbility(
                    effect = Effect.Healing(
                        healingRoll = { Die.d4.roll(1) }
                    )
                )
            ),
            SpellAbility(
                name = "Mass heal",
                resourceCost = 2,
                targetType = TargetType(range = 1, arity = TargetType.Arity.Triple),
                classRestriction = listOf(Class.Cleric),
                effect = Effect.Healing(
                    healingRoll = { Die.d6.roll(1) }
                )
            ),


            RangedWeaponAbility(
                name = "Entangle Shot",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Ranger),
                onHit = TriggeredAbility.TargetTriggeredAbility(
                    effect = Effect.ApplyStatus(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.dexterity,
                        targetAttributeType = Attribute.Type.strength,
                        status = Status.Entangled(roundsLeft = 1)
                    )
                )
            ),
            RangedWeaponAbility(
                name = "Volley",
                resourceCost = 2,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Ranger),
                attackRollBonusModifier = -2
            ),
            RangedWeaponAbility(
                name = "Twin Shot",
                resourceCost = 1,
                arity = TargetType.Arity.Double,
                classRestriction = listOf(Class.Ranger),
                attackRollBonusModifier = -2
            ),

            SpellAbility(
                name = "Fire bolt",
                resourceCost = 0,
                targetType = TargetType(range = 2, arity = TargetType.Arity.Single),
                classRestriction = listOf(Class.Wizard),
                effect =
                    Effect.AvoidableDamage(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.intelligence,
                        targetAttributeType = Attribute.Type.dexterity,
                        damageRoll = { Die.d8.roll(1) }
                    )

            ),
            SpellAbility(
                name = "Fireball",
                resourceCost = 1,
                targetType = TargetType(range = 2, arity = TargetType.Arity.Area),
                classRestriction = listOf(Class.Wizard),
                effect =
                Effect.AvoidableDamage(
                    baseDifficultyClass = 8,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damageRoll = { Die.d8.roll(1) }
                )
            ),
            SelfAbility(
                name = "Teleport",
                resourceCost = 1,
                movement = Movement.SpecialMovement(nodes = 3),
                classRestriction = listOf(Class.Wizard),
                effect = Effect.NoEffect
            ),

        )
    }

    sealed interface ExecutableAbility : Ability {

        val name: String
        val movement: Movement
        val resourceCost: Int
        val classRestriction: List<Class>
        val armsRestriction: (arms: Inventory.Arms) -> Boolean

        fun targetType(character: Character): TargetType
        fun isHarmful(): Boolean
        fun effects(character: Character): List<Effect>
        fun onSuccessEffect(): Effect



    }


    sealed interface WeaponAbility : ExecutableAbility {
        val arity: TargetType.Arity
        val attackRollBonusModifier: Int
        val damageRollMultiplier: Int
        val onHit: TriggeredAbility?

        override fun effects(character: Character): List<Effect> {
            return when (val arms = character.arms()) {
                is Inventory.Arms.DualWeapon ->
                    listOf(
                        Effect.WeaponDamage(
                            damageRoll = arms.mainHand.damageRoll,
                            attackRollBonusModifier = attackRollBonusModifier - 2,
                            damageRollMultiplier = damageRollMultiplier
                        ),
                        Effect.WeaponDamage(
                            damageRoll = arms.offHand.damageRoll,
                            attackRollBonusModifier = attackRollBonusModifier - 4,
                            damageRollMultiplier = damageRollMultiplier
                        ),
                    )

                is Inventory.Arms.OneHandedWeaponAndShield ->
                    listOf(
                        Effect.WeaponDamage(
                            damageRoll = arms.mainHand.damageRoll,
                            attackRollBonusModifier = attackRollBonusModifier,
                            damageRollMultiplier = damageRollMultiplier
                        )
                    )

                is Inventory.Arms.TwoHandedWeapon ->
                    listOf(
                        Effect.WeaponDamage(
                            damageRoll = arms.bothHands.damageRoll,
                            attackRollBonusModifier = attackRollBonusModifier,
                            damageRollMultiplier = damageRollMultiplier
                        )
                    )

                is Inventory.Arms.RangedWeapon ->
                    listOf(
                        Effect.WeaponDamage(
                            damageRoll = arms.bothHands.damageRoll,
                            attackRollBonusModifier = attackRollBonusModifier,
                            damageRollMultiplier = damageRollMultiplier
                        )
                    )
            }
        }

        override fun onSuccessEffect(): Effect {
            TODO("Not yet implemented")
        }

        override fun targetType(character: Character): TargetType {
            return when (val arms = character.arms()) {
                is Inventory.Arms.DualWeapon -> TargetType(range = 0, arity)
                is Inventory.Arms.OneHandedWeaponAndShield -> TargetType(range = 0, arity)
                is Inventory.Arms.RangedWeapon -> TargetType(range = arms.bothHands.range, arity)
                is Inventory.Arms.TwoHandedWeapon -> TargetType(range = 0, arity)
            }
        }

        override fun isHarmful(): Boolean {
            return true
        }

    }

    data class MeleeWeaponAbility(
        override val name: String,
        override val resourceCost: Int,
        override val movement: Movement = Movement.NormalMovement(1),
        override val arity: TargetType.Arity,
        override val classRestriction: List<Class>,
        override val armsRestriction: (arms: Inventory.Arms) -> Boolean = { arms -> arms !is Inventory.Arms.RangedWeapon },
        override val attackRollBonusModifier: Int = 0,
        override val damageRollMultiplier: Int = 1,
        override val onHit: TriggeredAbility? = null
    ) : WeaponAbility

    data class RangedWeaponAbility(
        override val name: String,
        override val resourceCost: Int,
        override val movement: Movement = Movement.NormalMovement(1),
        override val arity: TargetType.Arity,
        override val classRestriction: List<Class>,
        override val attackRollBonusModifier: Int = 0,
        override val damageRollMultiplier: Int = 1,
        override val onHit: TriggeredAbility? = null
    ) : WeaponAbility {

        override val armsRestriction: (arms: Inventory.Arms) -> Boolean
            get() = { arms -> arms is Inventory.Arms.RangedWeapon }
    }

    data class SpellAbility(
        override val name: String,
        override val resourceCost: Int,
        override val movement: Movement = Movement.NormalMovement(1),
        val targetType: TargetType,
        override val classRestriction: List<Class>,
        val effect: Effect,
        val onSuccess: TriggeredAbility? = null
    ) : ExecutableAbility {

        override fun isHarmful() : Boolean {
            return effect.isHarmful()
        }
        override val armsRestriction: (arms: Inventory.Arms) -> Boolean
            get() = { true }

        override fun targetType(character: Character): TargetType {
            return targetType
        }

    }

    data class SelfAbility(
        override val name: String,
        override val resourceCost: Int,
        override val movement: Movement = Movement.NormalMovement(1),
        override val classRestriction: List<Class>,
        val effect: Effect
    ) : ExecutableAbility {

        override val armsRestriction: (arms: Inventory.Arms) -> Boolean
            get() = { true }

        override fun targetType(character: Character): TargetType {
            return TargetType(range = 0, arity = TargetType.Arity.Self)
        }

        override fun isHarmful(): Boolean {
            return effect.isHarmful()
        }
    }

    sealed interface TriggeredAbility : Ability {
        data class SelfTriggeredAbility(
            val effect: Effect
        ) : TriggeredAbility {
            fun trigger(triggererContext: Encounter.CharacterContext) {
                effect.throwSave(triggererContext, triggererContext)
            }
        }

        data class TargetTriggeredAbility(
            val effect: Effect
        ) : TriggeredAbility {
            fun trigger(triggererContext: Encounter.CharacterContext, targetContext: Encounter.CharacterContext) {
                effect.throwSave(triggererContext, targetContext)
            }
        }
    }
}