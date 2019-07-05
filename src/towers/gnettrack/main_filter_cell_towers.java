/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.gnettrack;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import utils.DataHandler;

/**
 *
 * @author essam
 * @date Sat Mar 19 16:30:16 EET 2016
 * 
 *       java -Xss1g -d64 -XX:+UseG1GC -Xms7g -Xmx7g -XX:MaxGCPauseMillis=500
 *       -cp
 *       .:../lib/jcoord-1.0.jar:../lib/diva.jar:../lib/jgrapht.jar:../lib/guava
 *       .jar:../lib/commons-math3-3.5.jar
 *       towers.gnettrack.main_filter_cell_towers
 */

public class main_filter_cell_towers {

	/**
	 * @param args
	 *            the command line arguments
	 */

	public static void main(String[] args) {
		// project_opencellid_towers();
//		project_towers();
		// project_all_towers();
		filter_towers(Double.parseDouble(args[0]));
	}

	public static void project_opencellid_towers() {
		// TODO code application logic here
		// 4, 6, 7
		// origBoundary="29.439067,30.781731,30.128385,31.322947"

		String cell_towers_path = "/home/essam/traffic/cell_towers.csv";
		String output_path = "/home/essam/traffic/models/alex";
		int indices[] = { 2, 4, 6, 7 };

		List<String[]> towers = DataHandler.extract_info(DataHandler.read_csv(cell_towers_path, ","), indices);

		// Alex coordinates ...
		double minlat = 30.8362, minlon = 29.4969, maxlat = 31.3302, maxlon = 30.0916;

		// double coords[] = {29.439067, 30.781731, 30.128385, 31.322947};
		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		// double w = Math.abs(xymax[0] - xymin[0]);
		// double h = Math.abs(xymax[1] - xymin[1]);
		// System.out.println("Width: " + w + "\thieght: " + h);
		// remove labels ..
		Hashtable<String, List<String[]>> data = new Hashtable<>();

		towers.remove(0);
		System.out.println("cell_id,net_id,x,y");
		for (String[] t : towers) {
			double lon = Double.parseDouble(t[2]);
			double lat = Double.parseDouble(t[3]);
			double[] xy = DataHandler.proj_coordinates(lat, lon);
			// check if it is contain long cellid
			/**
			 * Long cell ID vs. short cell ID: The formula for the long cell ID
			 * is as follows: Long CID = 65536 * RNC + CID RNC: the Radio
			 * Network Controller CID: a short cell ID (an integer in the range
			 * of 0 to 65535) If you have the Long CID, you can get RNC and CID
			 * in the following way: RNC = Long CID / 65536 (integer division)
			 * CID = Long CID mod 65536 (modulo operation) Example for long cell
			 * ID 66808694: RNC = 66808694 / 65536 = 1019 CID = 66808694 mod
			 * 65536 = 27510
			 */
			// if (Integer.parseInt(t[1]) > 65535) {
			// // http://wiki.opencellid.org/wiki/FAQ
			// System.out.println("Long cell ID ...... ");
			// }
			if (xy[0] >= xymin[0] && xy[0] <= xymax[0] && xy[1] >= xymin[1] && xy[1] <= xymax[1]) {
				if (data.containsKey(t[0])) {
					List<String[]> net = data.get(t[0]);
					String[] rec = { Integer.toString(Integer.parseInt(t[1]) % 65536),
							Double.toString((xy[0] - xymin[0])), Double.toString((xy[1] - xymin[1])) };
					net.add(rec);
					data.replace(t[0], net);
				} else {
					List<String[]> net = new ArrayList<>();
					String[] rec = { Integer.toString(Integer.parseInt(t[1]) % 65536),
							Double.toString((xy[0] - xymin[0])), Double.toString((xy[1] - xymin[1])) };
					net.add(rec);
					data.put(t[0], net);

				}
				// System.out.printf("%s,%s,%f,%f\n", t[1], t[0], (xy[0] -
				// xymin[0]), (xy[1] - xymin[1]));
			}
		}

		data.entrySet().stream().forEach(entry -> {
			String net = entry.getKey();
			List<String[]> fields = entry.getValue();
			DataHandler.write_csv(fields, output_path + "/network_" + net + ".csv");
		});

	}

	public static void project_towers() {

		String cell_towers_path = "/home/essam/traffic/models/SITE_ARR_LONLAT.CSV";
		int indices[] = { 0, 2, 3 };
		// Dakar coordinates ...
		double minlat = 14.5597, minlon = -17.5616, maxlat = 14.9036, maxlon = -17.1098;

		List<String[]> towers = DataHandler.extract_info(DataHandler.read_csv(cell_towers_path, ","), indices);

		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		System.out.println("cell_id,x,y");
		for (String[] t : towers) {
			double lon = Double.parseDouble(t[1]);
			double lat = Double.parseDouble(t[2]);
			double[] xy = DataHandler.proj_coordinates(lat, lon);
			if (xy[0] >= xymin[0] && xy[0] <= xymax[0] && xy[1] >= xymin[1] && xy[1] <= xymax[1]) {
				// if (minlon <= lon && maxlon >= lon && minlat <= lat && maxlat
				// >= lat) {
				// double[] xy = DataHandler.proj_coordinates(lat, lon);
				System.out.printf("%d,%f,%f\n", Integer.parseInt(t[0]), (xy[0] - xymin[0]), (xy[1] - xymin[1]));
			}
		}
	}

	public static void filter_towers(double scale) {

		String cell_towers_path = "/home/essam/traffic/models/SITE_ARR_LONLAT.CSV";
		int indices[] = { 0, 2, 3 };
		// Dakar coordinates ...
		double minlat = 14.5597, minlon = -17.5616, maxlat = 14.9036, maxlon = -17.1098;

		List<String[]> towers = DataHandler.extract_info(DataHandler.read_csv(cell_towers_path, ","), indices);
		//
		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		// double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);
		System.out.println("ID,lat,lon");
		
		for (String[] t : towers) {
			double lon = Double.parseDouble(t[1]);
			double lat = Double.parseDouble(t[2]);
			double[] xy = DataHandler.proj_coordinates(lat, lon);
			// if (xy[0] >= xymin[0] && xy[0] <= xymax[0] && xy[1] >= xymin[1]
			// && xy[1] <= xymax[1]) {
			if (minlon <= lon && maxlon >= lon && minlat <= lat && maxlat >= lat) {
				// double[] xy = DataHandler.proj_coordinates(lat, lon);
				System.out.printf("%d,%f,%f\n", Integer.parseInt(t[0]), lat * scale, lon * scale);
			}
		}
	}

	public static void project_all_towers() {

		String cell_towers_path = "/home/essam/traffic/models/SITE_ARR_LONLAT.CSV";
		int indices[] = { 0, 2, 3 };
		// senegal coordinates ...
		// <bounds minlat="12.082" minlon="-17.787" maxlat="16.857"
		// maxlon="-11.173"/>
		double minlat = 12.082, minlon = -17.787, maxlat = 16.857, maxlon = -11.173;

		List<String[]> towers = DataHandler.extract_info(DataHandler.read_csv(cell_towers_path, ","), indices);

		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		// System.out.println("cell_id,x,y");
		for (String[] t : towers) {
			double lon = Double.parseDouble(t[1]);
			double lat = Double.parseDouble(t[2]);
			double[] xy = DataHandler.proj_coordinates(lat, lon);
			// if (xy[0] >= xymin[0] && xy[0] <= xymax[0] && xy[1] >= xymin[1]
			// && xy[1] <= xymax[1]) {
			// if (minlon <= lon && maxlon >= lon && minlat <= lat && maxlat >=
			// lat) {
			// double[] xy = DataHandler.proj_coordinates(lat, lon);
			System.out.printf("%d,%f,%f\n", Integer.parseInt(t[0]), (xy[0] - xymin[0]), (xy[1] - xymin[1]));
			// }
		}
	}
}
