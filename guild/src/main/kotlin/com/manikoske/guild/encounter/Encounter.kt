package com.manikoske.guild.encounter

import com.manikoske.guild.character.Character

class Encounter(
    val battleground: Battleground,
    var group1: Set<Character>,
    var group2: Set<Character>
) {
}