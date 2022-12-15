package day14

import kotlin.math.absoluteValue

typealias TwoDArray<T> = Array<Array<T>>

class Grid(val minCol: Int, val maxCol: Int, val minRow: Int, val maxRow: Int) {
    private val numRows = maxRow - minRow + 1 + 2 //floor below
    private val numCols = maxCol - minCol + 1
    private val grid = TwoDArray(numRows) {
        Array(numCols) { Tile.Air }
    }
    private val overflowLeft = mutableListOf<Array<Tile>>()
    private val overflowRight = mutableListOf<Array<Tile>>()

    init {
        for (x in minCol..maxCol) {
            set(x, maxRow + 2, Tile.Rock)
        }
    }

    fun set(x: Int, y: Int, tile: Tile) {
        val normalisedY = y - minRow
        val normalisedX = x - minCol
        if (normalisedX < 0) {
            val overflowIndex = normalisedX.absoluteValue - 1
            if (overflowLeft.size < overflowIndex) {
                overflowLeft.add(Array(numRows) { i -> if (i == numRows - 1) Tile.Rock else Tile.Air })
            }
            overflowLeft[overflowIndex][normalisedY] = tile
        } else if (normalisedX >= numCols) {
            val overflowIndex = normalisedX - numCols
            if (overflowRight.size < overflowIndex) {
                overflowRight.add(Array(numRows) { i -> if (i == numRows - 1) Tile.Rock else Tile.Air })
            }
            overflowRight[overflowIndex][normalisedY] = tile
        } else {
            grid[normalisedY][normalisedX] = tile
        }
    }

    fun draw() {
        for (row in grid) {
            for (tile in row) {
                print(tile.char)
            }
            println()
        }
    }

    private fun tileAt(x: Int, y: Int): Tile {
        val normalisedRow = y - minRow
        val normalisedCol = x - minCol
        if (normalisedRow == numRows - 1) {
            return Tile.Rock
        }
        if (normalisedCol < 0) {
            val overflowIndex = normalisedCol.absoluteValue - 1
            if (overflowLeft.size <= overflowIndex) {
                overflowLeft.add(Array(numRows) { i -> if (i == numRows - 1) Tile.Rock else Tile.Air })
            }
            return overflowLeft[overflowIndex][normalisedRow]
        } else if (normalisedCol >= numCols) {
            val overflowIndex = normalisedCol - numCols
            if (overflowRight.size <= overflowIndex) {
                overflowRight.add(Array(numRows) { i -> if (i == numRows - 1) Tile.Rock else Tile.Air })
            }
            return overflowRight[overflowIndex][normalisedRow]

        }
        return grid[normalisedRow][normalisedCol]
    }

    fun moveSand(currentSandPos: Pair<Int, Int>): Pair<Int, Int>? {
        //try to move down
        var tile = tileAt(currentSandPos.first, currentSandPos.second + 1)
        if (tile == Tile.Void) {
            return null
        }
        if (tile == Tile.Air) {
            return currentSandPos.first to currentSandPos.second + 1
        }

        tile = tileAt(currentSandPos.first - 1, currentSandPos.second + 1)
        if (tile == Tile.Void) {
            return null
        }
        if (tile == Tile.Air) {
            return currentSandPos.first - 1 to currentSandPos.second + 1
        }

        tile = tileAt(currentSandPos.first + 1, currentSandPos.second + 1)
        if (tile == Tile.Void) {
            return null
        }
        if (tile == Tile.Air) {
            return currentSandPos.first + 1 to currentSandPos.second + 1
        }
        return currentSandPos
    }

    fun simulate(): Boolean {
        var currentSandPos = 500 to 0
        var nextPos: Pair<Int, Int> = currentSandPos
        while (true) {
            var tryMove = moveSand(nextPos)
            if (tryMove == null) {
                return true
            } else if (tryMove == nextPos) {
                break
            }
            tryMove?.let { nextPos = it }
        }
        if(nextPos.first == 500 && nextPos.second == 0) {
            return true
        }
        set(nextPos.first, nextPos.second, Tile.Sand)
        return false
    }
}


enum class Tile(val char: Char) {
    Air('.'),
    Rock('#'),
    Sand('o'),
    Source('+'),
    Void('~')
}

val example = """
    498,4 -> 498,6 -> 496,6
    503,4 -> 502,4 -> 502,9 -> 494,9
""".trimIndent()

fun parseLine(t: String) = iterator {
    val nodes = t.split(" -> ")
    var previousNode: Pair<Int, Int>? = null
    nodes.forEach { coords ->
        val (column, row) = coords.split(',').map { it.toInt() }
        if (previousNode == null) {
            yield(column to row)
        }
        previousNode?.let {
            val columns = minOf(it.first, column)..maxOf(it.first, column)
            val rows = minOf(it.second, row)..maxOf(it.second, row)
            for (i in columns) {
                for (j in rows) {
                    yield(i to j)
                }
            }
        }
        previousNode = column to row
    }
}

fun parseLines(s: String): Grid {
    val extremes = parseExtremes(s).also { println(it) }
    val grid = Grid(extremes.first.first, extremes.first.second, extremes.second.first, extremes.second.second)
    s.lines().filterNot { it.isBlank() }.forEach {
        parseLine(it).forEach {
            grid.set(it.first, it.second, Tile.Rock)
        }
    }
    grid.set(500, 0, Tile.Source)
    return grid
}

fun parseExtremes(s: String): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    var minColumn = 500
    var maxColumn = 500
    var minRow = 0
    var maxRow = 0
    s.lines().filterNot { it.isBlank() }.forEach { line ->
        parseLine(line).forEach {
            minColumn = minOf(minColumn, it.first)
            maxColumn = maxOf(maxColumn, it.first)
            minRow = minOf(minRow, it.second)
            maxRow = maxOf(maxRow, it.second)
        }
    }
    return (minColumn to maxColumn) to (minRow to maxRow)
}


fun main() {
//    val grid = parseLines(example)
    val grid = parseLines(Utils.readResource("14.txt"))
    //One grain
    var done = false
    var iterations = 0
    while (!done) {
        done = grid.simulate()
        iterations += 1
    }
    grid.draw()
    println(iterations )
}