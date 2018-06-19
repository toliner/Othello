package toliner.othello

import kotlin.math.min

fun main(args: Array<String>) {
    println("Game Start")
    println("Input format: x y")
    println("top left is (0,0)")
    println("bottom right is (7,7)")
    Board(step = 0).apply {
        println(this)
        run()
    }
}

data class Board(private val lines: List<Line> = initBoard(), private var step: Int) {
    tailrec fun run() {
        if (step > 60) {
            finish()
            return
        }
        //Game Logic
        println("Step ${step + 1}")
        if (handlePlayerProcess(Color.BLACK) || handlePlayerProcess(Color.WHITE)) {
            finish()
            return
        }
        step++
        run()
    }

    private fun finish() {
        println("-------------------")
        println("| Game Finished!! |")
        println("-------------------")
        getAllCells().run {
            count { it.color == Color.BLACK } to count { it.color == Color.WHITE }
        }.apply {
            println("Player 1:$first")
            println("Player 2:$second")
            println(
                    when {
                        first > second -> "Player 1 win!!"
                        first < second -> "Player 2 win!!"
                        else -> "draw!!"
                    }
            )
        }
    }

    private tailrec fun handlePlayerProcess(playerColor: Color): Boolean {
        print("Player ${playerColor.ordinal + 1}:")
        try {
            readLine()!!.also {
                if (it == "pass") {
                    if (checkForPass(playerColor)) {
                        return true
                    } else {
                        println("you cannot pass now")
                    }
                } else {
                    processPlayerAction(this[it.split(' ').map { it.toInt() }.zipWithNext().first()], playerColor)
                    println(this)
                    return false
                }
            }
        } catch (e: NumberFormatException) {
            //入力が不正
            println("your input is illegal format.")
            println("obey format x y")
            println("top left is (0,0)")
            println("bottom right is (7,7)")
            println("for example,")
            println("0 3")
            println("this means 3rd top line and 1st left line")
        } catch (e: ArrayIndexOutOfBoundsException) {
            //入力座標が範囲外
            println("out of field.")
        } catch (e: IllegalPositionException) {
            //すでに配置されている
            when (e.type) {
                IllegalPositionType.ALREADY_USED -> println("already used.")
                IllegalPositionType.CANNOT_PUT -> println("you cannot put there.")
            }
        }
        println(this)
        return handlePlayerProcess(playerColor)
    }

    private fun checkForPass(playerColor: Color): Boolean {
        return !getAllCells().filter {
            it.color == Color.NONE
        }.any { cell ->
            Vec.values().any { this.check(cell, playerColor, it) != null }
        }
    }

    private fun processPlayerAction(target: Cell, playerColor: Color) {
        // targetの取得ができている時点で盤面内
        if (target.color != Color.NONE) {
            throw IllegalPositionException(target.x, target.y, IllegalPositionType.ALREADY_USED)
        }
        // 全方向愚直探査
        if (!Vec.values().map { checkAndReverse(target, playerColor, it) }.any { it }) {
            // Failed
            throw IllegalPositionException(target.x, target.y, IllegalPositionType.CANNOT_PUT)
        }
        target.color = playerColor
    }

    private fun checkAndReverse(target: Cell, playerColor: Color, vec: Vec): Boolean {
        // Vecの方向に探査
        check(target, playerColor, vec)?.forEach { this[it.x, it.y].reverse() } ?: return false
        return true
    }

    private fun check(target: Cell, playerColor: Color, vec: Vec): List<Cell>? {
        return (min(min(target.x, 7 - target.x), min(target.y, 7 - target.y))).let { num ->
            if (num <= 0) return@let null
            // Noneか同色が出るまで回す
            // Noneが出る→反転させずに(例外)終了
            // 同色が出る→反転
            (1..num).map { this[target.x + vec.x * it, target.y + vec.y * it] }
                    .takeWhile { if (it.color == Color.NONE) return@let null else it.color != playerColor }
        }
    }

    private fun getAllCells(): List<Cell> = lines.flatMap { it.cells }

    override fun toString(): String {
        return buildString {
            appendln("  0 1 2 3 4 5 6 7")
            lines.forEach {
                append(it.y)
                append(' ')
                appendln(it)
            }
        }
    }

    operator fun get(y: Int): Line = lines[y]
    operator fun get(x: Int, y: Int): Cell = lines[y][x]
    operator fun get(pair: Pair<Int, Int>) = this[pair.first, pair.second]
}

data class Line(val y: Int, val cells: List<Cell> = initLine(y)) {
    override fun toString(): String {
        return buildString {
            cells.forEach {
                append(when (it.color) {
                    Color.BLACK -> '○'
                    Color.WHITE -> '×'
                    Color.NONE -> '-'
                })
                append(' ')
            }
        }
    }

    operator fun get(x: Int): Cell = cells[x]
}

data class Cell(val x: Int, val y: Int, var color: Color = Color.NONE) {
    fun reverse() {
        color = color.reversed()
    }
}

enum class Color {
    BLACK,
    WHITE,
    NONE;

    fun reversed(): Color {
        return when (this) {
            BLACK -> WHITE
            WHITE -> BLACK
            else -> NONE
        }
    }
}

enum class IllegalPositionType(val message: String) {
    ALREADY_USED("already used."),
    CANNOT_PUT("you cannot put there.")
}

data class IllegalPositionException(val x: Int, val y: Int, val type: IllegalPositionType) : RuntimeException()

enum class Vec(val x: Int, val y: Int) {
    UP(0, -1),
    DOWN(0, 1),
    RIGHT(1, 0),
    LEFT(-1, 0),
    UP_RIGHT(1, -1),
    UP_LEFT(-1, -1),
    DOWN_RIGHT(1, 1),
    DOWN_LEFT(-1, 1)
}

fun initLine(y: Int): List<Cell> = Array(8) { x ->
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
}.toList()

fun initBoard(): List<Line> = Array(8) { y -> Line(y) }.toList()
