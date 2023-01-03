import java.lang.Integer.max
import kotlin.reflect.KClass

class Day22 : Day(22) {

    data class Character(
        val map: StrangeBoard<*>, var pos: Cord2, var direction: Direction2
    ) {
        fun move(move: Move) {
            when (move) {
                is Move.TurnMove -> direction = direction.turn(move.turn)
                is Move.Walk -> repeat(move.nrFields) {
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
            val next = pos + direction
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
                var normal: Cord3 = Cord3(0, 0, -1), //normal vec to xy plane
                var direction: Cord3 = Cord3(0, -1, 0), //vec pointing up (towards negative y)
            ) : MaybeFace() {
                fun rotate(rotation: Rotation3) {
                    normal *= rotation
                    direction *= rotation
                }

                fun xyFaceNormalDirection() = projectTo2dDirection(normal)

                fun xyFaceUpDirection() = projectTo2dDirection(direction)

                private fun projectTo2dDirection(cord: Cord3) = when (cord) {
                    Cord3(-1, 0, 0) -> Direction2.LEFT
                    Cord3(1, 0, 0) -> Direction2.RIGHT
                    Cord3(0, -1, 0) -> Direction2.UP
                    Cord3(0, 1, 0) -> Direction2.DOWN
                    else -> null
                }
            }

            private val faceWrapsCache = mutableMapOf<Pair<Cord2, Direction2>, Face>()

            private val cubeEdgeLength = max(map.height, map.width) / 4

            //maps 2d face cords to 3d faces that will be folded (transformed)
            //todo replace with (Cord2(0,0) .. Cord2(3,3))
            private fun cubeFaces() = (0..3).flatMap { y ->
                (0..3).map { x ->
                    Cord2(x, y)
                }
            }.associateWith { faceCord ->
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
                            it.xyFaceNormalDirection() == direction
                        }.also {
                            //unfold adjacent face to get 2d direction projection
                            val unfoldMatrix = getFoldRotation(originFaceCord, originFaceCord + direction).inverse()
                            it.rotate(unfoldMatrix)
                        }
                }

                //how much destination face is rotated
                val wrappedFaceDirectionDiff = wrappedFace.xyFaceUpDirection()!! - Direction2.UP
                val edgeRotationMatrix = Rotation2.rotate(wrappedFaceDirectionDiff)

                //rotate destination edges to align with origin face
                val destEdgesMap = wrappedFaceEdgeMapping(wrappedFace)
                destEdgesMap.forEachCompute { _, v -> v * edgeRotationMatrix }

                //get left/upper-most cord and offset to (0,0) to work on face local cords
                val destFaceLocalOffset = destEdgesMap.values.sortedBy(Cord2::x).sortedBy(Cord2::y).first()
                destEdgesMap.forEachCompute { _, v -> v - destFaceLocalOffset }

                //get origin face (0,0) local cords
                val localOriginFaceCord = current % cubeEdgeLength

                //get destination cord as face local cord
                val destEdgePos = when (direction) {
                    Direction2.LEFT -> Cord2(localOriginFaceCord.x + cubeEdgeLength - 1, localOriginFaceCord.y)
                    Direction2.RIGHT -> Cord2(localOriginFaceCord.x - cubeEdgeLength + 1, localOriginFaceCord.y)
                    Direction2.UP -> Cord2(localOriginFaceCord.x, localOriginFaceCord.y + cubeEdgeLength - 1)
                    Direction2.DOWN -> Cord2(localOriginFaceCord.x, localOriginFaceCord.y - cubeEdgeLength + 1)
                }

                //restore mapped cord on map from local edge cord
                val destCord = destEdgesMap.entries.first { it.value == destEdgePos }.key

                //turn direction as many times right as many right corners are between up vector of origin face and destination face
                val destDirection = direction.turn(Turn.RIGHT, wrappedFaceDirectionDiff / 90)

                return destCord to destDirection
            }

            private fun wrappedFaceEdgeMapping(wrappedFace: Face): HashMap<Cord2, Cord2> {
                val destEdgesMap = HashMap<Cord2, Cord2>()
                val destEdgeTopLeft = wrappedFace.cord * cubeEdgeLength
                val destEdgeTopRight = destEdgeTopLeft + Cord2(cubeEdgeLength - 1, 0)
                val destEdgeBottomLeft = destEdgeTopLeft + Cord2(0, cubeEdgeLength - 1)
                val destEdgeBottomRight = destEdgeTopLeft + Cord2(cubeEdgeLength - 1, cubeEdgeLength - 1)
                (destEdgeTopLeft..destEdgeTopRight).forEach { destEdgesMap[it] = it }
                (destEdgeTopLeft..destEdgeBottomLeft).forEach { destEdgesMap[it] = it }
                (destEdgeBottomLeft..destEdgeBottomRight).forEach { destEdgesMap[it] = it }
                (destEdgeTopRight..destEdgeBottomRight).forEach { destEdgesMap[it] = it }
                return destEdgesMap
            }

            private fun foldFaces(
                faceMap: Map<Cord2, MaybeFace>, foldedFace: Face, previousFace: Face? = null
            ) {
                //first fold adjacents so we only fold in xy plane
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
                fixed.x > folded.x -> /*to is on left*/ Rotation3.rotateY(-90)
                fixed.x < folded.x -> /*to is on right*/ Rotation3.rotateY(90)
                fixed.y > folded.y -> /*to is above*/ Rotation3.rotateX(90)
                fixed.y < folded.y -> /*to is below*/ Rotation3.rotateX(-90)
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

    sealed class Move {
        data class TurnMove(val turn: Turn) : Move()
        data class Walk(val nrFields: Int) : Move()
    }

    override fun solve(input: List<String>) {
        val map = parseMap(input)
        val strangeBoard1 = StrangeBoard(map, StrangeBoard.SimpleWrap::class)
        val moves = parseMoves(input)
        val player1 = Character(strangeBoard1, map.find('.'), Direction2.RIGHT)

        moves.forEach { player1.move(it) }
        println("part1: ${player1.getPassword()}") //117102

        val strangeBoard2 = StrangeBoard(map, StrangeBoard.CubicWrap::class)
        val player2 = Character(strangeBoard2, map.find('.'), Direction2.RIGHT)
        moves.forEach { player2.move(it) }
        println("part2: ${player2.getPassword()}") //135297

    }

    private fun parseMap(input: List<String>): Map2<Char> {
        return input.takeWhile { it.isNotBlank() }.map { row ->
                row.mapTo(ArrayList()) { it }
            }.let { Map2(it) }
    }

    private fun parseMoves(input: List<String>): List<Move> {
        val moves = mutableListOf<Move>()
        val movesString = input.last()
        var pos = 0
        while (pos < movesString.length) {
            val remaining = movesString.substring(pos)
            val move: Move = when {
                remaining.first().isDigit() -> {
                    remaining.takeWhile { it.isDigit() }.also { pos += it.length }.let { Move.Walk(it.toInt()) }
                }
                remaining.first().isLetter() -> {
                    pos += 1
                    Move.TurnMove(Turn.of(remaining.first()))
                }
                else -> throw IllegalStateException("Failed to parse remaining: $remaining")
            }
            moves.add(move)
        }
        return moves
    }
}
