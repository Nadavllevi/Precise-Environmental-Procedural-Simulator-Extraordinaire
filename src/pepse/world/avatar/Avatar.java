package pepse.world.avatar;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.util.Vector2;
import pepse.world.Terrain;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Avatar Class.
 * Represents a world figure.
 * Has different movement modes: Jump, Run and Idle.
 * Can conduct different movements that costs energy (different prices for different movements).
 * Energy is increased when avatar is idle or collided with fruit.
 * Contains energy counter to limit movements (there is also a maximum energy limit).
 * Animates the avatar using sets of images for different movement modes.
 * @author Nadav Levi.
 */
public class Avatar extends GameObject {
    /**
     * AVATAR_SIZE Constant in use of PepseGameManager.
     */
    public static final float AVATAR_SIZE = 50;
    /**
     * AVATAR_TAG Constant in use of Fruit to check Collisions.
     */
    public static final String AVATAR_TAG = "avatar";


    private static final float VELOCITY_X = 400;
    private static final float VELOCITY_Y = -450;
    private static final float GRAVITY = 400;
    private static final int RUN_THRESHOLD = 2;
    private static final int JUMP_THRESHOLD = 20;
    private static final int DOUBLE_JUMP_THRESHOLD = 50;
    private static final int FRUIT_EXTRA_ENERGY = 10;
    private static final int MAX_ENERGY = 1000;
    private static final float TIME_BETWEEN_IMAGES = 0.2f;
    private static final int IDLE_ENERGY = 1;

    private static final String[] RUN_IMAGE_PATH =
            {"assets/run_0.png", "assets/run_1.png", "assets/run_2.png",
                    "assets/run_3.png", "assets/run_4.png", "assets/run_5.png"};
    private static final String[] JUMP_IMAGE_PATH =
            {"assets/jump_0.png", "assets/jump_1.png", "assets/jump_2.png", "assets/jump_3.png"};
    private static final String[] IDLE_IMAGE_PATH =
            {"assets/idle_0.png", "assets/idle_1.png", "assets/idle_2.png", "assets/idle_3.png"};
    private static final String DEFAULT_IDLE_IMAGE = "assets/idle_0.png";


    private final UserInputListener inputListener;
    private final ImageReader imageReader;
    private final Consumer<Integer> energyListener;

    private int energy;
    private AvatarMode avatarMode;
    private AvatarMode lastMode;

    /**
     * Constructor for the Avatar.
     * Sets the renderer to initial image.
     * Sets physics to prevent avatar intersection with terrain.
     * @param topLeftCorner the initial top-left position of the avatar.
     * @param inputListener listener for capturing user keyboard input.
     * @param imageReader utility for reading images (for renderable and animations).
     * @param energyListener method reference for notifying energy changed.
     */
    public Avatar(Vector2 topLeftCorner,
                  UserInputListener inputListener,
                  ImageReader imageReader,
                  Consumer<Integer> energyListener) {
        super(topLeftCorner,
                Vector2.ONES.mult(AVATAR_SIZE),
                imageReader.readImage(DEFAULT_IDLE_IMAGE, true)
        );
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);
        this.inputListener = inputListener;
        this.energyListener = energyListener;
        this.imageReader = imageReader;
        this.setTag(AVATAR_TAG);
    }

    /**
     * Handle avatar movements based of user input.
     * Handle movement animations.
     * Checks if energy has changed, and notifies the energy display with the new energy value.
     * if energy doesn't change, the display shouldn't be updated.
     * @param deltaTime the time since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        // handle movement and return changed energy.
        int energyOffset = handleMovement();
        // handle animation according to current avatar mode.
        handleAnimation();

        // apply energy changes and notify observer.
        if (energyOffset != 0) {
            this.energy = Math.max(0, Math.min(this.energy + energyOffset, MAX_ENERGY));
            this.energyListener.accept(this.energy);
        }
        // saves last mode to change animation only when needed.
        this.lastMode = this.avatarMode;
    }

    /**
     * When avatar somehow fell through ground, it will be returned to surface and stopped.
     * @param other the other GameObject involved in the collision.
     * @param collision the collision data.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        if ((other.getTag().equals(Terrain.TOP_GROUND_TAG) || other.getTag().equals(Terrain.MID_GROUND_TAG))
                && collision.getRelativeVelocity().y() > 0) {
            this.transform().setVelocityY(0);
            this.setTopLeftCorner(new Vector2(
                    getTopLeftCorner().x(),
                    other.getTopLeftCorner().y() - AVATAR_SIZE
            ));
        }
    }

    /**
     * Adds Extra Energy when collided with fruit.
     * Notifies the display (the observer).
     */
    public void addExtraEnergy(){
        this.energy = Math.max(0, Math.min(this.energy + FRUIT_EXTRA_ENERGY, MAX_ENERGY));
        this.energyListener.accept(this.energy);
    }

    /**
     * Changes the set of images for the animation,
     * according to the avatar mode.
     * if mode have not changed, animation remains the same.
     * Flipping the renderer to match the velocity direction.
     */
    private void handleAnimation() {
        String[] images;
        // choose the correct images to animate over.
        if (avatarMode == AvatarMode.IDLE) {
            images = IDLE_IMAGE_PATH;
        } else if (avatarMode == AvatarMode.JUMP) {
            images = JUMP_IMAGE_PATH;
        } else {
            images = RUN_IMAGE_PATH;
        }

        // change only if mode has changed.
        if (this.lastMode != this.avatarMode) {
            renderer().setRenderable(new AnimationRenderable(
                    images,
                    this.imageReader,
                    true,
                    TIME_BETWEEN_IMAGES
            ));
        }
        // match velocity direction.
        renderer().setIsFlippedHorizontally(this.getVelocity().x() < 0);
    }

    /**
     * Moves the avatar according to the user input and sufficient amount of energy.
     * If no movement requested or not enough energy, avatar stays idle and gains energy.
     * @return the energy offset after the movement.
     */
    private int handleMovement() {
        float xVel = 0;
        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT)) xVel -= VELOCITY_X;
        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT)) xVel += VELOCITY_X;

        // Avatar is trying to jump.
        if (this.avatarMode != AvatarMode.JUMP &&
                inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                this.energy >= JUMP_THRESHOLD) {
            this.avatarMode = AvatarMode.JUMP;
            transform().setVelocityY(VELOCITY_Y);
            return -JUMP_THRESHOLD;
        }

        // Avatar is on ground.
        if (this.avatarMode != AvatarMode.JUMP) {
            // Avatar is trying to run.
            if (xVel != 0 && this.energy >= RUN_THRESHOLD) {
                this.avatarMode = AvatarMode.RUN;
                transform().setVelocityX(xVel);
                return -RUN_THRESHOLD;
            // on ground and didn't move -> idle mode.
            } else {
                this.avatarMode = AvatarMode.IDLE;
                transform().setVelocityX(0);
                return IDLE_ENERGY;
            }
        } else {
            // Avatar is in air.
            transform().setVelocityX(xVel);
            // trying to double jump, conduct movement only if falling.
            if (getVelocity().y() > 0 && inputListener.isKeyPressed(KeyEvent.VK_SPACE) &&
                    this.energy >= DOUBLE_JUMP_THRESHOLD) {
                transform().setVelocityY(VELOCITY_Y);
                return -DOUBLE_JUMP_THRESHOLD;
            } else if (getVelocity().y() == 0) {
                // returned to ground.
                this.avatarMode = AvatarMode.IDLE;
            }
            // no energy offset, was in air and didn't double jump.
            return 0;
        }
    }
}
