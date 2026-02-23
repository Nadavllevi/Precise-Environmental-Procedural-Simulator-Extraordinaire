package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * SunHalo Class.
 * Represent a sun halo world object.
 * Creates new halo that follows the location of a given sun GameObject.
 * @author Nadav Levi.
 */
public class SunHalo {
    private static final float HALO_FACTOR = 1.5f;
    private static final Color HALO_COLOR = new Color(255,255,0,20);

    /**
     * Constructor for SunHalo.
     * Create a bigger circle than the sun, with more transparent color shade.
     * @param sun the sun the halo should follow.
     * @return the created sun-halo game object.
     */
    public static GameObject create(GameObject sun) {
        Vector2 haloDimensions = sun.getDimensions().mult(HALO_FACTOR);
        GameObject sunHalo = new GameObject(
                Vector2.ZERO,
                haloDimensions,
                new OvalRenderable(HALO_COLOR)
        );
        // halo's coordinate space should be camera.
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        // matches the center of the halo to the center of the sun in each update.
        sunHalo.addComponent((float deltaTime) -> sunHalo.setCenter(sun.getCenter()));
        return sunHalo;
    }
}
