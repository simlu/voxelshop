package com.vitco.export;

import com.vitco.core.data.Data;
import com.vitco.core.data.container.Voxel;
import com.vitco.layout.content.console.ConsoleInterface;
import com.vitco.settings.DynamicSettings;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.file.FileOut;
import com.vitco.util.misc.ByteHelper;

import java.io.File;
import java.io.IOException;

/**
 * Exporter into *.qb (Qubicle 1.0)
 */
public class QbExporter extends AbstractExporter {

    // constructor
    public QbExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    // decide whether to use weighted center or origin (default)
    private boolean useCompression = true;
    public void setUseCompression(boolean flag) {
        useCompression = flag;
    }

    private boolean useBoxAsMatrix = false;
    public void setUseBoxAsMatrix(boolean useBoxAsMatrix) {
        this.useBoxAsMatrix = useBoxAsMatrix;
    }

    private boolean useOriginAsZero = true;
    public void setUseOriginAsZero(boolean useOriginAsZero) {
        this.useOriginAsZero = useOriginAsZero;
    }

    private boolean useVisMaskEncoding = true;
    public void setUseVisMaskEncoding(boolean useVisMaskEncoding) {
        this.useVisMaskEncoding = useVisMaskEncoding;
    }

    private boolean useRightHandedZAxisOrientation = true;
    public void setUseRightHandedZAxisOrientation(boolean useRightHandedZAxisOrientation) {
        this.useRightHandedZAxisOrientation = useRightHandedZAxisOrientation;
    }

    private static final int CODE_FLAG = 2;
    private static final int NEXT_SLICE_FLAG = 6;
    private static final int TRANSPARENT_VOXEL = 0x00000000;

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
        // version
        fileOut.writeIntRev(257);

        // color format
        fileOut.writeIntRev(0);

        // z axis orientation
        fileOut.writeIntRev(this.useRightHandedZAxisOrientation ? 1 : 0);
        int ax1 = this.useRightHandedZAxisOrientation ? 0 : 2;
        int ax2 = this.useRightHandedZAxisOrientation ? 2 : 0;

        // compressed
        fileOut.writeIntRev(this.useCompression ? 1 : 0);

        // vis mask encoding
        fileOut.writeIntRev(this.useVisMaskEncoding ? 1 : 0);

        Integer[]  layers = data.getLayers();

        // num matrices
        fileOut.writeIntRev(layers.length);

        for (int i = layers.length - 1; i >= 0; i--) {
            Integer layerId = layers[i];

            // write layer name
            String layerName = data.getLayerName(layerId);
            fileOut.writeByte((byte) layerName.length());
            fileOut.writeASCIIString(layerName);

            int[][] meta = get_meta(layerId);
            int[] min = meta[0];
            int[] max = meta[1];
            int[] size = meta[2];

            // write size
            fileOut.writeIntRev(size[ax1]);
            fileOut.writeIntRev(size[1]);
            fileOut.writeIntRev(size[ax2]);

            // write minimum
            fileOut.writeIntRev(this.useOriginAsZero ? min[ax1] : 0);
            fileOut.writeIntRev(this.useOriginAsZero ? -max[1] : 0);
            fileOut.writeIntRev(this.useOriginAsZero ? min[ax2] : 0);

            int currentColor = TRANSPARENT_VOXEL;
            int count = 0;

            for (int c1 = min[ax2]; c1 <= max[ax2]; c1++) {
                for (int y = max[1]; y >= min[1]; y--) {
                    for (int c2 = min[ax1]; c2 <= max[ax1]; c2++) {
                        int x = this.useRightHandedZAxisOrientation ? c2 : c1;
                        int z = this.useRightHandedZAxisOrientation ? c1 : c2;
                        Voxel voxel = data.searchVoxel(new int[]{x, y, z}, layerId);
                        int newColor;
                        if (voxel == null) {
                            newColor = TRANSPARENT_VOXEL;
                        } else {
                            byte visible = 1;
                            if (this.useVisMaskEncoding) {
                                if (data.searchVoxel(new int[]{x - 1, y, z}, layerId) == null) {
                                    visible = ByteHelper.setBit(visible, this.useRightHandedZAxisOrientation ? 5 : 1);
                                }
                                if (data.searchVoxel(new int[]{x + 1, y, z}, layerId) == null) {
                                    visible = ByteHelper.setBit(visible, this.useRightHandedZAxisOrientation ? 6 : 2);
                                }
                                if (data.searchVoxel(new int[]{x, y + 1, z}, layerId) == null) {
                                    visible = ByteHelper.setBit(visible, 3);
                                }
                                if (data.searchVoxel(new int[]{x, y - 1, z}, layerId) == null) {
                                    visible = ByteHelper.setBit(visible, 4);
                                }
                                if (data.searchVoxel(new int[]{x, y, z - 1}, layerId) == null) {
                                    visible = ByteHelper.setBit(visible, this.useRightHandedZAxisOrientation ? 2 : 5);
                                }
                                if (data.searchVoxel(new int[]{x, y, z + 1}, layerId) == null) {
                                    visible = ByteHelper.setBit(visible, this.useRightHandedZAxisOrientation ? 1 : 6);
                                }
                            }
                            newColor = voxel.getColor().getRGB();
                            newColor = ((this.useVisMaskEncoding ? visible : 0xFF) << 24) | (newColor & 0x000000FF) << 16 | (newColor & 0x0000FF00) | (newColor & 0x00FF0000) >> 16;
                        }

                        if (this.useCompression) {
                            if (newColor != currentColor) {
                                this.writeColor(fileOut, count, currentColor);
                                count = 0;
                            }
                            currentColor = newColor;
                            count++;
                        } else {
                            fileOut.writeIntRev(newColor);
                        }
                    }
                }
                if (this.useCompression) {
                    this.writeColor(fileOut, count, currentColor);
                    count = 0;
                    fileOut.writeIntRev(NEXT_SLICE_FLAG);
                }
            }
        }
        // success
        return true;
    }

    private int[][] get_meta(int layerId) {
        int[] min, max, size;
        if (!useBoxAsMatrix) { // determine actual size by using the voxels
            min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
            max = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
            size = new int[]{0, 0, 0};
            boolean hasVoxel = false;
            for (Voxel voxel : data.getLayerVoxels(layerId)) {
                min[0] = Math.min(voxel.x, min[0]);
                min[1] = Math.min(voxel.y, min[1]);
                min[2] = Math.min(voxel.z, min[2]);
                max[0] = Math.max(voxel.x, max[0]);
                max[1] = Math.max(voxel.y, max[1]);
                max[2] = Math.max(voxel.z, max[2]);
                hasVoxel = true;
            }
            if (hasVoxel) {
                size = new int[]{max[0] - min[0] + 1, max[1] - min[1] + 1, max[2] - min[2] + 1};
            }
        } else { // use the bounding box as size
            min = new int[] {
                    DynamicSettings.VOXEL_PLANE_RANGE_X_NEG + 1,
                    -DynamicSettings.VOXEL_PLANE_SIZE_Y + 1,
                    DynamicSettings.VOXEL_PLANE_RANGE_Z_NEG + 1
            };
//            System.out.println(min[0] + ", " + min[1] + ", " + min[2]);
            max = new int[] {
                    DynamicSettings.VOXEL_PLANE_RANGE_X_POS - 1,
                    0,
                    DynamicSettings.VOXEL_PLANE_RANGE_Z_POS - 1
            };
//            System.out.println(max[0] + ", " + max[1] + ", " + max[2]);
            size = new int[]{max[0] - min[0] + 1, max[1] - min[1] + 1, max[2] - min[2] + 1};
//            System.out.println(size[0] + ", " + size[1] + ", " + size[2]);
        }
        return new int[][] {
                min, max, size
        };
    }

    // Helper to write color information
    private void writeColor(FileOut fileOut, int count, int currentColor) throws IOException {
        if (count == 1) {
            fileOut.writeIntRev(currentColor);
        } else if (count == 2) {
            fileOut.writeIntRev(currentColor);
            fileOut.writeIntRev(currentColor);
        } else if (count == 3) {
            fileOut.writeIntRev(currentColor);
            fileOut.writeIntRev(currentColor);
            fileOut.writeIntRev(currentColor);
        } else if (count > 3) {
            fileOut.writeIntRev(CODE_FLAG);
            fileOut.writeIntRev(count);
            fileOut.writeIntRev(currentColor);
        }
    }

}
