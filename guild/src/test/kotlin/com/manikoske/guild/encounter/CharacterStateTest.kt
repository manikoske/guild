package com.manikoske.guild.encounter

import com.manikoske.guild.character.Effect
import com.manikoske.guild.action.Movement
import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.rules.Dice
import com.manikoske.guild.rules.Die
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class CharacterStateTest {

    @Test
    fun canMoveBy() {

        val normalMovement = Movement(type = Movement.Type.Normal, amount = 2)
        val specialMovement = Movement(type = Movement.Type.Special, amount = 3)

        val entangled = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val slow = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Haste>()

        val korgan = Randomizer.characterState("Korgan").copy(effects = CharacterState.noEffects())

        // no statuses, no alterations
        assertThat(korgan.canMoveBy(normalMovement, normalMovement.amount)).isTrue()
        assertThat(korgan.canMoveBy(specialMovement, specialMovement.amount)).isTrue()

        // movement prohibiting statuses only are present
        assertThat(korgan.addEffects(listOf(entangled)).canMoveBy(normalMovement, 1)).isFalse()

        assertThat(korgan.addEffects(listOf(entangled)).canMoveBy(specialMovement, specialMovement.amount)).isTrue()

        // movement altering statuses only are present

        assertThat(korgan.addEffects(listOf(slow)).canMoveBy(normalMovement, 2)).isFalse()
        assertThat(korgan.addEffects(listOf(slow)).canMoveBy(specialMovement, 3)).isFalse()

        assertThat(korgan.addEffects(listOf(haste)).canMoveBy(normalMovement, 3)).isTrue()
        assertThat(korgan.addEffects(listOf(haste)).canMoveBy(specialMovement, 4)).isTrue()

        // both movement statuses are present

        assertThat(korgan.addEffects(listOf(haste, entangled)).canMoveBy(normalMovement, 1)).isFalse()
        assertThat(korgan.addEffects(listOf(slow, entangled)).canMoveBy(normalMovement, 1)).isFalse()
    }



//    @Test
//    fun testHealRoll() {
//        // Constants for the test
//        val attributeModifier = 4
//        val levelModifier = 3
//        val attributeType = Attribute.Type.wisdom
//        val healDice = Dice.of(2, Die.d8)
//        val expectedResult = attributeModifier + levelModifier + healDice.roll(Dice.RollMethod.Median)
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.attributeModifier(attributeType) } returns attributeModifier
//        every { character.levelModifier() } returns levelModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the heal roll with Median method
//        val healRoll = characterState.healRoll(
//            attributeType = attributeType,
//            heal = healDice,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(healRoll.attributeModifier).isEqualTo(attributeModifier)
//        assertThat(healRoll.levelModifier).isEqualTo(levelModifier)
//        assertThat(healRoll.rolled.dice).isEqualTo(healDice)
//        assertThat(healRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testWeaponAttackRoll() {
//        // Constants for the test
//        val attributeModifier = 4
//        val weaponAttackModifier = 2
//        val actionAttackModifier = 3
//        val levelModifier = 2
//        val dice = Dice.of(Die.d20)
//        val expectedResult = dice.roll(Dice.RollMethod.Median) + weaponAttackModifier + weaponAttackModifier + actionAttackModifier + levelModifier
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.weaponAttributeModifier() } returns attributeModifier
//        every { character.weaponAttackModifier() } returns weaponAttackModifier
//        every { character.levelModifier() } returns levelModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the weapon attack roll with Median method
//        val weaponAttackRoll = characterState.weaponAttackRoll(
//            attackRollModifier = actionAttackModifier,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(weaponAttackRoll.attributeModifier).isEqualTo(attributeModifier)
//        assertThat(weaponAttackRoll.weaponAttackModifier).isEqualTo(weaponAttackModifier)
//        assertThat(weaponAttackRoll.actionAttackModifier).isEqualTo(actionAttackModifier)
//        assertThat(weaponAttackRoll.levelModifier).isEqualTo(levelModifier)
//        assertThat(weaponAttackRoll.rolled.dice).isEqualTo(dice)
//        assertThat(weaponAttackRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testWeaponDamageRoll() {
//        // Constants for the test
//        val attributeModifier = 3
//        val damageRollMultiplier = 2
//        val levelModifier = 2
//        val weaponDice = Dice.of(Die.d8)
//        val expectedResult = weaponDice.roll(Dice.RollMethod.Median) * damageRollMultiplier + attributeModifier + levelModifier
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.weaponAttributeModifier() } returns attributeModifier
//        every { character.weaponDamage() } returns weaponDice
//        every { character.levelModifier() } returns levelModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the weapon damage roll with Median method
//        val weaponDamageRoll = characterState.weaponDamageRoll(
//            damageRollMultiplier = damageRollMultiplier,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(weaponDamageRoll.attributeModifier).isEqualTo(attributeModifier)
//        assertThat(weaponDamageRoll.actionDamageMultiplier).isEqualTo(damageRollMultiplier)
//        assertThat(weaponDamageRoll.levelModifier).isEqualTo(levelModifier)
//        assertThat(weaponDamageRoll.rolled.dice).isEqualTo(weaponDice)
//        assertThat(weaponDamageRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testSpellDamageRoll() {
//        // Constants for the test
//        val attributeModifier = 5
//        val levelModifier = 3
//        val attributeType = Attribute.Type.intelligence
//        val damageDice = Dice.of(3, Die.d6)
//        val expectedResult = attributeModifier + levelModifier + damageDice.roll(Dice.RollMethod.Median)
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.attributeModifier(attributeType) } returns attributeModifier
//        every { character.levelModifier() } returns levelModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the spell damage roll with Median method
//        val spellDamageRoll = characterState.spellDamageRoll(
//            attributeType = attributeType,
//            damage = damageDice,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(spellDamageRoll.attributeModifier).isEqualTo(attributeModifier)
//        assertThat(spellDamageRoll.levelModifier).isEqualTo(levelModifier)
//        assertThat(spellDamageRoll.rolled.dice).isEqualTo(damageDice)
//        assertThat(spellDamageRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testSpellDefenseRoll() {
//        // Constants for the test
//        val attributeModifier = 2
//        val levelModifier = 3
//        val attributeType = Attribute.Type.constitution
//        val dice = Dice.of(Die.d20)
//        val expectedResult = attributeModifier + levelModifier + dice.roll(Dice.RollMethod.Median)
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.attributeModifier(attributeType) } returns attributeModifier
//        every { character.levelModifier() } returns levelModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the spell defense roll with Median method
//        val spellDefenseRoll = characterState.spellDefenseRoll(
//            attributeType = attributeType,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(spellDefenseRoll.attributeModifier).isEqualTo(attributeModifier)
//        assertThat(spellDefenseRoll.levelModifier).isEqualTo(levelModifier)
//        assertThat(spellDefenseRoll.rolled.dice).isEqualTo(dice)
//        assertThat(spellDefenseRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testDamageOverTimeRoll() {
//        // Constants for the test
//        val damageDice = Dice.of(Die.d4)
//        val expectedResult = damageDice.roll(Dice.RollMethod.Median)
//
//        // Mock the effect
//        val effect = mockk<Effect.DamageOverTimeEffect>()
//        val category = mockk<Effect.Category>()
//        every { effect.category } returns category
//        every { effect.damageDice } returns damageDice
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState()
//
//        // Test the damage over time roll with Median method
//        val damageOverTimeRoll = characterState.damageOverTimeRoll(
//            effect = effect,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(damageOverTimeRoll.effect).isEqualTo(effect)
//        assertThat(damageOverTimeRoll.rolled.dice).isEqualTo(damageDice)
//        assertThat(damageOverTimeRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testHealOverTimeRoll() {
//        // Constants for the test
//        val healDice = Dice.of(Die.d6)
//        val expectedResult = healDice.roll(Dice.RollMethod.Median)
//
//        // Mock the effect
//        val effect = mockk<Effect.HealOverTimeEffect>()
//        val category = mockk<Effect.Category>()
//        every { effect.category } returns category
//        every { effect.healDice } returns healDice
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState()
//
//        // Test the heal over time roll with Median method
//        val healOverTimeRoll = characterState.healOverTimeRoll(
//            effect = effect,
//            rollMethod = Dice.RollMethod.Median
//        )
//
//        // Verify results
//        assertThat(healOverTimeRoll.effect).isEqualTo(effect)
//        assertThat(healOverTimeRoll.rolled.dice).isEqualTo(healDice)
//        assertThat(healOverTimeRoll.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testSpellAttackDifficultyClass() {
//        // Constants for the test
//        val attributeModifier = 4
//        val baseDifficultyClass = 15
//        val levelModifier = 3
//        val attributeType = Attribute.Type.intelligence
//        val expectedResult = attributeModifier + baseDifficultyClass + levelModifier
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.attributeModifier(attributeType) } returns attributeModifier
//        every { character.levelModifier() } returns levelModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the spell attack difficulty class calculation
//        val spellAttackDC = characterState.spellAttackDifficultyClass(
//            attributeType = attributeType,
//            baseDifficultyClass = baseDifficultyClass
//        )
//
//        // Verify results
//        assertThat(spellAttackDC.spellAttributeModifier).isEqualTo(attributeModifier)
//        assertThat(spellAttackDC.spellDifficultyClass).isEqualTo(baseDifficultyClass)
//        assertThat(spellAttackDC.levelModifier).isEqualTo(levelModifier)
//        assertThat(spellAttackDC.result).isEqualTo(expectedResult)
//    }
//
//    @Test
//    fun testArmorClass() {
//        // Constants for the test
//        val armorDifficultyClass = 16
//        val armsModifier = 2
//        val levelModifier = 3
//        val armorAttributeModifier = 2
//        val expectedResult = armorDifficultyClass + armsModifier + levelModifier + armorAttributeModifier
//
//        // Mock the character for testing
//        val character = mockk<com.manikoske.guild.character.Character>()
//        every { character.armorDifficultyClass() } returns armorDifficultyClass
//        every { character.armorClassArmsModifier() } returns armsModifier
//        every { character.levelModifier() } returns levelModifier
//        every { character.armorLimitedDexterityModifier() } returns armorAttributeModifier
//
//        // Prepare the character state
//        val characterState = Randomizer.characterState().copy(character = character)
//
//        // Test the armor class calculation
//        val armorClass = characterState.armorClass()
//
//        // Verify results
//        assertThat(armorClass.armorDifficultyClass).isEqualTo(armorDifficultyClass)
//        assertThat(armorClass.armsModifier).isEqualTo(armsModifier)
//        assertThat(armorClass.levelModifier).isEqualTo(levelModifier)
//        assertThat(armorClass.armorAttributeModifier).isEqualTo(armorAttributeModifier)
//        assertThat(armorClass.result).isEqualTo(expectedResult)
//    }
}