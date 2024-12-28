package com.manikoske.guild.rules

import kotlin.math.min

enum class Armor(val type: Type, val armorClass: Int, val dexterityModifierLimit: (dexterityModifier: Int) -> Int) {

    robe(Type.cloth, 10, { dexterityModifier: Int -> dexterityModifier }),
    clothes(Type.cloth, 10, { dexterityModifier: Int -> dexterityModifier }),
    leather(Type.light, 11, { dexterityModifier: Int -> dexterityModifier }),
    studdedLeather(Type.light, 12, { dexterityModifier: Int -> dexterityModifier }),
    scaleMail(Type.medium, 14, { dexterityModifier: Int -> min(dexterityModifier, 2) }),
    halfPlate(Type.medium, 15, { dexterityModifier: Int -> min(dexterityModifier, 2) }),
    chainMail(Type.heavy, 16, { 0 }),
    splintMail(Type.heavy, 17, { 0 }),
    fullPlate(Type.heavy, 18, { 0 });

    enum class Type {
        cloth, light, medium, heavy
    }

}