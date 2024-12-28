package com.manikoske.guild.rules

class Weapon {

    enum class MeleeWeapon(val attributeType: Attribute.Type, val damageRoll: () -> Int, val attackRollBonus: Int, val type: Type, val isTwoHanded: Boolean) {

        longSword(Attribute.Type.strength, { Die.d4.roll(2, -1)}, 0, Type.martial, false),
        shortSword(Attribute.Type.dexterity, { Die.d6.roll(1)}, 0, Type.martial, false),
        dagger(Attribute.Type.dexterity, { Die.d4.roll(1)}, 0, Type.simple, false),
        twoHandedSword(Attribute.Type.strength, { Die.d6.roll(2)}, 0, Type.martial, true),
        greatAxe(Attribute.Type.strength, { Die.d12.roll(1)}, 0, Type.martial, true),
        axe(Attribute.Type.strength, { Die.d8.roll(1)}, 0, Type.martial, false),
        mace(Attribute.Type.strength, { Die.d6.roll(1)}, 0, Type.simple, false),
        warhammer(Attribute.Type.strength, { Die.d4.roll(1, 1)}, 0, Type.martial, false),
        spear(Attribute.Type.dexterity, { Die.d10.roll(1)}, 0, Type.simple, true);

    }

    enum class OffHandMeleeWeapon(val attributeType: Attribute.Type, val damageRoll: () -> Int, val attackRollBonus: Int, val type: Type, val armorClassBonus: Int, isShield: Boolean) {

        shortSword(Attribute.Type.dexterity, { Die.d6.roll(1)}, 0, Type.simple, 1, false),
        dagger(Attribute.Type.dexterity, { Die.d4.roll(1)}, 0, Type.simple, 1, false),
        buckler(Attribute.Type.dexterity, { Die.d2.roll(1, -1) }, -1, Type.simple, 1, true),
        shield(Attribute.Type.dexterity, { Die.d2.roll(1, -1) }, 0, Type.martial, 2, true),

    }

    enum class RangedWeapon(val attributeType: Attribute.Type, val damageRoll: () -> Int, val attackRollBonus: Int, val type: Type, normalRange: Int, maxRange: Int) {

        shortBow(Attribute.Type.dexterity, { Die.d6.roll(1)}, 0, Type.simple, 1, 2),
        longBow(Attribute.Type.dexterity, { Die.d8.roll(1)}, 0, Type.martial, 2, 3),
        sling(Attribute.Type.dexterity, { Die.d4.roll(1)}, 0, Type.simple, 1, 2),

    }

    enum class Type {
        simple, martial
    }

}