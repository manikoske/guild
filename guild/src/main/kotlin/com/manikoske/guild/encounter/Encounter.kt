package com.manikoske.guild.encounter

import com.manikoske.guild.action.Outcome
import com.manikoske.guild.character.Character

class Encounter(
    private val battleground: Battleground,
    private val attackers: Set<Character>,
    private val defenders: Set<Character>,
    private var encounterState: EncounterState
) {

    object Encounters {

        fun create(
            battleground: Battleground,
            attackersStartingNodeId: Int,
            defendersStartingNodeId: Int,
            attackers: Set<Character>,
            defenders: Set<Character>,
        ): Encounter {
            return Encounter(
                battleground = battleground,
                attackers = attackers,
                defenders = defenders,
                encounterState = EncounterState(
                    characterStates =
                        attackers.map {
                            CharacterState.CharacterStates.initialCharacterState(
                                character = it,
                                startingNodeId = attackersStartingNodeId,
                                allegiance = CharacterState.Allegiance.Attacker
                            )
                        }
                        +
                        defenders.map {
                            CharacterState.CharacterStates.initialCharacterState(
                                character = it,
                                startingNodeId = defendersStartingNodeId,
                                allegiance = CharacterState.Allegiance.Defender
                            )
                        }
                ),
            )
        }
    }

    fun simulateEncounter(
    ) {
        simulateRound()
    }

    private fun simulateRound() {
        (attackers + defenders)
            .sortedByDescending { character -> character.initiativeRoll() }
            .forEach { character -> simulateTurn(character.id) }
    }

    private fun simulateTurn(takerCharacterId: Int) {

        val takerPointOfView = encounterState.viewFrom(takerCharacterId, battleground)

        val eventualEndings: MutableList<EncounterState> = mutableListOf()

        takerPointOfView.taker.allExecutableActions().forEach { eventualAction ->

            encounterState.allAccessibleVantageNodes(takerPointOfView, eventualAction.movement)
                .forEach { eventualVantageNode ->
                    eventualEndings.addAll(
                        eventualVantageNode.targets
                            .map { eventualAction.outcome.resolve(takerPointOfView.taker, it) }
                            .filterIsInstance<Outcome.OutcomeResult.AppliedToTarget>()
                            .map {
                                encounterState.resolveEnding(
                                    takerCharacterId = takerPointOfView.taker.character.id,
                                    newPositionNodeId = eventualVantageNode.nodeId,
                                    resourceCost = eventualAction.resourceCost,
                                    updatedCharacterStates = it.updatedCharacterStates
                                )
                            }
                    )
                }
        }
        this.encounterState = eventualEndings.maxBy { it.utility() }
    }
}
