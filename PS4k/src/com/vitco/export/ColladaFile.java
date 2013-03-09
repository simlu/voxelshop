package com.vitco.export;

import com.vitco.res.VitcoSettings;
import com.vitco.util.DateTools;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.xml.XmlFile;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for easily exporting *.dae files.
 */
public class ColladaFile {

    // the xml Collada file
    private XmlFile xmlFile = new XmlFile("COLLADA");

    // holds all the used colors
    private final HashMap<String, Color> colors = new HashMap<String, Color>();
    // holds the planes
    private final HashMap<float[], Color> planes = new HashMap<float[], Color>();

    // internal - holds all the planes
    private void addPlane(float x, float y, float z, float rotx, float roty, float rotz, Color color) {
        colors.put(String.valueOf(color.getRGB()), color);
        planes.put(new float[]{x, y, z, rotx, roty, rotz}, color);
    }

    // add planes
    public void addPlane(float[] pos, int type, Color color) {
        // NOTE: Z AXIS IS ALWAYS UP (for blender at least)
        switch (type) {
            case 1:
                addPlane(-pos[0] + 0.5f, -pos[2], -pos[1], 0, 90, 0, color);
                break;
            case 0:
                addPlane(-pos[0] - 0.5f, -pos[2], -pos[1], 0, 90, 180, color);
                break;
            case 5:
                addPlane(-pos[0], -pos[2] + 0.5f, -pos[1], 180, 0, 90, color);
                break;
            case 4:
                addPlane(-pos[0], -pos[2] - 0.5f, -pos[1], 0, 0, 90, color);
                break;
            case 3:
                addPlane(-pos[0], -pos[2], -pos[1] + 0.5f, 0, 0, 0, color);
                break;
            case 2:
                addPlane(-pos[0], -pos[2], -pos[1] - 0.5f, 0, 180, 0, color);
                break;
        }
    }

    // constructor
    public ColladaFile() {
        // basic information
        xmlFile.addAttributes("", new String[] {
                "xmlns=http://www.collada.org/2005/11/COLLADASchema",
                "version=1.4.1"});

        // basic information
        xmlFile.resetTopNode("asset");
        xmlFile.addTextContent("contributor/author", "PS4k User");
        xmlFile.addTextContent("contributor/authoring_tool", VitcoSettings.VERSION_ID);
        String now = DateTools.now("yyyy-MM-dd'T'HH:mm:ss");
        xmlFile.addTextContent("created", now);
        xmlFile.addTextContent("modified", now);
        xmlFile.addAttributes("unit", new String[] {"name=meter","meter=1"});
        // blender default (can not be changed since blender ignores it!)
        xmlFile.addTextContent("up_axis", "Z_UP");

        xmlFile.resetTopNode("library_effects");

        xmlFile.resetTopNode("library_materials");

        // create the basic geometry
        xmlFile.resetTopNode("library_geometries");
        xmlFile.addAttributes("geometry", new String[] {
                "id=Plane-mesh",
                "name=Plane"
        });
        xmlFile.setTopNode("geometry/mesh");

        // Plane-mesh-positions
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-mesh-positions"
        });
        xmlFile.addAttrAndTextContent("source[0]/float_array",
                new String[]{
                        "id=Plane-mesh-positions-array",
                        "count=12"},
                "1 -1 0 -1 -1 0 1 1 0 -1 1 0");
        xmlFile.addAttributes("source[0]/technique_common/accessor", new String[]{
                "source=#Plane-mesh-positions-array",
                "count=4",
                "stride=3"
        });
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=X", "type=float"});
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=Y", "type=float"});
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=Z", "type=float"});

        // Plane-mesh-normals
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-mesh-normals"
        });
        xmlFile.addAttrAndTextContent("source[1]/float_array",
                new String[]{
                        "id=Plane-mesh-normals-array",
                        "count=3"},
                "0 0 1");
        xmlFile.addAttributes("source[1]/technique_common/accessor", new String[]{
                "source=#Plane-mesh-normals-array",
                "count=1",
                "stride=3"
        });
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=X", "type=float"});
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=Y", "type=float"});
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=Z", "type=float"});

        // vertices
        xmlFile.addAttributes("vertices", new String[] {
                "id=Plane-mesh-vertices"
        });
        xmlFile.addAttributes("vertices/input", new String[] {
                "semantic=POSITION",
                "source=#Plane-mesh-positions"
        });

        // polylist
        xmlFile.addAttributes("polylist", new String[] {
                "material=Material_R_G_B-material",
                "count=1"
        });
        xmlFile.addAttributes("polylist/input[-1]", new String[] {
                "semantic=VERTEX",
                "source=#Plane-mesh-vertices",
                "offset=0"
        });
        xmlFile.addAttributes("polylist/input[-1]", new String[] {
                "semantic=NORMAL",
                "source=#Plane-mesh-normals",
                "offset=1"
        });
        xmlFile.addTextContent("polylist/vcount", "4 ");
        xmlFile.addTextContent("polylist/p", "1 0 0 0 2 0 3 0");

        xmlFile.resetTopNode("library_visual_scenes");
        xmlFile.addAttributes("visual_scene", new String[]{
                "id=Scene",
                "name=Scene"
        });

        xmlFile.resetTopNode("scene");
        xmlFile.addAttributes("instance_visual_scene", new String[]{"url=#Scene"});
    }

    public final void finish() {
        // write all colors
        for (Color color : colors.values()) {
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
            String baseName = "Material_" + r + "_" + g + "_" + b;

            // add the effect
            xmlFile.resetTopNode("library_effects/effect[-1]");
            xmlFile.addAttributes("", new String[]{"id=" + baseName + "-effect"});
            xmlFile.setTopNode("profile_COMMON/technique");
            xmlFile.addAttributes("", new String[]{"sid=common"});
            xmlFile.setTopNode("lambert");
            xmlFile.addAttrAndTextContent("emission/color", new String[] {"sid=emission"}, "0 0 0 1");
            xmlFile.addAttrAndTextContent("ambient/color", new String[] {"sid=ambient"}, "0 0 0 1");
            xmlFile.addAttrAndTextContent("diffuse/color", new String[] {"sid=diffuse"},
                    r/(float)255 + " " + g/(float)255 + " " + b/(float)255 + " 1");
            xmlFile.addAttrAndTextContent("index_of_refraction/float", new String[] {"sid=index_of_refraction"}, "1");

            // add the material
            xmlFile.resetTopNode("library_materials/material[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=" + baseName + "-material",
                    "name=" + baseName
            });
            xmlFile.addAttributes("instance_effect", new String[] {
                    "url=#" + baseName + "-effect"
            });
        }

        // write all planes
        int id = 1;
        for (Map.Entry<float[], Color> plane : planes.entrySet()) {
            int r = plane.getValue().getRed();
            int g = plane.getValue().getGreen();
            int b = plane.getValue().getBlue();
            String baseName = "Material_" + r + "_" + g + "_" + b;

            xmlFile.resetTopNode("library_visual_scenes/visual_scene/node[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=Plane" + id,
                    "name=Plane" + id,
                    "type=NODE"
            });
            xmlFile.addAttrAndTextContent("translate", new String[]{"sid=location"},
                    plane.getKey()[0] + " " + plane.getKey()[1] + " " + plane.getKey()[2]);
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationZ"}, "0 0 1 " + plane.getKey()[3]);
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationY"}, "0 1 0 " + plane.getKey()[4]);
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationX"}, "1 0 0 " + plane.getKey()[5]);
            xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.5 0.5 1");

            xmlFile.addAttributes("instance_geometry", new String[] {"url=#Plane-mesh"});
            xmlFile.addAttributes("instance_geometry/bind_material/technique_common/instance_material", new String[] {
                    "symbol=" + baseName + "-material",
                    "target=#" + baseName + "-material"
            });

            id++;
        }
    }

    // save this file
    public boolean writeToFile(String filename, ErrorHandlerInterface errorHandler) {
        return xmlFile.writeToFile(filename, errorHandler);
    }

    // save this file
    public boolean writeToFile(File file, ErrorHandlerInterface errorHandler) {
        return xmlFile.writeToFile(file, errorHandler);
    }
}
