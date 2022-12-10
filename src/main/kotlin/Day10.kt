import kotlin.properties.Delegates

class Day10 : Day(10) {

    sealed class Instruction {
        abstract fun process(register: Cpu.Register): Boolean

        object Noop : Instruction() {
            override fun process(register: Cpu.Register): Boolean = true
        }

        class AddX(private val value: Int) : Instruction() {
            private var almostFinished = false
            override fun process(register: Cpu.Register): Boolean {
                return if (almostFinished) {
                    register.nextValue = register.value + value
                    true
                } else {
                    almostFinished = true
                    false
                }
            }
        }
    }

    class Clk {

        private val clkObservers = mutableListOf<(Int) -> Unit>()

        private var cycle by Delegates.observable(0) { _, _, clk ->
            clkObservers.forEach { it(clk) }
        }

        fun onTick(observer: (Int) -> Unit) {
            clkObservers.add(observer)
        }

        fun tick() {
            cycle += 1
        }
    }

    class Cpu(clk: Clk, private val instructionCache: Iterator<Instruction>) {

        data class Register(var value: Int, var nextValue: Int = value) {
            fun fetch() {
                //value updates at next fetch by CPU
                value = nextValue
            }
        }

        val register = Register(1)

        private var currentInstruction = instructionCache.next()

        init {
            clk.onTick {
                register.fetch()
                if (currentInstruction.process(register)) {
                    if (instructionCache.hasNext()) {
                        currentInstruction = instructionCache.next()
                    }
                }
            }
        }
    }


    class SignalProbe(clk: Clk, cpu: Cpu, probePoints: List<Int>) {

        var signalStrength = 0

        init {
            clk.onTick {
                if (probePoints.contains(it)) {
                    signalStrength += cpu.register.value * it
                }
            }
        }
    }


    class FrameBuffer(val width: Int, height: Int) {

        private val line = ArrayList<Char>()

        init {
            repeat(width * height) {
                line.add(' ')
            }
        }

        fun drawPixel(x: Int, y: Int, value: Char) {
            line[y * width + x] = value
        }
        fun drawFrame() {
            line.windowed(width, width)
                .map { it.joinToString("") }
                .forEach { println(it) }
        }
    }


    class CRT(clk: Clk, cpu: Cpu, private val fb: FrameBuffer) {

        private var spritePos = 1

        private var beamPos = 0

        init {
            clk.onTick {
                moveSpriteInLine(cpu.register.value)
                drawCurrentPixelToFb()
                beamPos += 1
            }
        }

        private fun moveSpriteInLine(positionInLine: Int) {
            spritePos = positionInLine + beamPos / fb.width * fb.width
        }
        private fun drawCurrentPixelToFb() {
            if (beamPos in (spritePos - 1..spritePos + 1)) {
                fb.drawPixel(beamPos % fb.width, beamPos / fb.width, '#')
            } else {
                fb.drawPixel(beamPos % fb.width, beamPos / fb.width, '.')
            }
        }
    }

    override fun solve(input: List<String>) {
        val clk = Clk()
        val cpu = Cpu(clk, parseInstructions(input).iterator())
        val fb = FrameBuffer(40, 6)
        val crt = CRT(clk, cpu, fb)
        val probe = SignalProbe(clk, cpu, listOf(20, 60, 100, 140, 180, 220))

        repeat(240) { clk.tick() }

        println("part1: ${probe.signalStrength}") //14540
        println("part2:")
        fb.drawFrame() //EHZFZHCZ
    }

    private fun parseInstructions(input: List<String>): List<Instruction> {
        return input.map {
            val tokens = it.split(" ")
            when (tokens[0]) {
                "addx" -> Instruction.AddX(tokens[1].toInt())
                "noop" -> Instruction.Noop
                else -> {
                    throw IllegalArgumentException("unsupported instruction: ${tokens[0]}")
                }
            }
        }
    }

}
