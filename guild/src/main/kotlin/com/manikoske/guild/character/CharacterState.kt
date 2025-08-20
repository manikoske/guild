package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Target
import com.manikoske.guild.log.LoggingUtils

data class CharacterState(
    val character: Character,
    val positionNodeId: Int,
    val allegiance: Allegiance,
    private val damageTaken: Int,
    private val resourcesSpent: Int,
    val statuses: List<Status>,

) {
    override fun toString(): String {
        return LoggingUtils.formatCharacterState(this)
    }

    enum class Allegiance {
        Attacker, Defender
    }


    fun utility(): Double {
        val hitPointRatio = currentHitPoints() / character.maxHitPoints().toDouble()
        val resourceRatio = currentResources() / character.maxResources().toDouble()

        // Define weights: health is three times more important than resources.
        val healthWeight = 3.0
        val resourceWeight = 1.0
        val epsilon = 1e-6  // To avoid division by zero

        // Compute the weighted harmonic mean:
        // Weighted Harmonic Mean = (w1 + w2) / (w1/healthRatio + w2/resourceRatio)
        return if (hitPointRatio > epsilon && resourceRatio > epsilon) {
            (healthWeight + resourceWeight) / ((healthWeight / (hitPointRatio + epsilon)) + (resourceWeight / (resourceRatio + epsilon)))
        } else {
            0.0
        }
    }

    sealed interface Result {
        val updatedTarget: CharacterState

        sealed interface TakeDamageResult : Result {

            data class StillStanding(
                override val updatedTarget: CharacterState,
                val takenDamage: Int,
                val statusesRemovedOnDamage: List<Status>,
                val statusOnHit: Status?
            ) : TakeDamageResult

            data class Downed(
                override val updatedTarget: CharacterState,
                val takenDamage: Int,
                val damagedOver: Int,
                val statusesRemovedOnDamage: List<Status>,
                val statusOnHit: Status?
            ) : TakeDamageResult

            data class AlreadyDown(override val updatedTarget: CharacterState) : TakeDamageResult
            data class NoDamageTaken(override val updatedTarget: CharacterState) : TakeDamageResult
        }

        sealed interface ReceiveHealingResult : Result {

            data class Healed(
                override val updatedTarget: CharacterState,
                val amountHealed: Int,
                val overHealed: Int,
            ) : ReceiveHealingResult
            data class NoHeal(
                override val updatedTarget: CharacterState,
            ) : ReceiveHealingResult
            data class AlreadyFull(
                override val updatedTarget: CharacterState,
            ) : ReceiveHealingResult
        }

        data class TickStatusesResult (
            override val updatedTarget: CharacterState,
            val removedStatuses: List<Status>,
            val updatedStatuses: List<Status>
        ) : Result

        sealed interface SpendResourcesResult : Result {

            data class ResourcesSpent(
                override val updatedTarget: CharacterState,
                val spentAmount: Int,
                val resourcesRemaining: Int
            ) : SpendResourcesResult

            data class NoResourcesSpent(
                override val updatedTarget: CharacterState,
                val resourcesRemaining: Int
            ) : SpendResourcesResult

            data class InsufficientResources(override val updatedTarget: CharacterState) : SpendResourcesResult
        }

        sealed interface MovementResult : Result {

            data class Movement(
                override val updatedTarget: CharacterState,
                val newPositionNodeIde: Int,
                val statusesRemovedByMovement: List<Status>
            ) : MovementResult

            data class NoMovement(override val updatedTarget: CharacterState) : MovementResult
        }

        sealed interface AddStatusResult : Result {
            val addedStatus: Status

            data class Added(
                override val updatedTarget: CharacterState,
                override val addedStatus: Status
            ) : AddStatusResult

            data class Replaced(
                override val updatedTarget: CharacterState,
                override val addedStatus: Status,
                val replacedStatus: Status
            ) : AddStatusResult
        }

        sealed interface RemoveStatusesResult : Result {

            data class Removed(
                override val updatedTarget: CharacterState,
                val removedStatuses: List<Status>
            ) : RemoveStatusesResult

            data class NothingRemoved(
                override val updatedTarget: CharacterState,
            ) : RemoveStatusesResult
        }

        sealed interface BoostResourcesResult : Result {

            data class ResourcesBoosted(
                override val updatedTarget: CharacterState,
                val amountBoosted: Int,
                val overBoosted: Int
            ) : BoostResourcesResult

            data class NoResourceBoost(
                override val updatedTarget: CharacterState,
            ) : BoostResourcesResult

            data class AlreadyFull(
                override val updatedTarget: CharacterState,
            ) : BoostResourcesResult

        }
    }

    fun takeDamage(damageToTake: Int, statusOnHit: Status? = null): Result.TakeDamageResult {

        return if (damageTaken == character.maxHitPoints()) {
            Result.TakeDamageResult.AlreadyDown(this)
        } else {
            if (damageToTake > 0) {

                val statusesRemovedOnDamage = statuses.filter { it.removedOnDamageTaken }

                if (damageTaken + damageToTake >= character.maxHitPoints()) {
                    Result.TakeDamageResult.Downed(
                        updatedTarget = this
                            .copy(damageTaken = character.maxHitPoints())
                            .removeStatuses(statusesRemovedOnDamage)
                            .addStatuses(listOfNotNull(statusOnHit) + Status.StatusFactory.down()),
                        statusesRemovedOnDamage = statusesRemovedOnDamage,
                        takenDamage = character.maxHitPoints() - damageTaken,
                        damagedOver = damageTaken + damageToTake - character.maxHitPoints(),
                        statusOnHit = statusOnHit
                    )
                } else {
                    Result.TakeDamageResult.StillStanding(
                        updatedTarget = this
                            .copy(damageTaken = damageTaken + damageToTake)
                            .removeStatuses(statusesRemovedOnDamage)
                            .addStatuses(listOfNotNull(statusOnHit)),
                        takenDamage = damageToTake,
                        statusesRemovedOnDamage = statusesRemovedOnDamage,
                        statusOnHit = statusOnHit
                    )
                }
            } else {
                Result.TakeDamageResult.NoDamageTaken(this)
            }
        }
    }

    fun heal(amountToHeal: Int): Result.ReceiveHealingResult {
        return if (currentHitPoints() == 0 || amountToHeal == 0) {
            Result.ReceiveHealingResult.NoHeal(this)
        } else if (damageTaken == 0) {
            Result.ReceiveHealingResult.AlreadyFull(this)
        } else {
            if (damageTaken < amountToHeal) {
                Result.ReceiveHealingResult.Healed(
                    updatedTarget = this.copy(damageTaken = 0),
                    overHealed = amountToHeal - damageTaken,
                    amountHealed = damageTaken,
                )
            } else {
                Result.ReceiveHealingResult.Healed(
                    updatedTarget = this.copy(damageTaken = damageTaken - amountToHeal),
                    overHealed = 0,
                    amountHealed = amountToHeal,
                )
            }
        }
    }

    fun tickStatuses(): Result.TickStatusesResult {
        val updatedStatuses = statuses.map { it.tick() }.filterIsInstance<Status.TickResult.Update>().map { it.updatedStatus }
        val removedStatuses = statuses.map { it.tick() }.filterIsInstance<Status.TickResult.Remove>().map { it.removedStatus }
        return Result.TickStatusesResult(
            updatedTarget = this
                .addStatuses(updatedStatuses)
                .removeStatuses(removedStatuses),
            removedStatuses = removedStatuses,
            updatedStatuses = updatedStatuses
        )
    }

    fun spendResources(amount: Int): Result.SpendResourcesResult {
        return if (amount == 0) {
            Result.SpendResourcesResult.NoResourcesSpent(updatedTarget = this, resourcesRemaining = currentResources())
        } else {
            if (currentResources() < amount) {
                Result.SpendResourcesResult.InsufficientResources(updatedTarget = this)
            } else {
                Result.SpendResourcesResult.ResourcesSpent(
                    updatedTarget = this.copy(resourcesSpent = resourcesSpent + amount),
                    spentAmount = amount,
                    resourcesRemaining = character.maxResources() - (resourcesSpent + amount)
                )
            }
        }
    }

    fun boostResources(amount: Int): Result.BoostResourcesResult {
        return if (resourcesSpent == 0) {
            Result.BoostResourcesResult.AlreadyFull(this)
        } else if (amount == 0) {
            Result.BoostResourcesResult.NoResourceBoost(this)
        } else {
            if (resourcesSpent < amount) {
                Result.BoostResourcesResult.ResourcesBoosted(
                    updatedTarget = this.copy(resourcesSpent = 0),
                    overBoosted = amount - resourcesSpent,
                    amountBoosted = resourcesSpent,
                )
            } else {
                Result.BoostResourcesResult.ResourcesBoosted(
                    updatedTarget = this.copy(resourcesSpent = resourcesSpent - amount),
                    overBoosted = 0,
                    amountBoosted = amount,
                )
            }
        }
    }

    fun moveTo(newPositionNodeIde: Int): Result.MovementResult {
        if (newPositionNodeIde == positionNodeId) {
            return Result.MovementResult.NoMovement(updatedTarget = this)
        } else {
            val statusesRemovedByMovement = statuses.filter { it.removedOnMovement }
            return Result.MovementResult.Movement(
                updatedTarget = this
                    .copy(positionNodeId = newPositionNodeIde)
                    .removeStatuses(statusesRemovedByMovement),
                newPositionNodeIde = newPositionNodeIde,
                statusesRemovedByMovement = statusesRemovedByMovement,
            )
        }
    }

    fun addStatus(status: Status): Result.AddStatusResult {

        val existingStatus = statuses.find { it.name == status.name }
        val updatedTarget = this.addStatuses(listOf(status))

        return if (existingStatus != null) {
            Result.AddStatusResult.Replaced(updatedTarget = updatedTarget, addedStatus = status, replacedStatus = existingStatus)
        } else {
            Result.AddStatusResult.Added(updatedTarget = updatedTarget, addedStatus = status)
        }

    }

    fun removeStatuses(name: Status.Name) : Result.RemoveStatusesResult {
        val statusesToRemove = statuses.filter { it.name == name }
        return if (statusesToRemove.isNotEmpty()) {
            Result.RemoveStatusesResult.Removed(updatedTarget = this.removeStatuses(statusesToRemove), removedStatuses = statusesToRemove)
        } else {
            Result.RemoveStatusesResult.NothingRemoved(updatedTarget = this)
        }
    }

    fun allExecutableActions(): List<Action> {

        val forcedNoAction = statuses
            .mapNotNull { it.actionAvailabilityAlteringEffect }
            .filterIsInstance<Effect.ActionAvailabilityAlteringEffect.NoActionForcingEffect>()
            .any()

        if (forcedNoAction) return listOf(Action.Actions.noAction)

        val forcedActions = statuses
            .mapNotNull { it.actionAvailabilityAlteringEffect }
            .filterIsInstance<Effect.ActionAvailabilityAlteringEffect.ActionsForcingEffect>()
            .flatMap { it.forcedActions }

        if (forcedActions.isNotEmpty()) return forcedActions

        val restrictedActions = statuses
            .mapNotNull { it.actionAvailabilityAlteringEffect }
            .filterIsInstance<Effect.ActionAvailabilityAlteringEffect.ActionRestrictingEffect>()
            .fold(Action.Actions.basicActions + character.availableActions) { filteredActions, status -> filteredActions.filter { status.predicate(it) }}

        return restrictedActions.filter { action ->
            action.resourceCost <= currentResources() &&
                    action.requiredStatus?.let { statusName -> statuses.any { it.name == statusName } } ?: true
        }
    }

    fun actualMovement(actionMovement: Movement) : Movement {
        val amountAlteredMovement = statuses.fold(actionMovement) { updatedMovement, status ->
            if (status.actionMovementAlteringEffect is Effect.ActionMovementAlteringEffect.ActionMovementAmountAlteringEffect) {
                status.actionMovementAlteringEffect.alteration(updatedMovement)
            } else {
                updatedMovement
            }
        }
        return statuses.fold(amountAlteredMovement) { updatedMovement, status ->
            if (status.actionMovementAlteringEffect is Effect.ActionMovementAlteringEffect.ActionMovementRestrictingEffect) {
                status.actionMovementAlteringEffect.restriction(updatedMovement)
            } else {
                updatedMovement
            }
        }
    }

    fun targetableBy(targetType: Target.Type) : Boolean {
        return statuses
            .mapNotNull { it.targetabilityAlteringEffect }
            .fold(Target.Type.entries.toSet()) { targetableBy, effect -> targetableBy intersect effect.targetableBy}
            .contains(targetType)
    }

    fun currentHitPoints(): Int {
        return character.maxHitPoints() - damageTaken
    }

    fun currentResources(): Int {
        return character.maxResources() - resourcesSpent
    }

    private fun addStatuses(statusesToAdd: List<Status>): CharacterState {
        return this.copy(statuses = (statusesToAdd + statuses).distinctBy { it.name })
    }

    private fun removeStatuses(statusesToRemove: List<Status>): CharacterState {
        return this.copy(statuses = statuses - statusesToRemove)
    }
}