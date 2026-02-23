package pepse.world.avatar;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.Color;

/**
 * EnergyDisplay class.
 * Used to display the avatar's current energy.
 * An observer of the energy value,
 * updates by a call from avatar when energy has changed.
 * @author Nadav Levi.
 */
public class EnergyDisplay extends GameObject {
    private static final float NUMERIC_DISPLAY_WIDTH = 200;
    private static final float NUMERIC_DISPLAY_HEIGHT = 50;
    private static final String ENERGY_TEXT_PREFIX = "%";
    private final TextRenderable textRenderable;

    /**
     * Constructor for EnergyDisplay.
     * Creates a new energy display using a given text renderable.
     * @param topLeftCorner the top-left corner of the display.
     * @param renderable the given text renderable (probably empty).
     */
    public EnergyDisplay(Vector2 topLeftCorner, TextRenderable renderable) {
        super(
                topLeftCorner,
                new Vector2(NUMERIC_DISPLAY_WIDTH, NUMERIC_DISPLAY_HEIGHT),
                renderable
        );
        this.textRenderable = renderable;
        // display's coordinate space should be camera.
        this.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        // sets displayed text color to Dark Gray.
        this.textRenderable.setColor(Color.DARK_GRAY);
    }

    /**
     * Called when energy changed.
     * Updates the text display to show the updated energy value.
     * @param newEnergy the updated energy value.
     */
    public void onEnergyChange(int newEnergy) {
        this.textRenderable.setString(Integer.toString(newEnergy) + ENERGY_TEXT_PREFIX);
    }
}
