import kotlin.random.Random

class Grid(val grid: IntArray, val size: Int, val rowAccessRule: (Int) -> Int) {
    fun access(row: Int, col: Int): Int {
        return grid[rowAccessRule(row) * size + col]
    }

    fun indexOnRow(row: Int, find: Int): Int {
        for (i in 0 until size) {
            if (grid[rowAccessRule(row) * size + i] == find) return i
        }

        return -1
    }

    /* generation */
    companion object {
        fun generateGrid(size: Int, rowAccessRule: (Int) -> Int): Grid {
            val grid: IntArray = generateBase(size)

            shuffleSwaps(size, grid)
            shuffleSlices(size, grid)
            sort(size, grid)

            return Grid(grid, size, rowAccessRule)
        }

        private fun plainIndexOnRow(grid: IntArray, size: Int, row: Int, find: Int): Int {
            for (i in 0 until size) {
                if (grid[row * size + i] == find) return i
            }

            return -1
        }

        private fun generateBase(size: Int): IntArray {
            return IntArray(size * size) { i ->
                val row = i / size
                val col = i % size
                (col + row) % size
            }
        }

        private fun findRow(grid: IntArray, size: Int, avoid: Int, col: Int, find: Int): Int {
            for (row in 0 until size) {
                if (row != avoid && grid[row * size + col] == find) {
                    return row
                }
            }

            return -1
        }

        private fun shuffleSwaps(size: Int, grid: IntArray) {
            for (first in 0 until size) {
                val second = Random.nextInt(size)
                var currentRow = 0

                while (currentRow != -1) {
                    val firstIndex = plainIndexOnRow(grid, size, currentRow, first)
                    val secondIndex = plainIndexOnRow(grid, size, currentRow, second)

                    grid[currentRow * size + firstIndex] = second
                    grid[currentRow * size + secondIndex] = first

                    currentRow = findRow(grid, size, currentRow, secondIndex, first)
                }
            }
        }

        private fun shuffleSlices(size: Int, grid: IntArray) {
            for (col0 in 0 until size) {
                var col1 = Random.nextInt(size)
                if (col0 == col1) col1 = (col1 + 1) % size

                for (j in 0 until size) {
                    val temp = grid[j * size + col0]
                    grid[j * size + col0] = grid[j * size + col1]
                    grid[j * size + col1] = temp
                }
            }

            for (row0 in 0 until size) {
                var row1 = Random.nextInt(size)
                if (row0 == row1) row1 = (row1 + 1) % size

                for (i in 0 until size) {
                    val temp = grid[row0 * size + i]
                    grid[row0 * size + i] = grid[row1 * size + i]
                    grid[row1 * size + i] = temp
                }
            }
        }

        private fun sort(size: Int, grid: IntArray) {
            val replaces = Array(grid.size) { 0 }
            for (i in 0 until size) replaces[grid[i]] = i

            for (i in grid.indices) {
                grid[i] = replaces[grid[i]]
            }
        }
    }
}
