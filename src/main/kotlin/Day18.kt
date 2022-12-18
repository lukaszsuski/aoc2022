class Day18 : Day(18) {

    override fun solve(input: List<String>) {
        val cords = parse3dCords(input)
        val lava = ShapeScan(cords)
        println("part1: ${lava.countArea(ShapeScan.CountAllWhereNearestIsNotLava)}") //3346
        println("part2: ${lava.countArea(ShapeScan.CountAllWhereNearestIsOuterAir)}") //1980
    }

    private fun parse3dCords(input: List<String>): List<Cord3d> {
        return input.map {
            val (x, y, z) = it.split(",")
            Cord3d(x.toInt(), y.toInt(), z.toInt())
        }
    }

    //todo clean up and move to handydandy
    data class Cord3d(val x: Int, val y: Int, val z: Int) {
        fun adjacents() = listOf(
            Cord3d(x + 1, y, z),
            Cord3d(x, y + 1, z),
            Cord3d(x, y, z + 1),
            Cord3d(x - 1, y, z),
            Cord3d(x, y - 1, z),
            Cord3d(x, y, z - 1),
        )
    }


    class ShapeScan(
        val cords: List<Cord3d>,
    ) {
        companion object {
            const val AIR = '.'
            const val LAVA = '#'
        }

        private val box: Pair<Cord3d, Cord3d> = getBoxBounds()
        private val map: HashMap<Cord3d, Char> = cords
            .associateWith { LAVA }
            .toMap(HashMap())

        init {
            inflateOuterAir()
        }

        fun interface SurfaceCountStrategy : (ShapeScan, Cord3d) -> Boolean

        object CountAllWhereNearestIsNotLava : SurfaceCountStrategy {
            override fun invoke(lava: ShapeScan, cord: Cord3d): Boolean {
                return lava.map[cord] != LAVA
            }
        }

        object CountAllWhereNearestIsOuterAir : SurfaceCountStrategy {
            override fun invoke(lava: ShapeScan, cord: Cord3d): Boolean {
                return lava.map[cord] == AIR
            }
        }

        fun countArea(countStrategy: SurfaceCountStrategy): Int {
            return cords
                .sumOf { cord ->
                    cord.adjacents()
                        .count {
                            countStrategy(this, it)
                        }
                }
        }

        private fun inflateOuterAir() {
            //start inflating from first empty space in box
            tryInflateAir(box.first)
        }

        //need to be run with at least -Xss2m, try refactor to loop
        private fun tryInflateAir(cord: Cord3d) {
            val current = map[cord]
            if (current == null) {
                map[cord] = AIR
                cord.adjacents()
                    .filter { it.isWithin(box) }
                    .forEach { tryInflateAir(it) }
            }
        }

        private fun Cord3d.isWithin(box: Pair<Cord3d, Cord3d>): Boolean {
            return x in box.first.x..box.second.x &&
                    y in box.first.y..box.second.y &&
                    z in box.first.z..box.second.z
        }

        private fun getBoxBounds(): Pair<Cord3d, Cord3d> {
            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var minZ = Int.MAX_VALUE
            var maxX = 0
            var maxY = 0
            var maxZ = 0

            cords.forEach { cord ->
                minX = Integer.min(cord.x, minX)
                minY = Integer.min(cord.y, minY)
                minZ = Integer.min(cord.z, minZ)
                maxX = Integer.max(cord.x, maxX)
                maxY = Integer.max(cord.y, maxY)
                maxZ = Integer.max(cord.z, maxZ)
            }
            //add buffer of one box on each side to inflate outer air
            return Cord3d(minX - 1, minY - 1, minZ - 1) to Cord3d(maxX + 1, maxY + 1, maxZ + 1)
        }
    }

}

