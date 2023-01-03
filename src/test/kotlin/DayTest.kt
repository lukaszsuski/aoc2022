import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream
import kotlin.test.Test

internal abstract class DayTest {
    val day = javaClass.simpleName.filter { it.isDigit() }.toInt()
        .let { Day.nr(it) }!!

    abstract val part1exampleExpected: Any?
    abstract val part2exampleExpected: Any?

    @Test
    fun part1() {
        assertEquals(part1exampleExpected, day.part1())
    }

    @Test
    fun part2() {
        assertEquals(part2exampleExpected, day.part2())
    }

    fun additionalCaseTest(day: Day, part1Expected: Any?, part2Expected: Any?) {
        part1Expected?.let {
            assertEquals(it, day.part1())
        }
        part2Expected?.let {
            assertEquals(it, day.part2())
        }
    }

}

fun argumentsDelegate(vararg args: Arguments): ArgumentsProvider = ArgumentsProvider {
    Stream.of(*args)
}

fun String.toList(): List<String> = this.split("\n")
