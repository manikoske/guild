package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.rules.Die
import kotlin.math.max
import kotlin.math.min

data class CharacterState(
    val character: Character,
    val positionNodeId: Int,
    val allegiance: Allegiance,
    private val damageTaken: Int,
    private val resourcesSpent: Int,
    val effects: Effects,
) {

    companion object {

        fun noEffects(): Effects {
            return Effects(
                actionForcingEffect = null,
                movementRestrictingEffect = null,
                movementAlteringEffects = listOf(),
                actionRestrictingEffects = listOf(),
                damageOverTimeEffects = listOf(),
                healOverTimeEffects = listOf()
            )
        }
    }

    fun isDying(): Boolean {
        return effects.actionForcingEffect is Effect.ActionForcingEffect.Dying
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

    private fun currentHitPoints(): Int {
        return character.maxHitPoints() - damageTaken
    }

    private fun currentResources(): Int {
        return character.maxResources() - resourcesSpent
    }

    private fun takeDamage(damageToTake: Int): CharacterState {
        return this.copy(damageTaken = min(character.maxHitPoints(), damageTaken + damageToTake))
    }

    private fun addEffects(addedEffects: List<Effect>): CharacterState {
        return this.copy(effects = addedEffects.fold(effects) { effects: Effects, effect: Effect -> effects.add(effect) })
    }

    private fun removeEffects(removedEffects: List<Effect>): CharacterState {
        return this.copy(effects = removedEffects.fold(effects) { effects: Effects, effect: Effect ->
            effects.remove(
                effect
            )
        })
    }

    private fun heal(amountToHeal: Int): CharacterState {
        return this.copy(damageTaken = max(0, damageTaken - amountToHeal))
    }

    private fun spendResources(amount: Int): CharacterState {
        return this.copy(resourcesSpent = min(character.maxResources(), resourcesSpent + amount))
    }

    private fun gainResources(amount: Int): CharacterState {
        return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
    }

    private fun moveTo(newPositionNodeIde: Int): CharacterState {
        return this.copy(positionNodeId = newPositionNodeIde)
    }

    private fun effectsToRemoveByDamage(): List<Effect> {
        return effects.all().filter { it.removeOnDamageTaken() }
    }

    private fun effectsToAddByDamage(damage: Int, effectsOnHit: List<Effect>): List<Effect> {
        return effectsOnHit + if (damage >= currentHitPoints()) listOf(Effect.ActionForcingEffect.Dying(0)) else listOf()
    }

    private fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionEffect = effects.actionRestrictingEffects.none { it.restrictedAction(eventualAction) }
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost == 0 || eventualAction.resourceCost < currentResources()
        return noActionRestrictionEffect && classRestriction && resourceRestriction
    }

    fun allExecutableActions(): List<Action> {
        return effects.actionForcingEffect?.forcedAction().let { forcedAction ->
            if (forcedAction == null) {
                Action.Actions.basicActions.filter { canExecuteAction(it) }
            } else {
                listOf(forcedAction)
            }
        }
    }

    fun canMoveBy(actionMovement: Movement, requiredMovementAmount : Int): Boolean {
        val finalMovement =
            (effects.movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
                .let { if (it.amount > 0) effects.movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement.amount >= requiredMovementAmount
    }

    enum class Allegiance {
        Attacker, Defender
    }

    fun rollInitiative() : Event.InitiativeRolled {
        return Event.InitiativeRolled(
            updatedTarget = this,
            initiativeRoll = Event.InitiativeRoll(
                initiativeAttributeModifier = this.character.attributeModifier(Attribute.Type.dexterity),
                levelModifier = this.character.levelModifier(),
                roll = Die.Roll(Die.Dice.of(Die.d20))
            )
        )
    }

    fun attackBy(
        attacker: CharacterState,
        baseDifficultyClass: Int,
        executorAttributeType: Attribute.Type,
        targetAttributeType: Attribute.Type,
        damage: Die.Dice,
        effectsOnHit: List<Effect>
    ): Event.SpellAttackEvent {
        val spellAttackDifficultyClass = Event.SpellAttackDifficultyClass(
            spellAttributeModifier = attacker.character.attributeModifier(executorAttributeType),
            spellDifficultyClass = baseDifficultyClass,
            levelModifier = attacker.character.levelModifier()
        )

        val spellDefenseRoll = Event.SpellDefenseRoll(
            spellAttributeModifier = this.character.attributeModifier(targetAttributeType),
            levelModifier = this.character.levelModifier(),
            roll = Die.Roll(Die.Dice.of(Die.d20))
        )

        if (spellAttackDifficultyClass.attack >= spellDefenseRoll.defense) {

            val spellDamageRoll = Event.SpellDamageRoll(
                spellAttributeModifier = attacker.character.attributeModifier(executorAttributeType),
                levelModifier = attacker.character.levelModifier(),
                roll = Die.Roll(damage)
            )

            val effectsRemovedByDamage = effectsToRemoveByDamage()
            val effectsAddedByDamage = effectsToAddByDamage(spellDamageRoll.damage, effectsOnHit)

            return Event.SpellAttackHit(
                updatedTarget = this
                    .takeDamage(spellDamageRoll.damage)
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
                updatedTarget = this,
                spellDefenseRoll = spellDefenseRoll,
                spellAttackDifficultyClass = spellAttackDifficultyClass
            )
        }
    }

    fun healBy(
        healer: CharacterState,
        executorAttributeType: Attribute.Type,
        heal: Die.Dice
    ): Event.Healed {

        val healRoll = Event.HealRoll(
            healAttributeModifier = healer.character.attributeModifier(executorAttributeType),
            levelModifier = healer.character.levelModifier(),
            roll = Die.Roll(heal)
        )

        return Event.Healed(
            updatedTarget = this.heal(healRoll.heal),
            healRoll = healRoll
        )
    }

    fun attackBy(
        attacker: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int,
        effectsOnHit: List<Effect>
    ): Event.WeaponAttackEvent {

        val armorClass = Event.ArmorClass(
            armorModifier = character.armorClassArmorModifier(),
            armsModifier = character.armorClassArmsModifier(),
            levelModifier = character.levelModifier(),
            armorAttributeModifier = character.armorLimitedDexterityModifier()
        )

        val weaponAttackRoll = Event.WeaponAttackRoll(
            weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
            weaponAttackModifier = attacker.character.weaponAttackModifier(),
            actionAttackModifier = attackRollModifier,
            levelModifier = attacker.character.levelModifier(),
            roll = Die.Roll(Die.Dice.of(Die.d20))
        )

        if (weaponAttackRoll.attack >= armorClass.armorClass) {

            val weaponDamageRoll = Event.WeaponDamageRoll(
                weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
                actionDamageMultiplier = damageRollMultiplier,
                levelModifier = attacker.character.levelModifier(),
                roll = Die.Roll(attacker.character.weaponDamage())
            )

            val effectsRemovedByDamage = effectsToRemoveByDamage()
            val effectsAddedByDamage = effectsToAddByDamage(weaponDamageRoll.damage, effectsOnHit)

            return Event.WeaponAttackHit(
                updatedTarget = this
                    .takeDamage(weaponDamageRoll.damage)
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
                updatedTarget = this,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }

    fun takeAction(
        newPositionNodeId: Int,
        resourceCost: Int
    ) : Event.ActionTaken {
        return Event.ActionTaken(
            updatedTarget = this
                .moveTo(newPositionNodeId)
                .spendResources(resourceCost),
            resourceCost = resourceCost,
            newPositionNodeId = newPositionNodeId
        )
    }

    fun tickEffects(): Event.EffectsTicked {
        val healOverTimeRolls =
            effects.healOverTimeEffects.map {
                Event.HealOverTimeRoll(
                    category = it.category,
                    roll = Die.Roll(it.healDice)
                )
            }
        val damageOverTimeRolls =
            effects.damageOverTimeEffects.map {
                Event.DamageOverTimeRoll(
                    category = it.category,
                    roll = Die.Roll(it.damageDice)
                )
            }

        val removedEffects = effects.all().filter { it.tick() == null }
        val updatedEffects = effects.all().mapNotNull { it.tick() }

        return Event.EffectsTicked(
            updatedTarget = this
                .heal(healOverTimeRolls.sumOf { it.roll.rolled })
                .takeDamage(damageOverTimeRolls.sumOf { it.roll.rolled })
                .removeEffects(removedEffects)
                .addEffects(updatedEffects),
            removedEffects = removedEffects,
            updatedEffects = updatedEffects,
            damageOverTimeRolls = damageOverTimeRolls,
            healOverTimeRolls = healOverTimeRolls
        )
    }

    fun boostResources(amount: Int) : Event.ResourceBoosted {
        return Event.ResourceBoosted(
            updatedTarget = this.gainResources(amount),
            amount = amount
        )
    }

    fun addEffect(effect: Effect) : Event.EffectAdded {
        return Event.EffectAdded(
            updatedTarget = this.addEffects(listOf(effect)),
            category = effect.category
        )
    }

    fun removeEffect(effect: Effect) : Event.EffectRemoved {
        return Event.EffectRemoved(
            updatedTarget = this.removeEffects(listOf(effect)),
            category = effect.category
        )
    }
}
