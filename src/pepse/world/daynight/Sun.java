package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import pepse.world.Terrain;

import java.awt.Color;

/**
 * Sun Class.
 * Represent a sun world object.
 * Creates new sun at the center of the screen.
 * The sun moves in circle to simulate real world sun that rise and sets.
 * @author Nadav Levi.
 */
public class Sun {
    private static final float SUN_SIZE = 100;
    private static final float MIN_ANGLE = 0;
    private static final float MAX_ANGLE = 360;
    private static final int QUARTER_SCREEN_HEIGHT_FACTOR = 4;
    private static final int HALF_SCREEN_FACTOR = 2;

    /**
     * Creates new sun GameObject.
     * This sun has a round loop transition,
     * around the center of the screen and the height of the ground in the start coordinate.
     * @param windowDimensions the window dimensions for the sun's transition.
     * @param cycleLength the length of one transition loop.
     * @return the sun game object.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        GameObject sun = new GameObject(
                Vector2.ZERO,
                Vector2.ONES.mult(SUN_SIZE),
                new OvalRenderable(Color.YELLOW)
        );
        // sun's coordinate space should be camera.
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        Vector2 initialSunCenter = new Vector2(
                windowDimensions.x() / HALF_SCREEN_FACTOR,
                windowDimensions.y() / QUARTER_SCREEN_HEIGHT_FACTOR);
        Vector2 cycleCenter = new Vector2(
                windowDimensions.x() / HALF_SCREEN_FACTOR,
                Terrain.GROUND_AT_X0_FACTOR*windowDimensions.y()
        );
        // creates transition.
        new Transition<Float>(
                sun,
                (Float angle) ->
                    sun.setCenter(
                            initialSunCenter.subtract(cycleCenter)
                                    .rotated(angle)
                                    .add(cycleCenter)
                    ),
                MIN_ANGLE,
                MAX_ANGLE,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                cycleLength,
                Transition.TransitionType.TRANSITION_LOOP,
                null
        );
        return sun;
    }
}