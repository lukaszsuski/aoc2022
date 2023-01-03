data class SectionRange(val from: Int, val to: Int) {
    fun fullyOverlapsWith(other: SectionRange): Boolean {
        return from >= other.from && to <= other.to
    }

    fun overlapsWith(other: SectionRange): Boolean {
        return Integer.max(from, other.from) <= Integer.min(to, other.to)
    }

    companion object {
        fun from(range: String): SectionRange {
            val fromTo = range.split('-')
            return SectionRange(fromTo[0].toInt(), fromTo[1].toInt())
        }
    }
}

class Day4(input: List<String>) : Day(input) {

    override fun part1(): Any? {
        return input
            .map {
                val ranges = it.split(",")
                val left = SectionRange.from(ranges[0].trim())
                val right = SectionRange.from(ranges[1].trim())
                left.fullyOverlapsWith(right) or right.fullyOverlapsWith(left)
            }.count { it }
    }

    override fun part2(): Any? {
        return input
            .map {
                val ranges = it.split(",")
                val left = SectionRange.from(ranges[0].trim())
                val right = SectionRange.from(ranges[1].trim())
                left.overlapsWith(right)
            }.count { it }
    }

}