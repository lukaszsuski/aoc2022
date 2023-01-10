import kotlin.math.abs

data class Sensor(
    val sensorCord: Cord2,
    val beaconCord: Cord2,
) {
    private val beaconDistance: Int =
        (sensorCord - beaconCord.toVec2()).let { abs(it.x) + abs(it.y) }

    fun hasRangeAt(cord2: Cord2): Boolean =
        (sensorCord - cord2.toVec2()).let { abs(it.x) + abs(it.y) } <= beaconDistance

    fun rangeAtLine(y: Int): IntRange? =
        abs(y - sensorCord.y)
            .takeIf { it <= beaconDistance }
            ?.let {
                val xDiff = beaconDistance - it
                sensorCord.x - xDiff..sensorCord.x + xDiff
            }
}

class Day15(input: List<String>) : Day(input) {

    private val sensors = parseSensors()
    private val part1LineToCheck = parseFirstIntMatching("Part1LineToCheck=")
    private val part2CordLimit = parseFirstIntMatching("Part2CordLimit=")

    override fun part1(): Any? {
        val totalCoverage = sensors
            .mapNotNull { it.rangeAtLine(part1LineToCheck) }
            .flatMapTo(HashSet()) { it.toList() }
            .size
        val nrOfBeacons = sensors
            .mapTo(HashSet()) { it.beaconCord }
            .count { it.y == part1LineToCheck }
        val nrOfSensors = sensors
            .mapTo(HashSet()) { it.sensorCord }
            .count { it.y == part1LineToCheck }
        return totalCoverage - nrOfBeacons - nrOfSensors
    }

    override fun part2(): Any? {
        var current = Cord2(0, 0)
        var sensor: Sensor? = null
        while (current.y < part2CordLimit) {
            sensor = sensors.firstOrNull { it.hasRangeAt(current) && it != sensor }
                ?: return current.let { it.x * 4000000L + it.y }
            current = Cord2(sensor.rangeAtLine(current.y)!!.last + 1, current.y)
            if (current.x >= part2CordLimit) {
                current = Cord2(0, current.y + 1)
            }
        }
        return 0L
    }

    private fun parseFirstIntMatching(match: String): Int {
        return input.first { it.startsWith(match) }
            .removePrefix(match)
            .toInt()
    }

    private fun parseSensors(): List<Sensor> {
        return input
            .mapNotNull {
                val regex = Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
                val result: MatchResult? = regex.matchEntire(it)
                result?.groupValues?.let { values ->
                    Sensor(
                        Cord2(values[1].toInt(), values[2].toInt()),
                        Cord2(values[3].toInt(), values[4].toInt())
                    )
                }
            }
            .sortedBy { it.sensorCord.x }
    }
}

