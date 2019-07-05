/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import Observations.Obs;
import utils.FromNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class GravityModel {

	/**
	 * Returns the maximum value in the array a[], -infinity if no such value.
	 */
	public static double max(double[] a) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < a.length; i++) {
			if (Double.isNaN(a[i])) {
				return Double.NaN;
			}
			if (a[i] > max) {
				max = a[i];
			}
		}
		return max;
	}

	/**
	 * Returns the average value in the array a[], NaN if no such value.
	 */
	public static double mean(double[] a) {
		if (a.length == 0) {
			return Double.NaN;
		}
		double sum = 0.0;
		for (int i = 0; i < a.length; i++) {
			sum = sum + a[i];
		}
		return sum / a.length;
	}

	/**
	 * Returns the minimum value in the array a[], +infinity if no such value.
	 */
	public static double min(double[] a) {
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < a.length; i++) {
			if (Double.isNaN(a[i])) {
				return Double.NaN;
			}
			if (a[i] < min) {
				min = a[i];
			}
		}
		return min;
	}

	/**
	 * Returns the sample standard deviation in the array a[], NaN if no such
	 * value.
	 */
	public static double stddev(double[] a) {
		return Math.sqrt(var(a));
	}

	/**
	 * Returns the sample variance in the array a[], NaN if no such value.
	 */
	public static double var(double[] a) {
		if (a.length == 0) {
			return Double.NaN;
		}
		double avg = mean(a);
		double sum = 0.0;
		for (int i = 0; i < a.length; i++) {
			sum += (a[i] - avg) * (a[i] - avg);
		}
		return sum / (a.length - 1);
	}

	/**
	 * Returns the population variance in the array a[], NaN if no such value.
	 */
	public static double varp(double[] a) {
		if (a.length == 0) {
			return Double.NaN;
		}
		double avg = mean(a);
		double sum = 0.0;
		for (int i = 0; i < a.length; i++) {
			sum += (a[i] - avg) * (a[i] - avg);
		}
		return sum / a.length;
	}

	final String RLM = "/";
	final String CLM = ",";
	final int HOUR = 60;

	final String SUB_EDGE = "_interpolated";

	final String regex = "_interpolated_\\[([0-9]+)\\]";

	// This class represents the traffic flows computed using the Gravity Model
	/*
	 * G(O, D) = O_out \times D_in / distance(O, D)^2
	 * 
	 * G(O, D) is the flow obtained by the gravity model O is origin; it
	 * represents the source of the traffic D is the destination O_out is the
	 * sum of flow outs of O D_in is the sum of flow ins into D distance(O, D)
	 * is the geographic distance between O and D
	 * 
	 */
	// ArrayList<String, Coordinate> EdgeCoordinates; // the edge in some
	// Cartesian coordinates
	Hashtable<String, Hashtable<String, Double>> distances; // the GeoDistance
															// Between Two Edges

	Hashtable<String, Hashtable<String, Double>> Flow; // flow between a
														// specific origin and
														// destination

	Hashtable<String, Double> Outs; // total flow out of a specific edge

	Hashtable<String, Double> Ins; // total flow into a specific edg

	public GravityModel() {
		Flow = new Hashtable<>();
		Outs = new Hashtable<>();
		Ins = new Hashtable<>();
		distances = new Hashtable<>();

	}

	public void average_flow(int no_day) {
		/**
		 * Avg flow
		 */
		// System.out.println("Flow");
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
			String fromkey = entrySet.getKey();
			Hashtable<String, Double> to_values = entrySet.getValue();
			for (Map.Entry<String, Double> toEntrySet : to_values.entrySet()) {
				String key = toEntrySet.getKey();
				double value = toEntrySet.getValue() / no_day;
				// System.out.println(toEntrySet.getValue());
				to_values.replace(key, value);
			}
			Flow.replace(fromkey, to_values);
		}

		/**
		 * Avg flow out
		 */
		for (Map.Entry<String, Double> entrySet : Outs.entrySet()) {
			String key = entrySet.getKey();
			double value = entrySet.getValue() / no_day;
			Outs.replace(key, value);
		}
		/**
		 * Avg flow in
		 */
		for (Map.Entry<String, Double> entrySet : Ins.entrySet()) {
			String key = entrySet.getKey();
			double value = entrySet.getValue() / no_day;
			Ins.replace(key, value);

		}
	}

	/**
	 * Compute average Gravity model values.
	 *
	 * @param fgod
	 * @return
	 */
	public Hashtable<Double, ArrayList<Double>> avgGOD(Hashtable<Double, ArrayList<Double>> fgod) {
		Hashtable<Double, ArrayList<Double>> avg = new Hashtable<>();

		for (Map.Entry<Double, ArrayList<Double>> entrySet : fgod.entrySet()) {
			Double key = entrySet.getKey();
			double sum = 0;
			double nmax = Double.MIN_VALUE;
			ArrayList<Double> value = entrySet.getValue();
			value.trimToSize();
			double[] stdValues = new double[value.size()];
			int i = 0;

			// for (Iterator<Double> iterator = value.iterator();
			// iterator.hasNext();) {
			// double val = iterator.next();
			// if (val > nmax) {
			// nmax = val;
			// }
			//
			// }
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			i = 0;
			stdValues = new double[value.size()];
			for (Iterator<Double> iterator = value.iterator(); iterator.hasNext();) {
				double val = iterator.next();
				// if (val == nmax) {
				// continue;
				// }
				stdValues[i++] = val;
				sum += val;
				if (val > max) {
					max = val;
				}
				if (val < min) {
					min = val;
				}

			}

			// System.out.println(sum / value.size());
			ArrayList<Double> tmp = new ArrayList<>();
			tmp.add(new Mean().evaluate(stdValues));
			tmp.add(Double.valueOf(value.size()));
			tmp.add(new Max().evaluate(stdValues));
			tmp.add(new Min().evaluate(stdValues));
			double std = new StandardDeviation().evaluate(stdValues);
			// double std = stddev(stdValues);
			tmp.add(std);
			tmp.add(std / Math.sqrt(value.size()));
			avg.put(key, tmp);
		}
		return avg;
	}

	/**
	 * Calculate the distances for the obtained flow.
	 *
	 * @param map
	 */
	public void calcDistances(ArrayList<FromNode> map) {
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
			String fromKey = entrySet.getKey();
			Hashtable<String, Double> value = entrySet.getValue();
			FromNode fromNode = getFromNode(map, fromKey);
			Hashtable<String, Double> toPnts = new Hashtable<>();
			for (Map.Entry<String, Double> toEntry : value.entrySet()) {
				String tokey = toEntry.getKey();
				FromNode toNode = getFromNode(map, tokey);
				/**
				 * Calculate the distance between from_node and to_node
				 */
				try {
					// double dist = Math.sqrt(Math.pow((fromNode.getX() -
					// toNode.getX()), 2) + Math.pow((fromNode.getY() -
					// toNode.getY()), 2));
					double dist = eculidean(fromNode.getX(), toNode.getX(), fromNode.getY(), toNode.getY());
					// System.out.println(dist + "\t ---> \t" + dist / 100);
					toPnts.put(tokey, dist);
				} catch (Exception e) {
					System.out.println(fromKey + "-->" + tokey);
				}

			}
			distances.put(fromKey, toPnts);

		}

	}

	/**
	 * Calculate distances between Voronnoi zones centers.
	 *
	 * @param towers
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> calcDistances(Hashtable<Integer, Vertex> towers) {

		for (Map.Entry<Integer, Vertex> entrySet : towers.entrySet()) {
			Integer from_key = entrySet.getKey();
			Vertex from_zone_vertex = entrySet.getValue();
			Hashtable<String, Double> to_distances = new Hashtable<>();
			for (Map.Entry<Integer, Vertex> to_entrySet : towers.entrySet()) {
				Integer to_key = to_entrySet.getKey();
				Vertex to_zone_vertex = to_entrySet.getValue();
				to_distances.put(to_key.toString(), eculidean(from_zone_vertex.getX(), to_zone_vertex.getX(),
						from_zone_vertex.getY(), to_zone_vertex.getY()));
			}
			distances.put(from_key.toString(), to_distances);
		}
		return distances;
	}

	public void calcDistances_1(ArrayList<FromNode> map) {
		for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
			FromNode from = iterator.next();
			for (Iterator<FromNode> iterator1 = map.iterator(); iterator1.hasNext();) {
				FromNode to = iterator1.next();
				double dist = Math
						.sqrt(Math.pow((from.getX() - to.getX()), 2) + Math.pow((from.getY() - to.getY()), 2));
				if (distances.containsKey(from.getID())) {
					Hashtable<String, Double> toPnts = distances.get(from.getID());
					toPnts.put(to.getID(), dist);
					distances.replace(from.getID(), toPnts);
				} else {
					Hashtable<String, Double> toPnts = new Hashtable<>();
					toPnts.put(to.getID(), dist);
					distances.put(from.getID(), toPnts);
				}
			}

		}
	}

	/**
	 *
	 * @return
	 */
	public Hashtable<Double, ArrayList<Double>> computeGOD(double threshold, boolean verbose) {
		// final double threshold = 200f;

		Hashtable<Double, ArrayList<Double>> fgod = new Hashtable<>();
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
			String fromkey = entrySet.getKey();
			Hashtable<String, Double> toFlow = entrySet.getValue();
			Hashtable<String, Double> toDistances = distances.get(fromkey);

			double outs = Outs.get(fromkey);
			for (Map.Entry<String, Double> toEntry : toFlow.entrySet()) {
				String toKey = toEntry.getKey();
				if (toKey == null || !toDistances.containsKey(toKey)) {
					continue;
				}
				double flow = toEntry.getValue();
				if (fromkey.equals(toKey)) {
					continue;
				}

				double dist = toDistances.get(toKey);
				// gravity is not a valide model for distance greater than 120km
				if (dist == 0 || dist <= threshold || dist >= 120000) {
					continue;
				}
				double ins = Ins.get(toKey);

				double god = god(ins, outs, dist/* round(dist, 100) */);

				if (verbose) {
					System.out.printf("%s\t%s\t%f\t%f\t%f\t%f\t%f\n", fromkey, toKey, dist, flow, outs, ins, god);
				}
				if (fgod.containsKey(flow)) {
					ArrayList<Double> tmp = fgod.get(flow);
					tmp.add(god);
					fgod.replace(flow, tmp);
				} else {
					ArrayList<Double> tmp = new ArrayList<>();
					tmp.add(god);
					fgod.put(flow, tmp);

				}

				// System.out.println(flow + "," + god);
			}

		}
		// return fgod;
		return exclude_outliers(fgod);
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

	private Hashtable<Double, ArrayList<Double>> exclude_outliers(Hashtable<Double, ArrayList<Double>> fgod) {
		Hashtable<Double, ArrayList<Double>> final_god = new Hashtable<>();
		for (Map.Entry<Double, ArrayList<Double>> entrySet : fgod.entrySet()) {
			Double key = entrySet.getKey();
			ArrayList<Double> value = entrySet.getValue();
			value.trimToSize();
			if (value.size() > 1) {
				final_god.put(key, value);
			}
		}
		return final_god;
	}

	public double find_all_linear_regression(Hashtable<Double, ArrayList<Double>> avgGOD, boolean intercept) {

		// int n = 0;
		// int size = 0;
		// for (Map.Entry<Double, ArrayList<Double>> entrySet :
		// avgGOD.entrySet()) {
		// ArrayList<Double> value = entrySet.getValue();
		// size += value.size();
		//
		// }
		// double[] x = new double[size];
		// double[] y = new double[size];
		//
		// for (Map.Entry<Double, ArrayList<Double>> entrySet :
		// avgGOD.entrySet()) {
		// Double key = entrySet.getKey();
		// ArrayList<Double> value = entrySet.getValue();
		// for (Iterator<Double> iterator = value.iterator();
		// iterator.hasNext();) {
		// Double next = iterator.next();
		// x[n] = key;
		// y[n] = next;
		// n++;
		// }
		//
		// }
		SimpleRegression regression = new SimpleRegression(!intercept);

		for (Map.Entry<Double, ArrayList<Double>> entrySet : avgGOD.entrySet()) {
			Double key = entrySet.getKey();
			ArrayList<Double> value = entrySet.getValue();
			for (Iterator<Double> iterator = value.iterator(); iterator.hasNext();) {
				Double next = iterator.next();
				regression.addData(key, next);
				// x[n] = key;
				// y[n] = next;
				// n++;
			}

		}

		// System.out.println("KendallsCorrelation\t" + new
		// KendallsCorrelation().correlation(x, y));
		// System.out.println("PearsonsCorrelation\t" + new
		// PearsonsCorrelation().correlation(x, y));
		// linear_reg(x, y);
		// return new PearsonsCorrelation().correlation(x, y);
		// return new SpearmansCorrelation().correlation(x, y);
		RegressionResults rr = regression.regress();
		return rr.getAdjustedRSquared();
	}

	/**
	 * Find Linear regression with avg values.
	 *
	 * @param avgGOD
	 */
	public double find_avg_linear_regression(Hashtable<Double, ArrayList<Double>> avgGOD, boolean intercept) {

		// int n = 0;
		// double[] x = new double[avgGOD.size()];
		// double[] y = new double[avgGOD.size()];
		SimpleRegression regression = new SimpleRegression(!intercept);
		for (Map.Entry<Double, ArrayList<Double>> entrySet : avgGOD.entrySet()) {
			// x[n] = entrySet.getKey();
			// y[n] = entrySet.getValue().get(0).doubleValue();
			// n++;

			regression.addData(entrySet.getKey(), entrySet.getValue().get(0).doubleValue());
		}
		// double cor = new KendallsCorrelation().correlation(x, y);
		// System.out.println("KendallsCorrelation\t" + new
		// KendallsCorrelation().correlation(x, y));
		// System.out.println("PearsonsCorrelation\t" + new
		// PearsonsCorrelation().correlation(x, y));
		// linear_reg(x, y);
		// return new PearsonsCorrelation().correlation(x, y);
		// return new SpearmansCorrelation().correlation(x, y);

		// double data[][] = {x, y};
		// regression.addData(data);
		RegressionResults rr = regression.regress();
		return rr.getAdjustedRSquared();
	}

	public Hashtable<String, Integer> generateNodesMap(ArrayList<FromNode> map) {
		Hashtable<String, Integer> nodesMap = new Hashtable<>();

		for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
			FromNode next = iterator.next();
			nodesMap.put(next.getID(), next.getZone());
		}
		return nodesMap;
	}

	public Hashtable<String, Hashtable<String, Double>> getDistances() {
		return distances;
	}

	/**
	 * Get specific node with the given id
	 *
	 * @param map
	 *            of all nodes in the map
	 * @param ID
	 *            Current node
	 * @return
	 */
	private FromNode getFromNode(ArrayList<FromNode> map, String ID) {

		for (FromNode fromNode : map) {
			if (fromNode.getID().equals(ID)) {
				return fromNode;
			}
		}
		return null;
	}

	/**
	 * Calculate G(O,D) for specific ins,and outs.
	 *
	 * @param ins
	 * @param outs
	 * @param distance
	 * @return
	 */
	private double god(double ins, double outs, double distance) {
		return ((ins * outs) / Math.pow(distance, 2));
	}

	private String handle_edge(String edge) {
		if (edge.contains(SUB_EDGE)) {
			edge = edge.substring(0, edge.indexOf(SUB_EDGE));
		}
		return edge;
	}

	/**
	 * Find the hourly flow of traffic over edges after that sum in and out over
	 * individual zones.
	 *
	 * @param obs
	 *            sequence of calculated trajectories
	 * @param map
	 *            network to identify edge's zone
	 * @param L
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handle_hourly_zones_Flow(ArrayList<String> obs,
			Hashtable<String, Integer> nodesMap, int L) {
		for (Iterator<String> iterator = obs.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			if (path.isEmpty()) {
				continue;
			}

			/**
			 * Create path sequences for observations that related to sub-zone
			 * of the country.
			 */
			String[] seqs = new String[] { path };
			;
			if (path.contains(RLM)) {
				seqs = path.split(RLM);
			}

			for (int i = 0; i < seqs.length; i++) {
				String[] pnts = seqs[i].split(CLM);
				if (pnts.length <= L) {
					continue;
				}
				String strt = pnts[0];
				String end = pnts[pnts.length - 1];

				// if (strt.equals(end)) {
				// continue;
				// }
				/**
				 * Update flow counter.
				 */
				// if (strt.isEmpty() || end.isEmpty()) {
				// System.out.println("empty String");
				// continue;
				// }
				if (Flow.containsKey(strt)) {
					/**
					 * If the flow contain the starting point of the current
					 * path, update the internal table with of the last point in
					 * the path after checking its existence.
					 */

					Hashtable<String, Double> tmp = Flow.get(strt);

					/**
					 * Check if the internal table contain the last point or
					 * not.
					 */
					if (tmp.containsKey(end)) {
						double val = tmp.get(end) + 1;
						tmp.replace(end, val);
					} else {
						tmp.put(end, 1.0);
					}

					Flow.replace(strt, tmp);
				} else {
					/**
					 * If this path not available in the flow path add it and
					 * its internal table.
					 */
					Hashtable<String, Double> tmp = new Hashtable<>();
					tmp.put(end, 1.0);
					Flow.put(strt, tmp);
				}

				// ----------------start{wrong_part}-------------------------
				/**
				 * Update Ins.
				 */
				if (Outs.containsKey(strt)) {
					double val = Outs.get(strt) + 1;
					Outs.put(strt, val);
				} else {
					Outs.put(strt, 1.0);
				}
				/**
				 * Update Outs.
				 */
				if (Ins.containsKey(end)) {
					double val = Ins.get(end) + 1;
					Ins.put(end, val);
				} else {
					Ins.put(end, 1.0);
				}
			}
		}
		mange_zones_flow(nodesMap);

		return Flow;
	}

	/**
	 * Find the flow of traffic over edges after that sum in and out over
	 * individual zones.
	 *
	 * @param obs
	 *            sequence of calculated trajectories
	 * @param map
	 *            network to identify edge's zone
	 * @param L
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handle_zones_Flow(ArrayList<String> obs,
			Hashtable<String, Integer> nodesMap, int L) {
		for (Iterator<String> iterator = obs.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			/**
			 * Remove interpolation with regex.
			 */
			// path = path.replaceAll(regex, "");
			/**
			 * Check path if no path calculated from viterbi continue
			 */
			if (path.isEmpty()) {
				continue;
			}

			/**
			 * Create path sequences for observations that related to sub-zone
			 * of the country.
			 */
			String[] seqs = new String[] { path };
			;
			if (path.contains(RLM)) {
				seqs = path.split(RLM);
			}

			for (int i = 0; i < seqs.length; i++) {
				String[] pnts = seqs[i].split(CLM);
				if (pnts.length <= L) {
					continue;
				}
				String strt = pnts[0];
				String end = pnts[pnts.length - 1];

				// if (strt.equals(end)) {
				// continue;
				// }
				/**
				 * Update flow counter.
				 */
				// if (strt.isEmpty() || end.isEmpty()) {
				// System.out.println("empty String");
				// continue;
				// }
				if (Flow.containsKey(strt)) {
					/**
					 * If the flow contain the starting point of the current
					 * path, update the internal table with of the last point in
					 * the path after checking its existence.
					 */

					Hashtable<String, Double> tmp = Flow.get(strt);

					/**
					 * Check if the internal table contain the last point or
					 * not.
					 */
					if (tmp.containsKey(end)) {
						double val = tmp.get(end) + 1;
						tmp.replace(end, val);
					} else {
						tmp.put(end, 1.0);
					}

					Flow.replace(strt, tmp);
				} else {
					/**
					 * If this path not available in the flow path add it and
					 * its internal table.
					 */
					Hashtable<String, Double> tmp = new Hashtable<>();
					tmp.put(end, 1.0);
					Flow.put(strt, tmp);
				}

				// ----------------start{wrong_part}-------------------------
				/**
				 * Update Ins.
				 */
				if (Outs.containsKey(strt)) {
					double val = Outs.get(strt) + 1;
					Outs.put(strt, val);
				} else {
					Outs.put(strt, 1.0);
				}
				/**
				 * Update Outs.
				 */
				if (Ins.containsKey(end)) {
					double val = Ins.get(end) + 1;
					Ins.put(end, val);
				} else {
					Ins.put(end, 1.0);
				}
				// ----------------end{wrong_part}---------------------------
				/**
				 * Tue 05 May 2015 07:03:30 PM JST
				 *
				 * Re-implement the IN/OUT part to be populated with all
				 * internal edges as well as the OD.
				 *
				 */
				// for (int j = 0; j < pnts.length - 1; j++) {
				// strt = pnts[j];
				// end = pnts[j + 1];
				//
				//// if (strt.equals(end)) {
				//// continue;
				//// }
				// /**
				// * Update Ins.
				// */
				// if (Outs.containsKey(strt)) {
				// double val = Outs.get(strt) + 1;
				// Outs.put(strt, val);
				// } else {
				// Outs.put(strt, 1.0);
				// }
				// /**
				// * Update Outs.
				// */
				// if (Ins.containsKey(end)) {
				// double val = Ins.get(end) + 1;
				// Ins.put(end, val);
				// } else {
				// Ins.put(end, 1.0);
				// }
				//
				// }

			}

		}

		mange_zones_flow(nodesMap);

		return Flow;
	}

	/**
	 * Find the flow of traffic over edges after that sum in and out over
	 * individual zones.
	 *
	 * @param obs
	 *            sequence of calculated trajectories
	 * @param map
	 *            network to identify edge's zone
	 * @param L
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handle_zones_Flow(
			Hashtable<String, Hashtable<Integer, Obs>> obs) {
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				String path = inEntry.getValue().getVitPath();

				/**
				 * Remove interpolation with regex.
				 */
				// path = path.replaceAll(regex, "");
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty()) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				;
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
				}

				for (int i = 0; i < seqs.length; i++) {
					if (seqs[i].equals("-")) {
						continue;
					}
					String[] pnts = seqs[i].split(CLM);
					// if (pnts.length <= L) {
					// continue;
					// }
					String strt = pnts[0];
					String end = pnts[pnts.length - 1];

					if (strt.equals(end)) {
						continue;
					}
					/**
					 * Update flow counter.
					 */
					// if (strt.isEmpty() || end.isEmpty()) {
					// System.out.println("empty String");
					// continue;
					// }
					if (Flow.containsKey(strt)) {
						/**
						 * If the flow contain the starting point of the current
						 * path, update the internal table with of the last
						 * point in the path after checking its existence.
						 */

						Hashtable<String, Double> tmp = Flow.get(strt);

						/**
						 * Check if the internal table contain the last point or
						 * not.
						 */
						if (tmp.containsKey(end)) {
							double val = tmp.get(end) + 1;
							tmp.replace(end, val);
						} else {
							tmp.put(end, 1.0);
						}

						Flow.replace(strt, tmp);
					} else {
						/**
						 * If this path not available in the flow path add it
						 * and its internal table.
						 */
						Hashtable<String, Double> tmp = new Hashtable<>();
						tmp.put(end, 1.0);
						Flow.put(strt, tmp);
					}

					/**
					 * Update Ins.
					 */
					if (Outs.containsKey(strt)) {
						double val = Outs.get(strt) + 1;
						Outs.put(strt, val);
					} else {
						Outs.put(strt, 1.0);
					}
					/**
					 * Update Outs.
					 */
					if (Ins.containsKey(end)) {
						double val = Ins.get(end) + 1;
						Ins.put(end, val);
					} else {
						Ins.put(end, 1.0);
					}

				}

			}

		}

		return Flow;
	}

	/**
	 * Find the flow of traffic over edges after that sum in and out over
	 * individual zones.
	 *
	 * @param obs
	 *            sequence of calculated trajectories
	 * @param map
	 *            network to identify edge's zone
	 * @param L
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handle_zones_Flow(List<String[]> obs) {
		for (Iterator<String[]> iterator = obs.iterator(); iterator.hasNext();) {
			String[] pnts = iterator.next();
			// if (pnts.length <= L) {
			// continue;
			// }
			String strt = pnts[0];
			String end = pnts[pnts.length - 1];

			if (strt.equals(end)) {
				continue;
			}
			/**
			 * Update flow counter.
			 */
			// if (strt.isEmpty() || end.isEmpty()) {
			// System.out.println("empty String");
			// continue;
			// }
			if (Flow.containsKey(strt)) {
				/**
				 * If the flow contain the starting point of the current path,
				 * update the internal table with of the last point in the path
				 * after checking its existence.
				 */

				Hashtable<String, Double> tmp = Flow.get(strt);

				/**
				 * Check if the internal table contain the last point or not.
				 */
				if (tmp.containsKey(end)) {
					double val = tmp.get(end) + 1;
					tmp.replace(end, val);
				} else {
					tmp.put(end, 1.0);
				}

				Flow.replace(strt, tmp);
				// } else if (Flow.containsKey(end)) {
				// /**
				// * If the flow contain the starting point of the current path,
				// * update the internal table with of the last point in the
				// path
				// * after checking its existence.
				// */
				//
				// Hashtable<String, Double> tmp = Flow.get(end);
				//
				// /**
				// * Check if the internal table contain the last point or not.
				// */
				// if (tmp.containsKey(strt)) {
				// double val = tmp.get(strt) + 1;
				// tmp.replace(strt, val);
				// } else {
				// tmp.put(strt, 1.0);
				// }
				//
				// Flow.replace(end, tmp);
			} else {
				/**
				 * If this path not available in the flow path add it and its
				 * internal table.
				 */
				Hashtable<String, Double> tmp = new Hashtable<>();
				tmp.put(end, 1.0);
				Flow.put(strt, tmp);
			}

			/**
			 * Update Ins.
			 */
			if (Outs.containsKey(strt)) {
				double val = Outs.get(strt) + 1;
				Outs.put(strt, val);
			} else {
				Outs.put(strt, 1.0);
			}
			/**
			 * Update Outs.
			 */
			if (Ins.containsKey(end)) {
				double val = Ins.get(end) + 1;
				Ins.put(end, val);
			} else {
				Ins.put(end, 1.0);
			}

		}

		return Flow;
	}

	/**
	 * Sun Jan 3 20:34:47 EET 2016
	 *
	 * Find the flow of traffic over edges after that sum in and out over
	 * individual zones.
	 *
	 * @param obs
	 *            sequence of calculated trajectories
	 * @param map
	 *            network to identify edge's zone
	 * @param L
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handle_zones_Flow_1(List<String[]> obs) {
		for (Iterator<String[]> iterator = obs.iterator(); iterator.hasNext();) {
			String[] pnts = iterator.next();
			// if (pnts.length <= L) {
			// continue;
			// }
			String strt = pnts[0];
			String end = pnts[pnts.length - 1];

			if (strt.equals(end)) {
				continue;
			}
			/**
			 * Update flow counter.
			 */
			if (Flow.containsKey(strt)) {
				/**
				 * If the flow contain the starting point of the current path,
				 * update the internal table with of the last point in the path
				 * after checking its existence.
				 */

				Hashtable<String, Double> tmp = Flow.get(strt);

				/**
				 * Check if the internal table contain the last point or not.
				 */
				if (tmp.containsKey(end)) {
					double val = tmp.get(end) + 1;
					tmp.replace(end, val);
				} else {
					tmp.put(end, 1.0);
				}

				Flow.replace(strt, tmp);
			} else {
				/**
				 * If this path not available in the flow path add it and its
				 * internal table.
				 */
				Hashtable<String, Double> tmp = new Hashtable<>();
				tmp.put(end, 1.0);
				Flow.put(strt, tmp);
			}

			for (int i = 0; i < pnts.length - 1; i++) {
				strt = pnts[i];
				end = pnts[i + 1];
				/**
				 * Update Ins.
				 */
				if (Outs.containsKey(strt)) {
					double val = Outs.get(strt) + 1;
					Outs.put(strt, val);
				} else {
					Outs.put(strt, 1.0);
				}
				/**
				 * Update Outs.
				 */
				if (Ins.containsKey(end)) {
					double val = Ins.get(end) + 1;
					Ins.put(end, val);
				} else {
					Ins.put(end, 1.0);
				}
			}

		}

		return Flow;
	}

	public Hashtable<String, Hashtable<String, Double>> handleFlow(ArrayList<String> routes) {
		/**
		 * Towers rang delimiter.
		 */
		final String rlm = "/";

		for (Iterator<String> iterator = routes.iterator(); iterator.hasNext();) {
			String path = iterator.next();

			/**
			 * Check path if no path calculated from viterbi continue
			 */
			if (path.isEmpty()) {
				continue;
			}

			/**
			 * Create path sequences for observations that related to sub-zone
			 * of the country.
			 */
			String[] seqs = new String[] { path };
			;
			if (path.contains(rlm)) {
				seqs = path.split(rlm);
			}

			for (int i = 0; i < seqs.length; i++) {
				String[] pnts = seqs[i].split(",");
				String strt = pnts[0];
				String end = pnts[pnts.length - 1];
				/**
				 * Update flow counter.
				 */
				if (strt.isEmpty() || end.isEmpty()) {
					System.out.println("empty String");
					continue;
				}
				if (Flow.containsKey(strt)) {
					/**
					 * If the flow contain the starting point of the current
					 * path, update the internal table with of the last point in
					 * the path after checking its existence.
					 */

					Hashtable<String, Double> tmp = Flow.get(strt);

					/**
					 * Check if the internal table contain the last point or
					 * not.
					 */
					if (tmp.containsKey(end)) {
						double val = tmp.get(end) + 1;
						tmp.replace(end, val);
					} else {
						tmp.put(end, 1.0);
					}

					Flow.replace(strt, tmp);
				} else {
					/**
					 * If this path not available in the flow path add it and
					 * its internal table.
					 */
					Hashtable<String, Double> tmp = new Hashtable<>();
					tmp.put(end, 1.0);
					Flow.put(strt, tmp);
				}

				/**
				 * Update Ins.
				 */
				if (Ins.containsKey(strt)) {
					double val = Ins.get(strt) + 1;
					Ins.put(strt, val);
				} else {
					Ins.put(strt, 1.0);
				}
				/**
				 * Update Outs.
				 */
				if (Outs.containsKey(end)) {
					double val = Outs.get(end) + 1;
					Outs.put(end, val);
				} else {
					Outs.put(end, 1.0);
				}
			}

		}
		return Flow;
	}

	/**
	 * Handle flow from the observations
	 *
	 * Tue 05 May 2015 06:25:45 PM JST
	 *
	 * It seems that this implementation is incorrect somehow. AllAboard paper,
	 * In particular, all the antennas are considered as either origin or
	 * destination, and the time is divided into 24 hourly intervals. This mean
	 * that all edges have to be considered as O or D and flow calculated only
	 * with specific state and end of users trips.
	 *
	 *
	 * So IN and OUT array must contain count of all edges including the transit
	 * states not only the start and end of users trips.
	 *
	 * @param obs
	 * @param L
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handleFlow(Hashtable<String, Hashtable<Integer, Obs>> obs) {
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				String path = inEntry.getValue().getVitPath();

				/**
				 * Remove interpolation with regex.
				 */
				path = path.replaceAll(regex, "");
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty()) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				;
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
				}

				for (int i = 0; i < seqs.length; i++) {
					String[] pnts = seqs[i].split(CLM);
					// if (pnts.length <= L) {
					// continue;
					// }
					String strt = pnts[0];
					String end = pnts[pnts.length - 1];

					if (strt.equals(end)) {
						continue;
					}
					/**
					 * Update flow counter.
					 */
					// if (strt.isEmpty() || end.isEmpty()) {
					// System.out.println("empty String");
					// continue;
					// }
					if (Flow.containsKey(strt)) {
						/**
						 * If the flow contain the starting point of the current
						 * path, update the internal table with of the last
						 * point in the path after checking its existence.
						 */

						Hashtable<String, Double> tmp = Flow.get(strt);

						/**
						 * Check if the internal table contain the last point or
						 * not.
						 */
						if (tmp.containsKey(end)) {
							double val = tmp.get(end) + 1;
							tmp.replace(end, val);
						} else {
							tmp.put(end, 1.0);
						}

						Flow.replace(strt, tmp);
					} else {
						/**
						 * If this path not available in the flow path add it
						 * and its internal table.
						 */
						Hashtable<String, Double> tmp = new Hashtable<>();
						tmp.put(end, 1.0);
						Flow.put(strt, tmp);
					}

					// ----------------start{wrong_part}-------------------------
					/**
					 * Update Ins.
					 */
					if (Outs.containsKey(strt)) {
						double val = Outs.get(strt) + 1;
						Outs.put(strt, val);
					} else {
						Outs.put(strt, 1.0);
					}
					/**
					 * Update Outs.
					 */
					if (Ins.containsKey(end)) {
						double val = Ins.get(end) + 1;
						Ins.put(end, val);
					} else {
						Ins.put(end, 1.0);
					}
					// ----------------end{wrong_part}---------------------------
					/**
					 * Tue 05 May 2015 07:03:30 PM JST
					 *
					 * Re-implement the IN/OUT part to be populated with all
					 * internal edges as well as the OD.
					 *
					 */
					// for (int j = 0; j < pnts.length - 1; j++) {
					// strt = pnts[j];
					// end = pnts[j + 1];
					//
					//// if (strt.equals(end)) {
					//// continue;
					//// }
					// /**
					// * Update Ins.
					// */
					// if (Outs.containsKey(strt)) {
					// double val = Outs.get(strt) + 1;
					// Outs.put(strt, val);
					// } else {
					// Outs.put(strt, 1.0);
					// }
					// /**
					// * Update Outs.
					// */
					// if (Ins.containsKey(end)) {
					// double val = Ins.get(end) + 1;
					// Ins.put(end, val);
					// } else {
					// Ins.put(end, 1.0);
					// }
					//
					// }

				}

			}

		}
		// Ins = new Hashtable<>(remove_IO_interploation(Ins));
		// Outs = new Hashtable<>(remove_IO_interploation(Outs));
		// remove_flow_interploation();
		return Flow;
	}

	public void handleHFlow(ArrayList<String> obs, int L) {
		for (Iterator<String> iterator = obs.iterator(); iterator.hasNext();) {
			String path = iterator.next();
			/**
			 * Remove interpolation with regex.
			 */
			path = path.replaceAll(regex, "");
			/**
			 * Check path if no path calculated from viterbi continue
			 */
			if (path.isEmpty()) {
				continue;
			}

			/**
			 * Create path sequences for observations that related to sub-zone
			 * of the country.
			 */
			String[] seqs = new String[] { path };
			;
			if (path.contains(RLM)) {
				seqs = path.split(RLM);
			}

			for (int i = 0; i < seqs.length; i++) {
				String[] pnts = seqs[i].split(CLM);
				// if (pnts.length <= L) {
				// continue;
				// }
				String strt = pnts[0];
				String end = pnts[pnts.length - 1];

				// if (strt.equals(end)) {
				// continue;
				// }
				/**
				 * Update flow counter.
				 */
				// if (strt.isEmpty() || end.isEmpty()) {
				// System.out.println("empty String");
				// continue;
				// }
				if (Flow.containsKey(strt)) {
					/**
					 * If the flow contain the starting point of the current
					 * path, update the internal table with of the last point in
					 * the path after checking its existence.
					 */

					Hashtable<String, Double> tmp = Flow.get(strt);

					/**
					 * Check if the internal table contain the last point or
					 * not.
					 */
					if (tmp.containsKey(end)) {
						double val = tmp.get(end) + 1;
						tmp.replace(end, val);
					} else {
						tmp.put(end, 1.0);
					}

					Flow.replace(strt, tmp);
				} else {
					/**
					 * If this path not available in the flow path add it and
					 * its internal table.
					 */
					Hashtable<String, Double> tmp = new Hashtable<>();
					tmp.put(end, 1.0);
					Flow.put(strt, tmp);
				}

				// ----------------start{wrong_part}-------------------------
				/**
				 * Update Ins.
				 */
				if (Outs.containsKey(strt)) {
					double val = Outs.get(strt) + 1;
					Outs.put(strt, val);
				} else {
					Outs.put(strt, 1.0);
				}
				/**
				 * Update Outs.
				 */
				if (Ins.containsKey(end)) {
					double val = Ins.get(end) + 1;
					Ins.put(end, val);
				} else {
					Ins.put(end, 1.0);
				}
				// ----------------end{wrong_part}---------------------------
				/**
				 * Tue 05 May 2015 07:03:30 PM JST
				 *
				 * Re-implement the IN/OUT part to be populated with all
				 * internal edges as well as the OD.
				 *
				 */
				// for (int j = 0; j < pnts.length - 1; j++) {
				// strt = pnts[j];
				// end = pnts[j + 1];
				//
				//// if (strt.equals(end)) {
				//// continue;
				//// }
				// /**
				// * Update Ins.
				// */
				// if (Outs.containsKey(strt)) {
				// double val = Outs.get(strt) + 1;
				// Outs.put(strt, val);
				// } else {
				// Outs.put(strt, 1.0);
				// }
				// /**
				// * Update Outs.
				// */
				// if (Ins.containsKey(end)) {
				// double val = Ins.get(end) + 1;
				// Ins.put(end, val);
				// } else {
				// Ins.put(end, 1.0);
				// }
				//
				// }

			}

		}

	}

	/**
	 * find Zones Flows, Ins and Outs instead of using edges.
	 *
	 * @param nodesMap
	 */
	public void mange_zones_flow(Hashtable<String, Integer> nodesMap) {

		/**
		 * handle flow
		 */
		Hashtable<String, Hashtable<String, Double>> tmp_flow = new Hashtable<>();

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {

			String from_zone = null;
			try {
				from_zone = Integer.toString(nodesMap.get(entrySet.getKey()));
			} catch (Exception e) {
				System.out.println("entrySet.getKey()\t" + entrySet.getKey());
				System.out.println("nodesMap contains\t" + nodesMap.containsKey(entrySet.getKey()));
			}
			Hashtable<String, Double> value = entrySet.getValue();
			Hashtable<String, Double> tmp_to_zone_flow;

			if (tmp_flow.containsKey(from_zone)) {
				tmp_to_zone_flow = tmp_flow.get(from_zone);
			} else {
				tmp_to_zone_flow = new Hashtable<>();
			}
			/**
			 * Convert edges count into zones.
			 */
			for (Map.Entry<String, Double> to_entrySet : value.entrySet()) {
				String to_zone = Integer.toString(nodesMap.get(to_entrySet.getKey()));
				Double cnt = to_entrySet.getValue();
				if (tmp_to_zone_flow.containsKey(to_zone)) {
					tmp_to_zone_flow.replace(to_zone, tmp_to_zone_flow.get(to_zone) + cnt);
				} else {
					tmp_to_zone_flow.put(to_zone, cnt);
				}

			}
			/**
			 * Update tmp_flow records.
			 */
			if (tmp_flow.containsKey(from_zone)) {
				tmp_flow.replace(from_zone, tmp_to_zone_flow);
			} else {
				tmp_flow.put(from_zone, tmp_to_zone_flow);
			}
		}
		/**
		 * Update flow with zone counts.
		 */
		Flow = new Hashtable<>(tmp_flow);

		/**
		 * handle Ins
		 */
		Hashtable<String, Double> tmp_ins = new Hashtable<>();
		for (Map.Entry<String, Double> entrySet : Ins.entrySet()) {
			String zone_key = Integer.toString(nodesMap.get(entrySet.getKey()));
			double cnt = entrySet.getValue();

			if (tmp_ins.containsKey(zone_key)) {
				tmp_ins.replace(zone_key, tmp_ins.get(zone_key) + cnt);
			} else {
				tmp_ins.put(zone_key, cnt);
			}
		}
		Ins = new Hashtable<>(tmp_ins);
		/**
		 * Handle Outs
		 */
		Hashtable<String, Double> tmp_outs = new Hashtable<>();
		for (Map.Entry<String, Double> entrySet : Outs.entrySet()) {
			String zone_key = Integer.toString(nodesMap.get(entrySet.getKey()));
			double cnt = entrySet.getValue();

			if (tmp_outs.containsKey(zone_key)) {
				tmp_outs.replace(zone_key, tmp_outs.get(zone_key) + cnt);
			} else {
				tmp_outs.put(zone_key, cnt);
			}
		}
		Outs = new Hashtable<>(tmp_outs);
	}

	/**
	 * Remove interpolation from all of the FLOW, INS, and OUTS table to be
	 * compared and calculated based on the initial traffic map.
	 */
	private void remove_flow_interploation() {
		/**
		 * Update the flow table.
		 */
		Hashtable<String, Hashtable<String, Double>> nflow = new Hashtable<>();

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
			String key = handle_edge(entrySet.getKey());
			Hashtable<String, Double> value = entrySet.getValue();
			Hashtable<String, Double> nvalue = new Hashtable<>();
			for (Map.Entry<String, Double> toEntrySet : value.entrySet()) {
				String to_key = handle_edge(toEntrySet.getKey());
				double cnt = toEntrySet.getValue();

				if (nvalue.containsKey(to_key)) {
					nvalue.replace(to_key, nvalue.get(to_key) + cnt);
				} else {
					nvalue.put(to_key, cnt);
				}

			}
			if (nflow.containsKey(key)) {
				Hashtable<String, Double> tmp_nvalue = nflow.get(key);
				for (Map.Entry<String, Double> ToEntrySet : tmp_nvalue.entrySet()) {
					String to_key = ToEntrySet.getKey();
					Double cnt = ToEntrySet.getValue();
					if (nvalue.containsKey(to_key)) {
						nvalue.replace(to_key, nvalue.get(to_key) + cnt);
					} else {
						nvalue.put(to_key, cnt);
					}

				}
			}
			nflow.put(key, nvalue);
		}
		/**
		 * update the global variable.
		 */
		Flow = new Hashtable<>(nflow);

	}

	/**
	 * Remove interpolation from both Ins and Outs
	 *
	 * @param IO
	 * @return
	 */
	private Hashtable<String, Double> remove_IO_interploation(Hashtable<String, Double> IO) {
		Hashtable<String, Double> nIO = new Hashtable<>();

		for (Map.Entry<String, Double> entrySet : IO.entrySet()) {
			String key = handle_edge(entrySet.getKey());
			double cnt = entrySet.getValue();
			if (nIO.containsKey(key)) {
				nIO.replace(key, nIO.get(key) + cnt);
			} else {
				nIO.put(key, cnt);
			}

		}
		return nIO;
	}

	public boolean reset() {
		try {
			Flow.clear();
			Outs.clear();
			Ins.clear();
			distances.clear();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Round up values ...
	 *
	 * @param i
	 * @param v
	 * @return
	 */
	private double round(double i, int v) {
		return Math.round(i / v) * v;
	}

	public void setDistances(Hashtable<String, Hashtable<String, Double>> distances) {
		this.distances = new Hashtable<>(distances);
	}

	/**
	 *
	 * @param path
	 * @param fgod
	 */
	public void writeAvgGOD(String path, Hashtable<Double, ArrayList<Double>> fgod) {
		try {

			File file = new File(path);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("flow, avg god,count, max, min, std, std_error\n");
			for (Map.Entry<Double, ArrayList<Double>> entrySet : fgod.entrySet()) {
				Double key = entrySet.getKey();
				ArrayList<Double> god = entrySet.getValue();
				bw.write(key + "," + god.get(0) + "," + god.get(1) + "," + god.get(2) + "," + god.get(3) + ","
						+ god.get(4) + "," + god.get(5));
				bw.newLine();

			}
			bw.close();
			// System.out.println("File Saved");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param path
	 * @param fgod
	 */
	public void writeFGOD(String path, Hashtable<Double, ArrayList<Double>> fgod) {
		try {

			File file = new File(path);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (Map.Entry<Double, ArrayList<Double>> entrySet : fgod.entrySet()) {
				Double key = entrySet.getKey();
				ArrayList<Double> value = entrySet.getValue();
				for (Iterator<Double> iterator = value.iterator(); iterator.hasNext();) {
					Double god = iterator.next();
					bw.write(key + "," + god);
					bw.newLine();
				}

			}
			bw.close();
			// System.out.println("File Saved");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write the flow informations.
	 *
	 * This functions must not call before these functions handleFlow, and
	 * calculate distance
	 *
	 * @param path
	 */
	public void writeFlow(String path) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("flow-defs");
			doc.appendChild(rootElement);
			NumberFormat formatter = new DecimalFormat("0.#######E0");
			for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
				String pKey = entrySet.getKey();
				Element fromNodeElement = doc.createElement("from");
				rootElement.appendChild(fromNodeElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(pKey);
				fromNodeElement.setAttributeNode(attr);

				attr = doc.createAttribute("ins");
				attr.setValue(String.valueOf(Ins.get(pKey)));
				fromNodeElement.setAttributeNode(attr);

				Hashtable<String, Double> value = entrySet.getValue();
				Hashtable<String, Double> toDist = distances.get(pKey);
				for (Map.Entry<String, Double> toEntrySet : value.entrySet()) {
					String toKey = toEntrySet.getKey();
					Double flow = toEntrySet.getValue();
					if (flow == 0) {
						continue;
					}
					Element toNodeElement = doc.createElement("to");
					fromNodeElement.appendChild(toNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(toKey);
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("flow");
					attr.setValue(String.valueOf(flow));
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("outs");
					attr.setValue(String.valueOf(Outs.get(toKey)));
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("distance");
					attr.setValue(String.valueOf(toDist.get(toKey)));
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

			// System.out.println("File saved!");
		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}
}
