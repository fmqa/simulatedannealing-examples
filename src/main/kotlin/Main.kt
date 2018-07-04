/*
 * Graphical simulated annealing demo.
 *
 * Re-implementation of the original demo by Project Nayuki (https://www.nayuki.io/page/simulated-annealing-demo).
 */

import org.kochab.simulatedannealing.ExponentialDecayScheduler
import org.kochab.simulatedannealing.MinimumListener
import org.kochab.simulatedannealing.Solver
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
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
        val mode = System.getenv("SA_MODE")
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

        val dof = when (mode) {
            "free" -> true
            "adjacent" -> false
            else -> false
        }

        problem.mode = dof

        val outfile = Paths.get(output ?: String.format("simulated-annealing-time%d-iters%d-starttemp%.1f.bmp",
                System.currentTimeMillis(), iterations, temp))

        val solver = Solver(problem, ExponentialDecayScheduler(temp, iterations), rng, MinimumListener { _, n, state ->
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            image.setRGB(0, 0, width, height, problem.data, 0, width)
            Files.createTempFile("simulated-annealing", "-$n").run {
                try {
                    ImageIO.write(image, "bmp", toFile())
                    Files.move(this, outfile, StandardCopyOption.ATOMIC_MOVE)
                } catch (e: Exception) {
                    Files.delete(this)
                }
            }
            val e = problem.energy(state)
            println("MIN\t#$n\tE=$e")
        })

        solver.solve()
    }

}

