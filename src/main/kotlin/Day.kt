abstract class Day(private val nr: Int) {
    fun solve() {
        println("==================")
        println("Day $nr:")
        this::class.java
            .getResourceAsStream("inputs/$nr")
            ?.bufferedReader()
            ?.let { solve(it.readLines()) }
        println("==================")
    }

    abstract fun solve(input: List<String>)

    companion object {
        fun trySolve(dayNr: Int) {
            val day = Class.forName("Day${dayNr}")
                .getConstructor()
                .newInstance() as Day
            day.solve()
        }
    }
}