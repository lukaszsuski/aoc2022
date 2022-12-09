class Day6 : Day(6) {

    class DistinctParser(private val value: String) {

        fun findDistinctPosition(expectedUnique: Int): Int? {
            return value.withIndex()
                .windowed(expectedUnique)
                .first { it.map { it.value }.toSet().size == expectedUnique }
                .last().index + 1
        }
    }

    override fun solve(input: List<String>) {
        val parser = DistinctParser(input[0])
        println("part1: ${parser.findDistinctPosition(4)}")
        println("part2: ${parser.findDistinctPosition(14)}")
    }

}

