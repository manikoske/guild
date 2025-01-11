package com.manikoske.guild.encounter

import com.manikoske.guild.action.Movement

class Battleground(
    private val nodes: Set<Node>,
) {

    class Node(
        val id: Int,
        val capacity: Int,
        val edges: List<Edge>
    )

    class Edge(
        val cost: Int,
        val fromNodeId: Int,
        val toNodeId: Int,
    )

    private fun nodeBy(id: Int) : Node {
        return nodes.first { it.id == id }
    }

    fun nodesInRange(startNodeId: Int, maxRange: Int): List<Int> {
        val distances = mutableMapOf<Node, Int>()
        val visited = mutableSetOf<Node>()
        val queue = ArrayDeque<Node>()
        val startNode = nodeBy(startNodeId)

        // Initialize
        distances[startNode] = 0
        visited += startNode
        queue += startNode

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDist = distances[current] ?: 0

            // Explore neighbors
            for (edge in current.edges) {

                val neighbor = nodeBy(edge.toNodeId)
                if (neighbor !in visited) {
                    visited += neighbor
                    distances[neighbor] = currentDist + 1
                    queue += neighbor
                }
            }
        }

        return distances.filterValues { it <= maxRange }.keys.map { it.id }
    }

    /**
     * Uses a Dijkstra-like algorithm to compute the minimal cost
     * (sum of edge costs) from `startNode` to every other node in `map`.
     * If a node is unreachable, it won't appear in the returned map.
     */
    fun getAccessibleNodesIds(
        startNodeId: Int,
        encounterContext: Encounter.EncounterContext,
        pointOfView: Encounter.EncounterContext.PointOfView,
        actionMovement: Movement
    ): List<Int> {
        // We'll store the minimal known cost to reach each node
        val dist = mutableMapOf<Node, Int>()
        // Start with infinite distances
        for (node in nodes) {
            dist[node] = Int.MAX_VALUE
        }
        val startNode = nodeBy(startNodeId)
        dist[startNode] = 0

        // Use a priority queue (min-heap) of (cost, Node)
        val pq = java.util.PriorityQueue<Pair<Int, Node>>(compareBy { it.first })
        // Initialize with (0, startNode)
        pq.add(0 to startNode)

        // Track visited if needed, or we can rely on checking better costs
        val visited = mutableSetOf<Node>()

        while (pq.isNotEmpty()) {
            val (currentCost, currentNode) = pq.poll()

            // If we've already visited with a better cost, skip
            if (currentNode in visited) continue
            visited += currentNode

            // Explore neighbors
            for (edge in currentNode.edges) {
                if (encounterContext.notPassable(
                        pointOfView = pointOfView,
                        actionMovement = actionMovement,
                        edge = edge,
                        toCapacity = nodeBy(edge.toNodeId).capacity
                    )
                ) continue
                val neighbor = nodeBy(edge.toNodeId)
                // cost so far + edge cost
                val newCost = currentCost + edge.cost
                if (newCost < dist.getValue(neighbor)) {
                    dist[neighbor] = newCost
                    pq.add(newCost to neighbor)
                }
            }
        }

        // dist now holds the minimal cost from startNode to every reachable node
        // If dist[node] == Int.MAX_VALUE, it was unreachable
        return dist.filterValues { it <= actionMovement.nodes }.keys.map { it.id }
    }

}