/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mergexml;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.Edge;
import utils.FromNode;
import utils.ToNode;

/**
 *
 * @author essam
 */
public class NetConstructor {

    public static void write_edges(ArrayList<Edge> edge_list, String path) {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("edges");
            doc.appendChild(rootElement);

            for (Edge edge : edge_list) {
                Element edgeElement = doc.createElement("edge");
                rootElement.appendChild(edgeElement);
                Attr attr = doc.createAttribute("id");
                attr.setValue(edge.id);
                edgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("from");
                attr.setValue(edge.from_node);
                edgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("to");
                attr.setValue(edge.to_node);
                edgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("shape");
                attr.setValue(edge.shape);
                edgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("type");
                attr.setValue(edge.type);
                edgeElement.setAttributeNode(attr);

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            // Output to_node console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
        }

    }
    String edgesPath;
    String ProbPath;
    ArrayList<Edge> edges;

    ArrayList<FromNode> netTrans;

    public NetConstructor(String edgesPath) {
        this.edgesPath = edgesPath;
    }

    public NetConstructor(String edgesPath, String ProbPath) {
        this.edgesPath = edgesPath;
        this.ProbPath = ProbPath;

//        readedges();
    }

    public ArrayList<FromNode> construct_edges_map() {
        netTrans = new ArrayList<>();
        FromNode fromEdge;
        ArrayList<ToNode> toEdges;
        ToNode newToEdge;

        for (Edge edge : this.edges) {
            String from_id = edge.id;
            fromEdge = new FromNode();
            fromEdge.ID = from_id;
            String fromXY[] = getCoords(edge.shape);
            fromEdge.x = Double.parseDouble(fromXY[0]);
            fromEdge.y = Double.parseDouble(fromXY[1]);
            toEdges = new ArrayList<>();
            for (Edge to_edge : this.edges) {
                String to_from_id = to_edge.from_node;
                if (from_id.equals(to_from_id)) {
                    newToEdge = new ToNode();
                    newToEdge.ID = to_edge.id;
                    String[] toXY = getCoords(to_edge.shape);
                    newToEdge.x = Double.parseDouble(toXY[0]);
                    newToEdge.y = Double.parseDouble(toXY[1]);
                    toEdges.add(newToEdge);
                }
            }
            fromEdge.toedges = toEdges;
            netTrans.add(fromEdge);
        }
//
//        for (Edge edge : this.edges) {
//            String id = edge.id;
//            String toKey = edge.to_node;
//            fromEdge = new FromNode();
//            toEdges = new ArrayList<>();
//            fromEdge.setID(edge.id);
//            String fromXY[] = getCoords(edge.shape);
//            fromEdge.x = Double.parseDouble(fromXY[0]);
//            fromEdge.y = Double.parseDouble(fromXY[1]);
//            for (Edge toEdge : this.edges) {
//
//                String from_node = toEdge.from_node;
//                String to_node = toEdge.to_node;
//                newToEdge = new ToNode();
//                if (id.equals(from_node)) {
//                    newToEdge.setID(toEdge.id);
//                    String[] toXY = getCoords(toEdge.shape);
//                    newToEdge.x = Double.parseDouble(toXY[0]);
//                    newToEdge.y = Double.parseDouble(toXY[1]);
//                    toEdges.add(newToEdge);
//                }
//
//            }
//            fromEdge.toedges = toEdges;
//            netTrans.add(fromEdge);
//
//        }

        return netTrans;
    }

    /**
     * Construct traffic network with road segments center coordinates.
     *
     * @return network of edges transitions
     */
    public ArrayList<FromNode> construct_traffic_map() {
        netTrans = new ArrayList<>();
        FromNode fromEdge;
        ArrayList<ToNode> toEdges;
        ToNode newToEdge;

        for (Edge edge : this.edges) {

//            String fromKey = edge.from_node;

            String toKey = edge.to_node;
            fromEdge = new FromNode();
            toEdges = new ArrayList<>();
            fromEdge.setID(edge.id);
            fromEdge.setType(edge.type);

            /**
             * Find the center point of a straight line segment
             */
            Point2D center_pnt = get_center_coords(edge.shape);
            fromEdge.x = center_pnt.getX();
            fromEdge.y = center_pnt.getY();
            for (Edge toEdge : this.edges) {
                String from = toEdge.from_node;
                String to = toEdge.to_node;
                newToEdge = new ToNode();
                if (toKey.equals(from)) {
                    newToEdge.setID(toEdge.id);
                    String[] toXY = getCoords(toEdge.shape);
                    newToEdge.x = Double.parseDouble(toXY[0]);
                    newToEdge.y = Double.parseDouble(toXY[1]);
                    toEdges.add(newToEdge);
                }

            }
            fromEdge.toedges = toEdges;
            netTrans.add(fromEdge);
        }

        return netTrans;
    }

    /**
     * Construct a traffic network, XY coordinates are the coordinates of the
     * first point in the edges. This is not totally correct it affects the
     * distance calculates and the gravity flow as I think.
     *
     * @return
     */
    public ArrayList<FromNode> constructTransitions() {
        netTrans = new ArrayList<>();
        FromNode fromEdge;
        ArrayList<ToNode> toEdges;
        ToNode newToEdge;

        for (Edge edge : this.edges) {
            String fromKey = edge.from_node;
            String toKey = edge.to_node;
            fromEdge = new FromNode();
            toEdges = new ArrayList<>();
            fromEdge.setID(edge.id);
            String fromXY[] = getCoords(edge.shape);
            fromEdge.x = Double.parseDouble(fromXY[0]);
            fromEdge.y = Double.parseDouble(fromXY[1]);
            for (Edge toEdge : this.edges) {
                String from = toEdge.from_node;
                String to = toEdge.to_node;
                newToEdge = new ToNode();
                if (toKey.equals(from)) {
                    newToEdge.setID(toEdge.id);
                    String[] toXY = getCoords(toEdge.shape);
                    newToEdge.x = Double.parseDouble(toXY[0]);
                    newToEdge.y = Double.parseDouble(toXY[1]);
                    toEdges.add(newToEdge);
                }

            }
            fromEdge.toedges = toEdges;
            netTrans.add(fromEdge);

        }

        return netTrans;
    }

    /**
     * Calculate the Eculidean distance.
     *
     * @param x
     * @param x1
     * @param y
     * @param y1
     * @return
     */
    private double eculidean(double x, double x1, double y, double y1) {
        return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
    }

    /**
     * Get the line middle point from a line shape <x0,y0 x1,y1 x2,y2 ...>
     *
     * @param shape
     * @return center point on a line segment
     */
    private Point2D get_center_coords(String shape) {
        String[] points = shape.split(" ");
        String[] orgn_pnt = points[0].split(",");
        String[] end_pnt = points[points.length - 1].split(",");

        return getMidPoint(new Point2D.Double(Double.parseDouble(orgn_pnt[0]), Double.parseDouble(orgn_pnt[1])),
                new Point2D.Double(Double.parseDouble(end_pnt[0]), Double.parseDouble(end_pnt[1])));
    }

    public double get_edge_length(String shape) {
        double len = 0;
        String pnts[] = shape.split(" ");
        for (int i = 0; i < pnts.length - 1; i++) {
            String coord1[] = pnts[i].split(",");
            String coord2[] = pnts[i + 1].split(",");
            len += eculidean(Double.parseDouble(coord1[0]),
                    Double.parseDouble(coord2[0]),
                    Double.parseDouble(coord1[1]),
                    Double.parseDouble(coord2[1]));
        }
        return len;
    }

    public Line2D get_line(String shape) {
        String pnts[] = shape.trim().split(" ");
//        System.out.println(pnts[0]+"\t"+pnts[pnts.length - 1]);
        String coord1[] = pnts[0].split(",");
        String coord2[] = pnts[pnts.length - 1].split(",");
        return new Line2D.Double(Double.parseDouble(coord1[0]),
                Double.parseDouble(coord1[1]),
                Double.parseDouble(coord2[0]),
                Double.parseDouble(coord2[1]));
    }

    private String[] getCoords(String shape) {
        String[] points = shape.split(" ");
        return points[0].split(",");
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    /**
     * Get the center point between two point on the same line.
     *
     * @param origin
     * @param endPnt
     * @return
     */
    public Point2D getMidPoint(Point2D origin, Point2D endPnt) {
        return new Point2D.Double((origin.getX() + endPnt.getX()) / 2, (origin.getY() + endPnt.getY()) / 2);
    }

    public ArrayList<FromNode> getNetTrans() {
        return netTrans;
    }

    private ArrayList<String> getNodes() {
        ArrayList<String> nodes = new ArrayList<>();
        for (Edge next : edges) {
            if (!nodes.contains(next.from_node)) {
                nodes.add(next.from_node);
            }
        }
        return nodes;
    }

    public ArrayList<Edge> interpolate_edges(double threshold) {
//        double threshold = 200;
        this.edges = new ArrayList<>();

        Hashtable<String, String> m_edges = new Hashtable<>();

        org.w3c.dom.Document dpDoc;
        File dpXmlFile = new File(this.edgesPath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dpDoc = dBuilder.parse(dpXmlFile);
            dpDoc.getDocumentElement().normalize();

            NodeList edgesList = dpDoc.getElementsByTagName("edge");
            for (int i = 0; i < edgesList.getLength(); i++) {
                Edge edge = new Edge();
                Node edgeNode = edgesList.item(i);
                if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) edgeNode;
                    edge.id = eElement.getAttribute("id");
                    edge.from_node = eElement.getAttribute("from");
                    edge.to_node = eElement.getAttribute("to");
                    edge.shape = eElement.getAttribute("shape");
                    /**
                     * For test
                     */

//                    edges.add(edge);
                    double length = get_edge_length(edge.shape);
                    if (length > 1.25 * threshold) {
                        /**
                         * Original edge info
                         */
                        String org_id = edge.id;
                        String org_from = edge.from_node;
                        String org_to = edge.to_node;
                        String pnts[] = edge.shape.split(" ");
                        String org_strt = pnts[0];
                        String org_end = pnts[pnts.length - 1];

//                        System.out.println(edge.id + "\t" + length + "\t" + edge.shape);
                        ArrayList<String> in_pnts = new ArrayList<>();
                        in_pnts.add(org_strt);
                        /**
                         * split edge into sub_edges ..
                         */
                        int no_seg = (int) (length / threshold) + 1;
                        for (int j = 1; j < no_seg; j++) {
//                            System.out.println("j*threshold\t" + j * threshold + "->" + length);
                            in_pnts.add(interpolationByDistance(get_line(edge.shape), j * threshold));
                        }
                        in_pnts.add(org_end);
                        /**
                         * Add dumy node as a transition from all connected
                         * nodes to the new segments.
                         */
//                        Edge edge_x = new Edge(org_id, org_from, org_id + "_inode_[" + 0 + "]", org_strt + " " + org_strt);
//                        edges.add(edge_x);
                        /**
                         * Replace the original edge with a set of segments
                         */
//                        String end_node = "";
                        for (int j = 0; j < in_pnts.size() - 1; j++) {
                            String strt_pnt = in_pnts.get(j);
                            String end_pnt = in_pnts.get(j + 1);
                            Edge edge_sub = new Edge();

                            edge_sub.id = org_id + "_interpolated_[" + j + "]";
                            if (j == 0) {
                                edge_sub.from_node = org_from;
//                                edge.id = org_id;
                            } else {
                                edge_sub.from_node = org_id + "_inode_[" + (j - 1) + "]";
                            }

                            if ((j + 1) == in_pnts.size() - 1) {
//                                end_node = edge_sub.id;
                                edge_sub.to_node = org_to;
                            } else {
                                edge_sub.to_node = org_id + "_inode_[" + j + "]";
                            }
                            edge_sub.shape = strt_pnt + " " + end_pnt;
                            edges.add(edge_sub);
                        }
//                        m_edges.put(org_id, end_node);
                    } else {
                        edges.add(edge);
                    }

                }
            }

//            Hashtable<Edge, Edge> m_e_edges = new Hashtable<>();
//            for (Map.Entry<String, String> entry : m_edges.entrySet()) {
//                String org_id = entry.getKey();
//                String end_id = entry.getValue();
//                for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
//                    Edge edge1 = it.next();
//                    if (edge1.from_node.equals(org_id)) {
//                        Edge edge2 = new Edge(edge1.id, end_id, edge1.to_node, edge1.shape);
//                        m_e_edges.put(edge1, edge2);
//                    }
//                }
//            }
//
//            for (Map.Entry<Edge, Edge> entry : m_e_edges.entrySet()) {
//                Edge edge1 = entry.getKey();
//                Edge edge2 = entry.getValue();
//                edges.remove(edge1);
//                edges.add(edge2);
//
//            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return edges;
    }

    public String interpolationByDistance(Line2D line, double d) {
        double len = eculidean(line.getX1(), line.getX2(), line.getY1(), line.getY2());
        double ratio = d / len;
//        System.out.println("Ratio\t"+ratio);
        double x = ratio * line.getX2() + (1.0 - ratio) * line.getX1();
        double y = ratio * line.getY2() + (1.0 - ratio) * line.getY1();
//        System.out.println(x + ", " + y);
//        return new Point2D.Double(x, y);
        return new String(Double.toString(x) + "," + Double.toString(y));
    }

    public ArrayList<Edge> readedges() {
        this.edges = new ArrayList<>();

        org.w3c.dom.Document dpDoc;
        File dpXmlFile = new File(this.edgesPath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dpDoc = dBuilder.parse(dpXmlFile);
            dpDoc.getDocumentElement().normalize();
            NodeList edgesList = dpDoc.getElementsByTagName("edge");
            Edge edge;
            for (int i = 0; i < edgesList.getLength(); i++) {
                edge = new Edge();
                Node edgeNode = edgesList.item(i);
                if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) edgeNode;
                    edge.id = eElement.getAttribute("id");
                    edge.from_node = eElement.getAttribute("from");
                    edge.to_node = eElement.getAttribute("to");
                    edge.shape = eElement.getAttribute("shape");
                    edge.type = eElement.getAttribute("type");
                    edges.add(edge);

                }
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return edges;
    }

    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    public void setNetTrans(ArrayList<FromNode> netTrans) {
        this.netTrans = netTrans;
    }

    public void writeProbFile(ArrayList<FromNode> netTrans) {
        ArrayList<FromNode> edges = new ArrayList<>(netTrans);
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("turn-defs");
            doc.appendChild(rootElement);

            for (FromNode fromEdge : edges) {
                Element fromEdgeElement = doc.createElement("fromNode");
                rootElement.appendChild(fromEdgeElement);

                // set attribute to_node staff element
                Attr attr = doc.createAttribute("id");
                attr.setValue(fromEdge.ID);
                fromEdgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("x");
                attr.setValue(String.valueOf(fromEdge.x));
                fromEdgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("y");
                attr.setValue(String.valueOf(fromEdge.y));
                fromEdgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("isExt");
                attr.setValue(String.valueOf(fromEdge.isExit));
                fromEdgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("zone");
                attr.setValue(String.valueOf(fromEdge.zone));
                fromEdgeElement.setAttributeNode(attr);

                attr = doc.createAttribute("type");
                attr.setValue(String.valueOf(fromEdge.type));
                fromEdgeElement.setAttributeNode(attr);

//                System.out.println(fromEdge.ID + "\t" + fromEdge.x + "\t" + fromEdge.y);
                ArrayList<ToNode> toEdgesList = fromEdge.toedges;
                for (ToNode toEdge : toEdgesList) {
                    Element toEdgeElement = doc.createElement("toNode");
                    fromEdgeElement.appendChild(toEdgeElement);

                    // set attribute to_node staff element
                    attr = doc.createAttribute("id");
                    attr.setValue(toEdge.ID);
                    toEdgeElement.setAttributeNode(attr);

                    attr = doc.createAttribute("probability");
                    attr.setValue(String.valueOf(toEdge.Prob));
                    toEdgeElement.setAttributeNode(attr);

                    attr = doc.createAttribute("x");
                    attr.setValue(String.valueOf(toEdge.x));
                    toEdgeElement.setAttributeNode(attr);

                    attr = doc.createAttribute("y");
                    attr.setValue(String.valueOf(toEdge.y));
                    toEdgeElement.setAttributeNode(attr);

//                attr = doc.createAttribute("isExt");
//                attr.setValue(String.valueOf(toEdge.isExit));
//                toEdgeElement.setAttributeNode(attr);
//                
//                attr = doc.createAttribute("zone");
//                attr.setValue(String.valueOf(toEdge.zone));
//                toEdgeElement.setAttributeNode(attr);
//                    System.out.println("\t" + toEdge.ID + "\t" + toEdge.Prob + "\t" + toEdge.x + "\t" + toEdge.y);
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(this.ProbPath));

            // Output to_node console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
        }

    }
}
