/*
 * Simulated annealing on image demo (Java)
 *
 * Copyright (c) 2017 Project Nayuki
 * All rights reserved. Contact Nayuki for licensing.
 * https://www.nayuki.io/page/simulated-annealing-demo
 */

import org.kochab.simulatedannealing.ExponentialDecayScheduler
import org.kochab.simulatedannealing.Solver
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import javax.imageio.ImageIO

object SimulatedAnnealingOnImage {
    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        // Configurable parameters
        val WIDTH = 256
        val HEIGHT = 256
        val NUM_ITERATIONS = 1000000000000
        val START_TEMPERATURE = 100.0
        val rng = SecureRandom()

        // Run the heavy computation
        val problem = PixelProblem(WIDTH, HEIGHT, rng) {
            n, e, retry -> if (n % 1000 == 0) println("#$n\tE=$e\tRETRY=$retry")
        }

        val solver = Solver(problem, ExponentialDecayScheduler(START_TEMPERATURE, NUM_ITERATIONS), rng)
        val result = solver.solve()

        // Write output image
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, WIDTH, HEIGHT, problem.data, 0, WIDTH)
        val outputFilename = String.format("simulated-annealing-time%d-iters%d-starttemp%.1f.bmp",
                System.currentTimeMillis(), NUM_ITERATIONS, START_TEMPERATURE)
        ImageIO.write(image, "bmp", File(outputFilename))
    }

}

