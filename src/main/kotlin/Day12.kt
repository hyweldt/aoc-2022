package day12

import Utils

val example = """
    Sabqponm
    abcryxxl
    accszExk
    acctuvwj
    abdefghi
""".trimIndent()

data class Cell(
    val index: Pair<Int, Int>,
    val char: Char,
    val height: Int,
    var distanceFromStart: Int,
    var distanceFromEnd: Int,
) {
    override fun toString(): String {
        return "[$char ${height.toString().padStart(2, '0')} $distanceFromStart $distanceFromEnd]"
    }
}

data class Grid(
    var startPosition: Pair<Int, Int>,
    var endPosition: Pair<Int, Int>,
    val g: Array<Array<Cell>>,
) {
    override fun toString(): String {
        return "$startPosition -> $endPosition\n${
            g.map {
                it.joinToString(" ") {
                    it.toString()
                }
            }.joinToString("\n")
        }"
    }

    fun toCoords(): String {
        return toStr {
            "${it.index.first},${it.index.second}"
        }
    }

    fun toDistanceFromStart(): String {
        return toStr {
            "${it.distanceFromStart.toString().padStart(3, ' ')}"
        }
    }

    fun toChar(): String {
        return toStr {
            "${it.char.toString().padStart(3, ' ')}"
        }
    }

    fun toCharAndDistanceFromStart(): String {
        return toChar().lines().zip(toDistanceFromStart().lines()).joinToString("\n") {
            it.first + "\n" + it.second
        }
    }

    fun toStr(f: (Cell) -> String): String {
        return g.map {
            it.joinToString(" ") { f(it) }
        }.joinToString("\n")
    }

    private fun stepOut(y: Int, x: Int) {
        val d = g[y][x].distanceFromStart
        val visited = mutableListOf<Pair<Int, Int>>()
        nextStep(y, x - 1, d, g[y][x].height).takeIf { it }?.let { visited.add(y to x - 1) }
        nextStep(y, x + 1, d, g[y][x].height).takeIf { it }?.let { visited.add(y to x + 1) }
        nextStep(y - 1, x, d, g[y][x].height).takeIf { it }?.let { visited.add(y - 1 to x) }
        nextStep(y + 1, x, d, g[y][x].height).takeIf { it }?.let { visited.add(y + 1 to x) }
        visited.forEach {
            stepOut(it.first, it.second)
        }
    }

    private fun nextStep(y: Int, x: Int, d: Int, height: Int): Boolean {
        if (y >= 0 && y < g.size
            && x >= 0 && x < g[0].size
            && (g[y][x].height <= height || g[y][x].height == height + 1)
            && (g[y][x].distanceFromStart == -1 || g[y][x].distanceFromStart > d + 1)
        ) {
            g[y][x].distanceFromStart = d + 1
            return true
        }
        return false
    }

    fun solve(startPosition: Pair<Int, Int>): Int {
        stepOut(startPosition.first, startPosition.second)
        return g[endPosition.first][endPosition.second].distanceFromStart
    }

    fun describe(): String {
        return "$startPosition -> $endPosition"
    }

    fun find(c: Char): List<Cell> {
        return g.flatMap { row ->
            row.filter { it.char == c }
        }
    }

    fun reset() {
        g.forEach { row ->
            row.forEach { cell ->
                cell.distanceFromStart = -1
                cell.distanceFromEnd = -1
            }
        }
    }
}

fun parseGrid(s: String): Grid {
    val lines = s.lines().filterNot { it.isBlank() }
    val g = Array(lines.size) { row ->
        Array(lines[0].length) { col ->
            Cell(row to col, ' ', 0, -1, -1)
        }
    }
    var startPosition: Pair<Int, Int>? = null
    var endPosition: Pair<Int, Int>? = null
    lines.forEachIndexed { index, line ->
        line.forEachIndexed { cI, c ->
            when (c) {
                'S' -> {
                    startPosition = index to cI
                    g[index][cI] = Cell(index to cI, c, 0, 0, -1)
                }

                'E' -> {
                    endPosition = index to cI
                    g[index][cI] = Cell(index to cI, c, 26, -1, 0)
                }

                else -> {
                    g[index][cI] = Cell(index to cI, c, c - 'a' + 1, -1, -1)
                }
            }
        }
    }
    return Grid(
        startPosition!!, endPosition!!, g
    )
}

fun main() {
    val grid = parseGrid(Utils.readResource("12.txt"))
//    val grid = parseGrid(example)
    println(grid.describe())
    //Part 1
//    grid.solve(grid.startPosition)
    //Part 2
    grid.find('a').map {
        grid.reset()
        it.distanceFromStart = 0
        val s = grid.solve(it.index)
        println(grid.toCharAndDistanceFromStart())
        println()
        s
    }.filterNot { it == -1 }.minOrNull().also { println(it) }
//    println(grid.toCharAndDistanceFromStart())
}