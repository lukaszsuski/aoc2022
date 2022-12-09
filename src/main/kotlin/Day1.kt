import kotlin.streams.toList

class Day1 : Day(1) {
    private var current: Int = 0

    override fun solve(input: List<String>) {
        val calls = HashMap<Int, Int>();
        input.forEach {
            when {
                (it.isEmpty()) -> current += 1
                else -> calls.compute(current) { _, old ->
                    (old ?: 0) + it.toInt()
                }
            }
        }

        val part1 = calls.values
            .stream()
            .sorted(Comparator.reverseOrder())
            .toList()
            .maxOrNull()

        println("part1: $part1")

        val part2 = calls.values
            .stream()
            .sorted(Comparator.reverseOrder())
            .limit(3)
            .toList()
            .sum()
        println("part2: $part2")
    }

}