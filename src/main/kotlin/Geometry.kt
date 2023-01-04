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

enum class Direction2 {
    LEFT, RIGHT, UP, DOWN;

    fun toVec2() = Vec2.unit(this)

    fun rotate(rotation: Rotation2) = (toVec2() * rotation).toDirection2()

    companion object {
        fun of(char: Char) = of(char.uppercase())

        fun of(string: String) = when (string.uppercase()) {
            "D", "DOWN" -> DOWN
            "U", "UP" -> UP
            "R", "RIGHT" -> RIGHT
            "L", "LEFT" -> LEFT
            else -> throw IllegalArgumentException("Unknown direction")
        }
    }

}

enum class Direction3 {
    LEFT, RIGHT, UP, DOWN, FRONT, BACK;

    fun toVec3() = Vec3.unit(this)

    fun toDirection2() = when (this) {
        LEFT -> Direction2.LEFT
        RIGHT -> Direction2.RIGHT
        UP -> Direction2.UP
        DOWN -> Direction2.DOWN
        else -> null
    }

    companion object {
        fun of(char: Char) = of(char.uppercase())

        fun of(string: String) = when (string.uppercase()) {
            "D", "DOWN" -> DOWN
            "U", "UP" -> UP
            "R", "RIGHT" -> RIGHT
            "L", "LEFT" -> LEFT
            "F", "FRONT" -> FRONT
            "B", "BACK" -> BACK
            else -> throw IllegalArgumentException("Unknown direction")
        }
    }
}

data class Vec2(val x: Int, val y: Int) {

    operator fun times(rotation: Rotation2) = Vec2(
        this.x * rotation.matrix[0][0] + this.y * rotation.matrix[1][0],
        this.x * rotation.matrix[0][1] + this.y * rotation.matrix[1][1],
    )

    fun toDirection2() = when {
        x < 0 && y == 0 -> Direction2.LEFT
        x > 0 && y == 0 -> Direction2.RIGHT
        x == 0 && y < 0 -> Direction2.UP
        x == 0 && y > 0 -> Direction2.DOWN
        else -> throw IllegalStateException("only l/r/u/d are supported")
    }

    fun toVec3() = Vec3(x, y, 0)

    operator fun minus(vector: Vec2) = Vec2(x - vector.x, y - vector.y)

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

data class Vec3(val x: Int, val y: Int, val z: Int) {

    operator fun times(rotation: Rotation3) = Vec3(
        this.x * rotation.matrix[0][0] + this.y * rotation.matrix[1][0] + this.z * rotation.matrix[2][0],
        this.x * rotation.matrix[0][1] + this.y * rotation.matrix[1][1] + this.z * rotation.matrix[2][1],
        this.x * rotation.matrix[0][2] + this.y * rotation.matrix[1][2] + this.z * rotation.matrix[2][2],
    )

    fun toDirection3() = when {
        x < 0 && y == 0 && z == 0 -> Direction3.LEFT
        x > 0 && y == 0 && z == 0 -> Direction3.RIGHT
        x == 0 && y < 0 && z == 0 -> Direction3.UP
        x == 0 && y > 0 && z == 0 -> Direction3.DOWN
        x == 0 && y == 0 && z < 0 -> Direction3.FRONT
        x == 0 && y == 0 && z > 0 -> Direction3.BACK
        else -> throw IllegalStateException("only l/r/u/d/f/b are supported")
    }

    fun toVec2() = takeIf { z == 0 }
        ?.let { Vec2(x, y) }

    companion object {
        fun unit(direction: Direction3): Vec3 {
            return when (direction) {
                Direction3.LEFT -> Vec3(-1, 0, 0)
                Direction3.RIGHT -> Vec3(1, 0, 0)
                Direction3.UP -> Vec3(0, -1, 0)
                Direction3.DOWN -> Vec3(0, 1, 0)
                Direction3.FRONT -> Vec3(0, 0, -1)
                Direction3.BACK -> Vec3(0, 0, 1)
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

    operator fun plus(vector: Vec2) = Cord2(x + vector.x, y + vector.y)
    operator fun minus(vector: Vec2) = Cord2(x - vector.x, y - vector.y)
    operator fun times(scalar: Int) = Cord2(x * scalar, y * scalar)
    operator fun div(scalar: Int) = Cord2(x / scalar, y / scalar)
    operator fun rem(scalar: Int) = Cord2(x % scalar, y % scalar)

    operator fun rangeTo(to: Cord2): List<Cord2> = when {
        (x < to.x) -> (x..to.x).flatMap { itX ->
            when {
                (y < to.y) -> (y..to.y).map { Cord2(itX, it) }
                else -> (to.y..y).map { Cord2(itX, it) }
            }
        }
        else -> (to.x..x).flatMap { itX ->
            when {
                (y < to.y) -> (y..to.y).map { Cord2(itX, it) }
                else -> (to.y..y).map { Cord2(itX, it) }
            }
        }
    }

    fun toVec2() = Vec2(x, y)

    fun toCord3() = Cord3(x, y, 0)
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

    fun toVec3() = Vec3(x, y, z)

    fun toCord2() = takeIf { z == 0 }
        ?.let { Cord2(x, y) }
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
) {

    fun inverse(): Rotation3 {
        val cols = matrix[0].size
        val rows = matrix.size
        val transposedMatrix = Array(cols) { j ->
            Array(rows) { i ->
                matrix[i][j]
            }
        }
        return Rotation3(transposedMatrix, "inv($axis)")
    }

    operator fun times(other: Rotation3) = Rotation3(
        arrayOf(
            arrayOf(
                matrix[0][0] * other.matrix[0][0] + matrix[0][1] * other.matrix[1][0] + matrix[0][2] * other.matrix[2][0],
                matrix[0][0] * other.matrix[0][1] + matrix[0][1] * other.matrix[1][1] + matrix[0][2] * other.matrix[2][1],
                matrix[0][0] * other.matrix[0][2] + matrix[0][1] * other.matrix[1][2] + matrix[0][2] * other.matrix[2][2],
            ),
            arrayOf(
                matrix[1][0] * other.matrix[0][0] + matrix[1][1] * other.matrix[1][0] + matrix[1][2] * other.matrix[2][0],
                matrix[1][0] * other.matrix[0][1] + matrix[1][1] * other.matrix[1][1] + matrix[1][2] * other.matrix[2][1],
                matrix[1][0] * other.matrix[0][2] + matrix[1][1] * other.matrix[1][2] + matrix[1][2] * other.matrix[2][2],
            ),
            arrayOf(
                matrix[2][0] * other.matrix[0][0] + matrix[2][1] * other.matrix[1][0] + matrix[2][2] * other.matrix[2][0],
                matrix[2][0] * other.matrix[0][1] + matrix[2][1] * other.matrix[1][1] + matrix[2][2] * other.matrix[2][1],
                matrix[2][0] * other.matrix[0][2] + matrix[2][1] * other.matrix[1][2] + matrix[2][2] * other.matrix[2][2],
            )
        ), "$this * $other")

    override fun toString() = "$($axis')"

    companion object {
        private val cache = HashMap<Pair<Char, Int>, Rotation3>()

        fun rotateX(deg: Int) = cache.computeIfAbsent('X' to deg) {
            val matrix = arrayOf(
                arrayOf(1, 0, 0),
                arrayOf(0, cos(deg.toRad()).toInt(), -sin(deg.toRad()).toInt()),
                arrayOf(0, sin(deg.toRad()).toInt(), cos(deg.toRad()).toInt())
            )
            Rotation3(matrix, "X@$deg")
        }

        fun rotateY(deg: Int) = cache.computeIfAbsent('Y' to deg) {
            val matrix = arrayOf(
                arrayOf(cos(deg.toRad()).toInt(), 0, sin(deg.toRad()).toInt()),
                arrayOf(0, 1, 0),
                arrayOf(-sin(deg.toRad()).toInt(), 0, cos(deg.toRad()).toInt()),
            )
            Rotation3(matrix, "Y@$deg")
        }

        fun rotateZ(deg: Int) = cache.computeIfAbsent('Z' to deg) {
            val matrix = arrayOf(
                arrayOf(cos(deg.toRad()).toInt(), -sin(deg.toRad()).toInt(), 0),
                arrayOf(sin(deg.toRad()).toInt(), cos(deg.toRad()).toInt(), 0),
                arrayOf(0, 0, 1),
            )
            Rotation3(matrix, "Z@$deg")
        }

        val noRotation = rotateX(0)
    }
}

fun Int.toRad() = Math.toRadians(this.toDouble())
