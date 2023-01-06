import java.time.LocalDate
import java.time.LocalDate.now

fun main(args: Array<String>) {

    val today = now()
    val xMas = LocalDate.of(2022, 12, 25)
    val solveTillDay = if (today.isBefore(xMas)) today.dayOfMonth else xMas.dayOfMonth

    (12..12).forEach {
        println("Day $it:")
        try {
            Day.nr(it)?.let { day ->
                println("Part1: \n${day.part1()}")
                println("Part2: \n${day.part2()}")
            }
        } catch (e: Throwable) {
            println("Failed: ${e}")
            e.printStackTrace()
        }
        println("***************")
    }
}
