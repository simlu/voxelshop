package com.vitco.app.util.misc;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class FontUtil {
    public static Font getTitleFont(Font font) {
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.TRACKING, 0.1); // more spacing
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD); // make bold
        return font.deriveFont(attributes);
    }
}
