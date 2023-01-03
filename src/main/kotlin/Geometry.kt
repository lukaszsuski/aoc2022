import java.util.function.BiFunction
import kotlin.math.cos
import kotlin.math.sin

data class Map2<T>(val map: List<ArrayList<T>>) {

    val width = map.maxOf { it.size }

    val height = map.size

    fun find(item: T): Cord2 = map.withIndex()
        .map { row -> Cord2(row.value.indexOfFirst { it == item }, row.index) }
        .first()

    operator fun get(cord: Cord2) = map.getOrNull(cord.y)?.getOrNull(cord.x)

    operator fun set(cord: Cord2, value: T) {
        map[cord.y][cord.x] = value
    }

    override fun toString(): String {
        return map.map {
            println(it.joinToString(", "))
        }.joinToString { "\n" }
    }

    companion object {
        fun <T> ofSize(width: Int, height: Int, init: T): Map2<T> {
            return Map2(ArrayList<ArrayList<T>>().apply {
                repeat(height) {
                    add(ArrayList<T>().apply {
                        repeat(width) { add(init) }
                    })
                }
            })
        }

        fun <T> of(vararg rows: ArrayList<T>): Map2<T> {
            return Map2(rows.toList())
        }
    }
}

enum class Turn {
    LEFT, RIGHT;

    companion object {
        fun of(char: Char) = when (char.uppercase()) {
            "L" -> LEFT
            "R" -> RIGHT
            else -> throw IllegalArgumentException("unknown turn")
        }
    }
}

enum class Direction2 {

    LEFT {
        override fun turn(turn: Turn) = when (turn) {
            Turn.LEFT -> DOWN
            Turn.RIGHT -> UP
        }
    },
    RIGHT {
        override fun turn(turn: Turn) = when (turn) {
            Turn.LEFT -> UP
            Turn.RIGHT -> DOWN
        }
    },
    UP {
        override fun turn(turn: Turn) = when (turn) {
            Turn.LEFT -> LEFT
            Turn.RIGHT -> RIGHT
        }
    },
    DOWN {
        override fun turn(turn: Turn) = when (turn) {
            Turn.LEFT -> RIGHT
            Turn.RIGHT -> LEFT
        }
    };

    abstract fun turn(turn: Turn): Direction2

    fun turn(turn: Turn, times: Int = 1): Direction2 {
        var direction = this
        repeat(times) {
            direction = direction.turn(turn)
        }
        return direction
    }

    operator fun minus(other: Direction2): Int {
        //y points down!!
        var tmp = this
        var angle = 0
        while (tmp != other) {
            tmp = tmp.turn(Turn.RIGHT)
            angle += 90
        }
        return angle
    }

}

enum class Direction3 {
    LEFT, RIGHT, UP, DOWN, FRONT, BACK;
}

data class Vec2(val x: Int, val y: Int) {

    operator fun times(rotation: Rotation2) = Vec2(
        this.x * rotation.matrix[0][0] + this.y * rotation.matrix[1][0],
        this.x * rotation.matrix[0][1] + this.y * rotation.matrix[1][1],
    )

    fun direction(): Direction2 {
        return when {
            x < 0 && y == 0 ->  Direction2.LEFT
            x > 0 && y == 0 ->  Direction2.RIGHT
            x == 0 && y < 0 ->  Direction2.UP
            x == 0 && y > 0 ->  Direction2.DOWN
            else -> throw IllegalStateException("only l/r/u/d are supported")
        }
    }

    companion object {
        fun unit(direction: Direction2): Vec2 {
            return when (direction) {
                Direction2.LEFT -> Vec2(-1, 0)
                Direction2.RIGHT -> Vec2(1, 0)
                Direction2.UP -> Vec2(0, -1)
                Direction2.DOWN -> Vec2(0, 1)
            }
        }
    }
}



data class Cord2(val x: Int, val y: Int) {
    fun adjacents() = listOf(
        Cord2(x + 1, y),
        Cord2(x - 1, y),
        Cord2(x, y + 1),
        Cord2(x, y - 1)
    )

    fun <T> isWithin(map: Map2<T>) = x >= 0 && x < map.width && y >= 0 && y < map.height
    operator fun plus(other: Cord2) = Cord2(x + other.x, y + other.y)
    operator fun plus(direction: Direction2) = when (direction) {
        Direction2.LEFT -> Cord2(x - 1, y)
        Direction2.RIGHT -> Cord2(x + 1, y)
        Direction2.UP -> Cord2(x, y - 1)
        Direction2.DOWN -> Cord2(x, y + 1)
    }
    operator fun minus(other: Cord2) = Cord2(x - other.x, y - other.y)
    operator fun times(value: Int) = Cord2(x * value, y * value)
    //todo same as Vec2
    operator fun times(rotation: Rotation2) = Cord2(
        this.x * rotation.matrix[0][0] + this.y * rotation.matrix[1][0],
        this.x * rotation.matrix[0][1] + this.y * rotation.matrix[1][1],
    )
    operator fun div(value: Int) = Cord2(x / value, y / value)
    operator fun rem(value: Int) = Cord2(x % value, y % value)

    operator fun rangeTo(to: Cord2): List<Cord2> = when {
        (x == to.x) -> {
            when {
                (y < to.y) -> (y..to.y).map { Cord2(x, it) }
                (y > to.y) -> (to.y..y).map { Cord2(x, it) }
                else -> emptyList()
            }
        }
        (y == to.y) -> {
            when {
                (x < to.x) -> (x..to.x).map { Cord2(it, y) }
                (x > to.x) -> (to.x..x).map { Cord2(it, y) }
                else -> emptyList()
            }
        }
        else -> {
            throw IllegalArgumentException("from and to must be in straight line")
        }
    }

    companion object {
        val ZERO: Cord2 = Cord2(0, 0)
    }

}

data class Cord3(val x: Int, val y: Int, val z: Int) {
    fun adjacents() = listOf(
        Cord3(x + 1, y, z),
        Cord3(x, y + 1, z),
        Cord3(x, y, z + 1),
        Cord3(x - 1, y, z),
        Cord3(x, y - 1, z),
        Cord3(x, y, z - 1),
    )

    operator fun times(rotation: Rotation3) = Cord3(
        this.x * rotation.matrix[0][0] + this.y * rotation.matrix[1][0] + this.z * rotation.matrix[2][0],
        this.x * rotation.matrix[0][1] + this.y * rotation.matrix[1][1] + this.z * rotation.matrix[2][1],
        this.x * rotation.matrix[0][2] + this.y * rotation.matrix[1][2] + this.z * rotation.matrix[2][2],
    )
}





class Rotation2(
    val matrix: Array<Array<Int>>,
    private val deg: Int
) {

    override fun toString() = "($deg')"

    companion object {
        private val cache = HashMap<Int, Rotation2>()

        fun rotate(deg: Int) = cache.computeIfAbsent(deg) {
            val matrix = arrayOf(
                arrayOf(cos(deg.toRad()).toInt(), -sin(deg.toRad()).toInt()),
                arrayOf(sin(deg.toRad()).toInt(), cos(deg.toRad()).toInt())
            )
            Rotation2(matrix, deg)
        }

        fun rotate(dir: Char): Rotation2 {
            return when (dir) {
                'L' -> rotate(90)
                'R' -> rotate(-90)
                else -> throw IllegalArgumentException("not supported dir $dir")
            }
        }
    }
}

class Rotation3(
    val matrix: Array<Array<Int>>,
    private val axis: String,
    private val deg: Int
) {

    fun inverse(): Rotation3 {
            val cols = matrix[0].size
            val rows = matrix.size
        val transposedMatrix = Array(cols) { j ->
            Array(rows) { i ->
                matrix[i][j]
            }
        }
        return Rotation3(transposedMatrix, "inv($axis)", deg)
    }

    override fun toString() = "$axis($deg')"

    companion object {
        private val cache = HashMap<Pair<Char, Int>, Rotation3>()

        fun rotateX(deg: Int) = cache.computeIfAbsent('X' to deg) {
            val matrix = arrayOf(
                arrayOf(1, 0, 0),
                arrayOf(0, cos(deg.toRad()).toInt(), -sin(deg.toRad()).toInt()),
                arrayOf(0, sin(deg.toRad()).toInt(), cos(deg.toRad()).toInt())
            )
            Rotation3(matrix, "X", deg)
        }

        fun rotateY(deg: Int) = cache.computeIfAbsent('Y' to deg) {
            val matrix = arrayOf(
                arrayOf(cos(deg.toRad()).toInt(), 0, sin(deg.toRad()).toInt()),
                arrayOf(0, 1, 0),
                arrayOf(-sin(deg.toRad()).toInt(), 0, cos(deg.toRad()).toInt()),
            )
            Rotation3(matrix, "Y", deg)
        }

        fun rotateZ(deg: Int) = cache.computeIfAbsent('Z' to deg) {
            val matrix = arrayOf(
                arrayOf(cos(deg.toRad()).toInt(), -sin(deg.toRad()).toInt(), 0),
                arrayOf(sin(deg.toRad()).toInt(), cos(deg.toRad()).toInt(), 0),
                arrayOf(0, 0, 1),
            )
            Rotation3(matrix, "Z", deg)
        }

        val noRotation = rotateX(0)


    }

}

fun Int.toRad() = Math.toRadians(this.toDouble())

fun <K, V> HashMap<K, V>.forEachCompute(remapping: BiFunction<K, V, V>) {
    forEach { (key, value) ->
        set(key, remapping.apply(key, value))
    }
}

