class Day3 : Day(3) {

    private fun Char.priority(): Int {
        return when {
            isLowerCase() -> minus('a') + 1
            else -> minus('A') + 27
        }
    }

    data class Group(val b1: Backpack, val b2: Backpack, val b3: Backpack) {

        fun badge(): Char {
            return b1.items().intersect(b2.items()).intersect(b3.items()).first()
        }
    }

    data class Backpack(val leftCompartment: List<Char>, val rightCompartment: List<Char>) {
        fun firstCommonInBothCompartments(): Char? {
            return leftCompartment.find { item ->
                rightCompartment.contains(item)
            }
        }

        fun items(): List<Char> {
            return leftCompartment.plus(rightCompartment)
        }

        fun contains(item: Char): Boolean {
            return leftCompartment.contains(item) or rightCompartment.contains(item)
        }

        companion object {
            fun from(blob: String): Backpack {
                val left = blob.substring(0, blob.length / 2)
                val right = blob.substring(blob.length / 2)
                return Backpack(left.toList(), right.toList())
            }
        }
    }

    override fun solve(input: List<String>) {
        val part1 = input.sumOf {
            Backpack.from(it).firstCommonInBothCompartments()!!.priority()
        }
        println("part1: $part1")

        val part2 = input
            .map { Backpack.from(it) }
            .withIndex()
            .groupBy { (it.index / 3) }
            .map { Group(it.value[0].value, it.value[1].value, it.value[2].value) }
            .sumOf { it.badge().priority() }
        println("part2: $part2")
    }
}