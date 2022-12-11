import java.lang.Exception

val example = """
    Monkey 0:
      Starting items: 79, 98
      Operation: new = old * 19
      Test: divisible by 23
        If true: throw to monkey 2
        If false: throw to monkey 3

    Monkey 1:
      Starting items: 54, 65, 75, 74
      Operation: new = old + 6
      Test: divisible by 19
        If true: throw to monkey 2
        If false: throw to monkey 0

    Monkey 2:
      Starting items: 79, 60, 97
      Operation: new = old * old
      Test: divisible by 13
        If true: throw to monkey 1
        If false: throw to monkey 3

    Monkey 3:
      Starting items: 74
      Operation: new = old + 3
      Test: divisible by 17
        If true: throw to monkey 0
        If false: throw to monkey 1
""".trimIndent()

data class Item(var worryLevel: Int)
data class Monkey(
    val id: Int,
    val items: MutableList<Item> = mutableListOf(),
    var inspect: (Item) -> Int,
    var test: (Item) -> Int,
    var inspectCount: Int = 0,
) {

    fun doInspect(item: Item): Int {
        inspectCount += 1
        return inspect(item)
    }

    fun takeTurn(): List<Pair<Int, Item>> {
        val swaps = mutableListOf<Pair<Int, Item>>()
        this.items.forEach { item ->
            item.worryLevel = doInspect(item)
            applyRelief(item)
            val targetMonkey = test(item)
            println("Item with worrylevel ${item.worryLevel} is thrown to monkey ${targetMonkey}")
            swaps.add(targetMonkey to item)
        }
        this.items.clear()
        return swaps
    }

    private fun applyRelief(item: Item) {
        item.worryLevel = item.worryLevel / 3
    }
}

fun runRound(monkeys: List<Monkey>) {
    monkeys.forEach { monkey ->
        val swaps = monkey.takeTurn()
        swaps.forEach {
            monkeys[it.first].items.add(it.second)
        }
    }
}

fun parseMonkeys(string: String): List<Monkey> {
    val monkeys = mutableListOf<Monkey>()
    var lines = string.lines().dropWhile { it.isBlank() }
    while (lines.isNotEmpty()) {
        val monkey = lines.takeWhile { it.isNotBlank() }
        monkeys.add(parseMonkey(monkey))
        lines = lines.drop(monkey.size).dropWhile { it.isBlank() }
    }
    return monkeys
}

fun parseMonkey(monkey: List<String>): Monkey {
    val monkeyId = "^Monkey (\\d+):$".toRegex().matchEntire(monkey[0])!!.groupValues[1].toInt()
    val items: List<Item> =
        "\\s+Starting items: (.*)$".toRegex().matchEntire(monkey[1])!!.groupValues[1].split(", ")
            .map { Item(it.toInt()) }
    val operation: (Item) -> Int = parseOperation(monkey[2])
    val test: (Item) -> Int = parseTest(monkey[3], monkey[4], monkey[5])
    return Monkey(monkeyId, items.toMutableList(), operation, test)
}

fun parseTest(testString: String, ifTrueString: String, ifFalseString: String): (Item) -> Int {
    val (divisible, by, divisor) = "\\s+Test: (.*)".toRegex().matchEntire(testString)!!.groupValues[1].split(" ")
    if (divisible != "divisible") {
        throw Exception("Unknown test: ${divisible}")
    }
    val ifTrueMonkey =
        "\\s+ If true: throw to monkey (\\d+)".toRegex().matchEntire(ifTrueString)!!.groupValues[1].toInt()
    val ifFalseMonkey =
        "\\s+ If false: throw to monkey (\\d+)".toRegex().matchEntire(ifFalseString)!!.groupValues[1].toInt()
    return {
        val divides = it.worryLevel % divisor.toInt() == 0
        if (divides) {
            println("${it.worryLevel} divides by ${divisor}")
            ifTrueMonkey
        } else {
            ifFalseMonkey
        }
    }
}

fun parseOperation(s: String): (Item) -> Int {
    val (new, eq, old, operator, operand) = "\\s+Operation: (new = .*)".toRegex().matchEntire(s)!!.groupValues[1].split(
        " "
    )
    return when (operator) {
        "+" -> { old ->
            old.worryLevel + (if (operand == "old") old.worryLevel else operand.toInt())
        }

        "*" -> { old -> old.worryLevel * (if (operand == "old") old.worryLevel else operand.toInt()) }
        else -> {
            throw Exception("Unknown operator $operator")
        }
    }
}

fun main() {
    val monkeys = parseMonkeys(Utils.readResource("11.txt"))
    println(monkeys)
    repeat(20) {
        runRound(monkeys)
        println(monkeys)
    }
    val monkeyBusiness = monkeys.sortedByDescending { it.inspectCount }.take(2).map { it.inspectCount }
    println(monkeyBusiness[0] * monkeyBusiness[1])
}