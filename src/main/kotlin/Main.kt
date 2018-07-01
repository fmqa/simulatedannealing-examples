/*
 * Graphical simulated annealing demo.
 *
 * Re-implementation of the original demo by Project Nayuki (https://www.nayuki.io/page/simulated-annealing-demo).
 */

import org.kochab.simulatedannealing.ExponentialDecayScheduler
import org.kochab.simulatedannealing.Solver
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import javax.imageio.ImageIO

object SAImage {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Configurable parameters
        val width = System.getenv("SA_WIDTH").let { if (it == null) 256 else it.toInt() }
        val height = System.getenv("SA_HEIGHT").let { if (it == null) 256 else it.toInt() }
        val iterations = System.getenv("SA_ITER").let { if (it == null) 10000000L else it.toLong() }
        val temp = System.getenv("SA_TEMP").let { if (it == null) 100.0 else it.toDouble() }
        val progress = System.getenv("SA_PROGRESS")
        val output = System.getenv("SA_OUTPUT") ?: progress
        val rng = SecureRandom()

        // Run the heavy computation
        val problem = PixelProblem(width, height, rng)

        problem.callback = { n, e, retry ->
            if (n % 1000 == 0) {
                println("#$n\tE=$e\tRETRY=$retry")
                if (progress != null) {
                    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                    image.setRGB(0, 0, width, height, problem.data, 0, width)
                    ImageIO.write(image, "bmp", File(progress))
                }
            }
        }

        val solver = Solver(problem, ExponentialDecayScheduler(temp, iterations), rng)
        val result = solver.solve()

        // Write output image
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, width, height, problem.data, 0, width)

        val outfile = File(output ?: String.format("simulated-annealing-time%d-iters%d-starttemp%.1f.bmp",
                System.currentTimeMillis(), iterations, temp))
        ImageIO.write(image, "bmp", outfile)
    }

}

