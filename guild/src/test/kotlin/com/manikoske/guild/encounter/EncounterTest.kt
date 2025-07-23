package com.manikoske.guild.encounter

import com.manikoske.guild.character.Attribute
import com.manikoske.guild.character.Bio
import com.manikoske.guild.character.Character
import com.manikoske.guild.character.Class
import com.manikoske.guild.character.Level
import com.manikoske.guild.encounter.EncounterTest.Characters.anomen
import com.manikoske.guild.encounter.EncounterTest.Characters.dorn
import com.manikoske.guild.encounter.EncounterTest.Characters.edwin
import com.manikoske.guild.encounter.EncounterTest.Characters.imoen
import com.manikoske.guild.encounter.EncounterTest.Characters.khalid
import com.manikoske.guild.encounter.EncounterTest.Characters.kivan
import com.manikoske.guild.encounter.EncounterTest.Characters.valygar
import com.manikoske.guild.encounter.EncounterTest.Characters.viconia
import com.manikoske.guild.encounter.EncounterTest.Characters.xan
import com.manikoske.guild.encounter.EncounterTest.Characters.yoshimo
import com.manikoske.guild.encounter.TestingCommons.bigBattleground
import com.manikoske.guild.inventory.Armor
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.inventory.Shield
import com.manikoske.guild.inventory.Weapon
import com.manikoske.guild.log.LoggingUtils
import org.junit.jupiter.api.RepeatedTest

class EncounterTest {

    object Characters {
        val khalid = Character(
            id = 1,
            bio = Bio(
                name = "Khalid",
                strength = Attribute(15, Attribute.Type.strength),
                dexterity = Attribute(11, Attribute.Type.dexterity),
                constitution = Attribute(15, Attribute.Type.constitution),
                wisdom = Attribute(12, Attribute.Type.wisdom),
                intelligence = Attribute(9, Attribute.Type.intelligence),
                charisma = Attribute(13, Attribute.Type.charisma),
                clazz = Class.Fighter
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.halfPlate,
                arms = Inventory.Arms.OneHandedWeaponAndShield(
                    mainHand = Weapon.Weapons.longSword,
                    shield = Shield.smallShield
                )
            )
        )

        val kivan = Character(
            id = 2,
            bio = Bio(
                name = "Kivan",
                strength = Attribute(12, Attribute.Type.strength),
                dexterity = Attribute(17, Attribute.Type.dexterity),
                constitution = Attribute(11, Attribute.Type.constitution),
                wisdom = Attribute(13, Attribute.Type.wisdom),
                intelligence = Attribute(10, Attribute.Type.intelligence),
                charisma = Attribute(10, Attribute.Type.charisma),
                clazz = Class.Ranger
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.studdedLeather,
                arms = Inventory.Arms.RangedWeapon(
                    bothHands = Weapon.Weapons.longBow
                )
            )
        )

        val anomen = Character(
            id = 3,
            bio = Bio(
                name = "Anomen",
                strength = Attribute(14, Attribute.Type.strength),
                dexterity = Attribute(10, Attribute.Type.dexterity),
                constitution = Attribute(14, Attribute.Type.constitution),
                wisdom = Attribute(15, Attribute.Type.wisdom),
                intelligence = Attribute(10, Attribute.Type.intelligence),
                charisma = Attribute(12, Attribute.Type.charisma),
                clazz = Class.Cleric
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.splintMail,
                arms = Inventory.Arms.OneHandedWeaponAndShield(
                    mainHand = Weapon.Weapons.mace,
                    shield = Shield.smallShield
                )
            )
        )

        val imoen = Character(
            id = 4,
            bio = Bio(
                name = "Imoen",
                strength = Attribute(10, Attribute.Type.strength),
                dexterity = Attribute(15, Attribute.Type.dexterity),
                constitution = Attribute(10, Attribute.Type.constitution),
                wisdom = Attribute(10, Attribute.Type.wisdom),
                intelligence = Attribute(10, Attribute.Type.intelligence),
                charisma = Attribute(14, Attribute.Type.charisma),
                clazz = Class.Rogue
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.leather,
                arms = Inventory.Arms.DualWeapon(
                    mainHand = Weapon.Weapons.shortSword,
                    offHand = Weapon.Weapons.dagger
                )
            )
        )

        val xan = Character(
            id = 5,
            bio = Bio(
                name = "Xan",
                strength = Attribute(8, Attribute.Type.strength),
                dexterity = Attribute(13, Attribute.Type.dexterity),
                constitution = Attribute(8, Attribute.Type.constitution),
                wisdom = Attribute(14, Attribute.Type.wisdom),
                intelligence = Attribute(17, Attribute.Type.intelligence),
                charisma = Attribute(12, Attribute.Type.charisma),
                clazz = Class.Wizard
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.robe,
                arms = Inventory.Arms.TwoHandedWeapon(
                    bothHands = Weapon.Weapons.quarterStaff
                )
            )
        )

        val dorn = Character(
            id = 6,
            bio = Bio(
                name = "Dorn",
                strength = Attribute(17, Attribute.Type.strength),
                dexterity = Attribute(9, Attribute.Type.dexterity),
                constitution = Attribute(18, Attribute.Type.constitution),
                wisdom = Attribute(12, Attribute.Type.wisdom),
                intelligence = Attribute(9, Attribute.Type.intelligence),
                charisma = Attribute(9, Attribute.Type.charisma),
                clazz = Class.Fighter
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.fullPlate,
                arms = Inventory.Arms.TwoHandedWeapon(
                    bothHands = Weapon.Weapons.greatSword
                )
            )
        )

        val valygar = Character(
            id = 7,
            bio = Bio(
                name = "Valygar",
                strength = Attribute(13, Attribute.Type.strength),
                dexterity = Attribute(15, Attribute.Type.dexterity),
                constitution = Attribute(13, Attribute.Type.constitution),
                wisdom = Attribute(11, Attribute.Type.wisdom),
                intelligence = Attribute(10, Attribute.Type.intelligence),
                charisma = Attribute(13, Attribute.Type.charisma),
                clazz = Class.Ranger
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.studdedLeather,
                arms = Inventory.Arms.RangedWeapon(
                    bothHands = Weapon.Weapons.longBow
                )
            )
        )

        val viconia = Character(
            id = 8,
            bio = Bio(
                name = "Viconia",
                strength = Attribute(10, Attribute.Type.strength),
                dexterity = Attribute(13, Attribute.Type.dexterity),
                constitution = Attribute(10, Attribute.Type.constitution),
                wisdom = Attribute(17, Attribute.Type.wisdom),
                intelligence = Attribute(10, Attribute.Type.intelligence),
                charisma = Attribute(14, Attribute.Type.charisma),
                clazz = Class.Cleric
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.scaleMail,
                arms = Inventory.Arms.OneHandedWeaponAndShield(
                    mainHand = Weapon.Weapons.warHammer,
                    shield = Shield.smallShield
                )
            )
        )

        val yoshimo = Character(
            id = 9,
            bio = Bio(
                name = "Yoshimo",
                strength = Attribute(11, Attribute.Type.strength),
                dexterity = Attribute(14, Attribute.Type.dexterity),
                constitution = Attribute(13, Attribute.Type.constitution),
                wisdom = Attribute(10, Attribute.Type.wisdom),
                intelligence = Attribute(12, Attribute.Type.intelligence),
                charisma = Attribute(10, Attribute.Type.charisma),
                clazz = Class.Rogue
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.studdedLeather,
                arms = Inventory.Arms.DualWeapon(
                    mainHand = Weapon.Weapons.shortSword,
                    offHand = Weapon.Weapons.dagger
                )
            )
        )

        val edwin = Character(
            id = 10,
            bio = Bio(
                name = "Edwin",
                strength = Attribute(8, Attribute.Type.strength),
                dexterity = Attribute(10, Attribute.Type.dexterity),
                constitution = Attribute(8, Attribute.Type.constitution),
                wisdom = Attribute(12, Attribute.Type.wisdom),
                intelligence = Attribute(16, Attribute.Type.intelligence),
                charisma = Attribute(13, Attribute.Type.charisma),
                clazz = Class.Wizard
            ),
            level = Level(level = 1),
            inventory = Inventory(
                armor = Armor.robe,
                arms = Inventory.Arms.TwoHandedWeapon(
                    bothHands = Weapon.Weapons.quarterStaff
                )
            )
        )
    }


    @RepeatedTest(1)
    fun simulate() {

        val encounter = Encounter(battleground = bigBattleground)

        val finalEncounterState = encounter.simulate(
            attackersStartingNodeId = 4,
            defendersStartingNodeId = 6,
            attackers = setOf(khalid, kivan, anomen, imoen, xan),
            defenders = setOf(dorn, valygar, viconia, yoshimo, edwin)
        )

        print(LoggingUtils.formatEncounter(finalEncounterState))
    }
}
