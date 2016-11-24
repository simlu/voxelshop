package com.vitco.app.layout.content.colorchooser;

import com.vitco.app.layout.content.colorchooser.basic.ColorChooserPrototype;
import com.vitco.app.util.misc.ColorTools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A very simple color chooser.
 */
public class PresetColorChooser extends ColorChooserPrototype {

    private final static Color[] DEFAULT_SWATCH = new Color[] {
            // black to white
            new Color(0, 0, 0), new Color(14, 14, 14), new Color(27, 27, 27), new Color(41, 41, 41),
            new Color(54, 54, 54), new Color(68, 68, 68), new Color(81, 81, 81), new Color(95, 95, 95),
            new Color(108, 108, 108), new Color(122, 122, 122), new Color(135, 135, 135), new Color(149, 149, 149),
            new Color(162, 162, 162), new Color(176, 176, 176), new Color(189, 189, 189), new Color(203, 203, 203),
            new Color(216, 216, 216), new Color(230, 230, 230), new Color(243, 243, 243), new Color(255, 255, 255),
            // "flower"
            new Color(247, 252, 249), new Color(247, 251, 247), new Color(248, 251, 238), new Color(248, 250, 230),
            new Color(246, 248, 213), new Color(245, 247, 213), new Color(248, 248, 212), new Color(246, 247, 209),
            new Color(240, 242, 201), new Color(234, 239, 191), new Color(216, 227, 170), new Color(210, 223, 165),
            new Color(207, 223, 165), new Color(208, 226, 177), new Color(217, 233, 205), new Color(217, 231, 213),
            new Color(216, 232, 216), new Color(220, 236, 223), new Color(233, 242, 236), new Color(244, 249, 245),
            new Color(247, 251, 247), new Color(247, 251, 240), new Color(247, 247, 230), new Color(245, 244, 210),
            new Color(244, 242, 201), new Color(243, 240, 198), new Color(246, 244, 203), new Color(246, 246, 198),
            new Color(241, 245, 188), new Color(234, 243, 175), new Color(213, 232, 155), new Color(206, 224, 154),
            new Color(193, 216, 154), new Color(185, 214, 153), new Color(184, 215, 160), new Color(196, 223, 188),
            new Color(201, 222, 201), new Color(208, 226, 209), new Color(218, 233, 221), new Color(236, 242, 237),
            new Color(247, 249, 236), new Color(245, 245, 228), new Color(245, 237, 204), new Color(241, 231, 187),
            new Color(239, 229, 183), new Color(241, 231, 180), new Color(247, 247, 183), new Color(246, 249, 156),
            new Color(238, 248, 141), new Color(229, 247, 130), new Color(205, 236, 114), new Color(193, 225, 118),
            new Color(181, 214, 125), new Color(175, 210, 142), new Color(154, 203, 138), new Color(151, 200, 143),
            new Color(167, 206, 165), new Color(190, 217, 194), new Color(203, 224, 209), new Color(215, 232, 221),
            new Color(244, 242, 224), new Color(241, 233, 202), new Color(237, 221, 180), new Color(238, 220, 173),
            new Color(239, 223, 173), new Color(244, 234, 158), new Color(248, 248, 140), new Color(246, 247, 125),
            new Color(240, 247, 112), new Color(227, 246, 104), new Color(195, 232, 90), new Color(178, 223, 89),
            new Color(162, 214, 99), new Color(157, 210, 111), new Color(142, 198, 126), new Color(139, 193, 135),
            new Color(137, 193, 142), new Color(154, 198, 164), new Color(183, 213, 196), new Color(195, 218, 208),
            new Color(239, 227, 193), new Color(236, 219, 181), new Color(233, 211, 165), new Color(234, 211, 163),
            new Color(242, 220, 150), new Color(249, 229, 126), new Color(249, 235, 105), new Color(252, 246, 87),
            new Color(244, 246, 87), new Color(223, 246, 86), new Color(177, 238, 81), new Color(146, 227, 77),
            new Color(136, 220, 74), new Color(112, 204, 83), new Color(109, 199, 101), new Color(124, 184, 131),
            new Color(126, 181, 139), new Color(128, 185, 145), new Color(161, 200, 182), new Color(183, 208, 200),
            new Color(234, 213, 175), new Color(227, 197, 151), new Color(232, 201, 151), new Color(240, 206, 139),
            new Color(245, 214, 119), new Color(247, 219, 109), new Color(249, 222, 79), new Color(253, 226, 80),
            new Color(243, 243, 86), new Color(216, 239, 83), new Color(160, 223, 75), new Color(123, 220, 74),
            new Color(76, 215, 71), new Color(82, 202, 66), new Color(85, 195, 91), new Color(88, 185, 110),
            new Color(109, 173, 138), new Color(117, 172, 141), new Color(116, 170, 156), new Color(165, 198, 190),
            new Color(227, 189, 147), new Color(224, 185, 132), new Color(233, 184, 119), new Color(242, 185, 95),
            new Color(245, 202, 102), new Color(247, 205, 90), new Color(254, 205, 73), new Color(240, 202, 72),
            new Color(201, 192, 66), new Color(174, 189, 64), new Color(120, 173, 57), new Color(77, 175, 56),
            new Color(69, 198, 65), new Color(71, 202, 66), new Color(65, 190, 83), new Color(66, 179, 109),
            new Color(70, 160, 135), new Color(93, 153, 140), new Color(103, 151, 143), new Color(117, 162, 157),
            new Color(221, 175, 128), new Color(224, 177, 120), new Color(239, 171, 90), new Color(242, 175, 78),
            new Color(252, 173, 63), new Color(253, 190, 68), new Color(239, 186, 66), new Color(190, 158, 54),
            new Color(155, 138, 46), new Color(114, 116, 36), new Color(76, 110, 33), new Color(43, 132, 40),
            new Color(51, 152, 48), new Color(63, 183, 59), new Color(63, 183, 71), new Color(49, 164, 130),
            new Color(56, 153, 139), new Color(72, 147, 145), new Color(95, 141, 142), new Color(103, 143, 144),
            new Color(218, 168, 116), new Color(222, 167, 107), new Color(239, 161, 73), new Color(239, 163, 65),
            new Color(251, 156, 57), new Color(251, 160, 59), new Color(202, 134, 47), new Color(157, 120, 40),
            new Color(93, 80, 38), new Color(46, 46, 16), new Color(41, 59, 18), new Color(30, 75, 28),
            new Color(37, 117, 46), new Color(40, 132, 88), new Color(45, 156, 134), new Color(40, 151, 149),
            new Color(46, 142, 151), new Color(59, 137, 151), new Color(86, 132, 144), new Color(92, 133, 142),
            new Color(215, 161, 110), new Color(223, 158, 96), new Color(234, 152, 68), new Color(239, 149, 55),
            new Color(250, 137, 52), new Color(247, 135, 51), new Color(193, 107, 38), new Color(125, 82, 27),
            new Color(60, 47, 24), new Color(27, 25, 15), new Color(19, 27, 13), new Color(18, 40, 28),
            new Color(15, 73, 75), new Color(20, 106, 124), new Color(22, 132, 168), new Color(19, 133, 177),
            new Color(41, 130, 163), new Color(56, 130, 158), new Color(83, 126, 148), new Color(89, 127, 144),
            new Color(211, 142, 97), new Color(216, 142, 88), new Color(230, 133, 63), new Color(243, 123, 47),
            new Color(249, 109, 44), new Color(249, 106, 43), new Color(195, 77, 30), new Color(123, 60, 25),
            new Color(55, 28, 19), new Color(22, 11, 16), new Color(16, 15, 28), new Color(22, 30, 58),
            new Color(25, 53, 107), new Color(8, 69, 165), new Color(11, 96, 208), new Color(6, 101, 212),
            new Color(38, 109, 184), new Color(52, 110, 172), new Color(74, 112, 154), new Color(82, 116, 152),
            new Color(208, 138, 93), new Color(211, 134, 88), new Color(226, 123, 63), new Color(239, 114, 44),
            new Color(248, 93, 40), new Color(248, 83, 37), new Color(200, 54, 25), new Color(152, 32, 16),
            new Color(83, 26, 25), new Color(48, 14, 31), new Color(40, 9, 45), new Color(32, 17, 67),
            new Color(45, 41, 134), new Color(34, 54, 170), new Color(30, 69, 212), new Color(19, 82, 210),
            new Color(44, 97, 182), new Color(54, 105, 172), new Color(74, 106, 152), new Color(82, 113, 154),
            new Color(207, 136, 98), new Color(206, 127, 87), new Color(220, 114, 64), new Color(225, 105, 52),
            new Color(241, 85, 37), new Color(244, 54, 31), new Color(229, 35, 26), new Color(187, 26, 20),
            new Color(132, 16, 23), new Color(77, 12, 39), new Color(57, 15, 60), new Color(51, 26, 93),
            new Color(70, 45, 143), new Color(81, 61, 189), new Color(66, 62, 191), new Color(37, 71, 190),
            new Color(54, 86, 170), new Color(59, 94, 161), new Color(74, 98, 148), new Color(92, 122, 163),
            new Color(208, 146, 116), new Color(202, 126, 90), new Color(208, 117, 75), new Color(214, 105, 64),
            new Color(218, 77, 50), new Color(223, 55, 33), new Color(229, 35, 26), new Color(216, 32, 24),
            new Color(145, 21, 39), new Color(108, 19, 56), new Color(78, 23, 79), new Color(77, 33, 109),
            new Color(86, 53, 165), new Color(84, 58, 179), new Color(76, 49, 154), new Color(71, 55, 148),
            new Color(61, 79, 154), new Color(68, 86, 143), new Color(70, 92, 152), new Color(106, 132, 183),
            new Color(216, 171, 152), new Color(204, 134, 107), new Color(191, 104, 83), new Color(190, 97, 75),
            new Color(202, 78, 63), new Color(208, 58, 52), new Color(205, 42, 46), new Color(201, 30, 30),
            new Color(163, 26, 52), new Color(125, 24, 69), new Color(96, 28, 91), new Color(91, 36, 117),
            new Color(86, 48, 151), new Color(84, 43, 138), new Color(80, 42, 133), new Color(77, 51, 131),
            new Color(70, 61, 119), new Color(72, 65, 126), new Color(86, 102, 162), new Color(149, 164, 199),
            new Color(220, 187, 173), new Color(211, 159, 143), new Color(193, 113, 98), new Color(181, 94, 83),
            new Color(174, 89, 81), new Color(188, 64, 65), new Color(187, 49, 62), new Color(164, 29, 66),
            new Color(151, 27, 67), new Color(129, 28, 79), new Color(104, 30, 94), new Color(92, 33, 106),
            new Color(83, 34, 111), new Color(81, 36, 117), new Color(79, 40, 116), new Color(73, 51, 106),
            new Color(75, 58, 111), new Color(83, 69, 132), new Color(141, 143, 183), new Color(167, 173, 204),
            new Color(228, 206, 197), new Color(218, 182, 171), new Color(199, 139, 129), new Color(181, 101, 93),
            new Color(167, 84, 81), new Color(162, 75, 79), new Color(146, 54, 77), new Color(139, 44, 77),
            new Color(126, 37, 79), new Color(113, 33, 82), new Color(92, 28, 85), new Color(92, 33, 106),
            new Color(75, 28, 90), new Color(73, 32, 91), new Color(72, 46, 95), new Color(72, 50, 99),
            new Color(83, 61, 117), new Color(125, 113, 160), new Color(171, 170, 198), new Color(190, 197, 215),
            new Color(238, 230, 226), new Color(226, 203, 197), new Color(213, 175, 170), new Color(191, 133, 130),
            new Color(170, 93, 93), new Color(157, 75, 80), new Color(134, 58, 80), new Color(127, 52, 83),
            new Color(114, 43, 82), new Color(107, 41, 82), new Color(88, 34, 81), new Color(80, 33, 80),
            new Color(70, 33, 79), new Color(63, 41, 75), new Color(70, 47, 91), new Color(84, 63, 110),
            new Color(123, 105, 147), new Color(168, 163, 190), new Color(196, 196, 213), new Color(233, 237, 240),
            new Color(246, 247, 244), new Color(238, 229, 226), new Color(222, 198, 195), new Color(207, 173, 173),
            new Color(187, 139, 143), new Color(151, 87, 101), new Color(133, 63, 86), new Color(122, 54, 81),
            new Color(108, 46, 78), new Color(97, 46, 76), new Color(76, 42, 72), new Color(70, 40, 69),
            new Color(64, 39, 70), new Color(65, 44, 77), new Color(84, 66, 97), new Color(139, 127, 154),
            new Color(167, 161, 183), new Color(194, 192, 208), new Color(227, 230, 234), new Color(243, 248, 247),
            new Color(248, 251, 248), new Color(244, 241, 238), new Color(234, 225, 222), new Color(213, 190, 190),
            new Color(197, 163, 166), new Color(177, 136, 146), new Color(147, 94, 117), new Color(124, 69, 96),
            new Color(108, 52, 83), new Color(96, 45, 76), new Color(77, 41, 72), new Color(72, 40, 70),
            new Color(73, 46, 75), new Color(87, 68, 96), new Color(145, 133, 153), new Color(161, 153, 171),
            new Color(185, 183, 197), new Color(222, 225, 229), new Color(242, 246, 245), new Color(246, 251, 249)
    };

    private void setupColors(int ROWS, int COLS, Color[] colors) {

        this.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, Color.BLACK));

        setLayout(new GridLayout(ROWS,COLS));

        // fill the whole panel with labels and colors
        int i = 0;
        for( int x=0 ; x<ROWS ; x++) {
            for( int y=0 ; y<COLS ; y++) {
                final JPanel panel = new JPanel();

                panel.setPreferredSize(new Dimension(13,13));
                panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
                if (colors.length > i) {
                    panel.setBackground(colors[i++]);
                } else {
                    panel.setBackground(Color.WHITE);
                }
                panel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        notifyListeners(ColorTools.colorToHSB(panel.getBackground()));
                    }
                });
                add(panel);
            }
        }
    }

    // custom constructor
    public PresetColorChooser(Color[] colors) {
        int maxHeight = (int)Math.floor(Math.sqrt(colors.length));
        setupColors(Math.min(colors.length, maxHeight), (int)Math.ceil(colors.length/(double)maxHeight), colors);
    }

    // custom constructor
    public PresetColorChooser(int ROWS, int COLS, Color[] colors) {
        setupColors(ROWS, COLS, colors);
    }

    // default constructor
    public PresetColorChooser() {

//        final int ROWS=12;
//        final int COLS=30;
//
//        Color[] colors = new Color[ROWS*COLS];
//
//        // generate colors
//        int i = 0;
//        for( int x=0 ; x<ROWS ; x++) {
//            for( int y=0 ; y<COLS ; y++) {
//                colors[i++] = Color.getHSBColor(
//                        ((float) y) / (COLS - 2), // don't draw duplicate (0 equal 1)
//                        Math.min(1, ((float) x * 2 + 1) / (ROWS + 1)),
//                        Math.min(1, 2 - ((float) x * 2 + 1) / (ROWS + 1))
//                );
//            }
//        }

        //setUpColors(ROWS, COLS, colors);

        setupColors(21, 20, DEFAULT_SWATCH);

    }
}
