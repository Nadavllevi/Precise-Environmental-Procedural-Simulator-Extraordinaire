package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Block Class.
 * Represents a single block in the terrain.
 * Blocks doesn't move and prevent intersections from any direction.
 * @author Nadav Levi.
 */
public class Block extends GameObject {
    /**
     * Block.SIZE constant in use of PepseGameManager.
     */
    public static final int SIZE = 30;

    /**
     * Constructor for Block.
     * Creates new block at a given location using a given renderable.
     * Sets physics to prevent intersections, if intersected, the block will not move.
     * @param topLeftCorner the top-left corner of the block.
     * @param renderable the renderable for the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }

}
