package com.manikoske.guild.encounter

import com.manikoske.guild.action.Effect
import com.manikoske.guild.character.Character
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest

class EncounterStateTest {


    @RepeatedTest(10)
    fun testRollInitiatives() {

        val chockCharacter = mockk<Character>()
        every { chockCharacter.initiativeRoll() } returns 10
        every { chockCharacter.id } returns 1

        val brokCharacter = mockk<Character>()
        every { brokCharacter.initiativeRoll() } returns 5
        every { brokCharacter.id } returns 2

        val trakkCharacter = mockk<Character>()
        every { trakkCharacter.initiativeRoll() } returns 15
        every { trakkCharacter.id } returns 3

        val chock = Randomizer.characterState("Chock").copy(character = chockCharacter)
        val brok = Randomizer.characterState("Brok").copy(character = brokCharacter)
        val trakk = Randomizer.characterState("Trakk").copy(character = trakkCharacter)

        val encounterState = EncounterState(updatedCharacterStates = listOf(chock, brok, trakk))

        assertThat(encounterState.rollInitiatives()).isEqualTo(listOf(trakk.character.id, chock.character.id, brok.character.id))
    }

    @RepeatedTest(10)
    fun hasNoWinner() {

        val dying = Randomizer.randomBuilder().giveMeOne<Effect.ActionForcingEffect.Dying>()

        val firkraag = Randomizer.characterState("Firkraag").copy(allegiance = CharacterState.Allegiance.Defender, effects = CharacterState.noEffects())
        val ajantis = Randomizer.characterState("Ajantis").copy(allegiance = CharacterState.Allegiance.Attacker, effects = CharacterState.noEffects())
        val kivan = Randomizer.characterState("Kivan").copy(allegiance = CharacterState.Allegiance.Attacker, effects = CharacterState.noEffects())

        assertThat(EncounterState(updatedCharacterStates = listOf(firkraag, ajantis, kivan)).hasNoWinner()).isTrue()
        assertThat(EncounterState(updatedCharacterStates = listOf(firkraag, ajantis, kivan.addEffect(dying))).hasNoWinner()).isTrue()
        assertThat(EncounterState(updatedCharacterStates = listOf(firkraag, ajantis.addEffect(dying), kivan.addEffect(dying))).hasNoWinner()).isFalse()


    }

}