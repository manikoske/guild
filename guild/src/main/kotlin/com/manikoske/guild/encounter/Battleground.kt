package com.manikoske.guild.encounter

import javax.sound.sampled.Line

class Battleground(
    private val nodes: Set<Node>,
) {

    class Node(
        val id: Int,
        val capacity: Int,
        val paths: Set<Path>,
        val lineOfSight: Set<LineOfSight>
    )

    class Path(
        val cost: Int,
        val toNodeId: Int,
    )

    class LineOfSight(
        val toNodeId: Int,
        val range: Int
    )

    private fun nodeBy(id: Int): Node {
        return nodes.first { it.id == id }
    }

    private fun hasNoCapacityLeft(
        nodeId: Int,
        allyCountPerNode: Map<Int, Int>,
        enemyCountPerNode: Map<Int, Int>
    ): Boolean {
        return allyCountPerNode.getOrDefault(nodeId, 0) +
                enemyCountPerNode.getOrDefault(nodeId, 0) >=
                nodeBy(nodeId).capacity
    }

    private fun canNotLeaveFrom(
        nodeId: Int,
        allyCountPerNode: Map<Int, Int>,
        enemyCountPerNode: Map<Int, Int>
    ): Boolean {
        return allyCountPerNode.getOrDefault(nodeId, 0) <= enemyCountPerNode.getOrDefault(nodeId, 0)
    }

    fun allBattlegroundNodes() : Set<Node> {
        return nodes
    }

    /**
     * Uses a Dijkstra-like algorithm to compute the minimal cost
     * (sum of edge costs) from `startNodeId` to every other nodeId in `map`.
     */

    fun getAllNodeNormalMovementRequirements(
        startNodeId: Int,
        allyCountPerNode: Map<Int, Int>,
        enemyCountPerNode: Map<Int, Int>,
    ): Map<Int, Int> {
        // We'll store the minimal known cost to reach each node
         val dist = mutableMapOf<Int, Int>()
        // Start with infinite distances
        for (node in nodes) {
            dist[node.id] = Int.MAX_VALUE
        }
        dist[startNodeId] = 0

        // Use a priority queue (min-heap) of (cost, Node)
        val pq = java.util.PriorityQueue<Pair<Int, Node>>(compareBy { it.first })
        // Initialize with (0, startNode)
        pq.add(0 to nodeBy(startNodeId))

        // Track visited if needed, or we can rely on checking better costs
        val visited = mutableSetOf<Int>()

        while (pq.isNotEmpty()) {
            val (currentCost, currentNode) = pq.poll()

            // If we've already visited with a better cost, skip
            if (currentNode.id in visited) continue
            visited += currentNode.id

            // Explore neighbors
            for (path in currentNode.paths) {
                val neighbor = nodeBy(path.toNodeId)
                if (
                    hasNoCapacityLeft(neighbor.id, allyCountPerNode, enemyCountPerNode) ||
                    canNotLeaveFrom(currentNode.id, allyCountPerNode, enemyCountPerNode)

                ) continue

                // cost so far + edge cost
                val newCost = currentCost + path.cost
                if (newCost < dist.getValue(neighbor.id)) {
                    dist[neighbor.id] = newCost
                    pq.add(newCost to neighbor)
                }
            }
        }

        // dist now holds the minimal cost from startNodeId to every reachable nodeId
        // If dist[nodeId] == Int.MAX_VALUE, it was unreachable
        return dist
    }

    fun getAllNodeSpecialMovementRequirements(
        startNodeId: Int,
        allyCountPerNode: Map<Int, Int>,
        enemyCountPerNode: Map<Int, Int>,
    ): Map<Int, Int> {
        return nodeBy(startNodeId).lineOfSight.associateBy(
            { it.toNodeId },
            { if (startNodeId != it.toNodeId && hasNoCapacityLeft(it.toNodeId, allyCountPerNode, enemyCountPerNode)) Int.MAX_VALUE else it.range}
        )
    }

}