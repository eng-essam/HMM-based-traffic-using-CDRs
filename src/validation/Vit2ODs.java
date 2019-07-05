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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import Observations.Obs;
import mergexml.MergeXML;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam Convert obtained viterbi into routes in order to be further
 *         converted into od
 */
public class Vit2ODs {

	final int HOUR = 60;
	/**
	 * Towers rang delimiter.
	 */
	final String RLM = "/";

	private ArrayList<String> unique_edges;

	private ArrayList<String> all_edges;

	public Hashtable<Integer, ArrayList<String>> generateNodesMap(ArrayList<FromNode> map) {
		Hashtable<Integer, ArrayList<String>> nodesMap = new Hashtable<>();

		for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
			FromNode next = iterator.next();
			int zone = next.getZone();
			String edge = next.getID();
			if (nodesMap.containsKey(zone)) {
				ArrayList<String> list = nodesMap.get(zone);
				list.add(edge);
				nodesMap.replace(zone, list);
			} else {
				ArrayList<String> list = new ArrayList<>();
				list.add(edge);
				;
				nodesMap.put(zone, list);
			}
		}
		return nodesMap;
	}

	public ArrayList<String> get_edges(Hashtable<String, Hashtable<Integer, Obs>> obs) {
		try {
			hFWDepart_interpolation(obs);
		} catch (ParseException parseException) {
			System.err.println("Error in parsing edges");
		}
		return all_edges;
	}

	public String get_random_edge(ArrayList<String> list) {
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(list.size());
		return list.get(index);
	}

	public ArrayList<String> get_unique_edges(Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {
		hFWDepart_interpolation(obs);
		return unique_edges;
	}

	/**
	 * Handle viterbi flow and return it as the flow some point to another with
	 * the number of repetitions for trips between both of these two points
	 *
	 * @param obs
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> handleFlow(Hashtable<Integer, Hashtable<Integer, Obs>> obs) {
		Hashtable<String, Hashtable<String, Double>> Flow = new Hashtable<>(); // the
																				// GeoDistance
																				// Between
																				// Two
																				// Edges
		/**
		 * Towers rang delimiter.
		 */
		final String rlm = "/";

		for (Map.Entry<Integer, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			Integer key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				Integer key1 = inEntry.getKey();
				String path = inEntry.getValue().getVitPath();
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty() || path.equals("-")) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				;
				if (path.contains(rlm)) {
					seqs = path.split(rlm);
				}

				for (int i = 0; i < seqs.length; i++) {

					if (seqs[i].equals("-")) {
						continue;
					}

					String[] pnts = seqs[i].split(",");
					String strt = pnts[0];
					String end = pnts[pnts.length - 1];
					/**
					 * Update flow counter.
					 */
					if (strt.isEmpty() || end.isEmpty()) {
						// System.out.println("empty String");
						continue;
					}
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

				}

			}

		}
		return Flow;
	}

	/**
	 * Handle the traffic flow as it is, without removing the interpolations.
	 *
	 * @param obs
	 * @return
	 * @throws ParseException
	 */
	public Hashtable<String, Multimap<String, Calendar>> handleFlowWDepart(
			Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {
		Hashtable<String, Multimap<String, Calendar>> Flow = new Hashtable<>(); // the
																				// GeoDistance
																				// Between
																				// Two
																				// Edges

		SimpleDateFormat parserSDF = new SimpleDateFormat("HH:mm:ss");
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				Integer key1 = inEntry.getKey();
				String path = inEntry.getValue().getVitPath();
				String stamps = inEntry.getValue().getTimeStamp();
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty() || path.equals("-")) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				String[] tseqs = new String[] { stamps };
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
					tseqs = stamps.split(RLM);
				}

				for (int i = 0; i < seqs.length; i++) {

					if (seqs[i].equals("-")) {
						continue;
					}

					String[] pnts = seqs[i].split(",");
					String[] tpnts = tseqs[i].split(" ");

					String strt = pnts[0];
					String depart = tpnts[0];
					String end = pnts[pnts.length - 1];
					/**
					 * Update flow counter.
					 */
					if (strt.isEmpty() || end.isEmpty()) {
						// System.out.println("empty String");
						continue;
					}
					// if the start equals the end ignore it
					if (strt.equals(end)) {
						continue;
					}

					if (Flow.containsKey(strt)) {
						/**
						 * If the flow contain the starting point of the current
						 * path, update the internal table with of the last
						 * point in the path after checking its existence.
						 */

						Multimap<String, Calendar> tmp = Flow.get(strt);

						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);

						tmp.put(end, cal);

						Flow.replace(strt, tmp);
					} else {
						/**
						 * If this path not available in the flow path add it
						 * and its internal table.
						 */
						Multimap<String, Calendar> tmp = ArrayListMultimap.create();
						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);
						/**
						 * Add terminal and depart time
						 */
						tmp.put(end, cal);

						Flow.put(strt, tmp);
					}

				}

			}

		}
		return Flow;
	}

	/**
	 * Generate OD from viterbi paths with ignoring loop around trips.
	 *
	 * @param obs
	 * @return
	 * @throws ParseException
	 */
	public Hashtable<String, Multimap<String, Calendar>> handleIMFlowWDepart(
			Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {
		Hashtable<String, Multimap<String, Calendar>> Flow = new Hashtable<>(); // the
																				// GeoDistance
																				// Between
																				// Two
																				// Edges

		SimpleDateFormat parserSDF = new SimpleDateFormat("HH:mm:ss");
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				Integer key1 = inEntry.getKey();
				String path = inEntry.getValue().getVitPath();
				String stamps = inEntry.getValue().getTimeStamp();
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty() || path.equals("-")) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				String[] tseqs = new String[] { stamps };
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
					tseqs = stamps.split(RLM);
				}
				outer: for (int i = 0; i < seqs.length; i++) {

					if (seqs[i].equals("-")) {
						continue;
					}

					String[] pnts = seqs[i].split(",");
					String[] tpnts = tseqs[i].split(" ");

					/**
					 * check start and end of trips per single path. If trips
					 * contain loop a round ignore it.
					 *
					 * e.g. < 5 7 8 5 6 4 6 3 2 1 6 5 7
					 * |-----|-----|-------|-----| t1 t2 t3 t4 />
					 *
					 */
					ArrayList<String> subseq = new ArrayList<>();
					for (int j = 0; j < pnts.length; j++) {
						String strt = pnts[j];
						if (subseq.contains(strt) && subseq.size() > 1) {
							continue outer;
						}

					}

					String strt = pnts[0];
					String depart = tpnts[0];
					String end = pnts[pnts.length - 1];

					if (strt.equals(end)) {
						continue;
					}

					if (Flow.containsKey(strt)) {
						/**
						 * If the flow contain the starting point of the current
						 * path, update the internal table with of the last
						 * point in the path after checking its existence.
						 */

						Multimap<String, Calendar> tmp = Flow.get(strt);

						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);
						tmp.put(end, cal);

						Flow.replace(strt, tmp);
					} else {
						/**
						 * If this path not available in the flow path add it
						 * and its internal table.
						 */
						Multimap<String, Calendar> tmp = ArrayListMultimap.create();
						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);
						/**
						 * Add terminal and depart time
						 */
						tmp.put(end, cal);

						Flow.put(strt, tmp);
					}

				}

			}

		}
		return Flow;
	}

	/**
	 * Generate OD from viterbi paths with multiple OD per single path, if the
	 * paths starts and end at the same point.
	 *
	 * @param obs
	 * @return
	 * @throws ParseException
	 */
	public Hashtable<String, Multimap<String, Calendar>> handleMFlowWDepart(
			Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {
		Hashtable<String, Multimap<String, Calendar>> Flow = new Hashtable<>(); // the
																				// GeoDistance
																				// Between
																				// Two
																				// Edges

		SimpleDateFormat parserSDF = new SimpleDateFormat("HH:mm:ss");
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				Integer key1 = inEntry.getKey();
				String path = inEntry.getValue().getVitPath();
				String stamps = inEntry.getValue().getTimeStamp();
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty() || path.equals("-")) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				String[] tseqs = new String[] { stamps };
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
					tseqs = stamps.split(RLM);
				}

				for (int i = 0; i < seqs.length; i++) {

					if (seqs[i].equals("-")) {
						continue;
					}

					String[] pnts = seqs[i].split(",");
					String[] tpnts = tseqs[i].split(" ");

					/**
					 * check start and end of trips per single path.
					 *
					 * e.g. < 5 7 8 5 6 4 6 3 2 1 6 5 7
					 * |-----|-----|-------|-----| t1 t2 t3 t4 />
					 *
					 */
					ArrayList<String> subseq = new ArrayList<>();
					boolean[] marks = new boolean[pnts.length];
					marks[0] = true;
					for (int j = 0; j < pnts.length; j++) {
						String strt = pnts[j];
						if (subseq.contains(strt) && subseq.size() > 1) {
							/**
							 * mark the current point as the start of the new OD
							 */
							System.out.println("new OD\t" + subseq.size());
							marks[j] = true;
							subseq = new ArrayList<>();
						} else {
							subseq.add(strt);
						}

					}

					String strt = pnts[0];
					String depart = tpnts[0];
					String end = pnts[marks.length - 1];
					for (int j = 1; j < marks.length; j++) {
						/**
						 * Check the whole seq contain only one OD
						 */
						if (j < marks.length - 1) {
							if (marks[j]) {
								end = pnts[j - 1];
							} else {
								continue;
							}
						} else {
							end = pnts[marks.length - 1];
						}
						if (end.equals(strt)) {
							continue;
						}
						if (Flow.containsKey(strt)) {
							/**
							 * If the flow contain the starting point of the
							 * current path, update the internal table with of
							 * the last point in the path after checking its
							 * existence.
							 */

							Multimap<String, Calendar> tmp = Flow.get(strt);

							Date departTime = parserSDF.parse(depart);
							Calendar cal = Calendar.getInstance();
							cal.setTime(departTime);
							tmp.put(end, cal);

							Flow.replace(strt, tmp);
						} else {
							/**
							 * If this path not available in the flow path add
							 * it and its internal table.
							 */
							Multimap<String, Calendar> tmp = ArrayListMultimap.create();
							Date departTime = parserSDF.parse(depart);
							Calendar cal = Calendar.getInstance();
							cal.setTime(departTime);
							/**
							 * Add terminal and depart time
							 */
							tmp.put(end, cal);

							Flow.put(strt, tmp);
						}
						strt = pnts[j];
						depart = tpnts[j];

					}

				}

			}

		}
		return Flow;
	}

	/**
	 * Thu 23 Apr 2015 01:06:49 PM JST
	 *
	 * Extract the OD of different trips and remove the interpolation segments.
	 *
	 * @param obs
	 * @return
	 * @throws ParseException
	 */
	public Hashtable<String, Multimap<String, Calendar>> hFWDepart_interpolation(
			Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {
		unique_edges = new ArrayList<>();
		all_edges = new ArrayList<>();

		Hashtable<String, Multimap<String, Calendar>> Flow = new Hashtable<>(); // the
																				// GeoDistance
																				// Between
																				// Two
																				// Edges

		SimpleDateFormat parserSDF = new SimpleDateFormat("HH:mm:ss");
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				Integer key1 = inEntry.getKey();
				String path = inEntry.getValue().getVitPath();
				String stamps = inEntry.getValue().getTimeStamp();
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty() || path.equals("-")) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				String[] tseqs = new String[] { stamps };
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
					tseqs = stamps.split(RLM);
				}

				for (int i = 0; i < seqs.length; i++) {

					if (seqs[i].equals("-")) {
						continue;
					}

					String[] pnts = seqs[i].split(" ");
					String[] tpnts = tseqs[i].split(" ");

					/**
					 * Sun 03 May 2015 12:07:52 AM JST
					 *
					 * Add edges to the list
					 */
					for (int j = 0; j < pnts.length; j++) {
						String edge = pnts[j];
						if (edge.contains("_interpolated")) {
							edge = edge.substring(0, edge.indexOf("_interpolated"));
						}
						if (!all_edges.contains(edge)) {
							all_edges.add(edge);
						}

					}
					/**
					 * end of modifications
					 */
					String strt = pnts[0];
					if (pnts[0].contains("_interpolated")) {
						strt = pnts[0].substring(0, pnts[0].indexOf("_interpolated"));
					}

					String depart = tpnts[0];

					String end = pnts[pnts.length - 1];

					/**
					 * Add edges to be used in correlation with the equilibrium
					 * model.
					 */
					if (!unique_edges.contains(strt)) {
						unique_edges.add(strt);
					} else if (!unique_edges.contains(end)) {
						unique_edges.add(end);
					}

					if (pnts[pnts.length - 1].contains("_interpolated")) {
						// try {
						end = pnts[pnts.length - 1].substring(0, pnts[pnts.length - 1].indexOf("_interpolated"));
						// } catch (StringIndexOutOfBoundsException e) {
						// System.out.println("error:\t" + pnts[pnts.length -
						// 1]);
						// }
					}

					/**
					 * Update flow counter.
					 */
					if (strt.isEmpty() || end.isEmpty()) {
						// System.out.println("empty String");
						continue;
					}
					// if the start equals the end ignore it
					if (strt.equals(end)) {
						continue;
					}

					if (Flow.containsKey(strt)) {
						/**
						 * If the flow contain the starting point of the current
						 * path, update the internal table with of the last
						 * point in the path after checking its existence.
						 */

						Multimap<String, Calendar> tmp = Flow.get(strt);

						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);

						tmp.put(end, cal);

						Flow.replace(strt, tmp);
					} else {
						/**
						 * If this path not available in the flow path add it
						 * and its internal table.
						 */
						Multimap<String, Calendar> tmp = ArrayListMultimap.create();
						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);
						/**
						 * Add terminal and depart time
						 */
						tmp.put(end, cal);

						Flow.put(strt, tmp);
					}

				}

			}

		}
		// System.out.println("Size of the unique edge:\t" +
		// unique_edges.size());

		return Flow;
	}

	/**
	 * Thu 23 Apr 2015 01:06:49 PM JST
	 *
	 * Extract the OD of different trips and remove the interpolation segments.
	 *
	 * @param obs
	 * @return
	 * @throws ParseException
	 */
	public Hashtable<String, Multimap<String, Calendar>> hFWDepart_zones_edges(
			Hashtable<String, Hashtable<Integer, Obs>> obs, Hashtable<Integer, ArrayList<String>> nodes)
			throws ParseException {
		unique_edges = new ArrayList<>();
		all_edges = new ArrayList<>();

		Hashtable<String, Multimap<String, Calendar>> Flow = new Hashtable<>(); // the
																				// GeoDistance
																				// Between
																				// Two
																				// Edges

		SimpleDateFormat parserSDF = new SimpleDateFormat("HH:mm:ss");
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> inEntry : value.entrySet()) {
				String path = inEntry.getValue().getVitPath();
				String stamps = inEntry.getValue().getTimeStamp();
				/**
				 * Check path if no path calculated from viterbi continue
				 */
				if (path.isEmpty() || path.equals("-")) {
					continue;
				}

				/**
				 * Create path sequences for observations that related to
				 * sub-zone of the country.
				 */
				String[] seqs = new String[] { path };
				String[] tseqs = new String[] { stamps };
				if (path.contains(RLM)) {
					seqs = path.split(RLM);
					tseqs = stamps.split(RLM);
				}

				for (int i = 0; i < seqs.length; i++) {

					if (seqs[i].equals("-")) {
						continue;
					}

					String[] pnts = seqs[i].split(",");
					String[] tpnts = tseqs[i].split(" ");
					if (!nodes.containsKey(Integer.parseInt(pnts[0]))
							|| !nodes.containsKey(Integer.parseInt(pnts[pnts.length - 1]))) {
						continue;
					}
					// System.out.println("-->" +
					// nodes.get(Integer.parseInt(pnts[0])) + "\t" + pnts[0]);
					String strt = get_random_edge(nodes.get(Integer.parseInt(pnts[0])));

					String depart = tpnts[0];
					// System.out.println("-->" +
					// nodes.get(Integer.parseInt(pnts[pnts.length - 1])) + "\t"
					// + pnts[pnts.length - 1]);
					String end = get_random_edge(nodes.get(Integer.parseInt(pnts[pnts.length - 1])));

					/**
					 * Add edges to be used in correlation with the equilibrium
					 * model.
					 */
					if (!unique_edges.contains(strt)) {
						unique_edges.add(strt);
					} else if (!unique_edges.contains(end)) {
						unique_edges.add(end);
					}

					/**
					 * Update flow counter.
					 */
					if (strt.isEmpty() || end.isEmpty()) {
						// System.out.println("empty String");
						continue;
					}
					// if the start equals the end ignore it
					if (strt.equals(end)) {
						continue;
					}

					if (Flow.containsKey(strt)) {
						/**
						 * If the flow contain the starting point of the current
						 * path, update the internal table with of the last
						 * point in the path after checking its existence.
						 */

						Multimap<String, Calendar> tmp = Flow.get(strt);

						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);

						tmp.put(end, cal);

						Flow.replace(strt, tmp);
					} else {
						/**
						 * If this path not available in the flow path add it
						 * and its internal table.
						 */
						Multimap<String, Calendar> tmp = ArrayListMultimap.create();
						Date departTime = parserSDF.parse(depart);
						Calendar cal = Calendar.getInstance();
						cal.setTime(departTime);
						/**
						 * Add terminal and depart time
						 */
						tmp.put(end, cal);

						Flow.put(strt, tmp);
					}

				}

			}

		}
		// System.out.println("Size of the unique edge:\t" +
		// unique_edges.size());

		return Flow;
	}

	/**
	 * Read user equilibrium routes
	 *
	 * @param routesPath
	 * @return
	 */
	public ArrayList<String> readRoutes(String routesPath) {
		ArrayList<String> routes = new ArrayList<>();

		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(routesPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList edgesList = dpDoc.getElementsByTagName("route");
			for (int i = 0; i < edgesList.getLength(); i++) {
				Node edgeNode = edgesList.item(i);
				if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) edgeNode;
					String route = eElement.getAttribute("edges").replace(DataHandler.SPACE_SEP, DataHandler.COMMA_SEP);
					// System.out.println(route);
					routes.add(route);
					// System.out.println(route);

				}
			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}

		return routes;
	}

	/**
	 * Write ODs in O-formate
	 *
	 * @param Flow
	 * @param path
	 * @param from
	 * @param to
	 */
	public void writeODs(Hashtable<String, Hashtable<String, Double>> Flow, String path, double from, double to) {

		File file = new File(path);
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			output.write("$OR;D2\n");
			output.write("* From-Time To-Time\n");
			output.write(from + "\t" + to + "\n");
			output.write("* Factor\n1.00\n");
			output.write("* some additional comments\n");
			for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
				String key = entrySet.getKey();
				Hashtable<String, Double> value = entrySet.getValue();
				for (Map.Entry<String, Double> tosEntry : value.entrySet()) {
					String toKey = tosEntry.getKey();
					Double count = tosEntry.getValue();

					output.write("\t\t\t" + key + "\t" + toKey + "\t" + String.valueOf(count));
					output.newLine();

				}

			}

		} catch (IOException ex) {
			Logger.getLogger(Vit2ODs.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	/**
	 * Write trips with random departure time
	 *
	 * @param Flow
	 * @param path
	 * @param from
	 * @param to
	 */
	public void writeTrips(Hashtable<String, Hashtable<String, Double>> Flow, String path, double from, double to) {
		/*
		 * <trips> <trip depart="0" from="beg" to="rend"/> <trip depart="0"
		 * from="beg" to="rend"/> </trips>
		 */

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("trips");
			doc.appendChild(rootElement);
			for (Map.Entry<String, Hashtable<String, Double>> entrySet : Flow.entrySet()) {
				String key = entrySet.getKey();
				Hashtable<String, Double> value = entrySet.getValue();
				for (Map.Entry<String, Double> tosEntry : value.entrySet()) {
					String toKey = tosEntry.getKey();
					Double count = tosEntry.getValue();
					for (int i = 0; i < count; i++) {
						Element tripNodeElement = doc.createElement("trip");
						rootElement.appendChild(tripNodeElement);

						// set attribute to staff element
						Attr attr = doc.createAttribute("depart");
						double depart = (int) (Math.random() * (to - from)) + from;
						attr.setValue(String.valueOf(depart));
						tripNodeElement.setAttributeNode(attr);

						attr = doc.createAttribute("from");
						attr.setValue(key);
						tripNodeElement.setAttributeNode(attr);

						attr = doc.createAttribute("to");
						attr.setValue(toKey);
						tripNodeElement.setAttributeNode(attr);

					}
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

	/**
	 * Write trips file with its time stamp as the departure time.
	 *
	 * @param Flow
	 * @param path
	 */
	public void writeTripsWDepart(Hashtable<String, Multimap<String, Calendar>> Flow, String path) {
		/*
		 * <trips> <trip depart="0" from="beg" to="rend"/> <trip depart="0"
		 * from="beg" to="rend"/> </trips>
		 */

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("trips");
			doc.appendChild(rootElement);
			for (Map.Entry<String, Multimap<String, Calendar>> entrySet : Flow.entrySet()) {
				String key = entrySet.getKey();
				Multimap<String, Calendar> value = entrySet.getValue();

				for (Map.Entry<String, Calendar> tosEntry : value.entries()) {
					String toKey = tosEntry.getKey();
					Calendar cal = tosEntry.getValue();
					// for (int i = 0; i < count; i++) {
					Element tripNodeElement = doc.createElement("trip");
					rootElement.appendChild(tripNodeElement);

					// set the departure time
					Attr attr = doc.createAttribute("depart");
					double depart = cal.get(Calendar.HOUR_OF_DAY) * HOUR * HOUR + cal.get(Calendar.MINUTE) * HOUR
							+ cal.get(Calendar.SECOND);
					attr.setValue(String.valueOf(depart));
					tripNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("from");
					attr.setValue(key);
					tripNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("to");
					attr.setValue(toKey);
					tripNodeElement.setAttributeNode(attr);

					// }
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

	/**
	 * Write trips file with its time stamp as the departure time.
	 *
	 * @param Flow
	 * @param path
	 */
	public void writeTripsWDepart_1(Hashtable<String, Multimap<String, Double>> Flow, String path) {
		/*
		 * <trips> <trip depart="0" from="beg" to="rend"/> <trip depart="0"
		 * from="beg" to="rend"/> </trips>
		 */

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("trips");
			doc.appendChild(rootElement);
			for (Map.Entry<String, Multimap<String, Double>> entrySet : Flow.entrySet()) {
				String key = entrySet.getKey();
				Multimap<String, Double> value = entrySet.getValue();

				for (Map.Entry<String, Double> tosEntry : value.entries()) {
					String toKey = tosEntry.getKey();
					double depart = tosEntry.getValue();
					// for (int i = 0; i < count; i++) {
					Element tripNodeElement = doc.createElement("trip");
					rootElement.appendChild(tripNodeElement);

					// set the departure time
					Attr attr = doc.createAttribute("depart");
					// double depart = cal.get(Calendar.HOUR_OF_DAY) * HOUR *
					// HOUR + cal.get(Calendar.MINUTE) * HOUR +
					// cal.get(Calendar.SECOND);
					attr.setValue(String.valueOf(depart));
					tripNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("from");
					attr.setValue(key);
					tripNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("to");
					attr.setValue(toKey);
					tripNodeElement.setAttributeNode(attr);

					// }
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
