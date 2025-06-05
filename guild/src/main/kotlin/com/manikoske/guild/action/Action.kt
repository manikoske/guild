package com.manikoske.guild.action

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Class
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Target
import com.manikoske.guild.rules.Die

sealed interface Action {

    object Actions {

        val noClassRestriction = listOf(Class.Fighter, Class.Rogue, Class.Ranger, Class.Cleric, Class.Wizard)

        val basicActions = listOf(
            OutcomeAction.AttackAction.WeaponAttack.WeaponSingleAttack(
                name = "Basic Attack",
                movement = Movement(type = Movement.Type.Normal, amount = 1),
                classRestriction = noClassRestriction,
                resourceCost = 0,
                resolution = Resolution.AttackResolution.WeaponDamageResolution(
                    attackRollModifier = 0,
                    damageRollMultiplier = 1,
                    effectsOnHit = listOf()
                ),
            ),
            OutcomeAction.AttackAction.SpellAttack.SpellSingleAttack(
                name = "Cantrip",
                movement = Movement(type = Movement.Type.Normal, amount = 1),
                classRestriction = listOf(Class.Wizard),
                resourceCost = 0,
                resolution = Resolution.AttackResolution.SpellDamageResolution(
                    baseDifficultyClass = 10,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damage =  Die.Dice.of(Die.d6),
                    effectsOnHit = listOf()
                ),
                range = 1
            ),
            NoOutcomeAction(
                name = "Disengage",
                movement = Movement(type = Movement.Type.Special, amount = 1),
                resourceCost = 0,
                classRestriction = noClassRestriction
            ),
            NoOutcomeAction(
                name = "Dash",
                movement = Movement(type = Movement.Type.Normal, amount = 2),
                resourceCost = 0,
                classRestriction = noClassRestriction
            ),
        )

        val noAction = NoOutcomeAction(
            name = "No Action",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            classRestriction = noClassRestriction
        )

        val standUp = OutcomeAction.SupportAction.SelfSupport(
            name = "Stand Up",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            classRestriction = noClassRestriction,
            resolution = Resolution.SupportResolution.RemoveEffect(
                effect = Effect.ActionForcingEffect.Prone(dummy = 1)
            )
        )

        val fightForLife = NoOutcomeAction(
            name = "Fight For Life",
            movement = Movement(type = Movement.Type.Normal, amount = 0),
            resourceCost = 0,
            classRestriction = noClassRestriction
        )
    }

    val name: String
    val movement: Movement
    val resourceCost: Int
    val classRestriction: List<Class>

    fun isValidTarget(executor: CharacterState, target: Target): Boolean

    fun execute(
        executor: CharacterState,
        target: Target,
        newPositionNodeId: Int
    ): List<Event> {

        executor.takeAction()

        return listOf()

    }

    fun executeOutcome(executor: CharacterState, target: Target) : List<Event>

    data class NoOutcomeAction(
        override val name: String,
        override val movement: Movement,
        override val resourceCost: Int,
        override val classRestriction: List<Class>,
    ) : Action {

        override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
            return target is Target.Self
        }

        override fun executeOutcome(executor: CharacterState, target: Target) : List<Event> {
            return listOf()
        }
    }

    sealed interface OutcomeAction : Action {

        val resolution: Resolution
        val selfResolution: Resolution.SupportResolution?

        override fun executeOutcome(executor: CharacterState, target: Target): List<Event> {
            when (target) {
                is Target.Other -> target.characterStates
                Target.Self -> executor
            }
            return listOf()
        }

        sealed class SupportAction : OutcomeAction {

            abstract override val resolution: Resolution.SupportResolution

            data class SelfSupport(
                override val resolution: Resolution.SupportResolution,
                override val selfResolution: Resolution.SupportResolution? = null,
                override val name: String,
                override val movement: Movement,
                override val resourceCost: Int,
                override val classRestriction: List<Class>
            ) : SupportAction() {
                override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                    return target is Target.Self
                }
            }

            sealed class SpellSupportAction : SupportAction() {
                abstract val range: Int

                data class SingleSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>
                ) : SpellSupportAction() {
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.SingleAlly && target.range <= range
                    }
                }

                data class DoubleSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>
                ) : SpellSupportAction() {
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.DoubleAlly && target.range <= range
                    }
                }

                data class NodeSpellSupport(
                    override val resolution: Resolution.SupportResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>
                ) : SpellSupportAction() {
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.NodeAlly && target.range <= range
                    }
                }
            }
        }

        sealed class AttackAction : OutcomeAction {

            abstract override val resolution: Resolution.AttackResolution

            sealed class WeaponAttack : AttackAction() {

                data class WeaponSingleAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : WeaponAttack() {

                    // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.SingleEnemy && target.range <= executor.character.arms().range()
                    }
                }

                data class WeaponDoubleAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : WeaponAttack() {

                    // TODO when target.range == 0 and arms().range() > 0, then return disadvantage instead of boolean
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.DoubleEnemy && target.range <= executor.character.arms().range()
                    }
                }

                data class WeaponNodeAttack(
                    override val resolution: Resolution.AttackResolution.WeaponDamageResolution,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : WeaponAttack() {
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.NodeEveryone &&
                                ((target.range == 0 && executor.character.arms().range() == 0) ||
                                        target.range > 0 && executor.character.arms().range() > 0)
                    }
                }

            }

            sealed class SpellAttack : AttackAction() {
                abstract val range: Int

                data class SpellSingleAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.SingleEnemy && target.range <= range
                    }
                }

                data class SpellDoubleAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.DoubleEnemy && target.range <= range
                    }
                }

                data class SpellNodeAttack(
                    override val resolution: Resolution.AttackResolution.SpellDamageResolution,
                    override val range: Int,
                    override val selfResolution: Resolution.SupportResolution? = null,
                    override val name: String,
                    override val movement: Movement,
                    override val resourceCost: Int,
                    override val classRestriction: List<Class>,
                ) : SpellAttack() {

                    // TODO when target.range == 0 and range > 0, then return disadvantage instead of boolean
                    override fun isValidTarget(executor: CharacterState, target: Target): Boolean {
                        return target is Target.NodeEveryone && target.range <= range
                    }
                }
            }
        }
    }

}