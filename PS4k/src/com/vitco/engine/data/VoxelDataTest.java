package com.vitco.engine.data;

import com.vitco.engine.data.container.Voxel;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

/**
 * Extensive test for voxel data.
 */
@SuppressWarnings("ConstantConditions")
public class VoxelDataTest {

    private final Data data = new Data();

    @Before
    public void setUp() throws Exception {
        // all tests expect an empty data container
        while (data.getLayers().length > 0) {
            data.deleteLayer(data.getLayers()[0]);
        }
        data.clearHistoryV();
    }

    // tests for voxels

    @Test
    public void testAddDeleteVoxel() throws Exception {
        assert -1 == data.addVoxel(Color.RED, new int[]{0,0,0});
        int lid1 = data.createLayer("layer1");
        assert -1 == data.addVoxel(Color.RED, new int[]{0,0,0});
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.RED, new int[]{0,0,0});
        assert id1 > -1;
        assert -1 == data.addVoxel(Color.RED, new int[]{0,0,0});
        assert data.removeVoxel(id1);
        assert data.getVoxel(id1) == null;
        data.undoV();
        assert data.getVoxel(id1).id == id1;
        int lid2 = data.createLayer("layer2");
        data.deleteLayer(lid1);
        assert data.getVoxel(id1) == null;
        data.undoV();
        assert data.getVoxel(id1).id == id1;
        assert data.addVoxel(Color.RED, new int[]{0,0,0}) == -1;
        data.selectLayer(lid2);
        int id2 = data.addVoxel(Color.RED, new int[]{0,0,0});
        assert id2 > -1;
        assert data.getVoxel(id2).id == id2;
        int id3 = data.addVoxel(Color.GREEN,  new int[]{5,0,0});
        data.deleteLayer(lid2);
        assert data.getVoxel(id3) == null;
        data.undoV();
        assert data.getVoxel(id3).id == id3;
    }

    @Test
    public void testMoveVoxel() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.createLayer("layer2");
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.RED, new int[]{5,6,7});
        int id2 = data.addVoxel(Color.RED, new int[]{7,8,9});
        data.moveVoxel(id2, new int[]{5,6,7});
        assert data.getVoxel(id1) == null;
        data.undoV();
        assert data.getVoxel(id1) != null;
        data.undoV();
        boolean moved = data.moveVoxel(id1, new int[]{3,1,2});
        assert moved;
        assert data.getVoxel(id1).getPosAsInt()[0] == 3;
        data.undoV();
        assert data.getVoxel(id1).getPosAsInt()[0] == 5;
        data.redoV();
        assert data.getVoxel(id1).getPosAsInt()[2] == 2;
    }

    @Test
    public void testColorVoxel() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.RED, new int[] {0,0,0});
        assert data.getColor(id1) == Color.RED;
        data.setColor(id1, Color.GREEN);
        assert data.getColor(id1) == Color.GREEN;
        data.undoV();
        assert data.getColor(id1) == Color.RED;
        data.redoV();
        assert data.getColor(id1) == Color.GREEN;

    }

    @Test
    public void testAlphaVoxel() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.RED, new int[] {0,0,0});
        assert data.getAlpha(id1) == -1;
        data.setAlpha(id1, 10);
        assert data.getAlpha(id1) == 10;
        data.undoV();
        assert data.getAlpha(id1) == -1;
        data.redoV();
        assert data.getAlpha(id1) == 10;

    }

    @Test
    public void testClearRange() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.RED, new int[] {6,4,3});
        int id2 = data.addVoxel(Color.RED, new int[] {4,4,4});
        int id3 = data.addVoxel(Color.RED, new int[] {5,4,4});
        int id4 = data.addVoxel(Color.RED, new int[] {6,5,2});
        data.clearRange(new int[]{6,4,3}, 1);
        assert data.getVoxel(id1) == null;
        assert data.getVoxel(id2).id == id2;
        assert data.getVoxel(id3) == null;
        assert data.getVoxel(id4) == null;
        data.undoV();
        assert data.getVoxel(id1).id == id1;
        assert data.getVoxel(id2).id == id2;
        assert data.getVoxel(id3).id == id3;
        assert data.getVoxel(id4).id == id4;
        data.redoV();
        assert data.getVoxel(id1) == null;
        assert data.getVoxel(id2).id == id2;
        assert data.getVoxel(id3) == null;
        assert data.getVoxel(id4) == null;
    }

    @Test
    public void testFillRange() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.selectLayer(lid1);
        data.fillRange(new int[]{6,5,3}, 3, Color.RED);
        assert !data.fillRange(new int[]{6,5,3}, 3, Color.RED);
        assert data.getLayerVoxels(lid1).length == 343;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 0;
        data.redoV();
        data.clearRange(new int[]{6,5,3},2);
        assert data.getLayerVoxels(lid1).length == 218;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 343;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 0;
        data.redoV();
        assert data.getLayerVoxels(lid1).length == 343;
        data.redoV();
        assert data.getLayerVoxels(lid1).length == 218;
        data.fillRange(new int[]{6,8,3}, 2, Color.GREEN);
        int colGreen = 0;
        for (Voxel voxel : data.getLayerVoxels(lid1)) {
            colGreen += (voxel.getColor() == Color.GREEN ? 1 : 0);
        }
        assert colGreen == 100;
    }

    @Test
    public void testClear() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.selectLayer(lid1);
        data.fillRange(new int[]{6,5,3}, 3, Color.RED);
        assert data.getLayerVoxels(lid1).length == 343;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 0;
        data.redoV();
        data.clearV(lid1);
        assert data.getLayerVoxels(lid1).length == 0;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 343;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 0;
        data.redoV();
        data.redoV();
        assert data.getLayerVoxels(lid1).length == 0;
        data.undoV();
        assert data.getLayerVoxels(lid1).length == 343;
    }

    @Test
    public void testMergeLayers() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.RED, new int[] {1,1,1});
        int id2 = data.addVoxel(Color.GREEN, new int[] {1,1,2});
        data.selectLayer(lid2);
        int id3 = data.addVoxel(Color.ORANGE, new int[] {1,1,1});
        data.mergeVisibleLayers();
        assert data.getLayers().length == 1;
        assert data.getVoxel(id1) == null;
        assert data.getVoxel(id2) == null;
        assert data.getVoxel(id3) == null;
        Voxel[] voxel = data.getLayerVoxels(data.getSelectedLayer());
        assert voxel.length == 2;
        assert (voxel[1].getPosAsInt()[2] == 2 && voxel[1].getColor() == Color.GREEN);
    }

    @Test
    public void testGetVoxelSlice() throws Exception {
        int lid1 = data.createLayer("layer1");
        data.selectLayer(lid1);
        assert !data.mergeVisibleLayers();
        data.addVoxel(Color.RED, new int[] {0,0,0});
        data.addVoxel(Color.RED, new int[] {5,2,0});
        data.addVoxel(Color.RED, new int[] {4,-10,0});
        data.addVoxel(Color.RED, new int[] {4,-10,2});
        assert data.getVoxelsXY(0).length == 3;
        assert data.getVoxelsXY(1).length == 0;
        assert data.getVoxelsXY(2).length == 1;
    }

    // tests for layers

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
        data.createLayer("layer2");
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
        assert !data.canRedoV();
        assert !data.canUndoV();
        int lid1 = data.createLayer("layer1");
        assert !data.canRedoV();
        assert data.canUndoV();
        data.undoV();
        assert data.canRedoV();
        assert !data.canUndoV();
        data.redoV();
        assert !data.canRedoV();
        assert data.canUndoV();
        data.deleteLayer(lid1);
        assert !data.canRedoV();
        assert data.canUndoV();
        data.undoV();
        assert data.canRedoV();
        assert data.canUndoV();
        data.undoV();
        assert data.canRedoV();
        assert !data.canUndoV();
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
        assert data.getLayerVisible(lid1);
        data.setVisible(lid1, false);
        assert !data.getLayerVisible(lid1);
        data.setVisible(lid1, true);
        assert data.getLayerVisible(lid1);
        data.undoV();
        assert !data.getLayerVisible(lid1);
        data.undoV();
        assert data.getLayerVisible(lid1);
        data.redoV();
        assert !data.getLayerVisible(lid1);
        data.redoV();
        assert data.getLayerVisible(lid1);
        data.setVisible(lid1, true); // this does not create a history entry
        data.undoV();
        assert !data.getLayerVisible(lid1);
    }

    @Test
    public void testMoveLayerUpDownAndCanMoveLayerUpDown() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        int lid3 = data.createLayer("layer3");
        assert data.canMoveLayerUp(lid1);
        assert data.canMoveLayerUp(lid2);
        assert !data.canMoveLayerUp(lid3);
        assert data.canMoveLayerDown(lid3);
        assert data.canMoveLayerDown(lid2);
        assert !data.canMoveLayerDown(lid1);
        data.moveLayerDown(lid1);
        data.undoV();
        assert data.getLayerName(lid3) == null;
        data.redoV();
        data.moveLayerUp(lid1);
        assert data.getLayerNames()[0].equals("layer3");
        assert data.getLayerNames()[1].equals("layer1");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2);
        assert data.canMoveLayerUp(lid1);
        assert !data.canMoveLayerUp(lid3);
        assert data.canMoveLayerDown(lid3);
        assert data.canMoveLayerDown(lid1);
        assert !data.canMoveLayerDown(lid2);
        data.moveLayerUp(lid1);
        assert data.getLayerNames()[0].equals("layer1");
        assert data.getLayerNames()[1].equals("layer3");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2);
        assert !data.canMoveLayerUp(lid1);
        assert data.canMoveLayerUp(lid3);
        assert data.canMoveLayerDown(lid3);
        assert data.canMoveLayerDown(lid1);
        assert !data.canMoveLayerDown(lid2);
        data.deleteLayer(lid1);
        data.undoV();
        data.undoV();
        assert data.getLayerNames()[0].equals("layer3");
        assert data.getLayerNames()[1].equals("layer1");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2);
        assert data.canMoveLayerUp(lid1);
        assert !data.canMoveLayerUp(lid3);
        assert data.canMoveLayerDown(lid3);
        assert data.canMoveLayerDown(lid1);
        assert !data.canMoveLayerDown(lid2);
        data.undoV();
        assert data.canMoveLayerUp(lid1);
        assert data.canMoveLayerUp(lid2);
        assert !data.canMoveLayerUp(lid3);
        assert data.canMoveLayerDown(lid3);
        assert data.canMoveLayerDown(lid2);
        assert !data.canMoveLayerDown(lid1);
        data.redoV();
        data.redoV();
        assert data.getLayerNames()[0].equals("layer1");
        assert data.getLayerNames()[1].equals("layer3");
        assert data.getLayerNames()[2].equals("layer2");
        assert data.canMoveLayerUp(lid2);
        assert !data.canMoveLayerUp(lid1);
        assert data.canMoveLayerUp(lid3);
        assert data.canMoveLayerDown(lid3);
        assert data.canMoveLayerDown(lid1);
        assert !data.canMoveLayerDown(lid2);
    }

    @Test
    public void testMigrateSelection() throws Exception {
        int lid1 = data.createLayer("layer1");
        int lid2 = data.createLayer("layer2");
        int lid3 = data.createLayer("layer2");
        data.selectLayer(lid1);
        int id1 = data.addVoxel(Color.BLACK, new int[]{1,2,3});
        int id2 = data.addVoxel(Color.GREEN, new int[]{1,2,4});
        data.selectLayer(lid2);
        int id3 = data.addVoxel(Color.ORANGE, new int[]{1,1,3});
        int id4 = data.addVoxel(Color.WHITE, new int[]{1,2,4});
        data.selectLayer(lid2);
        data.addVoxel(Color.BLUE, new int[]{1,1,3});
        data.addVoxel(Color.GRAY, new int[]{1,2,4});
        data.massSetVoxelSelected(new Integer[]{id1, id2, id3, id4}, true);
        data.setVisible(lid3, false);
        data.migrateVoxels(data.getSelectedVoxels());
        assert data.searchVoxel(new int[]{1,2,3}, true).getColor().equals(Color.BLACK);
        assert data.searchVoxel(new int[]{1,1,3}, true).getColor().equals(Color.ORANGE);
        assert data.searchVoxel(new int[]{1,2,4}, true).getColor().equals(Color.WHITE);
        // todo test undo/redo of this
    }

    // big final test
    @Test
    public void randomeMess() throws Exception {
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

            public int[] randPos() {
                return new int[]{rand.nextInt()%20, rand.nextInt()%20, rand.nextInt()%20};
            }

            public Color randCol() {
                return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
            }

            // XXXXXXXXXXXXXXXXXX

            public void createLayer() {
                int layercount = data.getLayers().length;
                data.createLayer(new BigInteger(130, rand).toString(32));
                assert layercount+1 == data.getLayers().length;
            }

            public void deleteLayer() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    int layercount = data.getLayers().length;
                    data.deleteLayer(layers[rem]);
                    assert layercount-1 == data.getLayers().length;
                }
            }

            public void renameLayer() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    String name = data.getLayerName(layers[rem]);
                    data.renameLayer(layers[rem], new BigInteger(130, rand).toString(32));
                    assert !name.equals(data.getLayerName(layers[rem])); // very unlikely that this fails!
                }
            }

            public void selectLayer() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.selectLayer(layers[rem]);
                    assert ((Integer)data.getSelectedLayer()).equals(layers[rem]);
                }
            }

            public void setVisible() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    boolean vis = rand.nextBoolean();
                    data.setVisible(layers[rem], vis);
                    assert data.getLayerVisible(layers[rem]) == vis;
                }
            }

            public void moveLayerUp() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    Integer[] layersIds = data.getLayers();
                    boolean moved = data.moveLayerUp(layers[rem]);
                    if (moved) {
                        assert data.getLayers() != layersIds;
                    }
                }
            }

            public void moveLayerDown() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    Integer[] layersIds = data.getLayers();
                    boolean moved = data.moveLayerDown(layers[rem]);
                    if (moved) {
                        assert data.getLayers() != layersIds;
                    }
                }
            }

            public void addVoxel() {
                if (data.getSelectedLayer() != -1) {
                    int voxCount = data.getLayerVoxels(data.getSelectedLayer()).length;
                    int id1 = data.addVoxel(randCol(), randPos());
                    if (id1 != -1) {
                        assert voxCount+1 == data.getLayerVoxels(data.getSelectedLayer()).length;
                    }
                }
            }

            public void deleteVoxel() {
                if ( data.getSelectedLayer() != -1) {
                    Voxel[] voxels = data.getLayerVoxels(data.getSelectedLayer());
                    if (voxels.length > 0) {
                        int voxCount = data.getLayerVoxels(data.getSelectedLayer()).length;
                        int rem = rand.nextInt(voxels.length);
                        data.removeVoxel(voxels[rem].id);
                        assert voxCount-1 == data.getLayerVoxels(data.getSelectedLayer()).length;
                    }
                }
            }

            public void moveVoxel() {
                if ( data.getSelectedLayer() != -1) {
                    Voxel[] voxels = data.getLayerVoxels(data.getSelectedLayer());
                    if (voxels.length > 0) {
                        int rem = rand.nextInt(voxels.length);
                        int[] newPos = randPos();
                        if (data.moveVoxel(voxels[rem].id, newPos)) {
                            assert data.getVoxel(voxels[rem].id).getPosAsInt()[2] == newPos[2];
                        }
                    }
                }
            }

            public void colorVoxel() {
                if ( data.getSelectedLayer() != -1) {
                    Voxel[] voxels = data.getLayerVoxels(data.getSelectedLayer());
                    if (voxels.length > 0) {
                        int rem = rand.nextInt(voxels.length);
                        Color col = randCol();
                        data.setColor(voxels[rem].id, col);
                        assert data.getVoxel(voxels[rem].id).getColor() == col;
                    }
                }
            }

            public void alphaVoxel() {
                if ( data.getSelectedLayer() != -1) {
                    Voxel[] voxels = data.getLayerVoxels(data.getSelectedLayer());
                    if (voxels.length > 0) {
                        int rem = rand.nextInt(voxels.length);
                        int alpha = rand.nextInt();
                        data.setAlpha(voxels[rem].id, alpha);
                        assert alpha == data.getVoxel(voxels[rem].id).getAlpha();
                    }
                }
            }

            public void clearRange() {
                if ( data.getSelectedLayer() != -1) {
                    int voxCount = data.getLayerVoxels(data.getSelectedLayer()).length;
                    if (data.clearRange(randPos(),rand.nextInt()%3)) {
                        assert voxCount > data.getLayerVoxels(data.getSelectedLayer()).length;
                    }
                }
            }

            public void fillRange() {
                if ( data.getSelectedLayer() != -1) {
                    int voxCount = data.getLayerVoxels(data.getSelectedLayer()).length;
                    if (data.fillRange(randPos(),rand.nextInt()%3, randCol())) {
                        assert voxCount < data.getLayerVoxels(data.getSelectedLayer()).length;
                    }
                }
            }

            public void clear() {
                if ( data.getSelectedLayer() != -1) {
                    data.clearV(data.getSelectedLayer());
                    assert data.getLayerVoxels(data.getSelectedLayer()).length == 0;
                }
            }

            public void selectVoxel() {
                if ( data.getSelectedLayer() != -1) {
                    Voxel[] voxels = data.getLayerVoxels(data.getSelectedLayer());
                    if (voxels.length > 0) {
                        int rem = rand.nextInt(voxels.length);
                        boolean bool = rand.nextBoolean();
                        data.setVoxelSelected(voxels[rem].id, bool);
                        assert bool == data.getVoxel(voxels[rem].id).isSelected();
                    }
                }
            }

            public void selectLayerSoft() {
                Integer[] layers = data.getLayers();
                if (layers.length > 0) {
                    int rem = rand.nextInt(layers.length);
                    data.selectLayerSoft(layers[rem]);
                    assert ((Integer)data.getSelectedLayer()).equals(layers[rem]);
                }
            }

            public void mergeLayers() {
                data.mergeVisibleLayers();
            }

            public void migrateSelection() {
                data.migrateVoxels(data.getSelectedVoxels());
            }

            public void massSelect() {
                ArrayList<Integer> voxelIds = new ArrayList<Integer>();
                for (int layerId : data.getLayers()) {
                    Voxel[] voxels = data.getLayerVoxels(layerId);
                    if (voxels.length > 0) {
                        int count = rand.nextInt(voxels.length);
                        for (int i = 0; i < count; i++) {
                            int pos = rand.nextInt(voxels.length);
                            voxelIds.add(voxels[pos].id);
                        }
                    }
                }
                Integer[] voxelIdsStatic = new Integer[voxelIds.size()];
                voxelIds.toArray(voxelIdsStatic);
                data.massSetVoxelSelected(voxelIdsStatic, rand.nextBoolean());
            }

            public void massRemove() {
                Voxel[] voxels = data.getSelectedVoxels();
                Integer[] voxelIds = new Integer[voxels.length];
                int i = 0;
                for (Voxel voxel : voxels) {
                    voxelIds[i++] = voxel.id;
                }
                data.massRemoveVoxel(voxelIds);
            }

            public void massAdd() {
                int length = rand.nextInt(20);
                Voxel[] voxel = new Voxel[length];
                int layerId = data.getSelectedLayer();
                for (int i = 0; i < length; i++) {
                    voxel[i] = new Voxel(-1, randPos(), randCol(), layerId);
                }
                data.massAddVoxel(voxel);
            }

            public void massColor() {
                Voxel[] voxels = data.getSelectedVoxels();
                Integer[] voxelIds = new Integer[voxels.length];
                int i = 0;
                for (Voxel voxel : voxels) {
                    voxelIds[i++] = voxel.id;
                }
                data.massSetColor(voxelIds,randCol());
            }

            public void massMove() {
                data.massMoveVoxel(data.getSelectedVoxels(), randPos());
            }

            public void rotate() {
                data.rotateVoxel(data.getSelectedVoxels(), rand.nextInt(3), 90 * rand.nextInt(3));
            }

            public void mirror() {
                data.mirrorVoxel(data.getSelectedVoxels(),rand.nextInt(3));
            }
        }

        final int poss = 26;

        for (int seed = 93; seed < 2000; seed ++) {
            Util util = new Util(seed);
            float[] prob = new float[poss];
            for (int k = 0; k < prob.length; k++) {
                prob[k] = util.getFloat();
            }
            for (int k = 0; k < 20000; k++) {
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
                            util.setVisible();
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
                    case 8:
                        if (util.getFloat() < prob[7]) {
                            util.addVoxel();
                        }
                        break;
                    case 9:
                        if (util.getFloat() < prob[8]) {
                            util.deleteVoxel();
                        }
                        break;
                    case 10:
                        if (util.getFloat() < prob[9]) {
                            util.moveVoxel();
                        }
                        break;
                    case 11:
                        if (util.getFloat() < prob[10]) {
                            util.colorVoxel();
                        }
                        break;
                    case 12:
                        if (util.getFloat() < prob[11]) {
                            util.alphaVoxel();
                        }
                        break;
                    case 13:
                        if (util.getFloat() < prob[12]) {
                            util.clearRange();
                        }
                        break;
                    case 14:
                        if (util.getFloat() < prob[13]) {
                            util.fillRange();
                        }
                        break;
                    case 15:
                        if (util.getFloat() < prob[14]) {
                            util.clear();
                        }
                        break;
                    case 16:
                        if (util.getFloat() < prob[15]) {
                            util.selectLayerSoft();
                        }
                        break;

                    case 17:
                        if (util.getFloat() < prob[16]) {
                            util.mergeLayers();
                        }
                        break;
                    case 18:
                        if (util.getFloat() < prob[17]) {
                            util.migrateSelection();
                        }
                        break;
                    case 19:
                        if (util.getFloat() < prob[18]) {
                            util.massSelect();
                        }
                        break;
                    case 20:
                        if (util.getFloat() < prob[19]) {
                            util.massRemove();
                        }
                        break;
                    case 21:
                        if (util.getFloat() < prob[20]) {
                            util.massAdd();
                        }
                        break;
                    case 22:
                        if (util.getFloat() < prob[21]) {
                            util.massColor();
                        }
                        break;
                    case 23:
                        if (util.getFloat() < prob[22]) {
                            util.massMove();
                        }
                        break;
                    case 24:
                        if (util.getFloat() < prob[23]) {
                            util.rotate();
                        }
                        break;
                    case 25:
                        if (util.getFloat() < prob[24]) {
                            util.mirror();
                        }
                        break;
                    case 26:
                        if (util.getFloat() < prob[25]) {
                            util.selectVoxel();
                        }
                        break;
                }
//                if (data.getVoxel(20390) != null) {
//                    data.historyManagerV.debug();
////                    System.out.println(
////                            data.getVoxel(20390).getPosAsInt()[0] + " " +
////                                    data.getVoxel(20390).getPosAsInt()[1] + " " +
////                                    data.getVoxel(20390).getPosAsInt()[2]
////                    );
////                    data.moveVoxel(20390, new int[]{0,0,0});
////                    System.out.println(
////                            data.getVoxel(20390).getPosAsInt()[0] + " " +
////                                    data.getVoxel(20390).getPosAsInt()[1] + " " +
////                                    data.getVoxel(20390).getPosAsInt()[2]
////                    );
//                }
            }

            System.out.println(seed);

            // wind forward
            while (data.canRedoV()) {
                data.redoV();
            }

            // store
            Integer[] layers = data.getLayers();
            String[] layerNames = data.getLayerNames();
            Voxel[][] layerVoxel = new Voxel[layers.length][];
            int c = 0;
            for (int i : layers) {
                layerVoxel[c++] = data.getLayerVoxels(i);
            }

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
            Voxel[][] layerVoxelN = new Voxel[layers.length][];
            c = 0;
            for (int i : layers) {
                layerVoxelN[c++] = data.getLayerVoxels(i);
            }

            // check
            assert layersN.length == layers.length;
            assert layerNamesN.length == layers.length;
            assert layerNamesN.length == layerNames.length;

            for (int i = 0; i < layersN.length; i++) {
                assert layersN[i].equals(layers[i]);
                assert layerNamesN[i].equals(layerNames[i]);
            }

            for (int i = 0; i < layers.length; i++) {
                for (int j = 0; j < Math.max(layerVoxel[i].length, layerVoxelN[i].length); j++) {
                    assert layerVoxel[i][j].equals(layerVoxelN[i][j]);
                }
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
