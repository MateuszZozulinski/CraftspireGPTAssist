package it.craftspire.gptassist

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

/**
 * Few algorithmic test cases to verify quality of the ChatGPT prompt
 */
class TestCases {

    /**
     * find how many numbers have to be added to an array to achieve increasing sequence
     */
    fun simpleAlgorithm(numbers: MutableList<Int>): Int {
        val size = numbers.size
        val sorted = numbers.sorted()
        return sorted[size-1] - sorted[0] - size + 1
    }

    /**
     * validate sudoku solution
     */
    fun isSudokuSolutionValid(grid: MutableList<MutableList<Int>>): Boolean {
        val validRows = grid.all { it.distinct().count() == 9 }
        val cols = (0 until 9).map { grid.map { r -> r[it] } }
        val validColumns = cols.all { it.distinct().count() == 9 }

        for (i in 0 until  9) {
            val set = mutableSetOf<Int>()
            val k = i * 3
            val x = (k / 9)*3
            val y = k % 9
            for (x1 in 0 until 3) {
                for (y1 in 0 until 3) {
                    set.add(grid[x + x1][y + y1])
                }
            }
            if (set.size != 9) return false
        }
        return validRows && validColumns
    }

    fun generatePossibleSublists(set: MutableList<Int>): MutableList<MutableList<Int>> {
        val result = mutableListOf<MutableList<Int>>()
        for (i in 0 until set.size) {
            for (j in i until set.size) {
                result.add(set.subList(i, j+1))
            }
        }
        return result
    }

    fun isAdmissibleOverpayment(prices: MutableList<Double>, notes: MutableList<String>, x: Double): Boolean {
        val overpay = prices.mapIndexed { index, price ->
            if (notes[index] == "Same as in-store") {
                return@mapIndexed 0.0
            } else if (notes[index].contains("lower than in-store")) {
                val percent = notes[index].replace("% lower than in-store", "").toDouble() / 100.0
                val instore = price / (1 - percent) * 1.0
                return@mapIndexed price - instore
            } else {
                val percent = notes[index].replace("% higher than in-store", "").toDouble() / 100.0
                val instore = price / (1 + percent)
                return@mapIndexed price - instore
            }
        }
        return overpay.sum() - x < 1e-8
    }

    suspend fun massiveRun(action: suspend () -> Unit): Long {
        val n = 100
        val k = 100
        val time = measureTimeMillis {
            coroutineScope {
                repeat(n) {
                    launch {
                        repeat(k) { action() }
                    }
                }
            }
        }
        return time
    }

}