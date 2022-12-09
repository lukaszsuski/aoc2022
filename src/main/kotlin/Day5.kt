typealias CratesStack = ArrayDeque<Char>
typealias CratesStacks = Map<Int, CratesStack>

class Day5 : Day(5) {

    interface Crane {
        fun process(cratesStacks: CratesStacks, moves: List<Move>)
    }

    class Crane9000 : Crane {
        override fun process(cratesStacks: CratesStacks, moves: List<Move>) {
            moves.forEach { move ->
                repeat(move.nrCratesToMove) {
                    val crate = cratesStacks[move.fromStack]!!.removeLast()
                    cratesStacks[move.toStack]!!.addLast(crate)
                }
            }
        }
    }

    class Crane9001 : Crane {
        override fun process(cratesStacks: CratesStacks, moves: List<Move>) {
            moves.forEach { move ->
                val pickedCrates = CratesStack()
                repeat(move.nrCratesToMove) {
                    pickedCrates.add(cratesStacks[move.fromStack]!!.removeLast())
                }
                cratesStacks[move.toStack]!!.addAll(pickedCrates.reversed())
            }
        }
    }

    data class Move(val nrCratesToMove: Int, val fromStack: Int, val toStack: Int) {
        companion object {
            fun from(move: String): Move {
                val tokens = move.split(" ")
                return Move(tokens[1].toInt(), tokens[3].toInt(), tokens[5].toInt())
            }
        }
    }

    override fun solve(input: List<String>) {
        val moves = parseMoves(input)

        val part1 = parseCratesStacks(input)
        Crane9000().process(part1, moves)
        println("part1: ${
            part1.values
                .map { it.last() }
                .joinToString("")
        }")

        val part2 = parseCratesStacks(input)
        Crane9001().process(part2, moves)
        println("part2: ${
            part2.values
                .map { it.last() }
                .joinToString("")
        }")
    }

    private fun parseMoves(input: List<String>): List<Move> {
        return input
            .filter { it.startsWith("move ") }
            .map { Move.from(it) }
    }

    private fun parseCratesStacks(input: List<String>): CratesStacks {
        return input
            .first { it.contains(Regex("\\d")) }
            .withIndex()
            .filter { it.value.isDigit() }
            .associate { stackNr ->
                val cratesStack = input
                    .filter { it.startsWith("[") }
                    .map { crates -> crates.elementAtOrElse(stackNr.index) { _ -> ' ' } }
                    .reversed()
                    .filter { crate -> crate.isLetter() }
                    .toCollection(ArrayDeque())
                stackNr.value.digitToInt() to cratesStack
            }
    }

}

