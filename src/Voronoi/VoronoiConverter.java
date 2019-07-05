/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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

import diva.util.java2d.Polygon2D;
import mergexml.MapDetails;
import mergexml.WriteXMLFile;
import utils.Edge;
import utils.FromNode;
import utils.Region;
import utils.ToNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class VoronoiConverter {

	public final String[] main_roads = { "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
			"secondary", "secondary_link", "tertiary", "unclassified", "residential" };
	public final String[] all_roads = { "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
			"secondary", "secondary_link", "tertiary", "unclassified", "residential", "service", "road" };

	public static Hashtable<Integer, Vertex> readTower(String path) {
		Hashtable<Integer, Vertex> towers = new Hashtable<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			Vertex vertex;
			Region region = null;
			boolean flag = false;
			while ((line = reader.readLine()) != null) {
				line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String lineSplit[] = line.split(",");
				vertex = new Vertex();
				int ps = Integer.parseInt(lineSplit[0]);
				vertex.x = Double.parseDouble(lineSplit[1]);
				vertex.y = Double.parseDouble(lineSplit[2]);

				towers.put(ps, vertex);

			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		}

		return towers;
	}

	public ArrayList<FromNode> determine_edges_cut_zones(String ProbPath, String voronoiPath, List<Edge> edges) {
		/**
		 * table contain edges that cut the boundary of zones. Hash table of
		 * <edge id, zone contain the geometric start of this edge>.
		 */
		Hashtable<String, Integer> cut_edges = new Hashtable<>();

		ArrayList<FromNode> probNodes = new MapDetails().readMap(ProbPath);
		System.out.format("Initial network reading completed with %d node(s)\n", probNodes.size());

		ArrayList<Region> voronoiRegions = readVoronoi(voronoiPath);

		System.out.println("voronoiRegions\t" + voronoiRegions.size());

		Regions: for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}
			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.vertices;
			double[] coord = new double[2 * vertices.size()];
			int i = 0;
			for (Vertex vert : vertices) {
				coord[i++] = vert.x;
				coord[i++] = vert.y;
			}
			Polygon2D.Double poly = new Polygon2D.Double(coord);

			for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
				Edge edge = it.next();
				String id = edge.getId();
				if (cut_edges.containsKey(id)) {
					continue;
				}
				String pnts[] = edge.getShape().split(" ");
				String strt[] = pnts[0].split(",");
				String end[] = pnts[pnts.length - 1].split(",");
				/**
				 * If start is in a polygon and the end is out and vice versa,
				 * then this edge is an edge that cut the boundary of a specific
				 * polygon.
				 *
				 * the edge will belong to the polygon contain the geometric
				 * start of this edge.
				 *
				 */

				if ((poly.contains(Double.parseDouble(strt[0]), Double.parseDouble(strt[1]))
						&& !poly.contains(Double.parseDouble(end[0]), Double.parseDouble(
								end[1]))) /*
											 * ||
											 * (poly.contains(Double.parseDouble
											 * (end[0]),
											 * Double.parseDouble(end[1])) &&
											 * !poly.contains(Double.parseDouble
											 * (strt[0]),
											 * Double.parseDouble(strt[1])))
											 */) {
					/**
					 * Test edge's start that may belong to more than one zone.
					 * <Impossible suituation>.
					 */

					cut_edges.put(id, region.id);
				}

			}
		}

		/**
		 * Modify the map informations
		 */
		for (FromNode node : probNodes) {

			if (cut_edges.containsKey(node.getID())) {
				int index = probNodes.indexOf(node);
				node.setIsExit(true);
				node.setZone(cut_edges.get(node.getID()).intValue());
				probNodes.set(index, node);
			}

		}

		return probNodes;
	}

	public ArrayList<FromNode> determine_edges_cut_zones(String ProbPath, String voronoiPath, List<Edge> edges,
			List<String> roads) {
		/**
		 * table contain edges that cut the boundary of zones. Hash table of
		 * <edge id, zone contain the geometric start of this edge>.
		 */
		Hashtable<String, Integer> cut_edges = new Hashtable<>();

		ArrayList<FromNode> probNodes = new MapDetails().readMap(ProbPath);
		System.out.format("Initial network reading completed with %d node(s)\n", probNodes.size());

		ArrayList<Region> voronoiRegions = readVoronoi(voronoiPath);

		System.out.println("voronoiRegions\t" + voronoiRegions.size());

		Regions: for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}
			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.vertices;
			double[] coord = new double[2 * vertices.size()];
			int i = 0;
			for (Vertex vert : vertices) {
				coord[i++] = vert.x;
				coord[i++] = vert.y;
			}
			Polygon2D.Double poly = new Polygon2D.Double(coord);

			for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
				Edge edge = it.next();
				String id = edge.getId();

				if (cut_edges.containsKey(id) || !roads.contains(edge.getType())) {
					continue;
				}
				String pnts[] = edge.getShape().split(" ");
				String strt[] = pnts[0].split(",");
				String end[] = pnts[pnts.length - 1].split(",");
				/**
				 * If start is in a polygon and the end is out and vice versa,
				 * then this edge is an edge that cut the boundary of a specific
				 * polygon.
				 *
				 * the edge will belong to the polygon contain the geometric
				 * start of this edge.
				 *
				 */

				if ((poly.contains(Double.parseDouble(strt[0]), Double.parseDouble(strt[1]))
						&& !poly.contains(Double.parseDouble(end[0]), Double.parseDouble(
								end[1]))) /*
											 * ||
											 * (poly.contains(Double.parseDouble
											 * (end[0]),
											 * Double.parseDouble(end[1])) &&
											 * !poly.contains(Double.parseDouble
											 * (strt[0]),
											 * Double.parseDouble(strt[1])))
											 */) {
					/**
					 * Test edge's start that may belong to more than one zone.
					 * <Impossible suituation>.
					 */

					cut_edges.put(id, region.id);
				}

			}
		}

		/**
		 * Modify the map informations
		 */
		for (FromNode node : probNodes) {

			if (cut_edges.containsKey(node.getID())) {
				int index = probNodes.indexOf(node);
				node.setIsExit(true);
				node.setZone(cut_edges.get(node.getID()).intValue());
				probNodes.set(index, node);
			}

		}

		return probNodes;
	}

	/**
	 * Identify exits with more than one class of roads
	 * 
	 * @param ProbPath
	 * @param voronoiPath
	 * @param edges
	 * @param roads_class
	 * @return
	 */
	public ArrayList<FromNode> determine_edges_cut_zones(String ProbPath, String voronoiPath, List<Edge> edges,
			int roads_class) {

		/**
		 * table contain edges that cut the boundary of zones. Hash table of
		 * <edge id, zone contain the geometric start of this edge>.
		 */

		List<String> rt = new ArrayList<>();
		boolean restrict_roads_flag = false;
		if (roads_class == 0) {
			rt = Arrays.asList(main_roads);
			restrict_roads_flag = true;
		} else if (roads_class == 1) {
			rt = Arrays.asList(all_roads);
			restrict_roads_flag = true;
		}

		Hashtable<String, Integer> cut_edges = new Hashtable<>();

		ArrayList<FromNode> probNodes = new MapDetails().readMap(ProbPath);
		System.out.format("Initial network reading completed with %d node(s)\n", probNodes.size());

		ArrayList<Region> voronoiRegions = readVoronoi(voronoiPath);

		System.out.println("voronoiRegions\t" + voronoiRegions.size());

		Regions: for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}
			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.vertices;
			double[] coord = new double[2 * vertices.size()];
			int i = 0;
			for (Vertex vert : vertices) {
				coord[i++] = vert.x;
				coord[i++] = vert.y;
			}
			Polygon2D.Double poly = new Polygon2D.Double(coord);

			for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
				Edge edge = it.next();
				String id = edge.getId();

				if (cut_edges.containsKey(id)) {
					continue;
				}
			
				//skip current edge if it is not on the restricted roads class
				if (!rt.contains(edge.getType()) && restrict_roads_flag)
					continue;

				String pnts[] = edge.getShape().split(" ");
				String strt[] = pnts[0].split(",");
				String end[] = pnts[pnts.length - 1].split(",");
				/**
				 * If start is in a polygon and the end is out and vice versa,
				 * then this edge is an edge that cut the boundary of a specific
				 * polygon.
				 *
				 * the edge will belong to the polygon contain the geometric
				 * start of this edge.
				 *
				 */

				if ((poly.contains(Double.parseDouble(strt[0]), Double.parseDouble(strt[1]))
						&& !poly.contains(Double.parseDouble(end[0]), Double.parseDouble(
								end[1]))) /*
											 * ||
											 * (poly.contains(Double.parseDouble
											 * (end[0]),
											 * Double.parseDouble(end[1])) &&
											 * !poly.contains(Double.parseDouble
											 * (strt[0]),
											 * Double.parseDouble(strt[1])))
											 */) {
					/**
					 * Test edge's start that may belong to more than one zone.
					 * <Impossible suituation>.
					 */

					cut_edges.put(id, region.id);
				}

			}
		}

		/**
		 * Modify the map informations
		 */
		for (FromNode node : probNodes) {

			if (cut_edges.containsKey(node.getID())) {
				int index = probNodes.indexOf(node);
				node.setIsExit(true);
				node.setZone(cut_edges.get(node.getID()).intValue());
				probNodes.set(index, node);
			}

		}

		return probNodes;
	}

	// public static void main(String[] args) {
	// String dPath = args[0];
	// String xmlPath = args[1];
	// writeProbFile(dPath, xmlPath);
	// }
	public ArrayList<FromNode> DetermineExits(String ProbPath, String voronoiPath) {

		/**
		 * boundary regions points wraps around the map, so regions cut down the
		 * map into smaller number of regions
		 */
		// double xmin, ymin, xmax, ymax;
		// xmin = 227761.06;
		// xmax = 240728.96;
		// ymin = 1620859.93;
		// ymax = 1635128.30;
		ArrayList<FromNode> probNodes = new MapDetails().readMap(ProbPath);
		System.out.format("Initial network reading completed with %d node(s)\n", probNodes.size());

		ArrayList<FromNode> tmpProbNodes = new ArrayList<>();

		ArrayList<Region> voronoiRegions = readVoronoi(voronoiPath);
		System.out.println("voronoiRegions\t" + voronoiRegions.size());

		Regions: for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}
			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.vertices;
			double[] coord = new double[2 * vertices.size()];
			int i = 0;
			for (Vertex vert : vertices) {
				/**
				 * error temp solution to avoid error in wrap around regions
				 */
				// if(vert.x<xmin||vert.x>xmax||vert.y<ymin||vert.y>ymax)
				// continue Regions;
				coord[i++] = vert.x;
				coord[i++] = vert.y;
			}

			// int xpoints[] = new int[vertices.size()];
			// int ypoints[] = new int[vertices.size()];
			//
			// for (Vertex vert : vertices) {
			//
			// xpoints[i] = (int) vert.x;
			// ypoints[i] = (int) vert.y;
			// i++;
			// }
			//
			// Polygon poly = new Polygon(xpoints, ypoints, vertices.size());
			Polygon2D.Double poly = new Polygon2D.Double(coord);

			for (FromNode edge : probNodes) {
				int index = probNodes.indexOf(edge);
				/**
				 * Error here skip visited nodes
				 */
				// if (edge.getZone() != -1) {
				// continue;
				// }
				if (poly.contains(edge.getX(), edge.getY())) {
					edge.setZone(region.id);
					// System.out.println("In point");
					ArrayList<ToNode> toNodes = edge.getToedges();

					for (ToNode next : toNodes) {
						/**
						 * If any to point is out of the polygon, means that
						 * this form point is an exit point
						 */
						if (!poly.contains(next.getX(), next.getY())) {
							// if(region.id==1)
							// System.out.println("1st");
							edge.setIsExit(true);
							tmpProbNodes.add(edge);
							break;
						}
					}

				}

				probNodes.set(index, edge);

			}

		}

		// tmpProbNodes.trimToSize();
		return probNodes;
	}

	public ArrayList<FromNode> determineVorZones(String ProbPath, String voronoiPath) {
		ArrayList<FromNode> probNodes = new MapDetails().readMap(ProbPath);
		ArrayList<Region> voronoiRegions = readVoronoi(voronoiPath);
		ArrayList<FromNode> exitPoints = new ArrayList<>();
		String znsDir = voronoiPath.substring(0, voronoiPath.lastIndexOf("/")) + "/voronoi-zones";
		new File(znsDir).mkdirs();
		ArrayList<FromNode> vorRogionPnts = new ArrayList<>();

		for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}
			ArrayList<Vertex> vertices = region.vertices;
			double[] coord = new double[2 * vertices.size()];
			int i = 0;
			for (Vertex vert : vertices) {
				coord[i++] = vert.x;
				coord[i++] = vert.y;
			}
			Polygon2D poly = new Polygon2D.Double(coord);

			for (int j = 0; j < probNodes.size(); j++) {

				FromNode edge = probNodes.get(j);
				if (poly.contains(edge.getX(), edge.getY())) {
					edge.setZone(region.id);
					ArrayList<ToNode> toNodes = edge.getToedges();
					// ArrayList<ToNodes> toExtNodes = new ArrayList<>();
					boolean isExt = false;
					for (ToNode next : toNodes) {
						/**
						 * If any to point is out of the polygon, means that
						 * this form point is an exit point
						 */
						if (!poly.contains(next.getX(), next.getY())) {
							edge.setIsExit(true);
							isExt = true;
							break;
						}

					}
					if (isExt) {
						exitPoints.add(edge);
						isExt = false;
					}
					vorRogionPnts.add(edge);
				}
				probNodes.set(j, edge);

			}
			/**
			 * Write individual regions points /** Write individual regions
			 * points /** Write individual regions points /** Write individual
			 * regions points /** Write individual regions points /** Write
			 * individual regions points /** Write individual regions points /**
			 * Write individual regions points
			 */

			new WriteXMLFile(znsDir + "/voronoi-zone_" + region.id + ".xml", vorRogionPnts).writeProbFile();
			vorRogionPnts.clear();

		}
		new WriteXMLFile(voronoiPath.substring(0, voronoiPath.lastIndexOf(".")) + ".extPnts.xml", exitPoints)
				.writeProbFile();
		return probNodes;
	}

	public ArrayList<Integer> getZones(ArrayList<Region> vor) {
		ArrayList<Integer> zoneIds = new ArrayList<>();
		for (Iterator<Region> it = vor.iterator(); it.hasNext();) {
			Region reg = it.next();
			zoneIds.add(reg.id);

		}
		return zoneIds;
	}

	public Hashtable<Integer, ArrayList<Integer>> readVorNeighbors(String path) {

		Hashtable<Integer, ArrayList<Integer>> map = new Hashtable<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			int id = -1;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String lineSplit[] = line.split(",");

				if (!line.startsWith(",")) {
					id = Integer.parseInt(lineSplit[0].trim());

				} else {
					ArrayList<Integer> neighbors = new ArrayList<>();
					for (int i = 1; i < lineSplit.length; i++) {
						neighbors.add(Integer.parseInt(lineSplit[i]));
					}
					map.put(id, neighbors);
				}

			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		}

		return map;
	}

	public ArrayList<Region> readVoronoi(String dPath) {
		ArrayList<Region> regions = new ArrayList<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(dPath));
			String line;
			Vertex vertex;
			Region region = null;
			boolean flag = false;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				if (!line.startsWith(",")) {
					if (flag) {
						regions.add(region);
					}
					region = new Region();
					region.id = Integer.parseInt(lineSplit[0].trim());
					// regions.add(region);
					flag = true;
				} else {
					vertex = new Vertex();
					vertex.x = Double.parseDouble(lineSplit[1]);
					vertex.y = Double.parseDouble(lineSplit[2]);
					region.vertices.add(vertex);
				}
			}
			regions.add(region);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		}
		return regions;
	}

	public void writeProbFile(String path, ArrayList<FromNode> edges) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("turn-defs");
			doc.appendChild(rootElement);

			for (FromNode fromNode : edges) {
				Element fromNodeElement = doc.createElement("fromNode");
				rootElement.appendChild(fromNodeElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(fromNode.getID());
				fromNodeElement.setAttributeNode(attr);

				attr = doc.createAttribute("x");
				attr.setValue(String.valueOf(fromNode.getX()));
				fromNodeElement.setAttributeNode(attr);

				attr = doc.createAttribute("y");
				attr.setValue(String.valueOf(fromNode.getY()));
				fromNodeElement.setAttributeNode(attr);

				attr = doc.createAttribute("isExt");
				attr.setValue(String.valueOf(fromNode.isIsExit()));
				fromNodeElement.setAttributeNode(attr);

				attr = doc.createAttribute("zone");
				attr.setValue(String.valueOf(fromNode.getZone()));
				fromNodeElement.setAttributeNode(attr);

				// System.out.println(fromNode.ID + "\t" + fromNode.x + "\t" +
				// fromNode.y);
				ArrayList<ToNode> toNodesList = fromNode.getToedges();
				for (ToNode toNode : toNodesList) {
					Element toNodeElement = doc.createElement("toNode");
					fromNodeElement.appendChild(toNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(toNode.getID());
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("probability");
					attr.setValue(String.valueOf(toNode.getProb()));
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("x");
					attr.setValue(String.valueOf(toNode.getX()));
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("y");
					attr.setValue(String.valueOf(toNode.getY()));
					toNodeElement.setAttributeNode(attr);

					// attr = doc.createAttribute("isExt");
					// attr.setValue(String.valueOf(toNode.isExit));
					// toNodeElement.setAttributeNode(attr);
					//
					// attr = doc.createAttribute("zone");
					// attr.setValue(String.valueOf(toNode.zone));
					// toNodeElement.setAttributeNode(attr);
					// System.out.println("\t" + toNode.ID + "\t" + toNode.Prob
					// + "\t" + toNode.x + "\t" + toNode.y);
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException | TransformerException pce) {
		}

	}

	public void writeVoronoiFile(String dPath, String xmlPath) {

		try {
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(dPath));
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("vor-boudaries");
			doc.appendChild(rootElement);

			String line;
			Element fromNodeElement = null;
			Attr attr;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				if (!line.startsWith(",")) {
					fromNodeElement = doc.createElement("region");

					rootElement.appendChild(fromNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(lineSplit[0]);
					fromNodeElement.setAttributeNode(attr);
				} else {
					// System.out.println("vert");
					Element toNodeElement = doc.createElement("vertex");
					fromNodeElement.appendChild(toNodeElement);
					// System.out.println(lineSplit[1] + "," + lineSplit[2]);

					Attr vattr = doc.createAttribute("x");
					vattr.setValue(lineSplit[1]);
					toNodeElement.setAttributeNode(vattr);

					vattr = doc.createAttribute("y");
					vattr.setValue(lineSplit[2]);
					toNodeElement.setAttributeNode(vattr);

					//
				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(xmlPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException | TransformerException pce) {
		} catch (FileNotFoundException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(VoronoiConverter.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
