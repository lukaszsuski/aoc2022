
typealias Map2d<T> = List<ArrayList<T>>
typealias Cord = Pair<Int, Int>

operator fun <T> Map2d<T>.get(cord: Cord) = this.getOrNull(cord.y())?.getOrNull(cord.x())
operator fun <T> Map2d<T>.set(cord: Cord, value: T) { this[cord.y()][cord.x()] = value }
fun <T> Map2d<T>.width() = this[0].size
fun <T> Map2d<T>.height() = this.size
fun <T> Map2d<T>.find(item: T): Cord = this.withIndex()
    .map { row -> row.value.indexOfFirst { it == item } to row.index }
    .first()

fun <T> mapOfSize(width: Int, height: Int, init: T): Map2d<T> {
    return ArrayList<ArrayList<T>>().apply {
        repeat(height) {
            add(ArrayList<T>().apply {
                repeat(width) { add(init) }
            })
        }
    }
}

fun <T> Map2d<T>.draw() {
    this.forEach {
        println(it.joinToString(""))
    }
}

fun Cord.adjacents() = listOf(x() + 1 to y(), x() - 1 to y(), x() to y() + 1, x() to y() - 1)
operator fun Cord.plus(other: Cord): Cord = x() + other.x() to y() + other.y()
operator fun Cord.minus(other: Cord): Cord = x() - other.x() to y() - other.y()
fun <T> Cord.isWithin(map: Map2d<T>) = x() >= 0 && x() < map.width() && y() >= 0 && y() < map.height()
fun Cord.cordsTo(to: Cord): List<Cord> = when {
    (x() == to.x()) -> {
        when  {
            (y() < to.y()) -> (y()..to.y()).map { x() to it }
            (y() > to.y()) -> (to.y()..y()).map { x() to it }
            else -> emptyList()
        }
    }
    (y() == to.y()) -> {
        when  {
            (x() < to.x()) -> (x()..to.x()).map { it to y() }
            (x() > to.x()) -> (to.x()..x()).map { it to y() }
            else -> emptyList()
        }
    }
    else -> { throw IllegalArgumentException("from and to must be in straight line")}
}

