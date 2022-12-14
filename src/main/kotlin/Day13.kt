package day13

sealed interface Type {
    data class IntType(val value: Int) : Type {
        override fun toString(): String {
            return value.toString()
        }
    }

    data class ListType(val value: MutableList<Type>) : Type {
        override fun toString(): String {
            return '[' + value.joinToString(",") + ']'
        }
    }
}

val example = """
    [1,1,3,1,1]
    [1,1,5,1,1]

    [[1],[2,3,4]]
    [[1],4]

    [9]
    [[8,7,6]]

    [[4,4],4,4]
    [[4,4],4,4,4]

    [7,7,7,7]
    [7,7,7]

    []
    [3]

    [[[]]]
    [[]]

    [1,[2,[3,[4,[5,6,7]]]],8,9]
    [1,[2,[3,[4,[5,6,0]]]],8,9]
""".trimIndent()

fun parseList(iterator: Iterator<Char>): Type.ListType {
    val outer = Type.ListType(mutableListOf())
    var intString = ""
    while (true) {
        val c = iterator.next()
//        print(c)
        if (c == '[') {
            val l = parseList(iterator)
            outer.value.add(l)
        } else if (c == ']') {
            if (intString.isNotEmpty()) {
                outer.value.add(Type.IntType(intString.toInt()))
                intString = ""
            }
//            println()
            return outer
        } else if (c == ',') {
            if (intString.isNotEmpty()) {
                outer.value.add(Type.IntType(intString.toInt()))
                intString = ""
            }
        } else {
            intString += c
        }
    }
}

fun parse(input: String): List<Type.ListType> {
    return input.lines().filterNot { it.isBlank() }.map { parseList(it.iterator().also { it.next() }) }
}

fun compare(t1: Type, t2: Type): Int {
    if (t1 is Type.IntType) {
        if (t2 is Type.IntType) {
            return t1.value - t2.value
        } else {
            return compare(Type.ListType(mutableListOf(t1)), t2)
        }
    } else {
        t1 as Type.ListType
        if (t2 is Type.IntType) {
            return compare(t1, Type.ListType(mutableListOf(t2)))
        } else {
            t2 as Type.ListType
            //compare by values
            t1.value.forEachIndexed { index, type ->
                if (t2.value.size <= index)
                    return 1
                val c = compare(type, t2.value[index])
                if (c != 0) {
                    return c
                }
            }
            //shortest list should come first
            return t1.value.size - t2.value.size
        }
    }
}

fun areOrdered(aType: Type.ListType, bType: Type.ListType): Boolean {
    return compare(aType, bType) <= 0
}

fun main() {
//    val iterator = "[[8],[1,2,3]]".iterator().also { it.next() }
//    parseList(iterator).also { println(it) }

    val dividerPackets = listOf("[[2]]", "[[6]]").map { parseList(it.iterator().also { it.next() }) }
    val file = Utils.readResource("13.txt")
    val pairs = parse(file) + dividerPackets
    var mul = 1
    val sorted = pairs.sortedWith(::compare).also { println(it.joinToString("\n")) }
    sorted.forEachIndexed { index, listType ->
        if (compare(listType, dividerPackets[0]) == 0 || compare(listType, dividerPackets[1]) == 0) {
            mul *= (index + 1)
        }
    }
    println(mul)
}