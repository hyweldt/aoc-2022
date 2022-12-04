fun parseRanges(string: String): Pair<IntRange, IntRange> {
    val ranges = string.split(",")
    val firstRange = ranges[0].split("-").map { it.toInt() }
    val secondRange = ranges[1].split("-").map { it.toInt() }
    return Pair(firstRange[0]..firstRange[1], secondRange[0]..secondRange[1])
}

fun IntRange.subsumes(intRange: IntRange): Boolean {
    return this.contains(intRange.first) && this.contains(intRange.last)
}

infix fun IntRange.overlaps(intRange: IntRange): Boolean {
    return this.contains(intRange.first)
            || this.contains(intRange.last)
            || intRange.contains(this.first)
            || intRange.contains(this.last)
}

fun main() {
    val input = Utils.readResource("04.txt")
    val ranges = input.lines().filterNot { it.isBlank() }.map { parseRanges(it) }
    ranges.fold(0) { acc, (r1,r2) ->
        if (r1 overlaps r2) {
            acc + 1
        } else {
            acc
        }
    }.let { println("Overlaps: $it") }
}