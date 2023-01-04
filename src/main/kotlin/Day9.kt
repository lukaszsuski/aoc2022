import java.lang.Integer.max
import kotlin.math.abs

class Rope(size: Size) {

    @JvmInline
    value class Size(val value: Int) {
        init {
            require(value > 1)
        }
    }

    private var knots = ArrayList<Cord2>()
    private val tailTrailMap = HashSet<Cord2>()

    init {
        repeat(size.value) {
            knots.add(Cord2(0, 0,))
        }
    }

    fun getUniqueTailPositions() =
        tailTrailMap.size

    fun pullHead(dir: Direction2) {
        //move head
        knots[0] = (knots[0] + dir.toVec2())

        //pull remaining
        knots.windowed(2)
            .forEachIndexed { idx, adjacent ->
                knots[idx + 1] = adjacent[1].follow(adjacent[0])
            }

        //mark position of tail
        tailTrailMap.add(knots.last())
    }

    private fun Cord2.follow(other: Cord2): Cord2 =
        when {
            distanceFrom(other) > 1 -> {
                val move = other - toVec2()
                this + move.toVec2().clamped()
            }
            else -> this
        }

    private fun Cord2.distanceFrom(other: Cord2): Int =
        max(abs(x - other.x), abs(y - other.y))

    private fun Vec2.clamped(): Vec2 =
        Vec2(x.clamp(-1, 1), y.clamp(-1, 1))

    private fun Int.clamp(min: Int, max: Int): Int =
        this.coerceAtLeast(min).coerceAtMost(max)
}

class RopeMover(val input: List<String>) {
    fun move(rope: Rope) {
        input.forEach { move ->
            val (dir, cnt) = move.split(" ")
            repeat(cnt.toInt()) {
                rope.pullHead(Direction2.of(dir))
            }
        }
    }
}

class Day9(input: List<String>) : Day(input) {

    private val mover = RopeMover(input)

    override fun part1(): Any? {
        val rope = Rope(Rope.Size(2))
        mover.move(rope)
        return rope.getUniqueTailPositions()
    }

    override fun part2(): Any? {
        val rope = Rope(Rope.Size(10))
        mover.move(rope)
        return rope.getUniqueTailPositions()
    }

}


