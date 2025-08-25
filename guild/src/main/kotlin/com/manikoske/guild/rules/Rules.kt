package com.manikoske.guild.rules

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Effect
import com.manikoske.guild.character.Status

object Rules {

    fun rollInitiative(
        target: CharacterState,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Event.InitiativeRolled {
        return Event.InitiativeRolled(
            target = target,
            updatedTarget = target,
            initiativeRoll = target.character.initiativeRoll(rollMethod)
        )
    }

    fun startAction(
        target: CharacterState,
        actionName: String,
        newPositionNodeId: Int,
        resourcesSpent: Int

    ) : Event.ActionStarted {

        val movementResult = target.moveTo(newPositionNodeId)
        val spendResourcesResult = movementResult.updatedTarget.spendResources(resourcesSpent)

        return Event.ActionStarted(
            actionName = actionName,
            target = target,
            updatedTarget = spendResourcesResult.updatedTarget,
            movementResult = movementResult,
            spendResourcesResult = spendResourcesResult
        )
    }
    // TODO when resolving statusOnHite return AddStatusResult
    fun spellAttackBy(
        executor: CharacterState,
        target: CharacterState,
        baseDifficultyClass: Int,
        executorAttributeType: Attribute.Type,
        targetAttributeType: Attribute.Type,
        damage: Dice,
        statusOnHit: Status?,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.SpellAttackEvent {

        val spellAttackDifficultyClass = executor.character.spellAttackDifficultyClass(
            attributeType = executorAttributeType,
            baseDifficultyClass = baseDifficultyClass
        )

        val spellDefenseRoll = target.character.spellDefenseRoll(
            attributeType = targetAttributeType,
            rollMethod = rollMethod
        )

        if (spellAttackDifficultyClass.result >= spellDefenseRoll.result) {

            val spellDamageRoll = executor.character.spellDamageRoll(
                attributeType = executorAttributeType,
                damage = damage,
                rollMethod = rollMethod
            )

            val damageResult = target.takeDamage(damageToTake = spellDamageRoll.result, statusOnHit = statusOnHit)

            return Event.SpellAttackHit(
                target = target,
                updatedTarget = damageResult.updatedTarget,
                spellAttackDifficultyClass = spellAttackDifficultyClass,
                spellDefenseRoll = spellDefenseRoll,
                spellDamageRoll = spellDamageRoll,
                takeDamageResult = damageResult
            )
        } else {
            return Event.SpellAttackMissed(
                target = target,
                updatedTarget = target,
                spellDefenseRoll = spellDefenseRoll,
                spellAttackDifficultyClass = spellAttackDifficultyClass
            )
        }
    }

    fun healBy(
        executor: CharacterState,
        target: CharacterState,
        executorAttributeType: Attribute.Type,
        heal: Dice,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.Healed {

        val healRoll = executor.character.healRoll(
            attributeType = executorAttributeType,
            heal = heal,
            rollMethod = rollMethod
        )

        val healResult = target.heal(healRoll.result)

        return Event.Healed(
            target = target,
            updatedTarget = healResult.updatedTarget,
            healRoll = healRoll,
            receiveHealingResult = healResult
        )
    }

    // TODO when resolving statusOnHite return AddStatusResult
    fun weaponAttackBy(
        executor: CharacterState,
        target: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int,
        statusOnHit: Status?,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.WeaponAttackEvent {

        val armorClass = target.character.armorClass()

        val weaponAttackRoll = executor.character.weaponAttackRoll(
            actionAttackRollModifier = attackRollModifier,
            rollMethod = rollMethod
        )

        if (weaponAttackRoll.result >= armorClass.result) {

            val weaponDamageRoll = executor.character.weaponDamageRoll(
                damageRollMultiplier = damageRollMultiplier,
                rollMethod = rollMethod
            )

            val damageResult = target.takeDamage(damageToTake = weaponDamageRoll.result, statusOnHit = statusOnHit)

            return Event.WeaponAttackHit(
                target = target,
                updatedTarget = damageResult.updatedTarget,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass,
                weaponDamageRoll = weaponDamageRoll,
                takeDamageResult = damageResult
            )
        } else {
            return Event.WeaponAttackMissed(
                target = target,
                updatedTarget = target,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }

    fun endAction(
        target: CharacterState,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.ActionEnded {

        val healOverTimeRolls = target.statuses
                .mapNotNull { it.hpAffectingOverTimeEffect }
                .filterIsInstance<Effect.HpAffectingOverTimeEffect.HealingOverTimeEffect>()
                .map { target.character.healOverTimeRoll(effect = it, rollMethod = rollMethod) }

        val damageOverTimeRolls =
            target.statuses
                .mapNotNull { it.hpAffectingOverTimeEffect }
                .filterIsInstance<Effect.HpAffectingOverTimeEffect.DamageOverTimeEffect>()
                .map { target.character.damageOverTimeRoll(effect = it, rollMethod = rollMethod) }

        val healResult = target.heal(healOverTimeRolls.sumOf { it.result })
        val damageResult = healResult.updatedTarget.takeDamage(damageOverTimeRolls.sumOf { it.result })
        val statusesTickResult = damageResult.updatedTarget.tickStatuses()

        return Event.ActionEnded(
            target = target,
            updatedTarget = statusesTickResult.updatedTarget,
            damageOverTimeRolls = damageOverTimeRolls,
            healOverTimeRolls = healOverTimeRolls,
            receiveHealingResult = healResult,
            takeDamageResult = damageResult,
            tickStatusesResult = statusesTickResult
        )
    }

    fun boostResources(
        target: CharacterState,
        amount: Int
    ) : Event.ResourceBoosted {

        val boostResourcesResult = target.boostResources(amount)
        return Event.ResourceBoosted(
            target = target,
            updatedTarget = boostResourcesResult.updatedTarget,
            boostResourcesResult = boostResourcesResult
        )
    }

    fun addStatus(
        target: CharacterState,
        status: Status
    ) : Event.StatusAdded {

        val addStatusResult = target.addStatus(status)

        return Event.StatusAdded(
            target = target,
            updatedTarget = addStatusResult.updatedTarget,
            addStatusResult = addStatusResult
        )
    }

    fun removeStatusByName(
        target: CharacterState,
        name: Status.Name
    ) : Event.StatusesRemoved {

        val removeStatusesResult = target.removeStatuses(name)

        return Event.StatusesRemoved(
            target = target,
            updatedTarget = removeStatusesResult.updatedTarget,
            removeStatusesResult = removeStatusesResult
        )
    }
}