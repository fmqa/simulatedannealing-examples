/*
 * Graphical simulated annealing demo.
 *
 * Re-implementation of the original demo by Project Nayuki (https://www.nayuki.io/page/simulated-annealing-demo).
 */

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.kochab.simulatedannealing.ExponentialDecayScheduler
import org.kochab.simulatedannealing.MinimumListener
import org.kochab.simulatedannealing.Solver
import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.SecureRandom
import java.util.*
import javax.imageio.ImageIO

class AnnealingArgs(parser: ArgParser) {
    val width by parser.storing("--width", help = "Image width.") {
        toInt()
    }.default(256)
    val height by parser.storing("--height", help = "Image height.") {
        toInt()
    }.default(256)
    val iterations by parser.storing("-i", "--iter", help = "Iteration count.") {
        toLong()
    }.default(10000000L)
    val temperature by parser.storing("-t", "--temperature", help = "Initial temperature.") {
        toDouble()
    }.default(100.0)
    val output by parser.storing("-o", "--output", help = "Output file.") {
        Paths.get(this)
    }.default<Path?>(null)
    val mode by parser.flagging("--free", help = "High DoF mode.")
    val secure by parser.flagging("--secure", help = "Use cryptographic RNG.")
}

@Throws(IOException::class)
fun main(args: Array<String>): Unit = mainBody {
    val parser = ArgParser(args)
    parser.parseInto(::AnnealingArgs).run {
        val rng = if (secure) SecureRandom() else Random()
        val problem = PixelProblem(width, height, rng) { n, e, retry ->
            if (n % 1000 == 0) {
                if (retry) {
                    println("RETRY\t#$n\tE=$e")
                } else {
                    println("MOVE\t#$n\tE=$e")
                }
            }
        }
        problem.mode = mode

        val scheduler = ExponentialDecayScheduler(temperature, iterations)

        val outpath = output ?: Paths.get("simulated-annealing-iters${iterations}-starttemp${temperature}.bmp")

        val solver = Solver(problem, scheduler, rng, MinimumListener { _, n, state ->
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            image.setRGB(0, 0, width, height, problem.data, 0, width)
            Files.createTempFile("simulated-annealing", "-$n").run {
                try {
                    ImageIO.write(image, "bmp", toFile())
                    Files.move(this, outpath, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: Exception) {
                    Files.delete(this)
                    throw e
                }
            }
            val e = problem.energy(state)
            println("MIN\t#$n\tE=$e")
        })

        solver.solve()
    }
}

