/*
 * Graphical simulated annealing demo.
 *
 * Re-implementation of the original demo by Project Nayuki (nayuki.io).
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
        val width = 256
        val height = 256
        val iterations = 3000000L
        val temp = 100.0
        val rng = SecureRandom()

        // Run the heavy computation
        val problem = PixelProblem(width, height, rng) {
            n, e, retry -> if (n % 1000 == 0) println("#$n\tE=$e\tRETRY=$retry")
        }

        val solver = Solver(problem, ExponentialDecayScheduler(temp, iterations), rng)
        val result = solver.solve()

        // Write output image
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, width, height, problem.data, 0, width)
        val outputFilename = String.format("simulated-annealing-time%d-iters%d-starttemp%.1f.bmp",
                System.currentTimeMillis(), iterations, temp)
        ImageIO.write(image, "bmp", File(outputFilename))
    }

}

