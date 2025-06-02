package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.character.Character
import com.manikoske.guild.rules.Die
import java.util.logging.Logger
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

    fun currentHitPoints() : Int {
        return character.maxHitPoints() - damageTaken
    }

    private fun currentResources() : Int {
        return character.maxResources() - resourcesSpent
    }

    fun takeDamage(damageToTake: Int) : CharacterState {
        if (damageToTake == 0) return this
        val damageTakenUpdated = min(character.maxHitPoints(), damageTaken + damageToTake)
        return this.copy(damageTaken = damageTakenUpdated)
    }

    fun addEffect(effect: Effect) : CharacterState {
        return this.copy(effects = effects.add(effect))
    }

    fun removeEffect(effect: Effect) : CharacterState {
        return this.copy(effects = effects.remove(effect))
    }

    fun tickEffects() : CharacterState {
        return this.copy(effects = effects.tick())
    }

    fun heal(amountToHeal: Int) : CharacterState {
        if (amountToHeal == 0) return this
        return this.copy(damageTaken = max(0, damageTaken - amountToHeal))
    }

    fun spendResources(amount: Int) : CharacterState {
        if (amount == 0) return this
        return this.copy(resourcesSpent = min(character.maxResources(), resourcesSpent + amount))
    }

    fun gainResources(amount: Int) : CharacterState {
        if (amount == 0) return this
        return this.copy(resourcesSpent = max(0, resourcesSpent - amount))
    }

    fun moveTo(newPositionNodeIde: Int) : CharacterState {
        return this.copy(positionNodeId = newPositionNodeIde)
    }

    fun applyOverTimeEffects() : CharacterState {
        return heal(effects.healOverTimeEffects.sumOf { it.heal().roll() })
            .takeDamage(effects.damageOverTimeEffects.sumOf { it.damage().roll() })
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
        val value = armorModifier + armsModifier + levelModifier + armorAttributeModifier
    }

    data class WeaponAttackRoll(
        val weaponAttributeModifier: Int,
        val weaponAttackModifier : Int,
        val actionAttackModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val value = roll.rolled + weaponAttackModifier + weaponAttackModifier + actionAttackModifier + levelModifier
    }

    data class WeaponDamageRoll(
        val weaponAttributeModifier: Int,
        val actionDamageMultiplier: Int,
        val levelModifier: Int,
        val roll : Die.Roll,
    ) {
        val value = roll.rolled * actionDamageMultiplier + weaponAttributeModifier + levelModifier
    }

    data class SpellAttackDifficultyClass(
        val spellAttributeModifier: Int,
        val spellDifficultyClass: Int,
        val levelModifier: Int,
    ) {
        val value = spellAttributeModifier + spellDifficultyClass + levelModifier
    }

    data class SpellDefenseRoll(
        val spellAttributeModifier: Int,
        val levelModifier: Int,
        val roll: Die.Roll,
    ) {
        val value = roll.rolled + spellAttributeModifier + levelModifier
    }

    data class SpellDamageRoll(
        val spellAttributeModifier: Int,
        val levelModifier: Int,
        val roll : Die.Roll,
    ) {
        val value = roll.rolled + spellAttributeModifier + levelModifier
    }

    data class HealRoll(
        val healAttributeModifier: Int,
        val levelModifier: Int,
        val roll : Die.Roll,
    ) {
        val value = roll.rolled + healAttributeModifier + levelModifier
    }


    sealed interface Outcome {

    }
    sealed interface WeaponAttackOutcome : Outcome
    data class WeaponAttackHit(
        val updatedCharacterState: CharacterState,
        val armorClass: ArmorClass,
        val weaponAttackRoll: WeaponAttackRoll,
        val weaponDamageRoll: WeaponDamageRoll

    ) : WeaponAttackOutcome

    data class WeaponAttackMiss(
        val updatedCharacterState: CharacterState,
        val armorClass: ArmorClass,
        val weaponAttackRoll: WeaponAttackRoll,
        val removedEffects : List<Effect>,
        val addedEffects: List<Effect>
    ) : WeaponAttackOutcome

    fun spellAttackBy() {
        TODO()
    }

    fun actionUsed() {
        TODO()
        // move, spend
    }

    fun applyOvertimeEffects() {
        TODO()

    }

    fun tickEffects() {
        TODO()
    }

    fun weaponAttackBy(
        attacker: CharacterState,
        attackRollModifier: Int,
        damageRollMultiplier: Int

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

        if (weaponAttackRoll.value >= armorClass.value) {

            val weaponDamageRoll = WeaponDamageRoll(
                weaponAttributeModifier = attacker.character.weaponAttributeModifier(),
                actionDamageMultiplier = damageRollMultiplier,
                levelModifier = attacker.character.levelModifier(),
                roll = Die.Roll(attacker.character.weaponDamage())
            )

            this.takeDamage(weaponDamageRoll.value)


            result.add(Event.WeaponDamageDealt(weaponDamageRoll = weaponDamageRoll))
            result.addAll(onDamageDealt(damageDealt = weaponDamageRoll.value, target = target))
            result.addAll(resolveEffect(effect))

            return WeaponAttackHit(
                updatedCharacterState = todo,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass,
                weaponDamageRoll = weaponDamageRoll
            )
        } else {
            return WeaponAttackMiss(
                updatedCharacterState = this,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }
}
