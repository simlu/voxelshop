package com.vitco.app.export;

import com.vitco.app.core.data.Data;
import com.vitco.app.core.data.container.Voxel;
import com.vitco.app.layout.content.console.ConsoleInterface;
import com.vitco.app.settings.DynamicSettings;
import com.vitco.app.util.components.progressbar.ProgressDialog;
import com.vitco.app.util.file.FileOut;
import com.vitco.app.util.misc.ByteHelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Export cross-section slices of the voxel into
 * series of bitmap files.
 */
public class SlicesExporter extends AbstractExporter {

    // constructor
    public SlicesExporter(File exportTo, Data data, ProgressDialog dialog, ConsoleInterface console) throws IOException {
        super(exportTo, data, dialog, console);
    }

    

	int ax1 = 0;
	int ax2 = 2;
	int ax3 = 1;
    public void setSliceDirection(String sliceAxis) {
        if(sliceAxis.equals("z")) {
        	ax1 = 1;
        	ax2 = 0;
        	ax3 = 2;
        }
        else if(sliceAxis.equals("y")) {
        	ax1 = 2;
        	ax2 = 0;
        	ax3 = 1;
        }
    }
    
    private String exportFormat = "png";
	public void setExportFormat(String format) {
		this.exportFormat = format;
	}
	
	private boolean invertOrder = false;
	public void setInvertOrder(boolean invert) {
		this.invertOrder = invert;
	}
	
	
	/**
	 * This method is created to avoid the creation of the exporTo file through
	 * the Exporter prototype. As this exporter generates a series of files
	 * and does not comply with the exporter prototype which assumes exporters
	 * only generate a single file. 
	 * This solution is not ideal, however it avoids changing code that may affect
	 * other exporters.
	 * @return success
	 * @throws IOException
	 */
	public boolean generateImages() throws IOException {
		return writeFile();
	}

    // write the file
    @Override
    protected boolean writeFile() throws IOException {
    	Integer[] layers = data.getLayers();

    	
    	// compute minimum and maximum coordinates accross all layers
        int[] min = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int[] max = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
    	for (int i = layers.length - 1; i >= 0; i--) {
            Integer layerId = layers[i];
            int[][] meta = get_meta(layerId);

            min = minimimum(min, meta[0]);
            max = maximum(max, meta[1]);
    	}
    	
    	// Calculate number of slices and dimensions
        int n_slices = max[ax1] - min[ax1] + 1; // number of slices
        int width = max[ax2] - min[ax2] + 1; // width of slice bitmap
        int height = max[ax3] - min[ax3] + 1;  // height of slcie bitmap
        
        
        // create all images for each slice 
        // (necessary to pre generate so all layers can be merged)
        BufferedImage[] slices = new BufferedImage[n_slices];
        for(int i=0; i<n_slices; i++)
        	slices[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	
        // generate images (merged for all layers)
    	for (int i = layers.length - 1; i >= 0; i--) {
            for (int x = min[ax1]; x <= max[ax1]; x++) {
            	for (int y = max[ax2]; y >= min[ax2]; y--) {
                    for (int z = min[ax3]; z <= max[ax3]; z++) {                    	
                        Voxel voxel = getVoxel(x,y,z, layers[i]);
                        
                        // ignore transparent pixels
                        if (voxel == null) continue;
                        slices[x - min[ax1]].setRGB(y - min[ax2], z-min[ax3], voxel.getColor().getRGB());  
                    }
                }
            }
        }
    	
    	// Save all the image files
    	for(int i=0; i<slices.length; i++) {
    		int n = i + 1;	
    		if(invertOrder) n = slices.length-i;
    		
    		String fileName = String.format("%s_%d.%s", exportTo.getAbsolutePath(), n, exportFormat);
    		System.out.println("Creating file: " + fileName);
    		ImageIO.write(slices[i], exportFormat, new File(fileName) ); 
    	}
    	
        return true; // success
    }

    private int[] maximum(int[] max, int[] is) {
		for(int i=0; i<max.length; i++) { 
			max[i] = Math.max(max[i], is[i]);
		}
		return max;
	}

	private int[] minimimum(int[] min, int[] is) {
		for(int i=0; i<min.length; i++) { 
			min[i] = Math.min(min[i], is[i]);
		}
		return min;
	}

	private Voxel getVoxel(int a, int b, int c, int layerId) {
    	int x = 0, y = 0, z =0;
    	if(ax1 == 0)
    		x = a;
    	else if(ax1 == 1)
    		y = a;
    	else if(ax1 == 2)
    		z = a;

    	if(ax2 == 0)
    		x = b;
    	else if(ax2 == 1)
    		y = b;
    	else if(ax2 == 2)
    		z = b;
    	
    	if(ax3 == 0)
    		x = c;
    	else if(ax3 == 1)
    		y = c;
    	else if(ax3 == 2)
    		z = c;
    	
    	
    	return data.searchVoxel(new int[]{x, y, z}, layerId);
	}

	private int[][] get_meta(int layerId) {
    	int[] min, max, size;
    	
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
        return new int[][] {
                min, max, size
        };
    }

}
