import org.kochab.simulatedannealing.Problem
import org.kochab.simulatedannealing.SearchState
import java.util.*
import kotlin.math.abs

typealias PixelProblemCallback = (n: Int, e: Double, retry: Boolean) -> Unit

/**
 * Adjacent pixel difference optimization problem.
 *
 * Partially based on https://www.nayuki.io/page/simulated-annealing-demo ("Simulated annealing demo" by Project Nayuki)
 */
class PixelProblem(val width: Int, val height: Int, val random: Random, var callback: PixelProblemCallback? = null) : Problem<PixelProblem.UndoableState> {
    /**
     * Current iteration.
     */
    var number = 0

    /**
     * Swap mode (true: free, false: adjacent).
     */
    var mode = false

    /**
     * State class that can rollback changes done to the pixel matrix.
     *
     * This class reverses the previous perturbation before applying a new one.
     */
    abstract inner class UndoableState: SearchState<UndoableState> {
        /**
         * Indicates whether this state has been visited or not.
         */
        private var visited = false

        /**
         * The level of energy before perturbation.
         */
        val energy = energy()

        /**
         * Swaps two pixels.
         */
        protected abstract fun swap()

        /**
         * Regenerates random variables.
         */
        protected abstract fun roll()

        /**
         * Returns the next state.
         *
         * @return The next state.
         */
        protected abstract fun next(): UndoableState

        override fun step(): UndoableState {
            if (visited) {
                // Rollback changes if this state was already visited.
                swap()
                // Generate new RNG values.
                roll()
            }
            swap()

            callback?.let {
                it(number, energy, visited)
            }
            number++

            // Set the visited state to true. If this state is visited again,
            // the random variables will be re-rolled, thus ensuring that a different
            // successor state is generated.
            visited = true

            return next()
        }
    }

    /**
     * State class that swaps arbitrary pixels.
     */
    inner class FreeSwapState : UndoableState() {
        /**
         * Random pixel X-coordinate.
         */
        var x1 = random.nextInt(width)

        /**
         * Random pixel Y-coordinate.
         */
        var y1 = random.nextInt(height)

        /**
         * Random pixel X-coordinate.
         */
        var x0 = random.nextInt(width)

        /**
         * Random pixel Y-coordinate.
         */
        var y0 = random.nextInt(height)

        /**
         * Swap pixel (x0, y0) with (x1, y1).
         */
        override fun swap() {
            val i = y0 * width + x0
            val j = y1 * width + x1
            with(data[i]) {
                data[i] = data[j]
                data[j] = this
            }
        }

        /**
         * Regenerate random variables.
         */
        override fun roll() {
            x0 = random.nextInt(width)
            y0 = random.nextInt(height)
            x1 = random.nextInt(width)
            y1 = random.nextInt(height)
        }

        override fun next() = FreeSwapState()
    }

    /**
     * State class that swaps neighboring pixels.
     */
    inner class AdjacentSwapState : UndoableState() {
        /**
         * Random horizontal translation offset (-1: Left, 1: Right).
         */
        var x = 1 + random.nextInt(width - 2)

        /**
         * Random vertical translation offset (-1: Top, 1: Bottom).
         */
        var y = 1 + random.nextInt(height - 2)

        /**
         * Random X-translation (-1/left or +1/right)
         */
        var dx = if (random.nextBoolean()) -1 else 1

        /**
         * Random Y-translation (-1/up or +1/down).
         */
        var dy = if (random.nextBoolean()) -1 else 1

        /**
         * Swap pixel (x, y) with (x + dx, y + dy).
         */
        override fun swap() {
            val i = y * width + x
            val j = (y + dy) * width + x + dx
            with(data[i]) {
                data[i] = data[j]
                data[j] = this
            }
        }

        /**
         * Regenerate random variables.
         */
        override fun roll() {
            x = 1 + random.nextInt(width - 2)
            y = 1 + random.nextInt(height -2 )
            dx = if (random.nextBoolean()) -1 else 1
            dy = if (random.nextBoolean()) -1 else 1
        }

        override fun next() = AdjacentSwapState()
    }

    /**
     * Flattened pixel data array.
     */
    val data = IntArray(width * height) {
        random.nextInt(0xffffff)
    }

    override fun energy(searchState: UndoableState) = searchState.energy

    override fun initialState() = if (mode) FreeSwapState() else AdjacentSwapState()

    /**
     * Computes the sum of absolute differences of the RGB components of two 24-bit RGB triples.
     *
     * @param p0 The first 24-bit RGB pixel.
     * @param p1 The seoncd 24-bit RGB pixel.
     * @return Sum of absolute RGB component differences between p0 and p1.
     */
    private fun diff(p0: Int, p1: Int): Int {
        val r0 = p0.ushr(16)
        val g0 = p0.ushr(8) and 0xff
        val b0 = p0 and 0xff
        val r1 = p1.ushr(16)
        val g1 = p1.ushr(8) and 0xff
        val b1 = p1 and 0xff
        return abs(r0 - r1) + abs(g0 - g1) + abs(b0 - b1)
    }

    /**
     * Computes the sum of absolute differences between the two pixels at (x0, y0) and (x1, y1).
     *
     * @param x0 The X-coordinate of the first pixel.
     * @param y0 The Y-coordinate of the first pixel.
     * @param x1 The X-coordinate of the second pixel.
     * @param y1 The Y-coordinate of the second pixel.
     */
    private fun diff(x0: Int, y0: Int, x1: Int, y1: Int) = diff(data[y0 * width + x0], data[y1 * width + x1])

    /**
     * Calculates the total sum of absolute differences between all adjacent pixels in the pixel matrix.
     *
     * @return The aggregate sum of adjacent absolute differences for all pixels.
     */
    private fun energy(): Double {
        var sum = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (x > 0) sum += diff(x, y, x - 1, y)
                if (y > 0) sum += diff(x, y, x, y - 1)
            }
        }
        return sum.toDouble()
    }
}
