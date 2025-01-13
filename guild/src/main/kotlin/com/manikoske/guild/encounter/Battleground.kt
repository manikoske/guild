package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement

class Battleground(
    private val nodes: Set<Node>,
    private val edges: Set<Edge>
) {

    class Node(
        val id: Int,
        val capacity: Int,
    )

    class Edge(
        val id: Int,
        val cost: Int,
        val fromNodeId: Int,
        val toNodeId: Int,
    )

    private fun nodeBy(id: Int): Node {
        return nodes.first { it.id == id }
    }

    private fun neighborsFrom(nodeId: Int): List<Int> {
        return edges.filter { edge -> edge.fromNodeId == nodeId }.map { edge -> edge.toNodeId }
    }

    private fun edgesFrom(nodeId: Int): List<Edge> {
        return edges.filter { edge -> edge.fromNodeId == nodeId }
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
        movementType: Movement.Type,
        allyCountPerNode: Map<Int, Int>,
        enemyCountPerNode: Map<Int, Int>
    ): Boolean {
        if (movementType == Movement.Type.Normal) {
            return allyCountPerNode.getOrDefault(nodeId, 0) <= enemyCountPerNode.getOrDefault(nodeId, 0)
        }
        if (movementType == Movement.Type.Special){
            return false
        }
        return false
    }

    fun nodeIdsInRange(startNodeId: Int): Map<Int, Int> {
        val distances = mutableMapOf<Int, Int>()
        val visited = mutableSetOf<Int>()
        val queue = ArrayDeque<Int>()

        // Initialize
        distances[startNodeId] = 0
        visited += startNodeId
        queue += startNodeId

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDist = distances[current] ?: 0

            // Explore neighbors
            for (neighbor in neighborsFrom(current)) {

                if (neighbor !in visited) {
                    visited += neighbor
                    distances[neighbor] = currentDist + 1
                    queue += neighbor
                }
            }
        }

        return distances
    }

    /**
     * Uses a Dijkstra-like algorithm to compute the minimal cost
     * (sum of edge costs) from `startNodeId` to every other nodeId in `map`.
     */

    fun getRequiredNodeMovements(
        startNodeId: Int,
        allyCountPerNode: Map<Int, Int>,
        enemyCountPerNode: Map<Int, Int>,
        movementType: Movement.Type
    ): Map<Int, Int> {
        // We'll store the minimal known cost to reach each node
         val dist = mutableMapOf<Int, Int>()
        // Start with infinite distances
        for (node in nodes) {
            dist[node.id] = Int.MAX_VALUE
        }
        dist[startNodeId] = 0

        // Use a priority queue (min-heap) of (cost, Node)
        val pq = java.util.PriorityQueue<Pair<Int, Int>>(compareBy { it.first })
        // Initialize with (0, startNode)
        pq.add(0 to startNodeId)

        // Track visited if needed, or we can rely on checking better costs
        val visited = mutableSetOf<Int>()

        while (pq.isNotEmpty()) {
            val (currentCost, currentNodeId) = pq.poll()

            // If we've already visited with a better cost, skip
            if (currentNodeId in visited) continue
            visited += currentNodeId

            // Explore neighbors
            for (edge in edgesFrom(currentNodeId)) {
                val neighbor = edge.toNodeId
                if (
                    hasNoCapacityLeft(neighbor, allyCountPerNode, enemyCountPerNode) ||
                    canNotLeaveFrom(currentNodeId, movementType, allyCountPerNode, enemyCountPerNode)
                ) continue

                // cost so far + edge cost
                val newCost = currentCost + edge.cost
                if (newCost < dist.getValue(neighbor)) {
                    dist[neighbor] = newCost
                    pq.add(newCost to neighbor)
                }
            }
        }

        // dist now holds the minimal cost from startNodeId to every reachable nodeId
        // If dist[nodeId] == Int.MAX_VALUE, it was unreachable
        return dist
    }

}