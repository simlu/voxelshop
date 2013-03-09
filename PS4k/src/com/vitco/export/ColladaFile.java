package com.vitco.export;

import com.vitco.export.container.Vertex;
import com.vitco.res.VitcoSettings;
import com.vitco.util.DateTools;
import com.vitco.util.error.ErrorHandlerInterface;
import com.vitco.util.xml.XmlFile;

import java.awt.*;
import java.io.File;
import java.util.*;

/**
 * Wrapper class for easily exporting *.dae files.
 *
 * Note: The object that is exported is references to as "Plane"
 */
public class ColladaFile {

    // the xml Collada file
    private XmlFile xmlFile = new XmlFile("COLLADA");

    // holds all the used colors
    private final HashMap<String, Color> colors = new HashMap<String, Color>();
    // holds the vertices
    private final HashMap<String, Vertex> vertices = new HashMap<String, Vertex>();
    // holds the planes
    private final HashMap<Vertex[], Color> planes = new HashMap<Vertex[], Color>();

    // the id of the last assigned vertex
    private int nextId = 0;

    // internal - holds all the planes
    private void addPlane(float x, float y, float z, int type, Color color) {
        // store the color
        colors.put(String.valueOf(color.getRGB()), color);
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
        // check if there are already points defined
        for (int i = 0; i < 4; i++) {
            Vertex existingVertex = vertices.get(planeVertices[i].toString());
            if (existingVertex != null) {
                // there is already a vertex
                planeVertices[i] = existingVertex;
            } else {
                // assign an id to this vertex
                planeVertices[i].setId(nextId++);
                // and store the vertex
                vertices.put(planeVertices[i].toString(), planeVertices[i]);
            }
        }
        // add the plane
        planes.put(planeVertices, color);
    }

    // add planes (public wrapper)
    public void addPlane(float[] pos, int type, Color color) {
        // NOTE: Z AXIS IS ALWAYS UP (for blender at least)
        switch (type) {
            case 1:
                addPlane(-pos[0] + 0.5f, -pos[2], -pos[1], type, color);
                break;
            case 0:
                addPlane(-pos[0] - 0.5f, -pos[2], -pos[1], type, color);
                break;
            case 5:
                addPlane(-pos[0], -pos[2] + 0.5f, -pos[1], type, color);
                break;
            case 4:
                addPlane(-pos[0], -pos[2] - 0.5f, -pos[1], type, color);
                break;
            case 3:
                addPlane(-pos[0], -pos[2], -pos[1] + 0.5f, type, color);
                break;
            case 2:
                addPlane(-pos[0], -pos[2], -pos[1] - 0.5f, type, color);
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

        // will hold all the color "effect" information
        xmlFile.resetTopNode("library_effects");

        // will contain the materials linked to the color information
        xmlFile.resetTopNode("library_materials");

        // will contain geometries (mesh) of the object
        xmlFile.resetTopNode("library_geometries");
        xmlFile.addAttributes("geometry", new String[] {
                "id=Plane-mesh",
                "name=Plane"
        });
        xmlFile.setTopNode("geometry/mesh");

        // =========================

        // contains the scene
        xmlFile.resetTopNode("library_visual_scenes/visual_scene");
        xmlFile.addAttributes("", new String[] {
                "id=Scene",
                "name=Scene"
        });
        xmlFile.resetTopNode("library_visual_scenes/visual_scene/node[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=PlaneX",
                "name=PlaneX",
                "type=NODE"
        });
        xmlFile.addAttrAndTextContent("translate", new String[]{"sid=location"},
                "0 0 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationZ"}, "0 0 1 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationY"}, "0 1 0 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationX"}, "1 0 0 0");
        // scale the scene down
        xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.5 0.5 0.5");

        xmlFile.addAttributes("instance_geometry", new String[] {"url=#Plane-mesh"});

        // link the library_visual_scenes node
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

            // add the material to the object
            xmlFile.resetTopNode();
            xmlFile.addAttributes("instance_geometry/bind_material/technique_common/instance_material[-1]", new String[] {
                    "symbol=" + baseName + "-material",
                    "target=#" + baseName + "-material"
            });

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

        // =========================

        // reset top node
        xmlFile.resetTopNode("library_geometries/geometry/mesh");

        // sort all points by id and write them
        ArrayList<Vertex> sortedVertices = new ArrayList<Vertex>();
        sortedVertices.addAll(vertices.values());
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
        // Plane-mesh-positions
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-mesh-positions"
        });
        xmlFile.addAttrAndTextContent("source[0]/float_array",
                new String[]{
                        "id=Plane-mesh-positions-array",
                        "count=" + (sortedVertices.size() * 3)},
                positions.toString());
        xmlFile.addAttributes("source[0]/technique_common/accessor", new String[]{
                "source=#Plane-mesh-positions-array",
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
                "id=Plane-mesh-vertices"
        });
        xmlFile.addAttributes("vertices/input", new String[] {
                "semantic=POSITION",
                "source=#Plane-mesh-positions"
        });

        // group planes according to the different colors
        HashMap<Color, ArrayList<Vertex[]>> colorPlanes = new HashMap<Color, ArrayList<Vertex[]>>();
        for (Map.Entry<Vertex[], Color> entry : planes.entrySet()) {
            ArrayList<Vertex[]> planes = colorPlanes.get(entry.getValue());
            if (planes == null) {
                planes = new ArrayList<Vertex[]>();
                colorPlanes.put(entry.getValue(), planes);
            }
            planes.add(entry.getKey());
        }

        // write the different poly-lists (each is linked to a material)
        for (Map.Entry<Color, ArrayList<Vertex[]>> entry : colorPlanes.entrySet()) {

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
            // generate the material information
            Color color = entry.getKey();
            String baseName = "Material_" + color.getRed() + "_" + color.getGreen() + "_" + color.getBlue();
            // write data
            xmlFile.resetTopNode("library_geometries/geometry/mesh/polylist[-1]");
            xmlFile.addAttributes("", new String[] {
                    "material=" + baseName + "-material",
                    "count=" + count
            });
            xmlFile.addAttributes("input[-1]", new String[] {
                    "semantic=VERTEX",
                    "source=#Plane-mesh-vertices",
                    "offset=0"
            });
            xmlFile.addTextContent("vcount", vcount.toString());
            xmlFile.addTextContent("p", pList.toString());

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
