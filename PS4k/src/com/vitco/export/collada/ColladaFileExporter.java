package com.vitco.export.collada;

import com.vitco.export.generic.ExportDataManager;
import com.vitco.export.generic.container.TexTriangleManager;
import com.vitco.export.generic.container.TriTexture;
import com.vitco.export.generic.container.TriTextureManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.components.progressbar.ProgressDialog;
import com.vitco.util.components.progressbar.ProgressReporter;
import com.vitco.util.file.FileTools;
import com.vitco.util.misc.DateTools;
import com.vitco.util.xml.XmlFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Export data to COLLADA file ( with optional settings )
 */
public class ColladaFileExporter extends ProgressReporter {
    // contains the data that will be used to write this collada file
    private final ExportDataManager exportDataManager;

    // the xml Collada file that we're building
    private final XmlFile xmlFile = new XmlFile("COLLADA");

    // prefix for the texture files (this should include the file name to prevent
    // overwriting of textures that belong to different files)
    private final String texturePrefix;

    // constructor
    public ColladaFileExporter(ProgressDialog dialog, ExportDataManager exportDataManager, String texturePrefix, String name) {
        super(dialog);
        this.exportDataManager = exportDataManager;
        this.texturePrefix = texturePrefix;
        // initialize the xml file
        setActivity("Creating File Data...", true);
        initXmlFile();
        // create the object in the scene
        setActivity("Creating Objects...", true);
        writeObject(name);
        // write the texture information
        setActivity("Creating Textures...", true);
        writeTextures();
        // write the mesh + uv of the object (triangles)
        setActivity("Creating Coordinates and UVs...", true);
        writeCoordinates();
    }

    // create the object in the scene
    private void writeObject(String name) {
        // create the object
        xmlFile.resetTopNode("library_visual_scenes/visual_scene/node[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=" + name,
                "name=" + name,
                "type=NODE"
        });
        xmlFile.addAttrAndTextContent("translate", new String[]{"sid=location"},
                "0 0 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationZ"}, "0 0 1 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationY"}, "0 1 0 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationX"}, "1 0 0 0");
        // scale the object down
        xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.05 0.05 0.05");

        // add the material to the object
        xmlFile.setTopNode("instance_geometry[-1]");
        xmlFile.addAttributes("", new String[]{"url=#Plane-tex-mesh"});
    }

    // helper to register a texture image
    private void addTexture(int id) {
        // write the image to the library
        xmlFile.resetTopNode("library_images/image[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=" + texturePrefix + id + "-image",
                "name=" + texturePrefix + id + "-image"
        });
        xmlFile.addTextContent("init_from",  texturePrefix + id + ".png");

        // create texture reference in object
        xmlFile.resetTopNode("library_visual_scenes/visual_scene/node/instance_geometry" +
                "/bind_material/technique_common/instance_material[-1]");
        xmlFile.addAttributes("", new String[]{
                "symbol=lambert" + id + "-material",
                "target=#lambert" + id + "-material"
        });
        // add the uv mapping
        xmlFile.addAttributes("bind_vertex_input", new String[]{
                "semantic=TEX0",
                "input_semantic=TEXCOORD",
                "input_set=0"
        });

        // add the material
        xmlFile.resetTopNode("library_materials/material[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=lambert" + id + "-material",
                "name=lambert" + id
        });
        xmlFile.addAttributes("instance_effect", new String[]{
                "url=#lambert" + id + "-fx"
        });

        // write the image effect
        xmlFile.resetTopNode("library_effects/effect[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=lambert" + id + "-fx"
        });
        xmlFile.setTopNode("profile_COMMON");
        // ----
        xmlFile.setTopNode("newparam[-1]");
        xmlFile.addAttributes("", new String[]{"sid=" + texturePrefix + id + "-surface"});
        xmlFile.addAttributes("surface", new String[]{"type=2D"});
        xmlFile.addTextContent("surface/init_from",  texturePrefix + id + "-image");
        // ----
        xmlFile.goUp();
        xmlFile.setTopNode("newparam[-1]");
        xmlFile.addAttributes("", new String[]{"sid=" + texturePrefix + id + "-sampler"});
        xmlFile.addTextContent("sampler2D/source", texturePrefix + id + "-surface");
        // ----
        xmlFile.goUp();
        xmlFile.setTopNode("technique[-1]");
        xmlFile.addAttributes("", new String[]{"sid=common"});
        xmlFile.addTextContent("lambert/emission/color", "0 0 0 1");
        xmlFile.addTextContent("lambert/ambient/color", "0 0 0 1");
        xmlFile.addAttributes("lambert/diffuse/texture", new String[]{
                "texture=" + texturePrefix + id + "-sampler",
                "texcoord=TEX0"
        });
    }

    // write the different textures
    private void writeTextures() {
        // obtain all texture ids
        int[][] textureIds = exportDataManager.getTriangleManager().getTextureIds();
        // write texture file names to xml file
        for (int[] textureId : textureIds) {
            addTexture(textureId[0]);
        }
    }

    // write the coordinates
    private void writeCoordinates() {
        TexTriangleManager texTriangleManager = exportDataManager.getTriangleManager();

        // reset top node
        xmlFile.resetTopNode("library_geometries/geometry[-1]");
        xmlFile.addAttributes("", new String[] {
                "id=Plane-tex-mesh",
                "name=Plane-tex"
        });
        xmlFile.setTopNode("mesh");

        // Plane-tex-mesh-positions
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-tex-mesh-positions"
        });
        xmlFile.addAttrAndTextContent("source[0]/float_array",
                new String[]{
                        "id=Plane-tex-mesh-positions-array",
                        "count=" + texTriangleManager.getUniquePointCount() * 3},
                texTriangleManager.getUniquePointString(true));
        xmlFile.addAttributes("source[0]/technique_common/accessor", new String[]{
                "source=#Plane-tex-mesh-positions-array",
                "count=" + texTriangleManager.getUniquePointCount(),
                "stride=3"
        });
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=X", "type=float"});
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=Y", "type=float"});
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=Z", "type=float"});

        // --- write the uvs
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-tex-mesh-uvs"
        });
        xmlFile.addAttrAndTextContent("source[1]/float_array",
                new String[]{
                        "id=Plane-tex-mesh-uvs-array",
                        "count=" + (texTriangleManager.getUniqueUVCount() * 2)},
                texTriangleManager.getUniqueUVString(false));

        xmlFile.addAttributes("source[1]/technique_common/accessor", new String[]{
                "source=#Plane-tex-mesh-uvs-array",
                "count=" + texTriangleManager.getUniqueUVCount(),
                "stride=2"
        });
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=S", "type=float"});
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=T", "type=float"});

        // vertices (generic information)
        xmlFile.addAttributes("vertices", new String[] {
                "id=Plane-tex-mesh-vertices"
        });
        xmlFile.addAttributes("vertices/input", new String[] {
                "semantic=POSITION",
                "source=#Plane-tex-mesh-positions"
        });

        // write one poly list for each texture
        for (int[] groupId : texTriangleManager.getTextureIds()) {

            // write data
            xmlFile.resetTopNode("library_geometries/geometry/mesh/polylist[-1]");
            xmlFile.addAttributes("", new String[]{
                    "material=lambert" + groupId[0] + "-material",
                    "count=" + groupId[1]
            });
            xmlFile.addAttributes("input[-1]", new String[]{
                    "semantic=VERTEX",
                    "source=#Plane-tex-mesh-vertices",
                    "offset=0"
            });
            xmlFile.addAttributes("input[-1]", new String[]{
                    "semantic=TEXCOORD",
                    "source=#Plane-tex-mesh-uvs",
                    "offset=1",
                    "set=0"
            });
            // fill with 3s (each triangle consists of three points)
            xmlFile.addTextContent("vcount", new String(new char[groupId[1]]).replace("\0", " 3").substring(1));
            xmlFile.addTextContent("p", texTriangleManager.getTrianglePolygonList(groupId[0]));

        }
    }

    // -----------------------

    // initialize the XML file
    private void initXmlFile() {
        // basic information
        xmlFile.addAttributes("", new String[] {
                "xmlns=http://www.collada.org/2005/11/COLLADASchema",
                "version=1.4.1"});

        // basic information
        xmlFile.resetTopNode("asset");
        xmlFile.addTextContent("contributor/author", "VoxelShop User");
        xmlFile.addTextContent("contributor/authoring_tool", "VoxelShop V" + VitcoSettings.VERSION_ID);
        String now = DateTools.now("yyyy-MM-dd'T'HH:mm:ss");
        xmlFile.addTextContent("created", now);
        xmlFile.addTextContent("modified", now);
        xmlFile.addAttributes("unit", new String[] {"name=meter","meter=1"});
        // blender default (can not be changed since blender ignores it!)
        xmlFile.addTextContent("up_axis", "Z_UP");

        // =========================

        // will hold the used images (library)
        // (this will be deleted later on if there is no
        // texture image used for this dae file)
        xmlFile.resetTopNode("library_images");

        // will hold all the color "effect" information
        xmlFile.resetTopNode("library_effects");

        // will contain the materials linked to the color information
        xmlFile.resetTopNode("library_materials");

        // will contain geometries (mesh) of the object
        xmlFile.resetTopNode("library_geometries");

        // =========================

        // contains the scene
        xmlFile.resetTopNode("library_visual_scenes/visual_scene");
        xmlFile.addAttributes("", new String[] {
                "id=Scene",
                "name=Scene"
        });

        // link the library_visual_scenes node
        xmlFile.resetTopNode("scene");
        xmlFile.addAttributes("instance_visual_scene", new String[]{"url=#Scene"});
    }

    // save this file
    public boolean writeToFile(File file, ErrorHandlerInterface errorHandler) {
        return xmlFile.writeToFile(file, errorHandler);
    }

    // write texture files
    public boolean writeTexturesToFolder(File folder, ErrorHandlerInterface errorHandler) {
        int[][] textureIds = exportDataManager.getTriangleManager().getTextureIds();
        TriTextureManager triTextureManager = exportDataManager.getTextureManager();
        try {
            for (int[] textureId : textureIds) {
                TriTexture texture = triTextureManager.getTexture(textureId[0]);
                BufferedImage textureImage = texture.getImage();
                ImageIO.write(textureImage, "png", new File(
                        FileTools.ensureTrailingSeparator(folder.getAbsolutePath()) + texturePrefix + textureId[0] + ".png"
                        ));
            }
            return true;
        } catch (IOException e) {
            errorHandler.handle(e);
        }
        return false;
    }

}
