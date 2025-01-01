package com.manikoske.guild.ability

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Class
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.*

sealed interface Ability {

    object Abilities {

        private val noClassRestriction = listOf(Class.fighter, Class.rogue, Class.ranger, Class.cleric, Class.wizard)

        private val abilities = listOf(

            MeleeWeaponAbility(
                name = "Basic Melee Attack",
                resourceCost = 0,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = noClassRestriction,
            ),
            RangedWeaponAbility(
                name = "Basic Ranged Attack",
                resourceCost = 0,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = noClassRestriction,
            ),
            SelfAbility(
                name = "Disengage",
                resourceCost = 0,
                classRestriction = noClassRestriction,
                outcome =
                Outcome.ApplyBuffEffect(
                    effect = Effect.Effects.opportunityAttackImmunity
                )
            ),
            SelfAbility(
                name = "Dash",
                resourceCost = 0,
                classRestriction = noClassRestriction,
                outcome =
                Outcome.ApplyBuffEffect(
                    effect = Effect.Effects.doubleMovement
                )
            ),

            MeleeWeaponAbility(
                name = "Shield Bash",
                resourceCost = 1,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.OneHandedWeaponAndShield },
                damageRollMultiplier = 0,
                onHit = TargetTriggeredAbility(
                    outcome = Outcome.ApplyEffect(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.strength,
                        targetAttributeType = Attribute.Type.constitution,
                        effect = Effect.Effects.stunned
                    )
                )
            ),
            MeleeWeaponAbility(
                name = "Cleave",
                resourceCost = 1,
                targetType = TargetType.MultiTargetWeapon(targetCount = 2),
                classRestriction = listOf(Class.fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.TwoHandedWeapon },
                attackRollBonusModifier = -2
            ),
            MeleeWeaponAbility(
                name = "Dual attack",
                resourceCost = 1,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.fighter),
                armsRestriction = { arms -> arms is Inventory.Arms.DualWeapon },
                attackRollBonusModifier = 2
            ),
            MeleeWeaponAbility(
                name = "Heavy Blow",
                resourceCost = 1,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.fighter),
                attackRollBonusModifier = -2,
                damageRollMultiplier = 2,
            ),
            MeleeWeaponAbility(
                name = "Whirlwind",
                resourceCost = 2,
                targetType = TargetType.AreaOfEffectWeapon,
                classRestriction = listOf(Class.fighter),
                attackRollBonusModifier = -2,
            ),
            SelfAbility(
                name = "Second Wind",
                resourceCost = 1,
                classRestriction = listOf(Class.fighter),
                outcome =

                Outcome.Healing(
                    healingRoll = { Die.d4.roll(1) }
                )
            ),


            MeleeWeaponAbility(
                name = "Rend",
                resourceCost = 1,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.rogue),
                onHit = TargetTriggeredAbility(
                    outcome = Outcome.ApplyEffect(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.dexterity,
                        targetAttributeType = Attribute.Type.constitution,
                        effect = Effect.DamageOverTime(
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
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.rogue),
                onHit = SelfTriggeredAbility(
                    outcome = Outcome.ResourceBoost(
                        amount = 2
                    )
                )
            ),

            MeleeWeaponAbility(
                name = "Holy Strike",
                resourceCost = 1,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.cleric),
                onHit = TargetTriggeredAbility(
                    outcome = Outcome.DirectDamage(
                        damageRoll = { Die.d4.roll(1) }
                    )
                )
            ),
            SpellAbility(
                name = "Divine heal",
                resourceCost = 1,
                targetType = TargetType.SingleRangedSpell(range = 2),
                classRestriction = listOf(Class.cleric),
                outcome = Outcome.Healing(
                    healingRoll = { Die.d8.roll(1) }
                ),
                onSuccess = SelfTriggeredAbility(
                    outcome = Outcome.Healing(
                        healingRoll = { Die.d4.roll(1) }
                    )
                )
            ),
            SpellAbility(
                name = "Mass heal",
                resourceCost = 2,
                targetType = TargetType.MultiRangedSpell(range = 2, targetCount = 3),
                classRestriction = listOf(Class.cleric),
                outcome = Outcome.Healing(
                    healingRoll = { Die.d4.roll(1) }
                )
            ),


            RangedWeaponAbility(
                name = "Entangle Shot",
                resourceCost = 1,
                targetType = TargetType.SingleTargetWeapon,
                classRestriction = listOf(Class.ranger),
                onHit = TargetTriggeredAbility(
                    outcome = Outcome.ApplyEffect(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.dexterity,
                        targetAttributeType = Attribute.Type.strength,
                        effect = Effect.Effects.entangled
                    )
                )
            ),
            RangedWeaponAbility(
                name = "Volley",
                resourceCost = 2,
                targetType = TargetType.AreaOfEffectWeapon,
                classRestriction = listOf(Class.ranger),
                attackRollBonusModifier = -2
            ),
            RangedWeaponAbility(
                name = "Twin Shot",
                resourceCost = 1,
                targetType = TargetType.MultiTargetWeapon(targetCount = 2),
                classRestriction = listOf(Class.ranger),
                attackRollBonusModifier = -2
            ),

            SpellAbility(
                name = "Fire bolt",
                resourceCost = 0,
                targetType = TargetType.SingleRangedSpell(range = 2),
                classRestriction = listOf(Class.wizard),
                outcome =
                    Outcome.AvoidableDamage(
                        baseDifficultyClass = 8,
                        executorAttributeType = Attribute.Type.intelligence,
                        targetAttributeType = Attribute.Type.dexterity,
                        damageRoll = { Die.d8.roll(1) }
                    )

            ),
            SpellAbility(
                name = "Fireball",
                resourceCost = 1,
                targetType = TargetType.AreaOfEffectRangedSpell(range = 2),
                classRestriction = listOf(Class.wizard),
                outcome =
                Outcome.AvoidableDamage(
                    baseDifficultyClass = 8,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damageRoll = { Die.d8.roll(1) }
                )
            ),

        )
    }

    sealed interface ExecutableAbility : Ability {

        // TODO add movement provider

        val name: String
        val resourceCost: Int
        val classRestriction: List<Class>
        val targetType: TargetType

        fun utility(): Int {
            // TODO implement with regard to various dimensions
            return 0
        }

        fun canBeExecutedBy(executor: Character, target: Character): Boolean {
            val classRestriction = classRestriction.contains(executor.isClass())
            val resourceRestriction = resourceCost < executor.currentResources()
            return classRestriction && resourceRestriction
        }
    }


    sealed interface WeaponAbility : ExecutableAbility {
        val armsRestriction: (arms: Inventory.Arms) -> Boolean
        val attackRollBonusModifier: Int
        val damageRollMultiplier: Int
        val onHit: TriggeredAbility?
        override fun canBeExecutedBy(executor: Character, target: Character): Boolean {
            return armsRestriction.invoke(executor.arms()) && super.canBeExecutedBy(executor, target)
        }

        fun execute(executor: Character, vararg targets: Character) {

            targets.forEach { target ->
                val weaponDamageOutcomes = when (val arms = executor.arms()) {
                    is Inventory.Arms.DualWeapon ->
                        listOf(
                            Outcome.WeaponDamage(
                                damageRoll = arms.mainHand.damageRoll,
                                attackRollBonusModifier = attackRollBonusModifier - 2,
                                damageRollMultiplier = damageRollMultiplier
                            ),
                            Outcome.WeaponDamage(
                                damageRoll = arms.offHand.damageRoll,
                                attackRollBonusModifier = attackRollBonusModifier - 4,
                                damageRollMultiplier = damageRollMultiplier
                            ),
                        )

                    is Inventory.Arms.OneHandedWeaponAndShield ->
                        listOf(
                            Outcome.WeaponDamage(
                                damageRoll = arms.mainHand.damageRoll,
                                attackRollBonusModifier = attackRollBonusModifier,
                                damageRollMultiplier = damageRollMultiplier
                            )
                        )

                    is Inventory.Arms.TwoHandedWeapon ->
                        listOf(
                            Outcome.WeaponDamage(
                                damageRoll = arms.bothHands.damageRoll,
                                attackRollBonusModifier = attackRollBonusModifier,
                                damageRollMultiplier = damageRollMultiplier
                            )
                        )

                    is Inventory.Arms.RangedWeapon ->
                        listOf(
                            Outcome.WeaponDamage(
                                damageRoll = arms.bothHands.damageRoll,
                                attackRollBonusModifier = attackRollBonusModifier,
                                damageRollMultiplier = damageRollMultiplier
                            )
                        )
                }
                weaponDamageOutcomes
                    .all { outcome -> outcome.resolve(executor, target) }
                    .let { when (val triggeredAbility = onHit) {
                        is SelfTriggeredAbility -> triggeredAbility.trigger(executor)
                        is TargetTriggeredAbility -> triggeredAbility.trigger(executor, target)
                        null -> Unit
                    } }
            }
        }

    }

    data class MeleeWeaponAbility(
        override val name: String,
        override val resourceCost: Int,
        override val targetType: TargetType.Weapon,
        override val classRestriction: List<Class>,
        override val armsRestriction: (arms: Inventory.Arms) -> Boolean = { arms -> arms !is Inventory.Arms.RangedWeapon },
        override val attackRollBonusModifier: Int = 0,
        override val damageRollMultiplier: Int = 1,
        override val onHit: TriggeredAbility? = null
    ) : WeaponAbility

    data class RangedWeaponAbility(
        override val name: String,
        override val resourceCost: Int,
        override val targetType: TargetType.Weapon,
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
        override val targetType: TargetType.Spell,
        override val classRestriction: List<Class>,
        val outcome: Outcome,
        val onSuccess: TriggeredAbility? = null
    ) : ExecutableAbility {

        fun execute(executor: Character, vararg targets: Character) {
            targets.forEach { target -> outcome.resolve(executor, target) }
        }
    }

    data class SelfAbility(
        override val name: String,
        override val resourceCost: Int,
        override val classRestriction: List<Class>,
        val outcome: Outcome
    ) : ExecutableAbility {
        override val targetType: TargetType
            get() = TargetType.Self

        fun execute(executor: Character) {
            outcome.resolve(executor, executor)
        }
    }

    sealed interface TriggeredAbility : Ability

    data class SelfTriggeredAbility(
        val outcome: Outcome
    ) : TriggeredAbility {
        fun trigger(triggerer: Character) {
            outcome.resolve(triggerer, triggerer)
        }
    }

    data class TargetTriggeredAbility(
        val outcome: Outcome
    ) : TriggeredAbility {
        fun trigger(triggerer: Character, target: Character) {
            outcome.resolve(triggerer, target)
        }
    }

}