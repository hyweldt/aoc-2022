/**
 * Reads a string of a list of integers (separated by newlines), returning the sum of
 * integers in contiguous rows (in the same order as the input rows). Blank lines separate entries.
 */
fun getTotalCaloriesPerElf(rows: String): List<Int> {
    val lines = rows.split('\n')
    if (lines.isEmpty()) {
        return emptyList()
    }
    val calories: MutableList<Int> = mutableListOf(0)
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed == "") {
            //add new item
            calories += 0
        } else {
            calories[calories.size - 1] += trimmed.toInt()
        }
    }
    if (calories.last() == 0) {
        calories.removeAt(calories.lastIndex)
    }
    return calories
}

fun main() {
    val testFile = Utils.readResource("01.txt")
    val elfCalories = getTotalCaloriesPerElf(testFile)
    part1(elfCalories)
    part2(elfCalories)
}

private fun part2(elfCalories: List<Int>) {
    println("Sum of top 3 calories: " + elfCalories.sorted().reversed().subList(0, 3).sum())
}

private fun part1(elfCalories: List<Int>) {
    println("Max calories: " + elfCalories.maxOrNull())
}