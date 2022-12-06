package day06

fun String.unique(): Boolean {
    return this.toSet().size == this.length
}

fun findUniqueStringPosition(length: Int, string: String): Int {
    var endOfStartPacketPos = 0
    var startOfPacket = ""
    for (i in string.indices) {
        startOfPacket += string[i]
        if (startOfPacket.length == length) {
            if (startOfPacket.unique()) {
                endOfStartPacketPos = i
                break
            }
            startOfPacket = startOfPacket.substring(1)
        }
    }
    return endOfStartPacketPos + 1
}

fun main() {
    val input = Utils.readResource("06.txt")
    println("Part one: " + findUniqueStringPosition(4, input))
    println("Part two: " + findUniqueStringPosition(14, input))
}
