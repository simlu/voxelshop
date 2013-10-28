package com.vitco.objects;

import java.awt.*;

/**
 * Player Class
 */
public class Player extends Entity {

    public Player(int x, int y, World world) {
        super(x, y, world);
    }

    public Point getPosition() {
        return new Point(entityPositionX, entityPositionY);
    }

    // other player logic
    // ...
}
