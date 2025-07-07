package com.manikoske.guild.log

import com.manikoske.guild.action.Action
import com.manikoske.guild.action.Effect
import com.manikoske.guild.action.Event
import com.manikoske.guild.encounter.CharacterState
import com.manikoske.guild.encounter.Encounter
import com.manikoske.guild.encounter.Round
import com.manikoske.guild.encounter.Target
import com.manikoske.guild.encounter.Turn

/**
 * An enhanced logger that provides detailed information about combat events
 * with rich formatting and complete information about characters and actions
 */
object DetailedCombatLogger {
    private var verbosityLevel = VerbosityLevel.NORMAL

    enum class VerbosityLevel {
        MINIMAL, // Only essential information
        NORMAL,  // Standard detail level
        VERBOSE, // Maximum detail
        TACTICAL // Focus on combat mechanics and decision-making
    }

    fun configure(
        verbosityLevel: VerbosityLevel = VerbosityLevel.NORMAL
    ) {
        this.verbosityLevel = verbosityLevel
    }

    // Color formatting methods that delegate to AnsiColorUtil
    private fun bold(text: String): String = AnsiColorUtil.bold(text)
    private fun italic(text: String): String = AnsiColorUtil.italic(text)
    private fun red(text: String): String = AnsiColorUtil.red(text)
    private fun green(text: String): String = AnsiColorUtil.green(text)
    private fun yellow(text: String): String = AnsiColorUtil.yellow(text)
    private fun blue(text: String): String = AnsiColorUtil.blue(text)
    private fun purple(text: String): String = AnsiColorUtil.purple(text)
    private fun cyan(text: String): String = AnsiColorUtil.cyan(text)
    private fun white(text: String): String = AnsiColorUtil.white(text)
    private fun bgRed(text: String): String = AnsiColorUtil.bgRed(text)
    private fun bgGreen(text: String): String = AnsiColorUtil.bgGreen(text)
    private fun bgBlue(text: String): String = AnsiColorUtil.bgBlue(text)
    private fun header(text: String): String = AnsiColorUtil.header(text)
    private fun colorize(text: String, color: String): String = AnsiColorUtil.colorize(text, color)
    private fun formatModifier(value: Int): String = AnsiColorUtil.formatModifier(value)

    // Main logging functions
    fun logEncounter(encounter: Encounter.State) {

        val attackers = encounter.updatedCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Attacker }
        val defenders = encounter.updatedCharacterStates.filter { it.allegiance == CharacterState.Allegiance.Defender }

        println("\n${header(" ⚔️  ENCOUNTER SUMMARY ⚔️  ").padEnd(80, ' ')}")

        // Print battle statistics
        val totalRounds = encounter.rounds.size
        val totalTurns = encounter.rounds.sumOf { it.turns.size }
        val totalDamageDealt = calculateTotalDamage(encounter.rounds)
        val totalHealing = calculateTotalHealing(encounter.rounds)

        println("\n${bold("Battle Statistics:")}") 
        println("  ${cyan("Total Rounds:")} $totalRounds")
        println("  ${cyan("Total Turns:")} $totalTurns")
        println("  ${red("Total Damage Dealt:")} $totalDamageDealt")
        println("  ${green("Total Healing:")} $totalHealing")

        // Print final character states
        println("\n${bold("Final Character States:")}")
        printTeamSummary("Attackers", attackers, AnsiColorUtil.BLUE)
        printTeamSummary("Defenders", defenders, AnsiColorUtil.RED)

        // Print battle outcome
        val attackersAlive = attackers.any { !it.isDying() }
        val defendersAlive = defenders.any { !it.isDying() }

        println("\n${bold("Battle Outcome:")}")
        when {
            attackersAlive && !defendersAlive -> println("  ${bgGreen(" ATTACKERS VICTORIOUS ")}")
            !attackersAlive && defendersAlive -> println("  ${bgRed(" DEFENDERS VICTORIOUS ")}")
            else -> println("  ${bgBlue(" BATTLE ENDED IN DRAW OR TIMEOUT ")}")
        }

        // Print detailed round information if verbosity level allows
        if (verbosityLevel != VerbosityLevel.MINIMAL) {
            println("\n${header(" DETAILED BATTLE LOG ").padEnd(80, ' ')}")
            // Rounds are logged separately by StatelessLoggingManager
        }
    }

    private fun calculateTotalDamage(rounds: List<Round.State>): Int {
        var damage = 0
        rounds.forEach { round ->
            round.turns.forEach { turn ->
                turn.outcome.targetEvents.forEach { event ->
                    when (event) {
                        is Event.WeaponAttackHit -> damage += event.weaponDamageRoll.damage
                        is Event.SpellAttackHit -> damage += event.spellDamageRoll.damage
                        else -> {}
                    }
                }
                // Include DoT damage
                damage += turn.effectsTicked.damageOverTimeRolls.sumOf { it.roll.rolled }
            }
        }
        return damage
    }

    private fun calculateTotalHealing(rounds: List<Round.State>): Int {
        var healing = 0
        rounds.forEach { round ->
            round.turns.forEach { turn ->
                turn.outcome.targetEvents.forEach { event ->
                    if (event is Event.Healed) healing += event.healRoll.heal
                }
                // Include HoT healing
                healing += turn.effectsTicked.healOverTimeRolls.sumOf { it.roll.rolled }
            }
        }
        return healing
    }

    private fun printTeamSummary(teamName: String, characters: List<CharacterState>, teamColor: String) {
        println("  ${colorize(teamName, teamColor)}:")
        characters.forEach { character ->
            val healthStatus = if (character.isDying()) {
                red("[DEAD]")
            } else {
                // Character is already a CharacterState, so we don't need to cast it
                // We can't directly access damageTaken as it's private, so use the utility methods
                val currentHp = character.character.maxHitPoints() - (character.character.maxHitPoints() - character.currentHitPoints())
                val maxHp = character.character.maxHitPoints()
                val healthPercentage = (currentHp.toDouble() / maxHp.toDouble() * 100).toInt()
                when {
                    healthPercentage > 75 -> green("[HP: $currentHp/$maxHp]")
                    healthPercentage > 35 -> yellow("[HP: $currentHp/$maxHp]")
                    else -> red("[HP: $currentHp/$maxHp]")
                }
            }

            // Use currentResources() method instead of trying to access private field
            val currentResources = character.currentResources()
            val maxResources = character.character.maxResources()
            val resourcesStatus = blue("[Resources: $currentResources/$maxResources]")

            val effectsText = if (character.effects.all().isNotEmpty()) {
                val formattedEffects = character.effects.all().joinToString(", ") { formatEffect(it) }
                "${yellow("Effects:")} $formattedEffects"
            } else {
                ""
            }

            println("    ${bold(character.character.bio.name)} $healthStatus $resourcesStatus")

            if (effectsText.isNotEmpty()) {
                println("      $effectsText")
            }

            if (verbosityLevel == VerbosityLevel.VERBOSE) {
                printCharacterDetails(character)
            }
        }
    }

    private fun printCharacterDetails(character: CharacterState) {
        val bio = character.character.bio
        val attributes = listOf<Pair<String, String>>(
            "STR: ${bio.strength.score}(${formatModifier(bio.strength.modifier())})" to AnsiColorUtil.RED,
            "DEX: ${bio.dexterity.score}(${formatModifier(bio.dexterity.modifier())})" to AnsiColorUtil.GREEN,
            "CON: ${bio.constitution.score}(${formatModifier(bio.constitution.modifier())})" to AnsiColorUtil.YELLOW,
            "INT: ${bio.intelligence.score}(${formatModifier(bio.intelligence.modifier())})" to AnsiColorUtil.BLUE,
            "WIS: ${bio.wisdom.score}(${formatModifier(bio.wisdom.modifier())})" to AnsiColorUtil.PURPLE,
            "CHA: ${bio.charisma.score}(${formatModifier(bio.charisma.modifier())})" to AnsiColorUtil.CYAN
        )

        val formattedAttributes = attributes.joinToString(" ") { (text, color) -> AnsiColorUtil.colorize(text, color) }
        val armorClass = character.character.armorClassArmorModifier() + 
                         character.character.armorClassArmsModifier() + 
                         character.character.levelModifier() + 
                         character.character.armorLimitedDexterityModifier()
        val armorText = "AC: $armorClass"
        val classText = "Class: ${character.character.clazz()}"
        val nodeText = "Position: Node ${character.positionNodeId}"

        println("      ${italic("$classText | $armorText | $nodeText")}")
        println("      $formattedAttributes")
    }


    /**
     * Log initiative rolls separately to ensure they appear before turn logs
     */
    fun logInitiative(initiativeRolls: List<Event.InitiativeRolled>) {

        println("${bold("Initiative Order:")}")
        initiativeRolls.forEachIndexed { index, roll ->
            val character = roll.updatedTarget.character
            val initiative = roll.initiativeRoll.initiative
            val diceRoll = roll.initiativeRoll.roll.rolled
            val dexMod = roll.initiativeRoll.initiativeAttributeModifier
            val levelMod = roll.initiativeRoll.levelModifier

            println("  ${index + 1}. ${character.bio.name}: ${yellow(initiative.toString())} (d20: $diceRoll, DEX: ${formatModifier(dexMod)}, Level: ${formatModifier(levelMod)})")
        }
    }

    fun logRound(round: Round.State) {

        println("\n${bold("==== ROUND ${round.sequence} ====")}")

        // Log each turn with detailed information (initiative is now logged separately)
        round.turns.forEach { logTurn(it) }
    }

    fun logTurn(turn: Turn.State) {

        val actor = turn.actionTaken.updatedTarget.character
        val actionName = turn.action.name
        val targetInfo = formatTargetInfo(turn.target)
        val utility = "%.2f".format(turn.utility)

        println("\n${bold("${actor.bio.name}'s Turn:")}")
        println("  ${purple("Action:")} ${bold(actionName)} → $targetInfo")

        if (verbosityLevel == VerbosityLevel.TACTICAL) {
            println("  ${italic("Utility Score:")} $utility")
        }

        // Log movement and resource cost
        val fromNode = turn.actionTaken.updatedTarget.positionNodeId
        val toNode = turn.actionTaken.newPositionNodeId
        val resourceCost = turn.actionTaken.resourceCost

        if (fromNode != toNode) {
            println("  ${blue("Movement:")} Node $fromNode → Node $toNode")
        }

        if (resourceCost > 0) {
            println("  ${cyan("Resource Cost:")} $resourceCost")
        }

        // Log detailed action information based on type
        if (verbosityLevel == VerbosityLevel.VERBOSE || verbosityLevel == VerbosityLevel.TACTICAL) {
            logActionDetails(turn.action)
        }

        // Log outcome events with enhanced details
        if (turn.outcome.selfEvent != null) {
            println("  ${cyan("Self Effect:")}")
            println("    ${formatDetailedEvent(turn.outcome.selfEvent)}")
        }

        if (turn.outcome.targetEvents.isNotEmpty()) {
            println("  ${red("Target Effects:")}")
            turn.outcome.targetEvents.forEach { event ->
                println("    ${formatDetailedEvent(event)}")
            }
        }

        // Log effects ticked with enhanced details
        val effectsTicked = turn.effectsTicked
        if (effectsTicked.removedEffects.isNotEmpty() || effectsTicked.updatedEffects.isNotEmpty() || 
            effectsTicked.damageOverTimeRolls.isNotEmpty() || effectsTicked.healOverTimeRolls.isNotEmpty()) {

            println("  ${yellow("Effects Processed:")}")

            if (effectsTicked.removedEffects.isNotEmpty()) {
                println("    ${red("Expired:")} ${effectsTicked.removedEffects.joinToString(", ") { formatEffect(it) }}")
            }

            if (effectsTicked.updatedEffects.isNotEmpty()) {
                println("    ${green("Continuing:")} ${effectsTicked.updatedEffects.joinToString(", ") { formatEffect(it) }}")
            }

            effectsTicked.damageOverTimeRolls.forEach { roll ->
                println("    ${red("DoT Damage (${formatCategory(roll.category)}):")} ${roll.roll.rolled} damage")
            }

            effectsTicked.healOverTimeRolls.forEach { roll ->
                println("    ${green("HoT Healing (${formatCategory(roll.category)}):")} ${roll.roll.rolled} healing")
            }
        }
    }

    private fun logActionDetails(action: Action) {
        // Show the action's movement type and amount
        println("  ${blue("Action Details:")}")
        println("    ${italic("Movement:")} ${action.movement.type} (${action.movement.amount})")
        println("    ${italic("Classes:")} ${action.classRestriction.joinToString(", ")}")

        // Show resolution details based on action type
        when (action) {
            is Action.TargetedAction.AttackAction.WeaponAttack.WeaponSingleAttack -> {
                val resolution = action.resolution
                println("    ${italic("Attack Modifier:")} ${formatModifier(resolution.attackRollModifier)}")
                println("    ${italic("Damage Multiplier:")} ${resolution.damageRollMultiplier}x")
            }
            is Action.TargetedAction.AttackAction.WeaponAttack.WeaponDoubleAttack -> {
                val resolution = action.resolution
                println("    ${italic("Attack Modifier:")} ${formatModifier(resolution.attackRollModifier)}")
                println("    ${italic("Damage Multiplier:")} ${resolution.damageRollMultiplier}x")
            }
            is Action.TargetedAction.AttackAction.WeaponAttack.WeaponNodeAttack -> {
                val resolution = action.resolution
                println("    ${italic("Attack Modifier:")} ${formatModifier(resolution.attackRollModifier)}")
                println("    ${italic("Damage Multiplier:")} ${resolution.damageRollMultiplier}x")
            }
            is Action.TargetedAction.AttackAction.SpellAttack.SpellSingleAttack -> {
                val resolution = action.resolution
                println("    ${italic("Spell DC:")} ${resolution.baseDifficultyClass}")
                println("    ${italic("Range:")} ${action.range}")
                println("    ${italic("Executor Attribute:")} ${resolution.executorAttributeType}")
                println("    ${italic("Target Attribute:")} ${resolution.targetAttributeType}")
            }
            is Action.TargetedAction.AttackAction.SpellAttack.SpellDoubleAttack -> {
                val resolution = action.resolution
                println("    ${italic("Spell DC:")} ${resolution.baseDifficultyClass}")
                println("    ${italic("Range:")} ${action.range}")
                println("    ${italic("Executor Attribute:")} ${resolution.executorAttributeType}")
                println("    ${italic("Target Attribute:")} ${resolution.targetAttributeType}")
            }
            is Action.TargetedAction.AttackAction.SpellAttack.SpellNodeAttack -> {
                val resolution = action.resolution
                println("    ${italic("Spell DC:")} ${resolution.baseDifficultyClass}")
                println("    ${italic("Range:")} ${action.range}")
                println("    ${italic("Executor Attribute:")} ${resolution.executorAttributeType}")
                println("    ${italic("Target Attribute:")} ${resolution.targetAttributeType}")
            }
            is Action.TargetedAction.SupportAction -> {
                // Show support details if they are accessible
                println("    ${italic("Support Action Type:")} ${action.name}")
                // Additional details could be shown here if resolution details are accessible
            }
            is Action.NoResolutionAction -> {
                println("    ${italic("No Outcome Action:")} ${action.name}")
            }
            else -> {
                println("    ${italic("Action Type:")} ${action.javaClass.simpleName}")
            }
        }
    }

    private fun formatTargetInfo(target: Target): String {
        return when (target) {
            is Target.Self -> "Self"
            is Target.SingleAlly -> {
                val ally = target.targetedCharacterStates.first()
                "Ally ${bold(ally.character.bio.name)} (range: ${target.range})"
            }
            is Target.SingleEnemy -> {
                val enemy = target.targetedCharacterStates.first()
                "Enemy ${bold(enemy.character.bio.name)} (range: ${target.range})"
            }
            is Target.DoubleAlly -> {
                val allies = target.targetedCharacterStates.joinToString(", ") { bold(it.character.bio.name) }
                "Allies: $allies (range: ${target.range})"
            }
            is Target.DoubleEnemy -> {
                val enemies = target.targetedCharacterStates.joinToString(", ") { bold(it.character.bio.name) }
                "Enemies: $enemies (range: ${target.range})"
            }
            is Target.NodeAlly -> "Node Allies (range: ${target.range})"
            is Target.NodeEnemy -> "Node Enemies (range: ${target.range})"
            is Target.NodeEveryone -> "Everyone in node (range: ${target.range})"
        }
    }

    /**
     * Format a category name consistently
     */
    private fun formatCategory(category: Effect.Category): String {
        return when (category) {
            is Effect.ActionForcingEffect.Category -> category.toString()
            is Effect.ActionRestrictingEffect.Category -> category.toString()
            is Effect.MovementAlteringEffect.Category -> category.toString()
            is Effect.MovementRestrictingEffect.Category -> category.toString()
            is Effect.DamageOverTimeEffect.Category -> category.toString()
            is Effect.HealOverTimeEffect.Category -> category.toString()
            else -> "Unknown"
        }
    }

    private fun formatEffect(effect: Effect): String {
        val detailText = EventFormatter.formatEffectDetails(effect)

        return when (effect) {
            is Effect.ActionForcingEffect -> red(detailText)
            is Effect.ActionRestrictingEffect -> yellow(detailText)
            is Effect.MovementAlteringEffect -> blue(detailText)
            is Effect.MovementRestrictingEffect -> purple(detailText)
            is Effect.DamageOverTimeEffect -> red(detailText)
            is Effect.HealOverTimeEffect -> green(detailText)
            else -> detailText
        }
    }

    private fun formatDetailedEvent(event: Event): String {
        val baseSummary = EventFormatter.formatEventSummary(event)
        val targetName = bold(event.updatedTarget.character.bio.name)

        // Add detailed breakdown to each event type
        return when (event) {
            is Event.WeaponAttackHit -> {
                val roll = event.weaponAttackRoll.roll.rolled
                val attrMod = formatModifier(event.weaponAttackRoll.weaponAttributeModifier)
                val weaponMod = formatModifier(event.weaponAttackRoll.weaponAttackModifier)
                val actionMod = formatModifier(event.weaponAttackRoll.actionAttackModifier)
                val levelMod = formatModifier(event.weaponAttackRoll.levelModifier)
                val totalAttack = event.weaponAttackRoll.attack
                val ac = event.armorClass.armorClass

                val damageRoll = event.weaponDamageRoll.roll.rolled
                val damageAttrMod = formatModifier(event.weaponDamageRoll.weaponAttributeModifier)
                val damageMultiplier = event.weaponDamageRoll.actionDamageMultiplier
                val damageLevelMod = formatModifier(event.weaponDamageRoll.levelModifier)
                val totalDamage = event.weaponDamageRoll.damage

                "${red("Hit")} $targetName for ${red("$totalDamage damage")}\n" +
                "      Attack: $roll (d20) + $attrMod (attr) + $weaponMod (weapon) + $actionMod (action) + $levelMod (level) = $totalAttack vs AC $ac\n" +
                "      Damage: $damageRoll (dice) × $damageMultiplier (multiplier) + $damageAttrMod (attr) + $damageLevelMod (level) = $totalDamage"
            }
            is Event.WeaponAttackMissed -> {
                val roll = event.weaponAttackRoll.roll.rolled
                val attrMod = formatModifier(event.weaponAttackRoll.weaponAttributeModifier)
                val weaponMod = formatModifier(event.weaponAttackRoll.weaponAttackModifier)
                val actionMod = formatModifier(event.weaponAttackRoll.actionAttackModifier)
                val levelMod = formatModifier(event.weaponAttackRoll.levelModifier)
                val totalAttack = event.weaponAttackRoll.attack
                val ac = event.armorClass.armorClass

                "${yellow("Missed")} $targetName\n" +
                "      Attack: $roll (d20) + $attrMod (attr) + $weaponMod (weapon) + $actionMod (action) + $levelMod (level) = $totalAttack vs AC $ac"
            }
            is Event.SpellAttackHit -> {
                val dc = event.spellAttackDifficultyClass.attack
                val defenseRoll = event.spellDefenseRoll.roll.rolled
                val defenseAttrMod = formatModifier(event.spellDefenseRoll.spellAttributeModifier)
                val defenseLevelMod = formatModifier(event.spellDefenseRoll.levelModifier)
                val totalDefense = event.spellDefenseRoll.defense

                val damageRoll = event.spellDamageRoll.roll.rolled
                val damageAttrMod = formatModifier(event.spellDamageRoll.spellAttributeModifier)
                val damageLevelMod = formatModifier(event.spellDamageRoll.levelModifier)
                val totalDamage = event.spellDamageRoll.damage

                "${purple("Spell hit")} $targetName for ${red("$totalDamage damage")}\n" +
                "      DC $dc vs Defense: $defenseRoll (d20) + $defenseAttrMod (attr) + $defenseLevelMod (level) = $totalDefense\n" +
                "      Damage: $damageRoll (dice) + $damageAttrMod (attr) + $damageLevelMod (level) = $totalDamage"
            }
            is Event.SpellAttackMissed -> {
                val dc = event.spellAttackDifficultyClass.attack
                val defenseRoll = event.spellDefenseRoll.roll.rolled
                val defenseAttrMod = formatModifier(event.spellDefenseRoll.spellAttributeModifier)
                val defenseLevelMod = formatModifier(event.spellDefenseRoll.levelModifier)
                val totalDefense = event.spellDefenseRoll.defense

                "${yellow("Spell missed")} $targetName\n" +
                "      DC $dc vs Defense: $defenseRoll (d20) + $defenseAttrMod (attr) + $defenseLevelMod (level) = $totalDefense"
            }
            is Event.Healed -> {
                val healRoll = event.healRoll.roll.rolled
                val healAttrMod = formatModifier(event.healRoll.healAttributeModifier)
                val healLevelMod = formatModifier(event.healRoll.levelModifier)
                val totalHeal = event.healRoll.heal

                "${green("Healed")} $targetName for ${green("$totalHeal HP")}\n" +
                "      Healing: $healRoll (dice) + $healAttrMod (attr) + $healLevelMod (level) = $totalHeal"
            }
            is Event.EffectAdded -> {
                "${purple("Added effect")} ${formatCategory(event.category)} to $targetName"
            }
            is Event.EffectRemoved -> {
                "${yellow("Removed effect")} ${formatCategory(event.category)} from $targetName"
            }
            is Event.ResourceBoosted -> {
                "${blue("Resource boost:")} $targetName gained ${event.amount} resources"
            }
            is Event.ActionTaken -> {
                val costText = if (event.resourceCost > 0) "used ${event.resourceCost} resources and " else ""
                "$targetName ${costText}moved to node ${event.newPositionNodeId}"
            }
            is Event.EffectsTicked -> {
                val doT = event.damageOverTimeRolls.sumOf { it.roll.rolled }
                val hoT = event.healOverTimeRolls.sumOf { it.roll.rolled }
                when {
                    doT > 0 && hoT > 0 -> "$targetName took ${red("$doT damage")} from DoTs and gained ${green("$hoT healing")} from HoTs"
                    doT > 0 -> "$targetName took ${red("$doT damage")} from DoTs"
                    hoT > 0 -> "$targetName gained ${green("$hoT healing")} from HoTs"
                    else -> "Effects processed for $targetName"
                }
            }
            is Event.InitiativeRolled -> {
                "$targetName rolled ${yellow("${event.initiativeRoll.initiative}")} for initiative (d20: ${event.initiativeRoll.roll.rolled})"
            }
        }
    }
}
