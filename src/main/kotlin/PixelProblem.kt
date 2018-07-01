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
     * State class that can rollback changes done to the pixel matrix.
     *
     * This class reverses the previous perturbation before applying a new one.
     */
    inner class UndoableState : SearchState<UndoableState> {
        /**
         * Energy of this state (before perturbation).
         */
        val energy = energy()

        /**
         * Random horizontal translation offset (-1: Left, 1: Right).
         */
        var dx = if (random.nextBoolean()) 1 else -1

        /**
         * Random vertical translation offset (-1: Top, 1: Bottom).
         */
        var dy = if (random.nextBoolean()) 1 else -1

        /**
         * Random pixel X-coordinate.
         */
        var x = 1 + random.nextInt(width - 2)

        /**
         * Random pixel Y-coordinate.
         */
        var y = 1 + random.nextInt(height - 2)

        /**
         * Indicates whether this state has already been visited.
         */
        var done = false

        /**
         * Swap pixel (x, y) with (x + dx, y + dy).
         */
        private fun swap() {
            with(data[y * width + x]) {
                data[y * width + x] = data[(y + dy) * width + x + dx]
                data[(y + dy) * width + x + dx] = this
            }
        }

        /**
         * Regenerate random variables.
         */
        private fun roll() {
            dx = if (random.nextBoolean()) 1 else -1
            dy = if (random.nextBoolean()) 1 else -1
            x = 1 + random.nextInt(width - 2)
            y = 1 + random.nextInt(height - 2)
        }

        override fun step(): UndoableState {
            if (done) {
                // Rollback changes if this state was already visited.
                swap()
                // Generate new RNG values.
                roll()
            }
            swap()

            callback?.let {
                it(number, energy, done)
            }
            number++

            done = true
            return UndoableState()
        }
    }

    /**
     * Flattened pixel data array.
     */
    val data = IntArray(width * height) {
        random.nextInt(0xffffff)
    }

    override fun energy(searchState: UndoableState) = searchState.energy

    override fun initialState() = UndoableState()

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
