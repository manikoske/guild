package com.manikoske.guild.action

import com.manikoske.guild.action.Action.Actions.noClassRestriction
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Class
import com.manikoske.guild.rules.Die
import com.manikoske.guild.rules.Rollable

sealed interface Action {

    object Actions {

        val noClassRestriction = listOf(Class.Fighter, Class.Rogue, Class.Ranger, Class.Cleric, Class.Wizard)

        val basicActions = listOf(
            BasicAction(
                name = "Basic Attack",
                movement = Movement(type = Movement.Type.Normal, amount = 1),
                outcome = Outcome.AttackOutcome.WeaponSingleAttack(
                    resolution = Resolution.WeaponDamageResolution(
                        attackRollBonusModifier = 0,
                        damageRollMultiplier = 1
                    ),
                )
            ),
            BasicAction(
                name = "Cantrip",
                movement = Movement(type = Movement.Type.Normal, amount = 1),
                outcome = Outcome.AttackOutcome.SpellSingleAttack(
                    resolution = Resolution.SpellResolution.SpellDamageResolution(
                        baseDifficultyClass = 10,
                        executorAttributeType = Attribute.Type.intelligence,
                        targetAttributeType = Attribute.Type.dexterity,
                        damage =  Rollable.Damage( roll = { Die.d6.roll(1) } )
                    ),
                    range = 1
                )
            ),
            BasicAction(
                name = "Disengage",
                movement = Movement(type = Movement.Type.Special, amount = 1),
                outcome = Outcome.OutcomeWithNoResolution
            ),
            BasicAction(
                name = "Dash",
                movement = Movement(type = Movement.Type.Normal, amount = 2),
                outcome = Outcome.OutcomeWithNoResolution
            ),
        )
    }

    val name: String
    val movement: Movement
    val resourceCost: Int
    val classRestriction: List<Class>
    val outcome: Outcome

    data class BasicAction(
        override val name: String,
        override val movement: Movement,
        override val resourceCost: Int = 0,
        override val classRestriction: List<Class> = noClassRestriction,
        override val outcome: Outcome
    ) : Action

    sealed class ForcedAction : Action {
        override val movement: Movement
            get() = Movement(type = Movement.Type.Normal, amount = 0)
        override val resourceCost: Int
            get() = 0
        override val classRestriction: List<Class>
            get() = noClassRestriction

        override val outcome: Outcome
            get() = Outcome.OutcomeWithNoResolution

        data object NoAction : ForcedAction() {
            override val name: String
                get() = "No Action"

        }

        data object StandUp : ForcedAction() {
            override val name: String
                get() = "Stand Up"

        }

        data object FightForLife : ForcedAction() {
            override val name: String
                get() = "Fight For Life"

        }
    }

}