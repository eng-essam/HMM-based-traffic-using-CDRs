/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netconvert;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import netconvert.Osm.Bounds;
import netconvert.Osm.XMLNode;
import netconvert.Osm.XMLWay;
import utils.DataHandler;
import utils.Edge;
import utils.StdDraw;

/**
 *
 * @author essam
 */
public class netconverter {

	// List<Way> ways;
	// List<Node> nodes;
	protected double xmin;
	protected double ymin;

	protected Bounds b;
	protected List<Node> nodes;
	protected List<Way> ways;
	private List<XMLWay> xmlways;
	private List<XMLNode> xmlnodes;

	private Hashtable<Long, Node> nodes_table;
	Hashtable<String, Color> colors;
	Hashtable<String, Double> width;

	double minlat = -1, minlon = -1;

	List<String> roads = Arrays.asList("motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
			"secondary", "secondary_link", "tertiary", "unclassified", "residential", "service", "road");

	public netconverter() {
	}

	public netconverter(double minlat, double minlon) {
		this.minlat = minlat;
		this.minlon = minlon;

	}

	public List<Edge> construct_map_edges() {
		List<Edge> edges = new ArrayList<>();
		for (Iterator<XMLWay> iterator = xmlways.iterator(); iterator.hasNext();) {
			XMLWay way = iterator.next();

			List<XMLWay.Tag> tags = way.getTag();
			List<XMLWay.Nd> nds = way.getNd();

			String type = get_highway_type(tags);

			if (!roads.contains(type)) {
				continue;
			}

			for (int i = 0; i < nds.size() - 1; i++) {
				XMLWay.Nd nd0 = nds.get(i);
				XMLWay.Nd nd1 = nds.get(i + 1);

				// Node n0 = nodes_table.get(nd0.ref);
				// Node n1 = nodes_table.get(nd1.ref);
				XMLNode n0 = get_xmlnode(nd0.ref);
				XMLNode n1 = get_xmlnode(nd1.ref);

				double xyn0[] = DataHandler.proj_coordinates(n0.lat, n0.lon);
				double xyn1[] = DataHandler.proj_coordinates(n1.lat, n1.lon);

				// shift coordminates
				xyn0[0] = xyn0[0] - xmin;
				xyn1[0] = xyn1[0] - xmin;

				xyn0[1] = xyn0[1] - ymin;
				xyn1[1] = xyn1[1] - ymin;

				String id = way.id + "_#" + i;
				String shape = xyn0[0] + DataHandler.COMMA_SEP + xyn0[1] + DataHandler.SPACE_SEP + xyn1[0]
						+ DataHandler.COMMA_SEP + xyn1[1];

				String from_node = Long.toString(n0.id);
				String to_node = Long.toString(n1.id);
				edges.add(new Edge(id, from_node, to_node, shape, type));
				if (!onway(tags)) {
					id = way.id + "_#" + i + "_" + i;
					shape = xyn1[0] + DataHandler.COMMA_SEP + xyn1[1] + DataHandler.SPACE_SEP + xyn0[0]
							+ DataHandler.COMMA_SEP + xyn0[1];
					from_node = Long.toString(n1.id);
					to_node = Long.toString(n0.id);
					edges.add(new Edge(id, from_node, to_node, shape));
				}
			}

		}
		return edges;
	}

	public List<Edge> construct_map_edges_no_proj(double scale) {
		List<Edge> edges = new ArrayList<>();
		for (Iterator<XMLWay> iterator = xmlways.iterator(); iterator.hasNext();) {
			XMLWay way = iterator.next();

			List<XMLWay.Tag> tags = way.getTag();
			List<XMLWay.Nd> nds = way.getNd();

			String type = get_highway_type(tags);

			if (!roads.contains(type)) {
				continue;
			}

			for (int i = 0; i < nds.size() - 1; i++) {
				XMLWay.Nd nd0 = nds.get(i);
				XMLWay.Nd nd1 = nds.get(i + 1);

				// Node n0 = nodes_table.get(nd0.ref);
				// Node n1 = nodes_table.get(nd1.ref);
				XMLNode n0 = get_xmlnode(nd0.ref);
				XMLNode n1 = get_xmlnode(nd1.ref);

				double xyn0[] = { n0.lat * scale, n0.lon * scale };
				double xyn1[] = { n1.lat * scale, n1.lon * scale };


				String id = way.id + "_#" + i;
				String shape = xyn0[0] + DataHandler.COMMA_SEP + xyn0[1] + DataHandler.SPACE_SEP + xyn1[0]
						+ DataHandler.COMMA_SEP + xyn1[1];

				String from_node = Long.toString(n0.id);
				String to_node = Long.toString(n1.id);
				edges.add(new Edge(id, from_node, to_node, shape, type));
				if (!onway(tags)) {
					id = way.id + "_#" + i + "_" + i;
					shape = xyn1[0] + DataHandler.COMMA_SEP + xyn1[1] + DataHandler.SPACE_SEP + xyn0[0]
							+ DataHandler.COMMA_SEP + xyn0[1];
					from_node = Long.toString(n1.id);
					to_node = Long.toString(n0.id);
					edges.add(new Edge(id, from_node, to_node, shape));
				}
			}

		}
		return edges;
	}

	/**
	 * get highway type from tags list ...
	 *
	 * @param tags
	 * @return
	 */
	public String get_highway_type(List<XMLWay.Tag> tags) {
		for (Iterator<XMLWay.Tag> iterator = tags.iterator(); iterator.hasNext();) {
			XMLWay.Tag t = iterator.next();
			if (t.getK().equals("highway")) {
				return t.getV().toLowerCase();
			}
		}
		return "";
	}

	/**
	 * Get node using node id ....
	 *
	 * @param id
	 * @return
	 */
	public Node get_node(long id) {
		for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
			Node nd = iterator.next();
			if (nd.id == id) {
				return nd;
			}
		}
		return null;
	}

	public XMLNode get_xmlnode(long id) {
		for (XMLNode nd : xmlnodes) {
			if (nd.id == id) {
				return nd;
			}
		}
		return null;
	}

	public Bounds getB() {
		return b;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public List<XMLWay> getWays() {
		return xmlways;
	}

	public double getXmin() {
		return xmin;
	}

	public double getYmin() {
		return ymin;
	}

	public boolean onway(List<XMLWay.Tag> tags) {
		for (Iterator<XMLWay.Tag> iterator = tags.iterator(); iterator.hasNext();) {
			XMLWay.Tag t = iterator.next();
			if (t.k.compareTo("oneway") == 0 && t.v.compareTo("yes") == 0) {
				return true;
			}
		}
		return false;
	}

	public void plot_cell_networks(List<String[]> towers) {
		for (Iterator<String[]> iterator = towers.iterator(); iterator.hasNext();) {
			double[] t = DataHandler.to_double(iterator.next());
			double xy[] = DataHandler.proj_coordinates(t[2], t[1]);

			if (t[0] == 2) {
				// StdDraw.picture(xy[0], xy[1], "vf_038434.png", 60, 60);
				StdDraw.setPenColor(Color.RED);
			} else if (t[0] == 1) {
				StdDraw.setPenColor(Color.ORANGE);
			} else {
				StdDraw.setPenColor(Color.GREEN);
			}

			StdDraw.filledCircle(xy[0] - xmin, xy[1] - ymin, 25);

		}

	}

	public void plot_map(boolean unicolor) {

		colors = new Hashtable<>();
		colors.put("motorway", Color.decode("#bfbfcf"));
		colors.put("motorway_link", Color.decode("#bfbfcf"));
		colors.put("trunk", Color.decode("#c8d8c8"));
		colors.put("trunk_link", Color.decode("#c8d8c8"));
		colors.put("primary", Color.decode("#d8c8c8"));
		colors.put("primary_link", Color.decode("#d8c8c8"));
		colors.put("secondary", Color.decode("#eeeec9"));
		colors.put("secondary_link", Color.decode("#eeeec9"));
		colors.put("tertiary", Color.decode("#999999"));
		colors.put("unclassified", Color.decode("#999999"));
		colors.put("residential", Color.decode("#993333"));
		colors.put("service", Color.decode("#990000"));
		colors.put("road", Color.decode("#990000"));

		width = new Hashtable<>();
		width.put("motorway", 7.0 / 3.0);
		width.put("motorway_link", 7.0 / 3.0);
		width.put("trunk", 7.0 / 3.0);
		width.put("trunk_link", 7.0 / 3.0);
		width.put("primary", 7.0 / 3.0);
		width.put("primary_link", 7.0 / 3.0);
		width.put("secondary", 7.0 / 3.0);
		width.put("secondary_link", 7.0 / 3.0);
		width.put("tertiary", 5.0 / 3.0);
		width.put("unclassified", 5.0 / 3.0);
		width.put("residential", 1.0);
		width.put("service", 1.0);
		width.put("road", 1.0);

		double pen_radius = 0.0002;
		StdDraw.setPenColor(Color.GRAY);
		StdDraw.setPenRadius(pen_radius);

		for (Iterator<XMLWay> iterator = xmlways.iterator(); iterator.hasNext();) {
			XMLWay way = iterator.next();
			List<XMLWay.Tag> tags = way.getTag();
			List<XMLWay.Nd> nds = way.getNd();

			String highway = get_highway_type(tags);
			if (colors.containsKey(highway)) {
				if (!unicolor) {
					StdDraw.setPenColor(colors.get(highway));
				}
				StdDraw.setPenRadius(pen_radius * width.get(highway));
			}
			for (int i = 0; i < nds.size() - 1; i++) {
				XMLWay.Nd nd0 = nds.get(i);
				XMLWay.Nd nd1 = nds.get(i + 1);

				XMLNode n0 = get_xmlnode(nd0.ref);
				XMLNode n1 = get_xmlnode(nd1.ref);
				// Node n0 = nodes_table.get(nd0.ref);
				// Node n1 = nodes_table.get(nd1.ref);

				double xyn0[] = DataHandler.proj_coordinates(n0.lat, n0.lon);
				double xyn1[] = DataHandler.proj_coordinates(n1.lat, n1.lon);

				// shift coordminates
				xyn0[0] = xyn0[0] - xmin;
				xyn1[0] = xyn1[0] - xmin;

				xyn0[1] = xyn0[1] - ymin;
				xyn1[1] = xyn1[1] - ymin;

				StdDraw.line(xyn0[0], xyn0[1], xyn1[0], xyn1[1]);

				// StdDraw.line(n0.lat, n0.lon, n1.lat, n1.lon);
			}
		}
	}

	/**
	 *
	 * @param shift_flag
	 */
	private void proj_nodes() {
		nodes = new ArrayList<>();
		nodes_table = new Hashtable<>();

		for (Iterator<XMLNode> iterator = xmlnodes.iterator(); iterator.hasNext();) {
			XMLNode nd = iterator.next();

			double[] xy = DataHandler.proj_coordinates(nd.lat, nd.lon);
			Node d;
			// if (shift_flag) {
			d = new Node(nd.id, nd.lat, nd.lon, (xy[0] - xmin), (xy[1] - ymin));
			// } else {
			// d = new Node(nd.id, nd.lat, nd.lon, xy[0], xy[1]);
			// }
			nodes.add(d);
			nodes_table.put(d.id, d);
		}

	}

	/**
	 *
	 * @param path
	 * @param shift_bounding_box
	 */
	public void read_osm(String path) {
		try {

			File file = new File(path);
			JAXBContext jaxbContext = JAXBContext.newInstance(Osm.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Osm osm = (Osm) jaxbUnmarshaller.unmarshal(file);
			this.b = osm.getBounds();
			if (minlat == -1 || minlon == -1) {
				minlat = b.minlat;
				minlon = b.minlon;
			}
			double[] xy = DataHandler.proj_coordinates(minlat, minlon);
			this.xmin = xy[0];
			this.ymin = xy[1];

			this.xmlways = osm.getWay();
			this.xmlnodes = osm.getNode();

			// proj_nodes();
			// set the minimum point for shifting the network ....
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public void setB(Bounds b) {
		this.b = b;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void setWays(List<XMLWay> ways) {
		this.xmlways = ways;
	}

	public void setXmin(double xmin) {
		this.xmin = xmin;
	}

	public void setYmin(double ymin) {
		this.ymin = ymin;
	}

}
