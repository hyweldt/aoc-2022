package day06

import java.util.concurrent.ArrayBlockingQueue

/**
 * Assume char represents an ASCII lowercase letter.
 * @return the offset of the letter from 'a', so 'a' -> 0, 'b' -> 1, etc.
 */
fun Char.lowerAlphaOffset(): Int {
    return this - 'a'
}


/**
 * This implementation attempts to minimize allocations and copying.
 */
class UniqueStringFinder(private val size: Int) {
    //bitmask of characters in string (all lowercase a-z so 32 bits is fine)
    private var alphaMask: Int = 0

    //Keep a queue of characters in the candidate marker
    private val charQueue = ArrayBlockingQueue<Int>(size)

    /**
     * Works only for strings of lowercase a-z characters.
     * @return the index of the character following the first unique prefix of length [size]
     * or -1 if no such prefix exists.
     */
    fun findUniquePrefixBoundary(string: String): Int {
        clear()
        for (i in string.indices) {
            val alphaOffset = string[i].lowerAlphaOffset()
            val charMask = 1 shl alphaOffset
            if (alphaMask or charMask == alphaMask) {
                //We've seen this character before so unique prefix can only
                // begin from at least the character after our duplicate
                dropUntilFirstInstanceOf(alphaOffset)
            }
            add(charMask, alphaOffset)

            if (charQueue.size == size) {
                //Found unique prefix - return the index of the next character
                return i + 1
            }
        }
        return -1
    }

    private fun clear() {
        alphaMask = 0
        charQueue.clear()
    }

    private fun add(charMask: Int, alphaOffset: Int) {
        alphaMask = alphaMask.or(charMask)
        charQueue.add(alphaOffset)
    }

    private fun dropUntilFirstInstanceOf(
        alphaOffset: Int,
    ) {
        //drop chars until we remove the first duplicate
        var toDrop = charQueue.poll()
        while (toDrop != alphaOffset) {
            alphaMask = alphaMask.and(1.shl(toDrop).inv())
            toDrop = charQueue.poll()
        }
    }

}

fun findUniqueStringPosition(length: Int, string: String): Int {
    val finder = UniqueStringFinder(length)
    return finder.findUniquePrefixBoundary(string)
}

fun main() {
    val input = Utils.readResource("06.txt")
    println("Part one: " + findUniqueStringPosition(4, input))
    println("Part two: " + findUniqueStringPosition(14, input))
}
