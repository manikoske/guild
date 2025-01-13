package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Class
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.Die

sealed interface Action {

    object Actions {

        private val noClassRestriction = listOf(Class.Fighter, Class.Rogue, Class.Ranger, Class.Cleric, Class.Wizard)

        val actions = listOf(

            MeleeWeaponAction(
                name = "Basic Melee Attack",
                resourceCost = 0,
                arity = TargetType.Arity.Single,
                classRestriction = noClassRestriction,
            ),
            RangedWeaponAction(
                name = "Basic Ranged Attack",
                resourceCost = 0,
                arity = TargetType.Arity.Single,
                classRestriction = noClassRestriction,
            ),
            SelfAction(
                name = "Disengage",
                resourceCost = 0,
                movement = Movement(type = Movement.Type.Normal, nodes = 1),
                classRestriction = noClassRestriction,
                effect = Effect.NoEffect
            ),
            SelfAction(
                name = "Dash",
                resourceCost = 0,
                movement = Movement(type = Movement.Type.Normal, nodes = 2),
                classRestriction = noClassRestriction,
                effect = Effect.NoEffect
            ),

            MeleeWeaponAction(
                name = "Shield Bash",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.OneHandedWeaponAndShield },
                damageRollMultiplier = 0,
                triggeredAction = TriggeredAction.TargetTriggeredAction(
                    effect = Effect.ApplyStatus(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.strength,
                        targetAttributeType = Attribute.Type.constitution,
                        status = Status.Stunned(roundsLeft = 1)
                    )
                )
            ),
            MeleeWeaponAction(
                name = "Cleave",
                resourceCost = 1,
                arity = TargetType.Arity.Double,
                classRestriction = listOf(Class.Fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.TwoHandedWeapon },
                attackRollBonusModifier = -2
            ),
            MeleeWeaponAction(
                name = "Dual attack",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.DualWeapon },
                attackRollBonusModifier = 2
            ),
            MeleeWeaponAction(
                name = "Heavy Blow",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                attackRollBonusModifier = -2,
                damageRollMultiplier = 2,
            ),
            MeleeWeaponAction(
                name = "Whirlwind",
                resourceCost = 2,
                arity = TargetType.Arity.Node,
                scope = TargetType.Scope.EveryoneElse,
                classRestriction = listOf(Class.Fighter),
                attackRollBonusModifier = -2,
            ),
            MeleeWeaponAction(
                name = "Charge",
                resourceCost = 1,
                movement = Movement(type = Movement.Type.Normal, nodes = 2),
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Fighter),
                attackRollBonusModifier = -2,
            ),
            SelfAction(
                name = "Second Wind",
                resourceCost = 1,
                classRestriction = listOf(Class.Fighter),
                effect =
                Effect.Healing(
                    healingRoll = { Die.d4.roll(1) }
                )
            ),


            MeleeWeaponAction(
                name = "Rend",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Rogue),
                triggeredAction = TriggeredAction.TargetTriggeredAction(
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
            MeleeWeaponAction(
                name = "Slice and Dice",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Rogue),
                triggeredAction = TriggeredAction.SelfTriggeredAction(
                    effect = Effect.ResourceBoost(
                        amount = 2
                    )
                )
            ),
            MeleeWeaponAction(
                name = "Shadow Step",
                resourceCost = 2,
                movement = Movement(type = Movement.Type.Special, nodes = 2),
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Rogue)
            ),

            MeleeWeaponAction(
                name = "Holy Strike",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Cleric),
                triggeredAction = TriggeredAction.TargetTriggeredAction(
                    effect = Effect.DirectDamage(
                        damageRoll = { Die.d4.roll(1) }
                    )
                )
            ),
            SpellAction(
                name = "Divine heal",
                resourceCost = 1,
                targetType = TargetType(scope = TargetType.Scope.Ally, range = 1, arity = TargetType.Arity.Single),
                classRestriction = listOf(Class.Cleric),
                effect = Effect.Healing(
                    healingRoll = { Die.d8.roll(1) }
                ),
                triggeredAction = TriggeredAction.SelfTriggeredAction(
                    effect = Effect.Healing(
                        healingRoll = { Die.d4.roll(1) }
                    )
                )
            ),
            SpellAction(
                name = "Mass heal",
                resourceCost = 2,
                targetType = TargetType(scope = TargetType.Scope.Ally, range = 1, arity = TargetType.Arity.Triple),
                classRestriction = listOf(Class.Cleric),
                effect = Effect.Healing(
                    healingRoll = { Die.d6.roll(1) }
                )
            ),


            RangedWeaponAction(
                name = "Entangle Shot",
                resourceCost = 1,
                arity = TargetType.Arity.Single,
                classRestriction = listOf(Class.Ranger),
                triggeredAction = TriggeredAction.TargetTriggeredAction(
                    effect = Effect.ApplyStatus(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.dexterity,
                        targetAttributeType = Attribute.Type.strength,
                        status = Status.Entangled(roundsLeft = 1)
                    )
                )
            ),
            RangedWeaponAction(
                name = "Volley",
                resourceCost = 2,
                arity = TargetType.Arity.Single,
                scope = TargetType.Scope.Everyone,
                classRestriction = listOf(Class.Ranger),
                attackRollBonusModifier = -2
            ),
            RangedWeaponAction(
                name = "Twin Shot",
                resourceCost = 1,
                arity = TargetType.Arity.Double,
                classRestriction = listOf(Class.Ranger),
                attackRollBonusModifier = -2
            ),

            SpellAction(
                name = "Fire bolt",
                resourceCost = 0,
                targetType = TargetType(scope = TargetType.Scope.Enemy, range = 2, arity = TargetType.Arity.Single),
                classRestriction = listOf(Class.Wizard),
                effect =
                Effect.AvoidableDamage(
                    baseDifficultyClass = 8,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damageRoll = { Die.d8.roll(1) }
                )

            ),
            SpellAction(
                name = "Fireball",
                resourceCost = 1,
                targetType = TargetType(scope = TargetType.Scope.Everyone, range = 2, arity = TargetType.Arity.Node),
                classRestriction = listOf(Class.Wizard),
                effect =
                Effect.AvoidableDamage(
                    baseDifficultyClass = 8,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damageRoll = { Die.d8.roll(1) }
                )
            ),
            SelfAction(
                name = "Teleport",
                resourceCost = 1,
                movement = Movement(type = Movement.Type.Special, nodes = 3),
                classRestriction = listOf(Class.Wizard),
                effect = Effect.NoEffect
            ),

            )
    }

    val name: String
    val movement: Movement
    val resourceCost: Int
    val classRestriction: List<Class>
    val armsRestriction: (arms: Inventory.Arms) -> Boolean
    val triggeredAction: TriggeredAction?

    fun targetType(character: Character): TargetType
    fun isHarmful(): Boolean
    fun effects(character: Character): List<Effect>


    sealed interface WeaponAction : Action {
        val arity: TargetType.Arity
        val scope: TargetType.Scope
        val attackRollBonusModifier: Int
        val damageRollMultiplier: Int

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

        override fun targetType(character: Character): TargetType {
            return when (val arms = character.arms()) {
                is Inventory.Arms.DualWeapon -> TargetType(scope = scope, range = 0, arity = arity)
                is Inventory.Arms.OneHandedWeaponAndShield -> TargetType(scope = scope, range = 0, arity = arity)
                is Inventory.Arms.RangedWeapon -> TargetType(scope = scope, range = arms.bothHands.range, arity = arity)
                is Inventory.Arms.TwoHandedWeapon -> TargetType(scope = scope, range = 0, arity = arity)
            }
        }

        override fun isHarmful(): Boolean {
            return true
        }

    }

    data class MeleeWeaponAction(
        override val name: String,
        override val resourceCost: Int,
        override val movement : Movement = Movement(type = Movement.Type.Normal, nodes = 1),
        override val arity: TargetType.Arity,
        override val scope: TargetType.Scope = TargetType.Scope.Enemy,
        override val classRestriction: List<Class>,
        override val armsRestriction: (arms: Inventory.Arms) -> Boolean = { arms -> arms !is Inventory.Arms.RangedWeapon },
        override val attackRollBonusModifier: Int = 0,
        override val damageRollMultiplier: Int = 1,
        override val triggeredAction: TriggeredAction? = null
    ) : WeaponAction

    data class RangedWeaponAction(
        override val name: String,
        override val resourceCost: Int,
        override val movement : Movement = Movement(type = Movement.Type.Normal, nodes = 1),
        override val arity: TargetType.Arity,
        override val scope: TargetType.Scope = TargetType.Scope.Enemy,
        override val classRestriction: List<Class>,
        override val attackRollBonusModifier: Int = 0,
        override val damageRollMultiplier: Int = 1,
        override val triggeredAction: TriggeredAction? = null
    ) : WeaponAction {

        override val armsRestriction: (arms: Inventory.Arms) -> Boolean
            get() = { arms -> arms is Inventory.Arms.RangedWeapon }
    }

    data class SpellAction(
        override val name: String,
        override val resourceCost: Int,
        override val movement : Movement = Movement(type = Movement.Type.Normal, nodes = 1),
        val targetType: TargetType,
        override val classRestriction: List<Class>,
        val effect: Effect,
        override val triggeredAction: TriggeredAction? = null
    ) : Action {

        override fun isHarmful(): Boolean {
            return effect.isHarmful()
        }

        override fun effects(character: Character): List<Effect> {
            return listOf(effect)
        }

        override val armsRestriction: (arms: Inventory.Arms) -> Boolean
            get() = { true }

        override fun targetType(character: Character): TargetType {
            return targetType
        }

    }

    data class SelfAction(
        override val name: String,
        override val resourceCost: Int,
        override val movement : Movement = Movement(type = Movement.Type.Normal, nodes = 1),
        override val classRestriction: List<Class>,
        val effect: Effect
    ) : Action {

        override val armsRestriction: (arms: Inventory.Arms) -> Boolean
            get() = { true }
        override val triggeredAction: TriggeredAction?
            get() = null

        override fun targetType(character: Character): TargetType {
            return TargetType(scope = TargetType.Scope.Self, range = 0, arity = TargetType.Arity.Single)
        }

        override fun isHarmful(): Boolean {
            return effect.isHarmful()
        }

        override fun effects(character: Character): List<Effect> {
            return listOf(effect)
        }
    }


}