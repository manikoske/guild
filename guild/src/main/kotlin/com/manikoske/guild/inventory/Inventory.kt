package com.manikoske.guild.inventory

data class Inventory(
    val armor: Armor,
    val arms: Arms
) {

    sealed interface Arms {

        fun armorClassBonus(): Int {
            return 0
        }

        fun isFinesse(): Boolean

        data class OneHandedWeaponAndShield(val mainHand: Weapon.MeleeWeapon, val shield: Shield) : Arms {
            override fun armorClassBonus(): Int {
                return shield.armorClass
            }

            override fun isFinesse(): Boolean {
                return mainHand.isFinesse()
            }

        }

        data class RangedWeapon(val bothHands: Weapon.RangedWeapon): Arms {
            override fun isFinesse(): Boolean {
                return true
            }
        }

        data class TwoHandedWeapon(val bothHands: Weapon.MeleeWeapon): Arms {
            override fun isFinesse(): Boolean {
                return bothHands.isFinesse()
            }
        }

        data class DualWeapon(val mainHand: Weapon.MeleeWeapon, val offHand: Weapon.MeleeWeapon): Arms {
            override fun isFinesse(): Boolean {
                return mainHand.isFinesse()
            }
        }


    }




}
