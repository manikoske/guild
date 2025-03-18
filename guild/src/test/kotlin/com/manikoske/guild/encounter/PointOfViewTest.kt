package com.manikoske.guild.encounter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class PointOfViewTest {

    @RepeatedTest(100)
    fun testAllVantageNodes() {

        val minsc = Randomizer.characterState("Minsc").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val khalid = Randomizer.characterState("Khalid").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Attacker)
        val jaheira = Randomizer.characterState("Jaheira").copy(positionNodeId = 1, allegiance = CharacterState.Allegiance.Attacker)
        val tazok = Randomizer.characterState("Tazok").copy(positionNodeId = 2, allegiance = CharacterState.Allegiance.Defender)
        val davaeorn = Randomizer.characterState("Davaeorn").copy(positionNodeId = 3, allegiance = CharacterState.Allegiance.Defender)

        val encounterState = EncounterState(characterStates = listOf(minsc, khalid, jaheira, tazok, davaeorn))

        val minscPointOfView = encounterState.viewFrom(minsc.character.id)

        assertThat(minscPointOfView)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(PointOfView(taker = minsc, allies = listOf(khalid, jaheira), enemies = listOf(tazok, davaeorn)))

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
                            Target.Single(range = 0, scope = Target.Scope.Ally, single = jaheira),
                            Target.Node(range = 0, scope = Target.Scope.Ally, targets = listOf(jaheira)),
                            Target.Everyone(range = 0, allies = listOf(jaheira), enemies = listOf()),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = tazok),
                            Target.Single(range = 1, scope = Target.Scope.Ally, single = khalid),
                            Target.Node(range = 1, scope = Target.Scope.Enemy, targets = listOf(tazok)),
                            Target.Node(range = 1, scope = Target.Scope.Ally, targets = listOf(khalid)),
                            Target.Everyone(range = 1, allies = listOf(khalid), enemies = listOf(tazok)),
                            Target.Self(self = minsc),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 2,
                        requiredNormalMovement = 0,
                        requiredSpecialMovement = 0,
                        targets = listOf(
                            Target.Single(range = 1, scope = Target.Scope.Ally, single = jaheira),
                            Target.Node(range = 1, scope = Target.Scope.Ally, targets = listOf(jaheira)),
                            Target.Everyone(range = 1, allies = listOf(jaheira), enemies = listOf()),
                            Target.Single(range = 0, scope = Target.Scope.Enemy, single = tazok),
                            Target.Single(range = 0, scope = Target.Scope.Ally, single = khalid),
                            Target.Node(range = 0, scope = Target.Scope.Ally, targets = listOf(khalid)),
                            Target.Node(range = 0, scope = Target.Scope.Enemy, targets = listOf(tazok)),
                            Target.Everyone(range = 0, allies = listOf(khalid), enemies = listOf(tazok)),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = davaeorn),
                            Target.Node(range = 1, scope = Target.Scope.Enemy, targets = listOf(davaeorn)),
                            Target.Everyone(range = 1, allies = listOf(), enemies = listOf(davaeorn)),
                            Target.Self(self = minsc),
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 3,
                        requiredNormalMovement = 1,
                        requiredSpecialMovement = 1,
                        targets = listOf(
                            Target.Single(range = 0, scope = Target.Scope.Enemy, single = davaeorn),
                            Target.Node(range = 0, scope = Target.Scope.Enemy, targets = listOf(davaeorn)),
                            Target.Everyone(range = 0, allies = listOf(), enemies = listOf(davaeorn)),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = tazok),
                            Target.Single(range = 1, scope = Target.Scope.Ally, single = khalid),
                            Target.Node(range = 1, scope = Target.Scope.Enemy, targets = listOf(tazok)),
                            Target.Node(range = 1, scope = Target.Scope.Ally, targets = listOf(khalid)),
                            Target.Everyone(range = 1, allies = listOf(khalid), enemies = listOf(tazok)),
                            Target.Self(self = minsc),
                        )
                    )
                )
            )


        val davaeornPointOfView = encounterState.viewFrom(davaeorn.character.id)

        assertThat(davaeornPointOfView)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(PointOfView(taker = davaeorn, allies = listOf(tazok), enemies = listOf(minsc, jaheira, khalid)))

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
                            Target.Single(range = 0, scope = Target.Scope.Enemy, single = jaheira),
                            Target.Node(range = 0, scope = Target.Scope.Enemy, targets = listOf(jaheira)),
                            Target.Everyone(range = 0, allies = listOf(), enemies = listOf(jaheira)),
                            Target.Single(range = 1, scope = Target.Scope.Ally, single = tazok),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = minsc),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = khalid),
                            Target.Double(range = 1, scope = Target.Scope.Enemy, first = minsc, second = khalid),
                            Target.Node(range = 1, scope = Target.Scope.Ally, targets = listOf(tazok)),
                            Target.Node(range = 1, scope = Target.Scope.Enemy, targets = listOf(minsc, khalid)),
                            Target.Everyone(range = 1, allies = listOf(tazok), enemies = listOf(minsc, khalid)),
                            Target.Self(self = davaeorn),

                            )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 2,
                        requiredNormalMovement = 2,
                        requiredSpecialMovement= 1,
                        targets = listOf(
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = jaheira),
                            Target.Node(range = 1, scope = Target.Scope.Enemy, targets = listOf(jaheira)),
                            Target.Everyone(range = 1, allies = listOf(), enemies = listOf(jaheira)),
                            Target.Single(range = 0, scope = Target.Scope.Ally, single = tazok),
                            Target.Single(range = 0, scope = Target.Scope.Enemy, single = minsc),
                            Target.Single(range = 0, scope = Target.Scope.Enemy, single = khalid),
                            Target.Double(range = 0, scope = Target.Scope.Enemy, first = minsc, second = khalid),
                            Target.Node(range = 0, scope = Target.Scope.Ally, targets = listOf(tazok)),
                            Target.Node(range = 0, scope = Target.Scope.Enemy, targets = listOf(khalid, minsc)),
                            Target.Everyone(range = 0, allies = listOf(tazok), enemies = listOf(minsc, khalid)),
                            Target.Self(self = davaeorn)
                        )
                    ),
                    PointOfView.VantageNode(
                        nodeId = 3,
                        requiredNormalMovement = 0,
                        requiredSpecialMovement = 0,
                        targets = listOf(
                            Target.Single(range = 1, scope = Target.Scope.Ally, single = tazok),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = khalid),
                            Target.Single(range = 1, scope = Target.Scope.Enemy, single = minsc),
                            Target.Double(range = 1, scope = Target.Scope.Enemy, first = minsc, second = khalid),
                            Target.Node(range = 1, scope = Target.Scope.Enemy, targets = listOf(khalid, minsc)),
                            Target.Node(range = 1, scope = Target.Scope.Ally, targets = listOf(tazok)),
                            Target.Everyone(range = 1, allies = listOf(tazok), enemies = listOf(minsc, khalid)),
                            Target.Self(self = davaeorn),
                        )
                    )
                )
            )

    }

}