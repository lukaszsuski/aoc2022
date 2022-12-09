class Day7 : Day(7) {

    companion object {
        private const val TOTAL_SPACE = 70_000_000
        private const val REQUIRED_SPACE = 30_000_000
    }

    sealed class Node(
        open val parent: Dir?,
        open val name: String
    ) {
        abstract fun size(): Int
    }


    data class File(
        override val parent: Dir,
        override val name: String,
        val size: Int
    ) : Node(parent, name) {
        override fun size(): Int = size
    }

    data class Dir(
        override val parent: Dir?,
        override val name: String,
        val nodes: ArrayList<Node> = ArrayList()
    ) : Node(parent, name) {
        override fun size(): Int = nodes.sumOf { it.size() }

        fun subDir(name: String): Dir = nodes
                .filterIsInstance<Dir>()
                .first { it.name == name }
    }

    override fun solve(input: List<String>) {

        val fs = FileSystem.fromTerminalOutput(input)

        fs.drawTree()

        val part1 = fs.findDirs { it.size() < 100000 }
            .sumOf { it.size() }
        println("part1: $part1")  //1443806

        val neededSpace = REQUIRED_SPACE - (TOTAL_SPACE - fs.totalSize())
        val part2 = fs.findDirs { it.size() >= neededSpace }
            .minByOrNull { it.size() }
        println("part2: ${part2?.size()}") //942298
    }

    class FileSystem {
        val root = Dir(null, "")

        fun totalSize(): Int = root.size()

        fun findDirs(predicate: (Dir) -> Boolean): List<Dir> {
            val dirs = ArrayList<Dir>()
            findDirs(dirs, root, predicate)
            return dirs
        }

        private fun findDirs(output: ArrayList<Dir>, current: Dir, predicate: (Dir) -> Boolean) {
            current.nodes
                .filterIsInstance<Dir>()
                .onEach { findDirs(output, it, predicate) }
                .filter(predicate)
                .toCollection(output)
        }

        fun drawTree(node: Node = root, indent: String = "") {
            when (node) {
                is File -> println("$indent File: ${node.name}, size: ${node.size}")
                is Dir -> {
                    println("$indent Dir: ${node.name}, size: ${node.size()}")
                    node.nodes.forEach { drawTree(it, "$indent\t") }
                }
            }
        }

        companion object {
            fun fromTerminalOutput(terminalOutput: List<String>): FileSystem {
                val fs = FileSystem()
                TerminalParser(fs).parse(terminalOutput)
                return fs
            }
        }
    }

    class TerminalParser(val fs: FileSystem) {

        private var cwd = fs.root

        fun parse(input: List<String>) {
            input.forEach { parseLine(it) }
        }

        private fun parseLine(line: String) {
            val isCdCmd = { l: String -> l.startsWith("$ cd") }
            val isDir = { l: String -> l.startsWith("dir") }
            val isFile = { l: String -> l.matches("\\d.*".toRegex()) }
            when {
                isDir(line) -> parseDir(line)
                isFile(line) -> parseFile(line)
                isCdCmd(line) -> parseCd(line)
            }
        }

        private fun parseCd(line: String) {
            when (val destination = line.split(" ")[2]) {
                "/" -> {
                    cwd = fs.root
                }
                ".." -> cwd = cwd.parent ?: fs.root
                else -> {
                    cwd = cwd.subDir(destination)
                }
            }
        }

        private fun parseFile(line: String) {
            val tokens = line.split(" ")
            val fileName = tokens[1]
            val fileSize = tokens[0].toInt()
            cwd.nodes.add(File(cwd, fileName, fileSize))
        }

        private fun parseDir(line: String) {
            val dirName = line.split(" ")[1]
            cwd.nodes.add(Dir(cwd, dirName))
        }
    }

}

