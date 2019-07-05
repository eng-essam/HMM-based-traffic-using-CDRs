/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mergexml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import towers.test.PlotTowersDAG;
import utils.FromNode;
import utils.ToNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class MapDetails {

	public ArrayList<FromNode> merge(ArrayList<FromNode> edges, ArrayList<Vertex> edgesDetails) {
		for (Vertex edge : edgesDetails) {
			for (FromNode fromNode : edges) {
				if (edge.ID.equals(fromNode.ID)) {
					fromNode.x = edge.x;
					fromNode.y = edge.y;
					// System.out.println(fromNode.ID+ "\t" + edge.x + "\t" +
					// edge.y);
				}
				ArrayList<ToNode> toNodesList = fromNode.toedges;
				for (ToNode toNode : toNodesList) {
					if (edge.ID.equals(toNode.ID)) {
						toNode.x = edge.x;
						toNode.y = edge.y;
						// System.out.println("\t" + toNode.ID+ "\t"
						// +toNode.Prob+ "\t" + edge.x + "\t" + edge.y);
					}
				}

			}

		}

		return edges;
	}

	public ArrayList<Vertex> parseEdges(String edgespath) {
		ArrayList<Vertex> edges = new ArrayList<>();

		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(edgespath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList edgesList = dpDoc.getElementsByTagName("edge");
			Vertex edge;
			for (int i = 0; i < edgesList.getLength(); i++) {
				edge = new Vertex();
				Node edgeNode = edgesList.item(i);
				if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) edgeNode;
					edge.ID = eElement.getAttribute("id");
					String shape = eElement.getAttribute("shape");
					String parts[] = shape.split(" ");
					String Coordinates[] = parts[0].split(",");
					edge.x = Double.parseDouble(Coordinates[0]);
					edge.y = Double.parseDouble(Coordinates[1]);
					edges.add(edge);

				}
			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}
		return edges;
	}

	public ArrayList<Vertex> parseNodes(String nodesPath) {
		ArrayList<Vertex> edges = new ArrayList<>();

		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(nodesPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList edgesList = dpDoc.getElementsByTagName("node");
			Vertex edge;
			for (int i = 0; i < edgesList.getLength(); i++) {
				edge = new Vertex();
				Node edgeNode = edgesList.item(i);
				if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) edgeNode;
					edge.ID = eElement.getAttribute("id");
					edge.x = Double.parseDouble(eElement.getAttribute("x"));
					edge.y = Double.parseDouble(eElement.getAttribute("y"));
					edges.add(edge);

				}
			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}
		return edges;
	}

	public ArrayList<FromNode> readInitialMap(String path) {
		ArrayList<FromNode> edges = new ArrayList<>();
		FromNode fromNode;

		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList fromNodeList = dpDoc.getElementsByTagName("fromEdge");

			for (int i = 0; i < fromNodeList.getLength(); i++) {

				Node fromNodeNode = fromNodeList.item(i);

				if (fromNodeNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) fromNodeNode;
					// System.out.println("From Node : " +
					// eElement.getAttribute("id"));
					fromNode = new FromNode();
					fromNode.ID = eElement.getAttribute("id");
					// if (fromNode.ID.startsWith(":")) {
					// continue;
					// }
					// fromNode.x =
					// Double.parseDouble(eElement.getAttribute("x"));
					// fromNode.y =
					// Double.parseDouble(eElement.getAttribute("y"));

					ArrayList<ToNode> toNodesList = new ArrayList<>();
					ToNode edge;
					NodeList toNodeList = eElement.getElementsByTagName("toEdge");

					for (int j = 0; j < toNodeList.getLength(); j++) {
						Node toNodeNode = toNodeList.item(j);

						if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
							edge = new ToNode();
							Element toNodeElement = (Element) toNodeNode;
							edge.ID = toNodeElement.getAttribute("id");
							edge.Prob = Double.parseDouble(toNodeElement.getAttribute("probability"));
							// edge.x =
							// Double.parseDouble(toNodeElement.getAttribute("x"));
							// edge.y =
							// Double.parseDouble(toNodeElement.getAttribute("y"));
							toNodesList.add(edge);
						}

					}
					fromNode.toedges = toNodesList;
					edges.add(fromNode);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}

		return edges;
	}

	public ArrayList<FromNode> readMap(String path) {
		ArrayList<FromNode> edges = new ArrayList<>();
		FromNode fromNode;

		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList fromNodeList = dpDoc.getElementsByTagName("fromNode");

			for (int i = 0; i < fromNodeList.getLength(); i++) {

				Node fromNodeNode = fromNodeList.item(i);

				if (fromNodeNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) fromNodeNode;
					// System.out.println("From Node : " +
					// eElement.getAttribute("id"));
					fromNode = new FromNode();
					fromNode.ID = eElement.getAttribute("id");
					// if (fromNode.ID.startsWith(":")) {
					// continue;
					// }
					fromNode.x = Double.parseDouble(eElement.getAttribute("x"));
					fromNode.y = Double.parseDouble(eElement.getAttribute("y"));

					ArrayList<ToNode> toNodesList = new ArrayList<>();
					ToNode edge;
					NodeList toNodeList = eElement.getElementsByTagName("toNode");

					for (int j = 0; j < toNodeList.getLength(); j++) {
						Node toNodeNode = toNodeList.item(j);

						if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
							edge = new ToNode();
							Element toNodeElement = (Element) toNodeNode;
							edge.ID = toNodeElement.getAttribute("id");
							edge.Prob = Double.parseDouble(toNodeElement.getAttribute("probability"));
							edge.x = Double.parseDouble(toNodeElement.getAttribute("x"));
							edge.y = Double.parseDouble(toNodeElement.getAttribute("y"));
							toNodesList.add(edge);
						}

					}
					fromNode.toedges = toNodesList;
					edges.add(fromNode);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}

		return edges;
	}

	public Hashtable<Integer, Vertex> readTowers(String path) {
		Hashtable<Integer, Vertex> towers = new Hashtable<>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			Vertex pnt = null;
			boolean flag = false;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");
				pnt = new Vertex();
				int index = Integer.parseInt(lineSplit[0]);
				pnt.x = Double.parseDouble(lineSplit[1]);
				pnt.y = Double.parseDouble(lineSplit[2]);
				// System.out.printf("%f,%f",pnt.x,pnt.y);
				towers.put(index, pnt);
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
		}
		return towers;
	}

	public Hashtable<Integer, ArrayList<Integer>> readVoronoiNeibors(String path) {
		Hashtable<Integer, ArrayList<Integer>> neighbours = new Hashtable<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			int id = 0;

			while ((line = reader.readLine()) != null) {
				ArrayList<Integer> tmp = new ArrayList<>();
				String lineSplit[] = line.split(",");

				if (!line.startsWith(",")) {
					id = Integer.parseInt(lineSplit[0].trim());
				} else {
					for (String lineSplit1 : lineSplit) {
						String toZone = lineSplit1.trim();
						if (toZone.equals("") || toZone == null) {
							continue;
						}
						tmp.add(Integer.parseInt(toZone));
					}
				}
				neighbours.put(id, tmp);
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(PlotTowersDAG.class.getName()).log(Level.SEVERE, null, ex);
		}
		return neighbours;
	}

}
