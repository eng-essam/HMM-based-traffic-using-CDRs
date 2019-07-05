/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import Observations.Obs;
import mergexml.MergeXML;
import utils.Edge;
import utils.StdDraw;
import utils.ToNode;

/**
 *
 * @author essam
 */
public class Density {

	final static String RLM = "/";
	final static String CLM = ",";

	/**
	 * Read the density map for the counted vehicles.
	 *
	 * @param path
	 * @return Hashtable<Exit point id, Hashtable<Exit point id, Counted
	 *         vehicles>>
	 */
	public static Hashtable<String, Hashtable<String, Double>> readDensity(String path) {
		Hashtable<String, Hashtable<String, Double>> table = new Hashtable<>();
		org.w3c.dom.Document dpDoc;
		// for (int zone = 1; zone < 300; zone++) {
		// System.out.println("zone:\t"+zone);
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
					String fromNode = eElement.getAttribute("id");

					ToNode edge;
					NodeList toNodeList = eElement.getElementsByTagName("to");
					Hashtable<String, Double> toTable = new Hashtable<>();
					for (int j = 0; j < toNodeList.getLength(); j++) {
						Node toNodeNode = toNodeList.item(j);
						if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
							Element toNodeElement = (Element) toNodeNode;
							toTable.put(toNodeElement.getAttribute("id"),
									Double.parseDouble(toNodeElement.getAttribute("density")));
						}
					}
					table.put(fromNode, toTable);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}

		return table;
	}

	public static void writeDensity(Hashtable<String, Hashtable<String, Double>> prob, String path) {
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
				// System.out.println(pKey);
				Hashtable<String, Double> value = entrySet.getValue();
				for (Map.Entry<String, Double> entrySet1 : value.entrySet()) {
					String zoneKey = entrySet1.getKey();
					Double probability = entrySet1.getValue();
					if (probability == 0) {
						continue;
					}
					Element toNodeElement = doc.createElement("to");
					fromNodeElement.appendChild(toNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(zoneKey);
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("density");
					attr.setValue(probability.toString());
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

	/**
	 * Colors gumets dimensions.
	 */
	final double gxmin = 228200.06;
	final double gxmax = 240000.96;

	final double gymin = 1620000.93;
	final double gymax = 1620800.30;
	/**
	 * Map dimensions
	 */
	final double xmin = 227761.06;
	final double xmax = 240728.96;

	final double ymin = 1619000.93;

	final double ymax = 1635128.30;

	double max = 0;

	/**
	 * Write the density map to XML file.
	 *
	 * @param density
	 *            which is Hashtable<exit id, Hashtable<exit id, counted
	 *            vehicles>> @param path
	 */
	public Density() {
	}

	public int count_time(ArrayList<String> tseq) {
		int count = 0;
		for (Iterator<String> iterator = tseq.iterator(); iterator.hasNext();) {
			String[] stamps = iterator.next().split(CLM);
			count += stamps.length - 1;

		}
		return count;
	}

	/**
	 *
	 * @param seq_hour
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> density(ArrayList<String> seq_hour) {
		Hashtable<String, Hashtable<String, Double>> densityTable = new Hashtable<>();
		for (int i = 0; i < seq_hour.size(); i++) {
			String[] pnts = seq_hour.get(i).split(CLM);
			// if (pnts.length==1) {
			// pnts = new String[]{pnts[0],pnts[0]};
			//// System.out.println("add self
			// transition\t"+pnts[0]+"\t"+pnts[1]);
			// }
			for (int j = 0; j < pnts.length - 1; j++) {

				String fromPnt = pnts[j];
				String toPnt = pnts[j + 1];

				/**
				 * Remove nodes that match in to and from for one transition
				 */
				// if (fromPnt.equals(toPnt)) {
				// continue;
				// }
				if (densityTable.containsKey(fromPnt)) {
					Hashtable<String, Double> tos = densityTable.get(fromPnt);
					if (tos.containsKey(toPnt)) {
						tos.replace(toPnt, (tos.get(toPnt).doubleValue() + 1));
					} else {
						tos.put(toPnt, 1.0);
					}
					densityTable.replace(fromPnt, tos);

				} else {
					Hashtable<String, Double> tos = new Hashtable<>();
					tos.put(toPnt, 1.0);
					densityTable.put(fromPnt, tos);
				}

			}

		}
		return densityTable;
	}

	/**
	 * get the density of the calculated viterbi paths.
	 *
	 * @param vit_out
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> density(Hashtable<Integer, Hashtable<Integer, Obs>> vit_out) {
		ArrayList<String> vitList = new ArrayList<>();
		for (Map.Entry<Integer, Hashtable<Integer, Obs>> entrySet : vit_out.entrySet()) {
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> tosEntry : value.entrySet()) {
				Obs obs = tosEntry.getValue();
				String vit = obs.getVitPath();
				String[] paths = vit.split(RLM);
				for (int i = 0; i < paths.length; i++) {
					String path = paths[i];
					if (path.isEmpty() || path.equals("-")) {
						continue;
					}
					vitList.add(path);
				}

			}

		}
		return density_val(vitList);
	}

	/**
	 * Does not allow self emissions, or just ignore it. It is useful in
	 * validation process.
	 *
	 * @param seq_hour
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> density_val(ArrayList<String> seq_hour) {
		Hashtable<String, Hashtable<String, Double>> densityTable = new Hashtable<>();
		for (int i = 0; i < seq_hour.size(); i++) {
			String[] pnts = seq_hour.get(i).split(CLM);
			// if (pnts.length==1) {
			// pnts = new String[]{pnts[0],pnts[0]};
			//// System.out.println("add self
			// transition\t"+pnts[0]+"\t"+pnts[1]);
			// }
			for (int j = 0; j < pnts.length - 1; j++) {

				String fromPnt = pnts[j];
				String toPnt = pnts[j + 1];

				/**
				 * Remove nodes that match in to and from for one transition
				 */
				// if (fromPnt.equals(toPnt)) {
				// continue;
				// }
				if (densityTable.containsKey(fromPnt)) {
					Hashtable<String, Double> tos = densityTable.get(fromPnt);
					if (tos.containsKey(toPnt)) {
						tos.replace(toPnt, (tos.get(toPnt).doubleValue() + 1));
					} else {
						tos.put(toPnt, 1.0);
					}
					densityTable.replace(fromPnt, tos);

				} else {
					Hashtable<String, Double> tos = new Hashtable<>();
					tos.put(toPnt, 1.0);
					densityTable.put(fromPnt, tos);
				}

			}

		}
		return densityTable;
	}

	public double get_hourly_calls(Calendar fromH, Calendar toH, Hashtable<String, Hashtable<Integer, Obs>> obs)
			throws ParseException {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		double sum = 0;

		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			Hashtable<Integer, Obs> dailyObs = entrySet.getValue();
			for (Map.Entry<Integer, Obs> dailyObsEntry : dailyObs.entrySet()) {
				Obs observations = dailyObsEntry.getValue();
				String seq = observations.getVitPath();
				String timeStamp = observations.getTimeStamp();

				if (seq.isEmpty()) {
					continue;
				}

				// System.out.println(seq);
				/**
				 * Initiate daily observation trips
				 */
				String[] tstamps = new String[] { timeStamp };
				if (seq.contains(RLM)) {
					tstamps = timeStamp.replace(RLM, CLM).split(CLM);
				}

				for (int j = 0; j < tstamps.length; j++) {
					String tstamp = tstamps[j];
					cal.setTime(formatter.parse(tstamp));
					/**
					 * If current time stamp in between the require period add
					 * it to the path.
					 */
					if ((fromH.before(cal) && toH.after(cal))) {
						// System.out.println("in");
						sum++;

					}

				}
			}
		}
		// System.out.println(toH.get(Calendar.HOUR_OF_DAY) + ":" +
		// toH.get(Calendar.MINUTE) + ":" + toH.get(Calendar.SECOND) + "\t" +
		// count_time(tseq_hour));
		return sum;
	}

	/**
	 * generate density map from DAILY Viterbi paths from a specific time to
	 * another.
	 *
	 * @param Calendar
	 *            fromH
	 * @param Calendar
	 *            toH
	 * @param Hashtable<Integer,
	 *            Hashtable<Integer, Obs>> obs
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getHDensity(Calendar fromH, Calendar toH,
			Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {

		Hashtable<String, Hashtable<String, Double>> hd = new Hashtable<>();
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		ArrayList<String> seq_hour = new ArrayList<>();
		ArrayList<String> tseq_hour = new ArrayList<>();

		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			Hashtable<Integer, Obs> dailyObs = entrySet.getValue();
			for (Map.Entry<Integer, Obs> dailyObsEntry : dailyObs.entrySet()) {
				Obs observations = dailyObsEntry.getValue();
				String seq = observations.getVitPath();
				String timeStamp = observations.getTimeStamp();

				if (seq.isEmpty()) {
					continue;
				}
				// skip undecoded routes
				if (seq.equals("-"))
					continue;

				// remove interpolation
				if (seq.contains("_interpolated_")) {
					seq = seq.replaceAll("_interpolated_\\[[0-9]+\\]", "");
				}
				// remove edges on the same road
				if (seq.contains("#")) {
					seq = seq.replaceAll("#[0-9]+", "");
				}

				// System.out.println(seq);
				/**
				 * Initiate daily observation trips
				 */
				String[] vitPnts = new String[] { seq };
				String[] obsTimes = new String[] { timeStamp };
				if (seq.contains(RLM)) {
					vitPnts = seq.split(RLM);
					obsTimes = timeStamp.split(RLM);
				}

				/**
				 * generate partial trips (trips based on time stamp and
				 * distances).
				 */
				for (int i = 0; i < vitPnts.length; i++) {
					/**
					 * If the Viterbi of this trip can not be determine continue
					 * to the next one.
					 */
					if (vitPnts[i].equals("-")) {
						// System.out.println(vitPnts[i]);
						continue;
					}
					// else {
					// System.out.println(vitPnts[i]);
					// }

					String[] pnts = vitPnts[i].split(CLM);
					String[] tstamps = obsTimes[i].split(CLM);

					String path = "";
					String time = "";
					for (int j = 0; j < tstamps.length; j++) {
						String tstamp = tstamps[j];
						cal.setTime(formatter.parse(tstamp));
						/**
						 * If current time stamp in between the require period
						 * add it to the path.
						 */
						if ((fromH.before(cal) && toH.after(cal))) {
							// System.out.println("in");
							time += tstamp + ",";
							path += pnts[j] + ",";
						}

					}
					/**
					 *
					 */
					if (!path.isEmpty()) {
						seq_hour.add(path);
						tseq_hour.add(time);
						// System.out.println(time + "\t" + path);
					}

				}
			}
		}
		// System.out.println(toH.get(Calendar.HOUR_OF_DAY) + ":" +
		// toH.get(Calendar.MINUTE) + ":" + toH.get(Calendar.SECOND) + "\t" +
		// count_time(tseq_hour));
		return density(seq_hour);
	}

	public Hashtable<String, Integer> get_Hourly_Density_street(Calendar fromH, Calendar toH,
			Hashtable<String, Hashtable<Integer, Obs>> obs) throws ParseException {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		ArrayList<String> seq_hour = new ArrayList<>();
		ArrayList<String> tseq_hour = new ArrayList<>();
		Hashtable<String, Integer> flow = new Hashtable<>();
		
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			Hashtable<Integer, Obs> dailyObs = entrySet.getValue();
			for (Map.Entry<Integer, Obs> dailyObsEntry : dailyObs.entrySet()) {
				Obs observations = dailyObsEntry.getValue();
				String seq = observations.getVitPath();
				String timeStamp = observations.getTimeStamp();

				if (seq.isEmpty()) {
					continue;
				}
				// skip undecoded routes
				if (seq.equals("-"))
					continue;

				// remove interpolation
				if (seq.contains("_interpolated_")) {
					seq = seq.replaceAll("_interpolated_\\[[0-9]+\\]", "");
				}
				// remove edges on the same road
				if (seq.contains("#")) {
					seq = seq.replaceAll("#[0-9]+", "");
				}

				// System.out.println(seq);
				/**
				 * Initiate daily observation trips
				 */
				String[] vitPnts = new String[] { seq };
				String[] obsTimes = new String[] { timeStamp };
				if (seq.contains(RLM)) {
					vitPnts = seq.split(RLM);
					obsTimes = timeStamp.split(RLM);
				}

				/**
				 * generate partial trips (trips based on time stamp and
				 * distances).
				 */
				for (int i = 0; i < vitPnts.length; i++) {
					/**
					 * If the Viterbi of this trip can not be determine continue
					 * to the next one.
					 */
					if (vitPnts[i].equals("-")) {
						// System.out.println(vitPnts[i]);
						continue;
					}
					// else {
					// System.out.println(vitPnts[i]);
					// }

					String[] pnts = vitPnts[i].split(CLM);
					String[] tstamps = obsTimes[i].split(CLM);

					String path = "";
					String time = "";
					for (int j = 0; j < tstamps.length; j++) {
						String tstamp = tstamps[j];
						cal.setTime(formatter.parse(tstamp));
						/**
						 * If current time stamp in between the require period
						 * add it to the path.
						 */
						if ((fromH.before(cal) && toH.after(cal))) {
							// System.out.println("in");
							time += tstamp + ",";
							path += pnts[j] + ",";
						}

					}
					/**
					 *
					 */
					if (!path.isEmpty()) {
						seq_hour.add(path);
						tseq_hour.add(time);
						
						List<String> segs = Arrays.asList(path.split(","));
						Set<String> uniqueSet = new HashSet<String>(segs);
						for (String temp : uniqueSet) {
							///////
							if (flow.containsKey(temp)) {
								int freq = flow.get(temp);
								flow.replace(temp, freq + 1);
							} else {
								int freq = 1;
								flow.put(temp, freq);
							}
						}

						// System.out.println(time + "\t" + path);
					}

				}
			}
		}
		// System.out.println(toH.get(Calendar.HOUR_OF_DAY) + ":" +
		// toH.get(Calendar.MINUTE) + ":" + toH.get(Calendar.SECOND) + "\t" +
		// count_time(tseq_hour));
		return flow;
	}

	/**
	 * Get the max number of accumulated vehicles
	 *
	 * @return
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Get the sum counted number of vehicles
	 *
	 * @param accum
	 * @return
	 */
	public int getMax(Hashtable<String, Integer> accum) {
		int max = 0;
		for (Map.Entry<String, Integer> entrySet : accum.entrySet()) {
			String key = entrySet.getKey();
			int val = entrySet.getValue();
			if (val > max) {
				max = val;
			}

		}
		return max;
	}

	/**
	 * Get the mean number of accumulated vehicles
	 *
	 * @param accum
	 * @return
	 */
	public int getMean(Hashtable<String, Integer> accum) {
		int sum = 0;

		for (Map.Entry<String, Integer> entrySet : accum.entrySet()) {
			String key = entrySet.getKey();
			int val = entrySet.getValue();
			sum += val;

		}
		return sum / accum.size();
	}

	private ArrayList<String> getStates(Hashtable<String, Hashtable<String, Integer>> dst) {
		ArrayList<String> states = new ArrayList<>();
		for (Map.Entry<String, Hashtable<String, Integer>> entrySet : dst.entrySet()) {
			Hashtable<String, Integer> value = entrySet.getValue();
			for (Map.Entry<String, Integer> entrySet1 : value.entrySet()) {
				String key = entrySet1.getKey();
				if (!states.contains(key)) {
					states.add(key);
				}
			}

		}
		return states;
	}

	public void mark_edges(ArrayList<Edge> edges, String map, ArrayList<String> edges_mark, String image,
			boolean all_routes) {

		Plot ploter = new Plot(edges, image);
		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}
		for (Iterator<String> iterator = edges_mark.iterator(); iterator.hasNext();) {
			String edge = iterator.next();
			ploter.mark_edge(edge, Color.RED);
		}

		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Plot density maps with fixed gamut colors with edges interpolation
	 *
	 * @param edges
	 * @param map
	 * @param density
	 * @param image
	 * @param all_routes
	 * @param date
	 * @param clock
	 * @param max
	 */
	public void plot_acc_Density(ArrayList<Edge> edges, String map, Hashtable<String, Double> accumVeh, String image,
			boolean all_routes, String date, String clock, double max) {

		Hashtable<Double, Color> colors = new Hashtable<>();
		colors.put(0.2, new Color(39, 230, 23));
		colors.put(0.4, new Color(230, 221, 23));
		colors.put(0.6, new Color(230, 122, 23));
		colors.put(0.8, new Color(230, 23, 23));
		colors.put(1.0, new Color(139, 23, 23));

		Plot ploter = new Plot(edges, image);
		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}
		// Density densityHandler = new Density();
		// Hashtable<String, Integer> accumVeh =
		// densityHandler.sumIEVehicles(density);
		// Hashtable<String, Integer> accumVeh =
		// densityHandler.sumVehicles(density);
		int decimal_pnt = 1;
		for (Map.Entry<String, Double> entrySet : accumVeh.entrySet()) {
			String key = entrySet.getKey();
			double value = entrySet.getValue();
			if (value < max / 5) {
				value = max / 5;
			} else if (value >= max / 5 && value < (2 * max) / 5) {
				value = 2 * max / 5;
			} else if (value >= (2 * max) / 5 && value < (3 * max) / 5) {
				value = 3 * max / 5;
			} else if (value >= (3 * max) / 5 && value < (4 * max) / 5) {
				value = 4 * max / 5;
			} else if (value >= (4 * max) / 5) {
				value = max;
			}
			double power = Plot.round(value / max, decimal_pnt);
			// Color color = ploter.getColor(power);
			// colors.put(power, color);
			ploter.mark_edge(key, colors.get(power));
		}

		ploter.plotHGamut(colors, decimal_pnt, max, gxmin, gymin, gxmax, gymax);
		// System.out.println(Font.getFont("DS-Digital").toString());
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 150));
		StdDraw.text(xmin + 3100, ymax - 500, date);
		StdDraw.text(xmin + 3100, ymax - 1300, clock);
		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Plot accumulated traffic densities
	 *
	 * @param edges
	 * @param map
	 * @param hashtable
	 * @param image
	 * @param all_routes
	 * @param date
	 * @param clock
	 * @param max
	 */
	public void plotAccDensity(ArrayList<Edge> edges, String map, Hashtable<String, Integer> hashtable, String image,
			boolean all_routes, String date, String clock, double max) {

		Hashtable<Double, Color> colors = new Hashtable<>();
		colors.put(0.2, new Color(39, 230, 23));
		colors.put(0.4, new Color(230, 221, 23));
		colors.put(0.6, new Color(230, 122, 23));
		colors.put(0.8, new Color(230, 23, 23));
		colors.put(1.0, new Color(139, 23, 23));

		Plot ploter = new Plot(edges, image);
		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}

		Hashtable<String, Integer> accumVeh = hashtable;
		int decimal_pnt = 1;
		for (Map.Entry<String, Integer> entrySet : accumVeh.entrySet()) {
			String key = entrySet.getKey();
			double value = entrySet.getValue();
			if (value < max / 5) {
				value = max / 5;
			} else if (value > max / 5 && value < (2 * max) / 5) {
				value = 2 * max / 5;
			} else if (value > (2 * max) / 5 && value < (3 * max) / 5) {
				value = 3 * max / 5;
			} else if (value > (3 * max) / 5 && value < (4 * max) / 5) {
				value = 4 * max / 5;
			} else {
				value = max;
			}
			double power = Plot.round(value / max, decimal_pnt);
			ploter.mark_edge(key, colors.get(power));
		}

		ploter.plotHGamut(colors, decimal_pnt, max, gxmin, gymin, gxmax, gymax);
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 100));
		StdDraw.text(xmin + 3000, ymax - 500, date);
		StdDraw.text(xmin + 3000, ymax - 1300, clock);
		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Plot density maps with fixed gamut colors with edges interpolations
	 *
	 * @param edges
	 * @param map
	 * @param density
	 * @param image
	 * @param all_routes
	 * @param date
	 * @param clock
	 * @param max
	 */
	public void plotDensity(ArrayList<Edge> edges, String map, Hashtable<String, Hashtable<String, Double>> density,
			double xmin, double ymin, double xmax, double ymax, String image, boolean all_routes, String date,
			String clock, double max) {

		Hashtable<Double, Color> colors = new Hashtable<>();
		colors.put(0.2, new Color(39, 230, 23));
		colors.put(0.4, new Color(230, 221, 23));
		colors.put(0.6, new Color(230, 122, 23));
		colors.put(0.8, new Color(230, 23, 23));
		colors.put(1.0, new Color(139, 23, 23));

		Plot ploter = new Plot(edges, image);

		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}
		Density densityHandler = new Density();
		Hashtable<String, Integer> accumVeh = densityHandler.sumVehicles(density);
		int decimal_pnt = 1;
		for (Map.Entry<String, Integer> entrySet : accumVeh.entrySet()) {
			String key = entrySet.getKey();
			/**
			 * wrong value (1.2) must be removed
			 */
			double value = 1.2 * entrySet.getValue();

			if (value < max / 5) {
				value = max / 5;
			} else if (value >= max / 5 && value < (2 * max) / 5) {
				value = 2 * max / 5;
			} else if (value >= (2 * max) / 5 && value < (3 * max) / 5) {
				value = 3 * max / 5;
			} else if (value >= (3 * max) / 5 && value < (4 * max) / 5) {
				value = 4 * max / 5;
			} else if (value >= (4 * max) / 5) {
				value = max;
			}
			double power = Plot.round(value / max, decimal_pnt);
			// Color color = ploter.getColor(power);
			// colors.put(power, color);
			// ploter.markEdge(key, value, true, colors.get(power));
			ploter.mark_edge(key, colors.get(power));
		}

		ploter.plotHGamut(colors, decimal_pnt, max, gxmin, gymin, gxmax, gymax);
		// System.out.println(Font.getFont("DS-Digital").toString());
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 100));
		StdDraw.text(xmin + 3100, ymax - 500, date);
		StdDraw.text(xmin + 3100, ymax - 1300, clock);
		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Plot density maps
	 *
	 * @param edges
	 * @param map
	 * @param density
	 * @param image
	 * @param all_routes
	 */
	public void plotDensity(ArrayList<Edge> edges, String map, Hashtable<String, Hashtable<String, Double>> density,
			String image, boolean all_routes) {

		Plot ploter = new Plot(edges, image);
		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}

		// StdDraw.setPenRadius(0.0005);
		Density densityHandler = new Density();
		// Hashtable<String, Hashtable<String, Double>> density =
		// densityHandler.readDensity(densityPath);
		Hashtable<String, Integer> accumVeh = densityHandler.sumIEVehicles(density);
		double max = densityHandler.getMax(accumVeh);
		// double mean = densityHandler.getMean(accumVeh);
		int decimal_pnt = 1;
		Hashtable<Double, Color> colors = new Hashtable<>();
		for (Map.Entry<String, Integer> entrySet : accumVeh.entrySet()) {
			String key = entrySet.getKey();
			double value = entrySet.getValue();
			double power = Plot.round(value / max, decimal_pnt);
			Color color = ploter.getColor(power);
			colors.put(power, color);
			ploter.mark_edge(key, color);
		}

		ploter.plotHGamut(colors, decimal_pnt, max, gxmin, gymin, gxmax, gymax);
		// System.out.println(Font.getFont("DS-Digital").toString());
		// StdDraw.setFont(font);
		// StdDraw.setPenRadius(1);
		// StdDraw.text(gxmin, gymin, to_clock);
		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Plot density maps with fixed gamut colors with edges interpolations
	 *
	 * @param edges
	 * @param map
	 * @param density
	 * @param image
	 * @param all_routes
	 * @param date
	 * @param clock
	 * @param max
	 */
	public void plotDensity(ArrayList<Edge> edges, String map, Hashtable<String, Hashtable<String, Double>> density,
			String image, boolean all_routes, String date, String clock, double max) {

		Hashtable<Double, Color> colors = new Hashtable<>();
		colors.put(0.2, new Color(39, 230, 23));
		colors.put(0.4, new Color(230, 221, 23));
		colors.put(0.6, new Color(230, 122, 23));
		colors.put(0.8, new Color(230, 23, 23));
		colors.put(1.0, new Color(139, 23, 23));

		Plot ploter = new Plot(edges, image);

		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}
		Density densityHandler = new Density();
		Hashtable<String, Integer> accumVeh = densityHandler.sumVehicles(density);
		int decimal_pnt = 1;
		for (Map.Entry<String, Integer> entrySet : accumVeh.entrySet()) {
			String key = entrySet.getKey();
			/**
			 * wrong value (1.2) must be removed
			 */
			double value = 1.2 * entrySet.getValue();

			if (value < max / 5) {
				value = max / 5;
			} else if (value >= max / 5 && value < (2 * max) / 5) {
				value = 2 * max / 5;
			} else if (value >= (2 * max) / 5 && value < (3 * max) / 5) {
				value = 3 * max / 5;
			} else if (value >= (3 * max) / 5 && value < (4 * max) / 5) {
				value = 4 * max / 5;
			} else if (value >= (4 * max) / 5) {
				value = max;
			}
			double power = Plot.round(value / max, decimal_pnt);
			// Color color = ploter.getColor(power);
			// colors.put(power, color);
			ploter.mark_edge(key, value, true, colors.get(power));
			// ploter.markEdge(key, colors.get(power));
		}

		ploter.plotHGamut(colors, decimal_pnt, max, gxmin, gymin, gxmax, gymax);
		// System.out.println(Font.getFont("DS-Digital").toString());
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 100));
		StdDraw.text(xmin + 3100, ymax - 500, date);
		StdDraw.text(xmin + 3100, ymax - 1300, clock);
		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Plot density maps with fixed gamut colors with edges interpolation
	 *
	 * @param edges
	 * @param map
	 * @param density
	 * @param image
	 * @param all_routes
	 * @param date
	 * @param clock
	 * @param max
	 */
	public void plotIEDensity(ArrayList<Edge> edges, String map, Hashtable<String, Hashtable<String, Double>> density,
			String image, boolean all_routes, String date, String clock, double max) {

		Hashtable<Double, Color> colors = new Hashtable<>();
		colors.put(0.2, new Color(39, 230, 23));
		colors.put(0.4, new Color(230, 221, 23));
		colors.put(0.6, new Color(230, 122, 23));
		colors.put(0.8, new Color(230, 23, 23));
		colors.put(1.0, new Color(139, 23, 23));

		Plot ploter = new Plot(edges, image);
		ploter.scale(xmin, ymin, xmax, ymax);
		if (all_routes) {
			ploter.plotMapData(map);
		} else {
			ploter.plotMapEdges();
		}
		Density densityHandler = new Density();
		Hashtable<String, Integer> accumVeh = densityHandler.sumIEVehicles(density);
		// Hashtable<String, Integer> accumVeh =
		// densityHandler.sumVehicles(density);
		int decimal_pnt = 1;
		for (Map.Entry<String, Integer> entrySet : accumVeh.entrySet()) {
			String key = entrySet.getKey();
			double value = entrySet.getValue();
			if (value < max / 5) {
				value = max / 5;
			} else if (value >= max / 5 && value < (2 * max) / 5) {
				value = 2 * max / 5;
			} else if (value >= (2 * max) / 5 && value < (3 * max) / 5) {
				value = 3 * max / 5;
			} else if (value >= (3 * max) / 5 && value < (4 * max) / 5) {
				value = 4 * max / 5;
			} else if (value >= (4 * max) / 5) {
				value = max;
			}
			double power = Plot.round(value / max, decimal_pnt);
			// Color color = ploter.getColor(power);
			// colors.put(power, color);
			ploter.mark_edge(key, colors.get(power));
		}

		ploter.plotHGamut(colors, decimal_pnt, max, gxmin, gymin, gxmax, gymax);
		// System.out.println(Font.getFont("DS-Digital").toString());
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 150));
		StdDraw.text(xmin + 3100, ymax - 500, date);
		StdDraw.text(xmin + 3100, ymax - 1300, clock);
		StdDraw.setPenRadius();
		ploter.display_save();
	}

	/**
	 * Read accumulated densities
	 *
	 * @param path
	 * @return
	 */
	public Hashtable<String, Integer> readAccumDensity(String path) {

		Hashtable<String, Integer> hd = new Hashtable<>();
		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();

			NodeList nodeList = dpDoc.getElementsByTagName("node");
			for (int i = 0; i < nodeList.getLength(); i++) {

				Node fromNodeNode = nodeList.item(i);

				if (fromNodeNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) fromNodeNode;
					String id = eElement.getAttribute("id");
					int density = Integer.parseInt(eElement.getAttribute("vehicles"));
					hd.put(id, density);
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}
		return hd;

	}

	public Hashtable<String, Integer> removeEdgesInterpolation(Hashtable<String, Integer> ihd) {
		Hashtable<String, Integer> hd = new Hashtable<>();

		for (Map.Entry<String, Integer> entry : ihd.entrySet()) {
			String id = entry.getKey();
			int density = entry.getValue();

			if (id.contains("_interpolated")) {
				id = id.substring(0, id.indexOf("_interpolated"));
			}

			if (hd.containsKey(id)) {

				density += hd.get(id);
				hd.replace(id, density);
			} else {
				hd.put(id, density);
			}
		}
		return hd;
	}

	public Hashtable<String, Hashtable<String, Integer>> sumHDayDensities(
			Hashtable<String, Hashtable<String, Integer>> dst, Hashtable<String, Hashtable<String, Integer>> src) {
		for (Map.Entry<String, Hashtable<String, Integer>> entrySet : src.entrySet()) {
			String srcHKey = entrySet.getKey();
			Hashtable<String, Integer> srcHD = entrySet.getValue();
			if (!dst.containsKey(srcHKey)) {
				dst.put(srcHKey, srcHD);
				continue;
			}
			Hashtable<String, Integer> dstHD = dst.get(srcHKey);
			for (Map.Entry<String, Integer> hdentry : srcHD.entrySet()) {
				String state = hdentry.getKey();
				Integer src_count = hdentry.getValue();
				if (!dstHD.containsKey(state)) {
					dstHD.put(state, src_count);
				} else {
					Integer dst_count = src_count + dstHD.get(state);
					dstHD.put(state, dst_count);
				}

			}
			dst.replace(srcHKey, dstHD);
		}
		return dst;
	}

	/**
	 * Accumulate vehicles per exit points (Interpolated edges)
	 *
	 * @param density
	 * @return
	 */
	public Hashtable<String, Integer> sumIEVehicles(Hashtable<String, Hashtable<String, Double>> density) {
		Hashtable<String, Integer> sv = new Hashtable<>();

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : density.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<String, Double> value = entrySet.getValue();
			int sum = 0;
			for (Map.Entry<String, Double> toentry : value.entrySet()) {
				sum += toentry.getValue();

			}
			if (sum > max) {
				max = sum;
			}
			sv.put(key, sum);

		}

		return sv;
	}

	/**
	 * Accumulate vehicles per exit points (Interpolated edges)
	 *
	 * @param density
	 * @return
	 */
	public Hashtable<String, Integer> sumVehicles(Hashtable<String, Hashtable<String, Double>> density) {
		Hashtable<String, Integer> sv = new Hashtable<>();

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : density.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<String, Double> value = entrySet.getValue();
			int sum = 0;
			for (Map.Entry<String, Double> toentry : value.entrySet()) {
				sum += toentry.getValue();

			}
			if (sum > max) {
				max = sum;
			}
			sv.put(key, sum);

		}
		// return sv;
		return removeEdgesInterpolation(sv);
	}

	/**
	 * Write the accumulated number of vehicle to XML file.
	 *
	 * @param sumTable
	 * @param path
	 */
	public void writeAccumDensity(Hashtable<String, Integer> sumTable, String path) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("exit-defs");
			doc.appendChild(rootElement);

			for (Map.Entry<String, Integer> entrySet : sumTable.entrySet()) {
				String pKey = entrySet.getKey();
				Element fromNodeElement = doc.createElement("node");
				rootElement.appendChild(fromNodeElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(pKey);
				fromNodeElement.setAttributeNode(attr);
				// System.out.println(pKey);
				int sum = entrySet.getValue();
				attr = doc.createAttribute("vehicles");
				attr.setValue(String.valueOf(sum));
				fromNodeElement.setAttributeNode(attr);
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

	public void writeHDstates(String path, Hashtable<String, Hashtable<String, Integer>> dst) throws ParseException {
		DateFormat dayformater = new SimpleDateFormat("yyyy-mm-dd");
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");

		try {
			ArrayList<String> states = getStates(dst);
			File file = new File(path);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			/**
			 * Write header.
			 */
			bw.write("day_hours");
			for (Iterator<String> iterator = states.iterator(); iterator.hasNext();) {
				String next = iterator.next();
				bw.write(CLM + next);
			}
			bw.newLine();
			for (Map.Entry<String, Hashtable<String, Integer>> entrySet : dst.entrySet()) {
				String key = entrySet.getKey();

				Calendar cal1 = Calendar.getInstance();
				cal1.setTime(dayformater.parse(key));
				bw.write(key + CLM + dateFormat.format(cal1.getTime()));

				Hashtable<String, Integer> hdstates = entrySet.getValue();
				for (Iterator<String> iterator = states.iterator(); iterator.hasNext();) {
					String next = iterator.next();
					int val = 0;
					if (hdstates.containsKey(next)) {
						val = hdstates.get(next);
					}
					bw.write(CLM + val);
				}
				bw.newLine();

			}
			bw.close();
			System.out.println("File Saved");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
