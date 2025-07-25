package com.manikoske.guild.encounter

import com.manikoske.guild.character.Effect
import com.manikoske.guild.character.CharacterState
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

class EffectsTest {

    @Test
    fun testActionForcingEffects() {
        val noEffects = CharacterState.noEffects()

        val prone = Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Prone>()
        val dying = Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Dying>()
        val firstStun = Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Stun>()
        val secondStun = Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Stun>()

        val effects1 = noEffects.add(firstStun).add(prone).add(dying)

        assertThat(effects1.all())
            .isEqualTo(listOf(dying))

        val effects2 = noEffects.add(prone).remove(prone).add(firstStun).add(secondStun)

        assertThat(effects2.all())
            .isEqualTo(listOf(secondStun))

        val effects3 = noEffects.add(firstStun).remove(secondStun)

        assertThat(effects3.all())
            .isEmpty()
    }

    @Test
    fun testMovementRestrictingEffects() {
        val noEffects = CharacterState.noEffects()

        val firstEntagled = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val secondEntagled = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val held = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Held>()

        val effects1 = noEffects.add(firstEntagled).add(held)

        assertThat(effects1.all())
            .isEqualTo(listOf(held))

        val effects2 = noEffects.add(firstEntagled).add(secondEntagled)

        assertThat(effects2.all())
            .isEqualTo(listOf(secondEntagled))

        val effects3 = noEffects.add(firstEntagled).remove(secondEntagled)

        assertThat(effects3.all())
            .isEmpty()
    }

    @Test
    fun testMovementAlteringEffects() {
        val noEffects = CharacterState.noEffects()

        val firstSlow = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val secondSlow = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Haste>()

        val effects1 = noEffects.add(firstSlow).add(haste)

        assertThat(effects1.all())
            .isEqualTo(listOf(firstSlow, haste))

        val effects2 = noEffects.add(firstSlow).add(secondSlow).add(haste).remove(haste)

        assertThat(effects2.all())
            .isEqualTo(listOf(secondSlow))

        val effects3 = noEffects.add(firstSlow).remove(secondSlow)

        assertThat(effects3.all())
            .isEmpty()
    }

    @Test
    fun testActionRestrictingEffects() {
        val noEffects = CharacterState.noEffects()

        val firstDisarm = Randomizer.randomBuilder().giveMeOne<Effect.ActionRestrictingEffect.Disarmed>()
        val secondDisarm = Randomizer.randomBuilder().giveMeOne<Effect.ActionRestrictingEffect.Disarmed>()
        val silenced = Randomizer.randomBuilder().giveMeOne<Effect.ActionRestrictingEffect.Silenced>()

        val effects1 = noEffects.add(firstDisarm).add(silenced)

        assertThat(effects1.all())
            .isEqualTo(listOf(firstDisarm, silenced))

        val effects2 = noEffects.add(firstDisarm).add(secondDisarm).add(silenced).remove(silenced)

        assertThat(effects2.all())
            .isEqualTo(listOf(secondDisarm))

        val effects3 = noEffects.add(firstDisarm).remove(secondDisarm)

        assertThat(effects3.all())
            .isEmpty()
    }

    @Test
    fun testDamageOverTimeEffects() {
        val noEffects = CharacterState.noEffects()

        val firstBleed = Randomizer.randomBuilder().giveMeOne<Effect.DamageOverTimeEffect.Bleed>()
        val secondBleed = Randomizer.randomBuilder().giveMeOne<Effect.DamageOverTimeEffect.Bleed>()
        val poison = Randomizer.randomBuilder().giveMeOne<Effect.DamageOverTimeEffect.Poison>()

        val effects1 = noEffects.add(firstBleed).add(poison)

        assertThat(effects1.all())
            .isEqualTo(listOf(firstBleed, poison))

        val effects2 = noEffects.add(firstBleed).add(secondBleed).add(poison).remove(poison)

        assertThat(effects2.all())
            .isEqualTo(listOf(secondBleed))

        val effects3 = noEffects.add(firstBleed).remove(secondBleed)

        assertThat(effects3.all())
            .isEmpty()
    }

    @Test
    fun testHealOverTimeEffects() {
        val noEffects = CharacterState.noEffects()

        val firstRegeneration = Randomizer.randomBuilder().giveMeOne<Effect.HealOverTimeEffect.Regeneration>()
        val secondRegeneration = Randomizer.randomBuilder().giveMeOne<Effect.HealOverTimeEffect.Regeneration>()

        val effects1 = noEffects.add(firstRegeneration).add(secondRegeneration)

        assertThat(effects1.all())
            .isEqualTo(listOf(secondRegeneration))

        val effects2 = noEffects.add(firstRegeneration).remove(secondRegeneration)

        assertThat(effects2.all())
            .isEmpty()

    }


    @RepeatedTest(1)
    fun testTick() {
        val prone = Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Prone>()
        val entangled = Randomizer.randomBuilder().giveMeOne<Effect.MovementRestrictingEffect.Entangled>().copy(roundsLeft = 2)
        val haste = Randomizer.randomBuilder().giveMeOne<Effect.MovementAlteringEffect.Haste>().copy(roundsLeft = 1)
        val regeneration = Randomizer.randomBuilder().giveMeOne<Effect.HealOverTimeEffect.Regeneration>().copy(roundsLeft = 1)
        val silenced = Randomizer.randomBuilder().giveMeOne<Effect.ActionRestrictingEffect.Silenced>().copy(roundsLeft = 1)
        val poison = Randomizer.randomBuilder().giveMeOne<Effect.DamageOverTimeEffect.Poison>().copy(roundsLeft = 2)

        assertThat(
            CharacterState.noEffects()
                .add(prone)
                .add(entangled)
                .add(haste)
                .add(regeneration)
                .add(silenced)
                .add(poison)
                .tick()
                .all()
        )
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(listOf(prone, entangled.copy(roundsLeft = 1), poison.copy(roundsLeft = 1)))
    }
}