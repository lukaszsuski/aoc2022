import java.lang.Integer.max
import kotlin.reflect.KClass

data class Character(
    val map: StrangeBoard<*>, var pos: Cord2, var direction: Direction2
) {
    fun move(move: BoardMove) {
        when (move) {
            is BoardMove.TurnMove -> direction = direction.rotate(move.turn)
            is BoardMove.Walk -> repeat(move.nrFields) {
                val (nextPos, nextDirection) = map.nextPos(pos, direction)
                pos = nextPos
                direction = nextDirection
            }
        }
    }

    fun getPassword(): Int {
        return 1000 * (pos.y + 1) + 4 * (pos.x + 1) + direction.let {
            when (it) {
                Direction2.RIGHT -> 0
                Direction2.DOWN -> 1
                Direction2.LEFT -> 2
                Direction2.UP -> 3
            }
        }
    }
}


class StrangeBoard<T : StrangeBoard.WrapPolicy>(
    private var map: Map2<Char>, wrapPolicyClass: KClass<T>
) {
    private val wrapPolicy: WrapPolicy = wrapPolicyClass.constructors.first().call(map)

    fun nextPos(pos: Cord2, direction: Direction2): Pair<Cord2, Direction2> {
        val next = pos + direction.toVec2()
        return when (map[next]) {
            OPEN_TILE -> next to direction
            SOLID_WALL -> pos to direction
            null, EMPTY -> {
                val (wrappedNextPos, wrappedNextDir) = wrapPolicy.wrapNext(pos, direction)
                when (map[wrappedNextPos]) {
                    OPEN_TILE -> wrappedNextPos to wrappedNextDir
                    SOLID_WALL -> pos to direction
                    else -> throw IllegalStateException("cheating!")
                }
            }
            else -> throw IllegalStateException("cheating!")
        }
    }

    sealed class WrapPolicy(val map: Map2<Char>) {
        abstract fun wrapNext(current: Cord2, direction: Direction2): Pair<Cord2, Direction2>
    }

    class SimpleWrap(map: Map2<Char>) : WrapPolicy(map) {
        override fun wrapNext(current: Cord2, direction: Direction2): Pair<Cord2, Direction2> {
            val nextPos = when (direction) {
                Direction2.LEFT -> map.lastHorizontalFrom(current) { it == OPEN_TILE || it == SOLID_WALL }
                Direction2.RIGHT -> map.firstHorizontalFrom(current) { it == OPEN_TILE || it == SOLID_WALL }
                Direction2.UP -> map.lastVerticalFrom(current) { it == OPEN_TILE || it == SOLID_WALL }
                Direction2.DOWN -> map.firstVerticalFrom(current) { it == OPEN_TILE || it == SOLID_WALL }
            }
            return nextPos to direction
        }

        private fun <T> Map2<T>.firstHorizontalFrom(cord: Cord2, pred: (T) -> Boolean) =
            map.getOrNull(cord.y)?.indexOfFirst(pred)?.let { Cord2(it, cord.y) }!!

        private fun <T> Map2<T>.lastHorizontalFrom(cord: Cord2, pred: (T) -> Boolean) =
            map.getOrNull(cord.y)?.indexOfLast(pred)?.let { Cord2(it, cord.y) }!!

        private fun <T> Map2<T>.firstVerticalFrom(cord: Cord2, pred: (T) -> Boolean) =
            Cord2(cord.x, map.indexOfFirst { it.getOrNull(cord.x)?.let(pred) == true })

        private fun <T> Map2<T>.lastVerticalFrom(cord: Cord2, pred: (T) -> Boolean) =
            Cord2(cord.x, map.indexOfLast { it.getOrNull(cord.x)?.let(pred) == true })
    }

    class CubicWrap(
        map: Map2<Char>
    ) : WrapPolicy(map) {
        sealed class MaybeFace
        object NoFace : MaybeFace()
        data class Face(
            var cord: Cord2,
            var rotation: Rotation3 = Rotation3.noRotation
        ) : MaybeFace() {
            fun rotate(r: Rotation3) {
                rotation *= r
            }

            fun normalDirection() = Vec3.unit(Direction3.FRONT) * rotation
        }

        private val faceWrapsCache = mutableMapOf<Pair<Cord2, Direction2>, Face>()

        private val cubeEdgeLength = max(map.height, map.width) / 4

        //maps 2d face cords to 3d faces that will be folded (transformed)
        private fun cubeFaces() = (Cord2(0, 0)..Cord2(3, 3)).associateWith { faceCord ->
            map[faceCord * cubeEdgeLength]?.let {
                when (it) {
                    EMPTY -> NoFace
                    else -> Face(faceCord)
                }
            } ?: NoFace
        }


        override fun wrapNext(current: Cord2, direction: Direction2): Pair<Cord2, Direction2> {
            val originFaceCord = current / cubeEdgeLength

            val wrappedFace = faceWrapsCache.computeIfAbsent(originFaceCord to direction) {
                val faces = cubeFaces()
                foldFaces(faces, faces[originFaceCord] as Face)
                faces.values.filterIsInstance<Face>().first {
                    //filter by the direction of face normal vector
                    it.normalDirection().toVec2() == direction.toVec2()
                }.also {
                    //fold adjacent face to make it overlay (inverted) with current face,
                    // so that face local current pos and destination pos overlaps and directions are opposite
                    val overlayMatrix = getFoldRotation(originFaceCord, originFaceCord + direction.toVec2())
                    it.rotate(overlayMatrix)
                }
            }

            val destCord = destinationCord(wrappedFace, current)
            val destinationDirection = destinationDirection(direction, wrappedFace)

            return destCord to destinationDirection
        }

        private fun destinationDirection(direction: Direction2, wrappedFace: Face): Direction2 {
            //direction of folded and overlapped face is opposite to current direction
            //find projection by rotating opposite  of current and applying inverse rotation of folded face
            val destDirection3 =
                direction.rotate(Rotation2.rotate(180)).toVec2().toVec3() * wrappedFace.rotation.inverse()
            return destDirection3.toDirection3().toDirection2()!!
        }

        private fun destinationCord(wrappedFace: Face, current: Cord2): Cord2 {
            //restores position on wrapped face by undoing rotation matrix

            //trace wrapped face corners to ease restoring of mapped cord
            val positionWithFaceCorners = listOf(
                wrappedFace.cord * cubeEdgeLength,
                wrappedFace.cord * cubeEdgeLength + Vec2(cubeEdgeLength - 1, 0),
                wrappedFace.cord * cubeEdgeLength + Vec2(0, cubeEdgeLength - 1),
                wrappedFace.cord * cubeEdgeLength + Vec2(cubeEdgeLength - 1, cubeEdgeLength - 1)
            ).associateWithTo(HashMap()) { it }
                .also { it[current] = current }

            val rotatedProjection = positionWithFaceCorners
                //work on face local cords - offset to (0,0)
                .mapValues { (_, v) -> v % cubeEdgeLength }
                //translate to 3d space
                .mapValues { (_, v) -> v.toCord3().toVec3() }
                //undo wrapped face rotation to restore local destination cord
                .mapValues { (_, v) -> v * wrappedFace.rotation.inverse() }
                //project back to 2d space
                .mapValues { (_, v) -> v.toVec2()!! }

            //get most left-upper corner
            val zeroOffset = rotatedProjection.values.sortedBy(Vec2::x).sortedBy(Vec2::y).first()

            //re-hook to (0,0) to get face local destination cord
            val zeroHookedProjection = rotatedProjection
                .mapValues { (_, v) -> v - zeroOffset }

            //get global destination cord
            return wrappedFace.cord * cubeEdgeLength + zeroHookedProjection[current]!!
        }

        private fun foldFaces(
            faceMap: Map<Cord2, MaybeFace>, foldedFace: Face, previousFace: Face? = null
        ) {
            //first fold adjacents so we need to only fold around x and y axes
            foldedFace.cord.adjacents().map { faceMap[it] }.filterIsInstance<Face>()
                .filterNot { it == previousFace }.forEach { foldFaces(faceMap, it, foldedFace) }

            //then rotate current with all its adjacents
            val rotationMatrix = getFoldRotation(previousFace?.cord ?: foldedFace.cord, foldedFace.cord)
            rotateFaces(faceMap, foldedFace, rotationMatrix, previousFace)
        }

        private fun rotateFaces(
            faceMap: Map<Cord2, MaybeFace>, foldedFace: Face, rotationMatrix: Rotation3, previousFace: Face? = null
        ) {
            if (previousFace != null) {
                foldedFace.cord.adjacents().map { faceMap[it] }.filterIsInstance<Face>()
                    .filterNot { it == previousFace }
                    .forEach { rotateFaces(faceMap, it, rotationMatrix, foldedFace) }
            }
            foldedFace.rotate(rotationMatrix)
        }

        private fun getFoldRotation(
            fixed: Cord2, folded: Cord2
        ): Rotation3 = when {
            //rotates back / behind the screen
            fixed.x > folded.x -> /*folded is on left*/ Rotation3.rotateY(-90)
            fixed.x < folded.x -> /*folded is on right*/ Rotation3.rotateY(90)
            fixed.y > folded.y -> /*folded is above*/ Rotation3.rotateX(90)
            fixed.y < folded.y -> /*folded is below*/ Rotation3.rotateX(-90)
            fixed == folded -> Rotation3.noRotation
            else -> throw IllegalArgumentException("Unsupported face relation")
        }
    }

    companion object {
        const val SOLID_WALL = '#'
        const val OPEN_TILE = '.'
        const val EMPTY = ' '
    }
}

sealed class BoardMove {
    data class TurnMove(val turn: Rotation2) : BoardMove()
    data class Walk(val nrFields: Int) : BoardMove()
}

class Day22(input: List<String>) : Day(input) {

    private val map = parseMap(input)

    private val moves = parseMoves(input)

    override fun part1(): Any? {
        val strangeBoard = StrangeBoard(map, StrangeBoard.SimpleWrap::class)

        val player = Character(strangeBoard, map.find('.'), Direction2.RIGHT)

        moves.forEach { player.move(it) }
        return player.getPassword() //117102
    }

    override fun part2(): Any? {
        val strangeBoard = StrangeBoard(map, StrangeBoard.CubicWrap::class)
        val player = Character(strangeBoard, map.find('.'), Direction2.RIGHT)
        moves.forEach { player.move(it) }
        return player.getPassword() //135297
    }

    private fun parseMap(input: List<String>): Map2<Char> {
        return input.takeWhile { it.isNotBlank() }.map { row ->
            row.mapTo(ArrayList()) { it }
        }.let { Map2(it) }
    }

    private fun parseMoves(input: List<String>): List<BoardMove> {
        val moves = mutableListOf<BoardMove>()
        val movesString = input.last()
        var pos = 0
        while (pos < movesString.length) {
            val remaining = movesString.substring(pos)
            val move: BoardMove = when {
                remaining.first().isDigit() -> {
                    remaining.takeWhile { it.isDigit() }.also { pos += it.length }.let { BoardMove.Walk(it.toInt()) }
                }
                remaining.first().isLetter() -> {
                    pos += 1
                    BoardMove.TurnMove(Rotation2.rotate(remaining.first()))
                }
                else -> throw IllegalStateException("Failed to parse remaining: $remaining")
            }
            moves.add(move)
        }
        return moves
    }
}
