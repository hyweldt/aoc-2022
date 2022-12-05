class Stack {
    val crates = mutableListOf<String>()
    fun getTopCrate(): String {
        return crates.last()
    }

    override fun toString(): String {
        return crates.joinToString(", ")
    }
}

class Stacks(val stacks: List<Stack>) {
    fun moveInBatches(count: Int, from: Int, to: Int) {
        val moving = stacks[from - 1].crates.takeLast(count)
        stacks[to - 1].crates.addAll(moving)
        repeat(count) { stacks[from - 1].crates.removeLast() }
    }

    fun moveOneByOne(count: Int, from: Int, to: Int) {
        repeat(count) {
            stacks[to - 1].crates.add(stacks[from - 1].crates.removeLast())
        }
    }

    override fun toString(): String {
        val maxHeight = stacks.maxOfOrNull { it.crates.size } ?: 0
        val sb = StringBuilder()
        for (i in maxHeight downTo 0) {
            for (stack in stacks) {
                if (stack.crates.size > i) {
                    sb.append(stack.crates[i] + "")
                } else {
                    sb.append(" ")
                }
                sb.append(" ")
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun topCrates(): List<String> {
        return stacks.map { it.getTopCrate() }
    }
}

val instructionPattern = Regex("move (\\d+) from (\\d+) to (\\d+)")
fun parseInstructions(line: String): Triple<Int, Int, Int> {
    val (count, from, to) = instructionPattern.matchEntire(line)!!.destructured
    return Triple(count.toInt(), from.toInt(), to.toInt())
}

fun parseStacks(lines: List<String>): List<Stack> {
    val stacks = mutableListOf<Stack>()
    val reversed = lines.reversed()
    val columns = reversed[0].split(" ").filter { it.isNotBlank() }
    println("Columns: $columns")
    repeat(columns.size) { stacks.add(Stack()) }
    reversed.drop(1).forEach { line ->
        val cratesInRow = line.chunked(4)
        cratesInRow.forEachIndexed { index, crate ->
            if (crate.isNotBlank()) {
                stacks[index].crates.add(crate.substring(1, 2))
            }
        }
    }
    return stacks
}

fun solvePuzzle(input: String) {
    val stackStrings = input.lines().takeWhile { it.isNotBlank() }
    val instructions = input.lines().drop(stackStrings.size + 1).filterNot { it.isBlank() }
    val solution1 = Stacks(parseStacks(stackStrings))
    val solution2 = Stacks(parseStacks(stackStrings))
    instructions.map { parseInstructions(it) }.forEach {
        println("Instruction: $it")
        solution1.moveOneByOne(it.first, it.second, it.third)
        solution2.moveInBatches(it.first, it.second, it.third)
    }
    println("Solution 1:${solution1.topCrates().joinToString("")}")
    println("Solution 2:${solution2.topCrates().joinToString("")}")
}

fun main() {
    val input = Utils.readResource("05.txt")
    solvePuzzle(input)
}