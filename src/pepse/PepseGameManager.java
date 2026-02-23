package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import pepse.world.Block;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.avatar.Avatar;
import pepse.world.avatar.EnergyDisplay;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Flora;
import pepse.world.trees.Fruit;
import pepse.world.trees.Leaf;
import pepse.world.trees.Tree;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * PepseGameManager Class.
 * Main game manager for the Precise Environmental Procedural Simulator Extraordinaire.
 * Manages world initialization, world loop updates, and serves as the sole authority
 * for modifying the world. All world objects (avatar, trees, blocks, UI, etc.)
 * are created and removed exclusively through this manager.
 * It separates different objects to layers to reduce the number of collisions.
 * Responsible for generating infinite world,
 * while removing irrelevant generated world, to reduce complexity.
 * @author Nadav Levi.
 */

public class PepseGameManager extends GameManager {
    private static final int CYCLE_LENGTH = 30;
    private static final float AVATAR_X_START = 3;
    private static final int LEAFS_LAYER = -50;
    private static final int FRUITS_LAYER = -10;
    private static final float OVER_RENDER_FACTOR = 0.8f;
    private static final int GRASS_LAYER = -25;
    private static final int GROUND_LAYER = -150;
    private static final int MID_GROUND_LAYER = -30;
    private static final float ENERGY_DISPLAY_PLACEMENT = 10f;
    private static final int TARGET_FRAMERATE = 60;

    private int minRender;
    private int maxRender;
    private Terrain terrain;
    private List<Block> ground;
    private List<Tree> trees;
    private Avatar avatar;
    private Vector2 windowDimensions;
    private Flora flora;

    /**
     * Initializes all world components and objects.
     * Creates sky, day-night transitions, sun, terrain, trees (with leaves and fruits),
     * and avatar.
     * Sets the camera to focus on the Avatar, so it will not go out of screen.
     * Sets the target frame rate to 30 FPS.
     * @param imageReader utility for reading image files.
     * @param soundReader utility for reading sound files (not in use).
     * @param inputListener listener for user input events.
     * @param windowController controller for window dimensions.
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        windowController.setTargetFramerate(TARGET_FRAMERATE);
        this.windowDimensions = windowController.getWindowDimensions();

        this.initializeSetting();
        this.initializeGround();
        this.initializeAvatar(imageReader, inputListener);
        this.initializeTrees();
        // sets camera to follow avatar.
        setCamera(new Camera(
                this.avatar,
                Vector2.ZERO,
                this.windowDimensions,
                this.windowDimensions)
        );
    }

    /**
     * Generates new world if avatar moves.
     * Calculates range for render according the new position of the avatar.
     * Creates additional world to fill all range (at the sides).
     * Calls helper method to remove out of range world.
     * @param deltaTime the time since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        // calculates new range.
        int newMinXRender = (int) (this.avatar.getCenter().x()
                - (OVER_RENDER_FACTOR * this.windowDimensions.x()));
        int newMaxXRender = (int) (this.avatar.getCenter().x()
                + (OVER_RENDER_FACTOR * this.windowDimensions.x()));

        // match a multiple of block size, so won't overlap.
        newMinXRender = (int) (Math.floor((float) newMinXRender / Block.SIZE) * Block.SIZE);
        newMaxXRender = (int) (Math.ceil((float) newMaxXRender / Block.SIZE) * Block.SIZE);

        // adding left world if needed.
        if (newMinXRender < this.minRender) {
            List<Block> leftNewGround = this.terrain.createInRange(newMinXRender, this.minRender);
            List<Tree> leftNewTrees = this.flora.createInRange(newMinXRender, this.minRender);
            this.addBlocks(leftNewGround, false);
            this.addFlora(leftNewTrees, false);
        }
        this.minRender = newMinXRender;

        // adding right world if needed.
        if (newMaxXRender > this.maxRender) {
            List<Block> rightNewGround = this.terrain.createInRange(this.maxRender + 1, newMaxXRender);
            List<Tree> rightNewTrees = this.flora.createInRange(this.maxRender + 1, newMaxXRender);
            this.addBlocks(rightNewGround, false);
            this.addFlora(rightNewTrees, false);
        }
        this.maxRender = newMaxXRender;
        // clears outdated world.
        this.cleanOutOfScreen();
    }

    /**
     * Clears world that is out of rendering range.
     * Iterates over all blocks and trees collections to find out of range ones.
     */
    private void cleanOutOfScreen() {
        // saved to remove later from ground collection.
        List<Block> blocksToRemove = new LinkedList<>();
        for (Block block : this.ground){
            if (block.getCenter().x() < this.minRender || block.getCenter().x() > this.maxRender){
                blocksToRemove.add(block);
                this.removeOrAddBlock(block, true);
            }
        }
        this.ground.removeAll(blocksToRemove);

        // saved to remove later from trees collection.
        List<Tree> treesToRemove = new LinkedList<>();
        for (Tree tree : this.trees) {
            if (tree.getCenter().x() < this.minRender || tree.getCenter().x() > this.maxRender) {
                treesToRemove.add(tree);
                this.removeOrAddTree(tree, true);
            }
        }
        this.trees.removeAll(treesToRemove);
    }

    /**
     * Adds all blocks in list to the world.
     * If not called during initialization, add blocks to ground collection.
     * @param newBlocks list of blocks to add.
     * @param isInitialize if true, doesn't add blocks to ground collection, otherwise it does.
     */
    private void addBlocks(List<Block> newBlocks, boolean isInitialize) {
        if (!isInitialize) {
            this.ground.addAll(newBlocks);
        }
        for (Block block : newBlocks) {
            this.removeOrAddBlock(block, false);
        }
    }

    /**
     * Adds all trees in list to the world.
     * If not called during initialization, add trees to trees collection.
     * @param newTrees list of trees to add.
     * @param isInitialize if true, doesn't add trees to ground collection, otherwise it does.
     */
    private void addFlora(List<Tree> newTrees, boolean isInitialize) {
        if (!isInitialize) {
            this.trees.addAll(newTrees);
        }
        for (Tree tree : newTrees) {
            this.removeOrAddTree(tree, false);
        }
    }

    /**
     * This function adds or removes the block from the GameManager.
     * @param blockToUse the given block to remove or add.
     * @param isRemove if true, it removes the block, otherwise it adds it.
     */
    private void removeOrAddBlock(Block blockToUse, boolean isRemove){
        BiConsumer<GameObject, Integer> action;
        if (isRemove){
            action = (gameObject, layer) ->
                    gameObjects().removeGameObject(gameObject, layer);
        } else {
            action = (gameObject, layer) ->
                    gameObjects().addGameObject(gameObject, layer);
        }
        String blockTag = blockToUse.getTag();
        if (blockTag != null && blockTag.equals(Terrain.TOP_GROUND_TAG)) {
            action.accept(blockToUse, GRASS_LAYER);
        } else if (blockTag != null && blockTag.equals(Terrain.MID_GROUND_TAG)) {
            action.accept(blockToUse, MID_GROUND_LAYER);
        } else if (blockTag != null && blockTag.equals(Terrain.REGULAR_GROUND_TAG)) {
            action.accept(blockToUse, GROUND_LAYER);
        }
    }

    /**
     * This function adds or removes the tree from the GameManager.
     * @param treeToUse the given tree to remove or add.
     * @param isRemove if true, it removes the tree, otherwise it adds it.
     */
    private void removeOrAddTree(Tree treeToUse, boolean isRemove){
        BiConsumer<GameObject, Integer> action;
        if (isRemove){
            action = (gameObject, layer) ->
                    gameObjects().removeGameObject(gameObject, layer);
        } else {
            action = (gameObject, layer) ->
                    gameObjects().addGameObject(gameObject, layer);
        }
        action.accept(treeToUse, Layer.STATIC_OBJECTS);
        // handle tree's leaves and fruits.
        List<Leaf> leaves = treeToUse.getLeaves();
        List<Fruit> fruits = treeToUse.getFruits();
        for (Leaf leaf : leaves) {
            action.accept(leaf, LEAFS_LAYER);
        }
        for (Fruit fruit : fruits) {
            action.accept(fruit, FRUITS_LAYER);
        }
    }

    /**
     * Creates sky, night-day transitions, and sun and sun halo.
     */
    private void initializeSetting() {
        // sky creation.
        GameObject sky = Sky.create(this.windowDimensions);
        gameObjects().addGameObject(sky, Layer.BACKGROUND);

        // night creation
        GameObject night = Night.create(this.windowDimensions, CYCLE_LENGTH);
        gameObjects().addGameObject(night, Layer.BACKGROUND);

        // sun creation.
        GameObject sun = Sun.create(this.windowDimensions, CYCLE_LENGTH);
        gameObjects().addGameObject(sun, Layer.BACKGROUND);
        GameObject sunHalo = SunHalo.create(sun);
        gameObjects().addGameObject(sunHalo, Layer.BACKGROUND);
    }
    /**
     * Initializes the start area terrain.
     * Sets collisions between layers for different rows of blocks and the avatar,
     * reduces the number of collision calculations.
     */
    private void initializeGround() {
        // initial world render range.
        this.minRender = (int) (AVATAR_X_START - (OVER_RENDER_FACTOR * this.windowDimensions.x()));
        this.maxRender = (int) (AVATAR_X_START + (OVER_RENDER_FACTOR * this.windowDimensions.x()));

        // terrain creation.
        this.terrain = new Terrain(this.windowDimensions, (int) new Random().nextGaussian());

        // saves blocks to iterate on irrelevant blocks.
        this.ground = terrain.createInRange(this.minRender, this.maxRender);
        this.addBlocks(this.ground, true);

        // separates two upper layers of blocks, to reduce collisions. (avatar = layer 0)
        gameObjects().layers().shouldLayersCollide(0, GRASS_LAYER, true);
        gameObjects().layers().shouldLayersCollide(0, MID_GROUND_LAYER, true);
        gameObjects().layers().shouldLayersCollide(MID_GROUND_LAYER, GRASS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(GROUND_LAYER, 0, false);
        gameObjects().layers().shouldLayersCollide(GROUND_LAYER, GRASS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(GROUND_LAYER, MID_GROUND_LAYER, false);
    }

    /**
     * Creates avatar for the world, and adds an energy display for the avatar.
     * @param imageReader utility for reading image files.
     * @param inputListener listener for user input events.
     */
    private void initializeAvatar(ImageReader imageReader, UserInputListener inputListener) {
        // energy display creation.
        EnergyDisplay energyDisplay = new EnergyDisplay(
                Vector2.ONES.mult(ENERGY_DISPLAY_PLACEMENT),
                new TextRenderable("")
        );
        gameObjects().addGameObject(energyDisplay, Layer.UI);

        // avatar creation, Avatar receives a callback to notify energy has been changed.
        Vector2 avatarStartLoc = new Vector2(
                AVATAR_X_START,
                terrain.groundHeightAt(AVATAR_X_START)-Avatar.AVATAR_SIZE
        );
        this.avatar = new Avatar(avatarStartLoc, inputListener, imageReader, energyDisplay::onEnergyChange);
        gameObjects().addGameObject(this.avatar);
    }

    /**
     * Initializes the start area trees.
     */
    private void initializeTrees() {
        this.flora = new Flora(
                terrain::groundHeightAt, this.avatar::addExtraEnergy,
                new Random().nextGaussian());
        this.trees = flora.createInRange(this.minRender, this.maxRender);
        this.addFlora(this.trees, true);
        // leafs and trunk shouldn't collide, trunk and avatar should.
        gameObjects().layers().shouldLayersCollide(Layer.STATIC_OBJECTS, LEAFS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(LEAFS_LAYER, LEAFS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(Layer.STATIC_OBJECTS, FRUITS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(FRUITS_LAYER, LEAFS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(0, LEAFS_LAYER, false);
        gameObjects().layers().shouldLayersCollide(Layer.STATIC_OBJECTS, 0, true);
        gameObjects().layers().shouldLayersCollide(FRUITS_LAYER, 0, true);
    }

    /**
     * Main entry point for the Pepse.
     * Creates and runs the world (calls initializeGame).
     * @param args command-line arguments.
     */
    public static void main (String[] args) {
        new PepseGameManager().run();
    }
}