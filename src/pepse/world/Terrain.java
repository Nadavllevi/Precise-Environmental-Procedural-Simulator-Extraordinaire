package pepse.world;

import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import pepse.utils.ColorSupplier;
import pepse.utils.NoiseGenerator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Terrain Class.
 * Creates the game's floor, using blocks.
 * Uses Perlin Noise algorithm to create a random-smooth terrain height.
 * Tags differently first and second row of blocks from other blocks,
 * due to their importance in stopping intersections.
 * @author Nadav Levi.
 */
public class Terrain {
    /**
     * GROUND_AT_X0_FACTOR constant in use for sun movement.
     */
    public static final float GROUND_AT_X0_FACTOR = 0.75f;
    /**
     * TOP_GROUND_TAG constant marks the first layer of blocks with special flag.
     */
    public static final String TOP_GROUND_TAG = "top_ground";
    /**
     * MID_GROUND_TAG constant marks the second layer of blocks with special flag.
     */
    public static final String MID_GROUND_TAG = "mid_ground";
    /**
     * REGULAR_GROUND_TAG constant marks the third and next layers of blocks with special flag.
     */
    public static final String REGULAR_GROUND_TAG = "ground";
    private static final int TERRAIN_DEPTH = 20;
    private static final int NOISE_FACTOR = 6;
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);


    private final float groundHeightAtX0;
    private final NoiseGenerator noiseGenerator;

    /**
     * Constructor for Terrain.
     * Sets the height of the first blocks column according to the window dimensions.
     * The first column height is used as a relative point that the noise will be generated from.
     * Initialize the NoiseGenerator for generating other columns' height.
     * @param windowDimensions the dimensions of the window.
     * @param seed this seed is the basis of the random noise generator.
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        this.groundHeightAtX0 = windowDimensions.y() * GROUND_AT_X0_FACTOR;
        this.noiseGenerator = new NoiseGenerator(seed, (int) this.groundHeightAtX0);
    }

    /**
     * Calculates the ground height for a given x coordinate in the screen.
     * Uses the NoiseGenerator to generate pseudo random noise.
     * @param x the x coordinate of the terrain to calculate height for.
     * @return the calculated ground height in this x coordinate.
     */
    public float groundHeightAt(float x) {
        // if x is zero, don't need noise.
        if (x == 0f){
            return this.groundHeightAtX0;
        }
        // add noise to the height.
        else {
            float noise = (float) this.noiseGenerator.noise(x, Block.SIZE * NOISE_FACTOR);
            return this.groundHeightAtX0 + noise;
        }
    }

    /**
     * Creates terrain in a given range.
     * This function is contracted to place blocks at multiples of block size,
     * and to fill at least the given range with blocks.
     * @param minX the minimum x coordinate in the range.
     * @param maxX the maximum x coordinate in the range.
     * @return list of generated blocks representing terrain in the given range.
     */
    public List<Block> createInRange(int minX, int maxX) {
        // match a multiple of block size.
        minX = (int) (Math.floor((float) minX / Block.SIZE) * Block.SIZE);
        maxX = (int) (Math.ceil((float) maxX / Block.SIZE) * Block.SIZE);
        int blocksInRow = (maxX - minX) / Block.SIZE;

        List<Block> blocksList = new ArrayList<>(blocksInRow * TERRAIN_DEPTH);
        for (int i = 0; i < TERRAIN_DEPTH; i++) {
            for(int j = 0; j < blocksInRow; j++) {
                int currentXpos = minX + (j*Block.SIZE);
                int currentYpos =
                        (int) (Math.floor(this.groundHeightAt(currentXpos) / Block.SIZE) * Block.SIZE)
                                + (i*Block.SIZE);
                Vector2 position = new Vector2(
                        currentXpos,
                        currentYpos
                );
                Block block = new Block(
                        position,
                        new RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR)));
                // differentiate terrain rows to stop intersections and reduce complexity.
                if (i == 0) {
                    block.setTag(TOP_GROUND_TAG);
                } else if (i == 1) {
                    block.setTag(MID_GROUND_TAG);
                } else {
                    block.setTag(REGULAR_GROUND_TAG);
                }
                blocksList.add(block);
            }
        }
        return blocksList;
    }
}
