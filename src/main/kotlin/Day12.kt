import java.lang.Integer.min

class Day12 : Day(12) {


    class PathFinder(val map: Map2d<Char>, val start: Cord, val end: Char) {

        private val currentPath = ArrayDeque<Cord>()
        private val visitedCords = HashMap<Cord, VisitedCord>().apply { put(start, VisitedCord(start, 0)) }

        class VisitedCord(val cord: Cord, var cost: Int = Int.MAX_VALUE) {
        }

        private var shortestPath = ArrayDeque<Cord>()

        fun getShortestPath(): ArrayDeque<Cord> {
            visitNext(start)
//            println("shortest found path of length: ${shortestPath.size}")
//            drawPath(shortestPath)
            return shortestPath
        }

        private fun drawPath(path: ArrayDeque<Cord>) {
            val pathMap = mapOfSize(map.width(), map.height(), '.')
            path.forEach {
                pathMap[it.y()][it.x()] = map[it]!!
            }
            pathMap.draw()
        }

        private fun visitNext(visited: Cord) {
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
                        if (adjacent.isWithin(map)
                            && canGoFromTo(visited, adjacent)
                            && currentPath.doesNotContain(adjacent)
                        ) {
                            //compare and set min of this+1 vs adjacent.cost
                            visitedCords
                                .computeIfAbsent(adjacent) { VisitedCord(start) }
                                .apply { cost = min(cost, visitedCords[visited]!!.cost+1) }
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

        private fun ArrayDeque<Cord>.doesNotContain(next: Cord) = !contains(next)

        private fun canGoFromTo(current: Cord, next: Cord): Boolean {
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

    override fun solve(input: List<String>) {

        val map = input.map { row ->
            row.mapTo(ArrayList()) { it }
        }

        val pathFinder = PathFinder(map, 0 to 0, 'E')
        println("part1: ${pathFinder.getShortestPath().size - 1}")

    }

}
