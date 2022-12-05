abstract class Day(private val nr: Int) {
    fun solve() {
        println("==================")
        println("Day $nr:")
        this::class.java
            .getResourceAsStream("$nr")
            ?.bufferedReader()
            ?.let { solve(it.readLines()) }
        println("==================")
    }

    abstract fun solve(input: List<String>)
}