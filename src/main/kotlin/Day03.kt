fun itemToPriority(char: Char): Int {
    return when (char) {
        in 'a'..'z' -> {
            char - 'a' + 1
        }
        in 'A'..'Z' -> {
            char - 'A' + 27
        }
        else -> {
            throw IllegalArgumentException("Invalid character: $char")
        }
    }
}

data class RucksackContents(
    val compartment1: List<String>,
    val compartment2: List<String>
) {
    fun findCommonPriority(): Int? {
        return compartment1.intersect(compartment2.toSet()).takeIf { it.isNotEmpty() }
            ?.map { itemToPriority(it.toCharArray()[0]) }?.toIntArray()?.sum()
    }
}

fun parseRucksackContents(contents: String): RucksackContents {
    val length = contents.length
    //first half of contents are in compartment1
    val compartment1 = contents.substring(0, length / 2).split("").filter { it.isNotEmpty() }
    //second half of contents are in compartment2
    val compartment2 = contents.substring(length / 2).split("").filter { it.isNotEmpty() }
    return RucksackContents(compartment1, compartment2)
}

fun identifyBadgeGroup(rucksacks: List<RucksackContents>): Int? {
    (rucksacks[0].compartment1 + rucksacks[0].compartment2).toSet().forEach {
        if (
            (rucksacks[1].compartment1 + rucksacks[1].compartment2).contains(it)
            && (rucksacks[2].compartment1 + rucksacks[2].compartment2).contains(it)) {
            return itemToPriority(it.toCharArray()[0])
        }
    }
    throw Exception("No common item found")
}

fun main() {
    val input = Utils.readResource("03.txt")
    var sum = 0
    for (sacks in input.lines().filterNot { it.isBlank() }.map { parseRucksackContents(it) }.chunked(3)) {
        val group = identifyBadgeGroup(sacks)
        sum += group ?: 0
    }
    println("Sum: $sum")
}