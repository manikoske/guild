package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
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

        fun noEffects() : Effects {
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

    fun isDying() : Boolean {
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

    private fun currentHitPoints() : Int {
        return character.maxHitPoints() - damageTaken
    }

    private fun currentResources() : Int {
        return character.maxResources() - resourcesSpent
    }

    private fun takeDamage(damageToTake: Int) : CharacterState {
        return this.copy(damageTaken = min(character.maxHitPoints(), damageTaken + damageToTake))
    }

    private fun addEffects(addedEffects: List<Effect>) : CharacterState {
        return this.copy(effects = addedEffects.fold(effects) { effects: Effects, effect: Effect -> effects.add(effect)})
    }

    private fun removeEffects(removedEffects: List<Effect>) : CharacterState {
        return this.copy(effects = removedEffects.fold(effects) { effects: Effects, effect: Effect -> effects.remove(effect)})
    }

    private fun heal(amountToHeal: Int) : CharacterState {
        return this.copy(damageTaken = max(0, damageTaken - amountToHeal))
    }

    private fun spendResources(amount: Int) : CharacterState {
        return this.copy(resourcesSpent = min(character.maxResources(), resourcesSpent + amount))
    }

    private fun gainResources(amount: Int) : CharacterState {
        return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
    }

    private fun moveTo(newPositionNodeIde: Int) : CharacterState {
        return this.copy(positionNodeId = newPositionNodeIde)
    }

    fun allExecutableActions() : List<Action> {
        return effects.actionForcingEffect?.forcedAction().let { forcedAction ->
            if (forcedAction == null) {
                Action.Actions.basicActions.filter { canExecuteAction(it) }
            } else {
                listOf(forcedAction)
            }
        }
    }

    fun canMoveBy(actionMovement: Movement): Movement {
        val finalMovement = (effects.movementRestrictingEffect?.restrictActionMovement(actionMovement) ?: actionMovement)
            .let { if (it.amount > 0) effects.movementAlteringEffects.fold(it) { a, b -> b.alterActionMovement(a) } else it }

        return finalMovement
    }

    private fun canExecuteAction(eventualAction: Action): Boolean {
        val noActionRestrictionEffect = effects.actionRestrictingEffects.none { it.restrictedAction(eventualAction) }
        val classRestriction = eventualAction.classRestriction.contains(character.clazz())
        val resourceRestriction = eventualAction.resourceCost == 0 || eventualAction.resourceCost < currentResources()
        return noActionRestrictionEffect && classRestriction && resourceRestriction
    }

    enum class Allegiance {
        Attacker, Defender
    }


    data class ArmorClass(
        val armorModifier: Int,
        val armsModifier: Int,
        val levelModifier: Int,
        val armorAttributeModifier: Int
    ) {
        val armorClass = armorModifier + armsModifier + levelModifier + armorAttributeModifier
    }

    data class WeaponAttackRoll(
        val weaponAttributeModifier: Int,
        val weaponAttackModifier : Int,
        val actionAttackModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val attack = roll.rolled + weaponAttackModifier + weaponAttackModifier + actionAttackModifier + levelModifier
    }

    data class WeaponDamageRoll(
        val weaponAttributeModifier: Int,
        val actionDamageMultiplier: Int,
        val levelModifier: Int,
        val roll : Die.Roll,
    ) {
        val damage = roll.rolled * actionDamageMultiplier + weaponAttributeModifier + levelModifier
    }



    data class DamageOverTimeRoll(
        val category: Effect.Category,
        val roll : Die.Roll
    )

    data class HealOverTimeRoll(
        val category: Effect.Category,
        val roll : Die.Roll
    )

    data class SpellAttackDifficultyClass(
        val spellAttributeModifier: Int,
        val spellDifficultyClass: Int,
        val levelModifier: Int,
    ) {
        val attack = spellAttributeModifier + spellDifficultyClass + levelModifier
    }

    data class SpellDefenseRoll(
        val spellAttributeModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val defense = roll.rolled + spellAttributeModifier + levelModifier
    }

    data class SpellDamageRoll(
        val spellAttributeModifier: Int,
        val levelModifier: Int,
        val roll : Die.Roll,
    ) {
        val damage = roll.rolled + spellAttributeModifier + levelModifier
    }

    data class HealRoll(
        val healAttributeModifier: Int,
        val levelModifier: Int,
        val roll : Die.Roll,
    ) {
        val heal = roll.rolled + healAttributeModifier + levelModifier
    }


    sealed interface Outcome {

    }

    data class EffectsTicked(
        val updatedTarget: CharacterState,
        val removedEffects: List<Effect>,
        val updatedEffects: List<Effect>,
        val damageOverTimeRolls: List<DamageOverTimeRoll>,
        val healOverTimeRolls: List<HealOverTimeRoll>,
    ) : Outcome

    sealed interface WeaponAttackOutcome : Outcome

    data class WeaponAttackHit(
        val updatedTarget: CharacterState,
        val armorClass: ArmorClass,
        val weaponAttackRoll: WeaponAttackRoll,
        val weaponDamageRoll: WeaponDamageRoll,
        val effectsRemovedByDamage : List<Effect>,
        val effectsAddedByHit: List<Effect>

    ) : WeaponAttackOutcome

    data class WeaponAttackMiss(
        val updatedTarget: CharacterState,
        val armorClass: ArmorClass,
        val weaponAttackRoll: WeaponAttackRoll,
    ) : WeaponAttackOutcome

    fun attackedBy() {
        TODO()
    }

    fun useAction() {
        TODO()
        // move, spend
    }

    fun tickEffects() : EffectsTicked {
        val healOverTimeRolls =
            effects.healOverTimeEffects.map { HealOverTimeRoll(category = it.category, roll = Die.Roll(it.healDice)) }
        val damageOverTimeRolls =
            effects.damageOverTimeEffects.map { DamageOverTimeRoll(category = it.category, roll = Die.Roll(it.damageDice)) }

        val removedEffects = effects.all().filter { it.tick() == null }
        val updatedEffects = effects.all().mapNotNull { it.tick() }

        return EffectsTicked(
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

    fun attackedBy(
        attacker: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int,
        effectsOnHit: List<Effect>
    ) : WeaponAttackOutcome {

        val armorClass = ArmorClass(
            armorModifier = character.armorClassArmorModifier(),
            armsModifier = character.armorClassArmsModifier(),
            levelModifier = character.levelModifier(),
            armorAttributeModifier = character.armorLimitedDexterityModifier()
        )

        val weaponAttackRoll = WeaponAttackRoll(
            weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
            weaponAttackModifier = attacker.character.weaponAttackModifier(),
            actionAttackModifier = attackRollModifier,
            levelModifier = attacker.character.levelModifier(),
            roll = Die.Roll(Die.Dice.of(Die.d20))
        )

        if (weaponAttackRoll.attack >= armorClass.armorClass) {

            val weaponDamageRoll = WeaponDamageRoll(
                weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
                actionDamageMultiplier = damageRollMultiplier,
                levelModifier = attacker.character.levelModifier(),
                roll = Die.Roll(attacker.character.weaponDamage())
            )

            val effectsRemovedByDamage = effects.all().filter { it.removeOnDamageTaken() }
            val effectsAddedByHit = effectsOnHit + if (weaponDamageRoll.damage >= currentHitPoints()) listOf(Effect.ActionForcingEffect.Dying(0)) else listOf()

            return WeaponAttackHit(
                updatedTarget = this
                    .takeDamage(weaponDamageRoll.damage)
                    .removeEffects(effectsRemovedByDamage)
                    .addEffects(effectsAddedByHit),
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass,
                weaponDamageRoll = weaponDamageRoll,
                effectsRemovedByDamage = effectsRemovedByDamage,
                effectsAddedByHit = effectsOnHit
            )
        } else {
            return WeaponAttackMiss(
                updatedTarget = this,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }
}
