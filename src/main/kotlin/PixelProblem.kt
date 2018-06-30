import org.kochab.simulatedannealing.Problem
import org.kochab.simulatedannealing.SearchState
import java.util.*
import kotlin.math.abs

typealias PixelProblemCallback = (n: Int, e: Double, retry: Boolean) -> Unit

class PixelProblem(val width: Int, val height: Int, val random: Random, val callback: PixelProblemCallback?) : Problem<PixelProblem.UndoableState> {
    var number = 0

    inner class UndoableState : SearchState<UndoableState> {
        val energy = energy()
        var dx = if (random.nextBoolean()) 1 else -1
        var dy = if (random.nextBoolean()) 1 else -1
        var x = 1 + random.nextInt(width - 2)
        var y = 1 + random.nextInt(height - 2)
        var done = false

        private fun swap() {
            with(data[y * width + x]) {
                data[y * width + x] = data[(y + dy) * width + x + dx]
                data[(y + dy) * width + x + dx] = this
            }
        }

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

    val data = IntArray(width * height) {
        random.nextInt(0xffffff)
    }

    override fun energy(searchState: UndoableState) = searchState.energy

    override fun initialState() = UndoableState()

    private fun diff(p0: Int, p1: Int): Int {
        val r0 = p0.ushr(16)
        val g0 = p0.ushr(8) and 0xff
        val b0 = p0 and 0xff
        val r1 = p1.ushr(16)
        val g1 = p1.ushr(8) and 0xff
        val b1 = p1 and 0xff
        return abs(r0 - r1) + abs(g0 - g1) + abs(b0 - b1)
    }

    private fun diff(x0: Int, y0: Int, x1: Int, y1: Int) = diff(data[y0 * width + x0], data[y1 * width + x1])

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