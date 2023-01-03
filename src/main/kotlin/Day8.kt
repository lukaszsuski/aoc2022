class TreeMap(val map: List<List<Int>>) {
    private val ySize = map.size
    private val xSize = map[0].size

    fun visibleTrees(): Int {
        return map.withIndex()
            .sumOf { column ->
                column.value.withIndex()
                    .count { row -> isVisible(column.index, row.index, row.value) }
            }
    }

    fun highestScenicScore(): Int {
        return map.withIndex()
            .filter { it.index in 1 until ySize-1 }
            .maxOf { row ->
                row.value.withIndex()
                    .filter { it.index in 1 until xSize-1 }
                    .maxOf { col -> scenicScore(row.index, col.index, col.value) }
            }
    }

    private fun scenicScore(row: Int, col: Int, value: Int): Int {

        var visibleOnLeft = map[row]
            .filterIndexed { idx, _ -> idx < col }
            .takeLastWhile { it < value }
            .count()
        if (visibleOnLeft < distanceFrom(Direction2.LEFT, row, col)) visibleOnLeft += 1

        var visibleOnRight = map[row]
            .filterIndexed { idx, _ -> idx > col }
            .takeWhile { it < value }
            .count()
        if (visibleOnRight < distanceFrom(Direction2.RIGHT, row, col)) visibleOnRight += 1

        var visibleOnTop = map
            .filterIndexed { idx, _ -> idx < row }
            .takeLastWhile { it[col] < value }
            .count()
        if (visibleOnTop < distanceFrom(Direction2.UP, row, col)) visibleOnTop += 1

        var visibleOnBottom = map
            .filterIndexed { idx, _ -> idx > row }
            .takeWhile { it[col] < value }
            .count()
        if (visibleOnBottom < distanceFrom(Direction2.DOWN, row, col)) visibleOnBottom += 1

        return visibleOnLeft * visibleOnRight * visibleOnTop * visibleOnBottom
    }

    private fun distanceFrom(dir: Direction2, row: Int, col: Int): Int {
        return when(dir) {
            Direction2.LEFT  -> col
            Direction2.RIGHT -> xSize - col - 1
            Direction2.UP    -> row
            Direction2.DOWN  -> ySize - row - 1
        }
    }

    private fun isVisible(col: Int, row: Int, value: Int): Boolean {
        val visibleFromTop = map[col]
            .filterIndexed { idx, _ -> idx < row }
            .all { it < value }

        val visibleFromBottom = map[col]
            .filterIndexed { idx, _ -> idx > row }
            .all { it < value }

        val visibleFromLeft = map
            .filterIndexed { idx, _ -> idx < col }
            .all { it[row] < value }

        val visibleFromRight = map
            .filterIndexed { idx, _ -> idx > col }
            .all { it[row] < value }

        return visibleFromTop or visibleFromBottom or visibleFromLeft or visibleFromRight
    }

}

class Day8(input: List<String>) : Day(input) {

    private val treeMap = TreeMap(input.map {
        it.toList().map {
            it.digitToInt()
        }
    })

    override fun part1(): Any? = treeMap.visibleTrees() //1787

    override fun part2(): Any? = treeMap.highestScenicScore() //440640

}



