package com.manikoske.guild.ability

sealed interface TargetType {
    sealed interface Weapon : TargetType
    data object SingleTargetWeapon : Weapon
    data class MultiTargetWeapon(val targetCount: Int) : Weapon
    data object AreaOfEffectWeapon : Weapon

    sealed interface Spell : TargetType {
        val range: Int
    }
    data class SingleRangedSpell(override val range: Int) : Spell
    data class MultiRangedSpell(val targetCount: Int, override val range: Int) : Spell
    data class AreaOfEffectRangedSpell(override val range: Int) : Spell

    data object Self : TargetType
}