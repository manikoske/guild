package com.manikoske.guild.encounter

import com.github.nylle.kotlinfixture.Fixture
import com.manikoske.guild.character.Bio
import com.manikoske.guild.character.Character
import com.manikoske.guild.character.CharacterState
import com.manikoske.guild.inventory.Inventory

object Fixture {

    fun characterState(name: String = "") : CharacterState {
        return CharacterState(
            character = Character(
                id = fixture().create(),
                bio = fixture().create<Bio>().copy(name = name),
                availableActions = listOf(),
                level = fixture().create(),
                inventory = Inventory(
                    armor = fixture().create(),
                    arms = fixture().create<Inventory.Arms.OneHandedWeaponAndShield>(),
                )
            ),
            resourcesSpent = 0,
            damageTaken = 0,
            positionNodeId = 1,
            allegiance = CharacterState.Allegiance.Defender,
            statuses = listOf()
        )
    }

    fun character() : Character {
        return Character(
            id = fixture().create(),
            bio = fixture().create<Bio>(),
            availableActions = listOf(),
            level = fixture().create(),
            inventory = Inventory(
                armor = fixture().create(),
                arms = fixture().create<Inventory.Arms.OneHandedWeaponAndShield>(),
            )
        )
    }


    private fun fixture() : Fixture {
        return Fixture()
    }

}