package com.manikoske.guild.encounter

import com.manikoske.guild.ability.*
import com.manikoske.guild.character.Character
import kotlin.math.max

class Encounter(
    val battleground: Battleground,
    val attackers: Set<Character>,
    val defenders: Set<Character>,
    var encounterContext: EncounterContext
) {

    fun simulateEncounter(
    ) {
        simulateRound()
    }

    private fun simulateRound() {
        (attackers + defenders)
            .sortedBy { character -> character.initiativeRoll() }
            .forEach { character -> simulateTurn(character) }
    }

    private fun simulateTurn(currentCharacter: Character) {

        val currentCharacterContext = encounterContext.characterContext(currentCharacter)

        val executableAbilities = Ability.Abilities.abilities
            .filter { ability -> currentCharacterContext.canExecuteAbility(ability) }

        executableAbilities.forEach { executableAbility ->

            val possibleMovementNodes = battleground.getAccessibleNodes(
                startNode = currentCharacterContext.position,
                abilityMovement = executableAbility.movement,
                pointOfView = encounterContext.pointOfView(currentCharacter)
            )

            possibleMovementNodes.forEach { possibleMovementNode ->
                val possibleTargets = encounterContext.resolveTargets(
                    battleground = battleground,
                    executorCharacterContext = currentCharacterContext,
                    executorPosition = possibleMovementNode,
                    ability = executableAbility
                )

                possibleTargets.forEach { possibleTarget ->
                    val possibleEncounterContext = encounterContext.resolveOutcomes(
                        battleground = battleground,
                        executorCharacterContext = currentCharacterContext,
                        executorPosition = possibleMovementNode,
                        ability = executableAbility,
                        targets = possibleTarget
                    )
                }
            }


        }


    }


    data class EncounterContext(
        private val attackerContexts: Set<CharacterContext>,
        private val defenderContexts: Set<CharacterContext>,
    ) {
        private val attackerPointOfView: PointOfView by lazy {
            PointOfView(
                friendsAtNodes = charactersAtNodes(attackerContexts),
                foesAtNodes = charactersAtNodes(defenderContexts),
            )
        }

        private val defenderPointOfView: PointOfView by lazy {
            PointOfView(
                friendsAtNodes = charactersAtNodes(defenderContexts),
                foesAtNodes = charactersAtNodes(attackerContexts),
            )
        }

        fun characterContext(character: Character): CharacterContext {
            return (attackerContexts + defenderContexts).first { characterContext -> characterContext.character.id == character.id }
        }

        fun pointOfView(character: Character): PointOfView {
            return if (attackerContexts.any { attackerContext -> attackerContext.character.id == character.id }) {
                attackerPointOfView
            } else {
                defenderPointOfView
            }
        }


        private fun charactersAtNodes(characterContexts: Set<CharacterContext>): Map<Battleground.Node, List<CharacterContext>> {
            return characterContexts.groupBy { it.position }
        }


        fun utility(): Int {
            return 1
        }

        fun resolveTargets(
            battleground: Battleground,
            executorCharacterContext: CharacterContext,
            executorPosition: Battleground.Node,
            ability: Ability.ExecutableAbility
        ): List<List<CharacterContext>> {

            val pointOfView: PointOfView = pointOfView(executorCharacterContext.character)
            val targetType = ability.targetType(executorCharacterContext.character)
            val result: MutableList<List<CharacterContext>> = mutableListOf()
            val possibleTargetNodes = battleground.nodesInRange(executorPosition, targetType.range)

            for (possibleTargetNode in possibleTargetNodes) {

                val targets =
                    if (ability.isHarmful()) pointOfView.foesAt(possibleTargetNode)
                    else pointOfView.friendsAt(possibleTargetNode)

                when (targetType.arity) {
                    TargetType.Arity.Self -> result.add(listOf(executorCharacterContext))
                    TargetType.Arity.Area -> result.add(pointOfView.everyoneAt(possibleTargetNode))
                    TargetType.Arity.Single -> result.addAll(targets.chunked(1))
                    TargetType.Arity.Double -> result.addAll(doubleTargets(targets))
                    TargetType.Arity.Triple -> result.addAll(tripleTarget(targets))
                }
            }
            return result
        }

        fun resolveOutcomes(
            battleground: Battleground,
            executorCharacterContext: CharacterContext,
            executorPosition: Battleground.Node,
            ability: Ability.ExecutableAbility,
            targets: List<CharacterContext>
        ) : EncounterContext {

            ability

            when (ability) {
                is Ability.SelfAbility -> TODO()
                is Ability.SpellAbility -> TODO()
                is Ability.MeleeWeaponAbility -> TODO()
                is Ability.RangedWeaponAbility -> TODO()
            }
        }

        private fun doubleTargets(targets: List<CharacterContext>): List<List<CharacterContext>> {

            val result: MutableList<List<CharacterContext>> = mutableListOf()
            for (i in 1..targets.size) {
                for (j in i..targets.size) {
                    result.add(listOf(targets[i], targets[j]))
                }
            }
            return result
        }

        private fun tripleTarget(targets: List<CharacterContext>): List<List<CharacterContext>> {

            val result: MutableList<List<CharacterContext>> = mutableListOf()
            for (i in 1..targets.size) {
                for (j in i..targets.size) {
                    for (k in j..targets.size) {
                        result.add(listOf(targets[i], targets[j], targets[k]))
                    }
                }
            }
            return result
        }


        data class PointOfView(
            private val friendsAtNodes: Map<Battleground.Node, List<CharacterContext>>,
            private val foesAtNodes: Map<Battleground.Node, List<CharacterContext>>,
        ) {
            fun notPassable(
                abilityMovement: Movement,
                from: Battleground.Node,
                to: Battleground.Node
            ): Boolean {
                val noCapacityLeft = friendsAt(to).size + foesAt(to).size >= to.capacity

                val canNotLeave = when (abilityMovement) {
                    is Movement.NormalMovement -> friendsAt(from).size <= foesAt(from).size
                    is Movement.SpecialMovement -> false
                }

                return noCapacityLeft || canNotLeave
            }

            fun friendsAt(node: Battleground.Node): List<CharacterContext> {
                return friendsAtNodes.getOrDefault(node, listOf())
            }

            fun foesAt(node: Battleground.Node): List<CharacterContext> {
                return foesAtNodes.getOrDefault(node, listOf())
            }

            fun everyoneAt(node: Battleground.Node): List<CharacterContext> {
                return friendsAtNodes.getOrDefault(node, listOf()) + foesAtNodes.getOrDefault(node, listOf())
            }
        }
    }


    data class CharacterContext(
        val character: Character,
        val position: Battleground.Node,
        val damageTaken: Int,
        val resourcesSpent: Int,
        val statuses: List<Status>
    ) {
        fun takeDamage(hitPoints: Int): CharacterContext {
            return this.copy(damageTaken = max(0, damageTaken + hitPoints))
        }

        fun heal(hitPoints: Int): CharacterContext {
            return this.copy(damageTaken = max(0, damageTaken - hitPoints))
        }

        fun spendResources(amount: Int): CharacterContext {
            return this.copy(resourcesSpent = max(0, resourcesSpent + amount))
        }

        fun gainResources(amount: Int): CharacterContext {
            return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
        }

        fun applyEffect(status: Status): CharacterContext {
            return this.copy(statuses = statuses + status)
        }

        fun moveTo(newPosition: Battleground.Node): CharacterContext {
            return this.copy(position = newPosition)
        }

        fun canExecuteAbility(executableAbility: Ability.ExecutableAbility): Boolean {
            val classRestriction = executableAbility.classRestriction.contains(character.clazz())
            val resourceRestriction = executableAbility.resourceCost < character.maxResources() - resourcesSpent
            val armsRestriction = executableAbility.armsRestriction.invoke(character.arms())
            return classRestriction && resourceRestriction && armsRestriction
        }

        fun resolveEffect(effect: Effect) : CharacterContext {
            when (effect) {
                is Effect.ApplyBuffStatus -> TODO()
                is Effect.ApplyStatus -> TODO()
                is Effect.AvoidableDamage -> TODO()
                is Effect.DirectDamage -> TODO()
                is Effect.Healing -> TODO()
                Effect.NoEffect -> TODO()
                is Effect.ResourceBoost -> TODO()
                is Effect.WeaponDamage -> TODO()
            }
        }
    }


    class EncounterFactory() {
        fun create(
            battleground: Battleground,
            attackersStartingNode: Battleground.Node,
            defendersStartingNode: Battleground.Node,
            attackers: Set<Character>,
            defenders: Set<Character>,
        ): Encounter {
            return Encounter(
                battleground = battleground,
                attackers = attackers,
                defenders = defenders,
                encounterContext = EncounterContext(
                    attackerContexts = attackers.map { initialize(it, attackersStartingNode) }.toSet(),
                    defenderContexts = defenders.map { initialize(it, defendersStartingNode) }.toSet()
                ),
            )
        }


        private fun initialize(character: Character, startingPosition: Battleground.Node): CharacterContext {
            return CharacterContext(
                character = character,
                position = startingPosition,
                damageTaken = 0,
                resourcesSpent = 0,
                statuses = listOf()
            )
        }
    }

}