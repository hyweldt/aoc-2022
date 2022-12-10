package day10

import Utils
import kotlin.collections.ArrayDeque

typealias Instruction = () -> Unit

class Cpu {
    var currentCycle = 1
    var x = 1
    var signalSum = 0
    private val instructions: ArrayDeque<Instruction> = ArrayDeque()
    fun noop() {
        instructions.add {
            println("${currentCycle} x:${x}: No-op")
        }
    }

    fun addX(n: Int) {
        instructions.add {
            println("${currentCycle} x:${x}: Add part 1 (${n})")
        }
        instructions.add {
            val oldX = x
            x += n
            println("${currentCycle} x:${oldX} -> ${x}: Add part 2 (${n})")
        }
    }

    /**
     * Returns false if there are no more instructions to process
     */
    fun tick(): Boolean {
        val nextInstruction = instructions.removeFirstOrNull()
        if (currentCycle == 20 || (currentCycle - 20) % 40 == 0) {
            val signalStrength = x * currentCycle
            println("${currentCycle} $signalStrength")
            signalSum += signalStrength
        }
        nextInstruction?.invoke()
        currentCycle += 1
        return nextInstruction != null
    }

    fun run() {
        while (this.tick()) {
            //CPU is running
        }
        println(signalSum)
    }
}

fun parseCommands(string: String, cpu: Cpu) {
    val lines = string.lines().filterNot { it.isBlank() }
    lines.forEach {
        val instr = it.split(" ")
        if (instr[0] == "noop") {
            cpu.noop()
        } else if(instr[0] == "addx") {
            cpu.addX(instr[1].toInt())
        }
    }
}

fun main() {
    val example1 = """
        noop
        addx 3
        addx -5
    """.trimIndent()
    val example2 = """
        addx 15
        addx -11
        addx 6
        addx -3
        addx 5
        addx -1
        addx -8
        addx 13
        addx 4
        noop
        addx -1
        addx 5
        addx -1
        addx 5
        addx -1
        addx 5
        addx -1
        addx 5
        addx -1
        addx -35
        addx 1
        addx 24
        addx -19
        addx 1
        addx 16
        addx -11
        noop
        noop
        addx 21
        addx -15
        noop
        noop
        addx -3
        addx 9
        addx 1
        addx -3
        addx 8
        addx 1
        addx 5
        noop
        noop
        noop
        noop
        noop
        addx -36
        noop
        addx 1
        addx 7
        noop
        noop
        noop
        addx 2
        addx 6
        noop
        noop
        noop
        noop
        noop
        addx 1
        noop
        noop
        addx 7
        addx 1
        noop
        addx -13
        addx 13
        addx 7
        noop
        addx 1
        addx -33
        noop
        noop
        noop
        addx 2
        noop
        noop
        noop
        addx 8
        noop
        addx -1
        addx 2
        addx 1
        noop
        addx 17
        addx -9
        addx 1
        addx 1
        addx -3
        addx 11
        noop
        noop
        addx 1
        noop
        addx 1
        noop
        noop
        addx -13
        addx -19
        addx 1
        addx 3
        addx 26
        addx -30
        addx 12
        addx -1
        addx 3
        addx 1
        noop
        noop
        noop
        addx -9
        addx 18
        addx 1
        addx 2
        noop
        noop
        addx 9
        noop
        noop
        noop
        addx -1
        addx 2
        addx -37
        addx 1
        addx 3
        noop
        addx 15
        addx -21
        addx 22
        addx -6
        addx 1
        noop
        addx 2
        addx 1
        noop
        addx -10
        noop
        noop
        addx 20
        addx 1
        addx 2
        addx 2
        addx -6
        addx -11
        noop
        noop
        noop
    """
//    val input = example2.trimIndent()
    val input = Utils.readResource("10.txt")
    val cpu = Cpu()
    parseCommands(input, cpu)
    cpu.run()
}