package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement
import com.manikoske.guild.action.Effect
import com.manikoske.guild.encounter.TestingCommons.randomBuilder
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class CharacterStateTest {


    data class MyData2(
        val roll : () -> Int
    )

    @Test
    fun test() {
        val sampled = FixtureMonkeyBuilder()
            .plugin(KotlinPlugin())
            .build()
            .giveMeKotlinBuilder<MyData2>()
            .sample()

        println("sampled: $sampled")
    }


    data class MyData(
        val id: Int,
        val name: String,
        val scheme: Scheme,
    )

    sealed class Scheme {
        data class Url(val scheme: String) : Scheme()
        data object None : Scheme()
    }

    @Test
    fun `test fixtureMonkey`() {
        val sampled = FixtureMonkeyBuilder()
            .plugin(KotlinPlugin())
            .build()
            .giveMeKotlinBuilder<MyData>()
            .set(MyData::scheme, Scheme.None)
            .sample()

        println("sampled: $sampled")
    }

    @Test
    fun canMoveBy() {

        val normalMovement = Movement(type = Movement.Type.Normal, amount = 2)
        val specialMovement = Movement(type = Movement.Type.Special, amount = 3)

        val entangled = randomBuilder.giveMeOne<Effect.MovementRestrictingEffect.Entangled>()
        val slow = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Slow>()
        val haste = randomBuilder.giveMeOne<Effect.MovementAlteringEffect.Haste>()

        val characterState = randomBuilder
            .giveMeKotlinBuilder<CharacterState>()
            .set(CharacterState::effects, CharacterState.CharacterStates.noEffects())
            .sample()


        // no statuses, no alterations
        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects()).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement)

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects()).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)


        // movement prohibiting statuses only are present
        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(entangled)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement)

        // movement altering statuses only are present

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(slow)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 1))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(slow)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 2))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(haste)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 3))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(haste)).canMoveBy(specialMovement))
            .usingRecursiveComparison().isEqualTo(specialMovement.copy(amount = 4))


        // both movement statuses only are present

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(haste).add(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))

        assertThat(characterState.copy(effects = CharacterState.CharacterStates.noEffects().add(slow).add(entangled)).canMoveBy(normalMovement))
            .usingRecursiveComparison().isEqualTo(normalMovement.copy(amount = 0))


    }
}