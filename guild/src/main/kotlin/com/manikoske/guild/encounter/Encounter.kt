package com.manikoske.guild.encounter

import com.manikoske.guild.action.*
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

        // TODO dako premistnit view do encounterState aby bol raz inicializovany self a PoW spravit ako suchotinu len strukturu
        val takerPointOfView = encounterState.viewFrom(takerCharacterId, battleground)

        // TODO encounterState.allPossibleActions(takerPointOfView)
        val possibleActions = Action.Actions.actions.filter { action -> takerPointOfView.self.canExecuteAction(action) }

        val possibleEndings: MutableList<EncounterState> = mutableListOf()

        possibleActions.forEach { possibleAction ->

            // TODO encounterState.allAccessibleVantageNodes(takerPointOfView, possibleAction.movement)
            encounterState.allAccessibleVantageNodes(possibleAction.movement).forEach {
                eventualVantageNode ->

                // TODO encounterState.possibleActionTargets(takerPointOfView, eventualVantageNode, possibleAction)
                val possibleTargets = takerPointOfView.allAccessibleVantageNodes(
                    vantageNode = eventualVantageNode,
                    targetType = possibleAction.targetType(takerPointOfView.self.character)
                )

                possibleTargets.forEach { targets ->
                    possibleEndings.add(
                        encounterState.resolveEnding(
                            executorCharacterId = takerCharacterId,
                            newPositionNodeId = eventualVantageNode.nodeId,
                            action = possibleAction,
                            targets = targets
                        )
                    )
                }
            }
        }
        this.encounterState = possibleEndings.sortedByDescending { it.utility() }.take(3).random()
    }
}
