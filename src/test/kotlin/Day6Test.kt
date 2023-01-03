import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

internal class Day6Test : DayTest() {

    override val part1exampleExpected: Any = 7

    override val part2exampleExpected: Any = 19

    @ParameterizedTest
    @ArgumentsSource(AdditionalDataStreams::class)
    fun additionalCases(day: Day, part1expected: Int, part2expected: Int) {
        additionalCaseTest(day, part1expected, part2expected)
    }

    private class AdditionalDataStreams : ArgumentsProvider by argumentsDelegate(
        Arguments.of(Day6("bvwbjplbgvbhsrlpgdmjqwftvncz".toList()), 5, 23),
        Arguments.of(Day6("nppdvjthqldpwncqszvftbrmjlhg".toList()), 6, 23),
        Arguments.of(Day6("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg".toList()), 10, 29),
        Arguments.of(Day6("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw".toList()), 11, 26),
    )
}