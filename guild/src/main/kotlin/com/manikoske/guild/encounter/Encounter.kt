package com.manikoske.guild.encounter

import com.manikoske.guild.action.*
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import kotlin.math.max

class Encounter(
    private val battleground: Battleground,
    private val attackers: Set<Character>,
    private val defenders: Set<Character>,
    private var encounterContext: EncounterContext
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
                encounterContext = EncounterContext(
                    characterContexts = attackers.associateBy(
                        { it.id },
                        {
                            initializeCharacterContext(
                                character = it,
                                startingNodeId = attackersStartingNodeId,
                                allegiance = CharacterContext.Allegiance.Attacker
                            )
                        }
                    ) + defenders.associateBy(
                        { it.id },
                        {
                            initializeCharacterContext(
                                character = it,
                                startingNodeId = defendersStartingNodeId,
                                allegiance = CharacterContext.Allegiance.Defender
                            )
                        }
                    ),
                ),
            )
        }

        private fun initializeCharacterContext(
            character: Character,
            startingNodeId: Int,
            allegiance: CharacterContext.Allegiance
        ): CharacterContext {
            return CharacterContext(
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

        val takerPointOfView = encounterContext.pointOfView(takerCharacterId)

        val possibleActions = Action.Actions.actions
            .filter { action -> takerPointOfView.self.canExecuteAction(action) }

        val possibleEndings: MutableList<EncounterContext> = mutableListOf()

        possibleActions.forEach { possibleAction ->

            val possibleMovementNodes = battleground.getAccessibleNodesIds(
                startNodeId = takerPointOfView.self.positionNodeId,
                encounterContext = encounterContext,
                pointOfView = takerPointOfView,
                actionMovement = possibleAction.movement
            )

            possibleMovementNodes.forEach { possibleMovementNodeId ->
                val possibleTargets = encounterContext.resolveTargets(
                    battleground = battleground,
                    pointOfView = takerPointOfView,
                    executorPositionNodeId = possibleMovementNodeId,
                    action = possibleAction
                )

                possibleTargets.forEach { targets ->
                    possibleEndings.add(
                        encounterContext.resolveEnding(
                            executorCharacterId = takerCharacterId,
                            newPositionNodeId = possibleMovementNodeId,
                            action = possibleAction,
                            targets = targets
                        )
                    )
                }
            }
        }
        this.encounterContext = possibleEndings.sortedByDescending { it.utility() }.take(3).random()
    }


    data class EncounterContext(
        private val characterContexts: Map<Int, CharacterContext>,
    ) {

        fun pointOfView(characterId: Int): PointOfView {
            val self = characterContext(characterId)
            return PointOfView(
                self = self,
                enemies = characterContexts.values.filter { characterContext -> characterContext.allegiance != self.allegiance },
                allies = characterContexts.values.filter { characterContext -> characterContext.allegiance == self.allegiance },
                everyone = characterContexts.values.toList(),
                everyoneElse = characterContexts.values.filter { characterContext -> characterContext.character.id != characterId }
            )
        }

        fun notPassable(
            pointOfView: PointOfView,
            actionMovement: Movement,
            edge: Battleground.Edge,
            toCapacity: Int
        ): Boolean {

            val noCapacityLeft =
                atNode(pointOfView.allies, edge.toNodeId).size + atNode(pointOfView.enemies, edge.toNodeId).size >= toCapacity

            val canNotLeave = when (actionMovement) {
                is Movement.NormalMovement ->
                    atNode(pointOfView.allies, edge.fromNodeId).size <= atNode(pointOfView.enemies, edge.fromNodeId).size
                is Movement.SpecialMovement -> false
            }

            return noCapacityLeft || canNotLeave
        }

        private fun atNode(characterContexts: List<CharacterContext>, nodeId: Int): List<CharacterContext> {
            return characterContexts.filter { it.positionNodeId == nodeId }
        }

        fun utility(): Int {
            return 1
        }

        fun resolveTargets(
            battleground: Battleground,
            pointOfView: PointOfView,
            executorPositionNodeId: Int,
            action: Action
        ): List<List<Int>> {

            val targetType = action.targetType(pointOfView.self.character)
            val result: MutableList<List<CharacterContext>> = mutableListOf()
            val possibleTargetNodeIds = battleground.nodesInRange(executorPositionNodeId, targetType.range)

            for (possibleTargetNodeId in possibleTargetNodeIds) {

                val scopedTargets = when (targetType.scope) {
                    TargetType.Scope.Ally -> pointOfView.allies
                    TargetType.Scope.Enemy -> pointOfView.enemies
                    TargetType.Scope.Self -> listOf(pointOfView.self)
                    TargetType.Scope.Everyone -> pointOfView.everyone
                    TargetType.Scope.EveryoneElse -> pointOfView.everyoneElse
                }

                val scopedTargetsAtPossibleTargetNode = atNode(scopedTargets, possibleTargetNodeId)

                when (targetType.arity) {
                    TargetType.Arity.Node -> result.add(scopedTargetsAtPossibleTargetNode)
                    TargetType.Arity.Single -> result.addAll(singleTarget(scopedTargetsAtPossibleTargetNode))
                    TargetType.Arity.Double -> result.addAll(doubleTarget(scopedTargetsAtPossibleTargetNode))
                    TargetType.Arity.Triple -> result.addAll(tripleTarget(scopedTargetsAtPossibleTargetNode))
                    TargetType.Arity.Battleground -> result.addAll(listOf(scopedTargets))
                }
            }
            return result.map { targets -> targets.map { target -> target.character.id } }
        }

        private fun copy(): EncounterContext {
            return EncounterContext(this.characterContexts.values.associateBy({ it.character.id }, { it.copy() }))
        }

        private fun characterContext(characterId: Int): CharacterContext {
            return characterContexts[characterId]!!
        }

        fun resolveEnding(
            executorCharacterId: Int,
            newPositionNodeId: Int,
            action: Action,
            targets: List<Int>
        ): EncounterContext {

            val possibleEnding = this.copy()
            val self = possibleEnding.characterContext(executorCharacterId)

            self.moveTo(newPositionNodeId)
            self.spendResources(action.resourceCost)

            targets.forEach { target ->
                action.effects(self.character).forEach { effect ->
                    val targetContext = possibleEnding.characterContext(target)
                    if (resolveEffect(effect, self, targetContext)) {
                        when (val triggeredAction = action.triggeredAction) {
                            is TriggeredAction.SelfTriggeredAction -> resolveEffect(
                                effect = triggeredAction.effect,
                                executorContext = self,
                                targetContext = self)
                            is TriggeredAction.TargetTriggeredAction -> resolveEffect(
                                effect = triggeredAction.effect,
                                executorContext = self,
                                targetContext = targetContext
                            )
                            null -> Unit
                        }
                    }
                }
            }
            return possibleEnding
        }

        private fun resolveEffect(
            effect: Effect,
            executorContext: CharacterContext,
            targetContext: CharacterContext,
        ): Boolean {
            if (!effect.savingThrow.saved(executorContext.character, targetContext.character)) {
                when (effect) {
                    is Effect.ApplyBuffStatus ->
                        targetContext.applyEffect(effect.status)

                    is Effect.ApplyStatus ->
                        targetContext.applyEffect(effect.status)

                    is Effect.AvoidableDamage ->
                        targetContext.takeDamage(
                            executorContext.character.attributeRoll(
                                effect.executorAttributeType,
                                effect.damageRoll
                            )
                        )

                    is Effect.DirectDamage ->
                        targetContext.takeDamage(effect.damageRoll.invoke())

                    is Effect.Healing ->
                        targetContext.heal(
                            executorContext.character.attributeRoll(
                                Attribute.Type.wisdom,
                                effect.healingRoll
                            )
                        )

                    Effect.NoEffect -> Unit
                    is Effect.ResourceBoost ->
                        targetContext.gainResources(effect.amount)

                    is Effect.WeaponDamage ->
                        targetContext.takeDamage(
                            executorContext.character.weaponDamageRoll(
                                effect.damageRoll,
                                effect.damageRollMultiplier
                            )
                        )
                }
                return true
            } else {
                return false
            }
        }


        private fun singleTarget(targets: List<CharacterContext>): List<List<CharacterContext>> {
            return targets.chunked(1)
        }

        private fun doubleTarget(targets: List<CharacterContext>): List<List<CharacterContext>> {
            val result: MutableList<List<CharacterContext>> = mutableListOf()
            if (targets.isNotEmpty()) {
                for (i in targets.indices) {
                    for (j in i..<targets.size) {
                        result.add(listOf(targets[i], targets[j]))
                    }
                }
            }
            return result
        }

        private fun tripleTarget(targets: List<CharacterContext>): List<List<CharacterContext>> {
            val result: MutableList<List<CharacterContext>> = mutableListOf()
            if (targets.isNotEmpty()) {
                for (i in targets.indices) {
                    for (j in i..<targets.size) {
                        for (k in j..<targets.size) {
                            result.add(listOf(targets[i], targets[j], targets[k]))
                        }
                    }
                }
            }
            return result
        }


        data class PointOfView(
            val self: CharacterContext,
            val enemies: List<CharacterContext>,
            val allies: List<CharacterContext>,
            val everyone: List<CharacterContext>,
            val everyoneElse: List<CharacterContext>,
        )
    }


    data class CharacterContext(
        val character: Character,
        var positionNodeId: Int,
        var allegiance: Allegiance,
        var damageTaken: Int,
        var resourcesSpent: Int,
        var statuses: List<Status>,
    ) {
        fun takeDamage(hitPoints: Int) {
            this.damageTaken = max(0, damageTaken + hitPoints)
        }

        fun heal(hitPoints: Int) {
            this.damageTaken = max(0, damageTaken - hitPoints)
        }

        fun spendResources(amount: Int) {
            this.resourcesSpent = max(0, resourcesSpent + amount)
        }

        fun gainResources(amount: Int) {
            this.resourcesSpent = max(0, resourcesSpent - amount)
        }

        fun applyEffect(status: Status) {
            this.statuses + status
        }

        fun moveTo(newPositionNodeIde: Int) {
            this.positionNodeId = newPositionNodeIde
        }

        fun canExecuteAction(executableAction: Action): Boolean {
            val classRestriction = executableAction.classRestriction.contains(character.clazz())
            val resourceRestriction = executableAction.resourceCost < character.maxResources() - resourcesSpent
            val armsRestriction = executableAction.armsRestriction.invoke(character.arms())
            return classRestriction && resourceRestriction && armsRestriction
        }

        enum class Allegiance {
            Attacker, Defender
        }
    }
}