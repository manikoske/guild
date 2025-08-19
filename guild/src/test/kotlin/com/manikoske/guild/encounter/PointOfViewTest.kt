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

        val minsc = Fixture.characterState("Minsc").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val khalid = Fixture.characterState("Khalid").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val jaheira = Fixture.characterState("Jaheira").copy(positionNodeId = 1, allegiance = CharacterState.Allegiance.Attacker)
        val tazok = Fixture.characterState("Tazok").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Defender)
        val davaeorn = Fixture.characterState("Davaeorn").copy(positionNodeId = 3, allegiance = CharacterState.Allegiance.Defender)


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
                            Target(type = Target.Type.SingleAlly,range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeAlly, range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeEveryone, range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.SingleAlly, range = 1, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.NodeEnemy, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeAlly, range = 1, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(khalid, tazok)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 2,
                        requiredNormalMovement = 0,
                        requiredSpecialMovement = 0,
                        targets = listOf(
                            Target(type = Target.Type.SingleAlly, range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeAlly, range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.SingleEnemy, range = 0, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.SingleAlly, range = 0, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.NodeAlly, range = 0, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.NodeEnemy, range = 0, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEveryone, range = 0, targetedCharacterStates = listOf(khalid, tazok)),
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(davaeorn)),
                            Target(type = Target.Type.NodeEnemy, range = 1, targetedCharacterStates = listOf(davaeorn)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(davaeorn)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 3,
                        requiredNormalMovement = 1,
                        requiredSpecialMovement = 1,
                        targets = listOf(
                            Target(type = Target.Type.SingleEnemy, range = 0, targetedCharacterStates = listOf(davaeorn)),
                            Target(type = Target.Type.NodeEnemy, range = 0, targetedCharacterStates = listOf(davaeorn)),
                            Target(type = Target.Type.NodeEveryone, range = 0, targetedCharacterStates = listOf(davaeorn)),
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.SingleAlly, range = 1, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.NodeAlly, range = 1, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.NodeEnemy, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(khalid, tazok)),
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
                        requiredNormalMovement = 3, // TODO implemenovat prekazanie cez efekt
                        requiredSpecialMovement = Int.MAX_VALUE,
                        targets = listOf(
                            Target(type = Target.Type.SingleEnemy, range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeEnemy, range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeEveryone, range = 0, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.SingleAlly, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(minsc)),
                            Target(type = Target.Type.NodeAlly, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEnemy, range = 1, targetedCharacterStates = listOf(khalid, minsc)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(tazok, khalid, minsc)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 2,
                        requiredNormalMovement = 2,
                        requiredSpecialMovement= 1,
                        targets = listOf(
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeEnemy, range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(jaheira)),
                            Target(type = Target.Type.SingleEnemy, range = 0, targetedCharacterStates = listOf(minsc)),
                            Target(type = Target.Type.SingleEnemy, range = 0, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.SingleAlly, range = 0, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEnemy, range = 0, targetedCharacterStates = listOf(minsc, khalid)),
                            Target(type = Target.Type.NodeAlly, range = 0, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEveryone, range = 0, targetedCharacterStates = listOf(minsc, khalid, tazok)),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 3,
                        requiredNormalMovement = 0,
                        requiredSpecialMovement = 0,
                        targets = listOf(
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(minsc)),
                            Target(type = Target.Type.SingleEnemy, range = 1, targetedCharacterStates = listOf(khalid)),
                            Target(type = Target.Type.SingleAlly, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEnemy, range = 1, targetedCharacterStates = listOf(minsc, khalid)),
                            Target(type = Target.Type.NodeAlly, range = 1, targetedCharacterStates = listOf(tazok)),
                            Target(type = Target.Type.NodeEveryone, range = 1, targetedCharacterStates = listOf(minsc, khalid, tazok)),
                        )
                    )
                )
            )
    }

    @Test
    fun testUpdateWith() {
        val minsc = Fixture.characterState("Minsc")
        val khalid = Fixture.characterState("Khalid")
        val jaheira = Fixture.characterState("Jaheira")
        val tazok = Fixture.characterState("Tazok")
        val davaeorn = Fixture.characterState("Davaeorn")


        val minscPointOfView = PointOfView(taker = minsc, others = listOf(khalid, jaheira, tazok, davaeorn))

        val minscEndAction = Rules.endAction(minsc)
        val khalidTargetEvent = Rules.boostResources(target = khalid, amount = 1)
        val jaheiraTargetEvent = Rules.boostResources(target = jaheira, amount = 2)
        val tazokTargetEvent = Rules.weaponAttackBy(executor = minsc, target = tazok, 0, 1, null)
        val davaeornTargetEvent = Rules.weaponAttackBy(executor = minsc, target = davaeorn, 0, 1, null)

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