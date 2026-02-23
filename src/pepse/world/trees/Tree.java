package pepse.world.trees;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import pepse.world.Block;

import java.awt.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;

/**
 * Tree Class.
 * Represents a tree in the world.
 * Contains List of leaves GameObjects representing the tree's leaves.
 * Each leaf have a transition to simulate wind.
 * Contains List of fruits GameObjects representing the tree's fruits.
 * Each tree is assigned with random set of leaves and fruits.
 * The random object is predefined according to the tree location,
 * for recrating identical tree at a certain location.
 * @author Nadav Levi.
 */
public class Tree extends GameObject {
    /**
     * LEAF_SIZE constant in use of Leaf and Tree.
     */
    public static final int LEAF_SIZE = 20;

    private static final int PADDING = 70;
    private static final Color TRUNK_COLOR = new Color(100,50,20);
    private static final int LEAVES_HEIGHT_ORIGIN = 70;
    private static final double LEAF_CHANCE = 0.6;
    private static final double FRUIT_CHANCE = 0.05;
    private static final int WAIT_TIME_LEAVES = 60;

    private final Vector2 topLeftTree;
    private final Random random;

    private List<Leaf> leaves;
    private List<Fruit> fruits;

    /**
     * Constructor for Tree.
     * Creates a new tree at a given location, with random leaves and fruits.
     * Sets physics to prevent intersections with avatar, if intersected, the tree will not move.
     * Sets the renderable for the trunk, while saving space for leaves.
     * Calculates the area for the leaves and fruits.
     * Saves the random for leaves and fruit creations.
     * Adds energy to the avatar when it is catching a fruit.
     * @param downLeftPos the down left position for the trunk placement.
     * @param height the height of the tree.
     * @param width the width of the tree.
     * @param random the given random for this tree.
     * @param addEnergy the method reference for adding energy when avatar is catching fruit.
     */
    public Tree(Vector2 downLeftPos, float height, float width, Random random, Runnable addEnergy){
        super(
                new Vector2(downLeftPos.x(), downLeftPos.y()-(height-PADDING)), // padding for leaves.
                new Vector2(Block.SIZE, height - PADDING), // dimensions of trunk.
                new RectangleRenderable(TRUNK_COLOR)
        );
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
        // top left coordinates of the tree.
        float topLeftTreeX = downLeftPos.x() - ((width - Block.SIZE) / 2);
        float topLeftTreeY = downLeftPos.y() - height;
        this.topLeftTree = new Vector2(topLeftTreeX, topLeftTreeY);
        this.random = random;
        // create leaves and saves its grid.
        int[] leavesDims = this.createLeaves(width, height);
        // match fruits to leaves.
        this.createFruits(leavesDims, addEnergy);

    }

    /**
     * Create random grid of leaves.
     * The grid starts at the topLeftTree,
     * spans over a given width, and random height - with given limit.
     * (this height has constant minimum to prevent tree with no leaves)
     * Leaves in the grid are chosen randomly.
     * Each leaf has ScheduledTask with random wait time, to start its transition.
     * @param width the width of the leaves grid.
     * @param maxHeight the height limit for the leaves grid.
     * @return the grid dimensions, represented as {leaves in row, leaves in column}.
     */
    private int[] createLeaves(float width, float maxHeight) {
        // random height of leaves grid.
        float leavesHeight = random.nextFloat(LEAVES_HEIGHT_ORIGIN, maxHeight-LEAVES_HEIGHT_ORIGIN);
        int gridRows = (int) (leavesHeight / LEAF_SIZE);
        int gridCols = (int) (width / LEAF_SIZE);
        this.leaves = new LinkedList<>();
        // fill grid with leaves randomly.
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridCols; j++) {
                double placeLeaf = random.nextDouble();
                if (placeLeaf < LEAF_CHANCE) {
                    // calculate leaf location.
                    float topLeftX = this.topLeftTree.x() + (j * LEAF_SIZE);
                    float topLeftY = this.topLeftTree.y() + (i * LEAF_SIZE);
                    Leaf leaf = new Leaf(new Vector2(topLeftX, topLeftY));
                    leaves.add(leaf);
                    // random wait time for transition.
                    float waitTime = random.nextFloat(WAIT_TIME_LEAVES);
                    new ScheduledTask(
                            leaf,
                            waitTime,
                            false, // transition is back and forth.
                            leaf::windTransition
                    );
                }
            }
        }
        return new int[]{gridRows, gridCols};
    }

    /**
     * Places fruits randomly in the leaves grid.
     * Chooses randomly between apples and oranges.
     * @param gridLeaves the leaves grid dimensions, represented as {leaves in row, leaves in column}.
     * @param addEnergy the method reference to call when fruit is being caught.
     */
    private void createFruits(int[] gridLeaves, Runnable addEnergy) {
        this.fruits = new LinkedList<>();
        for (int i = 0; i < gridLeaves[0]; i++) {
            for (int j = 0; j < gridLeaves[1]; j++) {
                double placeFruit = random.nextDouble();
                if (placeFruit < FRUIT_CHANCE){
                    // calculate fruit location.
                    float topLeftX = this.topLeftTree.x() + (j * LEAF_SIZE);
                    float topLeftY = this.topLeftTree.y() + (i * LEAF_SIZE);
                    // choose apple or orange.
                    boolean isOrange = random.nextBoolean();
                    Color fruitColor;
                    if (isOrange){
                        fruitColor = Color.orange;
                    }
                    else{
                        fruitColor = Color.red;
                    }
                    Fruit fruit = new Fruit(
                            new Vector2(topLeftX, topLeftY),
                            fruitColor,
                            addEnergy
                    );
                    this.fruits.add(fruit);
                }
            }
        }
    }

    /**
     * Getter for the leaves list.
     * Used by the GameManager to add all tree's leaves.
     * @return the list of this tree's leaves.
     */
    public List<Leaf> getLeaves() {
        return this.leaves;
    }

    /**
     * Getter for the fruits list.
     * Used by the GameManager to add all tree's fruits.
     * @return the list of this tree's fruits.
     */
    public List<Fruit> getFruits(){
        return this.fruits;
    }
}
