import kotlin.streams.toList

class Day1(input: List<String>) : Day(input) {
    private var current: Int = 0
    private val calls = HashMap<Int, Int>()

    init {
        input.forEach {
            when {
                (it.isEmpty()) -> current += 1
                else -> calls.compute(current) { _, old ->
                    (old ?: 0) + it.toInt()
                }
            }
        }
    }

    override fun part1(): Any? {
        return calls.values
            .stream()
            .sorted(Comparator.reverseOrder())
            .toList()
            .maxOrNull()
    }

    override fun part2(): Any? {
        return calls.values
            .stream()
            .sorted(Comparator.reverseOrder())
            .limit(3)
            .toList()
            .sum()
    }

}