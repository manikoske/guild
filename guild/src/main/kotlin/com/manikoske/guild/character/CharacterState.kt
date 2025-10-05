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
            ) : TakeDamageResult

            data class Downed(
                override val updatedTarget: CharacterState,
                val takenDamage: Int,
                val damagedOver: Int,
                val statusesRemovedOnDamage: List<Status>,
            ) : TakeDamageResult

            data class AlreadyDown(override val updatedTarget: CharacterState) : TakeDamageResult
            data class NoDamageTaken(override val updatedTarget: CharacterState) : TakeDamageResult
        }

        sealed interface ReceiveHealingResult : Result {

            data class Healed(
                override val updatedTarget: CharacterState,
                val amountHealed: Int,
                val statusesRemovedByHealing: List<Status>
            ) : ReceiveHealingResult
            data class HealedToFull(
                override val updatedTarget: CharacterState,
                val amountHealed: Int,
                val overHealed: Int,
                val statusesRemovedByHealing: List<Status>
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
            val tickRemovedStatuses: List<Status>,
            val tickUpdatedStatuses: List<Status>,
            val targetedActionRemovedStatuses: List<Status>,
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

            data class Added(
                override val updatedTarget: CharacterState,
                val addedStatus: Status
            ) : AddStatusResult

            data class Replaced(
                override val updatedTarget: CharacterState,
                val addedStatus: Status,
                val replacedStatus: Status
            ) : AddStatusResult

            data class NothingAdded(override val updatedTarget: CharacterState) : AddStatusResult
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

    fun takeDamage(damageToTake: Int): Result.TakeDamageResult {

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
                            .down(),
                        statusesRemovedOnDamage = statusesRemovedOnDamage,
                        takenDamage = character.maxHitPoints() - damageTaken,
                        damagedOver = damageTaken + damageToTake - character.maxHitPoints(),
                    )
                } else {
                    Result.TakeDamageResult.StillStanding(
                        updatedTarget = this
                            .copy(damageTaken = damageTaken + damageToTake)
                            .removeStatuses(statusesRemovedOnDamage),
                        takenDamage = damageToTake,
                        statusesRemovedOnDamage = statusesRemovedOnDamage,
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
            val statusesRemovedByHealing = statuses.filter { it.removedOnHealing }

            if (damageTaken < amountToHeal) {
                Result.ReceiveHealingResult.HealedToFull(
                    updatedTarget = this
                        .copy(damageTaken = 0)
                        .removeStatuses(statusesRemovedByHealing),
                    overHealed = amountToHeal - damageTaken,
                    amountHealed = damageTaken,
                    statusesRemovedByHealing = statusesRemovedByHealing

                )
            } else {
                Result.ReceiveHealingResult.Healed(
                    updatedTarget = this
                        .copy(damageTaken = damageTaken - amountToHeal)
                        .removeStatuses(statusesRemovedByHealing),
                    amountHealed = amountToHeal,
                    statusesRemovedByHealing = statusesRemovedByHealing
                )
            }
        }
    }

    fun tickStatuses(targetedActionTaken: Boolean): Result.TickStatusesResult {

        val targetedActionRemovedStatuses = statuses.filter { targetedActionTaken && it.removedOnTargetedAction }
        val updatedStatuses = statuses - targetedActionRemovedStatuses

        val tickUpdatedStatuses = updatedStatuses.map { it.tick() }.filterIsInstance<Status.TickResult.Update>().map { it.updatedStatus }
        val tickRemovedStatuses = updatedStatuses.map { it.tick() }.filterIsInstance<Status.TickResult.Remove>().map { it.removedStatus }

        return Result.TickStatusesResult(
            updatedTarget = this.copy(
                statuses = (tickUpdatedStatuses + updatedStatuses - tickRemovedStatuses).distinctBy { it.name }
            ),
            tickRemovedStatuses = tickRemovedStatuses,
            tickUpdatedStatuses = tickUpdatedStatuses,
            targetedActionRemovedStatuses = targetedActionRemovedStatuses,
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

    fun addStatus(status: Status?): Result.AddStatusResult {

        if (status == null) return Result.AddStatusResult.NothingAdded(updatedTarget = this)

        val existingStatus = statuses.find { it.name == status.name }

        return if (existingStatus != null) {
            Result.AddStatusResult.Replaced(
                updatedTarget = this.copy(statuses = (statuses - existingStatus + status)),
                addedStatus = status,
                replacedStatus = existingStatus
            )
        } else {
            Result.AddStatusResult.Added(
                updatedTarget = this.copy(statuses = (statuses + status)),
                addedStatus = status
            )
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

        if (forcedNoAction) return listOf()

        val forcedActions = statuses
            .mapNotNull { it.actionAvailabilityAlteringEffect }
            .filterIsInstance<Effect.ActionAvailabilityAlteringEffect.ActionsForcingEffect>()
            .flatMap { it.forcedActions }

        if (forcedActions.isNotEmpty()) return forcedActions

        val statusRestrictedActions = statuses
            .mapNotNull { it.actionAvailabilityAlteringEffect }
            .filterIsInstance<Effect.ActionAvailabilityAlteringEffect.ActionRestrictingEffect>()
            .fold(Action.Actions.basicActions + character.availableActions) { filteredActions, status -> filteredActions.filter { status.predicate(it) }}

        val resourceRestrictedActions =
            statusRestrictedActions.filter { action -> action.resourceCost <= currentResources() }

        val requiredStatusRestrictedActions =
            resourceRestrictedActions.filter { action -> action.requiredStatus?.let { statusName -> statuses.any { it.name == statusName } } ?: true }

        val forcedRandomAction = statuses
            .mapNotNull { it.actionAvailabilityAlteringEffect }
            .filterIsInstance<Effect.ActionAvailabilityAlteringEffect.RandomActionForcingEffect>()
            .minOfOrNull { it.randomActionsCount } ?: 0

        return if (forcedRandomAction > 0) {
            requiredStatusRestrictedActions.shuffled().take(forcedRandomAction)
        } else {
            requiredStatusRestrictedActions
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

    private fun down(): CharacterState {
        return this.copy(statuses = (statuses + Status.StatusFactory.down()))
    }

    private fun removeStatuses(statusesToRemove: List<Status>): CharacterState {
        return this.copy(statuses = statuses - statusesToRemove)
    }
}