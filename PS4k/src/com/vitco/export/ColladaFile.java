package com.vitco.export;

import com.vitco.export.container.PlaneMaterial;
import com.vitco.export.container.UVPoint;
import com.vitco.export.container.Vertex;
import com.vitco.res.VitcoSettings;
import com.vitco.util.DateTools;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.xml.XmlFile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Wrapper class for easily exporting *.dae files.
 *
 * Note: The object that is exported is references to as "Plane"
 */
public class ColladaFile {

    // the xml Collada file
    private XmlFile xmlFile = new XmlFile("COLLADA");

    // holds all the used texture Ids and maps them to their texture images
    private final HashMap<Integer, ImageIcon> textureIds = new HashMap<Integer, ImageIcon>();

    // holds the vertices
    private final HashMap<String, Vertex> colorVertices = new HashMap<String, Vertex>();
    private final HashMap<String, Vertex> texVertices = new HashMap<String, Vertex>();

    // holds the planes (consisting of vertices) and maps them to their material
    private final HashMap<Vertex[], PlaneMaterial> colorPlanes = new HashMap<Vertex[], PlaneMaterial>();
    private final HashMap<Vertex[], PlaneMaterial> texPlanes = new HashMap<Vertex[], PlaneMaterial>();

    // holds all the used colors (color of textured voxels is not stored!)
    private final HashMap<String, Color> colors = new HashMap<String, Color>();

    // holds the texture image (if there are any textures)
    // "null iff there are no textures"
    private BufferedImage textureMap = null;

    // the id of the last assigned vertex (needed for plane adding)
    private int[] nextColorPlaneId = new int[]{0};
    private int[] nextTexPlaneId = new int[]{0};

    // internal - holds all the planes
    private void addPlane(float x, float y, float z, int type, PlaneMaterial material) {
        // store the color (only iff no texture is used)
        if (!material.hasTexture) {
            colors.put(String.valueOf(material.color.getRGB()), material.color);
        }

        // generate the points for this plane
        Vertex[] planeVertices = new Vertex[4];
        switch (type) {
            case 1:
                planeVertices[0] = new Vertex(x, y + 0.5f, z + 0.5f);
                planeVertices[1] = new Vertex(x, y - 0.5f, z + 0.5f);
                planeVertices[2] = new Vertex(x, y - 0.5f, z - 0.5f);
                planeVertices[3] = new Vertex(x, y + 0.5f, z - 0.5f);
                break;
            case 0:
                planeVertices[3] = new Vertex(x, y + 0.5f, z + 0.5f);
                planeVertices[2] = new Vertex(x, y - 0.5f, z + 0.5f);
                planeVertices[1] = new Vertex(x, y - 0.5f, z - 0.5f);
                planeVertices[0] = new Vertex(x, y + 0.5f, z - 0.5f);
                break;
            case 5:
                planeVertices[3] = new Vertex(x + 0.5f, y, z + 0.5f);
                planeVertices[2] = new Vertex(x - 0.5f, y, z + 0.5f);
                planeVertices[1] = new Vertex(x - 0.5f, y, z - 0.5f);
                planeVertices[0] = new Vertex(x + 0.5f, y, z - 0.5f);
                break;
            case 4:
                planeVertices[0] = new Vertex(x + 0.5f, y, z + 0.5f);
                planeVertices[1] = new Vertex(x - 0.5f, y, z + 0.5f);
                planeVertices[2] = new Vertex(x - 0.5f, y, z - 0.5f);
                planeVertices[3] = new Vertex(x + 0.5f, y, z - 0.5f);
                break;
            case 3:
                planeVertices[3] = new Vertex(x + 0.5f, y + 0.5f, z);
                planeVertices[2] = new Vertex(x + 0.5f, y - 0.5f, z);
                planeVertices[1] = new Vertex(x - 0.5f, y - 0.5f, z);
                planeVertices[0] = new Vertex(x - 0.5f, y + 0.5f, z);
                break;
            case 2:
                planeVertices[0] = new Vertex(x + 0.5f, y + 0.5f, z);
                planeVertices[1] = new Vertex(x + 0.5f, y - 0.5f, z);
                planeVertices[2] = new Vertex(x - 0.5f, y - 0.5f, z);
                planeVertices[3] = new Vertex(x - 0.5f, y + 0.5f, z);
                break;
        }
        // ===================
        // store this plane (with the material) either
        // to color or tex lists
        HashMap<String, Vertex> vertices;
        HashMap<Vertex[], PlaneMaterial> planes;
        int[] nextPlaneId;
        if (!material.hasTexture) {
            vertices = colorVertices;
            planes = colorPlanes;
            nextPlaneId = nextColorPlaneId;
        } else {
            vertices = texVertices;
            planes = texPlanes;
            nextPlaneId = nextTexPlaneId;
        }
        // check if there are already points defined
        for (int i = 0; i < 4; i++) {
            Vertex existingVertex = vertices.get(planeVertices[i].toString());
            if (existingVertex != null) {
                // there is already a vertex
                planeVertices[i] = existingVertex;
            } else {
                // assign an id to this vertex
                planeVertices[i].setId(nextPlaneId[0]++);
                // and store the vertex
                vertices.put(planeVertices[i].toString(), planeVertices[i]);
            }
        }
        planes.put(planeVertices, material);
        // ===================
    }

    // register a texture id -> bufferedImage mapping
    // "to know what this texture looks like"
    public void registerTexture(int id, ImageIcon texture) {
        textureIds.put(id, texture);
    }

    // add planes (public wrapper)
    public void addPlane(float[] pos, int type,
                         Color color, Integer textureId, Integer rotation, Boolean flip) {
        // create the plane material
        PlaneMaterial material = new PlaneMaterial(color, textureId, rotation, flip, type);
        // NOTE: Z AXIS IS ALWAYS UP (for blender at least)
        switch (type) {
            case 1:
                addPlane(-pos[0] + 0.5f, -pos[2], -pos[1], type, material);
                break;
            case 0:
                addPlane(-pos[0] - 0.5f, -pos[2], -pos[1], type, material);
                break;
            case 5:
                addPlane(-pos[0], -pos[2] + 0.5f, -pos[1], type, material);
                break;
            case 4:
                addPlane(-pos[0], -pos[2] - 0.5f, -pos[1], type, material);
                break;
            case 3:
                addPlane(-pos[0], -pos[2], -pos[1] + 0.5f, type, material);
                break;
            case 2:
                addPlane(-pos[0], -pos[2], -pos[1] - 0.5f, type, material);
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

    // internal, rotate 4D int[] array
    private int[] rotate4D(int[] array, boolean addFront) {
        int[] result = array.clone();
        if (addFront) {
            result = new int[] {
                    result[3], result[0], result[1], result[2]
            };
        } else {
            result = new int[] {
                    result[1], result[2], result[3], result[0]
            };
        }
        return result;
    }

    // internal - helper function to alter 4D int array
    // this ensures the uv mapping is correct accoring to
    // "rotation", "flip" and "plane orientation (type)"
    private int[] alter4D(int[] array, int rotate, boolean flip, int type) {
        int[] result = array.clone();
        if (flip) {
            result = new int[] {
                    result[3], result[2], result[1], result[0]
            };
        }
        // correct default orientation of all the textures
        // (this depends on the plane orientation)
        switch (type) {
            case 0: result = rotate4D(result, true); break;
            case 1: result = rotate4D(result, false); break;
            case 4: result = rotate4D(result, false); break;
            case 5: result = rotate4D(result, true); break;
            default: break;
        }
        while (rotate > 0) {
            result = rotate4D(result, true);
            rotate--;
        }
        return result;
    }

    public final void finish(String textureFileName) {

        // create the big texture image (that contains all used textures)
        // =======================
        // contains the mapping "textureid" to
        // "top left position on big image" (as tile, not as pixel)
        HashMap<Integer, Point> texturePos = new HashMap<Integer, Point>();
        // width and height of the big image ("tile size")
        int width;
        int height;
        // stores the mapping point string uid -> uv point (as in dae)
        HashMap<String, UVPoint> uvIds = new HashMap<String, UVPoint>();
        // contains all the
        int texCount = textureIds.size();
        if (texCount > 0) {
            // calc size - use the sqrt as width for the image
            width = (int)Math.ceil(Math.sqrt(texCount));
            height = (int)Math.ceil(texCount / (float)width);
            // calculate the interpolation values
            float interpx = 0.001f/width;
            float interpy = 0.001f/height;
            // holds the final image
            textureMap = new BufferedImage(width * 32, height * 32, BufferedImage.TYPE_INT_RGB);
            // loop over all the used textures
            int x = 0;
            int y = 0;
            int c = 0; // next uv identifier (as used in the dae file)
            for (Map.Entry<Integer, ImageIcon> texture : textureIds.entrySet()) {
                // the pixel position (top left)
                Point pos = new Point(x, y);
                // write to "big" texture image
                textureMap.getGraphics().drawImage(
                        texture.getValue().getImage(), pos.x * 32, pos.y * 32, null);
                // store the mapping "texture id" -> "tile position"
                texturePos.put(texture.getKey(), pos);
                // write the (missing) uv mapping points for this tile position
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        float[] posFloat = new float[] {
                                ((x+i) / (float) width),
                                ((height - (y+j)) / (float) height)
                        };
                        uvIds.put(
                                (x+i) + "_" + (y+j) + "@" + i + "_" + j,
                                // store "reverse" for y values (they are inverted!)
                                new UVPoint(c++, posFloat,
                                        i == 0, j == 1,
                                        interpx, interpy)
                        );
                    }
                }
                // update the tile position
                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }
            }

            // write the image to the library
            xmlFile.resetTopNode("library_images/image[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=file1-image",
                    "name=file1-image"
            });
            xmlFile.addTextContent("init_from", textureFileName);

            // create the texture object
            xmlFile.resetTopNode("library_visual_scenes/visual_scene/node[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=PlaneTEX",
                    "name=PlaneTEX",
                    "type=NODE"
            });
            xmlFile.addAttrAndTextContent("translate", new String[]{"sid=location"},
                    "0 0 0");
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationZ"}, "0 0 1 0");
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationY"}, "0 1 0 0");
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationX"}, "1 0 0 0");
            // scale the object down
            xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.5 0.5 0.5");

            // add the material to the object
            xmlFile.setTopNode("instance_geometry[-1]");
            xmlFile.addAttributes("", new String[] {"url=#Plane-tex-mesh"});
            xmlFile.setTopNode("bind_material/technique_common/instance_material[-1]");
            xmlFile.addAttributes("", new String[] {
                    "symbol=lambert1-material",
                    "target=#lambert1-material"
            });
            // add the uv mapping
            xmlFile.addAttributes("bind_vertex_input", new String[] {
                    "semantic=TEX0",
                    "input_semantic=TEXCOORD",
                    "input_set=0"
            });

            // add the material
            xmlFile.resetTopNode("library_materials/material[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=lambert1-material",
                    "name=lambert1"
            });
            xmlFile.addAttributes("instance_effect", new String[] {
                    "url=#lambert1-fx"
            });

            // write the image effect
            xmlFile.resetTopNode("library_effects/effect[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=lambert1-fx"
            });
            xmlFile.setTopNode("profile_COMMON");
            // ----
            xmlFile.setTopNode("newparam[-1]");
            xmlFile.addAttributes("", new String[]{"sid=file1-surface"});
            xmlFile.addAttributes("surface", new String[]{"type=2D"});
            xmlFile.addTextContent("surface/init_from", "file1-image");
            // ----
            xmlFile.goUp();
            xmlFile.setTopNode("newparam[-1]");
            xmlFile.addAttributes("", new String[]{"sid=file1-sampler"});
            xmlFile.addTextContent("sampler2D/source", "file1-surface");
            // ----
            xmlFile.goUp();
            xmlFile.setTopNode("technique[-1]");
            xmlFile.addAttributes("", new String[]{"sid=common"});
            xmlFile.addTextContent("lambert/emission/color", "0 0 0 1");
            xmlFile.addTextContent("lambert/ambient/color", "0 0 0 1");
            xmlFile.addAttributes("lambert/diffuse/texture", new String[]{
                    "texture=file1-sampler",
                    "texcoord=TEX0"
            });
        } else {
            // there is no texture image linkes, so we need to remove
            // this node again (it was added before to be in the correct position)
            xmlFile.resetTopNode();
            xmlFile.deleteChild("library_images");
        }
        // =======================
        // store the colors that are being used on any plane
        if (colors.size() > 0) {
            // create the texture object
            xmlFile.resetTopNode("library_visual_scenes/visual_scene/node[-1]");
            xmlFile.addAttributes("", new String[]{
                    "id=PlaneCOL",
                    "name=PlaneCOL",
                    "type=NODE"
            });
            xmlFile.addAttrAndTextContent("translate", new String[]{"sid=location"},
                    "0 0 0");
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationZ"}, "0 0 1 0");
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationY"}, "0 1 0 0");
            xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationX"}, "1 0 0 0");
            // scale the object down
            if (texPlanes.size() > 0) {
                // large voxel for textured objects
                xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.5 0.5 0.5");
            } else {
                // small voxel for props
                xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.05 0.05 0.05");
            }
            // add the material to the object
            xmlFile.setTopNode("instance_geometry[-1]");
            xmlFile.addAttributes("", new String[] {"url=#Plane-color-mesh"});
            xmlFile.setTopNode("bind_material/technique_common");

            // write the instance materials
            for (Color color : colors.values()) {
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                String baseName = "Material_" + r + "_" + g + "_" + b;
                // add the material to the object
                xmlFile.addAttributes("instance_material[-1]", new String[] {
                        "symbol=" + baseName + "-material",
                        "target=#" + baseName + "-material"
                });
            }

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
        }
        // =========================

        // ######################################################
        // write color information and object
        // ######################################################
        if (colorPlanes.size() > 0) {
            // reset top node
            xmlFile.resetTopNode("library_geometries/geometry[-1]");
            xmlFile.addAttributes("", new String[] {
                    "id=Plane-color-mesh",
                    "name=Plane-color"
            });
            xmlFile.setTopNode("mesh");

            // sort all points by id and write them
            ArrayList<Vertex> sortedVertices = new ArrayList<Vertex>();
            sortedVertices.addAll(colorVertices.values());
            Collections.sort(sortedVertices, new Comparator<Vertex>() {
                @Override
                public int compare(Vertex o1, Vertex o2) {
                    return o1.getId() - o2.getId();
                }
            });
            StringBuilder positions = new StringBuilder();
            for (Vertex vertex : sortedVertices) {
                // scale by 2 and convert to integer to save space
                // (note: to compensate, the scene is scaled to 0.5)
                positions.append((int)(vertex.x * 2)).append(" ")
                        .append((int)(vertex.y * 2)).append(" ")
                        .append((int)(vertex.z * 2)).append(" ");
            }
            // Plane-color-mesh-positions
            xmlFile.addAttributes("source[-1]", new String[] {
                    "id=Plane-color-mesh-positions"
            });
            xmlFile.addAttrAndTextContent("source[0]/float_array",
                    new String[]{
                            "id=Plane-color-mesh-positions-array",
                            "count=" + (sortedVertices.size() * 3)},
                    positions.toString());
            xmlFile.addAttributes("source[0]/technique_common/accessor", new String[]{
                    "source=#Plane-color-mesh-positions-array",
                    "count=" + sortedVertices.size(),
                    "stride=3"
            });
            xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                    new String[]{"name=X", "type=float"});
            xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                    new String[]{"name=Y", "type=float"});
            xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                    new String[]{"name=Z", "type=float"});

            // vertices (generic information)
            xmlFile.addAttributes("vertices", new String[] {
                    "id=Plane-color-mesh-vertices"
            });
            xmlFile.addAttributes("vertices/input", new String[] {
                    "semantic=POSITION",
                    "source=#Plane-color-mesh-positions"
            });

            // group planes according to the different colors
            // (those that have no texture)
            HashMap<Color, ArrayList<Vertex[]>> colorMaterialToPlanes = new HashMap<Color, ArrayList<Vertex[]>>();
            for (Map.Entry<Vertex[], PlaneMaterial> entry : colorPlanes.entrySet()) {
                ArrayList<Vertex[]> planes = colorMaterialToPlanes.get(entry.getValue().color);
                if (planes == null) {
                    planes = new ArrayList<Vertex[]>();
                    colorMaterialToPlanes.put(entry.getValue().color, planes);
                }
                planes.add(entry.getKey());
            }

            // write the different poly-lists for colors (each is linked to a material)
            for (Map.Entry<Color, ArrayList<Vertex[]>> entry : colorMaterialToPlanes.entrySet()) {
                // polylist
                StringBuilder pList = new StringBuilder();
                StringBuilder vcount = new StringBuilder();
                int count = 0;
                for (Vertex[] vertices : entry.getValue()) {
                    // a plane
                    pList.append(vertices[0].getId()).append(" ")
                            .append(vertices[1].getId()).append(" ")
                            .append(vertices[2].getId()).append(" ")
                            .append(vertices[3].getId()).append(" ");
                    // denote that there is a plane (with four points)
                    vcount.append("4 ");
                    count++;
                }
                // generate the material information (color material)
                Color color = entry.getKey();
                String baseName = "Material_" + color.getRed() + "_" + color.getGreen() + "_" + color.getBlue();
                // write data
                xmlFile.setTopNode("polylist[-1]");
                xmlFile.addAttributes("", new String[] {
                        "material=" + baseName + "-material",
                        "count=" + count
                });
                xmlFile.addAttributes("input[-1]", new String[] {
                        "semantic=VERTEX",
                        "source=#Plane-color-mesh-vertices",
                        "offset=0"
                });
                xmlFile.addTextContent("vcount", vcount.toString());
                xmlFile.addTextContent("p", pList.toString());
                xmlFile.goUp();

            }
        }
        // ######################################################

        // ######################################################
        // write texture information and object
        // ######################################################
        // =================
        // write texture details (below)
        if (texPlanes.size() > 0) {
            // reset top node
            xmlFile.resetTopNode("library_geometries/geometry[-1]");
            xmlFile.addAttributes("", new String[] {
                    "id=Plane-tex-mesh",
                    "name=Plane-tex"
            });
            xmlFile.setTopNode("mesh");

            // sort all points by id and write them
            ArrayList<Vertex> sortedVertices = new ArrayList<Vertex>();
            sortedVertices.addAll(texVertices.values());
            Collections.sort(sortedVertices, new Comparator<Vertex>() {
                @Override
                public int compare(Vertex o1, Vertex o2) {
                    return o1.getId() - o2.getId();
                }
            });
            StringBuilder positions = new StringBuilder();
            for (Vertex vertex : sortedVertices) {
                // scale by 2 and convert to integer to save space
                // (note: to compensate, the scene is scaled to 0.5)
                positions.append((int)(vertex.x * 2)).append(" ")
                        .append((int)(vertex.y * 2)).append(" ")
                        .append((int)(vertex.z * 2)).append(" ");
            }
            // Plane-tex-mesh-positions
            xmlFile.addAttributes("source[-1]", new String[] {
                    "id=Plane-tex-mesh-positions"
            });
            xmlFile.addAttrAndTextContent("source[0]/float_array",
                    new String[]{
                            "id=Plane-tex-mesh-positions-array",
                            "count=" + (sortedVertices.size() * 3)},
                    positions.toString());
            xmlFile.addAttributes("source[0]/technique_common/accessor", new String[]{
                    "source=#Plane-tex-mesh-positions-array",
                    "count=" + sortedVertices.size(),
                    "stride=3"
            });
            xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                    new String[]{"name=X", "type=float"});
            xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                    new String[]{"name=Y", "type=float"});
            xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                    new String[]{"name=Z", "type=float"});

            // sort all uv points by id and write them
            ArrayList<UVPoint> sortedUVs = new ArrayList<UVPoint>();
            sortedUVs.addAll(uvIds.values());
            Collections.sort(sortedUVs, new Comparator<UVPoint>() {
                @Override
                public int compare(UVPoint o1, UVPoint o2) {
                    return o1.id - o2.id;
                }
            });
            StringBuilder uvPoints = new StringBuilder();
            for (UVPoint uvPoint : sortedUVs) {
                uvPoints.append(uvPoint.floatPos).append(" ");
            }
            // --- write the uvs
            xmlFile.addAttributes("source[-1]", new String[] {
                    "id=Plane-tex-mesh-uvs"
            });
            xmlFile.addAttrAndTextContent("source[1]/float_array",
                    new String[]{
                            "id=Plane-tex-mesh-uvs-array",
                            "count=" + (sortedUVs.size() * 2)},
                    uvPoints.toString());

            xmlFile.addAttributes("source[1]/technique_common/accessor", new String[]{
                    "source=#Plane-tex-mesh-uvs-array",
                    "count=" + sortedUVs.size(),
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

            // write the polylist that links to the texture image
            StringBuilder pList = new StringBuilder();
            StringBuilder vcount = new StringBuilder();
            int count = 0;
            for (Map.Entry<Vertex[], PlaneMaterial> plane : texPlanes.entrySet()) {
                PlaneMaterial material = plane.getValue();
                Vertex[] vertices = plane.getKey();
                // get the texture point
                Point point = texturePos.get(material.textureId);
                int[] planeUV = new int[] {
                        // the @i_j is for the interpolation
                        uvIds.get((point.x+1) + "_" + (point.y+1) + "@1_1").id,
                        uvIds.get((point.x+1) + "_" + (point.y) + "@1_0").id,
                        uvIds.get((point.x) + "_" + (point.y) + "@0_0").id,
                        uvIds.get((point.x) + "_" + (point.y+1) + "@0_1").id
                };
                planeUV = alter4D(planeUV, material.rotation, material.flip, material.type);
                // a plane
                pList.append(vertices[0].getId()).append(" ").append(planeUV[0]).append(" ")
                        .append(vertices[1].getId()).append(" ").append(planeUV[1]).append(" ")
                        .append(vertices[2].getId()).append(" ").append(planeUV[2]).append(" ")
                        .append(vertices[3].getId()).append(" ").append(planeUV[3]).append(" ");
                // denote that there is a plane (with four points)
                vcount.append("4 ");
                count++;
            }
            // write data
            xmlFile.setTopNode("polylist[-1]");
            xmlFile.addAttributes("", new String[] {
                    "material=lambert1-material",
                    "count=" + count
            });
            xmlFile.addAttributes("input[-1]", new String[] {
                    "semantic=VERTEX",
                    "source=#Plane-tex-mesh-vertices",
                    "offset=0"
            });
            xmlFile.addAttributes("input[-1]", new String[] {
                    "semantic=TEXCOORD",
                    "source=#Plane-tex-mesh-uvs",
                    "offset=1",
                    "set=0"
            });
            xmlFile.addTextContent("vcount", vcount.toString());
            xmlFile.addTextContent("p", pList.toString());

        }
        // ######################################################
        // =================

    }

    // returns true if there is a texture image attached to this dae file
    public boolean hasTextureMap() {
        return textureMap != null;
    }

    // write the texture image to file
    public boolean writeTextureMap(File file, ErrorHandlerInterface errorHandler) {
        boolean result = false;
        if (hasTextureMap()) {
            try {
                ImageIO.write(textureMap, "png", file);
                result = true;
            } catch (IOException e1) {
                errorHandler.handle(e1);
            }
        }
        return result;
    }

    // save this file (xml code)
    public boolean writeToFile(String filename, ErrorHandlerInterface errorHandler) {
        return xmlFile.writeToFile(filename, errorHandler);
    }

    // save this file
    public boolean writeToFile(File file, ErrorHandlerInterface errorHandler) {
        return xmlFile.writeToFile(file, errorHandler);
    }
}
