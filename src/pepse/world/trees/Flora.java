package pepse.world.trees;

import danogl.util.Vector2;
import pepse.world.Block;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Flora class.
 * Creates trees randomly in different coordinates.
 * Chooses random tree height within range.
 * Guarantees that the same random flora is generated for the same coordinates.
 * @author Nadav Levi.
 */
public class Flora {
    private static final int TREE_MIN_HEIGHT = 200;
    private static final int TREE_MAX_HEIGHT = 300;
    private static final int TREE_WIDTH = 200;
    private static final double TREE_CHANCE = 0.08;

    private final Function<Float, Float> groundHeight;
    private final double randomSeed;
    private final Runnable fruitAddEnergy;

    /**
     * Constructor for Flora.
     * Gets function reference that receives x coordinate of a tree,
     * and returns the ground height for placing the tree.
     * Saves callback to be used by Fruits objects, to add energy.
     * Saves given seed to generate the same random trees for each x coordinate.
     * @param groundHeight the function reference for getting ground height.
     * @param fruitAddEnergy the callback that will be called by fruits.
     * @param randomSeed the random seed for generating same random numbers.
     */
    public Flora(Function<Float, Float> groundHeight, Runnable fruitAddEnergy, double randomSeed) {
        this.groundHeight = groundHeight;
        this.fruitAddEnergy = fruitAddEnergy;
        this.randomSeed = randomSeed;
    }

    /**
     * Creates flora in a given range.
     * This function is contracted place random trees at multiples of block size (so it will match ground),
     * and to fill at least the given range with blocks.
     * If tree is placed at certain coordinate, it will always place the same tree there.
     * @param minX the minimum x coordinate in the range.
     * @param maxX the maximum x coordinate in the range.
     * @return list of generated trees representing flora in the given range.
     */
    public List<Tree> createInRange(int minX, int maxX) {
        // match a multiple of block size.
        minX = (int) (Math.floor((float) minX / Block.SIZE) * Block.SIZE);
        maxX = (int) (Math.ceil((float) maxX / Block.SIZE) * Block.SIZE);
        int blocksInRow = (maxX - minX) / Block.SIZE;

        List<Tree> trees = new LinkedList<>();
        for (int i = 0; i < blocksInRow; i++) {
            // get current coordinate for initializing a matching random object.
            int downX = minX + (i*Block.SIZE);
            Random random = new Random(Objects.hash(downX, this.randomSeed));

            double result = random.nextDouble();
            if (result < TREE_CHANCE) {
                float downY = groundHeight.apply((float) downX);
                int treeHeight = random.nextInt(TREE_MIN_HEIGHT, TREE_MAX_HEIGHT);
                trees.add(new Tree(
                   new Vector2(downX, downY),
                        treeHeight,
                        TREE_WIDTH,
                        random,
                        this.fruitAddEnergy
                ));
            }
        }
        return trees;
    }
}