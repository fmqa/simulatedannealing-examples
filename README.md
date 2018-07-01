# Simulated Annealing Demo

This demo uses the [simulated annealing library](https://github.com/kochab/simulatedannealing) to reorder a 256x256 RGB bitmap containing noise so as to minimize the adjacent difference between individual pixels.

See [Project Nayuki's post on simulated annealing](https://www.nayuki.io/page/simulated-annealing-demo) for an explanation of the optimization problem. This is a Kotlin re-implementation of the demo, using the simulated annealing library.

## Sample output

![64x64 Image with 500000 iterations and a starting temperature of 100](example-output/simulated-annealing-time1530375735368-iters500000-starttemp100,0.bmp?raw=true "64x64 Image with 500000 iterations and a starting temperature of 100")

The above 64x64 image is produced with 500000 iterations and a starting temperature of 100.
