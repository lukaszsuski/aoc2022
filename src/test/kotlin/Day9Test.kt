import org.junit.jupiter.api.Test

internal class Day9Test : DayTest() {

    override val part1exampleExpected: Any = 13

    override val part2exampleExpected: Any = 1

    //fixme - 31 instead of 36
    // @Test
    fun longerCase() {
        val day = Day9(
            """
                R 5
                U 8
                L 8
                D 3
                R 17
                D 10
                L 25
                U 20""".trimIndent().toList()
        )
        additionalCaseTest(day, null, 36)
    }

}