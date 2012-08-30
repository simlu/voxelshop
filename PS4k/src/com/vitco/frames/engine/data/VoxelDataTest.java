package com.vitco.frames.engine.data;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

/**
 * Extensive test for voxel data.
 */
public class VoxelDataTest {

    private final Data data = new Data();

    @Test
    public void testCreateDeleteLayer() throws Exception {
        int lid1 =data.createLayer("hello");
        assert data.getLayers().length == 1;
        data.undoV();
        assert data.getLayers().length == 0;
        data.redoV();
        assert data.getLayers().length == 1;
        data.deleteLayer(data.getLayers()[0]);
        assert data.getLayers().length == 0;
        data.undoV();
        assert data.getLayers().length == 1;
        data.undoV();
        assert data.getLayers().length == 0;
        data.redoV();
        data.redoV();
        assert data.getLayers().length == 0;
        data.undoV();
        assert data.getLayers().length == 1;
        assert data.getLayerNames()[0].equals(data.getLayerName(lid1));
        assert data.getLayerNames()[0].equals("hello");
        int lid2 = data.createLayer("hallo2");
        data.deleteLayer(lid1);
        data.deleteLayer(lid2);
        assert data.getLayers().length == 0;
        data.undoV();
        assert data.getLayers().length == 1;
        data.undoV();
        assert data.getLayers().length == 2;
        data.undoV();
        assert data.getLayers().length == 1;
        data.redoV();
        assert data.getLayers().length == 2;
        data.createLayer("tt1");
        data.createLayer("tt2");
        data.createLayer("tt3");
        assert data.getLayers().length == 5;
        data.undoV();
        data.undoV();
        assert data.getLayers().length == 3;
        data.undoV();
        data.undoV();
        data.undoV();
        assert data.getLayers().length == 0;
        while (data.canRedoV()) {
            data.redoV();
        }
        assert data.getLayers().length == 5;
        data.deleteLayer(lid1);
        data.deleteLayer(lid2);
        assert data.getLayers().length == 3;
    }

    @Test
    public void testRenameLayer() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        data.renameLayer(lid1, "newName");
        assert data.getLayerName(lid1).equals("newName");
        data.undoV();
        assert data.getLayerName(lid1).equals("layer1");
        data.undoV();
        data.undoV();
        assert data.getLayerName(lid1) == null;
        data.redoV();
        data.redoV();
        data.redoV();
        assert data.getLayerName(lid1).equals("newName");
        data.undoV();
        assert data.getLayerName(lid1).equals("layer1");
        data.undoV();
        assert data.getLayerName(lid1).equals("layer1");
    }

    @Test
    public void testSelectLayer() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        assert data.getSelectedLayer() == -1;
        data.selectLayer(lid1);
        assert data.getSelectedLayer() == lid1;
        data.selectLayer(lid2);
        assert data.getSelectedLayer() == lid2;
        data.selectLayer(-1);
        assert data.getSelectedLayer() == -1;
        data.undoV();
        assert data.getSelectedLayer() == lid2;
        data.undoV();
        assert data.getSelectedLayer() == lid1;
        data.undoV();
        assert data.getSelectedLayer() == -1;
        data.redoV();
        assert data.getSelectedLayer() == lid1;
        data.redoV();
        assert data.getSelectedLayer() == lid2;
    }

    @Test
    public void canUndoRedo() {
        assert data.canRedoV() == false;
        assert data.canUndoV() == false;
        int lid1 = data.createLayer("layer1");
        assert data.canRedoV() == false;
        assert data.canUndoV() == true;
        data.undoV();
        assert data.canRedoV() == true;
        assert data.canUndoV() == false;
        data.redoV();
        assert data.canRedoV() == false;
        assert data.canUndoV() == true;
        data.deleteLayer(lid1);
        assert data.canRedoV() == false;
        assert data.canUndoV() == true;
        data.undoV();
        assert data.canRedoV() == true;
        assert data.canUndoV() == true;
        data.undoV();
        assert data.canRedoV() == true;
        assert data.canUndoV() == false;
    }

    @Test
    public void testGetLayers() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        Integer[] layers = data.getLayers();
        assert lid1 == layers[1];
        assert lid2 == layers[0];
        data.undoV();
        layers = data.getLayers();
        assert layers[0] == lid1;
        data.undoV();
        assert data.getLayers().length == 0;
        data.redoV();
        layers = data.getLayers();
        assert layers[0] == lid1;
        data.redoV();
        layers = data.getLayers();
        assert lid1 == layers[1];
        assert lid2 == layers[0];
        data.undoV();
        data.undoV();
        assert data.getLayers().length == 0;
    }

    @Test
    public void testSetVisible() throws Exception {
        int lid1 = data.createLayer("layer1");
        assert data.getLayerVisible(lid1) == true;
        data.setVisible(lid1, false);
        assert data.getLayerVisible(lid1) == false;
        data.setVisible(lid1, true);
        assert data.getLayerVisible(lid1) == true;
        data.undoV();
        assert data.getLayerVisible(lid1) == false;
        data.undoV();
        assert data.getLayerVisible(lid1) == true;
        data.redoV();
        assert data.getLayerVisible(lid1) == false;
        data.redoV();
        assert data.getLayerVisible(lid1) == true;
        data.setVisible(lid1, true); // this does not create a history entry
        data.undoV();
        assert data.getLayerVisible(lid1) == false;
    }

    @Test
    public void testMoveLayerUpDownAndCanMoveLayerUpDown() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        int lid3 = data.createLayer("layer3");
        assert data.canMoveLayerUp(lid1) == true;
        assert data.canMoveLayerUp(lid2) == true;
        assert data.canMoveLayerUp(lid3) == false;
        assert data.canMoveLayerDown(lid3) == true;
        assert data.canMoveLayerDown(lid2) == true;
        assert data.canMoveLayerDown(lid1) == false;
        data.moveLayerDown(lid1);
        data.undoV();
        assert data.getLayerName(lid3) == null;
        data.redoV();
        data.moveLayerUp(lid1);
        assert data.getLayerNames()[0].equals("layer3");
        assert data.getLayerNames()[1].equals("layer1");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2) == true;
        assert data.canMoveLayerUp(lid1) == true;
        assert data.canMoveLayerUp(lid3) == false;
        assert data.canMoveLayerDown(lid3) == true;
        assert data.canMoveLayerDown(lid1) == true;
        assert data.canMoveLayerDown(lid2) == false;
        data.moveLayerUp(lid1);
        assert data.getLayerNames()[0].equals("layer1");
        assert data.getLayerNames()[1].equals("layer3");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2) == true;
        assert data.canMoveLayerUp(lid1) == false;
        assert data.canMoveLayerUp(lid3) == true;
        assert data.canMoveLayerDown(lid3) == true;
        assert data.canMoveLayerDown(lid1) == true;
        assert data.canMoveLayerDown(lid2) == false;
        data.deleteLayer(lid1);
        data.undoV();
        data.undoV();
        assert data.getLayerNames()[0].equals("layer3");
        assert data.getLayerNames()[1].equals("layer1");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2) == true;
        assert data.canMoveLayerUp(lid1) == true;
        assert data.canMoveLayerUp(lid3) == false;
        assert data.canMoveLayerDown(lid3) == true;
        assert data.canMoveLayerDown(lid1) == true;
        assert data.canMoveLayerDown(lid2) == false;
        data.undoV();
        assert data.canMoveLayerUp(lid1) == true;
        assert data.canMoveLayerUp(lid2) == true;
        assert data.canMoveLayerUp(lid3) == false;
        assert data.canMoveLayerDown(lid3) == true;
        assert data.canMoveLayerDown(lid2) == true;
        assert data.canMoveLayerDown(lid1) == false;
        data.redoV();
        data.redoV();
        assert data.getLayerNames()[0].equals("layer1");
        assert data.getLayerNames()[1].equals("layer3");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2) == true;
        assert data.canMoveLayerUp(lid1) == false;
        assert data.canMoveLayerUp(lid3) == true;
        assert data.canMoveLayerDown(lid3) == true;
        assert data.canMoveLayerDown(lid1) == true;
        assert data.canMoveLayerDown(lid2) == false;
    }

    @Test
    public void randomeMess() throws Exception {
        // todo complete!
        class Util {
            private final Random rand;
            public Util(int seed) {
                rand = new Random(seed);
            }

            public float getFloat() {
                return rand.nextFloat();
            }

            public int getInt(int range) {
                return rand.nextInt(range);
            }

            // XXXXXXXXXXXXXXXXXX

            public void createLayer() {
                data.createLayer(new BigInteger(130, rand).toString(32));
            }

            public void deleteLayer() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.deleteLayer(layers[rem]);
                }
            }

            public void renameLayer() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.renameLayer(layers[rem], new BigInteger(130, rand).toString(32));
                }
            }

            public void selectLayer() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.selectLayer(layers[rem]);
                }
            }

            public void setVisible() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.setVisible(layers[rem], rand.nextBoolean());
                }
            }

            public void moveLayerUp() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.moveLayerUp(layers[rem]);
                }
            }

            public void moveLayerDown() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.moveLayerDown(layers[rem]);
                }
            }

        }

        final int poss = 7;

        for (int seed = 0; seed < 10; seed ++) {
            Util util = new Util(seed);
            float[] prob = new float[poss];
            for (int k = 0; k < prob.length; k++) {
                prob[k] = util.getFloat();
            }
            for (int k = 0; k < 50000; k++) {
                switch (util.getInt(poss) + 1) {
                    case 1:
                        if (util.getFloat() < prob[0]) {
                            util.createLayer();
                        }
                        break;
                    case 2:
                        if (util.getFloat() < prob[1]) {
                            util.deleteLayer();
                        }
                        break;
                    case 3:
                        if (util.getFloat() < prob[2]) {
                            util.renameLayer();
                        }
                        break;
                    case 4:
                        if (util.getFloat() < prob[3]) {
                            util.selectLayer();
                        }
                        break;
                    case 5:
                        if (util.getFloat() < prob[4]) {
                            util.createLayer();
                        }
                        break;
                    case 6:
                        if (util.getFloat() < prob[5]) {
                            util.moveLayerUp();
                        }
                        break;
                    case 7:
                        if (util.getFloat() < prob[6]) {
                            util.moveLayerDown();
                        }
                        break;
                }
            }

            System.out.println(seed);

            // wind forward
            while (data.canRedoV()) {
                data.redoV();
            }

            // store
            Integer[] layers = data.getLayers();
            String[] layerNames = data.getLayerNames();

            // wind backwards
            while (data.canUndoV()) {
                data.undoV();
            }

            // assert
            assert data.getLayers().length == 0;
            assert data.getLayerNames().length == 0;

            // wind forward
            while (data.canRedoV()) {
                data.redoV();
            }

            // store new version
            Integer[] layersN = data.getLayers();
            String[] layerNamesN = data.getLayerNames();

            // check
            assert layersN.length == layers.length;
            assert layerNamesN.length == layers.length;
            assert layerNamesN.length == layerNames.length;

            for (int i = 0; i < layersN.length; i++) {
                assert layersN[i] == layers[i];
                assert layerNamesN[i].equals(layerNames[i]);
            }

            // wind backwards again
            while (data.canUndoV()) {
                data.undoV();
            }

            // assert
            assert data.getLayers().length == 0;
            assert data.getLayerNames().length == 0;

        }
    }

}
