private typealias CratesStack = ArrayDeque<Char>
private typealias CratesStacks = Map<Int, CratesStack>

interface Crane {
    fun process(cratesStacks: CratesStacks, moves: List<CraneMove>)
}

class Crane9000 : Crane {
    override fun process(cratesStacks: CratesStacks, moves: List<CraneMove>) {
        moves.forEach { move ->
            repeat(move.nrCratesToMove) {
                val crate = cratesStacks[move.fromStack]!!.removeLast()
                cratesStacks[move.toStack]!!.addLast(crate)
            }
        }
    }
}

class Crane9001 : Crane {
    override fun process(cratesStacks: CratesStacks, moves: List<CraneMove>) {
        moves.forEach { move ->
            val pickedCrates = CratesStack()
            repeat(move.nrCratesToMove) {
                pickedCrates.add(cratesStacks[move.fromStack]!!.removeLast())
            }
            cratesStacks[move.toStack]!!.addAll(pickedCrates.reversed())
        }
    }
}

data class CraneMove(val nrCratesToMove: Int, val fromStack: Int, val toStack: Int) {
    companion object {
        fun from(move: String): CraneMove {
            val tokens = move.split(" ")
            return CraneMove(tokens[1].toInt(), tokens[3].toInt(), tokens[5].toInt())
        }
    }
}

class Day5(input: List<String>) : Day(input) {

    private val moves = parseMoves(input)

    override fun part1(): Any? {
        val cratesStacks = parseCratesStacks(input)
        Crane9000().process(cratesStacks, moves)
        return cratesStacks.values
            .map { it.last() }
            .joinToString("")
    }


    override fun part2(): Any? {
        val cratesStacks = parseCratesStacks(input)
        Crane9001().process(cratesStacks, moves)
        return cratesStacks.values
            .map { it.last() }
            .joinToString("")
    }


    private fun parseMoves(input: List<String>): List<CraneMove> {
        return input
            .filter { it.startsWith("move ") }
            .map { CraneMove.from(it) }
    }

    private fun parseCratesStacks(input: List<String>): CratesStacks {
        return input
            .first { it.contains(Regex("\\d")) }
            .withIndex()
            .filter { it.value.isDigit() }
            .associate { stackNr ->
                val cratesStack = input
                    .filter { it.contains("[") }
                    .map { crates -> crates.elementAtOrElse(stackNr.index) { _ -> ' ' } }
                    .reversed()
                    .filter { crate -> crate.isLetter() }
                    .toCollection(ArrayDeque())
                stackNr.value.digitToInt() to cratesStack
            }
    }

}

