/*
 * Graphical simulated annealing demo.
 *
 * Re-implementation of the original demo by Project Nayuki (https://www.nayuki.io/page/simulated-annealing-demo).
 */

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.kochab.simulatedannealing.ExponentialDecayScheduler
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
        val problem = PixelProblem(width, height, rng, mode)

        val scheduler = ExponentialDecayScheduler(temperature, iterations)

        val outpath = (output
                ?: Paths.get("simulated-annealing-iters${iterations}-starttemp${temperature}.bmp")).toAbsolutePath()

        val solver = Solver(problem, scheduler, problem.initialState(), rng);

        for (candidate in solver) {
            if (candidate.isMinimum) {
                val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                image.setRGB(0, 0, width, height, problem.data, 0, width)
                Files.createTempFile(outpath.parent, "simulated-annealing", "-${candidate.iteration}").run {
                    try {
                        ImageIO.write(image, "bmp", toFile())
                        Files.move(this, outpath, StandardCopyOption.ATOMIC_MOVE)
                    } catch (e: Exception) {
                        throw e
                    } finally {
                        Files.deleteIfExists(this)
                    }
                }
                println("MIN\t#${candidate.iteration}\tE=${candidate.state.energy}")
            } else if (candidate.iteration % 1000 == 0L) {
                if (candidate.state.visited) {
                    println("RETRY\t#${candidate.iteration}\tE=${candidate.state.energy}")
                } else {
                    println("MOVE\t#${candidate.iteration}\tE=${candidate.state.energy}")
                }
            }
        }
    }
}

