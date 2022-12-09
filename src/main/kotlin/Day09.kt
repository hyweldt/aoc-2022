import kotlin.math.sign

inline class X(val int: Int)
inline class Y(val int: Int)

operator fun X.compareTo(other: X): Int {
    return int - other.int
}

operator fun Y.compareTo(other: Y): Int {
    return int - other.int
}

operator fun X.plus(other: X): X {
    return X(this.int + other.int)
}

operator fun Y.plus(other: Y): Y {
    return Y(this.int + other.int)
}

operator fun X.minus(other: X): Int {
    return this.int - other.int
}

operator fun Y.minus(other: Y): Int {
    return this.int - other.int
}

fun Pair<X, Y>.translate(dir: Pair<X, Y>): Pair<X, Y> {
    return (this.first + dir.first) to this.second + dir.second
}

fun Pair<X, Y>.delta(other: Pair<X, Y>): Pair<Int, Int> {
    return (other.first - first) to (other.second - second)
}

sealed class MoveCommand(val delta: Pair<X, Y>) {
    class MoveUp : MoveCommand(X(0) to Y(1))
    class MoveDown : MoveCommand(X(0) to Y(-1))
    class MoveLeft : MoveCommand(X(-1) to Y(0))
    class MoveRight : MoveCommand(X(1) to Y(0))

}

fun minOf(x: X, x2: X): X {
    return X(minOf(x.int, x2.int))
}


fun maxOf(x: X, x2: X): X {
    return X(maxOf(x.int, x2.int))
}

fun minOf(y: Y, y2: Y): Y {
    return Y(minOf(y.int, y2.int))
}

fun maxOf(y: Y, y2: Y): Y {
    return Y(maxOf(y.int, y2.int))
}

class KnotTravel {
    //grid co-ordinates of visited tiles
    private var headPos = X(0) to Y(0)
    private var tailPos = headPos
    private val visitedTiles = mutableListOf(tailPos)
    fun moveHead(command: MoveCommand) {
        headPos = headPos.translate(command.delta)
        moveTail()
//        printBoard()
    }

    fun setHead(head: Pair<X, Y>) {
        headPos = head
        moveTail()
    }

    fun getTail(): Pair<X, Y> {
        return tailPos
    }

    private fun moveTail() {
        val deltaToNewHead = tailPos.delta(headPos)
        val underTension = Math.abs(deltaToNewHead.first) > 1 || Math.abs(deltaToNewHead.second) > 1
        if (underTension) {
            val dir = when {
                deltaToNewHead.first != 0 && deltaToNewHead.second != 0 -> calculateDiagonal(deltaToNewHead)
                deltaToNewHead.first != 0 -> deltaToNewHead.first.sign to 0
                else -> 0 to deltaToNewHead.second.sign
            }
            tailPos = tailPos.translate(X(dir.first) to Y(dir.second))
        }
        visitedTiles.add(tailPos)
    }

    private fun calculateDiagonal(deltaToNewHead: Pair<Int, Int>): Pair<Int, Int> {
        return deltaToNewHead.first.sign to deltaToNewHead.second.sign
    }

    fun uniqueTiles(): Any {
        val tileSet = mutableSetOf<Pair<X, Y>>()
        tileSet.addAll(visitedTiles)
        return tileSet.size
    }

    private fun printBoard() {
        var minX = headPos.first
        var maxX = headPos.first
        var minY = headPos.second
        var maxY = headPos.second
        visitedTiles.forEach {
            minX = minOf(minX, it.first)
            minY = minOf(minY, it.second)
            maxX = maxOf(maxX, it.first)
            maxY = maxOf(maxY, it.second)
        }
        println("-----------------------")
        for (y in minY.int..maxY.int) {
            for (x in minX.int..maxX.int) {
                val symbol = when {
                    x == headPos.first.int && y == headPos.second.int -> 'H'
                    x == tailPos.first.int && y == tailPos.second.int -> 'T'
                    else -> '.'
                }
                print(symbol)
            }
            println()
        }
        println("-----------------------")
    }
}

fun parseCommands(s: String): List<MoveCommand> {
    val (dir, units) = s.split(" ")
    val command = when (dir) {
        "U" -> MoveCommand.MoveUp()
        "D" -> MoveCommand.MoveDown()
        "L" -> MoveCommand.MoveLeft()
        "R" -> MoveCommand.MoveRight()
        else -> throw IllegalArgumentException()
    }
    return command.repeated(units.toInt())
}

private fun MoveCommand.repeated(times: Int): List<MoveCommand> {
    return Array(times) { this }.toList()
}

fun followCommands(input: String) {
    val commands = input.lines().filterNot { it.isBlank() }.flatMap {
        parseCommands(it)
    }
    val knots = Array(9) { KnotTravel() }
    commands.forEach {
        knots[0].moveHead(it)
        var nextHead = knots[0].getTail()
        knots.drop(1).forEach { model ->
            model.setHead(nextHead)
            nextHead = model.getTail()
        }
    }
    println(knots.last().uniqueTiles())
}

fun main() {
//    val input = """
//        R 4
//        U 4
//        L 3
//        D 1
//        R 4
//        D 1
//        L 5
//        R 2
//    """.trimIndent()
    val input = Utils.readResource("09.txt")
    followCommands(input)
}