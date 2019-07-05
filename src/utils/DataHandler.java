/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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

import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.MergeXML;
import trajectory.importance.TCluster;
import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.UTMRef;

/**
 *
 * @author essam
 */
public class DataHandler {

	public static String COMMA_SEP = ",";
	public static String SPACE_SEP = " ";
	public static String TAB_SEP = "\t";
	public static String SLASH_SEP = "/";
	public static boolean ASC = true;
	public static boolean DESC = false;
	public static final boolean VITERBI_TRIPS_FLAG = true;
	public static final boolean OBSERVATIONS_TRIPS_FLAG = false;

	private static void adaptObs(Hashtable<String, Hashtable<Integer, Obs>> obs,
			Hashtable<String, List<String>> adapted_obs, Hashtable<String, List<String>> adapted_ts) {

		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
			String key = entrySet.getKey();
			Hashtable<Integer, Obs> value = entrySet.getValue();
			List<String> obs_list = new ArrayList<>();
			List<String> ts_list = new ArrayList<>();
			for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {

				String seq[] = entrySet1.getValue().getSeq().trim().replace(COMMA_SEP, SPACE_SEP).split(SLASH_SEP);
				String ts[] = entrySet1.getValue().getTimeStamp().trim().replace(COMMA_SEP, SPACE_SEP).split(SLASH_SEP);
				for (int i = 0; i < ts.length; i++) {
					String t = ts[i];
					obs_list.add(seq[i]);
					ts_list.add(ts[i]);

				}

			}
			adapted_ts.put(key, ts_list);
			adapted_obs.put(key, obs_list);
		}
	}

	/**
	 * Convert observations into trips list ....
	 *
	 * @param obs
	 * @param obs_vit_flag
	 * @return
	 */
	public static Hashtable<String, List<String[]>> convert_obs_routes(Hashtable<String, Hashtable<Integer, Obs>> obs,
			boolean obs_vit_flag) {
		Hashtable<String, List<String[]>> trips = new Hashtable<>();
		for (Map.Entry<String, Hashtable<Integer, Obs>> entry : obs.entrySet()) {
			String day_key = entry.getKey();
			Hashtable<Integer, Obs> day_obs = entry.getValue();
			List<String[]> day_trips = new ArrayList<>();
			for (Map.Entry<Integer, Obs> day_obs_entry : day_obs.entrySet()) {
				Obs usr_obs = day_obs_entry.getValue();
				String v[];
				if (obs_vit_flag) {
					v = usr_obs.getVitPath().split(SLASH_SEP);
				} else {
					v = usr_obs.getSeq().split(SLASH_SEP);
				}
				for (int i = 0; i < v.length; i++) {
					day_trips.add(v[i].split(COMMA_SEP));
				}
			}
			trips.put(day_key, day_trips);
		}
		return trips;
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
	public static double euclidean(double x, double y, double x1, double y1) {
		return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
	}

	public static Hashtable<Integer, Double> extract_gyration_field(String indicator_path) {
		Hashtable<Integer, Double> info_field = new Hashtable<>();
		List<String[]> data = DataHandler.read_multiple_csv(indicator_path, DataHandler.COMMA_SEP);
		for (String[] d : data) {
			// String[] d = data.get(i);
			int id = Integer.parseInt(d[0]);
			double rg = Double.parseDouble(d[29]);
			info_field.put(id, rg);
		}
		return info_field;
	}

	/**
	 * Get the distance between between two geopoints
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param unit
	 * @return
	 */
	public static double geodistance(double lat1, double lon1, double lat2, double lon2, String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "m") {
			dist = dist * 1.609344 * 1000;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	/**
	 * Extract part of the data with the provided indices ...
	 *
	 * @param data
	 * @param indices
	 * @return
	 */
	public static List<String[]> extract_info(List<String[]> data, int[] indices) {

		List<String[]> info = new ArrayList<>();
		for (Iterator<String[]> iterator = data.iterator(); iterator.hasNext();) {
			String[] d = iterator.next();
			String[] rec_info = new String[indices.length];
			for (int i = 0; i < indices.length; i++) {
				rec_info[i] = d[indices[i]];
			}
			info.add(rec_info);
		}
		return info;
	}

	/**
	 *
	 * @param data
	 * @param index
	 * @return
	 */
	public static String[] extract_info_field(List<String[]> data, int index) {
		String[] info_field = new String[data.size()];

		for (int i = 0; i < data.size(); i++) {
			String[] d = data.get(i);
			info_field[i] = d[index];
		}
		return info_field;
	}

	/**
	 * find indices of header names provided ..
	 *
	 * @param data
	 * @param fields
	 * @return
	 */
	public static int[] find_indices(List<String[]> data, String[] fields) {
		int[] indices = new int[fields.length];

		String[] header = data.get(0);
		for (int i = 0; i < fields.length; i++) {
			String f = fields[i];
			// if (f.contains(Utils.SLASH_SEP)) {
			// String fs[] = f.split(Utils.SLASH_SEP);
			// for (int j = 0; j < fs.length; j++) {
			// String f1 = fs[j];
			//
			// for (int k = 0; k < header.length; k++) {
			// if (header[k].compareToIgnoreCase(f1) == 0) {
			// indices[i] = k;
			// }
			// }
			//
			// }
			// } else {
			for (int j = 0; j < header.length; j++) {
				if (header[j].compareToIgnoreCase(f) == 0) {
					indices[i] = j;
				}
			}
			// }
		}
		return indices;
	}

	private static double[] generate_init(int ntrans) {

		double[] init = new double[ntrans];
		double sum = 0;
		for (int i = 0; i < ntrans; i++) {
			double num = Math.random();
			sum += num;
			init[i] = num;
		}

		for (int i = 0; i < ntrans; i++) {
			init[i] = init[i] / sum;

		}
		return init;
	}

	/**
	 * Find the commute distance between individuals home and work ...
	 *
	 * @param u_clusters
	 * @param towers
	 * @return
	 */
	public static double[] get_commute_distances(Hashtable<Integer, List<TCluster>> u_clusters,
			Hashtable<Integer, Vertex> towers) {
		List<Double> distances = new ArrayList<>();
		for (Map.Entry<Integer, List<TCluster>> entry : u_clusters.entrySet()) {
			Integer usr_key = entry.getKey();
			List<TCluster> clusters = entry.getValue();
			int home_id = -1, work_id = -1;
			for (Iterator<TCluster> iterator = clusters.iterator(); iterator.hasNext();) {
				TCluster t = iterator.next();
				if (t.is_home_loc()) {
					home_id = t.get_centroid();
				} else if (t.is_work_loc()) {
					work_id = t.get_centroid();
				}
			}
			if (home_id != -1 && work_id != -1) {
				Vertex v_h = towers.get(home_id);
				Vertex v_w = towers.get(work_id);
				distances.add(euclidean(v_h.getX(), v_h.getY(), v_w.getX(), v_w.getY()));
			}
		}
		double[] t_dist = new double[distances.size()];
		for (int i = 0; i < distances.size(); i++) {
			t_dist[i] = distances.get(i);

		}
		return t_dist;
	}

	public static String get_day_date(Date d) {
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd_EEEE");
		return dayFormat.format(d);
	}

	/**
	 * find the distribution of a list of data provided ....
	 *
	 * @param <T>
	 * @param data
	 * @return
	 */
	public static Hashtable<Double, Double> get_distribution(double[] data) {

		Hashtable<Double, Double> freq = get_frequency(data);
		Hashtable<Double, Double> dist = new Hashtable<>();
		int size = data.length;
		for (Map.Entry<Double, Double> entrySet : freq.entrySet()) {
			double key = entrySet.getKey();
			double val = entrySet.getValue();
			double p = val / size;
			dist.put(key, p);
		}
		return dist;
	}

	/**
	 * find the frequency of elements with the provided array ..
	 *
	 * @param <T>
	 * @param data
	 * @return
	 */
	public static Hashtable<Double, Double> get_frequency(double[] data) {

		Hashtable<Double, Double> freq = new Hashtable<>();
		for (int i = 0; i < data.length; i++) {
			double v = data[i];
			if (freq.containsKey(v)) {
				double count = freq.get(v) + 1.0;
				freq.replace(v, count);
			} else {
				freq.put(v, 1.0);
			}

		}
		return freq;
	}

	/**
	 * Get home work locations ..
	 *
	 * @param u_clusters
	 * @param home_loc_table
	 * @param work_loc_table
	 */
	public static void get_home_work_loc(Hashtable<Integer, List<TCluster>> u_clusters,
			Hashtable<Integer, Integer> home_loc_table, Hashtable<Integer, Integer> work_loc_table) {

		for (Map.Entry<Integer, List<TCluster>> entry : u_clusters.entrySet()) {
			Integer usr_key = entry.getKey();
			List<TCluster> clusters = entry.getValue();
			int home_id = -1, work_id = -1;
			for (Iterator<TCluster> iterator = clusters.iterator(); iterator.hasNext();) {
				TCluster t = iterator.next();
				if (t.is_home_loc()) {
					home_loc_table.put(usr_key, t.get_centroid());
				} else if (t.is_work_loc()) {
					work_loc_table.put(usr_key, t.get_centroid());
				}
			}

		}

	}

	/**
	 * Check if the provided date is a week day or not ....
	 *
	 * @param d
	 * @return
	 */
	public static boolean is_week_day(Date d) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		return ((dayOfWeek >= Calendar.MONDAY) && (dayOfWeek <= Calendar.FRIDAY));
	}

	/**
	 * Convert a list of strings into towers table ..
	 *
	 * @param data
	 * @return
	 */
	public static Hashtable<Integer, Vertex> list_towers(List<String[]> data) {

		Hashtable<Integer, Vertex> ts = new Hashtable<>();
		for (Iterator<String[]> iterator = data.iterator(); iterator.hasNext();) {
			String[] t = iterator.next();
			int index = Integer.parseInt(t[0]);
			double lan = Double.parseDouble(t[1]);
			double lat = Double.parseDouble(t[2]);
			ts.put(index, new Vertex(lat, lan));

		}
		return ts;
	}

	// public static void write_home_work_csv(Hashtable<Integer, Integer>
	// home_locs, Hashtable<Integer, Integer> work_locs, String header, String
	// path) {
	//
	// BufferedWriter writer = null;
	// try {
	// File logFile = new File(path);
	// writer = new BufferedWriter(new FileWriter(logFile));
	// writer.write(header);
	// writer.newLine();
	// for (Map.Entry<Double, Double> entrySet : data.entrySet()) {
	// double key = entrySet.getKey();
	// double value = entrySet.getValue();
	// writer.write(Double.toString(key) + "," + Double.toString(value));
	// writer.newLine();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// writer.close();
	// } catch (Exception e) {
	// }
	// }
	// }
	/**
	 * Get list of files in a directory ...
	 *
	 * @param folder
	 * @return
	 */
	public static ArrayList<String> listFilesForFolder(final File folder) {
		ArrayList<String> files = new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				files.add(fileEntry.getPath());
				// System.out.println(fileEntry.getName());
			}
		}
		return files;
	}

	/**
	 * Convert lat/lon coordinates into UTM coordinates ...
	 *
	 * @param lat
	 * @param lon
	 * @return
	 */
	public static double[] proj_coordinates(double lat, double lon) {
		int scaling_factor = 1;
		LatLng ll = new LatLng(lat, lon);

		UTMRef u = ll.toUTMRef();
		// OSRef u = ll.toOSRef();
		// System.out.println(u.getEasting()+"\t"+u.getNorthing());
		double[] xy = new double[2];
		xy[0] = u.getNorthing() / scaling_factor;
		xy[1] = u.getEasting() / scaling_factor;
		return xy;
	}

	/**
	 * Project and shift ...
	 * 
	 * @param lat
	 * @param lon
	 * @param xmin
	 * @param ymin
	 * @return
	 */
	public static double[] proj_coordinates(double lat, double lon, double xmin, double ymin) {
		double xy[] = DataHandler.proj_coordinates(lat, lon);
		xy[0] = xy[0] - xmin;
		xy[1] = xy[1] - ymin;
		return xy;
	}

	public static double[] proj_lat_long_from_wkt(String path, double lat, double lon) {
		String wkt;
		try {
			wkt = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		SpatialReference src = new SpatialReference();
		src.ImportFromProj4("+proj=latlong +datum=WGS84 +no_defs");

		SpatialReference dst = new SpatialReference(wkt);

		CoordinateTransformation ct = CoordinateTransformation.CreateCoordinateTransformation(src, dst);

		return ct.TransformPoint(lat, lon);
	}

	public static double[] proj_lat_long_from_wkt_str(String wkt, double lat, double lon) {
		SpatialReference src = new SpatialReference();
		src.ImportFromProj4("+proj=latlong +datum=WGS84 +no_defs");

		SpatialReference dst = new SpatialReference(wkt);

		CoordinateTransformation ct = CoordinateTransformation.CreateCoordinateTransformation(src, dst);

		return ct.TransformPoint(lat, lon);
	}

	public static double[] proj_lat_long_from_proj4(String proj_string, double lat, double lon) {
		SpatialReference src = new SpatialReference();
		src.ImportFromProj4("+proj=latlong +datum=WGS84 +no_defs");

		SpatialReference dst = new SpatialReference();
		dst.ImportFromProj4(proj_string);

		CoordinateTransformation ct = CoordinateTransformation.CreateCoordinateTransformation(src, dst);

		return ct.TransformPoint(lat, lon);
	}

	/**
	 * Read CSV file of comma separator ..
	 *
	 * @param path
	 * @param seperator
	 * @return
	 */
	public static List<String[]> read_csv(String path, String seperator) {

		List<String[]> segments = new ArrayList<>();
		BufferedReader reader;
		try {
			Reader isreader = new InputStreamReader(new FileInputStream(path), "utf-8");
			reader = new BufferedReader(isreader);

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				segments.add(line.trim().split(seperator));
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		return segments;
	}

	/**
	 * Read CDR data as it is; without any preprocessing of the data ..
	 *
	 * @param path
	 * @return
	 */
	public static List<CDR> read_dataset(String path) {

		List<CDR> cdrs = new ArrayList<>();

		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));

			String line;
			int i = 0;
			while ((line = reader.readLine()) != null) {
				CDR record = new CDR();
				String lineSplit[] = line.split(COMMA_SEP);
				record.id = Integer.parseInt(lineSplit[0]);
				// debug
				// if (record.id != 1817) {
				// continue;
				// }
				try {
					record.date = parserSDF.parse(lineSplit[1]);

					// Calendar cal = Calendar.getInstance();
					// cal.setTime(record.date);
					// int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
					//
					// if (!t_days.contains(dayOfYear)) {
					// t_days.add(dayOfYear);
					// }
				} catch (ParseException ex) {
					Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
				}
				record.twr_id = Integer.parseInt(lineSplit[2]);
				cdrs.add(record);
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(ObsTripsBuilder.class.getName()).log(Level.SEVERE, null, ex);
		}
		return cdrs;
	}

	/**
	 * Read multiple CSV files contain the same data types ...
	 *
	 * @param dataset_path
	 * @param seperator
	 * @return
	 */
	public static List<String[]> read_multiple_csv(String dataset_path, String seperator) {

		List<String[]> data = new ArrayList<>();

		ArrayList<String> files = DataHandler.listFilesForFolder(new File(dataset_path));
		// System.out.println("Number of file: "+files.size());
		files.stream().forEach((subsetPath) -> {
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (!(fileName.startsWith("."))) {
				if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
					data.addAll(read_csv(subsetPath, seperator));
				}
			}
		});

		return data;
	}

	public static List<String[]> read_multiple_routes_files(String path) {
		List<String[]> routes = new ArrayList<>();
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (fileName.startsWith(".")) {
				continue;
			}
			routes.addAll(DataHandler.read_routes(subsetPath));

		}
		return routes;
	}

	/**
	 * Read mapped routes csv files
	 *
	 * @param path
	 * @return
	 */
	public static List<String[]> read_routes(String path) {
		List<String[]> segments = DataHandler.read_csv(path, COMMA_SEP);

		List<String[]> routes = new ArrayList<>();
		for (int i = 3; i < segments.size(); i++) {
			String[] route = segments.get(i);
			if (route.length == 1) {
				continue;
			}
			routes.add(route);
		}
		return routes;
	}

	/**
	 * Read mapped routes csv files
	 *
	 * @param path
	 * @return
	 */
	public static List<String[]> get_trips(Hashtable<String, Hashtable<Integer, Obs>> data) {

		List<String[]> trips = new ArrayList<>();
		for (Map.Entry<String, Hashtable<Integer, Obs>> data_entry : data.entrySet()) {
			Hashtable<Integer, Obs> obs = data_entry.getValue();
			for (Map.Entry<Integer, Obs> obs_entry : obs.entrySet()) {
				Obs o = obs_entry.getValue();
				String seq = o.getSeq();
				if (seq.contains("/")) {
					String[] ts = seq.split("/");
					for (int i = 0; i < ts.length; i++) {
						trips.add(ts[i].split(","));
					}
				}
			}

		}

		return trips;
	}

	/**
	 * Read both the POI XML file
	 *
	 * @param path
	 * @return
	 */
	public static Hashtable<Integer, List<TCluster>> read_xml_poi(String path) {
		Hashtable<Integer, List<TCluster>> table = new Hashtable<>();
		org.w3c.dom.Document dpDoc;

		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList usrsPOIList = dpDoc.getElementsByTagName("user_node");

			for (int i = 0; i < usrsPOIList.getLength(); i++) {
				Node fromNodeNode = usrsPOIList.item(i);
				if (fromNodeNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) fromNodeNode;
					int usr_id = Integer.parseInt(eElement.getAttribute("id"));

					NodeList clusterList = eElement.getElementsByTagName("cluster");
					List<TCluster> u_clusters = new ArrayList<>();

					for (int j = 0; j < clusterList.getLength(); j++) {
						Node clusterNode = clusterList.item(j);
						if (clusterNode.getNodeType() == Node.ELEMENT_NODE) {
							Element clusterNodeElement = (Element) clusterNode;

							int centroid = Integer.parseInt(clusterNodeElement.getAttribute("centroid"));
							String members = clusterNodeElement.getAttribute("members");
							int days = Integer.parseInt(clusterNodeElement.getAttribute("days"));
							int tower_days = Integer.parseInt(clusterNodeElement.getAttribute("tower_days"));
							int duration = Integer.parseInt(clusterNodeElement.getAttribute("duration"));
							int home_events = Integer.parseInt(clusterNodeElement.getAttribute("home_hours_events"));
							int work_events = Integer.parseInt(clusterNodeElement.getAttribute("work_hours_events"));
							boolean is_home = Boolean.parseBoolean(clusterNodeElement.getAttribute("is_home"));
							boolean is_work = Boolean.parseBoolean(clusterNodeElement.getAttribute("is_work"));

							TCluster tc = new TCluster(centroid);
							tc.set_cluster_elements(members);
							tc.set_days(days);
							tc.set_duration(duration);
							tc.set_tower_days_sum(tower_days);
							tc.set_home_hour_events(home_events);
							tc.set_work_hour_events(work_events);
							tc.set_work_loc(is_work);
							tc.set_home_loc(is_home);
							tc.set_consistant_data(true);
							u_clusters.add(tc);
						}
					}
					table.put(usr_id, u_clusters);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		}

		return table;
	}

	/**
	 * remove handovers from the data ....
	 *
	 * @param dataset
	 * @return
	 */
	public static List<CDR> remove_handover(List<CDR> dataset) {
		List<CDR> ucdr = new ArrayList<>();
		int prev_usr = -1;
		Date prev_date = null;
		CDR prev_cdr = null;
		for (CDR rec : dataset) {

			if (prev_usr == rec.id) {
				if (prev_date.compareTo(rec.date) == 0) {
					if (!ucdr.contains(prev_cdr)) {
						ucdr.add(prev_cdr);
						// prev_date = rec.date;
					}
				} else {
					prev_date = rec.date;
					prev_cdr = rec;
					ucdr.add(rec);
				}
			} else {
				prev_usr = rec.id;
				prev_date = rec.date;
				prev_cdr = rec;
				ucdr.add(rec);
			}
		}
		return ucdr;
	}

	/**
	 * remove handovers from the data ....
	 *
	 * @param dataset
	 * @return
	 */
	public static List<String[]> remove_handover_str(List<String[]> dataset) {
		List<String[]> ucdr = new ArrayList<>();
		int prev_usr = -1;
		String prev_date = null;
		String[] prev_cdr = null;
		for (String[] rec : dataset) {
			int id = Integer.parseInt(rec[0]);
			if (prev_usr == id) {
				if (prev_date.compareTo(rec[1]) == 0) {
					if (!ucdr.contains(prev_cdr)) {
						ucdr.add(prev_cdr);
						// prev_date = rec.date;
					}
				} else {
					prev_date = rec[1];
					ucdr.add(rec);
				}
			} else {
				prev_usr = id;
				prev_date = rec[1];
				prev_cdr = rec;
				ucdr.add(rec);
			}
		}
		return ucdr;
	}

	/**
	 * Remove repeated observation from CDRs
	 *
	 * @param dataset
	 * @return
	 */
	public static List<CDR> remove_repeated_obs(List<CDR> dataset) {
		List<CDR> ucdr = new ArrayList<>();
		int prev_usr = -1;
		int prev_twr = -1;
		for (CDR rec : dataset) {
			if (prev_usr == rec.id) {
				if (rec.twr_id != prev_twr) {
					ucdr.add(rec);
					prev_twr = rec.twr_id;
				}
			} else {
				prev_usr = rec.id;
				prev_twr = rec.twr_id;
				ucdr.add(rec);
			}
		}
		return ucdr;
	}

	/**
	 * Remove repeated observation from CDRs
	 *
	 * @param dataset
	 * @return
	 */
	public static List<String[]> remove_repeated_obs_str(List<String[]> dataset) {
		List<String[]> ucdr = new ArrayList<>();
		int prev_usr = -1;
		int prev_twr = -1;
		for (String[] rec : dataset) {
			int id = Integer.parseInt(rec[0]);
			int twr_id = Integer.parseInt(rec[2]);
			if (prev_usr == id) {
				if (twr_id != prev_twr) {
					ucdr.add(rec);
					prev_twr = twr_id;
				}
			} else {
				prev_usr = id;
				prev_twr = twr_id;
				ucdr.add(rec);
			}
		}
		return ucdr;
	}

	/**
	 * Round up values ...
	 *
	 * @param i
	 * @param v
	 * @return
	 */
	public static double round(double i, int v) {
		return Math.round(i / v) * v;
	}

	public static double[] round_list(double[] data, int round_val) {
		for (int i = 0; i < data.length; i++) {
			data[i] = round(data[i], round_val);
		}
		return data;
	}

	public static Map<Integer, Integer> sort_int_map(Map<Integer, Integer> unsortMap, final boolean order) {

		List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			@Override
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
		for (Map.Entry<Integer, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	/**
	 * Sort map based on its values ascending or descending ordering.
	 *
	 * @param unsortMap
	 * @param order
	 * @return
	 */
	public static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {

		List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	/**
	 * convert an array of strings into double
	 *
	 * @param data
	 * @return
	 */
	public static double[] to_double(String[] data) {
		double[] d_d = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			try {
				d_d[i] = Double.parseDouble(data[i]);
			} catch (NumberFormatException numberFormatException) {
				System.out.println(data[i]);
			}
		}
		return d_d;
	}

	/**
	 * convert an array of strings into integers
	 *
	 * @param data
	 * @return
	 */
	public static int[] to_integer(String[] data) {
		int[] i_d = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			i_d[i] = Integer.parseInt(data[i]);
		}
		return i_d;
	}

	/**
	 * convert an array of strings into list of integers
	 *
	 * @param data
	 * @return
	 */
	public static List<Integer> to_integer_list(String[] data) {

		List<Integer> i_d = new ArrayList<>();
		for (int i = 0; i < data.length; i++) {
			i_d.add(i, Integer.parseInt(data[i]));
		}
		return i_d;
	}

	public static String to_string(List<Integer> data) {
		if (data.isEmpty()) {
			return "";
		}
		String tmp = Integer.toString(data.get(0));
		for (int i = 1; i < data.size(); i++) {
			tmp += COMMA_SEP + data.get(i);
		}
		return tmp;
	}

	public static String to_string_cdr(List<CDR> data) {
		if (data.isEmpty()) {
			return "";
		}
		String tmp = Integer.toString(data.get(0).twr_id);
		for (int i = 1; i < data.size(); i++) {
			tmp += COMMA_SEP + data.get(i).twr_id;
		}
		return tmp;
	}

	public static String to_string_date(List<Date> data) {
		if (data.isEmpty()) {
			return "";
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(data.get(0));

		String tmp = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);
		for (int i = 1; i < data.size(); i++) {
			cal.setTime(data.get(i));
			tmp += COMMA_SEP + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":"
					+ cal.get(Calendar.SECOND);
			;
		}
		return tmp;
	}

	/**
	 * Write CSV file ...
	 *
	 * @param <T>
	 * @param <K>
	 * @param data
	 * @param header
	 * @param path
	 */
	public static void write_csv(Hashtable<Double, Double> data, String header, String path) {

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(header);
			writer.newLine();
			for (Map.Entry<Double, Double> entrySet : data.entrySet()) {
				double key = entrySet.getKey();
				double value = entrySet.getValue();
				writer.write(Double.toString(key) + "," + Double.toString(value));
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
	 *
	 * @param data
	 * @param header
	 * @param path
	 */
	public static void write_csv(List<String[]> data, String path) {
		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));
			for (Iterator<String[]> iterator = data.iterator(); iterator.hasNext();) {
				String[] d = iterator.next();
				writer.write(d[0]);
				for (int i = 1; i < d.length; i++) {
					writer.write(COMMA_SEP + d[i]);
				}
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
	 *
	 * @param data
	 * @param header
	 * @param path
	 */
	public static void write_csv(List<String[]> data, String header, String path) {
		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(header);
			writer.newLine();
			for (Iterator<String[]> iterator = data.iterator(); iterator.hasNext();) {
				String[] d = iterator.next();
				writer.write(d[0]);
				for (int i = 1; i < d.length; i++) {
					writer.write(COMMA_SEP + d[i]);
				}
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

	public static void write_hmm(String output, String name, int zones, String network_path, String trans_path,
			String emit_path) {

		String states_path = output + "/" + name + ".states";
		String hmm_path = output + "/" + name + ".hmm";

		DataHandler adaptor = new DataHandler();
		adaptor.readNetworkDist(network_path);

		// read transitions
		Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(trans_path);
		// read emissions
		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emit_path);
		// read states
		ArrayList<String> exts = adaptor.getExts(trans_p);
		int nstates = exts.size();
		// System.out.println("Transition size: " + nstates);

		double[][] trans = adaptor.adaptTrans(trans_p, exts, nstates);
		double[][] emiss = adaptor.adaptEmission(emit_p, exts, nstates, zones);
		double[] init = DataHandler.generate_init(nstates);

		DataHandler.write_states(exts, nstates, states_path);
		DataHandler.write_hmm_csv(trans, emiss, init, nstates, zones, hmm_path);

	}

	public static void write_hmm_csv(double[][] trans, double[][] emiss, double[] init, int nstates, int nobs,
			String path) {

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));

			writer.write("nStates\n");
			writer.write(Integer.toString(nstates));
			writer.newLine();

			writer.write("nObservables\n");
			writer.write(Integer.toString(nobs));
			writer.newLine();

			// write init
			writer.write("initProbs");
			writer.newLine();
			for (int i = 0; i < nstates; i++) {
				double j = init[i];
				writer.write(String.format("%2.4e", j));
				writer.newLine();
			}

			// write transitions
			writer.write("transProbs");
			writer.newLine();
			for (int i = 0; i < nstates; i++) {
				double[] ds = trans[i];
				int k = 0;
				for (int j = 0; j < nstates; j++) {
					double d = ds[j];
					if (Double.isNaN(d)) {
						d = 0;
					}
					if (d == 0) {
						writer.write(Double.toString(d));
					} else {
						writer.write(String.format("%2.4e", d));
					}

					k = j;
					if (k++ < ds.length) {
						writer.write(' ');
					}
				}
				writer.newLine();
			}

			// write emission
			writer.write("emProbs");
			writer.newLine();
			for (int i = 0; i < nstates; i++) {
				double[] ds = emiss[i];
				int k = 0;
				for (int j = 0; j < nobs; j++) {
					double d = ds[j];
					if (Double.isNaN(d)) {
						d = 0;
					}
					if (d == 0) {
						writer.write(Double.toString(d));
					} else {
						writer.write(String.format("%2.4e", d));
					}
					k = j;
					if (k++ < ds.length) {
						writer.write(' ');
					}
				}
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

	private static void write_states(ArrayList<String> exts, int nstates, String path) {

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));

			writer.write("nStates");
			writer.newLine();
			writer.write(Integer.toString(exts.size()));
			writer.newLine();

			writer.write("states");
			// writer.newLine();

			for (int i = 0; i < nstates; i++) {
				if (i < exts.size()) {
					String ext_point = exts.get(i);
					writer.newLine();
					writer.write(ext_point);
				} else {
					String ext_point = "-";
					writer.newLine();
					writer.write(ext_point);
				}
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

	public static void write_trips(Hashtable<String, Hashtable<Integer, Obs>> obs, String path) {
		Hashtable<String, List<String>> adapted_obs = new Hashtable<>();
		Hashtable<String, List<String>> adapted_ts = new Hashtable<>();

		adaptObs(obs, adapted_obs, adapted_ts);

		BufferedWriter writer = null;
		try {

			for (Map.Entry<String, List<String>> entrySet : adapted_obs.entrySet()) {
				String daykey = entrySet.getKey();

				File logFile = new File(path + "/" + daykey + ".csv");
				writer = new BufferedWriter(new FileWriter(logFile));

				List<String> obs_list = entrySet.getValue();

				writer.write("nSequences\n");
				writer.write(Integer.toString(obs_list.size()));
				writer.newLine();

				writer.write("sequences\n");

				for (Iterator<String> iterator = obs_list.iterator(); iterator.hasNext();) {
					String next = iterator.next();
					writer.write(next);
					writer.newLine();
				}

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
	 * Write users clusters to XML file
	 *
	 * @param u_poi
	 * @param path
	 */
	public static void write_xml_poi(Hashtable<Integer, List<TCluster>> u_poi, String path) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("POI");
			doc.appendChild(rootElement);

			for (Map.Entry<Integer, List<TCluster>> entrySet : u_poi.entrySet()) {
				Integer usr_key = entrySet.getKey();
				List<TCluster> u_clusters = entrySet.getValue();

				Element usrNodeElement = doc.createElement("user_node");
				rootElement.appendChild(usrNodeElement);
				Attr attr = doc.createAttribute("id");
				attr.setValue(usr_key.toString());
				usrNodeElement.setAttributeNode(attr);

				for (int i = 0; i < u_clusters.size(); i++) {
					TCluster c = u_clusters.get(i);

					Element clusterNodeElement = doc.createElement("cluster");
					usrNodeElement.appendChild(clusterNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("centroid");
					attr.setValue(Integer.toString(c.get_centroid()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("members");
					attr.setValue(c.get_cluster_elements());
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("days");
					attr.setValue(Integer.toString(c.get_days()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("tower_days");
					attr.setValue(Integer.toString(c.get_tower_days()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("duration");
					attr.setValue(Integer.toString(c.get_duration()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("home_hours_events");
					attr.setValue(Integer.toString(c.get_home_hour_events()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("work_hours_events");
					attr.setValue(Integer.toString(c.get_work_hour_events()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("is_home");
					attr.setValue(Boolean.toString(c.is_home_loc()));
					clusterNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("is_work");
					attr.setValue(Boolean.toString(c.is_work_loc()));
					clusterNodeElement.setAttributeNode(attr);
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
	 * Read divided CDRs into days
	 *
	 * @param path
	 * @return Hashtable<Month day number, Hashtable<User ID, Observation
	 *         sequences>>, observation sequence may contain more than
	 *         sub-sequences
	 */
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
							usrsTable.put(Integer.parseInt(usrElement.getAttribute("id")),
									new Obs(usrElement.getAttribute("seq"), usrElement.getAttribute("timestamp"),
											usrElement.getAttribute("viterbi")));
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
	 * Read towers coordinates from comma separator file.
	 *
	 * @param path
	 * @return list of all towers coordinates
	 */
	public static Hashtable<Integer, Vertex> readTowers(String path) {
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
				pnt.setX(Double.parseDouble(lineSplit[1]));
				pnt.setY(Double.parseDouble(lineSplit[2]));
				// System.out.printf("%f,%f",pnt.x,pnt.y);
				towers.put(index, pnt);
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		return towers;
	}

	/**
	 * Read towers coordinates from comma separator file.
	 *
	 * @param path
	 * @return list of all towers coordinates
	 */
	public static Hashtable<Integer, Vertex> read_scaled_towers(String path, double scale) {
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
				double pxy[] = DataHandler.proj_coordinates(Double.parseDouble(lineSplit[1]) / scale,
						Double.parseDouble(lineSplit[2]) / scale);
				pnt.setX(pxy[0]);
				pnt.setY(pxy[1]);
				// System.out.printf("%f,%f",pnt.x,pnt.y);
				towers.put(index, pnt);
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(DataHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
		return towers;
	}

	ArrayList<FromNode> networkDist;

	Hashtable<Integer, String> observations;

	public DataHandler() {
		networkDist = new ArrayList<>();
		observations = new Hashtable<>();

	}

	public DataHandler(ArrayList<FromNode> netDist) {
		networkDist = new ArrayList<>(netDist);
		observations = new Hashtable<>();

	}

	public Hashtable<String, Hashtable<String, Double>> adaptEmission(Hashtable<String, Hashtable<String, Double>> prob,
			ArrayList<Integer> zones) {
		Hashtable<String, Hashtable<String, Double>> emission = new Hashtable<>();
		for (Map.Entry<String, Hashtable<String, Double>> entry : prob.entrySet()) {
			String from = entry.getKey();
			Hashtable<String, Double> hashtable = entry.getValue();
			Hashtable<String, Double> tmpHashtable = new Hashtable<>();
			for (Iterator<Integer> it = zones.iterator(); it.hasNext();) {
				Integer zone = it.next();
				if (hashtable.containsKey(String.valueOf(zone))) {
					// hashtable.get(String.valueOf(zone))
					tmpHashtable.put(String.valueOf(zone), hashtable.get(String.valueOf(zone)));
				} else {
					// tmpHashtable.put(String.valueOf(zone), (double)
					// Math.pow(Math.random(), 4));
					tmpHashtable.put(String.valueOf(zone), 0.0);
				}
			}
			emission.put(from, tmpHashtable);
		}
		// System.out.printf("emission\t%d", emission.size());
		return emission;
	}

	public double[][] adaptEmission(Hashtable<String, Hashtable<String, Double>> trans, List<String> states,
			int nstates, int zones) {
		double[][] aEmissions = new double[nstates][zones];

		for (double[] aEmission : aEmissions) {
			for (int j = 0; j < aEmission.length; j++) {
				aEmission[j] = 0;
			}
		}
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : trans.entrySet()) {
			String fromKey = entrySet.getKey();
			if (states.contains(fromKey)) {
				Hashtable<String, Double> value = entrySet.getValue();
				for (Map.Entry<String, Double> toEntrySet : value.entrySet()) {
					int toZone = Integer.parseInt(toEntrySet.getKey()) - 1;
					double prob = toEntrySet.getValue();
					if (!(states.contains(fromKey))) {
						System.out.printf("-> %s , %d\n", fromKey, toZone);
						continue;
					}
					if (Double.isNaN(prob)) {
						System.out.println("Error NaN values ....");
					}
					// prob = Math.log(prob);
					// System.out.printf("state: %d\t zone: %d\n",
					// states.indexOf(fromKey),toZone);
					aEmissions[states.indexOf(fromKey)][toZone] = prob;
				}
			}
		}
		return aEmissions;
	}

	public Hashtable<String, Hashtable<String, Double>> adaptTrans(Hashtable<String, Hashtable<String, Double>> trans) {
		ArrayList<String> exts = getExts();

		for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
			String extPnt = iterator.next();
			if (!trans.containsKey(extPnt)) {
				trans.put(extPnt, new Hashtable<>());
			}

		}
		Hashtable<String, Hashtable<String, Double>> tmpTrans = new Hashtable<>();
		for (Map.Entry<String, Hashtable<String, Double>> entry : trans.entrySet()) {
			String from = entry.getKey();
			Hashtable<String, Double> toHashtable = entry.getValue();
			Hashtable<String, Double> newToHT = new Hashtable<>();
			for (Iterator<String> it = exts.iterator(); it.hasNext();) {
				String ext = it.next();
				if (toHashtable.containsKey(ext)) {
					newToHT.put(ext, toHashtable.get(ext));
				} else {
					newToHT.put(ext, 0.0);
				}

			}
			tmpTrans.put(from, newToHT);

		}
		return tmpTrans;
	}

	public double[][] adaptTrans(Hashtable<String, Hashtable<String, Double>> trans, List<String> states, int nstates) {
		double[][] aTrans = new double[nstates][nstates];

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : trans.entrySet()) {
			String fromKey = entrySet.getKey();
			if (states.contains(fromKey)) {
				Hashtable<String, Double> value = entrySet.getValue();
				for (Map.Entry<String, Double> toEntrySet : value.entrySet()) {
					String toKey = toEntrySet.getKey();
					double prob = toEntrySet.getValue();
					// prob = Math.log(prob);
					if (Double.isNaN(prob)) {
						System.out.println("Error NaN values ....");
					}
					if (states.contains(toKey)) {
						aTrans[states.indexOf(fromKey)][states.indexOf(toKey)] = prob;
					}
				}
			}
		}
		return aTrans;
	}

	/**
	 * Filter a list by removing element not matched with the regular expression
	 * passed.
	 *
	 * @param list
	 * @param regx
	 * @return
	 */
	public ArrayList<String> filter(List<String> list, String regx) {
		ArrayList<String> nl = new ArrayList<>();
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String next = iterator.next();
			if (next.contains(regx)) {
				nl.add(next);
			}
		}
		return nl;
	}

	public Hashtable<String, ArrayList<String>> gen2From(Hashtable<String, Hashtable<String, Double>> trans) {
		Hashtable<String, ArrayList<String>> map2f = new Hashtable<>();
		for (Map.Entry<String, Hashtable<String, Double>> transEntry : trans.entrySet()) {
			String fromKey = transEntry.getKey();
			Hashtable<String, Double> value = transEntry.getValue();
			for (Map.Entry<String, Double> entrySet : value.entrySet()) {
				String toKey = entrySet.getKey();
				if (map2f.containsKey(toKey)) {
					ArrayList<String> fromkies = map2f.get(toKey);
					if (!fromkies.contains(fromKey)) {
						fromkies.add(fromKey);
						map2f.replace(toKey, fromkies);
					}
				} else {
					ArrayList<String> fromkies = new ArrayList<>();
					fromkies.add(fromKey);
					map2f.put(toKey, fromkies);
				}

			}

		}
		return map2f;
	}

	public Hashtable<String, Hashtable<String, Double>> getEmissionProb(
			Hashtable<String, Hashtable<String, Double>> trans) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();
		// String [] states = getStates(trans);
		/**
		 * It is impossible to have an exit point related to two different
		 * voronoi zones, so each table created for points to zones emissions
		 * will contain only single element with probability equals 1
		 */

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissions.entrySet()) {
			String fromKey = entrySet.getKey();
			Hashtable<String, Double> value = entrySet.getValue();
			if (!emissions.containsKey(fromKey)) {
				Hashtable<String, Double> tmp = new Hashtable<>();
				tmp.put(String.valueOf(getNode(fromKey).getZone()), 1.0);
				emissions.put(fromKey, tmp);
			}
			for (Map.Entry<String, Double> toEntrySet : value.entrySet()) {
				String toKey = toEntrySet.getKey();
				if (!emissions.containsKey(toKey)) {
					Hashtable<String, Double> tmp = new Hashtable<>();
					tmp.put(String.valueOf(getNode(toKey).getZone()), 1.0);
					emissions.put(toKey, tmp);
				}

			}

		}
		return emissions;
	}

	public Hashtable<String, Hashtable<String, Double>> getEmissionProb(
			Hashtable<String, Hashtable<String, Double>> trans, Hashtable<Integer, ArrayList<Integer>> voronoiNeibors) {
		Hashtable<String, Hashtable<String, Double>> emissions = new Hashtable<>();
		/**
		 * It is impossible to have an exit point related to two different
		 * voronoi zones, so each table created for points to zones emissions
		 * will contain only single element with probability equals 1
		 */
		for (Map.Entry<String, Hashtable<String, Double>> entry : trans.entrySet()) {
			String id = entry.getKey();
			Hashtable<String, Double> tmp = new Hashtable<>();
			int zone = getNode(id).getZone();
			tmp.put(String.valueOf(zone), 0.5);
			ArrayList<Integer> neighbours = voronoiNeibors.get(zone);
			int length = neighbours.size();
			for (Iterator<Integer> iterator = neighbours.iterator(); iterator.hasNext();) {
				Integer next = iterator.next();
				tmp.put(String.valueOf(next), 0.5 / length);
			}
			emissions.put(id, tmp);
		}

		return emissions;
	}

	public ArrayList<String> getExts() {
		ArrayList<String> exts = new ArrayList<>();
		for (Iterator<FromNode> iterator = networkDist.iterator(); iterator.hasNext();) {
			FromNode next = iterator.next();
			if (next.isIsExit()) {
				exts.add(next.getID());
			}

		}
		// Sets.powerSet(Sets.newConcurrentHashSet(exts));
		return exts;
	}

	public ArrayList<String> getExts(Hashtable<String, Hashtable<String, Double>> trans) {

		ArrayList<String> exts = new ArrayList<>();

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : trans.entrySet()) {
			String from_key = entrySet.getKey();
			Hashtable<String, Double> value = entrySet.getValue();
			if (!exts.contains(from_key)) {
				exts.add(from_key);
			}
			for (Map.Entry<String, Double> entry : value.entrySet()) {
				String to_key = entry.getKey();
				if (!exts.contains(to_key)) {
					exts.add(to_key);
				}
			}

		}
		// for (Iterator<FromNode> iterator = networkDist.iterator();
		// iterator.hasNext();) {
		// FromNode next = iterator.next();
		// if (next.isIsExit()) {
		// exts.add(next.getID());
		// }
		//
		// }
		// Sets.powerSet(Sets.newConcurrentHashSet(exts));
		return exts;
	}

	public Hashtable<String, Integer> getMapNodes() {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}
		Hashtable<String, Integer> mapNodes = new Hashtable<>();
		for (Iterator<FromNode> iterator = networkDist.iterator(); iterator.hasNext();) {
			FromNode node = iterator.next();
			if (node.isIsExit()) {
				mapNodes.put(node.getID(), node.getZone());
			}

		}

		return mapNodes;
	}

	public ArrayList<FromNode> getNetworkDist() {
		return networkDist;
	}

	private FromNode getNode(String id) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		for (FromNode fromNode : networkDist) {
			if (fromNode.getID().equals(id)) {
				return fromNode;
			}
		}
		return null;
	}

	public Hashtable<Integer, String> getObservations() {
		return observations;
	}

	public Integer[] getObsSequence(int sZone, int eZone) {
		ArrayList<Integer> obs = new ArrayList<>();

		/**
		 * do something here
		 */
		return obs.toArray(new Integer[obs.size()]);
	}

	public Hashtable<String, Double> getStartProb(Hashtable<String, Hashtable<String, Double>> emissionProb) {
		Hashtable<String, Double> strtPrb = new Hashtable<>();
		double sum = 0;
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissionProb.entrySet()) {
			String pKey = entrySet.getKey();
			strtPrb.put(pKey, (1.0 / emissionProb.size()));
		}
		return strtPrb;
	}

	public Hashtable<String, Double> getStartProb(int zoneID,
			Hashtable<String, Hashtable<String, Double>> emissionProb) {
		Hashtable<String, Double> strtPrb = new Hashtable<>();
		double sum = 0;
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissionProb.entrySet()) {
			String pKey = entrySet.getKey();
			// System.out.println(pKey);
			if (entrySet.getValue().containsKey(String.valueOf(zoneID))) {
				double val = entrySet.getValue().get(String.valueOf(zoneID)) + Math.random();
				sum += val;
				strtPrb.put(pKey, val);
			} else {
				strtPrb.put(pKey, 0.0);
			}
		}

		for (Map.Entry<String, Double> entry : strtPrb.entrySet()) {
			String string = entry.getKey();
			Double double1 = entry.getValue();
			strtPrb.replace(string, (double) (double1));
		}

		return strtPrb;
	}

	public Hashtable<String, Double> getStartProb(String[] obs,
			Hashtable<String, Hashtable<String, Double>> emissionProb) {
		Hashtable<String, Double> strtPrb = new Hashtable<>();
		double sum = 0;
		for (int i = 0; i < obs.length; i++) {
			int zoneID = Integer.parseInt(obs[i]);
			for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissionProb.entrySet()) {
				String pKey = entrySet.getKey();
				// System.out.println(pKey);
				if (entrySet.getValue().containsKey(String.valueOf(zoneID))) {
					double val = entrySet.getValue().get(String.valueOf(zoneID)) + Math.random();
					sum += val;
					strtPrb.put(pKey, val);
					// break;
				}
			}

		}

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : emissionProb.entrySet()) {
			String pKey = entrySet.getKey();
			if (!strtPrb.containsKey(pKey)) {
				strtPrb.put(pKey, 0.0);
			}
		}

		for (Map.Entry<String, Double> entry : strtPrb.entrySet()) {
			String string = entry.getKey();
			Double double1 = entry.getValue();
			strtPrb.replace(string, double1 / sum);
		}

		return strtPrb;
	}

	public String[] getStates(Hashtable<String, Hashtable<String, Double>> table) {
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		String states[] = new String[table.size()];
		int i = 0;
		for (Map.Entry<String, Hashtable<String, Double>> entrySet : table.entrySet()) {
			states[i++] = entrySet.getKey();
		}
		// String states[] = emissions.keySet().toArray(new
		// String[emissions.keySet().size()]);

		return states;
	}

	/**
	 * Transition probability is the relation between states, the transition
	 * from one point to another
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> getTransitionProb() {
		Hashtable<String, Hashtable<String, Double>> transtions = new Hashtable<>();
		Hashtable<String, Double> toNodesTable = null;
		if (networkDist == null || networkDist.isEmpty()) {
			return null;
		}

		for (FromNode fromNode : networkDist) {
			ArrayList<ToNode> toNodesList = fromNode.getToedges();
			for (ToNode toNode : toNodesList) {
				toNodesTable = new Hashtable<>();
				toNodesTable.put(toNode.getID(), toNode.getProb());
			}
			transtions.put(fromNode.getID(), toNodesTable);
		}

		return transtions;
	}

	/**
	 * Sun 31 May 2015 01:06:32 AM JST
	 *
	 * @param num
	 * @return return the next highest power of two of the number passed if it
	 *         is not power of two.
	 */
	public int highestOneBit(int num) {
		int pow2 = Integer.highestOneBit(num);
		if (num > pow2) {
			pow2 *= 2;
		}
		return pow2;
	}

	public double[][] matrix_padding(double[][] org_matrix) {
		int rows = highestOneBit(org_matrix.length);
		int columns = highestOneBit(org_matrix[0].length);

		double mod_matrix[][] = new double[rows][columns];
		for (int i = 0; i < org_matrix.length; i++) {
			double[] org_matrix1 = org_matrix[i];
			for (int j = 0; j < org_matrix1.length; j++) {
				double p = org_matrix1[j];
				mod_matrix[i][j] = p;

			}
		}
		return mod_matrix;
	}

	public void printTable(Hashtable<String, Hashtable<String, Double>> prob) {

		for (Map.Entry<String, Hashtable<String, Double>> entrySet : prob.entrySet()) {
			String pKey = entrySet.getKey();
			// System.out.println(pKey);
			Hashtable<String, Double> value = entrySet.getValue();
			for (Map.Entry<String, Double> entrySet1 : value.entrySet()) {
				String zoneKey = entrySet1.getKey();
				Double probability = entrySet1.getValue();
				if (Integer.parseInt(zoneKey) == 2) {
					System.out.format("\t%s\t%f\n", pKey, probability);
				}

			}

		}
	}

	/**
	 *
	 * @return
	 */
	public ArrayList<FromNode> readNetworkDist(String netPath) {

		FromNode fromNode;
		org.w3c.dom.Document dpDoc;
		// for (int zone = 1; zone < 300; zone++) {
		// System.out.println("zone:\t"+zone);
		File dpXmlFile = new File(netPath);
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
					fromNode.setID(eElement.getAttribute("id"));
					// if (fromNode.getID().startsWith(":")) {
					// continue;
					// }
					fromNode.setX(Double.parseDouble(eElement.getAttribute("x")));
					fromNode.setY(Double.parseDouble(eElement.getAttribute("y")));
					fromNode.setIsExit(Boolean.parseBoolean(eElement.getAttribute("isExt")));

					// System.out.println("eElement.getAttribute(\"isExt\")\t"+eElement.getAttribute("isExt"));
					// System.out.println("isExit\t"+fromNode.isIsExit());
					fromNode.setZone(Integer.parseInt(eElement.getAttribute("zone")));

					ArrayList<ToNode> toNodesList = new ArrayList<>();
					ToNode edge;
					NodeList toNodeList = eElement.getElementsByTagName("toNode");

					for (int j = 0; j < toNodeList.getLength(); j++) {
						Node toNodeNode = toNodeList.item(j);

						if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
							edge = new ToNode();
							Element toNodeElement = (Element) toNodeNode;
							edge.setID(toNodeElement.getAttribute("id"));
							edge.setProb(Double.parseDouble(toNodeElement.getAttribute("probability")));
							edge.setX(Double.parseDouble(toNodeElement.getAttribute("x")));
							edge.setY(Double.parseDouble(toNodeElement.getAttribute("y")));
							toNodesList.add(edge);
						}

					}
					fromNode.setToedges(toNodesList);
					networkDist.add(fromNode);
				}

			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}
		// }
		return this.networkDist;
	}

	/**
	 * Read observations without dividing it into days
	 *
	 * @param path
	 * @return
	 */
	public Hashtable<Integer, String> readObservations(String path) {
		org.w3c.dom.Document dpDoc;
		File dpXmlFile = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dpDoc = dBuilder.parse(dpXmlFile);
			dpDoc.getDocumentElement().normalize();
			NodeList ObsList = dpDoc.getElementsByTagName("user");

			for (int i = 0; i < ObsList.getLength(); i++) {

				Node obsNode = ObsList.item(i);

				if (obsNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) obsNode;
					// System.out.println("From Node : " +
					// eElement.getAttribute("id"));
					int usrID = Integer.parseInt(eElement.getAttribute("id"));
					String obs = eElement.getAttribute("sitesSequanace");
					observations.put(usrID, obs);

				}
			}

		} catch (ParserConfigurationException | SAXException | IOException ex) {
			Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
		}
		System.out.println("observations\t" + observations.size());
		return this.observations;
	}

	/**
	 * Read divided CDRs into days
	 *
	 * @param path
	 * @return Hashtable<Month day number, Hashtable<User ID, Observation
	 *         sequences>>, observation sequence may contain more than
	 *         sub-sequences
	 */
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
	 * Read both the transitions probabilities XML file and emissions as well.
	 *
	 * @param path
	 * @return
	 */
	public Hashtable<String, Hashtable<String, Double>> readProbXMLTable(String path) {
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
							double prob = Double.parseDouble(toNodeElement.getAttribute("probability"));
							if (Double.isNaN(prob)) {
								prob = 0;
							}
							toTable.put(toNodeElement.getAttribute("id"), prob);
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

	public void setNetworkDist(ArrayList<FromNode> networkDist) {
		this.networkDist = networkDist;
	}

	public void setObservations(Hashtable<Integer, String> observations) {
		this.observations = observations;
	}

	public void WriteCSVFile(double[][] table, String path) {

		try {
			File file = new File(path);
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < table.length; i++) {
				double[] table1 = table[i];
				for (int j = 0; j < table1.length; j++) {
					double u = table1[j];
					output.write(u + ",");

				}
				output.newLine();

			}

			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeXmlTable(Hashtable<String, Hashtable<String, Double>> prob, String path) {
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("exit-defs");
			doc.appendChild(rootElement);
			NumberFormat formatter = new DecimalFormat("0.#######E0");
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
					double probability = entrySet1.getValue();
					if (probability == 0) {
						continue;
					}
					Element toNodeElement = doc.createElement("to");
					fromNodeElement.appendChild(toNodeElement);

					// set attribute to staff element
					attr = doc.createAttribute("id");
					attr.setValue(zoneKey);
					toNodeElement.setAttributeNode(attr);

					attr = doc.createAttribute("probability");
					// attr.setValue(formatter.format(probability));
					attr.setValue(String.format("%2.4e", probability));
					// attr.setValue(String.valueOf(probability));
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
