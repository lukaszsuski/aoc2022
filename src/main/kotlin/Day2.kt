class Day2 : Day(2) {

    enum class Shape() {
        ROCK, PAPER, SCISSOR;

        fun shapeValue(): Int = when (this) {
            ROCK -> 1
            PAPER -> 2
            SCISSOR -> 3
        }

        fun otherTo(result: Result): Shape {
            return when (result) {
                Result.DRAW -> this
                Result.WON -> when (this) {
                    ROCK -> PAPER
                    PAPER -> SCISSOR
                    SCISSOR -> ROCK
                }
                Result.LOST -> when (this) {
                    ROCK -> SCISSOR
                    PAPER -> ROCK
                    SCISSOR -> PAPER
                }
            }
        }

        fun fight(other: Shape): Result {
            return when (this) {
                ROCK -> {
                    when (other) {
                        ROCK -> Result.DRAW
                        PAPER -> Result.LOST
                        SCISSOR -> Result.WON
                    }
                }
                PAPER -> {
                    when (other) {
                        ROCK -> Result.WON
                        PAPER -> Result.DRAW
                        SCISSOR -> Result.LOST
                    }
                }
                SCISSOR -> {
                    when (other) {
                        ROCK -> Result.LOST
                        PAPER -> Result.WON
                        SCISSOR -> Result.DRAW
                    }
                }
            }
        }
    }

    enum class Result(val points: Int) {
        WON(6), LOST(0), DRAW(3)
    }

    override fun solve(input: List<String>) {
        val part1 = input.sumOf {
            val elfShape = elfShape(it.split(" ")[0])
            val myShape = myShape(it.split(" ")[1])
            myShape.fight(elfShape).points + myShape.shapeValue()
        }
        println("part1: $part1")

        val part2 = input.sumOf {
            val elfShape = elfShape(it.split(" ")[0])
            val expectedResult = expectedResult(it.split(" ")[1])
            val myExpectedShape = elfShape.otherTo(expectedResult)
            myExpectedShape.shapeValue() + expectedResult.points
        }
        println("part2: $part2")
    }

    private fun elfShape(value: String): Shape {
        return when (value) {
            "A" -> Shape.ROCK
            "B" -> Shape.PAPER
            "C" -> Shape.SCISSOR
            else -> throw NotImplementedError()
        }
    }

    private fun myShape(value: String): Shape {
        return when (value) {
            "X" -> Shape.ROCK
            "Y" -> Shape.PAPER
            "Z" -> Shape.SCISSOR
            else -> throw NotImplementedError()
        }
    }

    private fun expectedResult(value: String): Result {
        return when (value) {
            "X" -> Result.LOST
            "Y" -> Result.DRAW
            "Z" -> Result.WON
            else -> throw NotImplementedError()
        }
    }
}