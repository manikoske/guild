package com.manikoske.guild.rules

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.character.Effect

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

        val effectsRemovedByMovement = if (target.positionNodeId != newPositionNodeId) target.effectsToRemoveByMovement() else listOf()

        return Event.ActionStarted(
            actionName = actionName,
            target = target,
            updatedTarget = target
                .moveTo(newPositionNodeId)
                .spendResources(resourcesSpent),
            newPositionNodeId = newPositionNodeId,
            resourcesSpent = resourcesSpent,
            effectsRemovedByMovement = effectsRemovedByMovement
        )
    }

    fun weaponAttackBy(
        executor: CharacterState,
        target: CharacterState,
        baseDifficultyClass: Int,
        executorAttributeType: Attribute.Type,
        targetAttributeType: Attribute.Type,
        damage: Dice,
        effectsOnHit: List<Effect>,
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

            val effectsRemovedByDamage = target.effectsToRemoveByDamage()
            val effectsAddedByDamage = target.effectsToAddByDamage(spellDamageRoll.result, effectsOnHit)

            return Event.SpellAttackHit(
                target = target,
                updatedTarget = target
                    .takeDamage(spellDamageRoll.result)
                    .removeEffects(effectsRemovedByDamage)
                    .addEffects(effectsAddedByDamage),
                spellAttackDifficultyClass = spellAttackDifficultyClass,
                spellDefenseRoll = spellDefenseRoll,
                spellDamageRoll = spellDamageRoll,
                effectsRemovedByDamage = effectsRemovedByDamage,
                effectsAddedByDamage = effectsAddedByDamage
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

        return Event.Healed(
            target = target,
            updatedTarget = target.heal(healRoll.result),
            healRoll = healRoll
        )
    }

    fun weaponAttackBy(
        executor: CharacterState,
        target: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int,
        effectsOnHit: List<Effect>,
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

            val effectsRemovedByDamage = target.effectsToRemoveByDamage()
            val effectsAddedByDamage = target.effectsToAddByDamage(weaponDamageRoll.result, effectsOnHit)

            return Event.WeaponAttackHit(
                target = target,
                updatedTarget = target
                    .takeDamage(weaponDamageRoll.result)
                    .removeEffects(effectsRemovedByDamage)
                    .addEffects(effectsAddedByDamage),
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass,
                weaponDamageRoll = weaponDamageRoll,
                effectsRemovedByDamage = effectsRemovedByDamage,
                effectsAddedByDamage = effectsAddedByDamage
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
        val healOverTimeRolls =
            target.effects.healOverTimeEffects.map { target.character.healOverTimeRoll(effect = it, rollMethod = rollMethod) }
        val damageOverTimeRolls =
            target.effects.damageOverTimeEffects.map { target.character.damageOverTimeRoll(effect = it, rollMethod = rollMethod) }

        val removedEffects = target.effects.all().filter { it.tick() == null }
        val updatedEffects = target.effects.all().mapNotNull { it.tick() }

        return Event.ActionEnded(
            target = target,
            updatedTarget = target
                .heal(healOverTimeRolls.sumOf { it.result })
                .takeDamage(damageOverTimeRolls.sumOf { it.result })
                .removeEffects(removedEffects)
                .addEffects(updatedEffects),
            removedEffects = removedEffects,
            updatedEffects = updatedEffects,
            damageOverTimeRolls = damageOverTimeRolls,
            healOverTimeRolls = healOverTimeRolls
        )
    }

    fun boostResources(
        target: CharacterState,
        amount: Int
    ) : Event.ResourceBoosted {
        return Event.ResourceBoosted(
            target = target,
            updatedTarget = target.gainResources(amount),
            amount = amount
        )
    }

    fun addEffect(
        target: CharacterState,
        effects: List<Effect>
    ) : Event.EffectAdded {
        return Event.EffectAdded(
            target = target,
            updatedTarget = target.addEffects(effects),
            effects = effects
        )
    }

    fun removeEffect(
        target: CharacterState,
        effects: List<Effect>
    ) : Event.EffectRemoved {
        return Event.EffectRemoved(
            target = target,
            updatedTarget = target.removeEffects(effects),
            effects = effects
        )
    }
}