import java.lang.Integer.max
import kotlin.math.abs

//todo move to Cord2
private typealias Position = Pair<Int, Int>
private typealias Move = Pair<Int, Int>

fun Position.x(): Int = first
fun Position.y(): Int = second

//todo use Direction2
enum class Direction { L, R, U, D }

class Rope(size: Size) {

    @JvmInline
    value class Size(val value: Int) {
        init {
            require(value > 1)
        }
    }

    private var knots = ArrayList<Position>()
    private val tailTrailMap = HashSet<Position>()

    init {
        repeat(size.value) {
            knots.add(0 to 0)
        }
    }

    fun getUniqueTailPositions() =
        tailTrailMap.size

    fun pullHead(dir: Direction) {
        //move head
        knots[0] = (knots[0] + dir)

        //pull remaining
        knots.windowed(2)
            .forEachIndexed { idx, adjacent ->
                knots[idx + 1] = adjacent[1].follow(adjacent[0])
            }

        //mark position of tail
        tailTrailMap.add(knots.last())
    }

    private fun Position.follow(other: Position): Position =
        when {
            distanceFrom(other) > 1 -> {
                val move = (other.x() - x()) to (other.y() - y())
                this + move.clamped()
            }
            else -> this
        }

    private fun Position.distanceFrom(other: Position): Int =
        max(abs(x() - other.x()), abs(y() - other.y()))

    private fun Move.clamped(): Move =
        x().clamp(-1, 1) to y().clamp(-1, 1)

    private fun Int.clamp(min: Int, max: Int): Int =
        this.coerceAtLeast(min).coerceAtMost(max)

    private operator fun Position.plus(move: Move): Position =
        x() + move.x() to y() + move.y()

    //todo Cord2 + Dir2.toVec2
    private operator fun Position.plus(dir: Direction): Position =
        when (dir) {
            Direction.D -> x() to y() - 1
            Direction.U -> x() to y() + 1
            Direction.R -> x() + 1 to y()
            Direction.L -> x() - 1 to y()
        }
}

class RopeMover(val input: List<String>) {
    fun move(rope: Rope) {
        input.forEach { move ->
            val (dir, cnt) = move.split(" ")
            repeat(cnt.toInt()) {
                rope.pullHead(Direction.valueOf(dir))
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


