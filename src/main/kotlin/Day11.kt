import java.math.BigInteger
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

val primesUntilRoot = { i: BigInteger ->
    val rt = i.sqrt()
    sequence {
        if (rt > 2.toBigInteger()) {
            yield(2L)
        }
        if (rt > 3.toBigInteger()) {
            yield(3L)
        }
        if (rt > 3.toBigInteger()) {
            yield(3L)
        }
        var x = 1L
        while ((6 * x).toBigInteger() < rt) {
            val base = x * 6
            yield(base - 1)
            yield(base + 1)
            x += 1
        }
    }
}

const val logging = true

fun log(s: String) {
    if (logging) {
        println(s)
    }
}

fun factorise(i: BigInteger, factors: MutableMap<Long, Int>) {
    if (i == BigInteger.ONE) {
        return
    }
    var factored = false
    for (v in primesUntilRoot(i)) {
        val vBigInt = v.toBigInteger()
        if (i.mod(vBigInt) == BigInteger.ZERO) {
            factored = true
            factors.merge(v, 1) { a, b ->
                a + b
            }
            factorise(i / vBigInt, factors)
            break
        }
    }
    if (!factored) {
        factors.merge(i.toLong(), 1) { a, b -> a + b }
    }
}

fun factorise(i: Int): Map<Long, Int> {
    val factors = mutableMapOf<Long, Int>()
    factorise(i.toBigInteger(), factors)
    return factors
}

class WorryLevel(initialLevel: Int) {
    private var level: Int = initialLevel
    private var factors = factorise(initialLevel).toMutableMap()
    private var additions = 0
    private var additionFactors = mutableMapOf<Long, Int>()
    private val partsFactors = mutableListOf<MutableSet<Long>>()

    fun dividesBy(divisor: Int): Boolean {
        return factorise(divisor).keys.all {
            factors.containsKey(it) && (additions == 0 || additionFactors.containsKey(it))
        }
    }

    fun add(n: Int) {
        log(" + $n")
        additions += n
        additionFactors.clear()
        factorise(n.toBigInteger(), additionFactors)
    }

    fun multiply(n: Int) {
        log(" * $n")
        factorise(n.toBigInteger(), factors)
    }

    override fun toString(): String {
        return "Worrylevel(initialLevel=$level factors=$factors additions=$additions)"
    }

    fun square() {
        log(" ^ 2")
        factors.entries.forEach {
            it.setValue(it.value * 2)
        }
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
//            applyRelief(item)
            val targetMonkey = test(item)
            log("Item with worrylevel ${item.worryLevel} is thrown to monkey $targetMonkey")
            swaps.add(targetMonkey to item)
        }
        this.items.clear()
        return swaps
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
            .map { Item(WorryLevel(it.toInt())) }
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
        val divides = item.worryLevel.dividesBy(divisor.toInt())
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
                old.worryLevel.add(operand.toInt())
            }
        }

        "*" -> { old ->
            if (operand == "old") {
                old.worryLevel.square()
            } else {
                old.worryLevel.multiply(operand.toInt())
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
    repeat(20) {
        runRound(monkeys)
//        if (it % 100 == 0) {
        println(it)
//        }
    }
    val monkeyBusiness = monkeys.sortedByDescending { it.inspectCount }.take(2).map { it.inspectCount }
    println(monkeyBusiness[0] * monkeyBusiness[1]) //should be 2713310158
//    println(primesUntilRoot(700.toBigInteger()).toList())
}