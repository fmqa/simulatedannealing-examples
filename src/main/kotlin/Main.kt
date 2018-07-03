/*
 * Graphical simulated annealing demo.
 *
 * Re-implementation of the original demo by Project Nayuki (https://www.nayuki.io/page/simulated-annealing-demo).
 */

import org.kochab.simulatedannealing.ExponentialDecayScheduler
import org.kochab.simulatedannealing.Solver
import org.kochab.simulatedannealing.MinimumListener
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import javax.imageio.ImageIO

object SAImage {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Configurable parameters.
        val width = System.getenv("SA_WIDTH").let { if (it == null) 256 else it.toInt() }
        val height = System.getenv("SA_HEIGHT").let { if (it == null) 256 else it.toInt() }
        val iterations = System.getenv("SA_ITER").let { if (it == null) 10000000L else it.toLong() }
        val temp = System.getenv("SA_TEMP").let { if (it == null) 100.0 else it.toDouble() }
        val output = System.getenv("SA_OUTPUT")
        val rng = SecureRandom()

        // Run the heavy computation.
        val problem = PixelProblem(width, height, rng)

        problem.callback = { n, e, retry ->
            if (n % 1000 == 0) {
                if (retry) {
                    println("RETRY\t#$n\tE=$e")
                } else {
                    println("MOVE\t#$n\tE=$e")
                }
            }
        }

        val outfile = File(output ?: String.format("simulated-annealing-time%d-iters%d-starttemp%.1f.bmp",
                System.currentTimeMillis(), iterations, temp))

        val solver = Solver(problem, ExponentialDecayScheduler(temp, iterations), rng, MinimumListener { _, n, _, e ->
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            image.setRGB(0, 0, width, height, problem.data, 0, width)
            ImageIO.write(image, "bmp", outfile)
            println("MIN\t#$n\tE=$e")
        })

        solver.solve()
    }

}

