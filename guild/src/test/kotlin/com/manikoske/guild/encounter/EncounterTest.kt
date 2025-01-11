package com.manikoske.guild.encounter

import com.manikoske.guild.character.*
import com.manikoske.guild.inventory.Armor
import com.manikoske.guild.inventory.Inventory
import com.manikoske.guild.inventory.Shield
import com.manikoske.guild.inventory.Weapon
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class EncounterTest {

    @Test
    fun simulateEncounter() {

        val battleground = Battleground(
            nodes = setOf(
                Battleground.Node(
                    id = 1,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 1,
                            toNodeId = 2
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 1,
                            toNodeId = 4
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 2,
                    capacity = 2,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 2,
                            toNodeId = 1
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 2,
                            toNodeId = 3
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 3,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 3,
                            toNodeId = 2
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 3,
                            toNodeId = 6
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 4,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 4,
                            toNodeId = 1
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 4,
                            toNodeId = 5
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 4,
                            toNodeId = 7
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 5,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 5,
                            toNodeId = 4
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 5,
                            toNodeId = 6
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 6,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 6,
                            toNodeId = 3
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 6,
                            toNodeId = 5
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 6,
                            toNodeId = 9
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 7,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 7,
                            toNodeId = 4
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 7,
                            toNodeId = 8
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 8,
                    capacity = 2,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 8,
                            toNodeId = 7
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 8,
                            toNodeId = 9
                        ),
                    ),
                ),
                Battleground.Node(
                    id = 9,
                    capacity = 5,
                    edges = listOf(
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 9,
                            toNodeId = 6
                        ),
                        Battleground.Edge(
                            cost = 1,
                            fromNodeId = 9,
                            toNodeId = 8
                        ),
                    ),
                ),
            )
        )

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

        val encounter = Encounter.Encounters.create(
            battleground = battleground,
            attackersStartingNodeId = 4,
            defendersStartingNodeId = 6,
            attackers = setOf(khalid, kivan, anomen, imoen, xan),
            defenders = setOf(dorn, valygar, viconia, yoshimo, edwin)
        )

        encounter.simulateEncounter()


    }
}