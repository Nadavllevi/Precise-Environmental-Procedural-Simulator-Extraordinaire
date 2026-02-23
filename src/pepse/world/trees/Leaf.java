package pepse.world.trees;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Leaf class.
 * Represent a leaf in the tree.
 * A leaf has two transitions, that changes it size and angle back and forth to simulate wind.
 * @author Nadav Levi.
 */
public class Leaf extends GameObject {
    private static final Color LEAF_COLOR = new Color(35, 153, 25);
    private static final Float MAX_ANGLE_LEAF = 30f;
    private static final float CYCLE_LENGTH = 1f;
    private static final float MIN_LEAF_SIZE_FACTOR = 0.9f;
    private static final String LEAF_TAG = "leaf";

    /**
     * Constructor for leaf.
     * Creates new square leaf at a given location.
     * @param topLeftPos the top-left position of the leaf.
     */
    public Leaf(Vector2 topLeftPos) {
        super(topLeftPos, new Vector2(Tree.LEAF_SIZE, Tree.LEAF_SIZE), new RectangleRenderable(LEAF_COLOR));
        this.setTag(LEAF_TAG);
    }

    /**
     * Starts two back-and-forth transitions for the current leaf.
     * First transition responsible for changing the leaf size.
     * Second transition responsible for changing the leaf angle.
     */
    public void windTransition() {
        // changes renderable angle.
        new Transition<Float>(
                this,
                (Float angle) -> this.renderer().setRenderableAngle(angle),
                0f,
                MAX_ANGLE_LEAF,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                CYCLE_LENGTH,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
        // changes GameObject's dimensions.
        new Transition<Float>(
                this,
                (Float dimensions) -> this.setDimensions(new Vector2(dimensions, dimensions)),
                (float) Tree.LEAF_SIZE,
                (float) Tree.LEAF_SIZE * MIN_LEAF_SIZE_FACTOR,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                CYCLE_LENGTH,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
    }
}