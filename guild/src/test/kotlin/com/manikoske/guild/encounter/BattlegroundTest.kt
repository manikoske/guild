package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement
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
        val node1 = Battleground.Node(id=1, capacity=3)
        val node2 = Battleground.Node(id=2, capacity=2)
        val node3 = Battleground.Node(id=3, capacity=1)
        val node4 = Battleground.Node(id=4, capacity=2)
        val node5 = Battleground.Node(id=5, capacity=3)

        val edges = setOf(
            Battleground.Edge(id=1, cost=2, fromNodeId=1, toNodeId=2),
            Battleground.Edge(id=2, cost=2, fromNodeId=2, toNodeId=3),
            Battleground.Edge(id=3, cost=1, fromNodeId=1, toNodeId=4),
            Battleground.Edge(id=4, cost=2, fromNodeId=2, toNodeId=5),
            Battleground.Edge(id=5, cost=1, fromNodeId=4, toNodeId=5)
        )

        // Build the battleground
        battleground = Battleground(
            nodes = setOf(node1, node2, node3, node4, node5),
            edges = edges
        )
    }

    @Test
    fun `test nodeIdsInRange BFS ignoring cost`() {
        // BFS ignoring cost from node1 => nodeIdsInRange(1)
        val distanceMap = battleground.nodeIdsInRange(startNodeId=1)

        // Node1 => dist=0
        assertEquals(0, distanceMap[1], "Starting node distance should be 0")

        // Node2 => 1 hop from node1 ignoring cost
        assertEquals(1, distanceMap[2], "Node2 should be 1 BFS hop from Node1")

        // Node4 => 1 hop ignoring cost (edge cost=2, but BFS sees it as 1 hop)
        assertEquals(1, distanceMap[4], "Node4 is 1 BFS hop from Node1 ignoring cost")

        // Node3 => 2 BFS hops (1->2->3), ignoring cost
        assertEquals(2, distanceMap[3], "Node3 should be 2 BFS hops away from Node1")

        // Node5 => e.g. 2 BFS hops (1->2->5 or 1->4->5)
        // whichever BFS path is found first
        assertEquals(2, distanceMap[5], "Node5 should be 2 BFS hops away from Node1")
    }

    @Test
    fun `test getRequiredNodeMovements with capacity and canNotLeaveFrom`() {
        val allyCount = mapOf(1 to 2, 2 to 1, 3 to 1, 4 to 0, 5 to 0)
        val enemyCount = mapOf(1 to 1, 2 to 0, 3 to 0, 4 to 1, 5 to 1)

        val result = battleground.getAllNodeMovementRequirements(
            startNodeId = 1,
            allyCountPerNode = allyCount,
            enemyCountPerNode = enemyCount,
            movementType = Movement.Type.Normal
        )

        assertEquals(0, result[1], "start node => cost=0")
        assertEquals(2, result[2], "Node2 is reachable")
        assertEquals(Int.MAX_VALUE, result[3], "Node3 unreachable because of full capacity")
        assertEquals(1, result[4], "Node4 reachable")
        assertEquals(4, result[5], "Node5 is reachable, but not via node4")

        // Print for debug
        println("Dijkstra with Normal movement from Node1 => $result")
    }

    @Test
    fun `test getRequiredNodeMovements ignoring capacity with Special movement`() {
        /**
         * If Movement.Type.Special means we can ignore canNotLeaveFrom,
         * we can see how the capacity logic might still block entering Node3 if it's full.
         */
        val allyCount = mapOf(1 to 1, 2 to 0, 3 to 1, 4 to 0, 5 to 0)
        val enemyCount = mapOf(1 to 1, 2 to 1, 3 to 0, 4 to 0, 5 to 0)

        // Node2 has capacity=2 => but there's 1 enemy, so 1 slot left.
        // We start from node1 => cost=0 => we can leave node1 since movement=Special => canNotLeaveFrom= false

        val result = battleground.getAllNodeMovementRequirements(
            startNodeId = 1,
            allyCountPerNode = allyCount,
            enemyCountPerNode = enemyCount,
            movementType = Movement.Type.Special
        )

        // Node1 => cost=0
        assertEquals(0, result[1])
        // Node2 => has capacity left => cost from edge(1->2)=2 => we should see cost=2
        assertEquals(2, result[2], "Node2 cost=2 from Node1 with special movement")

        // Node3 => capacity=1 => hasNoCapacityLeft= true => we can't move in => expect Int.MAX_VALUE
        assertEquals(Int.MAX_VALUE, result[3], "Node3 is full => can't enter => Int.MAX_VALUE")

        // Node4 => from setUp => 1->4 cost=1 => plus we can leave node1. So cost=1
        assertEquals(1, result[4], "Node4 is reachable with cost=1 from Node1")

        assertEquals(2, result[5], "Node5 from node4")

        println("Dijkstra with Special movement from Node1 => $result")
    }
}
