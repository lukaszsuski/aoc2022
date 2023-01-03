class DistinctParser(private val value: String) {

    fun findDistinctPosition(expectedUnique: Int): Int? {
        return value.withIndex()
            .windowed(expectedUnique)
            .first { it.map { it.value }.toSet().size == expectedUnique }
            .last().index + 1
    }
}

class Day6(input: List<String>) : Day(input) {

    private val parser = DistinctParser(input[0])

    override fun part1(): Any? = parser.findDistinctPosition(4)

    override fun part2(): Any? = parser.findDistinctPosition(14)
}

