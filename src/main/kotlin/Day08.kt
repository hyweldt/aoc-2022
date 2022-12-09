fun Array<IntArray>.getVisibleSides(i: Int, j: Int): Int {
    val height = this[i][j]
    var northVisible = 1
    var southVisible = 1
    var westVisible = 1
    var eastVisible = 1
    for (northIndex in 0 until i) {
        if (this[northIndex][j] >= height) {
            northVisible = 0
        }
    }
    for (southIndex in i + 1 until this.size) {
        if (this[southIndex][j] >= height) {
            southVisible = 0
        }
    }
    for (westIndex in 0 until j) {
        if (this[i][westIndex] >= height) {
            westVisible = 0
        }
    }
    for (eastIndex in j + 1 until this.size) {
        if (this[i][eastIndex] >= height) {
            eastVisible = 0
        }
    }
    return northVisible + southVisible + westVisible + eastVisible
}

fun countVisibleTrees(input: String): Int {
    var visibleTrees = 0
    parseTrees(input).let { trees ->

        for ((rowIndex, row) in trees.withIndex()) {
            for ((columnIndex, column) in row.withIndex()) {
                if (trees.getVisibleSides(rowIndex, columnIndex) > 0) {
                    visibleTrees += 1
                }
            }
        }
    }
    return visibleTrees
}

fun getBestScenicScore(input: String): Int {
    var bestScore = 0
    parseTrees(input).let { trees ->
        for ((rowIndex, row) in trees.withIndex()) {
            for ((columnIndex, column) in row.withIndex()) {
                trees.getScenicScore(rowIndex, columnIndex).takeIf { it > bestScore }?.let { bestScore = it }
            }
        }
    }
    return bestScore
}

fun Array<IntArray>.getScenicScore(i: Int, j: Int): Int {
    val height = this[i][j]
    var northVisible = 0
    var southVisible = 0
    var westVisible = 0
    var eastVisible = 0
    for (northIndex in i - 1 downTo 0) {
        northVisible += 1
        if (this[northIndex][j] >= height) {
            break
        }
    }
    for (southIndex in i + 1 until this.size) {
        southVisible += 1
        if (this[southIndex][j] >= height) {
            break
        }
    }
    for (westIndex in j - 1 downTo 0) {
        westVisible += 1
        if (this[i][westIndex] >= height) {
            break
        }
    }
    for (eastIndex in j + 1 until this.size) {
        eastVisible += 1
        if (this[i][eastIndex] >= height) {
            break
        }
    }
    return northVisible * southVisible * westVisible * eastVisible

}

private fun parseTrees(input: String): Array<IntArray> {
    val rows = input.lines().filterNot { it.isBlank() }
    val columns = rows.first().length
    val trees = Array(rows.size) { IntArray(columns) }
    for ((rowIndex, row) in rows.withIndex()) {
        val treesInRow = row.split("").filterNot { it.isBlank() }.map { it.toInt() }
        for ((colIndex, tree) in treesInRow.withIndex()) {
            trees[rowIndex][colIndex] = tree
        }
    }
    trees.forEach {
        println(it.contentToString())
    }
    return trees
}

fun main() {
//    val input = """
//        30373
//        25512
//        65332
//        33549
//        35390
//    """.trimIndent()
    val input = Utils.readResource("08.txt").trimIndent()
    countVisibleTrees(input).also { println(it) }
    getBestScenicScore(input).also { println(it) }
}