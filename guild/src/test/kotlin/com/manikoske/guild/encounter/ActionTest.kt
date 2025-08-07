package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action.TargetedAction
import com.manikoske.guild.action.Resolution
import com.manikoske.guild.action.Target
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Effect
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Die
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.junit.jupiter.api.Disabled
import kotlin.test.Test

class ActionTest {

    @Disabled()
    @Test
    fun testTargetedAction() {

        val actionName = "Test Action"
        val newPositionNodeId = 10

        val kivan = Randomizer.characterState("Kivan")
        val imoen = Randomizer.characterState("Imoen")
        val edwin = Randomizer.characterState("Edwin")

        val target = Randomizer.randomBuilder()
            .giveMeOne<Target.NodeEveryone>().copy(targetedCharacterStates = listOf(kivan, imoen))

        val action = Randomizer.randomBuilder()
            .giveMeOne<TargetedAction.AttackAction.SpellAttack.SpellNodeAttack>()
            .copy(
                name = actionName,
                resolution = Resolution.AttackResolution.SpellDamageResolution(
                    baseDifficultyClass = 10,
                    executorAttributeType = Attribute.Type.intelligence,
                    targetAttributeType = Attribute.Type.dexterity,
                    damage =  Dice.of(Die.d10),
                    statusOnHit = listOf(
                        Effect.ActionForcingEffect.Stun(roundsLeft = 2)
                    )
                ),
                selfResolution = Resolution.SupportResolution.AddStatus(
                    status = listOf(Effect.MovementAlteringEffect.Haste(roundsLeft = 1))
                ),
                resourceCost = 1
            )

//        assertThat(action.execute(
//            executor = edwin,
//            target = target,
//            newPositionNodeId = newPositionNodeId
//        )).isEqualTo(
//            TargetedActionOutcome(
//                actionStarted = Event.ActionStarted(
//                    actionName = actionName,
//                    target = edwin,
//                    updatedTarget = ,
//                    newPositionNodeId = newPositionNodeId,
//                    resourcesSpent = 1
//                )
//            )
//        )
    }

}