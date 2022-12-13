import java.util.concurrent.atomic.AtomicBoolean

class Day13 : Day(13) {

    sealed class Packet(var isDivider: Boolean = false): Comparable<Packet> {
        abstract override operator fun compareTo(other: Packet): Int

        fun asDivider() = apply {
            isDivider = true
        }
    }

    data class IntegerPacket(val element: Int): Packet() {
        override fun compareTo(other: Packet): Int {
            return when (other) {
                is IntegerPacket -> element - other.element
                is ListPacket -> asListPacket().compareTo(other)
            }
        }

        fun asListPacket(): Packet {
            return ListPacket(mutableListOf(this))
        }
    }

    data class ListPacket(val elements: MutableList<Packet> = ArrayList()): Packet() {
        fun add(element: Packet) = elements.add(element)

        override fun compareTo(other: Packet): Int {
            return when (other) {
                is IntegerPacket -> compareTo(other.asListPacket())
                is ListPacket -> {
                    elements.zip(other.elements)
                        .map { it.first.compareTo(it.second) }
                        .firstOrNull { it != 0 }
                        ?:(elements.size - other.elements.size)
                }
            }
        }
    }

    override fun solve(input: List<String>) {
        val pairs = parsePacketPairs(input)
        println("part1: ${pairs.withIndex()
            .filter { it.value.isRightOrder() }
            .sumOf { it.index + 1  }}")

        val packets = input.filter(String::isNotBlank)
            .map(::parsePacket)
            .toCollection(mutableListOf())
            .apply {
                add(parsePacket("[[2]]").asDivider())
                add(parsePacket("[[6]]").asDivider())
            }
        println("part2: ${packets.sorted().withIndex()
            .filter { it.value.isDivider }
            .map { it.index + 1 }
            .reduce(Int::times)}")
    }

    private fun Pair<Packet, Packet>.isRightOrder(): Boolean = first < second

    private fun parsePacketPairs(input: List<String>): List<Pair<Packet, Packet>> {
        return input.windowed(2, 3)
            .map {
                val first = parsePacket(it[0])
                val second = parsePacket(it[1])
                first to second
            }
    }

    private fun parsePacket(input: String): Packet {
        return if (input.startsWith("[") and input.endsWith("]"))
            parseList(input)
        else
            IntegerPacket(input.toInt())
    }

    private fun parseList(input: String): Packet {
        val listElement = ListPacket()
        val unwrapped = input.removeSurrounding("[", "]")
        var pos = 0
        while (pos < unwrapped.length) {
            val remaining = unwrapped.substring(pos)
            val element: String = when {
                remaining.first().isDigit() -> {
                    remaining.takeWhile { it.isDigit() }
                }
                remaining.first() == ',' -> {
                    " "
                }
                remaining.first() == '[' -> {
                    //find closing bracket and consume as list
                    var nrOpeningBrackets = 0
                    var nrClosingBrackets = 0
                    val allBracketsMatch = AtomicBoolean(false)
                    remaining.takeWhile {
                        when (it) {
                            '[' -> nrOpeningBrackets += 1
                            ']' -> nrClosingBrackets += 1
                        }
                        !allBracketsMatch.getAndSet(nrOpeningBrackets == nrClosingBrackets)
                    }
                }
                else -> { throw IllegalStateException("Failed to parse remaining: $remaining") }
            }

            if (element.isNotBlank()) {
                listElement.add(parsePacket(element))
            }
            pos += element.length
        }
        return listElement
    }

}