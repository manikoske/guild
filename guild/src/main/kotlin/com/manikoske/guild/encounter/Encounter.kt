package com.manikoske.guild.encounter

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
                    characterStates = attackers.associateBy(
                        { it.id },
                        {
                            initializeCharacterState(
                                character = it,
                                startingNodeId = attackersStartingNodeId,
                                allegiance = CharacterState.Allegiance.Attacker
                            )
                        }
                    ) + defenders.associateBy(
                        { it.id },
                        {
                            initializeCharacterState(
                                character = it,
                                startingNodeId = defendersStartingNodeId,
                                allegiance = CharacterState.Allegiance.Defender
                            )
                        }
                    ),
                ),
            )
        }

        private fun initializeCharacterState(
            character: Character,
            startingNodeId: Int,
            allegiance: CharacterState.Allegiance
        ): CharacterState {
            return CharacterState(
                character = character,
                positionNodeId = startingNodeId,
                allegiance = allegiance,
                damageTaken = 0,
                resourcesSpent = 0,
                statuses = listOf()
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

        val eventualActions = encounterState.allEventualActions(takerPointOfView)

        val eventualEndings: MutableList<EncounterState> = mutableListOf()

        eventualActions.forEach { eventualAction ->

            encounterState.allAccessibleVantageNodes(takerPointOfView, eventualAction.movement).forEach {
                eventualVantageNode ->
                encounterState.eventualActionTargets(takerPointOfView, eventualVantageNode, eventualAction).forEach { eventualActionTargets ->
                    eventualEndings.add(
                        encounterState.resolveEnding(
                            executorCharacterId = takerCharacterId,
                            newPositionNodeId = eventualVantageNode.nodeId,
                            action = eventualAction,
                            actionTargets = eventualActionTargets
                        )
                    )
                }
            }
        }
        this.encounterState = eventualEndings.maxBy { it.utility() }
    }
}
