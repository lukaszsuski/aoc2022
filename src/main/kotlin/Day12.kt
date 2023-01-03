import java.lang.Integer.min

class PathFinder(val map: Map2<Char>, val start: Cord2, val end: Char) {

    private val currentPath = ArrayDeque<Cord2>()
    private val visitedCords = HashMap<Cord2, VisitedCord>().apply { put(start, VisitedCord(start, 0)) }

    class VisitedCord(val cord: Cord2, var cost: Int = Int.MAX_VALUE) {
    }

    private var shortestPath = ArrayDeque<Cord2>()

    fun getShortestPath(): ArrayDeque<Cord2> {
        visitNext(start)
//            println("shortest found path of length: ${shortestPath.size}")
//            drawPath(shortestPath)
        return shortestPath
    }

    private fun drawPath(path: ArrayDeque<Cord2>) {
        val pathMap = Map2.ofSize(map.width, map.height, '.')
        path.forEach {
            pathMap[it] = map[it]!!
        }
        println(pathMap)
    }

    private fun visitNext(visited: Cord2) {
//            println("${map.at(visited)} ${visited}:")
        currentPath.addLast(visited)

        if (map[visited] == end) {
            //path found
            println("\t\tfound path of length: ${currentPath.size}")
//                drawPath(currentPath)
            if (currentPath.size < shortestPath.size || shortestPath.isEmpty()) {
                shortestPath.clear()
                shortestPath.addAll(currentPath)
            }
        } else {
            visited.adjacents()
                .mapNotNull { adjacent ->
//                    println("${map.at(visited)} -> ${map.at(adjacent)}:")
//                    println("$visited -> $adjacent:")
                    //todo avoid isWithin()
                    if (adjacent.isWithin(map)
                        && canGoFromTo(visited, adjacent)
                        && currentPath.doesNotContain(adjacent)
                    ) {
                        //compare and set min of this+1 vs adjacent.cost
                        visitedCords
                            .computeIfAbsent(adjacent) { VisitedCord(start) }
                            .apply { cost = min(cost, visitedCords[visited]!!.cost + 1) }
                        visitedCords[adjacent]
                    } else {
                        null
                    }
                }
                .sortedBy(VisitedCord::cost)
                .forEach { visitNext(it.cord) }
        }

        currentPath.removeLast()
    }

    private fun ArrayDeque<Cord2>.doesNotContain(next: Cord2) = !contains(next)

    private fun canGoFromTo(current: Cord2, next: Cord2): Boolean {
        if (current == start) {
//                println("\tcan go anywhere from start")
            return true
        }
        if (map[current] == 'z' && map[next] == end) {
//                println("\tcan go from z to end (current:${map.at(current)}, next:${map.at(next)})")
            return true
        }
        if (map[next] in 'a'..'z') {
            val b = map[next]!! - map[current]!! <= 1
//                println("\tnext is ${if (!b) "NOT" else ""} at most 1 higher")
            return b
        }
        return false
    }
}

class Day12(input: List<String>) : Day(input) {

    override fun part1(): Any? {
        val map = input.map { row ->
            row.mapTo(ArrayList()) { it }
        }

        val pathFinder = PathFinder(Map2(map), Cord2(0, 0), 'E')
        return pathFinder.getShortestPath().size - 1
    }

    override fun part2(): Any? {
        TODO("Not yet implemented")
    }

}
