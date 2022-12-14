package day14

typealias TwoDArray<T> = Array<Array<T>>

class Grid(val minCol: Int, val maxCol: Int, val minRow: Int, val maxRow: Int) {
    private val numRows = maxRow - minRow + 1
    private val numCols = maxCol - minCol + 1
    private val grid = TwoDArray(numRows) {
        Array(numCols) { Tile.Air }
    }

    fun set(x: Int, y: Int, tile: Tile) {
        grid[y - minRow][x - minCol] = tile
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
        if (normalisedRow < 0 || normalisedRow >= numRows || normalisedCol < 0 || normalisedCol >= numCols) {
            return Tile.Void
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
    println(iterations - 1)
}