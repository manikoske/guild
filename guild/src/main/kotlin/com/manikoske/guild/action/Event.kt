package com.manikoske.guild.action

sealed interface Event {

    data class WeaponAttackHit(
        val weaponAttackRoll : Resolution.WeaponAttackRoll,
        val armorClass: Resolution.ArmorClass
    ) : Event

    data class WeaponAttackMiss(
        val weaponAttackRoll : Resolution.WeaponAttackRoll,
        val armorClass: Resolution.ArmorClass
    ) : Event

    data class WeaponDamageDealt(
        val weaponDamageRoll : Resolution.WeaponDamageRoll,
    ) : Event

    data class EffectAdded(
        val effect: Effect
    ) : Event

    data class EffectRemoved(
        val effect: Effect
    ) : Event

    data class ResourcesGained(
        val amount: Int
    ) : Event

    data class ResourcesSpent(
        val amount: Int
    ) : Event

    data class SpellAttackHit(
        val spellAttackDifficultyClass: Resolution.SpellAttackDifficultyClass,
        val spellDefenseRoll: Resolution.SpellDefenseRoll
    ) : Event

    data class SpellAttackMiss(
        val spellAttackDifficultyClass: Resolution.SpellAttackDifficultyClass,
        val spellDefenseRoll: Resolution.SpellDefenseRoll
    ) : Event

    data class SpellDamageDealt(
        val spellDamageRoll: Resolution.SpellDamageRoll,
    ) : Event

    data class Healed(
        val healRoll: Resolution.HealRoll,
    ) : Event

    data class ActionTaken(
        val name: String,
        val newPositionNodeId: Int,
        val resourceCost: Int
    ) : Event

}