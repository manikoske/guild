package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Outcome
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

    fun canMoveBy(actionMovement: Movement): Movement {
        val finalMovement =
            (effects.movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
                .let { if (it.amount > 0) effects.movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement
    }

    enum class Allegiance {
        Attacker, Defender
    }

    fun attackBy(
        attacker: CharacterState,
        baseDifficultyClass: Int,
        executorAttributeType: Attribute.Type,
        targetAttributeType: Attribute.Type,
        damage: Die.Dice,
        effectsOnHit: List<Effect>
    ): Outcome.SpellAttackOutcome {
        val spellAttackDifficultyClass = Outcome.SpellAttackDifficultyClass(
            spellAttributeModifier = attacker.character.attributeModifier(executorAttributeType),
            spellDifficultyClass = baseDifficultyClass,
            levelModifier = attacker.character.levelModifier()
        )

        val spellDefenseRoll = Outcome.SpellDefenseRoll(
            spellAttributeModifier = this.character.attributeModifier(targetAttributeType),
            levelModifier = this.character.levelModifier(),
            roll = Die.Roll(Die.Dice.of(Die.d20))
        )

        if (spellAttackDifficultyClass.attack >= spellDefenseRoll.defense) {

            val spellDamageRoll = Outcome.SpellDamageRoll(
                spellAttributeModifier = attacker.character.attributeModifier(executorAttributeType),
                levelModifier = attacker.character.levelModifier(),
                roll = Die.Roll(damage)
            )

            val effectsRemovedByDamage = effectsToRemoveByDamage()
            val effectsAddedByDamage = effectsToAddByDamage(spellDamageRoll.damage, effectsOnHit)

            return Outcome.SpellAttackHit(
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
            return Outcome.SpellAttackMissed(
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
    ): Outcome.Healed {

        val healRoll = Outcome.HealRoll(
            healAttributeModifier = healer.character.attributeModifier(executorAttributeType),
            levelModifier = healer.character.levelModifier(),
            roll = Die.Roll(heal)
        )

        return Outcome.Healed(
            updatedTarget = this.heal(healRoll.heal),
            healRoll = healRoll
        )
    }

    fun attackBy(
        attacker: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int,
        effectsOnHit: List<Effect>
    ): Outcome.WeaponAttackOutcome {

        val armorClass = Outcome.ArmorClass(
            armorModifier = character.armorClassArmorModifier(),
            armsModifier = character.armorClassArmsModifier(),
            levelModifier = character.levelModifier(),
            armorAttributeModifier = character.armorLimitedDexterityModifier()
        )

        val weaponAttackRoll = Outcome.WeaponAttackRoll(
            weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
            weaponAttackModifier = attacker.character.weaponAttackModifier(),
            actionAttackModifier = attackRollModifier,
            levelModifier = attacker.character.levelModifier(),
            roll = Die.Roll(Die.Dice.of(Die.d20))
        )

        if (weaponAttackRoll.attack >= armorClass.armorClass) {

            val weaponDamageRoll = Outcome.WeaponDamageRoll(
                weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
                actionDamageMultiplier = damageRollMultiplier,
                levelModifier = attacker.character.levelModifier(),
                roll = Die.Roll(attacker.character.weaponDamage())
            )

            val effectsRemovedByDamage = effectsToRemoveByDamage()
            val effectsAddedByDamage = effectsToAddByDamage(weaponDamageRoll.damage, effectsOnHit)

            return Outcome.WeaponAttackHit(
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
            return Outcome.WeaponAttackMissed(
                updatedTarget = this,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }

    fun takeAction(
        name: String,
        newPositionNodeId: Int,
        resourceCost: Int
    ) : Outcome.ActionTaken {
        return Outcome.ActionTaken(
            updatedTarget = this
                .moveTo(newPositionNodeId)
                .spendResources(resourceCost),
            name = name,
            resourceCost = resourceCost,
            newPositionNodeId = newPositionNodeId
        )
    }

    fun tickEffects(): Outcome.EffectsTicked {
        val healOverTimeRolls =
            effects.healOverTimeEffects.map {
                Outcome.HealOverTimeRoll(
                    category = it.category,
                    roll = Die.Roll(it.healDice)
                )
            }
        val damageOverTimeRolls =
            effects.damageOverTimeEffects.map {
                Outcome.DamageOverTimeRoll(
                    category = it.category,
                    roll = Die.Roll(it.damageDice)
                )
            }

        val removedEffects = effects.all().filter { it.tick() == null }
        val updatedEffects = effects.all().mapNotNull { it.tick() }

        return Outcome.EffectsTicked(
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

    fun boostResources(amount: Int) : Outcome.ResourceBoosted {
        return Outcome.ResourceBoosted(
            updatedTarget = this.gainResources(amount),
            amount = amount
        )
    }

    fun addEffect(effect: Effect) : Outcome.EffectAdded {
        return Outcome.EffectAdded(
            updatedTarget = this.addEffects(listOf(effect)),
            category = effect.category
        )
    }

    fun removeEffect(effect: Effect) : Outcome.EffectRemoved {
        return Outcome.EffectRemoved(
            updatedTarget = this.removeEffects(listOf(effect)),
            category = effect.category
        )
    }
}
