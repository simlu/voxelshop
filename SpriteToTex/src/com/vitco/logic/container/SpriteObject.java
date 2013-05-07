package com.vitco.logic.container;

import java.awt.*;
import java.io.File;

/**
 * Sprite Object Class
 */
// sprite object
public final class SpriteObject {
    public transient final String prefix;
    public transient final String postfix;
    public final String type;
    public final Integer id;
    public transient final String sprite;
    public transient final Short zone;

    public SpriteObject(String prefix, String postfix, String type, Integer id, String sprite, Short zone) {
        this.prefix = prefix;
        this.postfix = postfix;
        this.type = type;
        this.id = id;
        this.sprite = sprite;
        this.zone = zone;
    }

    public String texture;
    public void setTexture(String texture) {
        this.texture = texture;
    }

    public int x;
    public int y;
    public void setPosition(Point position) {
        x = position.x;
        y = position.y;
    }

    public transient File file = null;
    public void setSpriteFile(File file) {
        this.file = file;
    }
}
