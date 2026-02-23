package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * Night Class.
 * Creates black background that change its opacity.
 * Simulates night and day by changing opacity.
 * @author Nadav Levi.
 */
public class Night {
    private static final Float MIDNIGHT_OPACITY = 0.5f;
    private static final String NIGHT_TAG = "night";
    private static final int TRANSITION_TIME_FACTOR = 2;

    /**
     * Creates black rectangle with the same dimensions as window for background.
     * Adds transition to change this rectangle opacity.
     * @param windowDimensions the window dimensions for the background.
     * @param cycleLength the length of one transition.
     * @return the night background game object.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        GameObject night = new GameObject(
                Vector2.ZERO,
                windowDimensions,
                new RectangleRenderable(Color.BLACK)
        );
        // night background's coordinate space should be camera.
        night.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        night.setTag(NIGHT_TAG);
        // creates back-and-forth transition that change opaqueness.
        new Transition<Float>(
                night,
                night.renderer()::setOpaqueness,
                0f,
                MIDNIGHT_OPACITY,
                Transition.CUBIC_INTERPOLATOR_FLOAT,
                cycleLength / TRANSITION_TIME_FACTOR,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null
        );
        return night;
    }
}