package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.world.avatar.Avatar;

import java.awt.Color;

/**
 * Fruit Class.
 * Represent a fruit in a tree.
 * Responsible to disappear when caught by the avatar,
 * and calls a callback to notify it has been caught and preform certain action.
 * @author Nadav Levi.
 */
public class Fruit extends GameObject {
    private static final int FRUIT_SIZE = 20;
    private static final int FRUIT_COOLDOWN = 850;
    private static final String UNACTIVE_FRUIT_TAG = "unactive_fruit";
    private static final String FRUIT_TAG = "fruit";

    private final Runnable addEnergyCallback;
    private final Color fruitColor;

    private int timeRemoved;

    /**
     * Constructor for fruit.
     * Creates new fruit object at given top left position, with constant size,
     * and round renderable with given fruit color.
     * Saves a Runnable reference to call on collisions.
     * @param topLeftPos the top-left position of the fruit.
     * @param fruitColor the color of the fruit renderable.
     * @param addEnergy the method to call on collision.
     */
    public Fruit(Vector2 topLeftPos, Color fruitColor, Runnable addEnergy){
        super(topLeftPos, new Vector2(FRUIT_SIZE, FRUIT_SIZE), new OvalRenderable(fruitColor));
        this.fruitColor = fruitColor;
        this.setTag(FRUIT_TAG);
        this.timeRemoved = 0;
        this.addEnergyCallback = addEnergy;
    }

    /**
     * Counts the time since the fruit disappeared, because the avatar caught it.
     * If enough time have passed, the fruit will return to be active.
     * @param deltaTime the time since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        String tag = this.getTag();
        // enough time passed.
        if (tag.equals(UNACTIVE_FRUIT_TAG) && this.timeRemoved > FRUIT_COOLDOWN){
            this.setTag(FRUIT_TAG);
            this.renderer().setRenderable(new OvalRenderable(this.fruitColor));
            this.timeRemoved = 0;
        } else if (tag.equals(UNACTIVE_FRUIT_TAG)){
            // fruit is removed, add to time counter.
            this.timeRemoved++;
        }
    }

    /**
     * Check if this fruit is active and collided with the avatar.
     * If it does, this function disables the current fruit,
     * and callback a function that preform certain actions (adds energy).
     * @param other the other GameObject involved in the collision.
     * @param collision the collision data.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);
        if (this.getTag().equals(FRUIT_TAG) && other.getTag().equals(Avatar.AVATAR_TAG)){
            this.disableFruit();
            this.addEnergyCallback.run();
        }
    }

    /**
     * Makes the fruit disappear, and disable its collision actions.
     */
    private void disableFruit(){
        this.setTag(UNACTIVE_FRUIT_TAG);
        this.timeRemoved = 0;
        this.renderer().setRenderable(null);
    }
}