package com.manikoske.guild.character

import com.manikoske.guild.action.Action
import com.manikoske.guild.character.Effect
import com.manikoske.guild.action.Movement
import com.manikoske.guild.character.Effects
import com.manikoske.guild.log.LoggingUtils
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Die
import com.manikoske.guild.rules.DifficultyClass
import com.manikoske.guild.rules.Event
import com.manikoske.guild.rules.Roll
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

    override fun toString(): String {
        return LoggingUtils.formatCharacterState(this)
    }


    enum class Allegiance {
        Attacker, Defender
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

    fun currentHitPoints(): Int {
        return character.maxHitPoints() - damageTaken
    }

    fun currentResources(): Int {
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
        val resourceRestriction = eventualAction.resourceCost == 0 || eventualAction.resourceCost <= currentResources()
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

    fun spellAttackDifficultyClass(
        attributeType: Attribute.Type,
        baseDifficultyClass: Int
    ) : DifficultyClass.SpellAttackDifficultyClass {
        return DifficultyClass.SpellAttackDifficultyClass(
            spellAttributeModifier = character.attributeModifier(attributeType),
            spellDifficultyClass = baseDifficultyClass,
            levelModifier = character.levelModifier()
        )
    }

    fun armorClass() : DifficultyClass.ArmorClass {
        return DifficultyClass.ArmorClass(
            armorDifficultyClass = character.armorDifficultyClass(),
            armsModifier = character.armorClassArmsModifier(),
            levelModifier = character.levelModifier(),
            armorAttributeModifier = character.armorLimitedDexterityModifier()
        )
    }

    fun initiativeRoll(
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.InitiativeRoll {
        return Roll.InitiativeRoll(
            attributeModifier = character.attributeModifier(Attribute.Type.dexterity),
            levelModifier = character.levelModifier(),
            rolled = Roll.Rolled(dice = Dice.Companion.of(Die.d20), rollMethod = rollMethod)
        )
    }

    fun spellDefenseRoll(
        attributeType: Attribute.Type,
        rollMethod: Dice.RollMethod
    ) : Roll.SpellDefenseRoll {
        return Roll.SpellDefenseRoll(
            attributeModifier = this.character.attributeModifier(attributeType),
            levelModifier = this.character.levelModifier(),
            rolled = Roll.Rolled(dice = Dice.Companion.of(Die.d20), rollMethod = rollMethod)
        )
    }

    fun spellDamageRoll(
        attributeType: Attribute.Type,
        damage: Dice,
        rollMethod: Dice.RollMethod
    ) : Roll.SpellDamageRoll {
        return Roll.SpellDamageRoll(
            attributeModifier = character.attributeModifier(attributeType),
            levelModifier = character.levelModifier(),
            rolled = Roll.Rolled(dice = damage, rollMethod = rollMethod)
        )
    }

    fun healRoll(
        attributeType: Attribute.Type,
        heal: Dice,
        rollMethod: Dice.RollMethod
    ) : Roll.HealRoll {
        return Roll.HealRoll(
            attributeModifier = character.attributeModifier(attributeType),
            levelModifier = character.levelModifier(),
            rolled = Roll.Rolled(dice = heal, rollMethod = rollMethod)
        )
    }

    fun weaponAttackRoll(
        attackRollModifier: Int,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.WeaponAttackRoll {
        return Roll.WeaponAttackRoll(
            attributeModifier = character.weaponAttributeModifier(),
            weaponAttackModifier = character.weaponAttackModifier(),
            actionAttackModifier = attackRollModifier,
            levelModifier = character.levelModifier(),
            rolled = Roll.Rolled(dice = Dice.Companion.of(Die.d20), rollMethod = rollMethod)
        )
    }

    fun weaponDamageRoll(
        damageRollMultiplier: Int,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Roll.WeaponDamageRoll {
        return Roll.WeaponDamageRoll(
            attributeModifier = character.weaponAttributeModifier(),
            actionDamageMultiplier = damageRollMultiplier,
            levelModifier = character.levelModifier(),
            rolled = Roll.Rolled(dice = character.weaponDamage(), rollMethod = rollMethod)
        )
    }

    fun healOverTimeRoll(
        effect: Effect.HealOverTimeEffect,
        rollMethod: Dice.RollMethod
    ) : Roll.HealOverTimeRoll {
        return Roll.HealOverTimeRoll(
            effect = effect,
            rolled = Roll.Rolled(dice = effect.healDice, rollMethod = rollMethod)
        )
    }

    fun damageOverTimeRoll(
        effect: Effect.DamageOverTimeEffect,
        rollMethod: Dice.RollMethod
    ) : Roll.DamageOverTimeRoll {
        return Roll.DamageOverTimeRoll(
            effect = effect,
            rolled = Roll.Rolled(dice = effect.damageDice, rollMethod = rollMethod)
        )
    }

    fun rollInitiative(method: Dice.RollMethod = Dice.RollMethod.Normal) : Event.InitiativeRolled {
        return Event.InitiativeRolled(
            target = this,
            updatedTarget = this,
            initiativeRoll = initiativeRoll(method)
        )
    }

    fun startAction(actionName: String, newPositionNodeId: Int, resourcesSpent: Int) : Event.ActionStarted {
        return Event.ActionStarted(
            actionName = actionName,
            target = this,
            updatedTarget = moveTo(newPositionNodeId).spendResources(resourcesSpent),
            newPositionNodeId = newPositionNodeId,
            resourcesSpent = resourcesSpent
        )
    }

    fun weaponAttackBy(
        attacker: CharacterState,
        baseDifficultyClass: Int,
        executorAttributeType: Attribute.Type,
        targetAttributeType: Attribute.Type,
        damage: Dice,
        effectsOnHit: List<Effect>,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.SpellAttackEvent {

        val spellAttackDifficultyClass = attacker.spellAttackDifficultyClass(
            attributeType = executorAttributeType,
            baseDifficultyClass = baseDifficultyClass
        )

        val spellDefenseRoll = this.spellDefenseRoll(
            attributeType = targetAttributeType,
            rollMethod = rollMethod
        )

        if (spellAttackDifficultyClass.result >= spellDefenseRoll.result) {

            val spellDamageRoll = attacker.spellDamageRoll(
                attributeType = executorAttributeType,
                damage = damage,
                rollMethod = rollMethod
            )

            val effectsRemovedByDamage = effectsToRemoveByDamage()
            val effectsAddedByDamage = effectsToAddByDamage(spellDamageRoll.result, effectsOnHit)

            return Event.SpellAttackHit(
                target = this,
                updatedTarget = this
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
                target = this,
                updatedTarget = this,
                spellDefenseRoll = spellDefenseRoll,
                spellAttackDifficultyClass = spellAttackDifficultyClass
            )
        }
    }

    fun healBy(
        healer: CharacterState,
        executorAttributeType: Attribute.Type,
        heal: Dice,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.Healed {

        val healRoll = healer.healRoll(
            attributeType = executorAttributeType,
            heal = heal,
            rollMethod = rollMethod
        )

        return Event.Healed(
            target = this,
            updatedTarget = this.heal(healRoll.result),
            healRoll = healRoll
        )
    }

    fun weaponAttackBy(
        attacker: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int,
        effectsOnHit: List<Effect>,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Event.WeaponAttackEvent {

        val armorClass = this.armorClass()

        val weaponAttackRoll = attacker.weaponAttackRoll(
            attackRollModifier = attackRollModifier,
            rollMethod = rollMethod
        )

        if (weaponAttackRoll.result >= armorClass.result) {

            val weaponDamageRoll = attacker.weaponDamageRoll(
                damageRollMultiplier = damageRollMultiplier,
                rollMethod = rollMethod
            )

            val effectsRemovedByDamage = effectsToRemoveByDamage()
            val effectsAddedByDamage = effectsToAddByDamage(weaponDamageRoll.result, effectsOnHit)

            return Event.WeaponAttackHit(
                target = this,
                updatedTarget = this
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
                target = this,
                updatedTarget = this,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }

    fun endAction(rollMethod: Dice.RollMethod = Dice.RollMethod.Normal): Event.ActionEnded {
        val healOverTimeRolls =
            effects.healOverTimeEffects.map { this.healOverTimeRoll(effect = it, rollMethod = rollMethod) }
        val damageOverTimeRolls =
            effects.damageOverTimeEffects.map { this.damageOverTimeRoll(effect = it, rollMethod = rollMethod) }

        val removedEffects = effects.all().filter { it.tick() == null }
        val updatedEffects = effects.all().mapNotNull { it.tick() }

        return Event.ActionEnded(
            target = this,
            updatedTarget = this
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

    fun boostResources(amount: Int) : Event.ResourceBoosted {
        return Event.ResourceBoosted(
            target = this,
            updatedTarget = this.gainResources(amount),
            amount = amount
        )
    }

    fun addEffect(effect: Effect) : Event.EffectAdded {
        return Event.EffectAdded(
            target = this,
            updatedTarget = this.addEffects(listOf(effect)),
            effect = effect
        )
    }

    fun removeEffect(effect: Effect) : Event.EffectRemoved {
        return Event.EffectRemoved(
            target = this,
            updatedTarget = this.removeEffects(listOf(effect)),
            effect = effect
        )
    }
}