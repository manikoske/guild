package com.manikoske.guild.encounter

import com.manikoske.guild.character.Character

class Battleground(
    private val nodes: Set<Node>,
    // TODO add mapping from and to between character and node
) {

    class Node(
        val id: Int,
        val capacity: Int,
        val edges: List<Edge>
    ) {
        fun hasRoomFor(character: Character): Boolean {
            // TODO character count in node + 1 <= capacity
            return false
        }

        fun canLeave(character: Character): Boolean {
            // TODO number of friends in node > number of foes in node (maybe certain class has bigger presence, immunityFromOpportunityAttacks avoids this check)
            return false
        }
    }

    class Edge(
        val cost: Int,
        val from: Node,
        val to: Node,
    ) {
        fun isPassableBy(character: Character): Boolean {
            return from.canLeave(character) && to.hasRoomFor(character)
        }
    }

    fun computeRangeFrom(startNode: Node): Map<Node, Int> {
        val distances = mutableMapOf<Node, Int>()
        val visited = mutableSetOf<Node>()
        val queue = ArrayDeque<Node>()

        // Initialize
        distances[startNode] = 0
        visited += startNode
        queue += startNode

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDist = distances[current] ?: 0

            // Explore neighbors
            for (edge in current.edges) {

                val neighbor = edge.to
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
     * (sum of edge costs) from `startNode` to every other node in `map`.
     * If a node is unreachable, it won't appear in the returned map.
     */
    fun computeDistancesFrom(startNode: Node, character: Character): Map<Node, Int> {
        // We'll store the minimal known cost to reach each node
        val dist = mutableMapOf<Node, Int>()
        // Start with infinite distances
        for (node in nodes) {
            dist[node] = Int.MAX_VALUE
        }
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
                if (!edge.isPassableBy(character)) continue
                val neighbor = edge.to
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
        return dist.filterValues { it != Int.MAX_VALUE }
    }

}