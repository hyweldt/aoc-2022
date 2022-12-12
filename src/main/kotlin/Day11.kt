import kotlin.math.roundToLong
import kotlin.math.sqrt

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

const val logging = false

const val reliefLevel = 1L

fun log(s: String) {
    if (logging) {
        println(s)
    }
}

val primeCandidatesUntil = { n: Long ->
    sequence<Long> {
        if (n > 2L) {
            yield(2L)
        }
        if (n > 3L) {
            yield(3L)
        }
        var x = 1
        while (6L * x - 1 < n) {
            yield((6L * x) - 1)
            yield((6L * x) + 1)
            x += 1
        }
    }
}

fun factorise(i: Long, factors: MutableSet<Long>) {
    if (i == 1L) {
        return
    }
    var factored = false
    //TODO more efficient prime checks
    for (v in primeCandidatesUntil(sqrt(i.toDouble()).roundToLong())) {
        if (i % v == 0L) {
            factored = true
            factors.add(v)
            factorise(i / v, factors)
            break
        }
    }
    if (!factored) {
        //i must be prime
        factors.add(i)
    }
}

fun factorise(i: Long): Set<Long> {
    val factors = mutableSetOf<Long>()
    factorise(i, factors)
    return factors
}

class WorryLevel(val initialLevel: Long, val mod: Long) {
    var level = initialLevel % mod

    fun dividesBy(divisor: Long): Boolean {
        return level % divisor == 0L
    }

    fun add(n: Long) {
        level = (level + n) % mod
    }

    fun multiply(n: Long) {
        level = (level * n) % mod
    }

    fun square() {
        multiply(level)
    }

    override fun toString(): String {
        return "WorryLevel ${initialLevel} now: ${level}"
    }
}

data class Item(var worryLevel: WorryLevel)
data class Monkey(
    val id: Int,
    val items: MutableList<Item> = mutableListOf(),
    var inspect: (Item) -> Unit,
    var test: (Item) -> Int,
    val divisor: Int,
    var inspectCount: Long = 0,
) {

    private fun doInspect(item: Item) {
        inspectCount += 1
        inspect(item)
    }

    fun takeTurn(): List<Pair<Int, Item>> {
        val swaps = mutableListOf<Pair<Int, Item>>()
        this.items.forEach { item ->
            doInspect(item)
            applyRelief(item)
            val targetMonkey = test(item)
            log("Item with worrylevel ${item.worryLevel} is thrown to monkey $targetMonkey")
            swaps.add(targetMonkey to item)
        }
        this.items.clear()
        return swaps
    }

    private fun applyRelief(item: Item) {
        item.worryLevel.level = item.worryLevel.level / reliefLevel
    }
}

fun runRound(monkeys: List<Monkey>) {
    log("Running round")
    monkeys.forEach { monkey ->
        log("Monkey: ${monkey.id}")
        val swaps = monkey.takeTurn()
        swaps.forEach {
            monkeys[it.first].items.add(it.second)
        }
    }
}

fun parseMonkeys(string: String): List<Monkey> {
    val monkeys = mutableListOf<Monkey>()
    //Get modulo
    var modulo = string.lines().filter { it.trim().startsWith("Test:") }
        .map {
            val (divisible, by, divisor) = "\\s+Test: (.*)".toRegex().matchEntire(it)!!.groupValues[1].split(" ")
            divisor.toLong()
        }.fold(1L) { a, b -> a * b }.also { println("Modulo: $it") }

    var lines = string.lines().dropWhile { it.isBlank() }
    while (lines.isNotEmpty()) {
        val monkey = lines.takeWhile { it.isNotBlank() }
        monkeys.add(parseMonkey(monkey, modulo))
        lines = lines.drop(monkey.size).dropWhile { it.isBlank() }
    }
    return monkeys
}

fun parseMonkey(monkey: List<String>, modulo: Long): Monkey {
    val monkeyId = "^Monkey (\\d+):$".toRegex().matchEntire(monkey[0])!!.groupValues[1].toInt()
    val items: List<Item> =
        "\\s+Starting items: (.*)$".toRegex().matchEntire(monkey[1])!!.groupValues[1].split(", ")
            .map { Item(WorryLevel(it.toLong(), modulo)) }
    val operation: (Item) -> Unit = parseOperation(monkey[2])
    val (test, divisor) = parseTest(monkey[3], monkey[4], monkey[5])
    return Monkey(monkeyId, items.toMutableList(), operation, test, divisor)
}

fun parseTest(testString: String, ifTrueString: String, ifFalseString: String): Pair<(Item) -> Int, Int> {
    val (divisible, by, divisor) = "\\s+Test: (.*)".toRegex().matchEntire(testString)!!.groupValues[1].split(" ")
    if (divisible != "divisible") {
        throw Exception("Unknown test: $divisible")
    }
    val ifTrueMonkey =
        "\\s+ If true: throw to monkey (\\d+)".toRegex().matchEntire(ifTrueString)!!.groupValues[1].toInt()
    val ifFalseMonkey =
        "\\s+ If false: throw to monkey (\\d+)".toRegex().matchEntire(ifFalseString)!!.groupValues[1].toInt()
    val fn = { item: Item ->
        val divides = item.worryLevel.dividesBy(divisor.toLong())
        if (divides) {
            log("${item.worryLevel} divides by $divisor")
            ifTrueMonkey
        } else {
            ifFalseMonkey
        }
    }
    return fn to divisor.toInt()
}

fun parseOperation(s: String): (Item) -> Unit {
    val (new, eq, old, operator, operand) = "\\s+Operation: (new = .*)".toRegex()
        .matchEntire(s)!!.groupValues[1].split(
        " "
    )
    return when (operator) {
        "+" -> { old ->
            if (operand == "old") {
                old.worryLevel.multiply(2)
            } else {
                old.worryLevel.add(operand.toLong())
            }
        }

        "*" -> { old ->
            if (operand == "old") {
                old.worryLevel.square()
            } else {
                old.worryLevel.multiply(operand.toLong())
            }
        }

        else -> {
            throw Exception("Unknown operator $operator")
        }
    }
}

fun main() {
    val monkeys = parseMonkeys(example)
//    val monkeys = parseMonkeys(Utils.readResource("11.txt"))
    repeat(10_000) {
        runRound(monkeys)
        if (it in listOf(0, 19, 1000, 2000)) {
            println("=======${it + 1}========")
            monkeys.forEach {
                println("${it.id} -> ${it.inspectCount}")
            }
            println("===============")
        }
    }
    val monkeyBusiness = monkeys.sortedByDescending { it.inspectCount }.take(2).map { it.inspectCount }
    println(monkeyBusiness[0] * monkeyBusiness[1]) //should be 2713310158
}