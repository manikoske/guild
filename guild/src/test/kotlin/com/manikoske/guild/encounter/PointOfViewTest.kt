package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Target
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.rules.Rules
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class PointOfViewTest {

    @Test
    fun testAllVantageNodes() {

        val minsc = Randomizer.characterState("Minsc").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val khalid = Randomizer.characterState("Khalid").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val jaheira = Randomizer.characterState("Jaheira").copy(positionNodeId = 1, allegiance = CharacterState.Allegiance.Attacker)
        val tazok = Randomizer.characterState("Tazok").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Defender)
        val davaeorn = Randomizer.characterState("Davaeorn").copy(positionNodeId = 3, allegiance = CharacterState.Allegiance.Defender)


        val minscPointOfView = PointOfView(taker = minsc, others = listOf(khalid, jaheira, tazok, davaeorn))

        assertThat(minscPointOfView.allVantageNodes(TestingCommons.smallBattleground))
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(
                listOf(
                    PointOfView.VantageNode(
                        nodeId = 1,
                        requiredNormalMovement = 1,
                        requiredSpecialMovement = 1,
                        targets = listOf(
                            Target.SingleAlly(range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeAlly(range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeEveryone(range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.SingleAlly(range = 1, targetedCharacterStates = listOf(khalid)),
                            Target.NodeEnemy(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.NodeAlly(range = 1, targetedCharacterStates = listOf(khalid)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(khalid, tazok)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 2,
                        requiredNormalMovement = 0,
                        requiredSpecialMovement = 0,
                        targets = listOf(
                            Target.SingleAlly(range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeAlly(range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target.SingleEnemy(range = 0, targetedCharacterStates = listOf(tazok)),
                            Target.SingleAlly(range = 0, targetedCharacterStates = listOf(khalid)),
                            Target.NodeAlly(range = 0, targetedCharacterStates = listOf(khalid)),
                            Target.NodeEnemy(range = 0, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEveryone(range = 0, targetedCharacterStates = listOf(khalid, tazok)),
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(davaeorn)),
                            Target.NodeEnemy(range = 1, targetedCharacterStates = listOf(davaeorn)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(davaeorn)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 3,
                        requiredNormalMovement = 1,
                        requiredSpecialMovement = 1,
                        targets = listOf(
                            Target.SingleEnemy(range = 0, targetedCharacterStates = listOf(davaeorn)),
                            Target.NodeEnemy(range = 0, targetedCharacterStates = listOf(davaeorn)),
                            Target.NodeEveryone(range = 0, targetedCharacterStates = listOf(davaeorn)),
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.SingleAlly(range = 1, targetedCharacterStates = listOf(khalid)),
                            Target.NodeAlly(range = 1, targetedCharacterStates = listOf(khalid)),
                            Target.NodeEnemy(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(khalid, tazok)),
                        )
                    )
                )
            )


        val davaeornPointOfView = PointOfView(taker = davaeorn, others = listOf(tazok, khalid, jaheira, minsc))

        assertThat(davaeornPointOfView.allVantageNodes(TestingCommons.smallBattleground))
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(
                listOf(
                    PointOfView.VantageNode(
                        nodeId = 1,
                        requiredNormalMovement = Int.MAX_VALUE,
                        requiredSpecialMovement = Int.MAX_VALUE,
                        targets = listOf(
                            Target.SingleEnemy(range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeEnemy(range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeEveryone(range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target.SingleAlly(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(khalid)),
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(minsc)),
                            Target.DoubleEnemy(range = 1, targetedCharacterStates = listOf(khalid, minsc)),
                            Target.NodeAlly(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEnemy(range = 1, targetedCharacterStates = listOf(khalid, minsc)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(khalid, tazok, minsc)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 2,
                        requiredNormalMovement = 2,
                        requiredSpecialMovement= 1,
                        targets = listOf(
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeEnemy(range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target.SingleEnemy(range = 0, targetedCharacterStates = listOf(minsc)),
                            Target.SingleEnemy(range = 0, targetedCharacterStates = listOf(khalid)),
                            Target.SingleAlly(range = 0, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEnemy(range = 0, targetedCharacterStates = listOf(minsc, khalid)),
                            Target.DoubleEnemy(range = 0, targetedCharacterStates = listOf(minsc, khalid)),
                            Target.NodeAlly(range = 0, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEveryone(range = 0, targetedCharacterStates = listOf(minsc, khalid, tazok)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 3,
                        requiredNormalMovement = 0,
                        requiredSpecialMovement = 0,
                        targets = listOf(
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(minsc)),
                            Target.SingleEnemy(range = 1, targetedCharacterStates = listOf(khalid)),
                            Target.SingleAlly(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEnemy(range = 1, targetedCharacterStates = listOf(minsc, khalid)),
                            Target.DoubleEnemy(range = 1, targetedCharacterStates = listOf(minsc, khalid)),
                            Target.NodeAlly(range = 1, targetedCharacterStates = listOf(tazok)),
                            Target.NodeEveryone(range = 1, targetedCharacterStates = listOf(minsc, khalid, tazok)),
                        )
                    )
                )
            )
    }

    @Test
    fun testUpdateWith() {
        val minsc = Randomizer.characterState("Minsc")
        val khalid = Randomizer.characterState("Khalid")
        val jaheira = Randomizer.characterState("Jaheira")
        val tazok = Randomizer.characterState("Tazok")
        val davaeorn = Randomizer.characterState("Davaeorn")


        val minscPointOfView = PointOfView(taker = minsc, others = listOf(khalid, jaheira, tazok, davaeorn))

        val minscEndAction = Rules.endAction(minsc)
        val khalidTargetEvent = Rules.boostResources(target = khalid, amount = 1)
        val jaheiraTargetEvent = Rules.boostResources(target = jaheira, amount = 2)
        val tazokTargetEvent = Rules.weaponAttackBy(executor = minsc, target = tazok, 0, 1, listOf())
        val davaeornTargetEvent = Rules.weaponAttackBy(executor = minsc, target = davaeorn, 0, 1, listOf())

        val targetedActionOutcome = mockk<Action.TargetedActionOutcome>()
        every { targetedActionOutcome.actionEnded } returns minscEndAction
        every { targetedActionOutcome.targetEvents } returns listOf(khalidTargetEvent, jaheiraTargetEvent, tazokTargetEvent, davaeornTargetEvent)


        assertThat(minscPointOfView.updateWith(targetedActionOutcome).characterStates())
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(listOf(
                minscEndAction.updatedTarget,
                khalidTargetEvent.updatedTarget,
                jaheiraTargetEvent.updatedTarget,
                tazokTargetEvent.updatedTarget,
                davaeornTargetEvent.updatedTarget
            ))

    }

}