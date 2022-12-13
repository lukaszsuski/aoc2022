import java.time.LocalDate
import java.time.LocalDate.now

const val SOLVE_ALL = "--all"

fun main(args: Array<String>) {

    val today = now()
    val xMas = LocalDate.of(2022, 12, 25)

    val solveTillDay = if (today.isBefore(xMas)) today.dayOfMonth else 25

    if (today.isAfter(xMas) or args.contains(SOLVE_ALL)) {
        (1..solveTillDay).forEach { Day.trySolve(it) }
    } else {
        Day.trySolve(today.dayOfMonth)
    }

}
