package com.manikoske.guild.encounter

import com.manikoske.guild.action.Effect
import com.manikoske.guild.encounter.TestingCommons.randomBuilder
import com.navercorp.fixturemonkey.junit.jupiter.annotation.Seed
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EffectsTest {

    @Test
    fun testActionForcingEffects() {
        val noEffects = CharacterState.CharacterStates.noEffects()

        val prone = Effect.ActionForcingEffect.Prone
        val dying = Effect.ActionForcingEffect.Dying
        val firstStun = randomBuilder.giveMeOne<Effect.ActionForcingEffect.Stun>()
        val secondStun = randomBuilder.giveMeOne<Effect.ActionForcingEffect.Stun>()

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
        val noEffects = CharacterState.CharacterStates.noEffects()

        val firstEntagled = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val secondEntagled = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val held = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Held>()

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
        val noEffects = CharacterState.CharacterStates.noEffects()

        val firstSlow = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val secondSlow = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Haste>()

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
        val noEffects = CharacterState.CharacterStates.noEffects()

        val firstDisarm = randomBuilder.giveMeOne<Effect.ActionRestrictingEffect.Disarmed>()
        val secondDisarm = randomBuilder.giveMeOne<Effect.ActionRestrictingEffect.Disarmed>()
        val silenced = randomBuilder.giveMeOne<Effect.ActionRestrictingEffect.Silenced>()

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
        val noEffects = CharacterState.CharacterStates.noEffects()

        val firstBleed = randomBuilder.giveMeOne<Effect.DamageOverTimeEffect.Bleed>()
        val secondBleed = randomBuilder.giveMeOne<Effect.DamageOverTimeEffect.Bleed>()
        val poison = randomBuilder.giveMeOne<Effect.DamageOverTimeEffect.Poison>()

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
        val noEffects = CharacterState.CharacterStates.noEffects()

        val firstRegeneration = randomBuilder.giveMeOne<Effect.HealOverTimeEffect.Regeneration>()
        val secondRegeneration = randomBuilder.giveMeOne<Effect.HealOverTimeEffect.Regeneration>()

        val effects1 = noEffects.add(firstRegeneration).add(secondRegeneration)

        assertThat(effects1.all())
            .isEqualTo(listOf(secondRegeneration))

        val effects2 = noEffects.add(firstRegeneration).remove(secondRegeneration)

        assertThat(effects2.all())
            .isEmpty()

    }

    @Test
    fun testRemoveOnDamageTaken() {
        val stun = randomBuilder.giveMeOne<Effect.ActionForcingEffect.Stun>()
        val entangled = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val haste = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Haste>()
        val regeneration = randomBuilder.giveMeOne<Effect.HealOverTimeEffect.Regeneration>()
        val silenced = randomBuilder.giveMeOne<Effect.ActionRestrictingEffect.Silenced>()
        val poison = randomBuilder.giveMeOne<Effect.DamageOverTimeEffect.Poison>()

        assertThat(
            CharacterState.CharacterStates.noEffects()
                .add(stun)
                .add(entangled)
                .add(haste)
                .add(regeneration)
                .add(silenced)
                .add(poison)
                .removeOnDamage()
                .all()
        )
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(listOf(stun, haste, silenced, regeneration, poison))
    }

    @Test
    @Seed(30)
    fun testTick() {
        val prone = Effect.ActionForcingEffect.Prone
        val entangled = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Entangled>().copy(roundsLeft = 2)
        val haste = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Haste>().copy(roundsLeft = 1)
        val regeneration = randomBuilder.giveMeOne<Effect.HealOverTimeEffect.Regeneration>().copy(roundsLeft = 1)
        val silenced = randomBuilder.giveMeOne<Effect.ActionRestrictingEffect.Silenced>().copy(roundsLeft = 1)
        val poison = randomBuilder.giveMeOne<Effect.DamageOverTimeEffect.Poison>().copy(roundsLeft = 2)

        assertThat(
            CharacterState.CharacterStates.noEffects()
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