sealed class YellingMonkey {
    abstract fun yell(): Long?
    abstract fun askFor(value: Long)
}

class ValueMonkey(
    val value: Long
) : YellingMonkey() {
    override fun yell(): Long = value
    override fun askFor(asked: Long) {
        throw IllegalArgumentException("nope")
    }
}

class HumanMonkeyWithAlzheimer(
    var value: Long? = null
) : YellingMonkey() {
    override fun yell(): Long? = value
    override fun askFor(value: Long) {
        this.value = value
    }
}

class CountingMonkey(
    val leftName: String,
    val rightName: String,
    val monkeyArbiter: (String) -> YellingMonkey,
    var operator: Operator
) : YellingMonkey() {

    override fun yell(): Long? {
        return operator(monkeyArbiter(leftName).yell(), monkeyArbiter(rightName).yell())
    }

    override fun askFor(result: Long) {
        val leftYell = monkeyArbiter(leftName).yell()
        val rightYell = monkeyArbiter(rightName).yell()
        when {
            (leftYell == null) -> monkeyArbiter(leftName)
                .askFor(operator.resolveLeft(result, rightYell!!))
            (rightYell == null) -> monkeyArbiter(rightName)
                .askFor(operator.resolveRight(result, leftYell))
        }
    }
}

interface Operator : (Long?, Long?) -> Long? {
    fun resolveRight(left: Long, result: Long): Long
    fun resolveLeft(right: Long, result: Long): Long
    fun Long.call(right: Long?, operator: Long.(other: Long) -> Long): Long? {
        return right?.let { operator(this, it) }
    }

    fun Long.call(right: Long, operator: Long.(other: Long) -> Long): Long {
        return operator(this, right)
    }
}

object AddOperator : Operator {
    override fun invoke(left: Long?, right: Long?) = left?.call(right, Long::plus)
    override fun resolveLeft(right: Long, result: Long) = right.call(result, Long::minus)
    override fun resolveRight(left: Long, result: Long) = left.call(result, Long::minus)
}

object SubtractOperator : Operator {
    override fun invoke(left: Long?, right: Long?) = left?.call(right, Long::minus)
    override fun resolveLeft(right: Long, result: Long) = result.call(right, Long::plus)
    override fun resolveRight(left: Long, result: Long) = result.call(left, Long::minus)//.neg()
}

object MultiplyOperator : Operator {
    override fun invoke(left: Long?, right: Long?) = left?.call(right, Long::times)
    override fun resolveLeft(right: Long, result: Long) = right.call(result, Long::div)
    override fun resolveRight(left: Long, result: Long) = left.call(result, Long::div)
}

object DivideOperator : Operator {
    override fun invoke(left: Long?, right: Long?) = left?.call(right, Long::div)
    override fun resolveLeft(right: Long, result: Long) = result.call(right, Long::times)
    override fun resolveRight(left: Long, result: Long) = 1 / result.call(left, Long::times)
}

class Day21(input: List<String>) : Day(input) {

    private val monkeys = parseMonkeys(input)

    override fun part1(): Any? = monkeys["root"]!!.yell()

    override fun part2(): Any? {
        monkeys.compute("humn") { _, _ -> HumanMonkeyWithAlzheimer() }
        monkeys["root"]!!.let { it as CountingMonkey }.operator = SubtractOperator
        monkeys["root"]!!.askFor(0L)
        return monkeys["humn"]!!.yell()
    }

    private fun parseMonkeys(input: List<String>): HashMap<String, YellingMonkey> {
        val monkeys = HashMap<String, YellingMonkey>()
        input.forEach {
            val name = it.substringBefore(':')
            val def = it.substringAfter(':').trim()
            if (def.first().isDigit()) {
                monkeys[name] = ValueMonkey(def.toLong())
            } else {
                val (left, oper, right) = def.split(" ")
                val operator: Operator = when (oper) {
                    "+" -> AddOperator
                    "-" -> SubtractOperator
                    "*" -> MultiplyOperator
                    "/" -> DivideOperator
                    else -> throw java.lang.IllegalArgumentException("unknown operator")
                }
                monkeys[name] = CountingMonkey(left, right, { monkeys[it]!! }, operator)
            }
        }
        return monkeys
    }


}
