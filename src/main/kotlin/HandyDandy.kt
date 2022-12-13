
typealias Map<T> = List<ArrayList<T>>
typealias Cord = Pair<Int, Int>


operator fun <T> Map<T>.get(cord: Cord) = this.getOrNull(cord.y())?.getOrNull(cord.x())
operator fun <T> Map<T>.set(cord: Cord, value: T) { this[cord.y()][cord.x()] = value }
fun <T> Map<T>.width() = this[0].size
fun <T> Map<T>.height() = this.size
fun <T> Map<T>.find(item: T): Cord = this.withIndex()
    .map { row -> row.value.indexOfFirst { it == item } to row.index }
    .first()

fun <T> mapOfSize(width: Int, height: Int, init: T): Map<T> {
    return ArrayList<ArrayList<T>>().apply {
        repeat(height) {
            add(ArrayList<T>().apply {
                repeat(width) { add(init) }
            })
        }
    }
}

fun <T> Map<T>.draw() {
    this.forEach {
        println(it.joinToString(""))
    }
}

fun Cord.getAdjacent() = listOf(x() + 1 to y(), x() - 1 to y(), x() to y() + 1, x() to y() - 1)
operator fun Cord.plus(other: Cord): Cord = x() + other.x() to y() + other.y()
operator fun Cord.minus(other: Cord): Cord = x() - other.x() to y() - other.y()
fun <T> Cord.isWithinMap(map: Map<T>) = x() >= 0 && x() < map.width() && y() >= 0 && y() < map.height()
fun Cord.cordsTo(to: Cord): List<Cord> = when {
    (x() == to.x()) -> {
        println("draw vertical from $this $to")
        when  {
            (y() < to.y()) -> (y()..to.y()).map { x() to it }
            (y() > to.y()) -> (to.y()..y()).map { x() to it }
            else -> emptyList()
        }
    }
    (y() == to.y()) -> {
        println("draw horizontal from $this to $to")
        when  {
            (x() < to.x()) -> (x()..to.x()).map { it to y() }
            (x() > to.x()) -> (to.x()..x()).map { it to y() }
            else -> emptyList()
        }
    }
    else -> { throw IllegalArgumentException("from and to must be in straight line")}
}

