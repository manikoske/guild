package com.manikoske.guild.encounter

import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Movement
import com.manikoske.guild.encounter.TestingCommons.smallBattleground
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class EncounterStateTest {

    @Test
    fun viewFrom() {

        val minsc = Randomizer.characterState("Minsc").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val khalid = Randomizer.characterState("Khalid").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val jaheira = Randomizer.characterState("Jaheira").copy(positionNodeId = 1, allegiance = CharacterState.Allegiance.Attacker)
        val tazok = Randomizer.characterState("Tazok").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Defender)
        val davaeorn = Randomizer.characterState("Davaeorn").copy(positionNodeId = 3, allegiance = CharacterState.Allegiance.Defender)

        val encounterState = EncounterState(characterStates = listOf(minsc, khalid, jaheira, tazok, davaeorn))

        val minscPointOfView = encounterState.viewFrom(minsc.character.id, smallBattleground)

        val minscExpectedPointOfView = PointOfView(
            taker = minsc,
            vantageNodes = listOf(
                PointOfView.VantageNode(
                    nodeId = 1,
                    requiredNormalMovement = 1,
                    requiredSpecialMovement = 1,
                    targets = listOf(
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Ally, single = jaheira),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Ally, targets = listOf(jaheira)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Everyone, targets = listOf(jaheira)),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = tazok),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Ally, single = khalid),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Enemy, targets = listOf(tazok)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Ally, targets = listOf(khalid)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(khalid, tazok)),
                        PointOfView.Target.Self(self = minsc),
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 2,
                    requiredNormalMovement = 0,
                    requiredSpecialMovement = 0,
                    targets = listOf(
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Ally, single = jaheira),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Ally, targets = listOf(jaheira)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(jaheira)),
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Enemy, single = tazok),
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Ally, single = khalid),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Ally, targets = listOf(khalid)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Enemy, targets = listOf(tazok)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Everyone, targets = listOf(khalid, tazok)),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = davaeorn),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Enemy, targets = listOf(davaeorn)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(davaeorn)),
                        PointOfView.Target.Self(self = minsc),
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 3,
                    requiredNormalMovement = 1,
                    requiredSpecialMovement = 1,
                    targets = listOf(
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Enemy, single = davaeorn),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Enemy, targets = listOf(davaeorn)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Everyone, targets = listOf(davaeorn)),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = tazok),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Ally, single = khalid),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Enemy, targets = listOf(tazok)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Ally, targets = listOf(khalid)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(khalid, tazok)),
                        PointOfView.Target.Self(self = minsc),
                    )
                )
            )
        )
        assertThat(minscPointOfView)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(minscExpectedPointOfView)

        val davaeornPointOfView = encounterState.viewFrom(davaeorn.character.id, smallBattleground)

        val davaeornExpectedPointOfView = PointOfView(
            taker = davaeorn,
            vantageNodes = listOf(
                PointOfView.VantageNode(
                    nodeId = 1,
                    requiredNormalMovement = Int.MAX_VALUE,
                    requiredSpecialMovement = Int.MAX_VALUE,
                    targets = listOf(
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Enemy, single = jaheira),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Enemy, targets = listOf(jaheira)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Everyone, targets = listOf(jaheira)),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Ally, single = tazok),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = minsc),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = khalid),
                        PointOfView.Target.Double(range = 1, scope = PointOfView.Target.Scope.Enemy, first = minsc, second = khalid),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Ally, targets = listOf(tazok)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Enemy, targets = listOf(minsc, khalid)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(tazok, minsc, khalid)),
                        PointOfView.Target.Self(self = davaeorn),

                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 2,
                    requiredNormalMovement = 2,
                    requiredSpecialMovement= 1,
                    targets = listOf(
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = jaheira),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Enemy, targets = listOf(jaheira)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(jaheira)),
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Ally, single = tazok),
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Enemy, single = minsc),
                        PointOfView.Target.Single(range = 0, scope = PointOfView.Target.Scope.Enemy, single = khalid),
                        PointOfView.Target.Double(range = 0, scope = PointOfView.Target.Scope.Enemy, first = minsc, second = khalid),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Ally, targets = listOf(tazok)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Enemy, targets = listOf(khalid, minsc)),
                        PointOfView.Target.Node(range = 0, scope = PointOfView.Target.Scope.Everyone, targets = listOf(tazok, minsc, khalid)),
                        PointOfView.Target.Self(self = davaeorn)
                    )
                ),
                PointOfView.VantageNode(
                    nodeId = 3,
                    requiredNormalMovement = 0,
                    requiredSpecialMovement = 0,
                    targets = listOf(
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Ally, single = tazok),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = khalid),
                        PointOfView.Target.Single(range = 1, scope = PointOfView.Target.Scope.Enemy, single = minsc),
                        PointOfView.Target.Double(range = 1, scope = PointOfView.Target.Scope.Enemy, first = minsc, second = khalid),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Enemy, targets = listOf(khalid, minsc)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Ally, targets = listOf(tazok)),
                        PointOfView.Target.Node(range = 1, scope = PointOfView.Target.Scope.Everyone, targets = listOf(minsc, khalid, tazok)),
                        PointOfView.Target.Self(self = davaeorn),
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

        val jan = mockk<CharacterState>()

        val encounterState = EncounterState(characterStates = listOf(jan))

        val janPointOfView = mockk<PointOfView>()
        every { janPointOfView.taker } returns jan

        val vantageNode0 = Randomizer.randomBuilder().giveMeOne<PointOfView.VantageNode>().copy(requiredNormalMovement = 0, requiredSpecialMovement = 0)
        val vantageNode1 = Randomizer.randomBuilder().giveMeOne<PointOfView.VantageNode>().copy(requiredNormalMovement = 1, requiredSpecialMovement = 2)
        val vantageNode2 = Randomizer.randomBuilder().giveMeOne<PointOfView.VantageNode>().copy(requiredNormalMovement = 2, requiredSpecialMovement = 1)

        every { janPointOfView.vantageNodes } returns listOf(vantageNode0, vantageNode1, vantageNode2)
        every { jan.canMoveBy(any()) } returns Movement(type = Movement.Type.Normal, amount = 1)
        assertThat(encounterState.allAccessibleVantageNodes(pointOfView = janPointOfView, Randomizer.randomBuilder().giveMeOne()))
            .containsExactlyInAnyOrder(vantageNode0, vantageNode1)

        every { janPointOfView.vantageNodes } returns listOf(vantageNode0, vantageNode1, vantageNode2)
        every { jan.canMoveBy(any()) } returns Movement(type = Movement.Type.Special, amount = 1)
        assertThat(encounterState.allAccessibleVantageNodes(pointOfView = janPointOfView, Randomizer.randomBuilder().giveMeOne()))
            .containsExactlyInAnyOrder(vantageNode0, vantageNode2)

        every { janPointOfView.vantageNodes } returns listOf(vantageNode0, vantageNode1, vantageNode2)
        every { jan.canMoveBy(any()) } returns Movement(type = Movement.Type.Normal, amount = 2)
        assertThat(encounterState.allAccessibleVantageNodes(pointOfView = janPointOfView, Randomizer.randomBuilder().giveMeOne()))
            .containsExactlyInAnyOrder(vantageNode0, vantageNode1, vantageNode2)
    }

    @RepeatedTest(10)
    fun resolveEnding() {

        val gorion = Randomizer.characterState("Gorion").copy(damageTaken = 0, resourcesSpent = 0, effects = CharacterState.CharacterStates.noEffects())
        val sarevok = Randomizer.characterState("Sarevok").copy(damageTaken = 10)

        val encounterState = EncounterState(characterStates = listOf(gorion, sarevok))


        val sarevokAttacked = sarevok.takeDamage(5)
        val sarevokSlowed = sarevokAttacked.addEffect(Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Slow>())
        val gorionHealed = gorion.heal(8)

        assertThat(encounterState.resolveEnding(
            takerCharacterId = gorion.character.id,
            newPositionNodeId = 20,
            resourceCost = 2,
            updatedCharacterStates = listOf(sarevokAttacked, sarevokSlowed, gorionHealed)
        ))
            .usingRecursiveComparison()
            .isEqualTo(EncounterState(characterStates = listOf(
                gorionHealed.moveTo(20).spendResources(2),
                sarevokSlowed
            )))



    }

}