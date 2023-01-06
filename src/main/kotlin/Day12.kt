class PathFinder(
    val map: Map2<Char>,
    private val startCord: Cord2,
    private val end: Char,
    private val traverseRules: TraverseRules
) {

    data class SearchResult(
        val cord: Cord2,
        val distance: Int
    )

    fun interface TraverseRules {
        fun canGoFromTo(from: Char, to: Char): Boolean
    }

    private data class VisitedCord(
        val cord: Cord2,
        var prev: Cord2? = null,
        var cost: Int = Int.MAX_VALUE,
        var visited: Boolean = false
    ) {
        override fun equals(other: Any?) = other is VisitedCord && cord == other.cord
        override fun hashCode() = cord.hashCode()
    }

    private val visitedCords: HashMap<Cord2, VisitedCord> by lazy {
        HashMap<Cord2, VisitedCord>().also { visitedCords ->
            val startVisited = visitedCords.compute(startCord) { _, _ -> VisitedCord(startCord, cost = 0, visited = true) }
            val toVisit = HashSet<VisitedCord>()
            toVisit.add(startVisited!!)

            while (toVisit.isNotEmpty()) {
                val visited = toVisit
                    .sortedBy(VisitedCord::cost)
                    .first()

                val value = map[visited.cord]!!

                visited.cord.adjacents()
                    .mapNotNull { map[it]?.let { ch -> it to ch } }
                    .filter { (adjCord, adjValue) ->
                        traverseRules.canGoFromTo(value, adjValue) && visitedCords[adjCord]?.visited != true
                    }
                    .mapNotNull { (adjCord, _) ->
                        visitedCords.computeIfAbsent(adjCord) { VisitedCord(it) }
                            .takeIf { !it.visited }
                            ?.apply {
                                if (cost > visitedCords[visited.cord]!!.cost + 1) {
                                    cost = visitedCords[visited.cord]!!.cost + 1
                                    prev = visited.cord
                                }
                            }
                    }
                    .also { visited.visited = true }
                    .toCollection(toVisit)

                toVisit.remove(visited)
            }
        }
    }

    fun getShortestPath(): SearchResult {
        return map.findAll(end)
            .mapNotNull { visitedCords[it] }
            .minBy { it.cost }
            .let { SearchResult(it.cord, it.cost) }
    }

    fun drawPathTo(location: SearchResult) {
        val path = ArrayDeque<VisitedCord>()
        var it = visitedCords[location.cord]
        while (it?.prev != null) {
            path.addFirst(it)
            it = visitedCords[it.prev]
        }

        val pathMap = Map2.ofSize(map.width, map.height, ".")
        map.forEachIndexed { row, list ->
            list.forEachIndexed { col, char -> pathMap[Cord2(col, row)] = char.toString() }
        }
        path.forEach {
            pathMap[it.cord] = pathMap[it.cord]?.red()!!
        }
        println(pathMap)
    }

    private fun String.red(): String = "\u001B[31m${this}\u001B[0m"

}

class Day12(input: List<String>) : Day(input) {

    val map = Map2(input.map { row ->
        row.mapTo(ArrayList()) { it }
    })

    override fun part1(): Any? {
        val ascendingRules = PathFinder.TraverseRules { from, to ->
            when {
                from == 'S' -> true
                from == 'z' && to == 'E' -> true
                to in 'a'..'z' -> to - from <= 1
                else -> false
            }
        }
        val pathFinder = PathFinder(map, map.findFirst('S'), 'E', ascendingRules)
        return pathFinder.getShortestPath()
//            .also { pathFinder.drawPathTo(it) }
            .distance
        //490
    }

    override fun part2(): Any? {
        val descendingRules = PathFinder.TraverseRules { from, to ->
            when {
                from == 'E' -> 'z' - to <= 1
                to in 'a'..'z' -> from - to <= 1
                else -> false
            }
        }
        val pathFinder = PathFinder(map, map.findFirst('E'), 'a', descendingRules)
        return pathFinder.getShortestPath()
//            .also { pathFinder.drawPathTo(it) }
            .distance
//        488
    }

}
