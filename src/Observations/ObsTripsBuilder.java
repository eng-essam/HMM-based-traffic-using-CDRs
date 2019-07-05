/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import mergexml.MergeXML;
import utils.ToNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class ObsTripsBuilder {

	/**
	 * Delimeters used in the data representation.
	 */
	final static String RLM = "/";
	final static String CLM = ",";
	final static int HOUR = 60;
	/**
	 * Map sort parameters.
	 */
	public static boolean ASC = true;

	public static boolean DESC = false;
	/**
	 * Trip detection parameters.
	 */
	final static int dist_th = 1000;
	final static int time_th = 60;
	/**
	 * Hourly increment values for hourly interval data.
	 */
	final static int increment = 1;

	public static void buildObsSeq(String srcPath, String dstPath, int stower, int etower) {

		BufferedReader reader;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Observations");
			doc.appendChild(rootElement);

			reader = new BufferedReader(new FileReader(srcPath));

			String line;
			String id = null;
			String nextID;
			String siteID;
			StringBuilder obsr = null;
			char dlm = '/';
			/**
			 * Data must be sorted by the user ID Data set Arrana=ged as the
			 * following ID Time Site ID
			 */
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();

				if (id == null) {
					id = nextID;
				}
				siteID = lineSplit[2].trim();
				int site = Integer.parseInt(siteID);

				if (obsr == null && site >= stower && site <= etower) {
					obsr = new StringBuilder(siteID);
				} else if (obsr != null && id.equals(nextID)) {

					if (site >= stower && site <= etower) {
						if (dlm != obsr.charAt(obsr.length() - 1)) {
							obsr.append(",");
						}
						obsr.append(siteID);
					} else if (dlm != obsr.charAt(obsr.length() - 1)) {
						// System.out.println(obsr.charAt(obsr.length() - 1));
						// obsr.append(",");
						obsr.append(dlm);
					}

				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */

					if (obsr != null) {
						Element userElement = doc.createElement("user");
						rootElement.appendChild(userElement);

						// set attribute to staff element
						Attr attr = doc.createAttribute("id");
						attr.setValue(id);
						userElement.setAttributeNode(attr);

						attr = doc.createAttribute("sitesSequanace");
						attr.setValue(obsr.toString());
						userElement.setAttributeNode(attr);
					}
					id = nextID;
					obsr = null;

				}

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(dstPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}

	/**
	 * extract only a sample of user observation that fall within a specific
	 * range of a towers .
	 *
	 * @param obs
	 * @param stwr
	 * @param etwr
	 * @param sample_size
	 * @return
	 */
	public static Hashtable<Integer, List<Integer>> get_com_usr_sample(Hashtable<Integer, List<Integer>> obs, int stwr,
			int etwr, int sample_size) {
		Hashtable<Integer, List<Integer>> sample = new Hashtable<>();
		for (Map.Entry<Integer, List<Integer>> entrySet : obs.entrySet()) {
			Integer key = entrySet.getKey();
			List<Integer> obs_lst = entrySet.getValue();
			boolean flag = true;
			for (Iterator<Integer> iterator = obs_lst.iterator(); iterator.hasNext();) {
				int t = iterator.next();
				if (!(t > stwr && t < etwr)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				// add to the sample ..
				sample.put(key, obs_lst);
			}

		}

		return sample;
	}

	public static ArrayList<String> list_dir_files(final File folder) {
		ArrayList<String> files = new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				list_dir_files(fileEntry);
			} else {
				files.add(fileEntry.getPath());
				// System.out.println(fileEntry.getName());
			}
		}
		return files;
	}

	public static ArrayList<String> list_directories(final File folder) {
		ArrayList<String> dirs = new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				dirs.add(fileEntry.getPath());
			}
		}
		return dirs;
	}

	public static Hashtable<String, Hashtable<Integer, Obs>> readObsDUT(String path) {
		Hashtable<String, Hashtable<Integer, Obs>> table = new Hashtable<>();
		org.w3c.dom.Document dpDoc;
		// for (int zone = 1; zone < 300; zone++) {
		// System.out.println("zone:\t"+zone);
		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList daysList = dpDoc.getElementsByTagName("week_day");

			for (int i = 0; i < daysList.getLength(); i++) {

				Node dayNode = daysList.item(i);

				if (dayNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) dayNode;
					String day = eElement.getAttribute("id");

					ToNode edge;
					NodeList toNodeList = eElement.getElementsByTagName("user");
					Hashtable<Integer, Obs> usrsTable = new Hashtable<>();
					for (int j = 0; j < toNodeList.getLength(); j++) {
						Node toNodeNode = toNodeList.item(j);
						if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
							Element usrElement = (Element) toNodeNode;

							String id = usrElement.getAttribute("id");
							Obs obs = new Obs(usrElement.getAttribute("seq"), usrElement.getAttribute("timestamp"),
									usrElement.getAttribute("viterbi"));
							usrsTable.put(Integer.parseInt(id), obs);
						}
					}
					table.put(day, usrsTable);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}

		return table;
	}

	/**
	 *
	 * Sun 07 Dec 2014 01:37:23 AM EET
	 *
	 * Write Observation tables
	 *
	 * @param obsTable
	 * @param dstPath
	 */
	public static void writeObsDUT(Hashtable<String, Hashtable<Integer, Obs>> obsTable, String dstPath) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("observation");
			doc.appendChild(rootElement);

			for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
				String userID = entrySet.getKey();
				Element dayElement = doc.createElement("week_day");
				rootElement.appendChild(dayElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(String.valueOf(userID));
				dayElement.setAttributeNode(attr);
				// System.out.println(pKey);
				Hashtable<Integer, Obs> value = entrySet.getValue();
				for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
					Integer weekDay = entrySet1.getKey();
					Obs obs = entrySet1.getValue();

					Element usrSeqElement = doc.createElement("user");
					dayElement.appendChild(usrSeqElement);

					attr = doc.createAttribute("id");
					attr.setValue(String.valueOf(weekDay));
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("seq");
					attr.setValue(obs.seq);
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("viterbi");
					attr.setValue(obs.vitPath);
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("timestamp");
					attr.setValue(obs.timeStamp);
					usrSeqElement.setAttributeNode(attr);

				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(dstPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}

	public static void writeObsIDUT(Hashtable<Integer, Hashtable<Integer, Obs>> obsTable, String dstPath) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("observation");
			doc.appendChild(rootElement);

			for (Map.Entry<Integer, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
				Integer userID = entrySet.getKey();
				Element dayElement = doc.createElement("week_day");
				rootElement.appendChild(dayElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(String.valueOf(userID));
				dayElement.setAttributeNode(attr);
				// System.out.println(pKey);
				Hashtable<Integer, Obs> value = entrySet.getValue();
				for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
					Integer weekDay = entrySet1.getKey();
					Obs obs = entrySet1.getValue();

					Element usrSeqElement = doc.createElement("user");
					dayElement.appendChild(usrSeqElement);

					attr = doc.createAttribute("id");
					attr.setValue(String.valueOf(weekDay));
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("seq");
					attr.setValue(obs.seq);
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("viterbi");
					attr.setValue(obs.vitPath);
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("timestamp");
					attr.setValue(obs.timeStamp);
					usrSeqElement.setAttributeNode(attr);

				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(dstPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}

	String dSPath;

	String xmlPath;

	public ObsTripsBuilder() {
	}

	public ObsTripsBuilder(String dSPath, String xmlPath) {
		this.dSPath = dSPath;
		this.xmlPath = xmlPath;
	}

	/**
	 *
	 * @param obs
	 * @param distance_threshold
	 * @param timing_threshold
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> adaptiveTrips(Hashtable<Integer, Hashtable<String, Obs>> obs,
			Hashtable<Integer, Vertex> towersXY, int dist_th, int time_th) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : obs.entrySet()) {
			Integer userID = entrySet.getKey();
			Hashtable<String, Obs> dailyObs = entrySet.getValue();
			for (Map.Entry<String, Obs> dailyObsEntry : dailyObs.entrySet()) {
				String dayID = dailyObsEntry.getKey();
				Obs observations = dailyObsEntry.getValue();
				String seq = observations.seq;
				String timeStamp = observations.timeStamp;
				/**
				 * Initiate daily observation trips
				 */
				String[] obsTowers = new String[] { seq };
				String[] obsTimes = new String[] { timeStamp };
				if (observations.seq.contains(RLM)) {
					obsTowers = seq.split(RLM);
					obsTimes = timeStamp.split(RLM);
				}

				/**
				 * generate partial trips (trips based on time stamp and
				 * distances).
				 */
				for (int i = 0; i < obsTimes.length; i++) {
					try {
						String[] towers = obsTowers[i].split(CLM);
						String[] tstamps = obsTimes[i].split(CLM);
						/**
						 * Check the complete sequence time difference between
						 * starting and end, if this time less than the timing
						 * threshold continue to the next sequence else
						 * determine the trip.
						 */

						Date sTime = formatter.parse(tstamps[0]);
						Date eTime = formatter.parse(tstamps[tstamps.length - 1]);
						cal.setTime(sTime);
						int hour = cal.get(Calendar.HOUR);
						int minute = cal.get(Calendar.MINUTE);

						cal.setTime(eTime);
						int ehour = cal.get(Calendar.HOUR);
						int eminute = cal.get(Calendar.MINUTE);

						int diff = (ehour - hour) * HOUR + (eminute - minute);
						/**
						 * check time difference with time threshold whatever
						 * the distance between the starting tower
						 */
						if (diff > time_th) {
							/**
							 * Find trips and update current observation seq and
							 * its time stamps
							 */
							// System.out.println("Diff > timing threshold");
							Obs newObs = findTrips(towers, tstamps, towersXY, dist_th, time_th);
							// System.out.println(newObs.seq+"---"+newObs.timeStamp);
							obsTowers[i] = newObs.seq;
							obsTimes[i] = newObs.timeStamp;
						}
						// else {
						//
						// /**
						// * Remove repeated observations from the single
						// * trips.
						// */
						// String time_tmp = "";
						// String obs_tmp = "";
						// time_tmp += tstamps[0];
						// obs_tmp += towers[0];
						// for (int j = 1; j < towers.length; j++) {
						//
						// if (towers[j].equals(towers[j - 1])) {
						// System.out.println(towers[j] + "\t=\t" + towers[j -
						// 1]);
						// continue;
						// }
						// /**
						// * Add comma separators
						// */
						// if (!time_tmp.isEmpty()) {
						// time_tmp += CLM;
						// obs_tmp += CLM;
						// }
						// time_tmp += tstamps[j];
						// obs_tmp += towers[j];
						// }
						// towerSubseqs[i] = obs_tmp;
						// stampSubseqs[i] = time_tmp;
						// }

					} catch (ParseException ex) {
						Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
					}

				}
				/**
				 * Update observations for the current user.
				 */
				seq = "";
				timeStamp = "";

				for (int i = 0; i < obsTimes.length; i++) {
					if (!seq.isEmpty()) {
						seq += RLM;
						timeStamp += RLM;
					}

					seq += obsTowers[i];
					timeStamp += obsTimes[i];

				}
				observations = new Obs(seq, timeStamp);
				dailyObs.replace(dayID, observations);

			}
			/**
			 * Update observations for the current day.
			 */
			obs.replace(userID, dailyObs);
		}

		return obs;
	}

	/**
	 * Mon 08 Jun 2015 03:16:32 PM JST
	 *
	 * This is the exact implementation of the algorithm provided in AllAboard
	 * paper but reduce the buffer to the last element and use the non stops
	 * part as traveling observation sequences.
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs algorithm2(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * trips sets and time stamps for these stops
		 */
		ArrayList<String> trips = new ArrayList<>();
		ArrayList<String> tstrips = new ArrayList<>();
		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();
		try {

			double max_distance = 0;
			int time_diff = 0;
			for (int i = 0; i < towers.length; i++) {
				Vertex a = towersXY.get(Integer.parseInt(towers[i]));
				for (int j = 0; j < buffer.size(); j++) {
					Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
					// System.out.println("b"+Integer.parseInt(buffer.get(j)));
					double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
					if (tmp_distance > max_distance) {
						max_distance = tmp_distance;
					}

				}

				buffer.add(towers[i]);
				tbuffer.add(tstamps[i]);

				if (max_distance > dist_th) {

					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					java.util.Date sTime = formatter.parse(tbuffer.get(0));
					java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
					cal.setTime(sTime);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

					if (time_diff > time_th) {

						if (trips.isEmpty()) {
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
						} else {
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
							trips.add(RLM);
							tstrips.add(RLM);
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
						}
						/**
						 * Reset buffers.
						 */
						buffer = new ArrayList<>();
						tbuffer = new ArrayList<>();
						/**
						 * Reset maximum distances
						 */
						max_distance = 0;

					} else {

						/**
						 * Add the first observation as the origin of the first
						 * trips and the remaining part of the buffer as the
						 * traveling observations, else add the complete buffer
						 * elements as the observation seq of the traveling
						 * observables.
						 */
						trips.addAll(buffer);
						tstrips.addAll(tbuffer);

						buffer = new ArrayList<>();
						tbuffer = new ArrayList<>();
						max_distance = 0;
					}

				}

			}
		} catch (NumberFormatException numberFormatException) {
			System.err.println("NumberFormatException\t" + numberFormatException.getMessage());
		} catch (ParseException parseException) {
			System.err.println("ParseException\t" + parseException.getMessage());
		}
		if (!buffer.isEmpty()) {
			trips.add(buffer.get(buffer.size() - 1));
			tstrips.add(tbuffer.get(buffer.size() - 1));

		}

		return new Obs(
				Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""),
				Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));
	}

	/**
	 *
	 * This is the exact implementation of the algorithm provided in AllAboard
	 * paper
	 *
	 * In this implementation we are using the last point in the stop sequences
	 * for the destination and the first point in the historical sub-sequence as
	 * the origin of the new trip.
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs algorithm2_2(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * Stops sets and time stamps for these stops
		 */
		ArrayList<String> trips = new ArrayList<>();
		ArrayList<String> tstrips = new ArrayList<>();

		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;
		for (int i = 0; i < towers.length; i++) {
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			buffer.add(towers[i]);
			tbuffer.add(tstamps[i]);

			if (max_distance > dist_th) {

				try {
					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					java.util.Date sTime = formatter.parse(tbuffer.get(0));
					java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
					cal.setTime(sTime);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

					if (time_diff > time_th) {

						if (trips.isEmpty()) {
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
						} else {
							trips.add(buffer.get(0));
							tstrips.add(tbuffer.get(0));
							trips.add(RLM);
							tstrips.add(RLM);
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
						}
						/**
						 * Reset buffers.
						 */
						buffer = new ArrayList<>();
						tbuffer = new ArrayList<>();
						/**
						 * Reset maximum distances
						 */
						max_distance = 0;

					} else {

						/**
						 * Add the first observation as the origin of the first
						 * trips and the remaining part of the buffer as the
						 * traveling observations, else add the complete buffer
						 * elements as the observation seq of the traveling
						 * observables.
						 */
						trips.addAll(buffer);
						tstrips.addAll(tbuffer);

						buffer = new ArrayList<>();
						tbuffer = new ArrayList<>();
						max_distance = 0;
					}
				} catch (ParseException parseException) {
					System.err.println("ParseException\t" + parseException.getMessage());
				}

			}

		}

		if (!buffer.isEmpty()) {
			trips.add(buffer.get(0));
			tstrips.add(tbuffer.get(0));

		}

		// System.out.println("stops:\t" + Arrays.toString(trips.toArray(new
		// String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		// System.out.println("time stamps:\t" +
		// Arrays.toString(tstrips.toArray(new
		// String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		return new Obs(
				Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""),
				Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));

	}

	/**
	 * Date: Tue Jun 20 13:01:53 EET 2017
	 * 
	 * This is the exact implementation of the algorithm provided in AllAboard
	 * papr
	 *
	 * In this implementation we are using the last point in the stop sequences
	 * for the destination and the first point in the historical sub-sequence as
	 * the origin of the new trip.
	 *
	 * Duration = a.time - first.time
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs algorithm2_3(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * Stops sets and time stamps for these stops
		 */
		ArrayList<String> trips = new ArrayList<>();
		ArrayList<String> tstrips = new ArrayList<>();

		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;
		for (int i = 0; i < towers.length; i++) {
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			// buffer.add(towers[i]);
			// tbuffer.add(tstamps[i]);
			if (max_distance > dist_th) {

				try {
					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					java.util.Date sTime = formatter.parse(tbuffer.get(0));
					java.util.Date eTime = formatter.parse(tstamps[i]);
					cal.setTime(sTime);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

					if (time_diff >= time_th) {

						if (trips.isEmpty()) {
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
						} else {
							trips.add(buffer.get(0));
							tstrips.add(tbuffer.get(0));
							trips.add(RLM);
							tstrips.add(RLM);
							trips.add(buffer.get(buffer.size() - 1));
							tstrips.add(tbuffer.get(buffer.size() - 1));
						}
						/**
						 * Reset buffers.
						 */
						buffer = new ArrayList<>();
						tbuffer = new ArrayList<>();
						/**
						 * Reset maximum distances
						 */
						max_distance = 0;

					} else {

						/**
						 * Add the first observation as the origin of the first
						 * trips and the remaining part of the buffer as the
						 * traveling observations, else add the complete buffer
						 * elements as the observation seq of the traveling
						 * observables.
						 */
						trips.addAll(buffer);
						tstrips.addAll(tbuffer);

						buffer = new ArrayList<>();
						tbuffer = new ArrayList<>();
						max_distance = 0;
					}
				} catch (ParseException parseException) {
					System.err.println("ParseException\t" + parseException.getMessage());
				}

			}
			buffer.add(towers[i]);
			tbuffer.add(tstamps[i]);

		}

		if (!buffer.isEmpty()) {
			trips.add(buffer.get(0));
			tstrips.add(tbuffer.get(0));

		}

		// System.out.println("stops:\t" + Arrays.toString(trips.toArray(new
		// String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		// System.out.println("time stamps:\t" +
		// Arrays.toString(tstrips.toArray(new
		// String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		return new Obs(
				Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""),
				Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));

	}

	/**
	 * Date: Wed 21 Jun 13:38:29 EET 2017
	 * 
	 * This is the exact implementation of the algorithm provided in AllAboard
	 * paper
	 *
	 * In this implementation we are using the last point in the stop sequences
	 * for the destination and the first point in the historical sub-sequence as
	 * the origin of the new trip.
	 *
	 * Duration: a.time -first.time Sliding window
	 * 
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs algorithm2_5(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * Stops sets and time stamps for these stops
		 */
		ArrayList<String> trips = new ArrayList<>();
		ArrayList<String> tstrips = new ArrayList<>();

		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;

		for (int i = 0; i < towers.length;) {
			boolean flag = true;
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			// buffer.add(towers[i]);
			// tbuffer.add(tstamps[i]);
			if (max_distance > dist_th) {
				java.util.Date sTime;
				java.util.Date eTime;
				flag = false;
				try {
					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					sTime = formatter.parse(tbuffer.get(0));
					eTime = formatter.parse(tstamps[i]);

					cal.setTime(sTime);

					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);
				} catch (ParseException parseException) {
					System.err.println("ParseException\t" + parseException.getMessage());
				}

				if (time_diff >= time_th) {
					flag = true;
					if (trips.isEmpty()) {
						trips.add(buffer.get(buffer.size() - 1));
						tstrips.add(tbuffer.get(buffer.size() - 1));
					} else {
						trips.add(buffer.get(0));
						tstrips.add(tbuffer.get(0));
						trips.add(RLM);
						tstrips.add(RLM);
						trips.add(buffer.get(buffer.size() - 1));
						tstrips.add(tbuffer.get(buffer.size() - 1));
					}
					/**
					 * Reset buffers.
					 */
					buffer = new ArrayList<>();
					tbuffer = new ArrayList<>();
					/**
					 * Reset maximum distances
					 */
					max_distance = 0;

				} else {

					/**
					 * Add the first observation as the origin of the first
					 * trips and the remaining part of the buffer as the
					 * traveling observations, else add the complete buffer
					 * elements as the observation seq of the traveling
					 * observables.
					 */
					trips.add(buffer.get(0));
					tstrips.add(tbuffer.get(0));

					buffer.remove(0);
					tbuffer.remove(0);

					// i--; // to keep a as it is.

					// buffer = new ArrayList<>();
					// tbuffer = new ArrayList<>();
					max_distance = 0;
				}

			}

			if (flag) {
				buffer.add(towers[i]);
				tbuffer.add(tstamps[i]);
				i++;
			}
		}

		if (!buffer.isEmpty()) {
			trips.add(buffer.get(0));
			tstrips.add(tbuffer.get(0));

		}

		// System.out.println("stops:\t" + Arrays.toString(trips.toArray(new
		// String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		// System.out.println("time stamps:\t" +
		// Arrays.toString(tstrips.toArray(new
		// String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		return new Obs(
				Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""),
				Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));

	}

	/**
	 * Date: Tue Jun 20 13:01:53 EET 2017
	 * 
	 * This is the exact implementation of the algorithm provided in AllAboard
	 * paper
	 *
	 * In this implementation we are using the last point in the stop sequences
	 * for the destination and the first point in the historical sub-sequence as
	 * the origin of the new trip.
	 *
	 * Sliding window Duration: last.time - first.time
	 * 
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs algorithm2_4(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * Stops sets and time stamps for these stops
		 */
		ArrayList<String> trips = new ArrayList<>();
		ArrayList<String> tstrips = new ArrayList<>();

		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;

		for (int i = 0; i < towers.length;) {
			boolean flag = true;
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			// buffer.add(towers[i]);
			// tbuffer.add(tstamps[i]);
			if (max_distance > dist_th) {
				java.util.Date sTime;
				java.util.Date eTime;
				flag = false;
				try {
					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					sTime = formatter.parse(tbuffer.get(0));
					eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));

					cal.setTime(sTime);

					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);
				} catch (ParseException parseException) {
					System.err.println("ParseException\t" + parseException.getMessage());
				}

				if (time_diff >= time_th) {
					flag = true;
					if (trips.isEmpty()) {
						trips.add(buffer.get(buffer.size() - 1));
						tstrips.add(tbuffer.get(buffer.size() - 1));
					} else {
						trips.add(buffer.get(0));
						tstrips.add(tbuffer.get(0));
						trips.add(RLM);
						tstrips.add(RLM);
						trips.add(buffer.get(buffer.size() - 1));
						tstrips.add(tbuffer.get(buffer.size() - 1));
					}
					/**
					 * Reset buffers.
					 */
					buffer = new ArrayList<>();
					tbuffer = new ArrayList<>();
					/**
					 * Reset maximum distances
					 */
					max_distance = 0;

				} else {

					/**
					 * Add the first observation as the origin of the first
					 * trips and the remaining part of the buffer as the
					 * traveling observations, else add the complete buffer
					 * elements as the observation seq of the traveling
					 * observables.
					 */
					trips.add(buffer.get(0));
					tstrips.add(tbuffer.get(0));

					buffer.remove(0);
					tbuffer.remove(0);

					// i--; // to keep a as it is.

					// buffer = new ArrayList<>();
					// tbuffer = new ArrayList<>();
					max_distance = 0;
				}

			}

			if (flag) {
				buffer.add(towers[i]);
				tbuffer.add(tstamps[i]);
				i++;
			}
		}

		if (!buffer.isEmpty()) {
			trips.add(buffer.get(0));
			tstrips.add(tbuffer.get(0));

		}

		// System.out.println("stops:\t" + Arrays.toString(trips.toArray(new
		// String[trips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		// System.out.println("time stamps:\t" +
		// Arrays.toString(tstrips.toArray(new
		// String[tstrips.size()])).replaceAll(" ", "").replaceAll(CLM + RLM +
		// CLM, RLM).replace("[", "").replace("]", ""));
		return new Obs(
				Arrays.toString(trips.toArray(new String[trips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""),
				Arrays.toString(tstrips.toArray(new String[tstrips.size()])).replaceAll(" ", "")
						.replaceAll(CLM + RLM + CLM, RLM).replace("[", "").replace("]", ""));

	}

	public ArrayList<Integer> get_trips_length(Hashtable<String, Hashtable<Integer, Obs>> obs_stops) {

		ArrayList<Integer> al_length = new ArrayList<>();
		obs_stops.entrySet().stream().forEach(obs -> {
			obs.getValue().entrySet().stream().forEach(O -> {
				String trips[] = O.getValue().seq.split(RLM);
				int length[] = new int[trips.length];
				for (int i = 0; i < trips.length; i++) {
					if (trips[i].split(CLM).length > 2)
						al_length.add(trips[i].split(CLM).length);
				}
			});
		});

		//
		// String trips[]=O.seq.split(RLM);
		// int length[] = new int[trips.length];
		// for (int i =0; i<trips.length;i++) {
		// length[i]=trips[i].split(CLM).length;
		// }
		// return al_length.toArray(new int[al_length.size()]);
		return al_length;
	}

	public static void write_trips_length(ArrayList<Integer> lengths, String path) {

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));

			writer.write("states");
			// writer.newLine();

			for (int i = 0; i < lengths.size(); i++) {

				String ext_point = lengths.get(i).toString();
				writer.newLine();
				writer.write(ext_point);

			}
			// for (Iterator<String> it = exts.iterator(); it.hasNext();) {
			// String ext_point = it.next();
			// writer.newLine();
			// writer.write(ext_point);
			//// if (it.hasNext()) {
			//// writer.write(',');
			//// }
			// }

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
	 * Sun 07 Dec 2014 01:37:23 AM EET
	 *
	 * Build observations per days for each user from CDRs with time stamp for
	 * each observation.
	 *
	 * @param srcPath
	 * @param dstPath
	 * @param stower
	 * @param etower
	 * @return Hashtable<UserID, Hashtable<Week Day, Observation Sequence>>
	 *         obstable
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> buildObsDUT(String srcPath, int stower, int etower) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<String, Obs>> obstable = new Hashtable<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd_EEEE");
		int cnt = 0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(srcPath));

			String line;
			String id = null;
			String nextID;
			int nextDay = -1;
			String time = null;
			int curDay = -1;
			String curDate = "", date = "";
			String siteID;
			StringBuilder obsr = new StringBuilder();
			StringBuilder tObsr = new StringBuilder();
			/**
			 * Towers rang delimiter.
			 */
			char rlm = '/';

			/**
			 * Data must be sorted by the user ID Data set Arrana=ged as the
			 * following ID Time Site ID
			 */
			Hashtable<String, Obs> userObs = null;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();
				// if (Integer.parseInt(nextID) == 100) {
				// break;
				// }
				try {
					Date nextDate = parserSDF.parse(lineSplit[1]);
					Calendar cal = Calendar.getInstance();
					cal.setTime(nextDate);
					nextDay = cal.get(Calendar.DAY_OF_MONTH);

					date = dayFormat.format(cal.getTime());

					time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
							+ cal.get(Calendar.SECOND);

				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

				if (id == null) {
					userObs = new Hashtable<>();
					id = nextID;
					curDay = nextDay;
					curDate = date;
				}
				siteID = lineSplit[2].trim();
				int site = Integer.parseInt(siteID);

				/**
				 * If the Date changed insert date delimiter.
				 */
				if (curDay != nextDay) {
					// System.out.println("date changed");
					// if (obsr != null) {
					// obsr.append(dlm);
					//
					// }

					// if (obsr == null) {
					// obsr = new StringBuilder(RLM);
					// }
					if (obsr.length() != 0) {
						// cnt++;
						// System.out.println(obsr.toString());
						// System.out.println(tObsr.toString());
						// if (cnt == 100) {
						// System.exit(0);
						// }

						userObs.put(curDate, new Obs(obsr.toString(), tObsr.toString()));
						obsr = new StringBuilder();
						tObsr = new StringBuilder();
					}
					curDay = nextDay;
					curDate = date;
					// if (site >= stower && site <= etower) {
					// obsr = new StringBuilder(siteID);
					// } else {
					// obsr = new StringBuilder(RLM);
					// }
					// continue;
				}

				if (obsr.length() == 0 && site >= stower && site <= etower) {
					obsr.append(siteID);
					tObsr.append(time);
				} else if (obsr.length() != 0 && id.equals(nextID)) {

					/**
					 * Current observation in the current region
					 */
					if (site >= stower && site <= etower) {

						if (rlm != obsr.charAt(obsr.length() - 1)) {
							obsr.append(",");
							tObsr.append(",");
						}
						obsr.append(siteID);
						tObsr.append(time);
					} else if (rlm != obsr.charAt(obsr.length() - 1)) {
						// System.out.println(obsr.charAt(obsr.length() - 1));
						// obsr.append(",");
						obsr.append(rlm);
						tObsr.append(rlm);
					}

				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */
					if (!userObs.isEmpty()) {

						obstable.put(Integer.parseInt(id), userObs);
						userObs = new Hashtable<>();
					}
					id = nextID;
					obsr = new StringBuilder();
					tObsr = new StringBuilder();

				}

			}

			// System.out.println("File saved!");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return obstable;
	}

	/**
	 * Sat 25 Apr 2015 02:43:22 PM JST
	 *
	 * Build observations per days for each user from CDRs with time stamp for
	 * each observation for specific user ID.
	 *
	 * @param srcPath
	 * @param dstPath
	 * @param stower
	 * @param etower
	 * @return Hashtable<UserID, Hashtable<Week Day, Observation Sequence>>
	 *         obstable
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> buildObsDUT(String srcPath, int usr_id, int stower, int etower) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<String, Obs>> obstable = new Hashtable<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd_EEEE");

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(srcPath));

			String line;
			String id = null;
			String nextID;
			int nextDay = -1;
			String time = null;
			int curDay = -1;
			String curDate = "", date = "";
			String siteID;
			StringBuilder obsr = new StringBuilder();
			StringBuilder tObsr = new StringBuilder();
			/**
			 * Towers rang delimiter.
			 */
			char rlm = '/';

			/**
			 * Data must be sorted by the user ID Data set Arranged as the
			 * following ID Time Site ID
			 */
			Hashtable<String, Obs> userObs = null;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");
				nextID = lineSplit[0].trim();
				/**
				 * Skep other users that do not match the required one. Data are
				 * in ascending order, so keep scanning of the data file until a
				 * user with an ID greater than the required ID is monitored.
				 */
				if (Integer.parseInt(nextID) < usr_id) {
					continue;
				} else if (Integer.parseInt(nextID) > usr_id) {
					break;
				}

				// System.out.println("-->" + nextID);
				try {
					Date nextDate = parserSDF.parse(lineSplit[1]);
					Calendar cal = Calendar.getInstance();
					cal.setTime(nextDate);
					nextDay = cal.get(Calendar.DAY_OF_MONTH);

					date = dayFormat.format(cal.getTime());

					time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
							+ cal.get(Calendar.SECOND);

				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

				if (id == null) {
					userObs = new Hashtable<>();
					id = nextID;
					curDay = nextDay;
					curDate = date;
				}
				siteID = lineSplit[2].trim();
				int site = Integer.parseInt(siteID);

				/**
				 * If the Date changed insert date delimiter.
				 */
				if (curDay != nextDay) {
					// System.out.println("date changed");
					// if (obsr != null) {
					// obsr.append(dlm);
					// }

					// if (obsr == null) {
					// obsr = new StringBuilder(RLM);
					// }
					if (obsr.length() != 0) {
						userObs.put(curDate, new Obs(obsr.toString(), tObsr.toString()));
						obsr = new StringBuilder();
						tObsr = new StringBuilder();
					}
					curDay = nextDay;
					curDate = date;
					// if (site >= stower && site <= etower) {
					// obsr = new StringBuilder(siteID);
					// } else {
					// obsr = new StringBuilder(RLM);
					// }
					// continue;
				}

				if (obsr.length() == 0 && site >= stower && site <= etower) {
					obsr.append(siteID);
					tObsr.append(time);
				} else if (obsr.length() != 0 && id.equals(nextID)) {

					/**
					 * Current observation in the current region
					 */
					if (site >= stower && site <= etower) {

						if (rlm != obsr.charAt(obsr.length() - 1)) {
							obsr.append(",");
							tObsr.append(",");
						}
						obsr.append(siteID);
						tObsr.append(time);
					} else if (rlm != obsr.charAt(obsr.length() - 1)) {
						// System.out.println(obsr.charAt(obsr.length() - 1));
						// obsr.append(",");
						obsr.append(rlm);
						tObsr.append(rlm);
					}

				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */
					if (!userObs.isEmpty()) {
						obstable.put(Integer.parseInt(id), userObs);
						userObs = new Hashtable<>();
					}
					id = nextID;
					obsr = new StringBuilder();
					tObsr = new StringBuilder();

				}

			}

			/**
			 * Skipping other users disable users addition to the observation
			 * table on varying from one user to another user. So additional
			 * instructions is required to add usr_id data to the observation
			 * table after scanning the CDRs data file.
			 */
			if (obstable.isEmpty() && !userObs.isEmpty()) {
				obstable.put(usr_id, userObs);
			}

			// System.out.println("File saved!");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return obstable;
	}

	/**
	 * Tue 12 May 2015 04:42:35 PM JST
	 *
	 * Build observations per days for each user from CDRs with time stamp for
	 * each observation.
	 *
	 * For zones greater than range, continue adding zones in normal way.
	 *
	 * @param srcPath
	 * @param dstPath
	 * @param stower
	 * @param etower
	 * @return Hashtable<UserID, Hashtable<Week Day, Observation Sequence>>
	 *         obstable
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> buildObsDUT_stops(String srcPath, int stower, int etower) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<String, Obs>> obstable = new Hashtable<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd_EEEE");
		int cnt = 0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(srcPath));

			String line;
			String id = null;
			String nextID;
			int nextDay = -1;
			String time = null;
			int curDay = -1;
			String curDate = "", date = "";
			String siteID;
			StringBuilder obsr = new StringBuilder();
			StringBuilder tObsr = new StringBuilder();

			/**
			 * Data must be sorted by the user ID Data set Arrana=ged as the
			 * following ID Time Site ID
			 */
			Hashtable<String, Obs> userObs = null;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();
				// if (Integer.parseInt(nextID) == 20) {
				// break;
				// }
				try {
					Date nextDate = parserSDF.parse(lineSplit[1]);
					Calendar cal = Calendar.getInstance();
					cal.setTime(nextDate);
					nextDay = cal.get(Calendar.DAY_OF_MONTH);

					date = dayFormat.format(cal.getTime());

					time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
							+ cal.get(Calendar.SECOND);

				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

				if (id == null) {
					userObs = new Hashtable<>();
					id = nextID;
					curDay = nextDay;
					curDate = date;
				}
				siteID = lineSplit[2].trim();
				int site = Integer.parseInt(siteID);

				/**
				 * If the Date changed insert date delimiter.
				 */
				if (curDay != nextDay) {
					// System.out.println("date changed");
					// if (obsr != null) {
					// obsr.append(dlm);
					//
					// }

					// if (obsr == null) {
					// obsr = new StringBuilder(RLM);
					// }
					if (obsr.length() != 0) {
						// cnt++;
						// System.out.println(obsr.toString());
						// System.out.println(tObsr.toString());
						// if (cnt == 100) {
						// System.exit(0);
						// }

						userObs.put(curDate, new Obs(obsr.toString(), tObsr.toString()));
						obsr = new StringBuilder();
						tObsr = new StringBuilder();
					}
					curDay = nextDay;
					curDate = date;
					// if (site >= stower && site <= etower) {
					// obsr = new StringBuilder(siteID);
					// } else {
					// obsr = new StringBuilder(RLM);
					// }
					// continue;
				}

				if (obsr.length() == 0 && site >= stower && site <= etower) {
					obsr.append(siteID);
					tObsr.append(time);
				} else if (obsr.length() != 0 && id.equals(nextID)) {

					/**
					 * Current observation in the current region
					 */
					if (site >= stower && site <= etower) {

						if (RLM != String.valueOf(obsr.charAt(obsr.length() - 1))) {
							obsr.append(CLM);
							tObsr.append(CLM);
						}
						obsr.append(siteID);
						tObsr.append(time);
					}
					// else if (rlm != obsr.charAt(obsr.length() - 1)) {
					//// System.out.println(obsr.charAt(obsr.length() - 1));
					//// obsr.append(",");
					// obsr.append(rlm);
					// tObsr.append(rlm);
					// }

				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */
					if (!userObs.isEmpty()) {

						obstable.put(Integer.parseInt(id), userObs);
						userObs = new Hashtable<>();
					}
					id = nextID;
					obsr = new StringBuilder();
					tObsr = new StringBuilder();

				}

			}

			// System.out.println("File saved!");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return obstable;
	}

	public void buildObsSeq() {

		BufferedReader reader;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Observations");
			doc.appendChild(rootElement);

			reader = new BufferedReader(new FileReader(this.dSPath));

			String line;
			String id = null;
			String nextID;
			String siteID;
			StringBuilder obsr = null;

			/**
			 * Data must be sorted by the user ID Data set Arranged as the
			 * following ID Time Site ID
			 */
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();

				if (id == null) {
					id = nextID;
				}
				siteID = lineSplit[2].trim();

				if (obsr == null) {
					obsr = new StringBuilder(siteID);
				} else if (id.equals(nextID)) {
					obsr.append(",");
					obsr.append(siteID);
				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */

					Element userElement = doc.createElement("user");
					rootElement.appendChild(userElement);

					// set attribute to staff element
					Attr attr = doc.createAttribute("id");
					attr.setValue(id);
					userElement.setAttributeNode(attr);

					attr = doc.createAttribute("sitesSequanace");
					attr.setValue(obsr.toString());
					userElement.setAttributeNode(attr);

					id = nextID;
					obsr = new StringBuilder(siteID);

				}

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(this.xmlPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}

	/**
	 * Mon 01 Dec 2014 11:37:23 PM EET
	 *
	 * Build observations per days for each user from CDRs.
	 *
	 * @param srcPath
	 * @param dstPath
	 * @param stower
	 * @param etower
	 * @return
	 */
	public Hashtable<Integer, Hashtable<Integer, String>> buildObsTU(String srcPath, int stower, int etower) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<Integer, String>> obstable = new Hashtable<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(srcPath));

			String line;
			String id = null;
			String nextID;
			int nextDay = -1;
			int curDay = -1;
			String siteID;
			StringBuilder obsr = new StringBuilder();

			/**
			 * Towers rang delimiter.
			 */
			char rlm = '/';
			/**
			 * Date delimiter.
			 */
			char dlm = '$';

			/**
			 * Data must be sorted by the user ID Data set Arrana=ged as the
			 * following ID Time Site ID
			 */
			Hashtable<Integer, String> userObs = null;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();
				// if (Integer.parseInt(nextID) == 50) {
				// break;
				// }
				try {
					Date nextDate = parserSDF.parse(lineSplit[1]);
					Calendar cal = Calendar.getInstance();
					cal.setTime(nextDate);
					nextDay = cal.get(Calendar.DAY_OF_MONTH);
				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

				if (id == null) {
					userObs = new Hashtable<>();
					id = nextID;
					curDay = nextDay;
				}
				siteID = lineSplit[2].trim();
				int site = Integer.parseInt(siteID);

				/**
				 * If the Date changed insert date delimiter.
				 */
				if (curDay != nextDay) {
					// System.out.println("date changed");
					// if (obsr != null) {
					// obsr.append(dlm);
					//
					// }

					// if (obsr == null) {
					// obsr = new StringBuilder(RLM);
					// }
					if (obsr.length() != 0) {
						userObs.put(curDay, obsr.toString());
						obsr = new StringBuilder();
					}
					curDay = nextDay;

					// if (site >= stower && site <= etower) {
					// obsr = new StringBuilder(siteID);
					// } else {
					// obsr = new StringBuilder(RLM);
					// }
					// continue;
				}

				if (obsr.length() == 0 && site >= stower && site <= etower) {
					obsr.append(siteID);
				} else if (obsr.length() != 0 && id.equals(nextID)) {

					/**
					 * Current observation in the current region
					 */
					if (site >= stower && site <= etower) {

						if (rlm != obsr.charAt(obsr.length() - 1)) {
							obsr.append(",");
						}
						obsr.append(siteID);
					} else if (rlm != obsr.charAt(obsr.length() - 1)) {
						// System.out.println(obsr.charAt(obsr.length() - 1));
						// obsr.append(",");
						obsr.append(rlm);
					}

				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */
					if (!userObs.isEmpty()) {
						obstable.put(Integer.parseInt(id), userObs);
						userObs = new Hashtable<>();
					}
					id = nextID;
					obsr = new StringBuilder();

				}

			}

			// System.out.println("File saved!");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return obstable;
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
	 * filter similar observation only if time difference is less than x minutes
	 *
	 * @param obs
	 * @param minutes
	 * @param trip
	 *            length
	 * @return
	 */
	public Obs filter(Obs obs, int time_th, int L) {
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String seq = obs.getSeq();
		String stamps = obs.getTimeStamp();

		String fseq = "";
		String fstamps = "";

		String[] towerSubseqs = new String[] { seq };
		String[] stampSubseqs = new String[] { stamps };
		if (seq.contains(RLM)) {
			towerSubseqs = seq.split(RLM);
			stampSubseqs = stamps.split(RLM);
		}

		for (int i = 0; i < towerSubseqs.length; i++) {

			String[] towers = towerSubseqs[i].trim().split(CLM);
			String[] tstamps = stampSubseqs[i].trim().split(CLM);

			// System.out.println(towers.length + "\t" +
			// Arrays.toString(towers));
			/**
			 * If the trip length less than specific length ignore it.
			 */
			if (towers.length < L) {
				// System.out.println("towers.length <= L");
				towerSubseqs[i] = "-";
				stampSubseqs[i] = "-";
				continue;
			}
			boolean[] marks = new boolean[towers.length];

			for (int j = 1; j < towers.length; j++) {
				try {

					if (!towers[j - 1].equals(towers[j])) {
						// System.out.println("! " + towers[j - 1] + " equals( "
						// + towers[j] + " )");
						marks[j] = true;
						continue;
					}

					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					Date sTime = formatter.parse(tstamps[j - 1]);
					Date eTime = formatter.parse(tstamps[j]);
					cal.setTime(sTime);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					int diff = (ehour - hour) * HOUR + (eminute - minute);
					/**
					 * check time difference with time threshold whatever the
					 * distance between the starting tower
					 */
					if (diff < time_th) {
						// System.out.println("diff < time_th");
						marks[j] = false;
					}
				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			/**
			 * construct sequences from towers: <\n If the length of the trinp
			 * after removing the repeated values less than L ignore it else
			 * construct a trip from the non-removed observations.>
			 */
			// System.out.println("length\t" + length);
			// if (length <= L) {
			// System.out.println("length <= L\t" + length);
			// towerSubseqs[i] = "-";
			// stampSubseqs[i] = "-";
			// } else {

			String tmpObs = "";
			String tmpTime = "";

			for (int j = 0; j < marks.length; j++) {
				boolean mark = marks[j];
				if (mark) {
					if (!tmpObs.isEmpty()) {
						tmpObs += CLM;
						tmpTime += CLM;
					}
					tmpObs += towers[j];
					tmpTime += tstamps[j];
				}
			}
			// System.out.println("last \t" + tmpObs.split(CLM).length + "\t" +
			// Arrays.toString(tmpObs.split(CLM)));
			if (tmpObs.split(CLM).length > L) {
				towerSubseqs[i] = tmpObs;
				stampSubseqs[i] = tmpTime;
			}

			// }
		}
		/**
		 * Construct trips
		 */
		for (int i = 0; i < towerSubseqs.length; i++) {
			if (!towerSubseqs[i].equals("-")) {
				if (!fseq.isEmpty()) {
					fseq += RLM;
					fstamps += RLM;
				}

				fseq += towerSubseqs[i];
				fstamps += stampSubseqs[i];
			}
		}

		return new Obs(fseq, fstamps);
	}

	/**
	 * Thu 07 May 2015 03:07:46 AM JST
	 *
	 * filter trips from similar observation only if time difference is less
	 * than x minutes
	 *
	 * WRONG DECISION --> repeated observation must completely filtered our as
	 * handovers as well.
	 *
	 * @param trips
	 * @param f_time_th
	 * @param trip_length
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> filterTrips(Hashtable<Integer, Hashtable<String, Obs>> trips,
			int f_time_th, int trip_length) {
		for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : trips.entrySet()) {
			Integer userID = entrySet.getKey();
			Hashtable<String, Obs> value = entrySet.getValue();
			Hashtable<String, Obs> tmpObs = new Hashtable<>();
			for (Map.Entry<String, Obs> obsentry : value.entrySet()) {
				String dayID = obsentry.getKey();
				Obs obs = filter(obsentry.getValue(), f_time_th, trip_length);

				/**
				 * Check the seq for containing valid towers
				 */
				String tmp = obs.getSeq().replace(RLM, CLM).replace("-" + CLM, "").replace("-", "").trim();

				if (!tmp.isEmpty()) {
					// System.out.println(">>>" + tmp);
					// value.replace(dayID, obs);
					tmpObs.put(dayID, obs);
				}
				// else {
				// value.remove(dayID);
				// }
			}
			trips.replace(userID, tmpObs);

		}
		return trips;
	}

	/**
	 *
	 * Identify the stop points
	 *
	 * This is the exact implementation of the algorithm provided in AllAboard
	 * paper
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs findStops(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * Stops sets and time stamps for these stops
		 */
		String stops = "";
		String tstops = "";

		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;
		for (int i = 0; i < towers.length; i++) {
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			buffer.add(towers[i]);
			tbuffer.add(tstamps[i]);

			if (max_distance > dist_th) {

				try {
					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					java.util.Date sTime = formatter.parse(tbuffer.get(0));
					java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
					cal.setTime(sTime);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

					if (time_diff > time_th) {
						/**
						 * Add buffer mode to the stops
						 */
						int index = modeIndex(buffer);
						if (index != -1) {
							// System.out.println("Find stop");
							if (!stops.isEmpty()) {
								stops += CLM;
								tstops += CLM;
							}
							stops += buffer.get(index);
							tstops += tbuffer.get(index);

						}
					}
					// else {
					buffer = new ArrayList<>();
					tbuffer = new ArrayList<>();
					/**
					 * Reset maximum distances
					 */
					max_distance = 0;
					// }
				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

			}

		}

		if (!buffer.isEmpty()) {
			/**
			 * Add buffer mode to the stops
			 */
			int index = modeIndex(buffer);
			if (index != -1) {
				// System.out.println("Find from the remaining buffer");
				if (!stops.isEmpty()) {
					stops += CLM;
					tstops += CLM;
				}
				stops += buffer.get(index);
				tstops += tbuffer.get(index);

			}

		}

		// System.out.println("stops:\t" + stops);
		// System.out.println("time stamps:\t" + tstops);
		return new Obs(stops, tstops);

	}

	/**
	 * Identify trips using stop buffers.
	 *
	 * Generate ODs not trips and it is an exact implementation of findtrips
	 * functions. Don't use it for HMM model.
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs findStops_trips(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY)
			throws ParseException {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		/**
		 * Stops sets and time stamps for these stops
		 */
		String stops = towers[0];
		String tstops = tstamps[0];

		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;
		for (int i = 0; i < towers.length; i++) {
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			buffer.add(towers[i]);
			tbuffer.add(tstamps[i]);

			if (max_distance > dist_th) {

				/**
				 * if the time exceeds timing threshold, then check the distance
				 * between towers. If this distance less than the distance
				 * threshold, then previous tower is the end of the current
				 * trip.
				 *
				 */
				java.util.Date sTime = formatter.parse(tbuffer.get(0));
				java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
				cal.setTime(sTime);
				int hour = cal.get(Calendar.HOUR);
				int minute = cal.get(Calendar.MINUTE);

				cal.setTime(eTime);
				int ehour = cal.get(Calendar.HOUR);
				int eminute = cal.get(Calendar.MINUTE);

				time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

				if (time_diff > time_th) {
					// if (buffer.size() >= trip_length) {
					if (!stops.isEmpty()) {
						stops += CLM;
						tstops += CLM;
					}
					/**
					 * Add start and end of the trips to the stop sequences
					 */
					stops += buffer.get(buffer.size() - 1);
					tstops += tbuffer.get(tbuffer.size() - 1);

					// }
				}
				// else {
				buffer = new ArrayList<>();
				tbuffer = new ArrayList<>();
				/**
				 * Reset maximum distances
				 */
				max_distance = 0;
				// }

			}

		}

		if (!buffer.isEmpty()) {
			// if (buffer.size() >= trip_length) {
			if (!stops.isEmpty()) {
				stops += CLM;
				tstops += CLM;
			}
			/**
			 * Add start and end of the trips to the stop sequences
			 */
			stops += buffer.get(buffer.size() - 1);
			tstops += tbuffer.get(tbuffer.size() - 1);

			// }
		}

		// System.out.println("stops:\t" + stops);
		// System.out.println("time stamps:\t" + tstops);
		return new Obs(stops, tstops);

	}

	/**
	 * Identify trips using stop buffers.
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 * @throws ParseException
	 */
	public Obs findtrips(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY) {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();

		boolean marks[] = new boolean[towers.length];
		/**
		 * Buffers: <\n buffer holds sequence of observations that did not meet
		 * buffer clearance criterias.> <\n tbuffer holds time stamps values
		 * corresponding to those in the buffer.>
		 */
		ArrayList<String> buffer = new ArrayList<>();
		ArrayList<String> tbuffer = new ArrayList<>();

		double max_distance = 0;
		int time_diff = 0;
		for (int i = 0; i < towers.length; i++) {
			Vertex a = towersXY.get(Integer.parseInt(towers[i]));
			for (int j = 0; j < buffer.size(); j++) {
				Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
				// System.out.println("b"+Integer.parseInt(buffer.get(j)));
				double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
				if (tmp_distance > max_distance) {
					max_distance = tmp_distance;
				}

			}

			buffer.add(towers[i]);
			tbuffer.add(tstamps[i]);

			if (max_distance > dist_th) {

				try {
					/**
					 * if the time exceeds timing threshold, then check the
					 * distance between towers. If this distance less than the
					 * distance threshold, then previous tower is the end of the
					 * current trip.
					 *
					 */
					java.util.Date sTime = formatter.parse(tbuffer.get(0));
					java.util.Date eTime = formatter.parse(tbuffer.get(tbuffer.size() - 1));
					cal.setTime(sTime);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);

					cal.setTime(eTime);
					int ehour = cal.get(Calendar.HOUR);
					int eminute = cal.get(Calendar.MINUTE);

					time_diff = Math.abs((ehour - hour)) * HOUR + (eminute - minute);

					if (time_diff > time_th) {
						marks[i] = true;

					}
					// else {
					buffer = new ArrayList<>();
					tbuffer = new ArrayList<>();
					/**
					 * Reset maximum distances
					 */
					max_distance = 0;
					// }
				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

			}

		}

		if (!buffer.isEmpty()) {
			marks[marks.length - 1] = true;
		}

		/**
		 * User trips buffers.
		 */
		String trips = towers[0];
		String tstrips = tstamps[0];

		for (int i = 1; i < marks.length; i++) {
			boolean mark = marks[i];
			trips += CLM + towers[i];
			tstrips += CLM + tstamps[i];

			/**
			 * The end of the previous trip is the start of the new trip.
			 */
			if (mark && i != marks.length - 1) {
				trips += RLM + towers[i];
				tstrips += RLM + tstamps[i];
			}

		}
		return new Obs(trips, tstrips);

	}

	/**
	 * Find trips from single sequence based on time stamps and towers
	 * positions.
	 *
	 * @param towers
	 * @param tstamps
	 * @param towersXY
	 * @param dist_th
	 * @param time_th
	 * @return
	 */
	private Obs findTrips(String[] towers, String[] tstamps, Hashtable<Integer, Vertex> towersXY, int dist_th,
			int time_th) {
		/**
		 * Marks array contain index of the towers that represent the starting
		 * observation of a new trip.
		 */
		boolean[] marks = new boolean[tstamps.length];
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		/**
		 * add 0 as the start of the first tower in this seq.
		 */
		// int mIndex = 0;
		// marks[mIndex++] = 0;

		int current = 0;
		for (int i = 1; i < tstamps.length; i++) {
			try {
				String tstamp = tstamps[i];

				/**
				 * if the time exceeds timing threshold, then check the distance
				 * between towers. If this distance less than the distance
				 * threshold, then previous tower is the end of the current
				 * trip.
				 *
				 */
				Date sTime = formatter.parse(tstamps[current]);
				Date eTime = formatter.parse(tstamp);
				cal.setTime(sTime);
				int hour = cal.get(Calendar.HOUR);
				int minute = cal.get(Calendar.MINUTE);

				cal.setTime(eTime);
				int ehour = cal.get(Calendar.HOUR);
				int eminute = cal.get(Calendar.MINUTE);

				int diff = (ehour - hour) * HOUR + (eminute - minute);
				/**
				 * check time difference with time threshold whatever the
				 * distance between the starting tower
				 */
				if (diff > time_th) {
					/**
					 * Check distance, if it distance less than distance
					 * threshold mark the current tower as the start of a new
					 * trip.
					 */
					Vertex sTower = towersXY.get(Integer.parseInt(towers[i - 1]));
					Vertex tower = towersXY.get(Integer.parseInt(towers[i]));

					if (eculidean(sTower.getX(), tower.getX(), sTower.getY(), tower.getY()) < dist_th) {
						/**
						 * Update the trip sequences
						 */
						marks[i] = true;
						current = i;
					}

				}
			} catch (ParseException ex) {
				Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		/**
		 * construct observations and time stamps
		 */
		String time = "";
		String obs = "";
		// time += tstamps[0];
		// obs += towers[0];
		for (int i = 0; i < marks.length; i++) {
			if (marks[i]) {

				time += RLM + tstamps[i];
				obs += RLM + towers[i];
			} else {
				// if (towers[i].equals(towers[i - 1])) {
				// System.out.println(towers[i] + "\t=\t" + towers[i - 1]);
				// continue;
				// }
				/**
				 * Add comma separators
				 */
				if (!time.isEmpty()) {
					time += CLM;
					obs += CLM;
				}
				time += tstamps[i];
				obs += towers[i];
			}

		}
		return new Obs(obs, time);
	}

	/**
	 * extract only a sample of user observation that fall within a specific
	 * range of a towers .
	 *
	 * @param obs
	 * @param stwr
	 * @param etwr
	 * @param sample_size
	 * @return
	 */
	public Hashtable<String, Hashtable<Integer, String>> get_usr_sample(Hashtable<String, Hashtable<Integer, Obs>> obs,
			int stwr, int etwr, int sample_size) {
		Hashtable<String, Hashtable<Integer, String>> sample = new Hashtable<>();

		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			boolean flag = true;
			Hashtable<Integer, String> ds = new Hashtable<>();
			for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
				Integer key1 = entrySet1.getKey();
				String[] obs_str = entrySet1.getValue().seq.split(CLM);
				for (int i = 0; i < obs_str.length; i++) {
					int t = Integer.parseInt(obs_str[i]);
					if (!(t > stwr && t < etwr)) {
						flag = false;
						break;
					}

				}
				if (flag) {
					// add to the sample ..
					ds.put(key1, entrySet1.getValue().seq);
				}

			}
			if (!ds.isEmpty()) {
				sample.put(key, ds);
			}

		}
		return sample;
	}

	private int get_week_no(String path) {
		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String lineSplit[] = line.split(CLM);
				cal.setTime(parserSDF.parse(lineSplit[1]));
				return cal.get(Calendar.WEEK_OF_YEAR);
			}
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParseException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return -1;
	}

	public String getdSPath() {
		return dSPath;
	}

	public String getXmlPath() {
		return xmlPath;
	}

	/**
	 * Get the median index of an array list
	 *
	 * @param list
	 * @return
	 */
	public int modeIndex(ArrayList<String> list) {
		int index = -1;
		/**
		 * If the list is empty return -1 as the median index, else if list size
		 * is odd, make index it equal size/2+1 else make it equal size/2.
		 */
		if (!list.isEmpty()) {
			list.trimToSize();
			int size = list.size();
			if (size % 2 != 0) {
				index = size / 2;
			} else {
				index = size / 2 - 1;
			}
		}
		return index;
	}

	/**
	 * parse only distinct observations not all patterns exclude from the
	 * observations can significantly determine users movement patterns
	 */
	public void obtainDisObsr() {

		BufferedReader reader;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Observations");
			doc.appendChild(rootElement);

			reader = new BufferedReader(new FileReader(this.dSPath));

			String line;
			String id = null;
			String nextID;
			String siteID;
			String siteID_Old = null;
			StringBuilder obsr = null;

			/**
			 * Data must be sorted by the user ID Data set Arrana=ged as the
			 * following ID Time Site ID
			 */
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();

				if (id == null) {
					id = nextID;
				}
				siteID = lineSplit[2].trim();

				if (siteID_Old == null) {
					siteID_Old = siteID;
				} else if (siteID.equals(siteID_Old) && id.equals(nextID)) {
					// System.out.println("same id");
					continue;
				} else if (!siteID.equals(siteID_Old)) {
					siteID_Old = siteID;
				}

				if (obsr == null) {
					obsr = new StringBuilder(siteID);
				} else if (id.equals(nextID)) {
					obsr.append(",");
					obsr.append(siteID);
				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */

					Element userElement = doc.createElement("user");
					rootElement.appendChild(userElement);

					// set attribute to staff element
					Attr attr = doc.createAttribute("id");
					attr.setValue(id);
					userElement.setAttributeNode(attr);

					attr = doc.createAttribute("sitesSequanace");
					attr.setValue(obsr.toString());
					userElement.setAttributeNode(attr);

					id = nextID;
					obsr = new StringBuilder(siteID);

				}

			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(this.xmlPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}

	/**
	 * Thu Sep 17 09:30:16 JST 2015
	 *
	 * Get a list of all visited points regardless of the timestamp of each
	 * point. Only separate data by users, workingday or weekend. this data will
	 * be used to test the TraClus trajectory mining method ..
	 *
	 *
	 * @param srcPath
	 * @param stower
	 * @param etower
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> read_obs_data(String srcPath) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<String, Obs>> obstable = new Hashtable<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd_EEEE");
		int cnt = 0;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(srcPath));

			String line;
			String id = null;
			String nextID;
			int nextDay = -1;
			String time = null;
			int curDay = -1;
			String curDate = "", date = "";
			String siteID;
			StringBuilder obsr = new StringBuilder();
			StringBuilder tObsr = new StringBuilder();

			/**
			 * Data must be sorted by the user ID Data set Arrana=ged as the
			 * following ID Time Site ID
			 */
			Hashtable<String, Obs> userObs = null;
			while ((line = reader.readLine()) != null) {
				String lineSplit[] = line.split(",");

				nextID = lineSplit[0].trim();
				// if (Integer.parseInt(nextID) == 4) {
				// break;
				// }
				try {
					Date nextDate = parserSDF.parse(lineSplit[1]);
					Calendar cal = Calendar.getInstance();
					cal.setTime(nextDate);
					nextDay = cal.get(Calendar.DAY_OF_MONTH);

					date = dayFormat.format(cal.getTime());

					time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
							+ cal.get(Calendar.SECOND);

				} catch (ParseException ex) {
					Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
				}

				if (id == null) {
					userObs = new Hashtable<>();
					id = nextID;
					curDay = nextDay;
					curDate = date;
				}
				siteID = lineSplit[2].trim();
				int site = Integer.parseInt(siteID);

				/**
				 * If the Date changed insert date delimiter.
				 */
				if (curDay != nextDay) {
					// System.out.println("date changed");
					// if (obsr != null) {
					// obsr.append(dlm);
					//
					// }

					// if (obsr == null) {
					// obsr = new StringBuilder(RLM);
					// }
					if (obsr.length() != 0) {
						// cnt++;
						// System.out.println(obsr.toString());
						// System.out.println(tObsr.toString());
						// if (cnt == 100) {
						// System.exit(0);
						// }

						userObs.put(curDate, new Obs(obsr.toString(), tObsr.toString()));
						obsr = new StringBuilder();
						tObsr = new StringBuilder();
					}
					curDay = nextDay;
					curDate = date;
					// if (site >= stower && site <= etower) {
					// obsr = new StringBuilder(siteID);
					// } else {
					// obsr = new StringBuilder(RLM);
					// }
					// continue;
				}

				if (obsr.length() == 0) {
					obsr.append(siteID);
					tObsr.append(time);
				} else if (obsr.length() != 0 && id.equals(nextID)) {
					obsr.append(CLM);
					tObsr.append(CLM);

					obsr.append(siteID);
					tObsr.append(time);

				} else {
					/**
					 * User changed write the XL file Data Change the user ID ,
					 * and record site with the new user
					 */
					if (!userObs.isEmpty()) {

						obstable.put(Integer.parseInt(id), userObs);
						userObs = new Hashtable<>();
					}
					id = nextID;
					obsr = new StringBuilder();
					tObsr = new StringBuilder();

				}

			}

			// System.out.println("File saved!");
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return obstable;
	}

	/**
	 * Get the sequences of observations with a specific range of towers ...
	 *
	 * @param dataset_file
	 * @param stwr
	 * @param etwr
	 * @return
	 */
	public Hashtable<String, Hashtable<Integer, Obs>> read_obs_data(String dataset_file, int stwr, int etwr) {
		Hashtable<String, Hashtable<Integer, Obs>> obs_table = transposeWUT(
				remove_repeated(remove_handovers(buildObsDUT_stops(dataset_file, stwr, etwr))));
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
				Integer key1 = entrySet1.getKey();
				String obs_str = entrySet1.getValue().seq.replaceAll(RLM, CLM);
				String t_obs_str = entrySet1.getValue().timeStamp.replaceAll(RLM, CLM);
				Obs value1 = new Obs(obs_str, t_obs_str);
				value.replace(key1, value1);
			}
			obs_table.replace(key, value);
		}
		return obs_table;
	}

	public Hashtable<Integer, Hashtable<Integer, String>> readObsTU(String path) {
		Hashtable<Integer, Hashtable<Integer, String>> table = new Hashtable<>();
		org.w3c.dom.Document dpDoc;
		// for (int zone = 1; zone < 300; zone++) {
		// System.out.println("zone:\t"+zone);
		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList daysList = dpDoc.getElementsByTagName("week_day");

			for (int i = 0; i < daysList.getLength(); i++) {

				Node dayNode = daysList.item(i);

				if (dayNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) dayNode;
					int day = Integer.parseInt(eElement.getAttribute("id"));

					ToNode edge;
					NodeList toNodeList = eElement.getElementsByTagName("user");
					Hashtable<Integer, String> usrsTable = new Hashtable<>();
					for (int j = 0; j < toNodeList.getLength(); j++) {
						Node toNodeNode = toNodeList.item(j);
						if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
							Element usrElement = (Element) toNodeNode;
							usrsTable.put(Integer.parseInt(usrElement.getAttribute("id")),
									usrElement.getAttribute("seq"));
						}
					}
					table.put(day, usrsTable);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}

		return table;
	}

	/**
	 * remove observations with the same time_stamp.
	 *
	 * @param obs
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> remove_handovers(Hashtable<Integer, Hashtable<String, Obs>> obs) {
		Hashtable<Integer, Hashtable<String, Obs>> tmp_obs = new Hashtable<>();

		for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : obs.entrySet()) {
			Integer usr_key = entrySet.getKey();
			Hashtable<String, Obs> obs_raw_data = entrySet.getValue();
			Hashtable<String, Obs> tmp_obs_raw_data = new Hashtable<>();
			for (Map.Entry<String, Obs> obs_raw_entrySet : obs_raw_data.entrySet()) {

				String day_key = obs_raw_entrySet.getKey();
				Obs day_val = obs_raw_entrySet.getValue();

				String[] twrs = day_val.getSeq().split(CLM);
				String[] tstamps = day_val.getTimeStamp().split(CLM);

				String handled_twrs = twrs[0];
				String handled_tstamps = tstamps[0];

				for (int i = 0; i < tstamps.length - 1; i++) {
					String tstamp = tstamps[i];
					String htstamp = tstamps[i + 1];
					if (!tstamp.equals(htstamp)) {
						handled_twrs += CLM + twrs[i + 1];
						handled_tstamps += CLM + tstamps[i + 1];
					}

				}
				tmp_obs_raw_data.put(day_key, new Obs(handled_twrs, handled_tstamps));
				// System.out.println(day_val.timeStamp);
				// System.out.println("++" + handled_tstamps);

			}
			tmp_obs.put(usr_key, tmp_obs_raw_data);

		}
		return tmp_obs;
	}

	/**
	 * Remove observations with the same time stamp and mark neighbor towers
	 * that serve users follow by the previous observation.
	 *
	 * @param obs
	 * @param voronnoi_neighbors
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> remove_handovers(Hashtable<Integer, Hashtable<String, Obs>> obs,
			Hashtable<Integer, ArrayList<Integer>> vor_neighbours) {

		Hashtable<Integer, Hashtable<String, Obs>> tmp_obs = new Hashtable<>();

		for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : obs.entrySet()) {
			Integer usr_key = entrySet.getKey();
			Hashtable<String, Obs> obs_raw_data = entrySet.getValue();
			Hashtable<String, Obs> tmp_obs_raw_data = new Hashtable<>();
			for (Map.Entry<String, Obs> obs_raw_entrySet : obs_raw_data.entrySet()) {

				String day_key = obs_raw_entrySet.getKey();
				Obs day_val = obs_raw_entrySet.getValue();

				String[] twrs = day_val.getSeq().split(CLM);
				String[] tstamps = day_val.getTimeStamp().split(CLM);

				String handled_twrs = twrs[0];
				String handled_tstamps = tstamps[0];

				for (int i = 0; i < tstamps.length - 1; i++) {
					String tstamp = tstamps[i];
					String htstamp = tstamps[i + 1];

					/**
					 * Tue 19 May 2015 07:10:49 AM JST to be completed .......
					 * <-- for a sequence of observations:
					 * {{x0,t0},{x1,t1},{x2,t2},{x3,t3},...{xn,tn}}
					 *
					 * if(t1=t2) then handover(x2|x1) else if((x1 is neighbor to
					 * x2 )&& (x1==x3)) then handover(x2) end if --!>
					 */
				}
				tmp_obs_raw_data.put(day_key, new Obs(handled_twrs, handled_tstamps));

			}
			tmp_obs.put(usr_key, tmp_obs_raw_data);

		}
		return tmp_obs;
	}

	/**
	 * Eliminate the repeated observation, this step must be carried out before
	 * performing anything on the data. Repeated observations harmly affect the
	 * results.
	 *
	 * @param obs
	 * @return
	 */
	public Hashtable<Integer, Hashtable<String, Obs>> remove_repeated(Hashtable<Integer, Hashtable<String, Obs>> obs) {
		Hashtable<Integer, Hashtable<String, Obs>> tmp_obs = new Hashtable<>();

		for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : obs.entrySet()) {
			Integer usr_key = entrySet.getKey();
			Hashtable<String, Obs> obs_raw_data = entrySet.getValue();
			Hashtable<String, Obs> tmp_obs_raw_data = new Hashtable<>();
			for (Map.Entry<String, Obs> obs_raw_entrySet : obs_raw_data.entrySet()) {

				String day_key = obs_raw_entrySet.getKey();
				Obs day_val = obs_raw_entrySet.getValue();

				String[] twrs = day_val.getSeq().split(CLM);
				String[] tstamps = day_val.getTimeStamp().split(CLM);

				String handled_twrs = twrs[0];
				String handled_tstamps = tstamps[0];

				for (int i = 0; i < twrs.length - 1; i++) {
					String twr1 = twrs[i];
					String twr2 = twrs[i + 1];

					if (!twr1.equals(twr2)) {
						handled_twrs += CLM + twrs[i + 1];
						handled_tstamps += CLM + tstamps[i + 1];
					}
				}

				/**
				 * add last element
				 */
				// handled_twrs += twrs[twrs.length - 1];
				// handled_tstamps += tstamps[twrs.length - 1];
				tmp_obs_raw_data.put(day_key, new Obs(handled_twrs, handled_tstamps));

				// System.out.println(day_val.seq);
				// System.out.println("++" + handled_twrs);
				//
				// System.out.println(day_val.timeStamp);
				// System.out.println("++" + handled_tstamps);
				// System.exit(0);
				// obs_raw_data.replace(day_key, new Obs(handled_twrs,
				// handled_tstamps));
			}
			// obs.replace(usr_key, obs_raw_data);
			tmp_obs.put(usr_key, tmp_obs_raw_data);

		}
		return tmp_obs;
	}

	public void setdSPath(String dSPath) {
		this.dSPath = dSPath;
	}

	public void setXmlPath(String xmlPath) {
		this.xmlPath = xmlPath;
	}

	/**
	 * Split the data into weeks dataset
	 *
	 * @param path
	 */
	public void split_daily_data(String path) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<String, Obs>> obstable = new Hashtable<>();
		String subpath = path.substring(0, path.lastIndexOf('.'));

		// splitted data ..
		Hashtable<String, List<String>> datasets = new Hashtable<>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));

			String line;
			while ((line = reader.readLine()) != null) {
				String[] seg = line.split(CLM);
				String day = seg[1].split(" ")[0];
				if (datasets.containsKey(day)) {
					List<String> data = datasets.get(day);
					data.add(line);
					datasets.replace(day, data);
				} else {
					List<String> data = new ArrayList<>();
					data.add(line);
					datasets.put(day, data);
				}
				//
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}

		for (Map.Entry<String, List<String>> entry : datasets.entrySet()) {
			String day = entry.getKey();
			List<String> data = entry.getValue();
			String output = subpath + "_Day_" + day + ".CSV";
			File day_file = new File(output);
			try {
				if (!day_file.exists()) {
					day_file.createNewFile();
				}
				FileWriter day_writer = new FileWriter(day_file.getAbsoluteFile());
				BufferedWriter day_bw = new BufferedWriter(day_writer);
				for (Iterator<String> iterator = data.iterator(); iterator.hasNext();) {
					String l = iterator.next();
					day_bw.append(l);
					day_bw.newLine();
				}
				day_bw.close();
				day_writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Split the data into weeks dataset
	 *
	 * @param path
	 */
	public void split_weeky_data(String path) {

		/**
		 * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
		 */
		Hashtable<Integer, Hashtable<String, Obs>> obstable = new Hashtable<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(Calendar.MONDAY);

		int cnt = 0;
		int week_no = get_week_no(path);
		String subpath = path.substring(0, path.lastIndexOf('.'));
		String week1_path = subpath + "_WEEK_" + week_no + ".CSV";
		String week2_path = subpath + "_WEEK_" + (week_no + 1) + ".CSV";

		File w1file = new File(week1_path);
		File w2file = new File(week2_path);
		// if file doesnt exists, then create it
		try {
			if (!w1file.exists()) {
				w1file.createNewFile();
			}

			if (!w2file.exists()) {
				w2file.createNewFile();
			}

			FileWriter week1_fw = new FileWriter(w1file.getAbsoluteFile());
			BufferedWriter week1_bw = new BufferedWriter(week1_fw);

			FileWriter week2_fw = new FileWriter(w2file.getAbsoluteFile());
			BufferedWriter week2_bw = new BufferedWriter(week2_fw);

			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(path));

				String line;
				while ((line = reader.readLine()) != null) {
					String lineSplit[] = line.split(CLM);
					cal.setTime(parserSDF.parse(lineSplit[1]));
					int recent = cal.get(Calendar.WEEK_OF_YEAR);

					if (week_no == recent) {
						week1_bw.append(line);
						week1_bw.newLine();
						//
					} else {
						week2_bw.append(line);
						week2_bw.newLine();
						//
					}

					// if (Integer.parseInt(lineSplit[0]) != 1) {
					// break;
					// }
				}
				week1_bw.close();
				week2_bw.close();
			} catch (FileNotFoundException ex) {
				Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ParseException ex) {
				Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Hashtable<Integer, Hashtable<Integer, String>> transposeWU(
			Hashtable<Integer, Hashtable<Integer, String>> obsTable) {
		Hashtable<Integer, Hashtable<Integer, String>> transposed = new Hashtable<>();

		for (Map.Entry<Integer, Hashtable<Integer, String>> entry : obsTable.entrySet()) {
			Integer userID = entry.getKey();
			Hashtable<Integer, String> dayObstable = entry.getValue();
			for (Map.Entry<Integer, String> daysEntry : dayObstable.entrySet()) {
				Integer weekday = daysEntry.getKey();
				String obsStr = daysEntry.getValue();

				if (transposed.containsKey(weekday)) {
					Hashtable<Integer, String> usrObstable = transposed.get(weekday);
					usrObstable.put(userID, obsStr);
					transposed.replace(weekday, usrObstable);
				} else {
					Hashtable<Integer, String> usrObstable = new Hashtable<>();
					usrObstable.put(userID, obsStr);
					transposed.put(weekday, usrObstable);
				}

			}

		}
		return transposed;
	}

	public Hashtable<String, Hashtable<Integer, Obs>> transposeWUT(
			Hashtable<Integer, Hashtable<String, Obs>> obsTable) {
		Hashtable<String, Hashtable<Integer, Obs>> transposed = new Hashtable<>();

		for (Map.Entry<Integer, Hashtable<String, Obs>> entry : obsTable.entrySet()) {
			Integer userID = entry.getKey();
			Hashtable<String, Obs> dayObstable = entry.getValue();
			for (Map.Entry<String, Obs> daysEntry : dayObstable.entrySet()) {
				String weekday = daysEntry.getKey();
				Obs obsStr = daysEntry.getValue();

				if (transposed.containsKey(weekday)) {
					Hashtable<Integer, Obs> usrObstable = transposed.get(weekday);
					usrObstable.put(userID, obsStr);
					transposed.replace(weekday, usrObstable);
				} else {
					Hashtable<Integer, Obs> usrObstable = new Hashtable<>();
					usrObstable.put(userID, obsStr);
					transposed.put(weekday, usrObstable);
				}

			}

		}
		return transposed;
	}

	/**
	 * find stops of the whole observation table.
	 *
	 * @param obs_table
	 * @param towers_list
	 * @return
	 * @throws ParseException
	 */
	public Hashtable<String, Hashtable<Integer, Obs>> Update_obs_table(
			Hashtable<String, Hashtable<Integer, Obs>> obs_table, Hashtable<Integer, Vertex> towers_list,
			boolean weekend) {

		Hashtable<String, Hashtable<Integer, Obs>> obs_stops_table = new Hashtable<>();
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_table.entrySet()) {
			String day_key = entrySet.getKey();
			if (!weekend) {
				if (day_key.contains("Saturday") || day_key.contains("Sunday")) {
					continue;
				}
			}
			Hashtable<Integer, Obs> usr_obs = entrySet.getValue();
			Hashtable<Integer, Obs> obs_tmp = new Hashtable<>();
			for (Map.Entry<Integer, Obs> entrySet1 : usr_obs.entrySet()) {
				Integer usr_key = entrySet1.getKey();
				Obs obs_val = entrySet1.getValue();
				String seq = obs_val.getSeq();
				String tstamps = obs_val.getTimeStamp();
				// change it to algorithm 2_3 after finishing the current test
				obs_val = algorithm2_3(seq.split(CLM), tstamps.split(CLM), towers_list);

				obs_tmp.put(usr_key, obs_val);

			}
			obs_stops_table.put(day_key, obs_tmp);
		}
		return obs_stops_table;
	}

	/**
	 * Write week data.
	 *
	 * @param path
	 * @param data
	 */
	private void write_week_data(String path, String data) {
		try {

			File file = new File(path);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append(data);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 *
	 * Mon 01 Dec 2014 11:37:23 PM EET
	 *
	 * Write Observation tables
	 *
	 * @param obsTable
	 * @param dstPath
	 */
	public void writeObsTU(Hashtable<Integer, Hashtable<Integer, String>> obsTable, String dstPath) {

		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("observation");
			doc.appendChild(rootElement);

			for (Map.Entry<Integer, Hashtable<Integer, String>> entrySet : obsTable.entrySet()) {
				Integer userID = entrySet.getKey();
				Element dayElement = doc.createElement("week_day");
				rootElement.appendChild(dayElement);

				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(String.valueOf(userID));
				dayElement.setAttributeNode(attr);
				// System.out.println(pKey);
				Hashtable<Integer, String> value = entrySet.getValue();
				for (Map.Entry<Integer, String> entrySet1 : value.entrySet()) {
					Integer weekDay = entrySet1.getKey();
					String obs = entrySet1.getValue();

					Element usrSeqElement = doc.createElement("user");
					dayElement.appendChild(usrSeqElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(String.valueOf(weekDay));
					usrSeqElement.setAttributeNode(attr);

					attr = doc.createAttribute("seq");
					// attr.setValue(formatter.format(probability));
					attr.setValue(obs);
					usrSeqElement.setAttributeNode(attr);

				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(dstPath));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException | TransformerException pce) {
		}
	}
}
