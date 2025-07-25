package com.manikoske.guild.rules

sealed interface DifficultyClass {

    val result: Int
    val levelModifier: Int
    val attributeModifier: Int
    val baseDifficultyClass: Int

    data class ArmorClass(
        val armorDifficultyClass: Int,
        val armsModifier: Int,
        override val levelModifier: Int,
        val armorAttributeModifier: Int
    ) : DifficultyClass {
        override val result = armorDifficultyClass + armsModifier + levelModifier + armorAttributeModifier
        override val attributeModifier: Int
            get() = armorAttributeModifier
        override val baseDifficultyClass: Int
            get() = armorDifficultyClass
    }

    data class SpellAttackDifficultyClass(
        val spellAttributeModifier: Int,
        val spellDifficultyClass: Int,
        override val levelModifier: Int,
    ) : DifficultyClass {
        override val result = spellAttributeModifier + spellDifficultyClass + levelModifier

        override val attributeModifier: Int
            get() = spellAttributeModifier
        override val baseDifficultyClass: Int
            get() = spellDifficultyClass
    }
}