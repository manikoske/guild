package com.manikoske.guild.character

import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.rules.*

data class Character(
    val id: Int,
    val bio: Bio,
    private val level: Level,
    private val inventory: Inventory,
) {

    private fun attribute(attributeType: Attribute.Type): Attribute {
        return when (attributeType) {
            Attribute.Type.strength -> bio.strength
            Attribute.Type.dexterity -> bio.dexterity
            Attribute.Type.constitution -> bio.constitution
            Attribute.Type.wisdom -> bio.wisdom
            Attribute.Type.intelligence -> bio.intelligence
            Attribute.Type.charisma -> bio.charisma
        }
    }

    fun maxHitPoints(): Int {
        return (bio.clazz.hpDie.sides + attribute(Attribute.Type.constitution).modifier()) * level.level
    }

    fun maxResources(): Int {
        return bio.clazz.baseResources * level.level
    }

    private fun weaponAttributeModifier(): Int {
        return attribute(arms().attributeType()).modifier()
    }

    private fun weaponAttackModifier() : Int {
        return when (arms()) {
            is Inventory.Arms.DualWeapon -> -4
            is Inventory.Arms.OneHandedWeaponAndShield -> 0
            is Inventory.Arms.TwoHandedWeapon -> 0
            is Inventory.Arms.RangedWeapon -> 0
        }
    }

    private fun weaponDamage() : Dice {
        return when (val arms = arms()) {
            is Inventory.Arms.DualWeapon -> Dice.combine(arms.mainHand.damageDice, arms.offHand.damageDice)
            is Inventory.Arms.OneHandedWeaponAndShield -> arms.mainHand.damageDice
            is Inventory.Arms.TwoHandedWeapon -> arms.bothHands.damageDice
            is Inventory.Arms.RangedWeapon -> arms.bothHands.damageDice
        }
    }

    private fun attributeModifier(attributeType: Attribute.Type): Int {
        return attribute(attributeType).modifier()
    }

    fun clazz(): Class {
        return bio.clazz
    }

    fun arms(): Inventory.Arms {
        return inventory.arms
    }


    fun spellAttackDifficultyClass(
        attributeType: Attribute.Type,
        baseDifficultyClass: Int
    ) : DifficultyClass.SpellAttackDifficultyClass {
        return DifficultyClass.SpellAttackDifficultyClass(
            spellAttributeModifier = attributeModifier(attributeType),
            spellDifficultyClass = baseDifficultyClass,
            levelModifier = level.modifier()
        )
    }

    fun armorClass() : DifficultyClass.ArmorClass {
        return DifficultyClass.ArmorClass(
            armorDifficultyClass = inventory.armor.armorDifficultyClass,
            armsModifier = inventory.arms.armorClassModifier(),
            levelModifier = level.modifier(),
            armorAttributeModifier = inventory.armor.dexterityModifierLimit(attribute(Attribute.Type.dexterity).modifier())
        )
    }

    fun initiativeRoll(
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.InitiativeRoll {
        return Roll.InitiativeRoll(
            attributeModifier = attributeModifier(Attribute.Type.dexterity),
            levelModifier = level.modifier(),
            rolled = Roll.Rolled(dice = Dice.Companion.of(Die.d20), rollMethod = rollMethod)
        )
    }


    fun spellDefenseRoll(
        attributeType: Attribute.Type,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.SpellDefenseRoll {
        return Roll.SpellDefenseRoll(
            attributeModifier = attributeModifier(attributeType),
            levelModifier = level.modifier(),
            rolled = Roll.Rolled(dice = Dice.Companion.of(Die.d20), rollMethod = rollMethod)
        )
    }

    fun spellDamageRoll(
        attributeType: Attribute.Type,
        damage: Dice,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.SpellDamageRoll {
        return Roll.SpellDamageRoll(
            attributeModifier = attributeModifier(attributeType),
            levelModifier = level.modifier(),
            rolled = Roll.Rolled(dice = damage, rollMethod = rollMethod)
        )
    }

    fun healRoll(
        attributeType: Attribute.Type,
        heal: Dice,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.HealRoll {
        return Roll.HealRoll(
            attributeModifier = attributeModifier(attributeType),
            levelModifier = level.modifier(),
            rolled = Roll.Rolled(dice = heal, rollMethod = rollMethod)
        )
    }

    fun weaponAttackRoll(
        actionAttackRollModifier: Int,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.WeaponAttackRoll {
        return Roll.WeaponAttackRoll(
            attributeModifier = attribute(arms().attributeType()).modifier(),
            weaponAttackModifier = weaponAttackModifier(),
            actionAttackModifier = actionAttackRollModifier,
            levelModifier = level.modifier(),
            rolled = Roll.Rolled(dice = Dice.Companion.of(Die.d20), rollMethod = rollMethod)
        )
    }

    fun weaponDamageRoll(
        damageRollMultiplier: Int,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ): Roll.WeaponDamageRoll {
        return Roll.WeaponDamageRoll(
            attributeModifier = weaponAttributeModifier(),
            actionDamageMultiplier = damageRollMultiplier,
            levelModifier = level.modifier(),
            rolled = Roll.Rolled(dice = weaponDamage(), rollMethod = rollMethod)
        )
    }

    fun healOverTimeRoll(
        effect: Effect.HealOverTimeEffect,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.HealOverTimeRoll {
        return Roll.HealOverTimeRoll(
            effect = effect,
            rolled = Roll.Rolled(dice = effect.healDice, rollMethod = rollMethod)
        )
    }

    fun damageOverTimeRoll(
        effect: Effect.DamageOverTimeEffect,
        rollMethod: Dice.RollMethod = Dice.RollMethod.Normal
    ) : Roll.DamageOverTimeRoll {
        return Roll.DamageOverTimeRoll(
            effect = effect,
            rolled = Roll.Rolled(dice = effect.damageDice, rollMethod = rollMethod)
        )
    }

}