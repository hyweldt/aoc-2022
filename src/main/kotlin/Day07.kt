data class Node(val name: String, val size: Long, val children: MutableList<Node>, val parent: Node?) {
    val path: String
        get() {
            var p = parent
            var s = name
            while (p != null) {
                s = p.name + "/" + s
                p = p.parent
            }
            return s
        }

    fun isDir(): Boolean {
        return this.size == 0L
    }

    fun getOrAppendChild(path: String): Node {
        val existing = children.find { it.name == path }
        if (existing != null) {
            return existing
        }
        val child = Node(path, 0, mutableListOf(), this)
        children.add(child)
        return child
    }

    fun appendChild(name: String, size: Long) {
        if (children.none { it.path == name }) {
            children.add(Node(name, size, mutableListOf(), this))
        }
    }

    override fun toString(): String {
        return "name:$name children:${children.map { it.toString() }} size: $size"
    }

    fun calculateSize(): Long {
        var size = this.size
        for (c in children) {
            size += c.calculateSize()
        }
        return size
    }

    fun findDirectoriesSmallerThanOrEqual(x: Long): List<Node> {
        val nodes = mutableListOf<Node>()
        for (c in children.filter { it.isDir() }) {
            val childSize = c.calculateSize()
            if (childSize < x) {
                nodes += c
                nodes += c.recursivelyFindDirectories()
            } else {
                nodes += c.findDirectoriesSmallerThanOrEqual(x)
            }
        }
        return nodes
    }

    private fun recursivelyFindDirectories(): List<Node> {
        val dirs = mutableListOf<Node>()
        for (c in children.filter { it.isDir() }) {
            dirs.addAll(c.recursivelyFindDirectories())
            dirs.add(c)
        }
        return dirs
    }

    fun findDirectoriesLargerThanOrEqual(deltaRequired: Long): List<Node> {
        val nodes = mutableListOf<Node>()
        for (c in children.filter { it.isDir() }) {
            val childSize = c.calculateSize()
            if (childSize >= deltaRequired) {
                nodes += c
                nodes += c.findDirectoriesLargerThanOrEqual(deltaRequired)
            }
        }
        return nodes

    }
}

data class Model(
    val rootNode: Node = Node("", 0, mutableListOf(), null),
) {
    private var currentNode = rootNode
    private fun cd(path: String) {
        println("Changing to $path")
        currentNode = if (path == "/") {
            rootNode
        } else if (path == "..") {
            if (currentNode.parent == null) {
                throw IllegalStateException("Cannot traverse past root")
            } else {
                currentNode.parent!!
            }
        } else {
            currentNode.getOrAppendChild(path)
        }
        println(currentNode.path)
    }

    fun applyCommand(s: String) {
        val parts = s.split(" ").drop(1)
        when (parts[0]) {
            "cd" -> cd(parts[1])
            "ls" -> ls()
        }
    }

    private fun ls() {
        //Do nothing
    }

    fun processFile(fileLine: String) {
        val (typeOrSize, name) = fileLine.split(" ")
        println("Contains entry $typeOrSize $name")
        if (typeOrSize != "dir") {
            currentNode.appendChild(name, typeOrSize.toLong())
        } else {
            currentNode.getOrAppendChild(name)
        }
        println(currentNode)
    }
}

fun String.isCommand(): Boolean {
    return this.startsWith("$")
}

fun Model.parseCommandLine(s: String) {
    val lines = s.lines().filterNot { it.isBlank() }
    lines.forEach {
        if (it.isCommand()) {
            applyCommand(it)
        } else {
            processFile(it)
        }
    }
}

fun main() {
//    val input = """
//        ${'$'} cd /
//        ${'$'} ls
//        dir a
//        14848514 b.txt
//        8504156 c.dat
//        dir d
//        ${'$'} cd a
//        ${'$'} ls
//        dir e
//        29116 f
//        2557 g
//        62596 h.lst
//        ${'$'} cd e
//        ${'$'} ls
//        584 i
//        ${'$'} cd ..
//        ${'$'} cd ..
//        ${'$'} cd d
//        ${'$'} ls
//        4060174 j
//        8033020 d.log
//        5626152 d.ext
//        7214296 k
//    """.trimIndent()
    val input = Utils.readResource("07.txt")
    val model = Model()
    model.parseCommandLine(input)
    val smallDirs = model.rootNode.findDirectoriesSmallerThanOrEqual(100_000).toMutableList()
    val total = smallDirs.sumOf { it.calculateSize() }
    println(total)
    val requiredSpace = 30_000_000
    val totalSpace = 70_000_000
    val usedSpace = model.rootNode.calculateSize()
    val freeSpace = totalSpace - usedSpace
    val deltaRequired = requiredSpace - freeSpace
    println("Need $deltaRequired")
    val foldersGreaterThanDelta = model.rootNode.findDirectoriesLargerThanOrEqual(deltaRequired)
    val smallest = foldersGreaterThanDelta.minByOrNull { it.calculateSize() }
    println(smallest?.calculateSize())
}