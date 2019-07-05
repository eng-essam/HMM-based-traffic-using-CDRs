/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

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

import Voronoi.VoronoiConverter;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.ToNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class TransBuilder {

	/**
	 * Get the mean square error between the old transitions probabilities and
	 * new updated transitions.
	 *
	 * This distance is equivalent to the euclidian distance between two
	 * matrices.
	 *
	 * @param transitions
	 * @param update
	 * @return
	 */
	public static double getError(Hashtable<String, Hashtable<String, Double>> transitions,
			Hashtable<String, Hashtable<String, Double>> update) {
		double sum = 0.0;
		// NumberFormat formatter = new DecimalFormat("0.#######E0");
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : transitions.entrySet()) {
			String fromKey = entrySet.getKey();
			Hashtable<String, Double> tosTrans = entrySet.getValue();
			Hashtable<String, Double> uTosTrans = update.get(fromKey);

			for (Map.Entry<String, Double> tosEntry : tosTrans.entrySet()) {

				String toKey = tosEntry.getKey();
				double prob = tosEntry.getValue().doubleValue();
				double uprob = uTosTrans.get(toKey).doubleValue();
				double diff = Math.abs(prob - uprob);
				if (Double.isNaN(diff)) {
					continue;
				}
				sum += diff * diff;

			}
		}

		return Math.sqrt(sum);
		// return sum;

	}

	/**
	 * Get the mean square error between the old transitions probabilities and
	 * new updated transitions.
	 *
	 * This distance is equivalent to the euclidian distance between two
	 * matrices.
	 *
	 * @param transitions
	 * @param update
	 * @return
	 */
	public static double getError2(Hashtable<String, Hashtable<String, Double>> transitions,
			Hashtable<String, Hashtable<String, Double>> update) {
		double sum = 0.0;
		int i = 0;
		// NumberFormat formatter = new DecimalFormat("0.#######E0");
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : transitions.entrySet()) {
			String fromKey = entrySet.getKey();
			Hashtable<String, Double> tosTrans = entrySet.getValue();
			if (!update.containsKey(fromKey)) {
				continue;
			}
			Hashtable<String, Double> uTosTrans = update.get(fromKey);
			/**
			 * to avoid null exceptions
			 */
			// if (uTosTrans==null) {
			// uTosTrans=new Hashtable<>();
			// }
			double gsum = 0.0;
			double vit_sum = 0.0;
			double dua_sum = 0.0;

			int j = 0;
			for (Map.Entry<String, Double> tosEntry : tosTrans.entrySet()) {

				String toKey = tosEntry.getKey();
				double prob = tosEntry.getValue().doubleValue();
				// double uprob = 0;
				//
				// if (uTosTrans.containsKey(toKey)) {
				// uprob = uTosTrans.get(toKey).doubleValue();
				// }

				if (!uTosTrans.containsKey(toKey)) {
					continue;
				}
				double uprob = uTosTrans.get(toKey).doubleValue();
				// double prob = Math.log(tosEntry.getValue());
				// double uprob = Math.log(uTosTrans.get(toKey));
				// System.out.println(String.valueOf(prob) + " - " +
				// String.valueOf(uprob));
				j++;
				i++;
				double diff = prob - uprob;
				vit_sum += prob;
				dua_sum += uprob;

				gsum += diff * diff;
				sum += diff * diff;
				// if (diff > 0) {
				// System.out.println(String.valueOf(prob) + " - " +
				// String.valueOf(uprob));
				// }

			}
			if (vit_sum != 0 || dua_sum != 0) {
				System.out.println(fromKey + "," + Math.sqrt(gsum) / j + "," + vit_sum + "," + dua_sum);
			}
		}

		return Math.sqrt(sum) / i;

	}

	/**
	 * Update the transition table with the counted values by maximum the
	 * transition with the counted values.
	 *
	 * @param transitions
	 * @param density
	 * @param alpha
	 * @param beta
	 * @return
	 */
	public static Hashtable<String, Hashtable<String, Double>> updateTrans(
			Hashtable<String, Hashtable<String, Double>> transitions,
			Hashtable<String, Hashtable<String, Double>> density, double alpha, double beta) {

		density.entrySet().parallelStream().forEach(entrySet -> {
			String fromKey = entrySet.getKey();
			Hashtable<String, Double> toValues = entrySet.getValue();
			double sum = 0.0;
			for (Map.Entry<String, Double> toEntry : toValues.entrySet()) {
				/**
				 * Exclude sefl state from the transitions updates, by removing
				 * it from the sum and next normilisation step.
				 */
				// String toKey = toEntry.getKey();
				// if (toKey.equals(fromKey)) {
				// continue;
				// }

				double prob = toEntry.getValue().doubleValue();
				sum += prob;
			}

			Hashtable<String, Double> trans_to_values = new Hashtable<>();

			if (transitions.containsKey(fromKey)) {
				trans_to_values = transitions.get(fromKey);
			}

			for (Map.Entry<String, Double> toEntry : toValues.entrySet()) {
				String toKey = toEntry.getKey();
				// if (toKey.equals(fromKey)) {
				// continue;
				// }

				double prob = toEntry.getValue().doubleValue();
				double trans_prob = 0.0;
				if (!trans_to_values.containsKey(toKey)) {
					/**
					 * if it a new entry so it's final value will equal density
					 * probability only.
					 */

					trans_prob = prob / sum;
				} else {
					trans_prob = trans_to_values.get(toKey);
				}

				double update = ((alpha * trans_prob) + (beta * prob / sum)) / (alpha + beta);
				trans_to_values.replace(toKey, update);
			}
			transitions.replace(fromKey, trans_to_values);
		});
		return transitions;
	}

	ArrayList<FromNode> networkDist;
	String distPath;
	Hashtable<String, Integer> nodesMap;
	ArrayList<String> history;
	ArrayList<String> nodeExts;

	ArrayList<Integer> zones;

	ArrayList<Double> probs;

	String vorNeighborsPath;

	/**
	 * Fri 09 Jan 2015 09:55:05 AM EET
	 *
	 */
	Hashtable<String, Integer> from_map;

	public TransBuilder(ArrayList<FromNode> networkDist, String vorNghbrs) {
		this.networkDist = new ArrayList<>(networkDist);
		nodesMap = new Hashtable<>();
		this.vorNeighborsPath = vorNghbrs;

		generateNodesMap();
	}

	public Hashtable<String, Hashtable<String, Double>> emitNeighbors(
			Hashtable<String, Hashtable<String, Double>> emissions) {
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);
		// System.out.println("voronoiNeibors" + voronoiNeibors.size());

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissions.entrySet()) {
			String pnt = entrySet.getKey();
			Hashtable<String, Double> zones = entrySet.getValue();
			Hashtable<String, Double> tmpZones = new Hashtable<>();
			double sum = 0;
			String orgZone;
			for (Map.Entry<String, Double> zonesEntry : zones.entrySet()) {
				orgZone = zonesEntry.getKey();
				Double value = zonesEntry.getValue();
				tmpZones.put(orgZone, value);
				sum += value;
				ArrayList<Integer> zoneNeighbors = voronoiNeibors.get(Integer.parseInt(orgZone));
				for (Iterator<Integer> iterator = zoneNeighbors.iterator(); iterator.hasNext();) {
					Integer newEmZone = iterator.next();
					double prob = 1.0f;// (double) Math.random();
					sum += prob;
					tmpZones.put(String.valueOf(newEmZone), prob);
				}
				/**
				 * Normalize
				 */

				for (Map.Entry<String, Double> normalizationEntry : tmpZones.entrySet()) {
					String zone = normalizationEntry.getKey();
					Double prob = normalizationEntry.getValue();
					if (zone.equals(orgZone)) {
						tmpZones.replace(zone, 0.8);
					} else {
						tmpZones.replace(zone, 0.2 * prob / sum);
					}

				}

			}

			emissions.replace(pnt, tmpZones);

		}
		return emissions;
	}

	/**
	 * Fri 09 Jan 2015 09:55:05 AM EET
	 *
	 * @param emissions
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> emitNeighbors_1(
			Hashtable<String, Hashtable<String, Double>> emissions) {
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissions.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<String, Double> pnt_zones = entrySet.getValue();
			/**
			 * The initial emission table contain emission only for the original
			 * zone.
			 */
			String org_zone = new ArrayList<String>(pnt_zones.keySet()).get(0);
			/**
			 * Emit the neighbors
			 */
			pnt_zones.replace(org_zone, 0.997);

			ArrayList<Integer> neighbors = voronoiNeibors.get(Integer.parseInt(org_zone));

			// if (neighbors==null) {
			// System.out.println(org_zone);
			// continue;
			// }
			for (Iterator<Integer> iterator = neighbors.iterator(); iterator.hasNext();) {
				Integer next_zone = iterator.next();
				pnt_zones.put(String.valueOf(next_zone), 0.003 / neighbors.size());
			}
			emissions.replace(key, pnt_zones);
		}
		return emissions;
	}

	/**
	 * Sat 13 Jun 2015 02:30:02 AM JST
	 *
	 * @param emissions
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> emitNeighbors_2(
			Hashtable<String, Hashtable<String, Double>> emissions, Hashtable<Integer, Vertex> towers) {
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissions.entrySet()) {
			String key = entrySet.getKey();
			FromNode node = getFromNode(key);
			Hashtable<String, Double> pnt_zones = entrySet.getValue();
			/**
			 * The initial emission table contain emission only for the original
			 * zone.
			 */
			String org_zone = new ArrayList<String>(pnt_zones.keySet()).get(0);
			/**
			 * Emit the neighbors
			 */
			double origin_zone_prob = 0.95;
			double neighbors_prob = 1 - origin_zone_prob;
			pnt_zones.replace(org_zone, origin_zone_prob);

			ArrayList<Integer> neighbors = voronoiNeibors.get(Integer.parseInt(org_zone));

			double sum = origin_zone_prob;
			for (Iterator<Integer> iterator = neighbors.iterator(); iterator.hasNext();) {
				Integer next_zone = iterator.next();
				Vertex tower_xy = towers.get(next_zone);
				double dist = neighbors_prob / euclidean(node.getX(), node.getY(), tower_xy.getX(), tower_xy.getY());
				sum += dist;
				pnt_zones.put(String.valueOf(next_zone), dist);
			}

			for (Map.Entry<String, Double> entrySet1 : pnt_zones.entrySet()) {
				String key1 = entrySet1.getKey();
				Double value = entrySet1.getValue() / sum;
				pnt_zones.replace(key1, value);
			}
			emissions.replace(key, pnt_zones);
		}
		return emissions;
	}

	public Hashtable<String, Hashtable<String, Double>> emitNeighbors_3(ArrayList<FromNode> map,
			Hashtable<String, Hashtable<String, Double>> emissions, Hashtable<Integer, Vertex> towers) {

		for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
			FromNode node = iterator.next();
			if (node.isIsExit()) {
				double sum = 1;
				Hashtable<String, Double> tos = emissions.get(node.getID());
				for (Map.Entry<Integer, Vertex> entrySet : towers.entrySet()) {
					int key = entrySet.getKey();
					if (!tos.containsKey(Integer.toString(key))) {
						Vertex vertex = entrySet.getValue();
						double d = 1 / euclidean(node.getX(), node.getY(), vertex.getX(), vertex.getY());
						sum += d;
						tos.put(Integer.toString(key), d);
					}

				}
				for (Map.Entry<String, Double> entrySet : tos.entrySet()) {
					String key = entrySet.getKey();
					double value = entrySet.getValue() / sum;
					// value /= sum;
					tos.replace(key, value);

				}
				emissions.replace(node.getID(), tos);
			}

		}
		return emissions;
	}

	/**
	 * Wed Oct 7 06:15:24 JST 2015
	 *
	 * Emis = 1/D^2 .
	 *
	 * @param emissions
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> emitNeighbors_3(
			Hashtable<String, Hashtable<String, Double>> emissions, Hashtable<Integer, Vertex> towers) {
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissions.entrySet()) {
			String key = entrySet.getKey();
			FromNode node = getFromNode(key);
			Hashtable<String, Double> pnt_zones = entrySet.getValue();
			/**
			 * The initial emission table contain emission only for the original
			 * zone.
			 */
			String org_zone = new ArrayList<String>(pnt_zones.keySet()).get(0);
			Vertex tower_xy = towers.get(Integer.parseInt(org_zone));

			/**
			 * Emit the neighbors
			 */
			double dist = 0;
			try {
				dist = 1 / Math.pow(euclidean(node.getX(), node.getY(), tower_xy.getX(), tower_xy.getY()), 2);
			} catch (Exception e) {
				if (node == null) {
					System.out.println("null node\t" + key);
				}
				if (tower_xy == null) {
					System.out.println("null tower\t" + org_zone);
				}
				// System.err.printf("node.getX(%f), node.getY(%f),
				// tower_xy.getX(%f), tower_xy.getY(%f)\n",node.getX(),
				// node.getY(), tower_xy.getX(), tower_xy.getY());
			}
			pnt_zones.replace(org_zone, dist);

			ArrayList<Integer> neighbors = voronoiNeibors.get(Integer.parseInt(org_zone));

			double sum = dist;
			for (Iterator<Integer> iterator = neighbors.iterator(); iterator.hasNext();) {
				Integer next_zone = iterator.next();
				tower_xy = towers.get(next_zone);
				dist = 1 / Math.pow(euclidean(node.getX(), node.getY(), tower_xy.getX(), tower_xy.getY()), 2);
				sum += dist;
				pnt_zones.put(String.valueOf(next_zone), dist);
			}

			for (Map.Entry<String, Double> entrySet1 : pnt_zones.entrySet()) {
				String key1 = entrySet1.getKey();
				Double value = entrySet1.getValue() / sum;
				pnt_zones.replace(key1, value);
			}
			emissions.replace(key, pnt_zones);
		}
		return emissions;
	}

	/**
	 * Calculate distance between two cartesian points
	 *
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @return
	 */
	private double euclidean(double x, double y, double x1, double y1) {
		return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
	}

	/**
	 * Wed Jan 6 12:10:42 EET 2016
	 *
	 * Emission probability is the relation between states and observations, the
	 * probability of obtaining specific state if a given observation occurs
	 *
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> generate_emission_prob(Hashtable<Integer, Vertex> towers) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);
		/**
		 * It is impossible to have an exit point related to two different
		 * voronoi zones, so each table created for points to zones emissions
		 * will contain only single element with probability equals 1
		 */

		Hashtable<String, Double> pntToZonesTable;
		// final double prob = 1f;
		for (FromNode node : this.networkDist) {
			if (node.isIsExit()) {
				int zone = node.getZone();
				if (!voronoiNeibors.containsKey(zone)) {
					System.out.println("Zone: " + zone + " is not present in the voronoi nieghbors file");
					continue;
				}
				ArrayList<Integer> nvor = voronoiNeibors.get(zone);
				nvor.add(zone);
				double x = node.getX();
				double y = node.getY();
				double sum = 0;
				pntToZonesTable = new Hashtable<>();
				/**
				 * Calculate the emission probability as the inverse distance
				 * from the an exit node to all adjacent zones to the original
				 * one ..
				 */
				for (Iterator<Integer> iterator1 = nvor.iterator(); iterator1.hasNext();) {
					zone = iterator1.next();
					Vertex v = towers.get(zone);

					double prob = 1 / euclidean(x, y, v.getX(), v.getY());
					sum += prob;
					pntToZonesTable.put(Integer.toString(zone), prob);
				}

				/**
				 * Normalize probabilities ..
				 */
				for (Map.Entry<String, Double> entry : pntToZonesTable.entrySet()) {
					String key = entry.getKey();
					double value = entry.getValue() / sum;
					pntToZonesTable.replace(key, value);

				}
				emissions.put(node.getID(), pntToZonesTable);
			}
		}

		return emissions;
	}

	/**
	 * @date Tue Mar 22 21:32:05 EET 2016
	 * 
	 *       emit not only for the adjacent zones but to zones within a
	 *       threshold distance
	 * 
	 * @param towers
	 * @param threshold
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> generate_emission_prob(Hashtable<Integer, Vertex> towers,
			double threshold) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();

		for (FromNode node : this.networkDist) {
			if (node.isIsExit()) {
				Hashtable<String, Double> tos = new Hashtable<>();
				double sum = 0;
				for (Map.Entry<Integer, Vertex> entry : towers.entrySet()) {
					double dist = DataHandler.euclidean(node.x, node.y, entry.getValue().x, entry.getValue().y);
					if (dist < threshold) {
						sum += 1.0 / dist;
						tos.put(Integer.toString(entry.getKey()), 1.0 / dist);
					}
				}
				// normalise ..
				Hashtable<String, Double> ntos = new Hashtable<>();
				for (Map.Entry<String, Double> entry : tos.entrySet()) {
					ntos.put(entry.getKey(), entry.getValue() / sum);
				}
				emissions.put(node.ID, ntos);
			}
		}
		return emissions;
	}

	/**
	 * @date Tue Mar 22 22:14:24 EET 2016 Emit adjacent and adjacent of adjacent
	 *       zones
	 * 
	 * @param towers
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> generate_emission_prob_adj_adj_zones(
			Hashtable<Integer, Vertex> towers) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> neighbors = vorCon.readVorNeighbors(this.vorNeighborsPath);

		for (FromNode node : this.networkDist) {
			if (node.isIsExit()) {
				Hashtable<String, Double> tos = new Hashtable<>();
				double sum = 0;
				int zone = node.zone;
				List<Integer> emitables = adjacent_zones(zone, neighbors);
				emitables.add(zone);
				for (int z : emitables) {
					if (tos.containsKey(Integer.toString(z)))
						continue;
					Vertex v = towers.get(z);
					double dist = euclidean(node.x, node.y, v.x, v.y);
					sum += 1.0 / Math.pow(dist, 2);
					tos.put(Integer.toString(z), 1.0 / Math.pow(dist, 2));
				}
				// normalise ..
				Hashtable<String, Double> ntos = new Hashtable<>();
				for (Map.Entry<String, Double> entry : tos.entrySet()) {
					ntos.put(entry.getKey(), entry.getValue() / sum);
				}
				emissions.put(node.ID, ntos);
			}
		}

		return emissions;
	}

	private List<Integer> adjacent_zones(int zone, Hashtable<Integer, ArrayList<Integer>> neighbors) {
		List<Integer> adj_2_adj = new ArrayList<>();
		ArrayList<Integer> adj = neighbors.get(zone);
		adj_2_adj.addAll(adj);

		for (int z : adj) {
			adj = neighbors.get(z);
			if (adj != null && !adj.isEmpty())
				adj_2_adj.addAll(adj);
		}
		return adj_2_adj;
	}

	public void generate_from_nodes_map() {
		from_map = new Hashtable<>();
		for (int i = 0; i < networkDist.size(); i++) {
			String node = networkDist.get(i).getID();
			from_map.put(node, i);
		}

	}

	/**
	 * Fri Oct 2 20:14:58 JST 2015
	 *
	 * transition to adjacent zones only
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> generate_transition_prob(double threshold) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);
		System.out.println("End building graph ...");

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();

		VConnectivityInspector handler = new VConnectivityInspector();

		Hashtable<Integer, ArrayList<String>> regions_exts = handler.getRegionExists(networkDist);
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);

		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromRegionKey = entrySet.getKey();
			// if (!allRegionsTable.containsKey(fromRegionKey)) {
			// continue;
			// }
			// ArrayList<FromNode> regFromNodes = new ArrayList<>();
			// regFromNodes.addAll(allRegionsTable.get(fromRegionKey));
			if (!regions_exts.containsKey(fromRegionKey)) {
				continue;
			}
			ArrayList<String> fromExts = regions_exts.get(fromRegionKey);
			if (fromExts.isEmpty()) {
				continue;
			}
			//
			// ArrayList<String> toExts = new ArrayList<>();
			ArrayList<Integer> adjacentRegions = entrySet.getValue();
			System.out.printf("From zone: %d\n there are %d neighbors\n", fromRegionKey, adjacentRegions.size());
			// for (Iterator<Integer> iterator = adjacentRegions.iterator();
			// iterator.hasNext();) {
			// Integer toRegionKey = iterator.next();
			// if (!regions.containsKey(toRegionKey)) {
			// System.out.println("Region doesn't available in regions table:
			// "+toRegionKey);
			// }
			// toExts.addAll(regions.get(toRegionKey));
			// }
			for (Iterator<String> iterator = fromExts.iterator(); iterator.hasNext();) {
				String from_ext = iterator.next();
				int from_zone = -1;
				try {
					from_zone = networkDist.get(nodesMap.get(from_ext)).getZone();
				} catch (Exception e) {
					System.err.println("From node exception \t" + from_ext);
				}
				Hashtable<String, Double> tos = new Hashtable<>();
				double sum = 0;
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;

				for (int i = 0; i < adjacentRegions.size(); i++) {
					Integer toRegionKey = adjacentRegions.get(i);
					if (!regions_exts.containsKey(toRegionKey)) {
						System.out.println("Region doesn't available in regions table: " + toRegionKey);
						continue;
					}

					ArrayList<String> toExts = regions_exts.get(toRegionKey);
					for (Iterator<String> toIterator = toExts.iterator(); toIterator.hasNext();) {
						String to_ext = toIterator.next();
						if (from_ext.compareTo(to_ext) == 0) {
							continue;
						}

						int to_zone = -1;
						try {
							to_zone = networkDist.get(nodesMap.get(to_ext)).getZone();
						} catch (Exception e) {
							// continue;
							System.out.println("\t Node \t" + to_ext + "\t has no childs");
						}
						/**
						 * Check if they are belong to the same zone of not. if
						 * the state is not exit state continue.
						 */
						if (from_zone == to_zone || from_zone == -1 || to_zone == -1) {
							// System.out.println("Zones are equal");
							continue;
						}

						double dist = graph.getPathLength(from_ext, to_ext, threshold);
						if (dist == Double.POSITIVE_INFINITY) {
							continue;
						}
						// if (min > dist) {
						// min = dist;
						// }
						if (max < dist) {
							max = dist;
						}
						dist = 1 / dist;

						sum += dist;
						tos.put(to_ext, dist);
					}
				}
				// for (Iterator<Integer> iterator1 =
				// adjacentRegions.iterator(); iterator.hasNext();) {
				// Integer toRegionKey = iterator1.next();
				// }
				if (tos.isEmpty()) {
					continue;
				}

				/**
				 * Self transitions
				 */
				// double dist;
				//
				// if (min < 1) {
				// dist = min;
				// } else {
				// dist = min - 1;
				// }
				double dist = 1 / max;
				// dist = 1 / dist;
				sum += dist;
				tos.put(from_ext, dist);
				System.out.println("sum:\t" + sum);
				/**
				 * Normalize probabilities and add unconnected.
				 */
				double tst_sum = 0;
				for (Map.Entry<String, Double> toentry : tos.entrySet()) {
					String key = toentry.getKey();
					double val = toentry.getValue() / sum;
					tst_sum += val;
					// double norm = ((sum - val) / ((tos.size() - 1) * sum));
					// double norm = val / sum;
					tos.replace(key, val);
				}

				if (tst_sum != 1) {
					System.out.println("tst_sum\t" + tst_sum);
				}
				transitions.put(from_ext, tos);
				System.out.println("From -> To");
			}
		}
		return transitions;
	}

	/**
	 * @date Sun Mar 20 19:26:50 EET 2016
	 *
	 *       Transition to all states within a specific distance ..
	 *
	 * @return transition table ....
	 */
	public Hashtable<String, Hashtable<String, Double>> generate_transition_prob(double threshold,
			ArrayList<String> exts) {

		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);
		System.out.println("End building graph ...");

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();

		double[][] dists = new double[exts.size()][exts.size()];
		for (int i = 0; i < dists.length; i++) {
			Arrays.fill(dists[i], 0);
		}

		exts.parallelStream().forEach(from_ext -> {
			int from_index = exts.indexOf(from_ext);
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (Iterator<String> iterator1 = exts.iterator(); iterator1.hasNext();) {
				String to_ext = iterator1.next();
				int to_index = exts.indexOf(to_ext);
				if (from_index == to_index /*
											 * || dists[from_index][to_index] !=
											 * 0
											 */) {
					continue;
				}

				double dist = graph.getPathLength(from_ext, to_ext, threshold);
				if (dist == Double.POSITIVE_INFINITY) {
					continue;
				}

				if (max < dist) {
					max = dist;
				}
				if (min > dist && dist != 0)
					min = dist;
				// dist = 1 / dist;

				// Assumption: the same traveling distance between two point in
				// reverse directions

				dists[from_index][to_index] = 1 / dist;
				// dists[to_index][from_index] = 1 / dist;

			}
			dists[from_index][from_index] = 1 / min;
		});
		// for (Iterator<String> iterator = exts.iterator();
		// iterator.hasNext();) {
		// String from_ext = iterator.next();
		// int from_index = exts.indexOf(from_ext);
		// double max = Double.MIN_VALUE;
		// for (Iterator<String> iterator1 = exts.iterator();
		// iterator1.hasNext();) {
		// String to_ext = iterator1.next();
		// int to_index = exts.indexOf(to_ext);
		// if (from_index == to_index || dists[from_index][to_index] != 0) {
		// continue;
		// }
		//
		// double dist = graph.getPathLength(from_ext, to_ext, threshold);
		// if (dist == Double.POSITIVE_INFINITY) {
		// continue;
		// }
		//
		// if (max < dist) {
		// max = dist;
		// }
		// // dist = 1 / dist;
		//
		// // Assumption: the same traveling distance between two point in
		// // reverse directions
		//
		// dists[from_index][to_index] = 1 / dist;
		// dists[to_index][from_index] = 1 / dist;
		//
		// }
		// dists[from_index][from_index] = 1 / max;
		// }

		// double max = Double.MIN_VALUE;
		exts.parallelStream().forEach(from -> {
			int from_index = exts.indexOf(from);
			Hashtable<String, Double> tos = new Hashtable<>();
			double[] d = dists[from_index];
			double sum = DoubleStream.of(d).sum();
			double tst_sum = 0;
			for (Iterator<String> iterator1 = exts.iterator(); iterator1.hasNext();) {
				String to = iterator1.next();
				int to_index = exts.indexOf(to);
				if (d[to_index] != 0) {
					tos.put(to, d[to_index] / sum);
					tst_sum += tos.get(to);
				}

			}
			System.out.printf("exist: %s \t sum: %f \t test sum: %f\n", from, sum, tst_sum);
			transitions.put(from, tos);
		});
		// for (Iterator<String> iterator = exts.iterator();
		// iterator.hasNext();) {
		// String from = iterator.next();
		// int from_index = exts.indexOf(from);
		// Hashtable<String, Double> tos = new Hashtable<>();
		// double[] d = dists[from_index];
		// double sum = DoubleStream.of(d).sum();
		// double tst_sum = 0;
		// for (Iterator<String> iterator1 = exts.iterator();
		// iterator1.hasNext();) {
		// String to = iterator1.next();
		// int to_index = exts.indexOf(to);
		// if (d[to_index] != 0) {
		// tos.put(to, d[to_index] / sum);
		// tst_sum += tos.get(to);
		// }
		//
		// }
		// System.out.printf("exist: %s \t sum: %f \t test sum: %f\n", from,
		// sum, tst_sum);
		// transitions.put(from, tos);
		// }
		return transitions;
	}

	/**
	 * @date Sun Jul 31 04:23:17 EET 2016
	 */
	Hashtable<String, Edge> edgesMap;

	public void index_edges(ArrayList<Edge> edges_list) {
		this.edgesMap = new Hashtable<>();

		for (Iterator<Edge> it = edges_list.iterator(); it.hasNext();) {
			Edge edge = it.next();
			edgesMap.put(edge.getId(), edge);
		}
	}

	private double get_euclidean_dist(String edge0, String edge1) {
		if (edgesMap.containsKey(edge0) && edgesMap.containsKey(edge1)) {
			double[] e0 = edgesMap.get(edge0).getCenterPnt();
			double[] e1 = edgesMap.get(edge1).getCenterPnt();

			return euclidean(e0[0], e0[1], e1[0], e1[1]);
		}
		return -1;
	}

	/**
	 * @date Sun Jul 31 04:23:17 EET 2016
	 *
	 *       Transition to all states within a specific distance ..
	 *
	 * @return transition table ....
	 */
	public Hashtable<String, Hashtable<String, Double>> generate_transition_prob_eculidain(double threshold,
			ArrayList<String> exts, ArrayList<Edge> edges) {

		// if (networkDist == null || networkDist.isEmpty()) {
		// return null;
		// }

		index_edges(edges);

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();

		double[][] dists = new double[exts.size()][exts.size()];
		for (int i = 0; i < dists.length; i++) {
			Arrays.fill(dists[i], 0);
		}

		exts.parallelStream().forEach(from_ext -> {
			int from_index = exts.indexOf(from_ext);
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (Iterator<String> iterator1 = exts.iterator(); iterator1.hasNext();) {
				String to_ext = iterator1.next();
				int to_index = exts.indexOf(to_ext);
				if (from_index == to_index) {
					continue;
				}

				double dist = get_euclidean_dist(from_ext, to_ext);

				if (dist == Double.POSITIVE_INFINITY || dist == -1 || dist > threshold) {
					continue;
				}

				if (max < dist) {
					max = dist;
				}
				if (min > dist && dist != 0)
					min = dist;
				// dist = 1 / dist;

				// Assumption: the same traveling distance between two point in
				// reverse directions

				dists[from_index][to_index] = 1 / dist;
				// dists[to_index][from_index] = 1 / dist;

			}
			dists[from_index][from_index] = 1 / min;
		});

		exts.parallelStream().forEach(from -> {
			int from_index = exts.indexOf(from);
			Hashtable<String, Double> tos = new Hashtable<>();
			double[] d = dists[from_index];
			double sum = DoubleStream.of(d).sum();
			double tst_sum = 0;
			for (Iterator<String> iterator1 = exts.iterator(); iterator1.hasNext();) {
				String to = iterator1.next();
				int to_index = exts.indexOf(to);
				if (d[to_index] != 0) {
					tos.put(to, d[to_index] / sum);
					tst_sum += tos.get(to);
				}

			}
			System.out.printf("exist: %s \t sum: %f \t test sum: %f\n", from, sum, tst_sum);
			transitions.put(from, tos);
		});
		return transitions;
	}

	/**
	 * the purpose is to elminate the redandant search in the network
	 * distribution and convert it into map
	 *
	 * MAP --> TO-ID --to--> its FROM-NODE-Index
	 */
	public void generateNodesMap() {
		if (networkDist == null || networkDist.isEmpty()) {
			return;
		}
		for (FromNode fromNode : networkDist) {
			ArrayList<ToNode> toNodes = fromNode.getToedges();
			for (ToNode edge : toNodes) {
				if (!nodesMap.containsKey(edge.getID())) {
					// System.out.format("%s\t%d\n", edge.getID(),
					// getFromNodeIndex(edge.getID()));
					nodesMap.put(edge.getID(), getFromNodeIndex(edge.getID()));
				} else {
					nodesMap.replace(edge.getID(), getFromNodeIndex(edge.getID()));
				}

			}
		}
	}

	/**
	 * Fri 09 Jan 2015 09:55:05 AM EET
	 *
	 * Get distance between two exit points with their ids.
	 *
	 * @param ext1
	 * @param ext2
	 * @return
	 */
	private double get_exts_eculidean(String ext1, String ext2) {
		FromNode fext1 = null;
		FromNode fext2 = null;
		try {
			fext1 = networkDist.get(from_map.get(ext1));
			fext2 = networkDist.get(from_map.get(ext2));
		} catch (NullPointerException e) {
			System.out.println("NUll exception from get eculidean between exits in transition builder ...");
			return -1;
		}
		return euclidean(fext1.getX(), fext1.getY(), fext2.getX(), fext2.getY());
	}

	public Hashtable<Integer, ArrayList<Integer>> getAdajcents(int threshold, double[][] dists) {
		ArrayList<Integer> adajcents;
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = new Hashtable<>();
		for (int i = 1; i < dists.length; i++) {
			adajcents = new ArrayList<>();
			for (int j = 1; j < dists.length; j++) {
				double dist = dists[i][j];
				if (dist <= threshold && j != i) {

					adajcents.add(j);
				}
			}
			// System.out.println("adajcents \t"+adajcents.size());
			voronoiNeibors.put(i, adajcents);
		}

		return voronoiNeibors;
	}

	/**
	 * Missed towers position will make the whole procedure to act in incorrect
	 * way. This means if we have 500 tower and the towers contain only 499, the
	 * missed tower will shift all towers to incorrect positions.
	 *
	 * @param towers
	 * @return
	 */
	private double[][] getDistances(Hashtable<Integer, Vertex> towers) {

		double[][] distances = new double[towers.size() + 1][towers.size() + 1];
		for (int i = 1; i <= towers.size(); i++) {
			Vertex vi = towers.get(i);
			for (int j = i; j <= towers.size(); j++) {
				Vertex vj = towers.get(j);
				double dist = euclidean(vi.getX(), vi.getY(), vj.getX(), vj.getY());
				distances[i][j] = dist;
				distances[j][i] = dist;
			}
		}
		return distances;
	}

	/**
	 * Emission probability is the relation between states and observations, the
	 * probability of obtaining specific state if a given observation occurs
	 *
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getEmissionProb(ArrayList<String> exts) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();
		/**
		 * It is impossible to have an exit point related to two different
		 * voronoi zones, so each table created for points to zones emissions
		 * will contain only single element with probability equals 1
		 */

		Hashtable<String, Double> pntToZonesTable;
		final double prob = 1f;

		for (String next : exts) {
			pntToZonesTable = new Hashtable<>();

			if (!nodesMap.containsKey(next)) {

				FromNode node = getFromNode(next);
				pntToZonesTable.put(String.valueOf(node.getZone()), prob);
				// System.out.println("Skip node -->" + node.getZone());

			} else {
				pntToZonesTable.put(String.valueOf(networkDist.get(nodesMap.get(next)).getZone()), prob);
			}
			emissions.put(next, pntToZonesTable);
		}

		return emissions;
	}

	/**
	 * Emission probability is the relation between states and observations, the
	 * probability of obtaining specific state if a given observation occurs
	 *
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getEmissionProb_1(ArrayList<FromNode> map) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();
		/**
		 * It is impossible to have an exit point related to two different
		 * voronoi zones, so each table created for points to zones emissions
		 * will contain only single element with probability equals 1
		 */

		Hashtable<String, Double> pntToZonesTable;
		final double prob = 1f;

		for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
			FromNode node = iterator.next();
			if (node.isIsExit()) {
				pntToZonesTable = new Hashtable<>();
				pntToZonesTable.put(String.valueOf(node.getZone()), prob);
				emissions.put(node.getID(), pntToZonesTable);
			}
		}

		return emissions;
	}

	public ArrayList<String> getExstFromToZone(int from, int to) {
		ArrayList<String> exts = new ArrayList<>();
		for (Iterator<FromNode> iterator = networkDist.iterator(); iterator.hasNext();) {
			FromNode next = iterator.next();
			int zone = next.getZone();
			if (next.isIsExit() && zone == from) {
				ArrayList<ToNode> toNodes = next.getToedges();
				for (Iterator<ToNode> toIt = toNodes.iterator(); toIt.hasNext();) {
					ToNode node = toIt.next();
					// if
					// (!nodesMap.containsKey(node.getID())||!networkDist.contains(nodesMap.get(node.getID())))
					// {
					// continue;
					// }
					FromNode toFN = networkDist.get(nodesMap.get(node.getID()));
					if (toFN.getZone() == to) {
						exts.add(next.getID());
					}
				}
			}

		}
		return exts;
	}

	private FromNode getFromNode(String ID) {

		for (FromNode fromNode : networkDist) {
			if (fromNode.getID().equals(ID)) {
				return fromNode;
			}
		}
		return null;
	}

	private int getFromNodeIndex(String ID) {

		for (int i = 0; i < networkDist.size(); i++) {
			FromNode fromNode = networkDist.get(i);
			if (fromNode.getID().equals(ID)) {
				return i;
			}
		}
		return -1;
	}

	public ArrayList<FromNode> getNetworkDist() {
		return networkDist;
	}

	public Hashtable<String, Integer> getNodesMap() {
		return nodesMap;
	}

	public Hashtable<String, Hashtable<String, Double>> getTransitionProb() {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();
		VConnectivityInspector handler = new VConnectivityInspector();

		Hashtable<Integer, ArrayList<String>> regions = handler.getRegionExists(networkDist);
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);
		System.out.println("voronoiNeibors" + voronoiNeibors.size());

		System.out.println("Regions\t" + regions.size());

		Hashtable<Integer, ArrayList<FromNode>> allRegionsTable = handler.seperateRegions(networkDist);

		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromRegionKey = entrySet.getKey();
			if (!allRegionsTable.containsKey(fromRegionKey)) {
				continue;
			}
			ArrayList<FromNode> regFromNodes = new ArrayList<>();
			regFromNodes.addAll(allRegionsTable.get(fromRegionKey));

			ArrayList<String> fromExts = regions.get(fromRegionKey);
			if (!regions.containsKey(fromRegionKey)) {
				continue;
			}

			ArrayList<Integer> adjacentRegions = entrySet.getValue();

			for (Iterator<Integer> iterator = adjacentRegions.iterator(); iterator.hasNext();) {
				Integer toRegionKey = iterator.next();
				if (!allRegionsTable.containsKey(toRegionKey)) {
					continue;
				}
				ArrayList<FromNode> subGraph = new ArrayList<>();
				subGraph.addAll(allRegionsTable.get(toRegionKey));
				subGraph.addAll(regFromNodes);
				handler.constructMap(subGraph);

				ArrayList<String> toExts = regions.get(toRegionKey);
				if (!regions.containsKey(toRegionKey)) {

					continue;
				}
				// System.out.println("\ttoRegionKey" + toRegionKey + "\t" +
				// toExts.size());

				for (Iterator<String> fromit = fromExts.iterator(); fromit.hasNext();) {
					String fromPnt = fromit.next();
					// System.out.println(fromPnt);
					Hashtable<String, Double> toTrans = new Hashtable<>();
					double sum = 0;
					double max = 0;
					for (Iterator<String> toIt = toExts.iterator(); toIt.hasNext();) {
						String toPnt = toIt.next();
						// System.out.println("\t" + toPnt);
						/**
						 * check connectivity.
						 */
						history = new ArrayList<>();

						if (handler.isConnected(fromPnt, toPnt)) {
							//// System.out.println("connected to");
							// double prob = (double) Math.random();
							// sum += prob;
							// toTrans.put(toPnt, prob);
							double prob = handler.getPathLength(fromPnt, toPnt);
							sum += prob;
							toTrans.put(toPnt, prob);
						}
						// double prob = graph.getPathLength(fromPnt, toPnt);
						// sum += prob;
						// toTrans.put(toPnt, prob);

					}
					/**
					 * Normalize prob.
					 */

					for (Map.Entry<String, Double> toTransEntry : toTrans.entrySet()) {
						String key = toTransEntry.getKey();
						double val = toTransEntry.getValue();
						toTrans.replace(key, val, val / sum);

					}
					if (!toTrans.isEmpty()) {
						transitions.put(fromPnt, toTrans);
					}
				}
			}
		}
		return transitions;
	}

	/**
	 * Sat 15 Nov 2014 10:04:21 PM EET
	 *
	 * @param exts
	 * @return
	 *
	 * 		Allow all transitions for the adjacent/non-adjacent regions.
	 *         Transitions calculated using the shortest path between any two
	 *         points.
	 *
	 *         The differences between mono-exits and bi-exits/ any complex
	 *         states transitions: 1- Mono-exits is simplest. 2- All complex
	 *         states have the same limitations of the single state if the
	 *         number of regions equal the number of points in the complex state
	 *         plus one. 3- Allow all transitions for adjacent/non-adjacent
	 *         regions remains the same but in the multi-state transitions the
	 *         number of the states exponentially increased because of the
	 *         creation of the new states from one region to another.
	 *
	 *         Beam search can not accelerate the HMM calculation with such
	 *         function, as this function produce non zeros transitions
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb(ArrayList<String> exts) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();
		VConnectivityInspector handler = new VConnectivityInspector();
		handler.constructMap(networkDist);

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String fromExt = iterator.next();
			long sum = 0;
			Hashtable<String, Double> toTrans = new Hashtable<>();
			for (Iterator<String> toIt = exts.iterator(); toIt.hasNext();) {
				String toExt = toIt.next();
				double prob = handler.getPathLength(fromExt, toExt);
				;
				// double prob = (double) Math.random();
				sum += prob;
				toTrans.put(toExt, prob);
			}
			/**
			 * Normalize probabilities.
			 */

			for (Map.Entry<String, Double> toTransEntry : toTrans.entrySet()) {
				String key = toTransEntry.getKey();
				double val = toTransEntry.getValue();
				double norm = ((sum - val) / ((toTrans.size() - 1) * sum));
				toTrans.replace(key, norm);
			}
			transitions.put(fromExt, toTrans);

		}

		return transitions;
	}

	/**
	 * Mon 17 Nov 2014 01:22:24 AM EET
	 *
	 * Generate Transitions only for specific set of adjacent regions that lie
	 * with in specific threshold.
	 *
	 * @param threshold
	 * @param zPath
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb(int threshold, ArrayList<String> exts,
			String zPath) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();
		VConnectivityInspector handler = new VConnectivityInspector();

		Hashtable<Integer, ArrayList<String>> regions = handler.getRegionExists(networkDist);

		Hashtable<Integer, ArrayList<FromNode>> allRegionsTable = handler.seperateRegions(networkDist);

		Hashtable<Integer, Vertex> towers = VoronoiConverter.readTower(zPath);
		System.out.println("towers \t" + towers.size());
		double[][] distances = getDistances(towers);
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = getAdajcents(threshold, distances);
		/**
		 *
		 */
		handler.constructMap(networkDist);
		/**
		 * print the selected voronoi neighbors within specific threshold
		 */
		// for (Map.Entry<Integer, ArrayList<Integer>> entry :
		// voronoiNeibors.entrySet()) {
		// Integer integer = entry.getKey();
		// ArrayList<Integer> arrayList = entry.getValue();
		// System.out.printf("%d,\n\t", integer);
		// for (Iterator<Integer> it = arrayList.iterator(); it.hasNext();) {
		// Integer integer1 = it.next();
		// System.out.printf(",%d", integer1);
		// }
		// System.out.println("");
		// }
		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromKey = entrySet.getKey();
			ArrayList<Integer> adjacents = entrySet.getValue();
			if (!regions.containsKey(fromKey)) {
				continue;
			}
			ArrayList<String> fromExts = regions.get(fromKey);

			/**
			 * Construct subgraph that contain all allowed zones and all exit
			 * points from these zones.
			 */
			// ArrayList<FromNode> subGraph = new ArrayList<>();
			//
			// if (allRegionsTable.containsKey(fromKey)) {
			// subGraph.addAll(allRegionsTable.get(fromKey));
			// }
			ArrayList<String> toExts = new ArrayList<>();

			for (Iterator<Integer> iterator = adjacents.iterator(); iterator.hasNext();) {
				Integer toKey = iterator.next();

				if (allRegionsTable.containsKey(toKey) && regions.containsKey(toKey)) {
					// subGraph.addAll(allRegionsTable.get(toKey));
					toExts.addAll(regions.get(toKey));
				}
			}
			/**
			 * Construct map with sub graph, to minimize the dijskra time, nodes
			 * to calculate paths
			 *
			 */
			// System.out.println("To Exits \t" + toExts.size());
			// graph.constructMap(subGraph);

			/**
			 * Create transitions from->to exit points.
			 *
			 */
			for (Iterator<String> iterator = fromExts.iterator(); iterator.hasNext();) {
				String from = iterator.next();
				Hashtable<String, Double> toTrans = new Hashtable<>();
				double sum = 0;
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;

				for (Iterator<String> toIterator = toExts.iterator(); toIterator.hasNext();) {
					String to = toIterator.next();
					if (from.equals(to)) {
						continue;
					}
					// if (graph.isConnected(from, to)) {
					double prob = handler.getPathLength(from, to);
					// double prob = (double) Math.random();
					if (Double.isFinite(prob)) {
						sum += prob;
						toTrans.put(to, prob);
						// System.out.println(max);
						if (min > prob) {
							min = prob;
						}
						if (prob > max) {
							max = prob;
						}
					}
				}
				/**
				 * Allow self transitions with distance less than the min to
				 * maximize the transitions probability if the same observation
				 * passe twice.
				 */

				// sum += min/2;
				// toTrans.put(from, min/2);
				// System.out.println(">>" + max);
				/**
				 * If from point is an isolated point do not add self states
				 */
				if (toTrans.isEmpty()) {
					continue;
				}
				sum += 3 * max;
				toTrans.put(from, 3 * max);

				// System.out.printf("---->> To Transitions \t %d\n",
				// toTrans.size());
				/**
				 * Normalize probabilities and add unconnected.
				 */
				for (Map.Entry<String, Double> toentry : toTrans.entrySet()) {
					String key = toentry.getKey();
					Double val = toentry.getValue();
					// double norm = ((sum - val) / ((toTrans.size() - 1) *
					// sum));
					double norm = val / sum;
					toTrans.replace(key, norm);
				}
				// toTrans.put(from, 0.0);

				// for (Iterator<String> extsIt = exts.iterator();
				// extsIt.hasNext();) {
				// String ext = extsIt.next();
				// if (toTrans.containsKey(ext)) {
				// double val = toTrans.get(ext);
				// double norm = ((sum - val) / ((toTrans.size() - 1) * sum));
				//// double norm = val / sum;
				//// if (Double.isNaN(norm)) {
				//// System.out.printf("sum\t%f\t -- Size\t%d\n", sum,
				// toTrans.size());
				//// }
				// toTrans.replace(ext, norm);
				// } else {
				// toTrans.put(ext, 0.0);
				// }
				// }
				// System.out.printf("---->> To Transitions \t %d\n",
				// toTrans.size());
				if (!toTrans.isEmpty() && toTrans.size() > 0) {
					transitions.put(from, toTrans);
				}
				// break;
			}
		}

		// int count = 0;
		// for (Iterator<String> it = exts.iterator(); it.hasNext();) {
		// String ext = it.next();
		// if (!transitions.containsKey(ext)) {
		// System.out.println("+++++++++\t"+count++);
		// Hashtable<String, Double> tmp = new Hashtable<>();
		// for (Iterator<String> it1 = exts.iterator(); it1.hasNext();) {
		// String string = it1.next();
		// tmp.put(ext, 0f);
		//
		// }
		// transitions.put(ext, tmp);
		// }
		// }
		return transitions;
	}

	/**
	 * Fri 09 Jan 2015 10:12:52 PM EET
	 *
	 * Generate Transitions only for specific set of adjacent regions that lie
	 * with in specific threshold.
	 *
	 * @param threshold
	 * @param zPath
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_1(int threshold, ArrayList<String> exts,
			String zPath) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();
		WieghtedGraph graph = new WieghtedGraph();

		Hashtable<Integer, ArrayList<String>> regions = graph.getRegionExists(networkDist);

		Hashtable<Integer, ArrayList<FromNode>> allRegionsTable = graph.seperateRegions(networkDist);

		Hashtable<Integer, Vertex> towers = VoronoiConverter.readTower(zPath);
		System.out.println("towers \t" + towers.size());
		double[][] distances = getDistances(towers);
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = getAdajcents(threshold / 2, distances);
		/**
		 *
		 */
		graph.constructMap(networkDist);
		/**
		 * print the selected voronoi neighbors within specific threshold
		 */
		// for (Map.Entry<Integer, ArrayList<Integer>> entry :
		// voronoiNeibors.entrySet()) {
		// Integer integer = entry.getKey();
		// ArrayList<Integer> arrayList = entry.getValue();
		// System.out.printf("%d,\n\t", integer);
		// for (Iterator<Integer> it = arrayList.iterator(); it.hasNext();) {
		// Integer integer1 = it.next();
		// System.out.printf(",%d", integer1);
		// }
		// System.out.println("");
		// }
		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromKey = entrySet.getKey();
			ArrayList<Integer> adjacents = entrySet.getValue();
			if (!regions.containsKey(fromKey)) {
				continue;
			}
			ArrayList<String> fromExts = regions.get(fromKey);

			/**
			 * Construct subgraph that contain all allowed zones and all exit
			 * points from these zones.
			 */
			// ArrayList<FromNode> subGraph = new ArrayList<>();
			//
			// if (allRegionsTable.containsKey(fromKey)) {
			// subGraph.addAll(allRegionsTable.get(fromKey));
			// }
			ArrayList<String> toExts = new ArrayList<>();

			for (Iterator<Integer> iterator = adjacents.iterator(); iterator.hasNext();) {
				Integer toKey = iterator.next();

				if (allRegionsTable.containsKey(toKey) && regions.containsKey(toKey)) {
					// subGraph.addAll(allRegionsTable.get(toKey));
					toExts.addAll(regions.get(toKey));
				}
			}
			/**
			 * Construct map with sub graph, to minimize the dijskra time, nodes
			 * to calculate paths
			 *
			 */
			// System.out.println("To Exits \t" + toExts.size());
			// graph.constructMap(subGraph);

			/**
			 * Create transitions from->to exit points.
			 *
			 */
			for (Iterator<String> iterator = fromExts.iterator(); iterator.hasNext();) {
				String from = iterator.next();
				Hashtable<String, Double> toTrans = new Hashtable<>();
				double sum = 0;
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;

				for (Iterator<String> toIterator = toExts.iterator(); toIterator.hasNext();) {
					String to = toIterator.next();
					if (from.equals(to)) {
						continue;
					}
					// if (graph.isConnected(from, to)) {
					double prob = graph.getPathLength(from, to, threshold);
					// double prob = (double) Math.random();
					if (prob != Double.POSITIVE_INFINITY) {
						sum += prob;
						toTrans.put(to, prob);
						// System.out.println(max);
						if (min > prob) {
							min = prob;
						}
						if (prob > max) {
							max = prob;
						}
					}
				}
				/**
				 * Allow self transitions with distance less than the min to
				 * maximize the transitions probability if the same observation
				 * passe twice. /** Allow self transitions with distance less
				 * than the min to maximize the transitions probability if the
				 * same observation passe twice.
				 */

				// sum += min/2;
				// toTrans.put(from, min/2);
				// System.out.println(">>" + max);
				/**
				 * If from point is an isolated point do not add self states
				 */
				if (toTrans.isEmpty()) {
					continue;
				}
				sum += 3 * max;
				toTrans.put(from, 3 * max);

				// System.out.printf("---->> To Transitions \t %d\n",
				// toTrans.size());
				/**
				 * Normalize probabilities and add unconnected.
				 */
				for (Map.Entry<String, Double> toentry : toTrans.entrySet()) {
					String key = toentry.getKey();
					Double val = toentry.getValue();
					// double norm = ((sum - val) / ((toTrans.size() - 1) *
					// sum));
					double norm = val / sum;
					toTrans.replace(key, norm);
				}
				// toTrans.put(from, 0.0);

				// for (Iterator<String> extsIt = exts.iterator();
				// extsIt.hasNext();) {
				// String ext = extsIt.next();
				// if (toTrans.containsKey(ext)) {
				// double val = toTrans.get(ext);
				// double norm = ((sum - val) / ((toTrans.size() - 1) * sum));
				//// double norm = val / sum;
				//// if (Double.isNaN(norm)) {
				//// System.out.printf("sum\t%f\t -- Size\t%d\n", sum,
				// toTrans.size());
				//// }
				// toTrans.replace(ext, norm);
				// } else {
				// toTrans.put(ext, 0.0);
				// }
				// }
				// System.out.printf("---->> To Transitions \t %d\n",
				// toTrans.size());
				if (!toTrans.isEmpty() && toTrans.size() > 0) {
					transitions.put(from, toTrans);
				}
				// break;
			}
		}

		// int count = 0;
		// for (Iterator<String> it = exts.iterator(); it.hasNext();) {
		// String ext = it.next();
		// if (!transitions.containsKey(ext)) {
		// System.out.println("+++++++++\t"+count++);
		// Hashtable<String, Double> tmp = new Hashtable<>();
		// for (Iterator<String> it1 = exts.iterator(); it1.hasNext();) {
		// String string = it1.next();
		// tmp.put(ext, 0f);
		//
		// }
		// transitions.put(ext, tmp);
		// }
		// }
		return transitions;
	}

	/**
	 * Sat 13 Jun 2015 01:25:26 AM JST
	 *
	 * Change the normalization way. We are calculating the transition by the
	 * same way, but we change the normalization to be 1/dist instead of
	 * normalizing by sum.
	 *
	 * @param threshold
	 * @param exts
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_2(int threshold, ArrayList<String> exts) {
		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generate_from_nodes_map();
		System.out.println("Nodes Map size:\t" + from_map.size());
		System.out.println("Exits size:\t" + exts.size());

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String from_ext = iterator.next();
			Hashtable<String, Double> tos = new Hashtable<>();
			double sum = 0;
			for (Iterator<String> toIterator = exts.iterator(); toIterator.hasNext();) {
				String to_ext = toIterator.next();
				double dist = graph.getPathLength(from_ext, to_ext, threshold);
				if (dist > threshold || dist == Double.POSITIVE_INFINITY) {
					continue;
				}

				sum += 1 / dist;
				tos.put(to_ext, (1 / dist));
			}
			if (tos.isEmpty()) {
				continue;
			}
			/**
			 * Self transitions
			 */
			double dist = 1;
			sum += dist;
			tos.replace(from_ext, (1 / dist));

			/**
			 * Normalize probabilities and add unconnected.
			 */
			for (Map.Entry<String, Double> toentry : tos.entrySet()) {
				String key = toentry.getKey();
				Double val = toentry.getValue();
				// double norm = ((sum - val) / ((tos.size() - 1) * sum));
				double norm = val / sum;
				tos.replace(key, norm);
			}

			transitions.put(from_ext, tos);
			System.out.println("From -> To");
		}
		return transitions;
	}

	/**
	 * Sat 13 Jun 2015 02:13:11 AM JST
	 *
	 * Change the normalization way. We are calculating the transition by the
	 * same way, but we change the normalization to be 1/dist instead of
	 * normalizing by sum.
	 *
	 * Exclude transition to nodes belong to the same zone.
	 *
	 * @param threshold
	 * @param exts
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_3(int threshold, ArrayList<String> exts) {
		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		// System.out.println("Nodes Map size:\t" + from_map.size());
		// System.out.println("Exits size:\t" + exts.size());
		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String from_ext = iterator.next();
			int from_zone = -1;
			try {
				from_zone = networkDist.get(nodesMap.get(from_ext)).getZone();
			} catch (Exception e) {
				System.err.println("From node exception \t" + from_ext);
			}
			Hashtable<String, Double> tos = new Hashtable<>();
			double sum = 0;
			for (Iterator<String> toIterator = exts.iterator(); toIterator.hasNext();) {
				String to_ext = toIterator.next();
				int to_zone = -1;
				try {
					to_zone = networkDist.get(nodesMap.get(to_ext)).getZone();
				} catch (Exception e) {
					// continue;
					System.out.println("\t Node \t" + to_ext + "\t has no childs");
				}
				/**
				 * Check if they are belong to the same zone of not. if the
				 * state is not exit state continue.
				 */
				if (from_zone == to_zone || from_zone == -1 || to_zone == -1) {
					// System.out.println("Zones are equal");
					continue;
				}

				double dist = 1 / graph.getPathLength(from_ext, to_ext, threshold);
				if (dist > threshold || dist == Double.POSITIVE_INFINITY) {
					continue;
				}

				sum += dist;
				tos.put(to_ext, dist);
			}
			if (tos.isEmpty()) {
				continue;
			}
			/**
			 * Self transitions
			 */
			double dist = 1;
			sum += dist;
			tos.put(from_ext, dist);

			System.out.println("sum:\t" + sum);
			/**
			 * Normalize probabilities and add unconnected.
			 */
			double tst_sum = 0;
			for (Map.Entry<String, Double> toentry : tos.entrySet()) {
				String key = toentry.getKey();
				double val = toentry.getValue() / sum;
				tst_sum += val;
				// double norm = ((sum - val) / ((tos.size() - 1) * sum));
				// double norm = val / sum;
				tos.replace(key, val);
			}

			if (tst_sum != 1) {
				System.out.println("tst_sum\t" + tst_sum);
			}
			transitions.put(from_ext, tos);
			System.out.println("From -> To");
		}
		return transitions;
	}

	/**
	 * Sat 13 Jun 2015 06:39:51 AM JST
	 *
	 * Change the normalization way. We are calculating the transition by the
	 * same way, but we change the normalization to be 1/dist instead of
	 * normalizing by sum.
	 *
	 * @param threshold
	 * @param exts
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_4(int threshold, ArrayList<String> exts) {
		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		// System.out.println("Nodes Map size:\t" + from_map.size());
		// System.out.println("Exits size:\t" + exts.size());
		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String from_ext = iterator.next();
			int from_zone = -1;
			try {
				from_zone = networkDist.get(nodesMap.get(from_ext)).getZone();
			} catch (Exception e) {
				System.err.println("From node exception \t" + from_ext);
			}
			Hashtable<String, Double> tos = new Hashtable<>();
			double sum = 0;
			double min = Double.MAX_VALUE;

			for (Iterator<String> toIterator = exts.iterator(); toIterator.hasNext();) {
				String to_ext = toIterator.next();
				int to_zone = -1;
				try {
					to_zone = networkDist.get(nodesMap.get(to_ext)).getZone();
				} catch (Exception e) {
					// continue;
					System.out.println("\t Node \t" + to_ext + "\t has no childs");
				}
				/**
				 * Check if they are belong to the same zone of not. if the
				 * state is not exit state continue.
				 */
				if (from_zone == to_zone || from_zone == -1 || to_zone == -1 || from_ext.equals(to_ext)) {
					// System.out.println("Zones are equal");
					continue;
				}

				double dist = graph.getPathLength(from_ext, to_ext, threshold);
				if (min > dist) {
					min = dist;
				}
				dist = 1 / dist;
				if (dist == Double.POSITIVE_INFINITY) {
					continue;
				}

				sum += dist;
				tos.put(to_ext, dist);
			}
			if (tos.isEmpty()) {
				continue;
			}
			/**
			 * Self transitions
			 */
			double dist = 1 / min;
			// dist = 1 / dist;
			sum += dist;
			tos.put(from_ext, dist);

			System.out.println("sum:\t" + sum);
			/**
			 * Normalize probabilities and add unconnected.
			 */
			double tst_sum = 0;
			for (Map.Entry<String, Double> toentry : tos.entrySet()) {
				String key = toentry.getKey();
				double val = toentry.getValue() / sum;
				tst_sum += val;
				// double norm = ((sum - val) / ((tos.size() - 1) * sum));
				// double norm = val / sum;
				tos.replace(key, val);
			}

			if (tst_sum != 1) {
				System.out.println("tst_sum\t" + tst_sum);
			}
			transitions.put(from_ext, tos);
			System.out.println("From -> To");
		}
		return transitions;
	}

	/**
	 * Wed Oct 7 04:45:55 JST 2015
	 *
	 * Transition to all states ..
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_5(ArrayList<String> exts) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);
		System.out.println("End building graph ...");

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		double[][] dists = new double[exts.size()][exts.size()];
		for (int i = 0; i < dists.length; i++) {
			Arrays.fill(dists[i], -1);
		}

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String from = iterator.next();
			int from_index = exts.indexOf(from);
			double max = Double.MIN_VALUE;
			for (Iterator<String> iterator1 = exts.iterator(); iterator1.hasNext();) {
				String to = iterator1.next();
				int to_index = exts.indexOf(to);
				if (from_index == to_index || dists[from_index][to_index] != -1) {
					continue;
				}

				double d = graph.getPathLength(from, to);
				dists[from_index][to_index] = 1 / d;
				dists[to_index][from_index] = 1 / d;

				if (max < d) {
					max = d;
				}
			}
			dists[from_index][from_index] = 1 / max;
		}

		// double max = Double.MIN_VALUE;
		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String from = iterator.next();
			int from_index = exts.indexOf(from);
			Hashtable<String, Double> tos = new Hashtable<>();
			double[] d = dists[from_index];
			double sum = DoubleStream.of(d).sum();
			for (Iterator<String> iterator1 = exts.iterator(); iterator1.hasNext();) {
				String to = iterator1.next();
				int to_index = exts.indexOf(to);
				tos.put(to, d[to_index] / sum);
			}
			transitions.put(from, tos);
		}
		return transitions;
	}

	/**
	 * Sat Oct 3 15:06:22 JST 2015
	 *
	 * transition to adjacent zones only ... transition done based on the
	 * straight line length instead of the real map with no self transition.
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_6() {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		// WieghtedGraph graph = new WieghtedGraph();
		// graph.constructMap(networkDist);
		// System.out.println("End building graph ...");
		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();

		VConnectivityInspector handler = new VConnectivityInspector();

		Hashtable<Integer, ArrayList<String>> regions = handler.getRegionExists(networkDist);
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);

		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromRegionKey = entrySet.getKey();
			// if (!allRegionsTable.containsKey(fromRegionKey)) {
			// continue;
			// }
			// ArrayList<FromNode> regFromNodes = new ArrayList<>();
			// regFromNodes.addAll(allRegionsTable.get(fromRegionKey));
			if (!regions.containsKey(fromRegionKey)) {
				continue;
			}
			ArrayList<String> fromExts = regions.get(fromRegionKey);
			if (fromExts.isEmpty()) {
				continue;
			}
			// ArrayList<String> toExts = new ArrayList<>();
			ArrayList<Integer> adjacentRegions = entrySet.getValue();
			System.out.printf("From zone: %d\n there are %d neighbors\n", fromRegionKey, adjacentRegions.size());
			// for (Iterator<Integer> iterator = adjacentRegions.iterator();
			// iterator.hasNext();) {
			// Integer toRegionKey = iterator.next();
			// if (!regions.containsKey(toRegionKey)) {
			// System.out.println("Region doesn't available in regions table:
			// "+toRegionKey);
			// }
			// toExts.addAll(regions.get(toRegionKey));
			// }

			for (Iterator<String> iterator = fromExts.iterator(); iterator.hasNext();) {
				String from_ext = iterator.next();
				FromNode from_ext_node = networkDist.get(nodesMap.get(from_ext));

				int from_zone = -1;
				try {
					from_zone = networkDist.get(nodesMap.get(from_ext)).getZone();
				} catch (Exception e) {
					System.err.println("From node exception \t" + from_ext);
				}
				Hashtable<String, Double> tos = new Hashtable<>();
				double sum = 0;
				// double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;
				for (int i = 0; i < adjacentRegions.size(); i++) {
					Integer toRegionKey = adjacentRegions.get(i);
					if (!regions.containsKey(toRegionKey)) {
						System.out.println("Region doesn't available in regions table: " + toRegionKey);
						continue;
					}

					ArrayList<String> toExts = regions.get(toRegionKey);
					for (Iterator<String> toIterator = toExts.iterator(); toIterator.hasNext();) {
						String to_ext = toIterator.next();
						FromNode to_ext_node = networkDist.get(nodesMap.get(to_ext));

						if (from_ext.compareTo(to_ext) == 0) {
							continue;
						}

						int to_zone = -1;
						try {
							to_zone = networkDist.get(nodesMap.get(to_ext)).getZone();
						} catch (Exception e) {
							// continue;
							System.out.println("\t Node \t" + to_ext + "\t has no childs");
						}
						/**
						 * Check if they are belong to the same zone of not. if
						 * the state is not exit state continue.
						 */
						if (from_zone == to_zone || from_zone == -1 || to_zone == -1) {
							// System.out.println("Zones are equal");
							continue;
						}

						double dist = euclidean(from_ext_node.getX(), from_ext_node.getY(), to_ext_node.getX(),
								to_ext_node.getY());
						// double dist = graph.getPathLength(from_ext, to_ext,
						// threshold);
						if (max < dist) {
							max = dist;
						}
						dist = 1 / dist;
						if (dist == Double.POSITIVE_INFINITY) {
							continue;
						}

						sum += dist;
						tos.put(to_ext, dist);
					}
				}
				// for (Iterator<Integer> iterator1 =
				// adjacentRegions.iterator(); iterator.hasNext();) {
				// Integer toRegionKey = iterator1.next();
				// }
				if (tos.isEmpty()) {
					continue;
				}
				/**
				 * Self transitions
				 */

				// double dist;
				//
				// if (min < 1) {
				// dist = min;
				// } else {
				// dist = min - 1;
				// }
				//
				double dist = 1 / max;
				sum += dist;
				tos.put(from_ext, dist);
				// if (sum < 0) {
				// System.out.println("");
				// }
				System.out.println("sum:\t" + sum);
				/**
				 * Normalize probabilities and add unconnected.
				 */
				double tst_sum = 0;
				for (Map.Entry<String, Double> toentry : tos.entrySet()) {
					String key = toentry.getKey();
					double val = toentry.getValue() / sum;
					tst_sum += val;
					// double norm = ((sum - val) / ((tos.size() - 1) * sum));
					// double norm = val / sum;
					tos.replace(key, val);
				}

				if (tst_sum != 1) {
					System.out.println("tst_sum\t" + tst_sum);
				}
				transitions.put(from_ext, tos);
				System.out.println("From -> To");
			}

		}
		return transitions;
	}

	/**
	 * Fri 09 Jan 2015 09:55:05 AM EET
	 *
	 * @param threshold
	 * @param exts
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb_mod(int threshold, ArrayList<String> exts) {
		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generate_from_nodes_map();
		System.out.println("Nodes Map size:\t" + from_map.size());
		System.out.println("Exits size:\t" + exts.size());

		WieghtedGraph graph = new WieghtedGraph();
		graph.constructMap(networkDist);
		// double[][] dists = new double[exts.size()][exts.size()];
		// for (int i = 0; i < dists.length; i++) {
		// for (int j = i; j < dists.length; j++) {
		// dists[i][j] = graph.getPathLength(exts.get(i), exts.get(j));
		// dists[j][i] = dists[i][j];
		// }
		// System.out.printf("dist ----> %d row\n" + i);
		// }

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String from_ext = iterator.next();
			Hashtable<String, Double> tos = new Hashtable<>();
			double max = Double.MIN_VALUE;
			double sum = 0;
			for (Iterator<String> toIterator = exts.iterator(); toIterator.hasNext();) {
				String to_ext = toIterator.next();
				// if (graph.isConnected(from_ext, to_ext)) {
				// double dist = get_exts_eculidean(from_ext, to_ext);
				double dist = graph.getPathLength(from_ext, to_ext, threshold);
				if (dist > threshold) {
					continue;
				}
				// double dist =
				// dists[exts.indexOf(from_ext)][exts.indexOf(to_ext)];
				// double dist = Math.random();
				// System.out.println("dist\t" + dist);
				if (dist == Double.POSITIVE_INFINITY) {
					// System.out.println("Greater than threshold");
					continue;
				}
				if (dist > max) {
					max = dist;
				}
				sum += dist;
				tos.put(to_ext, dist);
				// }
			}
			if (tos.isEmpty()) {
				continue;
			}
			double dist = 2 * max;
			sum += dist;
			tos.replace(from_ext, dist);

			/**
			 * Normalize probabilities and add unconnected.
			 */
			for (Map.Entry<String, Double> toentry : tos.entrySet()) {
				String key = toentry.getKey();
				Double val = toentry.getValue();
				double norm = ((sum - val) / ((tos.size() - 1) * sum));
				tos.replace(key, norm);
			}

			transitions.put(from_ext, tos);
			System.out.println("From -> To");
		}
		return transitions;
	}

	public Hashtable<String, Hashtable<String, Double>> getTransitionProb2() {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		Hashtable<String, Hashtable<String, Double>> transitions = new Hashtable<>();
		generateNodesMap();
		VConnectivityInspector handler = new VConnectivityInspector();

		Hashtable<Integer, ArrayList<String>> regions = handler.getRegionExists(networkDist);
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(this.vorNeighborsPath);
		System.out.println("voronoiNeibors" + voronoiNeibors.size());

		System.out.println("Regions\t" + regions.size());

		Hashtable<Integer, ArrayList<FromNode>> allRegionsTable = handler.seperateRegions(networkDist);

		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromRegionKey = entrySet.getKey();
			if (!allRegionsTable.containsKey(fromRegionKey) || !regions.containsKey(fromRegionKey)) {
				continue;
			}

			ArrayList<Integer> adjacentRegions = entrySet.getValue();

			for (Iterator<Integer> iterator = adjacentRegions.iterator(); iterator.hasNext();) {
				Integer toRegionKey = iterator.next();
				if (!allRegionsTable.containsKey(toRegionKey) || !regions.containsKey(toRegionKey)) {
					continue;
				}
				ArrayList<String> fromExts = getExstFromToZone(fromRegionKey, toRegionKey);

				// System.out.println("fromExts"+fromExts.size());
				// System.out.println("fromRegionKey: " + fromRegionKey + "\t" +
				// fromExts.size());
				ArrayList<FromNode> subGraph = new ArrayList<>();
				subGraph.addAll(allRegionsTable.get(toRegionKey));
				subGraph.addAll(allRegionsTable.get(fromRegionKey));
				handler.constructMap(subGraph);

				ArrayList<String> toExts = regions.get(toRegionKey);
				// System.out.println("\ttoRegionKey: " + toRegionKey + "\t" +
				// toExts.size());

				for (Iterator<String> fromit = fromExts.iterator(); fromit.hasNext();) {
					String fromPnt = fromit.next();
					// System.out.println(fromPnt);
					Hashtable<String, Double> toTrans = new Hashtable<>();
					double sum = 0;
					for (Iterator<String> toIt = toExts.iterator(); toIt.hasNext();) {
						String toPnt = toIt.next();
						// System.out.println("\t" + toPnt);
						/**
						 * check connectivity.
						 */
						// history = new ArrayList<>();

						if (handler.isConnected(fromPnt, toPnt)) {
							// System.out.println("is Connected");
							// double prob = graph.getPathLength(fromPnt,
							// toPnt);;
							double prob = Math.random();
							sum += prob;
							toTrans.put(toPnt, prob);
						}

					}
					/**
					 * Normalize prob.
					 */

					for (Map.Entry<String, Double> toTransEntry : toTrans.entrySet()) {
						String key = toTransEntry.getKey();
						double val = toTransEntry.getValue();
						// double norm = ((sum - val) / ((toTrans.size() - 1) *
						// sum));
						// toTrans.replace(fromkey, val, norm);

						// double nval1 = toTrans.get(fromKey);
						double norm = val / sum;
						//// System.out.printf("%f , %f\n",val, norm);
						toTrans.replace(key, norm);
					}
					// double nval2=
					// transitions.get("-166458117#4").get("166458110#0");
					if (!toTrans.isEmpty()) {
						transitions.put(fromPnt, toTrans);
					}
				}

				/**
				 * Constraint violation
				 */
				// for (Iterator<String> fromit = fromExts.iterator();
				// fromit.hasNext();) {
				// String fromPnt = fromit.next();
				//// System.out.println(fromPnt);
				// Hashtable<String, Double> toTrans = transitions.get(fromPnt);
				//
				// for (Iterator<String> toIt = toExts.iterator();
				// toIt.hasNext();) {
				// String toPnt = toIt.next();
				// if (!toTrans.containsKey(toPnt)) {
				// /**
				// * Add a very small probability to move to this exit point
				// */
				// toTrans.put(toPnt, 0.00001f);
				// }
				//
				// }
				// transitions.replace(fromPnt, toTrans);
				// }
			}
		}
		return transitions;
	}

	/**
	 * scale the probability table to overcome the NaN problem results from
	 * multiplications of small numbers ....
	 *
	 * @param prob_table
	 * @param scale
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> prob_scale(
			Hashtable<String, Hashtable<String, Double>> prob_table) {
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : prob_table.entrySet()) {
			String fromkey = entrySet.getKey();
			Hashtable<String, Double> value = entrySet.getValue();
			for (Map.Entry<String, Double> entrySet1 : value.entrySet()) {
				String tokey = entrySet1.getKey();
				double prob = entrySet1.getValue();
				value.replace(tokey, Math.log(prob));
			}
			prob_table.replace(fromkey, value);
		}

		return prob_table;

	}

	public void removeUnconneced() {
		VConnectivityInspector handler = new VConnectivityInspector();

		Hashtable<Integer, ArrayList<String>> regions = handler.getRegionExists(networkDist);
		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(vorNeighborsPath);
		// System.out.println("voronoiNeibors\t"+voronoiNeibors.size());
		// System.out.println("networkDist\t"+networkDist.size());
		Hashtable<Integer, ArrayList<FromNode>> allRegionsTable = handler.seperateRegions(networkDist);

		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer fromRegionKey = entrySet.getKey();
			if (!allRegionsTable.containsKey(fromRegionKey)) {
				continue;
			}
			ArrayList<FromNode> subGraph = new ArrayList<>(allRegionsTable.get(fromRegionKey));

			if (!regions.containsKey(fromRegionKey)) {
				continue;
			}
			System.out.printf("\n%d,\n", fromRegionKey);
			ArrayList<Integer> adjacentRegions = entrySet.getValue();

			for (Iterator<Integer> iterator = adjacentRegions.iterator(); iterator.hasNext();) {
				Integer toRegionKey = iterator.next();
				if (!allRegionsTable.containsKey(toRegionKey)) {
					continue;
				}
				ArrayList<String> fromExts = getExstFromToZone(fromRegionKey, toRegionKey);
				ArrayList<FromNode> regToNodes = new ArrayList<>(allRegionsTable.get(toRegionKey));
				regToNodes.addAll(subGraph);
				handler.constructMap(regToNodes);

				ArrayList<String> toExts = regions.get(toRegionKey);
				if (!regions.containsKey(toRegionKey)) {

					continue;
				}
				outer: for (Iterator<String> fromit = fromExts.iterator(); fromit.hasNext();) {
					String fromPnt = fromit.next();
					for (Iterator<String> toIt = toExts.iterator(); toIt.hasNext();) {
						String toPnt = toIt.next();
						/**
						 * check connectivity. if the two regions are connected,
						 * check the next neighbor
						 */

						if (handler.isConnected(fromPnt, toPnt)) {
							System.out.print("," + toRegionKey);
							break outer;
						}
					}
				}
			}

		}
	}

	public void writeZonesTrans(Hashtable<String, Hashtable<String, Double>> prob, String path) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("exit-defs");
			doc.appendChild(rootElement);

			for (Map.Entry<String, Hashtable<String, Double>> entrySet : prob.entrySet()) {
				String pKey = entrySet.getKey();
				Element fromNodeElement = doc.createElement("fromNode");
				rootElement.appendChild(fromNodeElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(pKey);
				fromNodeElement.setAttributeNode(attr);

				int fromZone = 0;
				try {
					fromZone = networkDist.get(nodesMap.get(pKey)).getZone();
				} catch (NullPointerException e) {
					fromZone = -1;
				}

				attr = doc.createAttribute("zone");
				attr.setValue(String.valueOf(fromZone));
				fromNodeElement.setAttributeNode(attr);

				// System.out.println(pKey);
				Hashtable<String, Double> value = entrySet.getValue();
				for (Map.Entry<String, Double> entrySet1 : value.entrySet()) {
					String zoneKey = entrySet1.getKey();
					int toZone = 0;
					try {
						toZone = networkDist.get(nodesMap.get(zoneKey)).getZone();
					} catch (NullPointerException e) {
						toZone = -1;
					}
					Element toNodeElement = doc.createElement("to");
					fromNodeElement.appendChild(toNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(zoneKey);
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("zone");
					attr.setValue(String.valueOf(toZone));
					toNodeElement.setAttributeNode(attr);

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
}
