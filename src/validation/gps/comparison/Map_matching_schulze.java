/**
 * 
 */
package validation.gps.comparison;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Viterbi.WieghtedGraph;
import Voronoi.VoronoiConverter;
import diva.util.java2d.Polygon2D;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.GPSTrkPnt;
import utils.Region;
import utils.StdDraw;
import utils.Vertex;

/**
 * @author essam
 *
 */
public class Map_matching_schulze {

	/**
	 * Sort hashtable values ...
	 * 
	 * @author essam
	 *
	 */
	class ValueComparator implements Comparator<String> {

		Hashtable<String, Double> base;

		public ValueComparator(Hashtable<String, Double> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return 1;
			} else {
				return -1;
			} // returning 0 would merge keys
		}

	}

	public static String[] colors = { "#FFFF00", "#808000", "#00FF00", "#008000", "#00FFFF", "#0000FF", "#000080",
			"#7D6608", "#D4AC0D", "#BA4A00", "#2E86C1", "#8E44AD", "#9B59B6", "#CB4335", "#C0392B", "#DC7633",
			"#EC7063", "#2C3E50", "#7FB3D5" };

	/**
	 * sample straight line into smaller segments ...
	 * 
	 * @param p0
	 * @param p1
	 * @param increment
	 * @return
	 */
	public static List<Vertex> segment_line(Vertex p0, Vertex p1, double increment) {

		List<Vertex> segs = new ArrayList<>();

		// check start of the segment ..
		if (p1.getX() < p0.getX()) {
			Vertex tmp = new Vertex(p0.getX(), p0.getY());
			p0.setLocation(p1.getX(), p1.getY());
			p1.setLocation(tmp.getX(), tmp.getY());
		}

		double dy = p1.getY() - p0.getY();
		double dx = p1.getX() - p0.getX();

		if (dy == 0) {
			// horizontal line
			segs.add(p0);
			for (int i = 1; i <= (dx / increment); i++) {
				segs.add(new Vertex(p0.getX() + i * increment, p0.getY()));
			}
			segs.add(p1);

		} else if (dx == 0) {
			// vertical line
			if (p1.getY() < p0.getY()) {
				Vertex tmp = new Vertex(p0.getX(), p0.getY());
				p0.setLocation(p1.getX(), p1.getY());
				p1.setLocation(tmp.getX(), tmp.getY());
			}

			segs.add(p0);
			for (int i = 1; i <= (dy / increment); i++) {
				segs.add(new Vertex(p0.getX(), p0.getY() + i * increment));
			}
			segs.add(p1);

		} else {
			// normal situations
			double m = (dy / dx);
			double C = p0.getY() - m * p0.getX();
			double length = DataHandler.euclidean(p0.getX(), p0.getY(), p1.getX(), p1.getY());

			segs.add(p0);
			double x = p0.getX(), y = p0.getY();
			for (int i = 1; i <= (length / increment); i++) {
				double A = x;
				double B = y;

				double a = 1 + Math.pow(m, 2);
				double b = 2 * m * (C - B) - 2 * A;
				double c = Math.pow(A, 2) + Math.pow(B, 2) + Math.pow(C, 2) - 2 * B * C - Math.pow(increment, 2);

				// System.out.println(m + "\t" + A + "\t" + B + "\t" + a + "\t"
				// + b + "\t" + c);
				double q_rs = Math.sqrt(Math.pow(b, 2) - 4 * a * c);
				// System.out.println(q_rs);

				double q_x = (-b + q_rs) / (2 * a);
				double q_x1 = (-b - q_rs) / (2 * a);

				if (q_x > x && q_x < p1.getX()) {
					x = q_x;
				} else {
					x = q_x1;
				}
				y = x * m + C;
				segs.add(new Vertex(x, y));
			}
			segs.add(p1);

		}
		return segs;
	}

	Hashtable<String, Edge> edgesMap;

	/**
	 * 
	 * @param network_file_path
	 * @return WieghtedGraph
	 */
	public WieghtedGraph construct_traffic_graph(String network_file_path) {
		DataHandler adaptor = new DataHandler();
		ArrayList<FromNode> map = adaptor.readNetworkDist(network_file_path);

		if (map == null || map.isEmpty()) {
			return null;
		}

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(map);
		System.out.println("End building graph ...");

		return graph;
	}

	// public WieghtedGraph construct_search_graph(String network_file_path,
	// Hashtable<Integer, Vertex> twrs, String obs[],
	// double deviation, double redius, List<String> selected_edges) {
	// DataHandler adaptor = new DataHandler();
	// List<FromNode> map = adaptor.readNetworkDist(network_file_path);
	//
	// if (map == null || map.isEmpty()) {
	// return null;
	// }
	//
	// double geo_radius = redius / get_geofactor(twrs);
	// List<FromNode> subgraph = new ArrayList<>();
	// // List<Integer> tmp = new ArrayList<>();
	//
	// for (int i = 0; i < obs.length - 1; i++) {
	// if (obs[i] == obs[i + 1])
	// continue;
	// Vertex v0 = twrs.get(Integer.parseInt(obs[i]));
	// Vertex v1 = twrs.get(Integer.parseInt(obs[i + 1]));
	// System.out.print(i + ",");
	// Vertex cntr = new Vertex((v0.x + v1.x) / 2, (v0.y + v1.y) / 2);
	// double n_radius = DataHandler.euclidean(v0.x, v0.y, cntr.x, cntr.y) + 2;
	//// System.out.println(n_radius);
	// List<FromNode> tmpsubgraph = find_corridor_map_edges(map, cntr,
	// n_radius);
	//// StdDraw.filledCircle(cntr.x, cntr.y, n_radius);
	// tmpsubgraph.stream().forEach(fn -> {
	// if (!subgraph.contains(fn) && fn != null) {
	// subgraph.add(fn);
	// selected_edges.add(fn.ID);
	// }
	// });
	//
	// }
	//
	//// System.out.println("Number of edges: " + selected_edges.size());
	//
	// WieghtedGraph graph = new WieghtedGraph();
	// graph.constructMap(subgraph);
	// System.out.println("End building graph ...");
	//
	// return graph;
	//
	// }

	public WieghtedGraph construct_search_graph(String network_file_path, Hashtable<Integer, Vertex> twrs, String obs[],
			double deviation, double radius, double scale, List<String> selected_edges) {

		
		DataHandler adaptor = new DataHandler();
		List<FromNode> map = adaptor.readNetworkDist(network_file_path);

		if (map == null || map.isEmpty()) {
			return null;
		}

		// double geo_radius = redius / get_geofactor(twrs);
		List<FromNode> subgraph = new ArrayList<>();
		// List<Integer> tmp = new ArrayList<>();

		for (int i = 0; i < obs.length - 1; i++) {
			if (obs[i] == obs[i + 1])
				continue;
			Vertex v0 = twrs.get(Integer.parseInt(obs[i]));
			Vertex v1 = twrs.get(Integer.parseInt(obs[i + 1]));

			double[] v0c = DataHandler.proj_coordinates(v0.x / scale, v0.y / scale);
			double[] v1c = DataHandler.proj_coordinates(v1.x / scale, v1.y / scale);

			System.out.print(i + ",");
			Vertex cntr = new Vertex((v0c[0] + v1c[0]) / 2, (v0c[1] + v1c[1]) / 2);
			double n_radius = DataHandler.euclidean(v0c[0], v0c[1], cntr.x, cntr.y) + radius;
			List<FromNode> tmpsubgraph = find_corridor_map_edges(map, cntr, n_radius, scale);
			tmpsubgraph.stream().forEach(fn -> {
				if (!subgraph.contains(fn) && fn != null) {
					subgraph.add(fn);
					selected_edges.add(fn.ID);
				}
			});

		}

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(subgraph);
		System.out.println("End building graph ...");

		return graph;

	}

	public WieghtedGraph construct_search_graph(String network_file_path, Hashtable<Integer, Vertex> twrs, String obs[],
			double deviation, double radius, double scale) {
		DataHandler adaptor = new DataHandler();
		List<FromNode> map = adaptor.readNetworkDist(network_file_path);

		if (map == null || map.isEmpty()) {
			return null;
		}

		// double geo_radius = redius / get_geofactor(twrs);
		List<FromNode> subgraph = new ArrayList<>();
		// List<Integer> tmp = new ArrayList<>();

		for (int i = 0; i < obs.length - 1; i++) {
			if (obs[i] == obs[i + 1])
				continue;
			Vertex v0 = twrs.get(Integer.parseInt(obs[i]));
			Vertex v1 = twrs.get(Integer.parseInt(obs[i + 1]));

			double[] v0c = DataHandler.proj_coordinates(v0.x / scale, v0.y / scale);
			double[] v1c = DataHandler.proj_coordinates(v1.x / scale, v1.y / scale);

			System.out.print(i + ",");
			Vertex cntr = new Vertex((v0c[0] + v1c[0]) / 2, (v0c[1] + v1c[1]) / 2);
			double n_radius = DataHandler.euclidean(v0c[0], v0c[1], cntr.x, cntr.y) + radius;
			List<FromNode> tmpsubgraph = find_corridor_map_edges(map, cntr, n_radius, scale);
			tmpsubgraph.stream().forEach(fn -> {
				if (!subgraph.contains(fn) && fn != null) {
					subgraph.add(fn);
				}
			});

		}

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(subgraph);
		System.out.println("End building graph ...");

		return graph;

	}

	private List<FromNode> find_corridor_map_edges(List<FromNode> map, Vertex cntr, double radius, double scale) {
		List<FromNode> subgraph = new ArrayList<>();
		Ellipse2D circle2D = new Ellipse2D.Double(cntr.x - radius, cntr.y - radius, 2.0 * radius, 2.0 * radius);

		map.stream().forEach(fn -> {
			double[] p = DataHandler.proj_coordinates(fn.x / scale, fn.y / scale);
			if (circle2D.contains(p[0], p[1]))
				subgraph.add(fn);
		});
		return subgraph;
	}

	// private double get_geofactor(Hashtable<Integer, Vertex> twrs,double
	// scale) {
	// double sum = 0;
	// List<Vertex> vs = new ArrayList<>(twrs.values());
	// for (int i = 0; i < vs.size() - 1; i++) {
	// double[] v0 =
	// DataHandler.proj_coordinates(vs.get(i).x/scale,vs.get(i).y/scale);
	// double[] v1 =
	// DataHandler.proj_coordinates(vs.get(i+1).x/scale,vs.get(i+1).y/scale);
	// double d1 = DataHandler.geodistance(v0[0], v0[1], v1[0], v1[1], "m");
	// double d2 = DataHandler.euclidean(v0.x, v0.y, v1.x, v1.y);
	// sum += d1 / d2;
	// // System.out.println(d1 + "\t" + d2 + "\t" + d1 / d2);
	// }
	// return (sum / (vs.size() - 1));
	// }

	/**
	 * Schulze 2015 proposed method
	 * 
	 * @param network_file_path
	 * @param close_twrs_edges
	 * @param trk_pnts_list
	 * @return
	 */
	public List<String> find_route(WieghtedGraph graph, Hashtable<Integer, List<String>> close_twrs_edges,
			List<GPSTrkPnt> trk_pnts_list, double max_search_dist) {

		List<String> route = new ArrayList<>();
		// iterate observations ..
		for (int i = 1; i < trk_pnts_list.size(); i++) {
			String strt = "";
			String end = "";
			/**
			 * for the first part of the trip, iterate over all selected
			 * segments in the first two consecutive observations .... else
			 * check the distances from the end of the previous, to the current,
			 * observation to all segment in the next observations ..
			 * 
			 */
			if (route.isEmpty()) {

				int obs0 = trk_pnts_list.get(i - 1).zone;
				int obs1 = trk_pnts_list.get(i).zone;

				List<String> z0_near_seg = close_twrs_edges.get(obs0);
				List<String> z1_near_seg = close_twrs_edges.get(obs1);

				double[][] dists = new double[z0_near_seg.size()][z1_near_seg.size()];

				z0_near_seg.parallelStream().forEach(z0_edge -> {

					z1_near_seg.parallelStream().forEach(z1_edge -> {
						dists[z0_near_seg.indexOf(z0_edge)][z1_near_seg.indexOf(z1_edge)] = graph.getPathLength(z0_edge,
								z1_edge, max_search_dist);
					});
				});
				// find the shortest path length within the stored matrix then
				// get the shortest path's and record its start and end segment
				double tmp_min = dists[0][0];
				strt = z0_near_seg.get(0);
				end = z1_near_seg.get(0);

				for (int j = 0; j < z0_near_seg.size(); j++) {
					for (int k = 0; k < z1_near_seg.size(); k++) {
						if (dists[j][k] < tmp_min
						// && z0_near_seg.get(j).compareTo(z1_near_seg.get(k))
						// != 0
						) {
							tmp_min = dists[j][k];

							strt = z0_near_seg.get(j);
							end = z1_near_seg.get(k);
						}
					}
				}

				// System.out.println("End first if ..." + strt + " " + end);
			} else {
				int obs1 = trk_pnts_list.get(i).zone;
				List<String> z1_near_seg = close_twrs_edges.get(obs1);

				strt = route.get(route.size() - 1);
				end = "";
				double min_dist = Double.MAX_VALUE;
				for (int j = 0; j < z1_near_seg.size(); j++) {
					double dist = graph.getPathLength(strt, z1_near_seg.get(j), max_search_dist);
					if (dist < min_dist) {
						end = z1_near_seg.get(j);
						min_dist = dist;
					}
				}

			}

			if (!end.isEmpty()) {
				// please the obtained shortest path, if it contains the start
				// and the end of the trips or not ??!
				List<String> subroute = graph.getPath(strt, end, max_search_dist);
				// System.out.println(subroute);

				if (!route.isEmpty()) {
					// remove the start of the subroute as it is already
					// contained in the previous subroute ....
					subroute.remove(0);
				}
				route.addAll(subroute);

			} else {
				System.out.println("No end segment ... error!!!");
				// return something
				return route;
			}
		}
		return route;
	}

	/**
	 * Fri Aug 5 06:19:28 EET 2016
	 * 
	 * @param graph
	 * @param close_twrs_edges
	 * @param max_search_dist
	 * @return
	 */
	public List<String> find_route(WieghtedGraph graph, Hashtable<Integer, List<String>> close_twrs_edges,
			double max_search_dist) {

		List<String> route = new ArrayList<>();
		List<Integer> keys = new ArrayList<>(close_twrs_edges.keySet());
		// iterate observations ..
		for (int i = 1; i < keys.size(); i++) {
			String strt = "";
			String end = "";
			/**
			 * for the first part of the trip, iterate over all selected
			 * segments in the first two consecutive observations .... else
			 * check the distances from the end of the previous, to the current,
			 * observation to all segment in the next observations ..
			 * 
			 */
			if (route.isEmpty()) {

				// int obs0 = trk_pnts_list.get(i - 1).zone;
				// int obs1 = trk_pnts_list.get(i).zone;

				List<String> z0_near_seg = close_twrs_edges.get(keys.get(i - 1));
				List<String> z1_near_seg = close_twrs_edges.get(keys.get(i));

				double[][] dists = new double[z0_near_seg.size()][z1_near_seg.size()];

				z0_near_seg.parallelStream().forEach(z0_edge -> {

					z1_near_seg.parallelStream().forEach(z1_edge -> {
						dists[z0_near_seg.indexOf(z0_edge)][z1_near_seg.indexOf(z1_edge)] = graph.getPathLength(z0_edge,
								z1_edge, max_search_dist);
					});
				});
				// find the shortest path length within the stored matrix then
				// get the shortest path's and record its start and end segment
				double tmp_min = dists[0][0];
				strt = z0_near_seg.get(0);
				end = z1_near_seg.get(0);

				for (int j = 0; j < z0_near_seg.size(); j++) {
					for (int k = 0; k < z1_near_seg.size(); k++) {
						if (dists[j][k] < tmp_min
						// && z0_near_seg.get(j).compareTo(z1_near_seg.get(k))
						// != 0
						) {
							tmp_min = dists[j][k];

							strt = z0_near_seg.get(j);
							end = z1_near_seg.get(k);
						}
					}
				}

				// System.out.println("End first if ..." + strt + " " + end);
			} else {
				List<String> z1_near_seg = close_twrs_edges.get(keys.get(i));

				strt = route.get(route.size() - 1);
				end = "";
				double min_dist = Double.MAX_VALUE;
				for (int j = 0; j < z1_near_seg.size(); j++) {
					double dist = graph.getPathLength(strt, z1_near_seg.get(j), max_search_dist);
					if (dist < min_dist) {
						end = z1_near_seg.get(j);
						min_dist = dist;
					}
				}

			}

			if (!end.isEmpty()) {
				// please the obtained shortest path, if it contains the start
				// and the end of the trips or not ??!
				List<String> subroute = graph.getPath(strt, end, max_search_dist);
				// System.out.println(subroute);

				if (!route.isEmpty()) {
					// remove the start of the subroute as it is already
					// contained in the previous subroute ....
					subroute.remove(0);
				}
				route.addAll(subroute);

			} else {
				System.out.println("No end segment ... error!!!");
				// return something
				return route;
			}
		}
		return route;
	}

	/**
	 * Calculate the alignment between two line one them is the ground truth and
	 * the other is calculated using either viterbi which is ours or with
	 * Schulze's approach
	 * 
	 * @param ground_truth_path
	 * @param calc_path
	 * @param scale
	 * @return
	 */
	public double[] georesiduals(List<Vertex> ground_truth_path, List<Vertex> calc_path, double scale) {
		double[] residuals = new double[calc_path.size()];

		calc_path.parallelStream().forEach(from -> {
			double min_dist = Double.MAX_VALUE;
			for (int i = 0; i < ground_truth_path.size(); i++) {
				Vertex to = ground_truth_path.get(i);
				double dist = DataHandler.geodistance(from.getX() / scale, from.getY() / scale, to.getX() / scale,
						to.getY() / scale, "m");
				if (dist < min_dist)
					min_dist = dist;
			}
			residuals[calc_path.indexOf(from)] = min_dist;
		});

		return residuals;

	}

	/**
	 * Calculate the matched length from the estimated cellular-based routes ..
	 * 
	 * @param calc_path
	 * @param residuals
	 * @param scale
	 * @param threshold
	 * @param sampling_distance
	 * @return
	 */
	public double geomatched_routed_length(List<Vertex> calc_path, double[] residuals, double scale, double threshold,
			double increment) {
		double matched_length = 0;

		for (int i = 0; i < residuals.length - 1; i++) {
			// if any two consecutive points have residuals less than a given
			// one; this mean that this segment is matched
			if (residuals[i] <= threshold && residuals[i + 1] <= threshold) {
				Vertex from = calc_path.get(i);
				Vertex to = calc_path.get(i + 1);
				double edist = DataHandler.euclidean(from.getX(), from.getY(), to.getX(), to.getY());

				// double dist = DataHandler.geodistance(from.getX() / scale,
				// from.getY() / scale, to.getX() / scale,
				// to.getY() / scale, "m");
				// to avoid the unarranged points, we add only matched distances
				// less than double the increment/sampling
				if (edist <= (2 * increment))
					matched_length += edist;
				else
					matched_length += increment;
			}
		}

		return matched_length;

	}

	public double geomatched_routed_length(List<Vertex> calc_path, double[] residuals, double scale, double threshold) {
		double matched_length = 0;

		for (int i = 0; i < residuals.length - 1; i++) {
			// if any two consecutive points have residuals less than a given
			// one; this mean that this segment is matched
			if (residuals[i] <= threshold && residuals[i + 1] <= threshold) {
				Vertex from = calc_path.get(i);
				Vertex to = calc_path.get(i + 1);
				double dist = DataHandler.geodistance(from.getX() / scale, from.getY() / scale, to.getX() / scale,
						to.getY() / scale, "m");
				matched_length += dist;
			}
		}

		return matched_length;

	}

	/**
	 * calculate the alignment ratio ...
	 * 
	 * @param residuals
	 * @param threshold
	 * @return
	 */
	public double get_alignment_ratio(double[] residuals, double threshold) {
		double match = 0;
		for (double d : residuals) {
			if (d <= threshold)
				match++;
		}
		return match / (double) residuals.length;
	}

	/**
	 * get the list of GPS points
	 * 
	 * @param gps_trk_seg
	 * @param scale
	 * @param increment
	 * @return
	 */
	public List<Vertex> get_gps_points(List<GPSTrkPnt> gps_trk_seg, double scale) {

		List<Vertex> route_pnts = new ArrayList<>();

		gps_trk_seg.stream().forEachOrdered(pnt -> {
			double xy[] = DataHandler.proj_coordinates(pnt.x / scale, pnt.y / scale);
			route_pnts.add(new Vertex(xy[0], xy[1]));
		});
		return route_pnts;
	}

	/**
	 * get the list of GPS points including sampling of lines between these
	 * points
	 * 
	 * @param gps_trk_seg
	 * @param scale
	 * @param increment
	 * @return
	 */
	public List<Vertex> get_gps_points(List<GPSTrkPnt> gps_trk_seg, double scale, double increment, double[] ls) {

		double l = 0;
		double el = 0;
		List<Vertex> route_pnts = new ArrayList<>();
		for (int i = 0; i < gps_trk_seg.size() - 1; i++) {
			GPSTrkPnt pnt = gps_trk_seg.get(i);
			GPSTrkPnt pnt1 = gps_trk_seg.get(i + 1);

			// calculate the length of the route ..
			l += DataHandler.geodistance(pnt.x / scale, pnt.y / scale, pnt1.x / scale, pnt1.y / scale, "m");
			el += DataHandler.euclidean(pnt.x, pnt.y, pnt1.x, pnt1.y);
			route_pnts.addAll(
					Map_matching_schulze.segment_line(new Vertex(pnt.x, pnt.y), new Vertex(pnt1.x, pnt1.y), increment));
		}
		ls[0] = l;
		ls[1] = el;
		return route_pnts;
	}

	/**
	 * Get Map matching sequance of points ..
	 * 
	 * @param route
	 * @param edges
	 * @return
	 */
	public List<Vertex> get_mm_points(List<String> route, double scale, double increment, double[] ls) {

		List<Vertex> route_pnts = new ArrayList<>();
		StdDraw.setPenColor(Color.GREEN);
		for (String edge : route) {
			if (edgesMap.containsKey(edge)) {
				String[] points = edgesMap.get(edge).getShape().split(" ");
				for (int i = 0; i < points.length - 1; i++) {
					String[] str = points[i].split(",");
					// double xy[] =
					// DataHandler.proj_coordinates((Double.parseDouble(str[0])
					// / scale),
					// (Double.parseDouble(str[1]) / scale));
					Vertex p = new Vertex(Double.parseDouble(str[0]), Double.parseDouble(str[1]));

					route_pnts.add(p);
				}
			}
		}

		double[] tmp_l = new double[2];
		List<Vertex> tmp = sample_routes(route_pnts, scale, increment, tmp_l);
		ls[4] = tmp_l[0];
		ls[5] = tmp_l[1];
		return tmp;
	}

	/**
	 * Get Map matching sequance of points ..
	 * 
	 * @param route
	 * @param edges
	 * @return
	 */
	public List<Vertex> get_mm_points(List<String> route, List<Edge> edges, double scale) {

		List<Vertex> route_pnts = new ArrayList<>();
		edges.stream().forEachOrdered(e -> {
			if (route.contains(e.id)) {
				e.set_start_end_pnts();
				double xy[] = DataHandler.proj_coordinates(e.start.getX() / scale, e.start.getY() / scale);
				route_pnts.add(new Vertex(xy[0], xy[1]));
				double xy1[] = DataHandler.proj_coordinates(e.end.getX() / scale, e.end.getY() / scale);
				route_pnts.add(new Vertex(xy1[0], xy1[1]));
			}
		});
		return route_pnts;
	}

	/**
	 * Get observations from GPS records ..
	 * 
	 * @param pnts
	 * @return
	 */
	public String[] get_observations(List<GPSTrkPnt> pnts) {
		int prev = pnts.get(0).zone;
		String obs = Integer.toString(prev);
		for (int i = 1; i < pnts.size(); i++) {

			int crrnt = pnts.get(i).zone;
			if (prev != crrnt) {
				obs += "," + prev;
				prev = crrnt;
			}
		}
		obs += "," + prev;
		return obs.split(",");
	}

	/**
	 * According to Schulze et. al 2015, a set of edges close to the cellular
	 * towers have to be chosen to be use as the start and end of the
	 * subscribers sub-trips
	 * 
	 * @param twrs
	 * @param edges
	 * @param trk_pnts_list
	 * @param distance_thrshd
	 * @return
	 */
	public Hashtable<Integer, List<String>> get_start_end_egdes(Hashtable<Integer, Vertex> twrs, List<Edge> edges,
			List<GPSTrkPnt> trk_pnts_list, float distance_thrshd) {
		Hashtable<Integer, List<String>> close_twrs_edges = new Hashtable<>();
		List<Integer> indices = new ArrayList<>();
		indices.add(0);
		indices.add(trk_pnts_list.size() - 1);

		// if the start and the end are the same split the problem in two
		// problems...
		if (trk_pnts_list.get(0).zone == trk_pnts_list.get(trk_pnts_list.size() - 1).zone) {
			indices.add((trk_pnts_list.size() - 1) / 2);
		}
		trk_pnts_list.stream().forEach(obs -> {
			int index = trk_pnts_list.indexOf(obs);
			// get only edges related to the start and the end zones ...
			if (index == 0 || index == trk_pnts_list.size() - 1) {
				int twr = obs.zone;
				Vertex v = twrs.get(twr);

				Hashtable<String, Double> egdes_distances = new Hashtable<>();

				ValueComparator bvc = new ValueComparator(egdes_distances);
				TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);

				edges.stream().forEach(edge -> {
					double[] edge_cntr = edge.getCenterPnt();

					double dist = DataHandler.euclidean(v.x, v.y, edge_cntr[0], edge_cntr[1]);
					// System.out.printf("%f,%f\t%f,%f\t%f\n",v.x, v.y,
					// edge_cntr[0], edge_cntr[1],dist);
					if (dist <= distance_thrshd)
						egdes_distances.put(edge.id, dist);
				});

				// System.out.println(twr+"\t"+egdes_distances.size());
				sorted_map.putAll(egdes_distances);
				// System.out.println(sorted_map.keySet());
				// Add the closest five edges to the center of the current zones
				// ..
				close_twrs_edges.put(twr, new ArrayList<>(sorted_map.keySet()).subList(0, 5));
			}
		});
		return close_twrs_edges;
	}

	public Hashtable<Integer, List<String>> get_start_end_egdes(Hashtable<Integer, Vertex> twrs, List<Edge> edges,
			List<GPSTrkPnt> trk_pnts_list, List<String> graph_edges, float distance_thrshd) {
		Hashtable<Integer, List<String>> close_twrs_edges = new Hashtable<>();
		List<Integer> indices = new ArrayList<>();
		indices.add(0);
		indices.add(trk_pnts_list.size() - 1);

		// if the start and the end are the same split the problem in two
		// problems...
		if (trk_pnts_list.get(0).zone == trk_pnts_list.get(trk_pnts_list.size() - 1).zone) {
			indices.add((trk_pnts_list.size() - 1) / 2);
		}
		trk_pnts_list.stream().forEach(obs -> {
			int index = trk_pnts_list.indexOf(obs);
			// get only edges related to the start and the end zones ...
			if (index == 0 || index == trk_pnts_list.size() - 1) {
				int twr = obs.zone;
				Vertex v = twrs.get(twr);

				Hashtable<String, Double> egdes_distances = new Hashtable<>();

				ValueComparator bvc = new ValueComparator(egdes_distances);
				TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);

				edges.stream().forEach(edge -> {
					double[] edge_cntr = edge.getCenterPnt();

					double dist = DataHandler.euclidean(v.x, v.y, edge_cntr[0], edge_cntr[1]);
					// System.out.printf("%f,%f\t%f,%f\t%f\n",v.x, v.y,
					// edge_cntr[0], edge_cntr[1],dist);
					if (dist <= distance_thrshd)
						egdes_distances.put(edge.id, dist);
				});

				// System.out.println(twr+"\t"+egdes_distances.size());
				sorted_map.putAll(egdes_distances);
				// System.out.println(sorted_map.keySet());
				// Add the closest five edges to the center of the current zones
				// ..
				List<String> tmp = new ArrayList<>(sorted_map.keySet());
				List<String> close_edges = new ArrayList<>();
				int ecount = 0;
				for (int ie = 0; ie < tmp.size(); ie++) {
					String k = tmp.get(ie);
					if (graph_edges.contains(k)) {
						close_edges.add(k);
						ecount++;
					}
					if (ecount == 5)
						break;

				}
				close_twrs_edges.put(twr, close_edges);
			}
		});
		return close_twrs_edges;
	}

	/**
	 * Get a diff between two dates
	 * 
	 * @param the_oldest_date
	 * @param the_newest_date
	 * @param time_unit
	 *            the unit in which you want the diff
	 * @return the diff value, in the provided unit
	 */
	public long get_time_diff(Date date1, Date date2, TimeUnit time_unit) {
		long diff_millies = date2.getTime() - date1.getTime();
		return time_unit.convert(diff_millies, TimeUnit.MILLISECONDS);
	}

	/**
	 * Get sequance of points of the viterbi output ..
	 * 
	 * @param vedges
	 * @param edges
	 * @return
	 */
	public List<Vertex> get_vpath_points(String edges[], double scale, double increment, double[] ls) {

		List<Vertex> path = new ArrayList<>();
		StdDraw.setPenColor(Color.GREEN);
		for (String edge : edges) {
			if (edgesMap.containsKey(edge)) {
				Edge e = edgesMap.get(edge);
				// double[] pnt = e.getCenterPnt();
				e.set_start_end_pnts();
				// double xy[] = DataHandler.proj_coordinates((e.start.getX() /
				// scale), (e.start.getY() / scale));
				if (!path.contains(e.start))
					path.add(new Vertex(e.start.getX(), e.start.getY()));
				// double xy1[] = DataHandler.proj_coordinates((e.end.getX() /
				// scale), (e.end.getY() / scale));
				if (!path.contains(e.end))
					path.add(new Vertex(e.end.getX(), e.end.getY()));

			}
		}
		double[] tmp_l = new double[2];
		List<Vertex> tmp = sample_routes(path, scale, increment, tmp_l);
		ls[2] = tmp_l[0];
		ls[3] = tmp_l[1];
		return tmp;
	}

	/**
	 * Get sequance of points of the viterbi output ..
	 * 
	 * @param vedges
	 * @param edges
	 * @return
	 */
	public List<Vertex> get_vpath_points(String vedges[], List<Edge> edges, double scale) {
		List<String> vpath = new ArrayList<>(Arrays.asList(vedges));
		List<Vertex> vpath_pnts = new ArrayList<>();

		edges.stream().forEachOrdered(e -> {
			if (vpath.contains(e.id)) {
				double[] cnt = e.getCenterPnt();
				double xy[] = DataHandler.proj_coordinates(cnt[0] / scale, cnt[1] / scale);
				vpath_pnts.add(new Vertex(xy[0], xy[1]));
			}
		});
		return vpath_pnts;
	}

	public void index_edges(ArrayList<Edge> edges_list) {
		this.edgesMap = new Hashtable<>();

		for (Iterator<Edge> it = edges_list.iterator(); it.hasNext();) {
			Edge edge = it.next();
			edgesMap.put(edge.getId(), edge);
		}
	}

	/**
	 * Map GPS points into cellular coverage areas
	 * 
	 * @param trk_segs_list
	 * @param voronoi_file_path
	 * @return
	 */
	public List<List<GPSTrkPnt>> map_gps_pnt_zones(List<List<GPSTrkPnt>> trk_segs_list_org, String voronoi_file_path) {
		VoronoiConverter vc = new VoronoiConverter();
		ArrayList<Region> voronoiRegions = vc.readVoronoi(voronoi_file_path);
		List<List<GPSTrkPnt>> trk_segs_list = new ArrayList<List<GPSTrkPnt>>(trk_segs_list_org);
		for (int i = 0; i < trk_segs_list.size(); i++) {
			List<GPSTrkPnt> trk_pnts_list = trk_segs_list.get(i);
			for (int j = 0; j < trk_pnts_list.size(); j++) {
				GPSTrkPnt pnt = trk_pnts_list.get(j);
				// find zones ..
				for (Region vr : voronoiRegions) {
					ArrayList<Vertex> vertices = vr.vertices;
					double[] coord = new double[2 * vertices.size()];
					int k = 0;
					for (Vertex vert : vertices) {
						coord[k++] = vert.x;
						coord[k++] = vert.y;
					}
					Polygon2D.Double poly = new Polygon2D.Double(coord);
					if (poly.contains(new Point2D.Double(pnt.x, pnt.y))) {
						pnt.zone = vr.id;
						break;
					}
				}

				trk_pnts_list.set(j, pnt);
			}
			trk_segs_list.set(i, trk_pnts_list);
		}
		return trk_segs_list;
	}

	/**
	 * Map GPS points into cellular coverage areas
	 * 
	 * @param trk_segs_list
	 * @param voronoi_file_path
	 * @return
	 */
	public List<List<GPSTrkPnt>> map_gps_pnt_zones(List<List<GPSTrkPnt>> trk_segs_list_org, String voronoi_file_path,
			double scale, boolean rand_flag) {

		VoronoiConverter vc = new VoronoiConverter();
		ArrayList<Region> voronoiRegions = vc.readVoronoi(voronoi_file_path);
		List<List<GPSTrkPnt>> trk_segs_list = new ArrayList<List<GPSTrkPnt>>(trk_segs_list_org);
		for (int i = 0; i < trk_segs_list.size(); i++) {
			List<GPSTrkPnt> trk_pnts_list = trk_segs_list.get(i);
			for (int j = 0; j < trk_pnts_list.size(); j++) {
				GPSTrkPnt pnt;
				if (rand_flag) {
					pnt = new GPSTrkPnt();
					pnt.x = trk_pnts_list.get(j).x + getGaussian(0, scale);
					pnt.y = trk_pnts_list.get(j).y + getGaussian(0, scale);
				} else {
					pnt = trk_pnts_list.get(j);
				}
				// find zones ..
				for (Region vr : voronoiRegions) {
					ArrayList<Vertex> vertices = vr.vertices;
					double[] coord = new double[2 * vertices.size()];
					int k = 0;
					for (Vertex vert : vertices) {
						coord[k++] = vert.x;
						coord[k++] = vert.y;
					}
					Polygon2D.Double poly = new Polygon2D.Double(coord);
					if (poly.contains(new Point2D.Double(pnt.x, pnt.y))) {
						pnt.zone = vr.id;
						break;
					}
				}

				trk_pnts_list.set(j, pnt);
			}
			trk_segs_list.set(i, trk_pnts_list);
		}
		return trk_segs_list;
	}

	private double getGaussian(double aMean, double aVariance) {

		java.util.Random fRandom = new java.util.Random();
		return (aMean + fRandom.nextGaussian() * aVariance) / 100;
	}

	/**
	 * Map GPS points into cellular coverage areas and sample observation based
	 * on the sampling threshold ....
	 * 
	 * @param trk_segs_list
	 * @param voronoi_file_path
	 * @return
	 */
	public List<List<GPSTrkPnt>> map_gps_pnt_zones(List<List<GPSTrkPnt>> trk_segs_list_org, String voronoi_file_path,
			double sampling_time_seconds) {
		// copy the data in order not to modify the original content of the gpx
		// traces
		List<List<GPSTrkPnt>> trk_segs_list = new ArrayList<List<GPSTrkPnt>>(trk_segs_list_org);

		VoronoiConverter vc = new VoronoiConverter();
		ArrayList<Region> voronoiRegions = vc.readVoronoi(voronoi_file_path);

		for (int i = 0; i < trk_segs_list.size(); i++) {
			List<GPSTrkPnt> trk_pnts_list = trk_segs_list.get(i);
			for (int j = 0; j < trk_pnts_list.size(); j++) {
				GPSTrkPnt pnt = trk_pnts_list.get(j);
				// find zones ..
				for (Region vr : voronoiRegions) {
					ArrayList<Vertex> vertices = vr.vertices;
					double[] coord = new double[2 * vertices.size()];
					int k = 0;
					for (Vertex vert : vertices) {
						coord[k++] = vert.x;
						coord[k++] = vert.y;
					}
					Polygon2D.Double poly = new Polygon2D.Double(coord);
					if (poly.contains(new Point2D.Double(pnt.x, pnt.y))) {
						pnt.zone = vr.id;
						break;
					}
				}

				trk_pnts_list.set(j, pnt);
			}
			trk_segs_list.set(i, trk_pnts_list);
		}
		return sample_obs_time(trk_segs_list, sampling_time_seconds);
	}

	public void plot_route_pnst(List<Vertex> pnts, Color c) {
		StdDraw.setPenColor(c);
		pnts.stream().forEachOrdered(p -> {
			StdDraw.filledCircle(p.getX(), p.getY(), 0.5);
			// System.out.println(p.getX() + "," + p.getY());
		});

	}

	/**
	 * Read GPX file format
	 * 
	 * @param gpx_file_path
	 * @param scale
	 * @return
	 */
	public List<List<GPSTrkPnt>> read_gpx_data_no_proj(String gpx_file_path, double scale) {
		SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		List<List<GPSTrkPnt>> trk_segs_list = new ArrayList<>();
		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(gpx_file_path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();

			NodeList trk_seg_list = dpDoc.getElementsByTagName("trkseg");
			for (int is = 0; is < trk_seg_list.getLength(); is++) {

				Node trkseg = trk_seg_list.item(is);
				if (trkseg.getNodeType() == Node.ELEMENT_NODE) {
					Element trksegElement = (Element) trkseg;
					NodeList trk_pnt_list = trksegElement.getElementsByTagName("trkpt");

					List<GPSTrkPnt> trk_pnts_list = new ArrayList<>();
					for (int i = 0; i < trk_pnt_list.getLength(); i++) {
						GPSTrkPnt pnt = new GPSTrkPnt();
						Node trkpnt = trk_pnt_list.item(i);
						if (trkpnt.getNodeType() == Node.ELEMENT_NODE) {
							Element trkpntElement = (Element) trkpnt;
							pnt.lat = Double.parseDouble(trkpntElement.getAttribute("lat"));
							pnt.lon = Double.parseDouble(trkpntElement.getAttribute("lon"));

							pnt.x = pnt.lat * scale;
							pnt.y = pnt.lon * scale;

							// eElement.getChildNodes()
							NodeList timestamp_list = trkpntElement.getElementsByTagName("time");
							String date = "XXX";
							for (int j = 0; j < timestamp_list.getLength(); j++) {
								Node time_node = timestamp_list.item(j);

								if (time_node.getNodeType() == Node.ELEMENT_NODE) {
									pnt.timestamp = dtf.parse(
											time_node.getTextContent().replace('T', ' ').replace('Z', ' ').trim());
								}
							}
							// System.out.printf("%f,%f,%s\n", lon, lat, date);
							trk_pnts_list.add(pnt);
						}

					}
					trk_segs_list.add(trk_pnts_list);
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException | ParseException ex) {
			Logger.getLogger(main_read_gpx_without_projection.class.getName()).log(Level.SEVERE, null, ex);
		}
		return trk_segs_list;
	}

	/**
	 * 
	 * @param vpath
	 * @return
	 */
	public String[] remove_duplicated_edges(String[] vpath) {
		List<String> lvpath = new ArrayList<>(Arrays.asList(vpath));
		// System.out.println(lvpath);
		// Set<String> mySet = new HashSet<>(lvpath);
		for (String s : vpath) {
			int count = Collections.frequency(lvpath, s);
			for (int i = 0; i < count - 1; i++) {
				lvpath.remove(lvpath.lastIndexOf(s));
			}
			// System.out.println(s + " " +Collections.frequency(lvpath,s));

		}

		return lvpath.toArray(new String[lvpath.size()]);
	}

	public List<List<GPSTrkPnt>> sample_obs_time(List<List<GPSTrkPnt>> trk_segs_list, double sampling_time_seconds) {

		List<List<GPSTrkPnt>> trk_segs_list_n = new ArrayList<List<GPSTrkPnt>>(trk_segs_list);
		
		for (int i = 0; i < trk_segs_list.size(); i++) {
			List<GPSTrkPnt> trk_pnts_list = trk_segs_list.get(i);
			List<GPSTrkPnt> trk_pnts_list_n = new ArrayList<>();

			trk_pnts_list_n.add(trk_pnts_list.get(0));
			for (int j = 1; j < trk_pnts_list.size(); j++) {
				GPSTrkPnt pnt = trk_pnts_list_n.get(trk_pnts_list_n.size() - 1);
				GPSTrkPnt pnt1 = trk_pnts_list.get(j);

				// check time difference between observations ..
				double t_diff = get_time_diff(pnt.timestamp, pnt1.timestamp, TimeUnit.SECONDS);
				if (t_diff >= sampling_time_seconds) {
					trk_pnts_list_n.add(pnt1);
				}

			}
			trk_segs_list_n.set(i, trk_pnts_list_n);
		}

		return trk_segs_list_n;
	}

	public List<Vertex> sample_routes(List<Vertex> pnts, double increment) {
		List<Vertex> segs = new ArrayList<>();
		for (int i = 0; i < pnts.size() - 1; i++) {

			List<Vertex> tmp = Map_matching_schulze.segment_line(new Vertex(pnts.get(i).getX(), pnts.get(i).getY()),
					new Vertex(pnts.get(i + 1).getX(), pnts.get(i + 1).getY()), increment);
			if (!segs.isEmpty())
				tmp.remove(0);
			segs.addAll(tmp);
		}
		return segs;
	}

	public List<Vertex> sample_routes(List<Vertex> pnts, double scale, double increment, double[] length) {

		double path_length = 0;
		double euc_path_length = 0;
		List<Vertex> segs = new ArrayList<>();
		for (int i = 0; i < pnts.size() - 1; i++) {

			double dist = DataHandler.geodistance(pnts.get(i).getX() / scale, pnts.get(i).getY() / scale,
					pnts.get(i + 1).getX() / scale, pnts.get(i + 1).getY() / scale, "m");
			if (!Double.isNaN(dist))
				path_length += dist;

			double edist = DataHandler.euclidean(pnts.get(i).getX(), pnts.get(i).getY(), pnts.get(i + 1).getX(),
					pnts.get(i + 1).getY());
			if (!Double.isNaN(dist))
				euc_path_length += edist;

			List<Vertex> tmp = Map_matching_schulze.segment_line(new Vertex(pnts.get(i).getX(), pnts.get(i).getY()),
					new Vertex(pnts.get(i + 1).getX(), pnts.get(i + 1).getY()), increment);
			if (!segs.isEmpty())
				tmp.remove(0);
			segs.addAll(tmp);
		}
		length[0] = path_length;
		length[1] = euc_path_length;
		return segs;
	}

	/**
	 * smooth transition between Viterbi selected edges ....
	 * 
	 * @param graph
	 * @param path
	 * @return
	 */
	public String[] smooth_viterbi_path(WieghtedGraph graph, String[] path) {

		List<String> route = new ArrayList<>();
		for (int i = 0; i < path.length - 1; i++) {
			String strt = path[i];
			String end = path[i + 1];
			List<String> sub = graph.getPath(strt, end, 1000000);
			if (!route.isEmpty()) {
				sub.remove(0);
			}
			route.addAll(sub);
		}
		return route.toArray(new String[route.size()]);
	}

	public void write_csv(double[] values, String output_file_path) {
		BufferedWriter writer = null;
		try {
			File logFile = new File(output_file_path);
			writer = new BufferedWriter(new FileWriter(logFile));
			for (double r : values) {
				writer.write(Double.toString(r));
				writer.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}

	}

	public void write_csv(List<String> values, String output_file_path) {
		BufferedWriter writer = null;
		try {
			File logFile = new File(output_file_path);
			writer = new BufferedWriter(new FileWriter(logFile));
			for (String r : values) {
				writer.write(r);
				writer.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}

	}

	/**
	 * Read residual files ...
	 * 
	 * @param file_path
	 * @return
	 */
	public double[] read_residuals(String file_path) {

		List<Double> segments = new ArrayList<>();
		BufferedReader reader;
		try {
			Reader isreader = new InputStreamReader(new FileInputStream(file_path), "utf-8");
			reader = new BufferedReader(isreader);

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				segments.add(Double.parseDouble(line.trim()));
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		double[] tmp = new double[segments.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = segments.get(i).doubleValue();
		}
		return tmp;

	}
}
