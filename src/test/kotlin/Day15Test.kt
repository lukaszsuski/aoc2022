import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class Day15Test : DayTest() {

    override val part1exampleExpected: Any = 26

    override val part2exampleExpected: Any = 56000011L

    @ParameterizedTest
    @CsvSource(
        "2, 2, 4, 4, 3, -1, 5",
        "2, 2, 4, 4, 6, 2, 2",
        "2, 2, -2, -2, 0, -4, 8",
    )
    fun sensorRangeAtLineTest(
        sensorX: Int, sensorY: Int,
        beaconX: Int, beaconY: Int,
        line: Int,
        expectedRangeFrom: Int, expectedRangeTo: Int
    ) {
        val sensor = Sensor(
            Cord2(sensorX, sensorY),
            Cord2(beaconX, beaconY),
        )

        val rangeAtLine = sensor.rangeAtLine(line)!!

        assertEquals(expectedRangeFrom..expectedRangeTo, rangeAtLine)
    }

    @ParameterizedTest
    @CsvSource(
        "12, 12, true",
        "8, 8, true",
        "2, 2, false",
        "2, 20, false",
    )
    fun sensorRangeTest(
        x: Int, y: Int, expectedHasRange: Boolean
    ) {
        val sensor = Sensor(
            Cord2(10, 10),
            Cord2(15, 15),
        )

        val hasRange = sensor.hasRangeAt(Cord2(x, y))

        assertEquals(expectedHasRange, hasRange)
    }
}