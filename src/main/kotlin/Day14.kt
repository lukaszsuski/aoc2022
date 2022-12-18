import Day14.Cave.Companion.ROCK
import Day14.Cave.Companion.VOID
import java.lang.Integer.max
import java.lang.Integer.min

typealias RockPath = Pair<Cord, Cord>

private fun RockPath.from() = first
private fun RockPath.to() = second

private val VoidCord: Cord = Int.MIN_VALUE to Int.MAX_VALUE

class Day14 : Day(14) {

    override fun solve(input: List<String>) {
        val rockPaths = parseRockPaths(input)

        val cave1 = Cave(rockPaths, VOID)
        println("part1: ${
            (0..Int.MAX_VALUE).asSequence()
                .map { cave1.dropSand(500 to 0) }
                .takeWhile { it != VoidCord }
                .count()
        }")

        val cave2 = Cave(rockPaths, ROCK, 200, 200)
        println("part1: ${
            (0..Int.MAX_VALUE).asSequence()
                .map { cave2.dropSand(500 to 0) }
                .takeWhile { it != 500 to 0 }
                .count() + 1
        }")
    }

    class Cave(
        rockPaths: List<RockPath>,
        bottom: Char,
        emptyLeft: Int = 1,
        emptyRight: Int = 1,
        emptyBellow: Int = 2
    ) {
        companion object {
            const val SAND = '*'
            const val AIR = '.'
            const val ROCK = '#'
            const val VOID = 'V'
        }

        internal val map: Map2d<Char>
        private val offset: Cord

        init {
            var minX = Int.MAX_VALUE
            var maxX = 0
            var maxY = 0

            rockPaths.forEach { path ->
                minX = min(path.from().x(), minX)
                minX = min(path.to().x(), minX)
                maxX = max(path.from().x(), maxX)
                maxX = max(path.to().x(), maxX)
                maxY = max(path.from().y(), maxY)
                maxY = max(path.to().y(), maxY)
            }

            val minWidth = maxX - minX + 1
            val width = minWidth + emptyLeft + emptyRight
            val minHeight = maxY + 1
            val height = minHeight + emptyBellow
            //todo check if right offset
            offset = minX - emptyLeft to 0

            map = mapOfSize(width, height, AIR)
            repeat(width) {
                map[it to height - 1] = bottom
            }

            rockPaths.forEach {
                val (from, to) = it
                from.cordsTo(to).forEach {
                    map[it - offset] = ROCK
                }
            }
        }

        fun dropSand(sandCord: Cord): Cord {
            return tryFallBelow(sandCord - offset)
        }

        private fun tryFallBelow(sandCord: Cord): Cord {
            return when {
                map[sandCord.below()] == VOID -> {
                    VoidCord
                }
                map[sandCord.below()] == AIR -> {
                    tryFallBelow(sandCord.below())
                }
                map[sandCord.belowLeft()] == AIR -> {
                    tryFallBelow(sandCord.belowLeft())
                }
                map[sandCord.belowRight()] == AIR -> {
                    tryFallBelow(sandCord.belowRight())
                }
                else -> {
                    map[sandCord] = SAND
                    sandCord + offset
                }
            }
        }

        private fun Cord.below(): Cord = x() to y() + 1
        private fun Cord.belowLeft(): Cord = x() - 1 to y() + 1
        private fun Cord.belowRight(): Cord = x() + 1 to y() + 1

    }

    private fun parseRockPaths(input: List<String>): List<RockPath> {
        return input
            .flatMap {
                it.split(" -> ")
                    .map {
                        val (col, row) = it.trim().split(",")
                        col.toInt() to row.toInt()
                    }
                    .windowed(2)
                    .map {
                        val (from, to) = it
                        from to to
                    }
            }
    }

}

