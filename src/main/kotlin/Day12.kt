
class Day12 : Day(12) {


    class PathFinder(val map: Map<Char>, val start: Cord, val end: Char) {

        private val currentPath = ArrayDeque<Cord>()
        private val visited = HashMap<Cord, VisitedCord>()

        sealed class VisitedCord(var cost: Int = Int.MAX_VALUE) {

            object Start : VisitedCord(0)
        }

        private var shortestPath = ArrayDeque<Cord>()

        fun getShortestPath(): ArrayDeque<Cord> {
            visitNext(start)
//            println("shortest found path of length: ${shortestPath.size}")
//            drawPath(shortestPath)
            return shortestPath
        }

        private fun drawPath(path: ArrayDeque<Cord>) {
            val pathMap = map.map { row ->
                row.mapTo(mutableListOf()) { '.' }
            }
            path.forEach {
                pathMap[it.y()][it.x()] = map[it]!!
            }
            pathMap.forEach {
                println(it.joinToString(""))
            }
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
                visited.getAdjacent().forEach { adjacent ->
//                    println("${map.at(visited)} -> ${map.at(adjacent)}:")
//                    println("$visited -> $adjacent:")
                    if(adjacent.isWithinMap(map)
                        && canGoFromTo(visited, adjacent)
                        && currentPath.doesNotContain(adjacent)
                    ) {
                        visitNext(adjacent)
                    }
                }
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
        println("part1: ${pathFinder.getShortestPath().size-1}")

    }

}
