package toliner.othello

import kotlin.math.min

fun main(args: Array<String>) {
    println("Game Start")
    println("Input format: x y")
    println("top left is (0,0)")
    println("bottom right is (7,7)")
    Board(step = 0).run()
}

data class Board(private val lines: List<Line> = initBoard(), private var step: Int) {
    tailrec fun run() {
        if (step > 60) return //ToDo: 終了時処理
        //Game Logic
        println("Step $step")
        println(this)
        handlePlayerProcess(Color.BLACK)
        handlePlayerProcess(Color.WHITE)
        step++
        run()
    }

    private fun handlePlayerProcess(playerColor: Color) {
        print("Player ${playerColor.ordinal + 1}:")
        try {
            processPlayerAction(this[readLine()!!.split(' ').map { it.toInt() }.zipWithNext().first()], playerColor)
            return
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
            println("already used.")
        }
        handlePlayerProcess(playerColor)
    }

    private fun processPlayerAction(target: Cell, playerColor: Color) {
        // targetの取得ができている時点で盤面内
        if (target.color != Color.NONE) {
            throw IllegalPositionException(target.x, target.y)
        }
        // 全方向愚直探査
        // 上方向
        (7 - target.y).let { num ->
            if (num <= 0) {
                return@let false
            }
            // 反転可能判定
            if ((1..num).any { this[target.x, target.y + it].color == playerColor }) {
                //反転
                //whileしたいだけなので戻ってきた値は捨てる
                (1..num).takeWhile {
                    this[target.x, target.y + it].run {
                        if (color == playerColor) false
                        else {
                            reverse()
                            true
                        }
                    }
                }
                return@let true
            } else return@let false
        }
        // 残り5方向にやって、全部の結果のorを取りたい。
        // falseなら設置不可で例外。
    }

    private fun checkAndReverse(target: Cell, playerColor: Color, vec: Vec): Boolean {
        // targetの取得ができている時点で盤面内
        if (target.color != Color.NONE) {
            throw IllegalPositionException(target.x, target.y)
        }
        // Vecの方向に探査
        return (min(min(target.x, 7 - target.x), min(target.y, 7 - target.y))).let { num ->
            if (num <= 0) return@let false
            // Noneか同色が出るまで回す
            // Noneが出る→反転させずに(例外)終了
            // 同色が出る→反転
            return try {
                (1..num).map { this[target.x + vec.x * num, target.y + vec.y * num] }
                        .takeWhile { if (playerColor == Color.NONE) throw NoneCellException() else it.color == playerColor }
                        .forEach { it.reverse() }
                true
            } catch (e: NoneCellException) {
                false
            }
        }
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

data class IllegalPositionException(val x: Int, val y: Int) : RuntimeException()

class NoneCellException : RuntimeException()

enum class Vec(val x: Int, val y: Int) {
    UP(0, 1),
    DOWN(0, -1),
    RIGHT(1, 0),
    LEFT(-1, 0),
    UP_RIGHT(1, 1),
    UP_LEFT(-1, 1),
    DOWN_RIGHT(1, -1),
    DOWN_LEFT(-1, -1)
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
