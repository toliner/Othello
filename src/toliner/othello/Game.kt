package toliner.othello

fun main(args: Array<String>) {
    println("Game Start")
    println("Input format: x y")
    println("top left is (0,0)")
    println("bottom right is (7,7)")
    Board(step = 0).run()
}

data class Board(private val lines: List<Line> = initBoard(), val step: Int) {
    fun run() {
        if (step > 60) return //ToDo: 終了時処理
        //Game Logic
        println("Step $step")
        println(this)

    }

    fun processPlayerAction(target: Cell) {

    }

    override fun toString(): String {
        return buildString {
            lines.forEach {
                appendln(it)
            }
        }
    }

    operator fun get(y: Int): Line = lines[y]
    operator fun get(x: Int, y: Int): Cell = lines[y][x]
    operator fun get(pair: Pair<Int, Int>) = this[pair.first, pair.second]
}

data class Line(private val y: Int, private val cells: List<Cell> = initLine(y)) {
    override fun toString(): String {
        return buildString {
            cells.forEach {
                append(when (it.color) {
                    Color.BLACK -> '◯'
                    Color.WHITE -> '☓'
                    Color.NONE -> '・'
                })
            }
        }
    }

    operator fun get(x: Int): Cell = cells[x]
}

data class Cell(val x: Int, val y: Int, var color: Color = Color.NONE)

enum class Color {
    BLACK,
    WHITE,
    NONE
}

fun initLine(y: Int): List<Cell> = Array(8, { x ->
    when (x) {
        3 -> when (y) {
            3 -> Cell(x, y, Color.BLACK)
            4 -> Cell(x, y, Color.WHITE)
            else -> Cell(x, y)
        }
        4 -> when (y) {
            3 -> Cell(x, y, Color.WHITE)
            4 -> Cell(x, y, Color.BLACK)
            else -> Cell(x, y)
        }
        else -> Cell(x, y)
    }
}).toList()

fun initBoard(): List<Line> = Array(8, { y -> Line(y) }).toList()
