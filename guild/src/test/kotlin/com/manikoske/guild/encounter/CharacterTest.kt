package com.manikoske.guild.encounter

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Bio
import com.manikoske.guild.character.Effect
import com.manikoske.guild.character.Level
import com.manikoske.guild.inventory.Armor
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.inventory.Shield
import com.manikoske.guild.inventory.Weapon
import com.manikoske.guild.inventory.Weapon.Weapons.spear
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Die
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class CharacterTest {

    @Test
    fun testInitiativeRoll() {
        // Constants for the test
        val level = Level(5)
        val attributeScore = 18
        val dice = Dice.of(Die.d20)
        val expectedAttributeModifier = 4
        val expectedLevelModifier = 2
        val expectedResult = 16

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.dexterity } returns Attribute(score = attributeScore, type = Attribute.Type.dexterity)

        // Prepare the character state
        val character = Fixture.character()
            .copy(level = level, bio = bio)

        // Test the initiative roll with Median method
        val initiativeRoll = character.initiativeRoll(Dice.RollMethod.Median)

        // Verify results
        assertThat(initiativeRoll.attributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(initiativeRoll.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(initiativeRoll.rolled.dice).isEqualTo(dice)
        assertThat(initiativeRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testSpellAttackDifficultyClass() {
        // Constants for the test
        val attributeType = Attribute.Type.intelligence
        val baseDifficultyClass = 15
        val attributeScore = 18
        val level = Level(5)
        val expectedAttributeModifier = 4
        val expectedLevelModifier = 2
        val expectedResult = baseDifficultyClass + expectedAttributeModifier + expectedLevelModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.intelligence } returns Attribute(score = attributeScore, type = attributeType)

        // Prepare the character state
        val character = Fixture.character()
            .copy(level = level, bio = bio)

        // Test the spell attack difficulty class
        val spellAttackDC = character.spellAttackDifficultyClass(attributeType, baseDifficultyClass)

        // Verify results
        assertThat(spellAttackDC.spellAttributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(spellAttackDC.spellDifficultyClass).isEqualTo(baseDifficultyClass)
        assertThat(spellAttackDC.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(spellAttackDC.result).isEqualTo(expectedResult)
    }

    @Test
    fun testArmorClass() {
        // Constants for the test
        val level = Level(5)
        val armor = Armor.splintMail
        val arms = Inventory.Arms.OneHandedWeaponAndShield(
            mainHand = Weapon.Weapons.longSword,
            shield = Shield.smallShield
        )
        val dexterityScore = 16
        val expectedArmorDC = armor.armorDifficultyClass
        val expectedArmsModifier = arms.armorClassModifier()
        val expectedLevelModifier = 2
        val expectedDexModifier = 0
        val expectedResult = expectedArmorDC + expectedArmsModifier + expectedLevelModifier + expectedDexModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.dexterity } returns Attribute(score = dexterityScore, type = Attribute.Type.dexterity)

        val inventory = mockk<Inventory>()
        every { inventory.armor } returns armor
        every { inventory.arms } returns arms


        // Prepare the character state
        val character = Fixture.character()
            .copy(level = level, bio = bio, inventory = inventory)


        // Test armor class calculation
        val armorClass = character.armorClass()

        // Verify results
        assertThat(armorClass.armorDifficultyClass).isEqualTo(expectedArmorDC)
        assertThat(armorClass.armsModifier).isEqualTo(expectedArmsModifier)
        assertThat(armorClass.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(armorClass.armorAttributeModifier).isEqualTo(expectedDexModifier)
        assertThat(armorClass.result).isEqualTo(expectedResult)
    }

    @Test
    fun testSpellDamageRoll() {
        // Constants for the test
        val attributeType = Attribute.Type.intelligence
        val damageDice = Dice.of(2, Die.d8)
        val attributeScore = 18
        val level = Level(5)
        val expectedAttributeModifier = 4
        val expectedLevelModifier = 2
        val expectedResult = damageDice.roll(Dice.RollMethod.Median) + expectedAttributeModifier + expectedLevelModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.intelligence } returns Attribute(score = attributeScore, type = attributeType)

        // Prepare the character state
        val characterState = Fixture.character()
            .copy(level = level, bio = bio)

        // Test spell damage roll with Median method
        val spellDamageRoll = characterState.spellDamageRoll(
            attributeType,
            damageDice,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(spellDamageRoll.attributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(spellDamageRoll.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(spellDamageRoll.rolled.dice).isEqualTo(damageDice)
        assertThat(spellDamageRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testSpellDefenseRoll() {
        // Constants for the test
        val attributeType = Attribute.Type.wisdom
        val attributeScore = 16
        val level = Level(5)
        val dice = Dice.of(Die.d20)
        val expectedAttributeModifier = 3
        val expectedLevelModifier = 2
        val expectedResult = dice.roll(Dice.RollMethod.Median) + expectedAttributeModifier + expectedLevelModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.wisdom } returns Attribute(score = attributeScore, type = attributeType)

        // Prepare the character state
        val characterState = Fixture.character()
            .copy(level = level, bio = bio)

        // Test spell defense roll with Median method
        val spellDefenseRoll = characterState.spellDefenseRoll(
            attributeType,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(spellDefenseRoll.attributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(spellDefenseRoll.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(spellDefenseRoll.rolled.dice).isEqualTo(dice)
        assertThat(spellDefenseRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testHealRoll() {
        // Constants for the test
        val attributeType = Attribute.Type.wisdom
        val healDice = Dice.of(2, Die.d8)
        val attributeScore = 16
        val level = Level(5)
        val expectedAttributeModifier = 3
        val expectedLevelModifier = 2
        val expectedResult = healDice.roll(Dice.RollMethod.Median) + expectedAttributeModifier + expectedLevelModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.wisdom } returns Attribute(score = attributeScore, type = attributeType)

        // Prepare the character state
        val characterState = Fixture.character()
            .copy(level = level, bio = bio)

        // Test heal roll with Median method
        val healRoll = characterState.healRoll(
            attributeType,
            healDice,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(healRoll.attributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(healRoll.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(healRoll.rolled.dice).isEqualTo(healDice)
        assertThat(healRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testWeaponAttackRoll() {
        // Constants for the test
        val arms = Inventory.Arms.TwoHandedWeapon(
            bothHands = spear
        )
        val attributeType = Attribute.Type.dexterity
        val attributeScore = 16
        val level = Level(5)
        val dice = Dice.of(Die.d20)
        val expectedAttributeModifier = 3
        val expectedWeaponModifier = 0
        val actionAttackRollModifier = 1
        val expectedLevelModifier = 2
        val expectedResult = dice.roll(Dice.RollMethod.Median) + expectedAttributeModifier +
                expectedWeaponModifier + actionAttackRollModifier + expectedLevelModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.dexterity } returns Attribute(score = attributeScore, type = attributeType)

        val inventory = mockk<Inventory>()
        every { inventory.arms } returns arms


        // Prepare the character state
        val character = Fixture.character()
            .copy(level = level, bio = bio, inventory = inventory)


        // Test weapon attack roll with Median method
        val weaponAttackRoll = character.weaponAttackRoll(
            actionAttackRollModifier,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(weaponAttackRoll.attributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(weaponAttackRoll.weaponAttackModifier).isEqualTo(expectedWeaponModifier)
        assertThat(weaponAttackRoll.actionAttackModifier).isEqualTo(actionAttackRollModifier)
        assertThat(weaponAttackRoll.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(weaponAttackRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testWeaponDamageRoll() {
        // Constants for the test
        val arms = Inventory.Arms.RangedWeapon(
            bothHands = Weapon.Weapons.longBow
        )
        val attributeType = Attribute.Type.dexterity
        val attributeScore = 8
        val level = Level(5)
        val damageMultiplier = 2
        val expectedAttributeModifier = -1
        val expectedLevelModifier = 2
        val expectedResult = arms.bothHands.damageDice.roll(Dice.RollMethod.Median) * damageMultiplier +
                expectedAttributeModifier + expectedLevelModifier

        // Mock for testing
        val bio = mockk<Bio>()
        every { bio.dexterity } returns Attribute(score = attributeScore, type = attributeType)

        val inventory = mockk<Inventory>()
        every { inventory.arms } returns arms


        // Prepare the character state
        val character = Fixture.character()
            .copy(level = level, bio = bio, inventory = inventory)

        // Test weapon damage roll with Median method
        val weaponDamageRoll = character.weaponDamageRoll(
            damageMultiplier,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(weaponDamageRoll.attributeModifier).isEqualTo(expectedAttributeModifier)
        assertThat(weaponDamageRoll.actionDamageMultiplier).isEqualTo(damageMultiplier)
        assertThat(weaponDamageRoll.levelModifier).isEqualTo(expectedLevelModifier)
        assertThat(weaponDamageRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testHealOverTimeRoll() {
        // Constants for the test
        val healDice = Dice.of(Die.d4)
        val expectedResult = healDice.roll(Dice.RollMethod.Median)

        // Mock the effect
        val effect = mockk<Effect.HpAffectingOverTimeEffect.HealingOverTimeEffect>()
        every { effect.healDice } returns healDice

        val character = Fixture.character()

        // Test heal over time roll with Median method
        val healOverTimeRoll = character.healOverTimeRoll(
            effect,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(healOverTimeRoll.rolled.dice).isEqualTo(healDice)
        assertThat(healOverTimeRoll.result).isEqualTo(expectedResult)
    }

    @Test
    fun testDamageOverTimeRoll() {
        // Constants for the test
        val damageDice = Dice.of(Die.d6)
        val expectedResult = damageDice.roll(Dice.RollMethod.Median)

        // Mock the effect
        val effect = mockk<Effect.HpAffectingOverTimeEffect.DamageOverTimeEffect>()
        every { effect.damageDice } returns damageDice

        val character = Fixture.character()

        // Test heal over time roll with Median method
        val healOverTimeRoll = character.damageOverTimeRoll(
            effect,
            Dice.RollMethod.Median
        )

        // Verify results
        assertThat(healOverTimeRoll.rolled.dice).isEqualTo(damageDice)
        assertThat(healOverTimeRoll.result).isEqualTo(expectedResult)
    }

}