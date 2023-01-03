abstract class Day(val input: List<String>) {
    abstract fun part1(): Any?
    abstract fun part2(): Any?

    companion object {
        fun nr(dayNr: Int) = javaClass.getResourceAsStream("inputs/${dayNr}")
            ?.let { stream ->
                Class.forName("Day${dayNr}")
                    ?.getConstructor(List::class.java)
                    ?.newInstance(stream.bufferedReader().readLines()) as Day
            }
    }
}

