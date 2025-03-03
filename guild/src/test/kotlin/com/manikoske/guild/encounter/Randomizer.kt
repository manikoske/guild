package com.manikoske.guild.encounter

import com.manikoske.guild.character.Bio
import com.manikoske.guild.character.Character
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.pushAssignableTypeArbitraryIntrospector

object Randomizer {

    private val randomBuilder = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .pushAssignableTypeArbitraryIntrospector<Effects> {
            ArbitraryIntrospectorResult(CombinableArbitrary.from(CharacterState.CharacterStates.noEffects()))
        }
//        .pushAssignableTypeArbitraryIntrospector<Effect.ActionForcingEffect> {
//            ArbitraryIntrospectorResult(CombinableArbitrary.from(Effect.ActionForcingEffect.Prone))
//        }
//        .pushAssignableTypeArbitraryIntrospector<Effect.ActionForcingEffect> {
//            ArbitraryIntrospectorResult(CombinableArbitrary.from(Effect.ActionForcingEffect.Dying))
//        }
//        .register(Effect.ActionForcingEffect.Dying::class.java) {
//            it.giveMeBuilder<Effects>().instantiateBy { constructor<Effect.ActionForcingEffect.Dying>() }
//        }
        .build()

    fun characterState(name: String) : CharacterState  {
        return randomBuilder.giveMeOne<CharacterState>().copy(
            character = randomBuilder.giveMeOne<Character>().copy(
                bio = randomBuilder.giveMeOne<Bio>().copy(name = name)
            )
        )
    }

    fun randomBuilder() : FixtureMonkey {
        return randomBuilder
    }

}