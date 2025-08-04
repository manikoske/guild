package com.manikoske.guild.encounter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for the updated Battleground class,
 * specifically focusing on capacity logic (hasNoCapacityLeft)
 * and canNotLeaveFrom references to the "currentNodeId" fix.
 */
class BattlegroundTest {

    private lateinit var battleground: Battleground

    /**
     * We'll build a small 5-node map with edges of cost=1 or 2,
     * then set up interesting capacity + ally/enemy distributions.
     *
     * ASCII Diagram:
     *
     *    (1) --2-- (2) --2-- (3)
     *     |          |
     *     1          2
     *     |          |
     *    (4) --1-- (5)
     *
     * Node capacities vary to demonstrate "no capacity left."
     */
    @BeforeEach
    fun setUp() {
        // Create 5 nodes with different capacities
        val node1 = Battleground.Node(
            id = 1,
            capacity = 3,
            paths = setOf(
                Battleground.Path(cost = 2, toNodeId = 2),
                Battleground.Path(cost = 1, toNodeId = 4)
            ),
            lineOfSight = setOf(
                Battleground.LineOfSight(toNodeId = 1, range = 0),
                Battleground.LineOfSight(toNodeId = 2, range = 1),
                Battleground.LineOfSight(toNodeId = 3, range = 2),
                Battleground.LineOfSight(toNodeId = 4, range = 1),
                Battleground.LineOfSight(toNodeId = 5, range = 2),
            )
        )
        val node2 = Battleground.Node(
            id = 2,
            capacity = 2,
            paths = setOf(
                Battleground.Path(cost = 2, toNodeId = 1),
                Battleground.Path(cost = 2, toNodeId = 3),
                Battleground.Path(cost = 2, toNodeId = 5),
            ),
            lineOfSight = setOf(
                Battleground.LineOfSight(toNodeId = 1, range = 1),
                Battleground.LineOfSight(toNodeId = 2, range = 0),
                Battleground.LineOfSight(toNodeId = 3, range = 1),
                Battleground.LineOfSight(toNodeId = 4, range = 2),
                Battleground.LineOfSight(toNodeId = 5, range = 1),
            )
        )
        val node3 = Battleground.Node(
            id = 3,
            capacity = 1,
            paths = setOf(
                Battleground.Path(cost = 2, toNodeId = 2),
            ),
            lineOfSight = setOf(
                Battleground.LineOfSight(toNodeId = 1, range = 2),
                Battleground.LineOfSight(toNodeId = 2, range = 1),
                Battleground.LineOfSight(toNodeId = 3, range = 0),
                Battleground.LineOfSight(toNodeId = 4, range = 3),
                Battleground.LineOfSight(toNodeId = 5, range = 2),
            )
        )
        val node4 = Battleground.Node(
            id = 4,
            capacity = 2,
            paths = setOf(
                Battleground.Path(cost = 1, toNodeId = 4),
                Battleground.Path(cost = 1, toNodeId = 2)
            ),
            lineOfSight = setOf(
                Battleground.LineOfSight(toNodeId = 1, range = 1),
                Battleground.LineOfSight(toNodeId = 2, range = 2),
                Battleground.LineOfSight(toNodeId = 3, range = 2),
                Battleground.LineOfSight(toNodeId = 4, range = 0),
                Battleground.LineOfSight(toNodeId = 5, range = 1),
            )
        )
        val node5 = Battleground.Node(
            id = 5,
            capacity = 3,
            paths = setOf(
                Battleground.Path(cost = 2, toNodeId = 2),
                Battleground.Path(cost = 1, toNodeId = 4)
            ),
            lineOfSight = setOf(
                Battleground.LineOfSight(toNodeId = 1, range = 2),
                Battleground.LineOfSight(toNodeId = 2, range = 1),
                Battleground.LineOfSight(toNodeId = 3, range = 2),
                Battleground.LineOfSight(toNodeId = 4, range = 1),
                Battleground.LineOfSight(toNodeId = 5, range = 0),
            )
        )


        // Build the battleground
        battleground = Battleground(nodes = setOf(node1, node2, node3, node4, node5))
    }

    @Test
    fun `test getAllNodeNormalMovementRequirements`() {

        val result = battleground.getAllNodeNormalMovementRequirements(
            startNodeId = 1,
        )

        assertEquals(0, result[1], "start node => cost=0")
        assertEquals(2, result[2], "Node2 is reachable")
        assertEquals(4, result[3], "Node3 is reachable")
        assertEquals(1, result[4], "Node4 reachable")
        assertEquals(4, result[5], "Node5 is reachable, but not via node4")

        // Print for debug
        println("Dijkstra with Normal movement from Node1 => $result")
    }

    @Test
    fun `test getAllNodeSpecialMovementRequirements`() {
        // Node2 has capacity=2 => but there's 1 enemy, so 1 slot left.
        // We start from node1 => cost=0 => we can leave node1 since movement=Special => canNotLeaveFrom= false

        val result = battleground.getAllNodeSpecialMovementRequirements(
            startNodeId = 1,
        )

        // Node1 => cost=0
        assertEquals(0, result[1])
        // Node2 => has capacity left => cost from edge(1->2)=2 => we should see cost=2
        assertEquals(1, result[2], "Node2 cost=2 from Node1 with special movement")

        // Node3 => capacity=1 => hasNoCapacityLeft= true => we can't move in => expect Int.MAX_VALUE
        assertEquals(2, result[3], "Node3 is reachable")

        // Node4 => from setUp => 1->4 cost=1 => plus we can leave node1. So cost=1
        assertEquals(1, result[4], "Node4 is reachable with cost=1 from Node1")

        assertEquals(2, result[5], "Node5 from node4")

        println("Dijkstra with Special movement from Node1 => $result")
    }
}
