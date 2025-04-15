package com.manikoske.guild.inventory

import com.manikoske.guild.character.Attribute

data class Inventory(
    val armor: Armor,
    val arms: Arms
) {

    sealed interface Arms {

        fun armorClassModifier(): Int {
            return 0
        }

        fun attributeType(): Attribute.Type

        fun range() : Int

        data class OneHandedWeaponAndShield(val mainHand: Weapon.MeleeWeapon, val shield: Shield) : Arms {
            override fun armorClassModifier(): Int {
                return shield.armorClass
            }

            override fun attributeType(): Attribute.Type {
                return mainHand.attributeType()
            }

            override fun range(): Int {
                return 0
            }

        }

        data class RangedWeapon(val bothHands: Weapon.RangedWeapon): Arms {
            override fun attributeType(): Attribute.Type {
                return bothHands.attributeType()
            }

            override fun range(): Int {
                return bothHands.range
            }
        }

        data class TwoHandedWeapon(val bothHands: Weapon.MeleeWeapon): Arms {
            override fun attributeType(): Attribute.Type {
                return bothHands.attributeType()
            }

            override fun range(): Int {
                return 0
            }
        }

        data class DualWeapon(val mainHand: Weapon.MeleeWeapon, val offHand: Weapon.MeleeWeapon): Arms {
            override fun attributeType(): Attribute.Type {
                return mainHand.attributeType()
            }

            override fun range(): Int {
                return 0
            }
        }


    }




}
