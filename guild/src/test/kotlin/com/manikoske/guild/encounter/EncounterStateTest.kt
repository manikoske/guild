package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement
import com.manikoske.guild.character.Character
import com.manikoske.guild.encounter.TestingCommons.randomBuilder
import com.manikoske.guild.encounter.TestingCommons.smallBattleground
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EncounterStateTest {

    @Test
    fun resolveEnding() {
    }

    @Test
    fun eventualActionTargets() {
    }

    @Test
    fun viewFrom() {
        
        val minsc = randomBuilder.giveMeOne<Character>()
        val jaheira = randomBuilder.giveMeOne<Character>()
        val tazok = randomBuilder.giveMeOne<Character>()
        val davaeorn = randomBuilder.giveMeOne<Character>()

        val encounterState = EncounterState(
            characterStates = mapOf(
                minsc.id to randomBuilder.giveMeOne<CharacterState>().copy(character = minsc, positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker),
                jaheira.id to randomBuilder.giveMeOne<CharacterState>().copy(character = jaheira, positionNodeId = 1, allegiance = CharacterState.Allegiance.Attacker),
                tazok.id to randomBuilder.giveMeOne<CharacterState>().copy(character = tazok, positionNodeId = 2, allegiance = CharacterState.Allegiance.Defender),
                davaeorn.id to randomBuilder.giveMeOne<CharacterState>().copy(character = davaeorn, positionNodeId = 3, allegiance = CharacterState.Allegiance.Defender),
            )
        )

        val minscPointOfView = encounterState.viewFrom(minsc.id, smallBattleground)

        val minscExpectedPointOfView = PointOfView(
            self = minsc.id,
            enemies = listOf(tazok.id, davaeorn.id),
            allies = listOf(minsc.id, jaheira.id),
            everyone = listOf(minsc.id, jaheira.id, tazok.id, davaeorn.id),
            everyoneElse = listOf(jaheira.id, tazok.id, davaeorn.id),
            vantageNodes = listOf(
                PointOfView.VantageNode(
                    nodeId = 1,
                    hasEnemiesPresent = false,
                    requiredNormalMovement = Int.MAX_VALUE,
                    requiredSpecialMovement = 1,
                    targetNodes = listOf(
                        PointOfView.TargetNode(nodeId = 1, characterIds = listOf(jaheira.id), range = 0),
                        PointOfView.TargetNode(nodeId = 2, characterIds = listOf(minsc.id, tazok.id), range = 1),
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 2,
                    hasEnemiesPresent = true,
                    requiredNormalMovement = 0,
                    requiredSpecialMovement = 0,
                    targetNodes = listOf(
                        PointOfView.TargetNode(nodeId = 1, characterIds = listOf(jaheira.id), range = 1),
                        PointOfView.TargetNode(nodeId = 2, characterIds = listOf(minsc.id, tazok.id), range = 0),
                        PointOfView.TargetNode(nodeId = 3, characterIds = listOf(davaeorn.id), range = 1),
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 3,
                    hasEnemiesPresent = true,
                    requiredNormalMovement = Int.MAX_VALUE,
                    requiredSpecialMovement = 1,
                    targetNodes = listOf(
                        PointOfView.TargetNode(nodeId = 2, characterIds = listOf(minsc.id, tazok.id), range = 1),
                        PointOfView.TargetNode(nodeId = 3, characterIds = listOf(davaeorn.id), range = 0),
                    )
                )
            )
        )
        assertThat(minscPointOfView)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(minscExpectedPointOfView)

        val davaeornPointOfView = encounterState.viewFrom(davaeorn.id, smallBattleground)

        val davaeornExpectedPointOfView = PointOfView(
            self = davaeorn.id,
            enemies = listOf(minsc.id, jaheira.id),
            allies = listOf(tazok.id, davaeorn.id),
            everyone = listOf(minsc.id, jaheira.id, tazok.id, davaeorn.id),
            everyoneElse = listOf(minsc.id, jaheira.id, tazok.id),
            vantageNodes = listOf(
                PointOfView.VantageNode(
                    nodeId = 1,
                    hasEnemiesPresent = true,
                    requiredNormalMovement = Int.MAX_VALUE,
                    requiredSpecialMovement = Int.MAX_VALUE,
                    targetNodes = listOf(
                        PointOfView.TargetNode(nodeId = 1, characterIds = listOf(jaheira.id), range = 0),
                        PointOfView.TargetNode(nodeId = 2, characterIds = listOf(minsc.id, tazok.id), range = 1),
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 2,
                    hasEnemiesPresent = true,
                    requiredNormalMovement = 2,
                    requiredSpecialMovement= 1,
                    targetNodes = listOf(
                        PointOfView.TargetNode(nodeId = 1, characterIds = listOf(jaheira.id), range = 1),
                        PointOfView.TargetNode(nodeId = 2, characterIds = listOf(minsc.id, tazok.id), range = 0),
                        PointOfView.TargetNode(nodeId = 3, characterIds = listOf(davaeorn.id), range = 1),
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 3,
                    hasEnemiesPresent = false,
                    requiredNormalMovement = 0,
                    requiredSpecialMovement = 0,
                    targetNodes = listOf(
                        PointOfView.TargetNode(nodeId = 2, characterIds = listOf(minsc.id, tazok.id), range = 1),
                        PointOfView.TargetNode(nodeId = 3, characterIds = listOf(davaeorn.id), range = 0),
                    )
                )
            )
        )
        assertThat(davaeornPointOfView)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(davaeornExpectedPointOfView)


    }

    @Test
    fun allAccessibleVantageNodes() {

        val jan = randomBuilder.giveMeOne<Character>()
        val janCharacterState = mockk<CharacterState>()

        val encounterState = EncounterState(characterStates = mapOf(jan.id to janCharacterState))

        val janPointOfView = mockk<PointOfView>()
        every { janPointOfView.self } returns jan.id


        val vantageNode0 = randomBuilder.giveMeOne<PointOfView.VantageNode>().copy(requiredNormalMovement = 0, requiredSpecialMovement = 0)
        val vantageNode1 = randomBuilder.giveMeOne<PointOfView.VantageNode>().copy(requiredNormalMovement = 1, requiredSpecialMovement = 2)
        val vantageNode2 = randomBuilder.giveMeOne<PointOfView.VantageNode>().copy(requiredNormalMovement = 2, requiredSpecialMovement = 1)

        every { janPointOfView.vantageNodes } returns listOf(vantageNode0, vantageNode1, vantageNode2)
        every { janCharacterState.canMoveBy(any()) } returns Movement(type = Movement.Type.Normal, amount = 1)
        assertThat(encounterState.allAccessibleVantageNodes(pointOfView = janPointOfView, randomBuilder.giveMeOne()))
            .containsExactlyInAnyOrder(vantageNode0, vantageNode1)

        every { janPointOfView.vantageNodes } returns listOf(vantageNode0, vantageNode1, vantageNode2)
        every { janCharacterState.canMoveBy(any()) } returns Movement(type = Movement.Type.Special, amount = 1)
        assertThat(encounterState.allAccessibleVantageNodes(pointOfView = janPointOfView, randomBuilder.giveMeOne()))
            .containsExactlyInAnyOrder(vantageNode0, vantageNode2)

        every { janPointOfView.vantageNodes } returns listOf(vantageNode0, vantageNode1, vantageNode2)
        every { janCharacterState.canMoveBy(any()) } returns Movement(type = Movement.Type.Normal, amount = 2)
        assertThat(encounterState.allAccessibleVantageNodes(pointOfView = janPointOfView, randomBuilder.giveMeOne()))
            .containsExactlyInAnyOrder(vantageNode0, vantageNode1, vantageNode2)
    }

    @Test
    fun allEventualActions() {
        // too simple to test
    }

    @Test
    fun utility() {
    }
}