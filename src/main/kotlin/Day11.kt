data class Item(var worryLevel: Long)

class Operation(
    private val operator: Char,
    private val operand: String
) {
    operator fun invoke(item: Item) {
        val resolvedOperand = resolveOperandFor(item)
        when (operator) {
            '+' -> item.worryLevel += resolvedOperand
            '*' -> item.worryLevel *= resolvedOperand
        }
    }

    private fun resolveOperandFor(item: Item) =
        when (operand.trim()) {
            "old" -> item.worryLevel
            else -> operand.trim().toLong()
        }
}

class ThrownItemRouter(
    val divisor: Long,
    val onTrue: Int,
    val onFalse: Int
) {
    operator fun invoke(item: Item): Int {
        return if (item.worryLevel % divisor == 0L) {
            onTrue
        } else {
            onFalse
        }
    }
}

class Monkey(
    private val items: ArrayDeque<Item>,
    private val operation: Operation,
    val thrownItemRouter: ThrownItemRouter
)
{
    var timesThrown = 0L

    fun catchItem(item: Item) {
        items.addLast(item)
    }

    fun throwItems(worryReducer: (Item) -> Unit): List<Pair<Item, Int>> {
        val thrownItems = items
            .onEach(::inspectItem)
            .onEach(worryReducer)
            .map { it to thrownItemRouter(it) }

        timesThrown += items.size
        items.clear()
        return thrownItems
    }

    private fun inspectItem(item: Item) {
        operation(item)
    }
}

class MonkeyBusiness(
    monkeys: ArrayList<Monkey>
) {
    val level = monkeys
        .sortedByDescending(Monkey::timesThrown)
        .take(2)
        .map { it.timesThrown }
        .reduce(Long::times)
}

class Round(
    monkeys: ArrayList<Monkey>,
    worryRelief: (Item) -> Unit
) {
    init {
        monkeys.forEach { monkey ->
            monkey.throwItems(worryRelief).forEach {
                monkeys[it.second].catchItem(it.first)
            }
        }
    }
}

class Day11(input: List<String>) : Day(input) {

    override fun part1(): Any? {
        val monkeys = parseMonkeys(input)
        repeat(20) { Round(monkeys) { it.worryLevel /= 3 } }
        return MonkeyBusiness(monkeys).level //102399
    }

    override fun part2(): Any? {
        val monkeys = parseMonkeys(input)
        val divisorProduct = monkeys.map { it.thrownItemRouter.divisor }.reduce(Long::times)
        val adviceFromRandomGuyOnInternetHowToReliefWorry = { item: Item -> item.worryLevel %= divisorProduct }
        repeat(10_000) { Round(monkeys, adviceFromRandomGuyOnInternetHowToReliefWorry)}
        return MonkeyBusiness(monkeys).level //23641658401
    }

    private fun parseMonkeys(input: List<String>): ArrayList<Monkey> {
        return input.windowed(6, 7)
            .map {
                val items = it[1].removePrefix("  Starting items: ")
                    .split(",")
                    .map { Item(it.trim().toLong()) }
                    .toCollection(ArrayDeque())
                val (operator, operand) = it[2].removePrefix("  Operation: new = old ").split(" ")
                val operation = Operation(operator[0], operand)
                val divisor = it[3].removePrefix("  Test: divisible by ").toLong()
                val onTrue = it[4].removePrefix("    If true: throw to monkey ").toInt()
                val onFalse = it[5].removePrefix("    If false: throw to monkey ").toInt()
                val router = ThrownItemRouter(divisor, onTrue, onFalse)
                Monkey(items, operation, router)
            }.toCollection(ArrayList())
    }

}
