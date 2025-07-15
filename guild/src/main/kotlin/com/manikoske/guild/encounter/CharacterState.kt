package com.manikoske.guild.encounter

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.log.AnsiColorUtil
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

    fun rollInitiative() : Event.InitiativeRolled {
        return Event.InitiativeRolled(
            target = this,
            updatedTarget = this,
            initiativeRoll = Event.InitiativeRoll(
                initiativeAttributeModifier = this.character.attributeModifier(Attribute.Type.dexterity),
                levelModifier = this.character.levelModifier(),
                roll = Die.Roll(Die.Dice.of(Die.d20))
            )
        )
    }

    fun startAction(newPositionNodeId: Int, resourcesSpent: Int) : Event.ActionStarted {
        return Event.ActionStarted(
            target = this,
            updatedTarget = moveTo(newPositionNodeId).spendResources(resourcesSpent),
            newPositionNodeId = newPositionNodeId,
            resourcesSpent = resourcesSpent
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
                target = this,
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
        heal: Die.Dice
    ): Event.Healed {

        val healRoll = Event.HealRoll(
            healAttributeModifier = healer.character.attributeModifier(executorAttributeType),
            levelModifier = healer.character.levelModifier(),
            roll = Die.Roll(heal)
        )

        return Event.Healed(
            target = this,
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
                target = this,
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
                target = this,
                updatedTarget = this,
                weaponAttackRoll = weaponAttackRoll,
                armorClass = armorClass
            )
        }
    }

    fun endAction(): Event.ActionEnded {
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

        return Event.ActionEnded(
            target = this,
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
            target = this,
            updatedTarget = this.gainResources(amount),
            amount = amount
        )
    }

    fun addEffect(effect: Effect) : Event.EffectAdded {
        return Event.EffectAdded(
            target = this,
            updatedTarget = this.addEffects(listOf(effect)),
            category = effect.category
        )
    }

    fun removeEffect(effect: Effect) : Event.EffectRemoved {
        return Event.EffectRemoved(
            target = this,
            updatedTarget = this.removeEffects(listOf(effect)),
            category = effect.category
        )
    }

    /**
     * Returns a brief one-line representation of character state
     */
    fun displayBrief(): String {
        val effectSymbols = effects.all().map { AnsiColorUtil.getEffectSymbol(it.category) }.joinToString(" ")
        val statusPrefix = when {
            isDying() -> AnsiColorUtil.bgRed("DYING")
            else -> AnsiColorUtil.bgGreen("${allegiance.name}")
        }

        val healthDisplay = AnsiColorUtil.formatHitPoints(currentHitPoints(), character.maxHitPoints())
        val resourceDisplay = AnsiColorUtil.formatResources(currentResources(), character.maxResources())

        return "$statusPrefix ${AnsiColorUtil.bold(character.bio.name)} [$healthDisplay | $resourceDisplay] ${if (effectSymbols.isNotEmpty()) "⟪$effectSymbols⟫" else ""}"
    }

    /**
     * Returns a detailed multi-line representation of character state
     */
    fun displayDetailed(): String {
        val sb = StringBuilder()

        // Header with name and class
        sb.appendLine(AnsiColorUtil.header("─── CHARACTER STATE: ${character.bio.name} (${character.clazz().name}) ───"))

        // Basic stats
        sb.appendLine("${AnsiColorUtil.bold("Position:")} Node $positionNodeId    ${AnsiColorUtil.bold("Side:")} ${allegiance.name}")
        sb.appendLine("${AnsiColorUtil.bold("Health:")} ${AnsiColorUtil.formatHitPoints(currentHitPoints(), character.maxHitPoints())}    ${AnsiColorUtil.bold("Utility:")} ${String.format("%.2f", utility())}")
        sb.appendLine("${AnsiColorUtil.bold("Resources:")} ${AnsiColorUtil.formatResources(currentResources(), character.maxResources())}")

        // Character stats
        sb.appendLine("\n${AnsiColorUtil.bold("Character Stats:")}")
        sb.appendLine("${AnsiColorUtil.bold("Level:")} ${character.levelModifier() * 2}    ${AnsiColorUtil.bold("Armor Class:")} ${character.armorClassArmorModifier() + character.armorClassArmsModifier() + character.armorLimitedDexterityModifier() + character.levelModifier()}")

        // Attributes
        sb.appendLine("\n${AnsiColorUtil.bold("Attributes:")}")
        val strMod = AnsiColorUtil.formatModifier(character.attributeModifier(Attribute.Type.strength))
        val dexMod = AnsiColorUtil.formatModifier(character.attributeModifier(Attribute.Type.dexterity))
        val conMod = AnsiColorUtil.formatModifier(character.attributeModifier(Attribute.Type.constitution))
        val intMod = AnsiColorUtil.formatModifier(character.attributeModifier(Attribute.Type.intelligence))
        val wisMod = AnsiColorUtil.formatModifier(character.attributeModifier(Attribute.Type.wisdom))
        val chaMod = AnsiColorUtil.formatModifier(character.attributeModifier(Attribute.Type.charisma))
        sb.appendLine("STR: $strMod  DEX: $dexMod  CON: $conMod  INT: $intMod  WIS: $wisMod  CHA: $chaMod")

        // Weapon info
        val arms = character.arms()
        sb.appendLine("\n${AnsiColorUtil.bold("Weapons:")}")
        when (arms) {
            is com.manikoske.guild.inventory.Inventory.Arms.DualWeapon -> {
                sb.appendLine("Dual-wielding: ${arms.mainHand.name} & ${arms.offHand.name}")
            }
            is com.manikoske.guild.inventory.Inventory.Arms.OneHandedWeaponAndShield -> {
                sb.appendLine("One-handed weapon and shield: ${arms.mainHand.name}")
            }
            is com.manikoske.guild.inventory.Inventory.Arms.TwoHandedWeapon -> {
                sb.appendLine("Two-handed weapon: ${arms.bothHands.name}")
            }
            is com.manikoske.guild.inventory.Inventory.Arms.RangedWeapon -> {
                sb.appendLine("Ranged weapon: ${arms.bothHands.name} (Range: ${arms.range()})")
            }
        }

        // Effects
        if (effects.all().isNotEmpty()) {
            sb.appendLine("\n${AnsiColorUtil.bold("Active Effects:")}")

            // Action forcing effects
            effects.actionForcingEffect?.let {
                val duration = if (it is Effect.TimedEffect) "(${it.roundsLeft} rounds)" else ""
                sb.appendLine("${AnsiColorUtil.getEffectSymbol(it.category)} ${it.javaClass.simpleName} $duration")
            }

            // Movement restricting effects
            effects.movementRestrictingEffect?.let {
                val duration = if (it is Effect.TimedEffect) "(${it.roundsLeft} rounds)" else ""
                sb.appendLine("${AnsiColorUtil.getEffectSymbol(it.category)} ${it.javaClass.simpleName} $duration")
            }

            // Movement altering effects
            effects.movementAlteringEffects.forEach { effect ->
                val duration = if (effect is Effect.TimedEffect) "(${effect.roundsLeft} rounds)" else ""
                sb.appendLine("${AnsiColorUtil.getEffectSymbol(effect.category)} ${effect.javaClass.simpleName} $duration")
            }

            // Action restricting effects
            effects.actionRestrictingEffects.forEach { effect ->
                val duration = if (effect is Effect.TimedEffect) "(${effect.roundsLeft} rounds)" else ""
                sb.appendLine("${AnsiColorUtil.getEffectSymbol(effect.category)} ${effect.javaClass.simpleName} $duration")
            }

            // Damage over time effects
            effects.damageOverTimeEffects.forEach { effect ->
                val duration = if (effect is Effect.TimedEffect) "(${effect.roundsLeft} rounds)" else ""
                sb.appendLine("${AnsiColorUtil.getEffectSymbol(effect.category)} ${effect.javaClass.simpleName} $duration - ${effect.damageDice}")
            }

            // Heal over time effects
            effects.healOverTimeEffects.forEach { effect ->
                val duration = if (effect is Effect.TimedEffect) "(${effect.roundsLeft} rounds)" else ""
                sb.appendLine("${AnsiColorUtil.getEffectSymbol(effect.category)} ${effect.javaClass.simpleName} $duration - ${effect.healDice}")
            }
        }

        return sb.toString()
    }
}
