/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import Observations.ObsTripsBuilder;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class RunOD_1 {

	private static Hashtable<String, Multimap<String, Double>> construct_trips(List<String[]> routes,
			List<String[]> ts) {

		Hashtable<String, Multimap<String, Double>> flow = new Hashtable<>();
		for (int i = 0; i < ts.size(); i++) {
			String[] times = ts.get(i)[0].split(":");
			double deprt = Integer.parseInt(times[0]) * 60 * 60 + Integer.parseInt(times[1]) * 60
					+ Integer.parseInt(times[2]);
			String[] exts = routes.get(i);
			String strt = exts[0];
			String end = exts[exts.length - 1];

			int j = exts.length - 1;
			/**
			 * make sure that the start and the end of the trip are different,
			 * if both are equal get the element before the current from the end
			 * of the trip sequence.
			 */

			while (strt.equals(end) && j >= 0) {
				j--;
				end = exts[j];
			}
			if (j == 0) {
				continue;
			}

			// remove interpolation ..
			if (strt.contains("_interpolated")) {
				strt = strt.substring(0, strt.indexOf("_interpolated"));
			}

			if (end.contains("_interpolated")) {
				end = end.substring(0, end.indexOf("_interpolated"));
			}

			if (flow.containsKey(strt)) {
				Multimap<String, Double> tmp = flow.get(strt);
				tmp.put(end, deprt);
				flow.replace(strt, tmp);
			} else {
				Multimap<String, Double> tmp = ArrayListMultimap.create();
				tmp.put(end, deprt);
				flow.put(strt, tmp);
			}

		}
		return flow;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws ParseException {

		String viterbi_path = args[0];
		String output = args[1];

		String timestamp_path = output + "/trips_timestamps";
		String trips_path = output + "/interpolated_trips";

		//
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(viterbi_path));
		Vit2ODs v2od = new Vit2ODs();
		String routes_path = output + "/routes";
		File f = new File(routes_path);
		f.mkdir();

		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (fileName.startsWith(".")) {
				continue;
			}

			List<String[]> routes = DataHandler.read_routes(subsetPath);
			List<String[]> ts = DataHandler.read_routes(timestamp_path + "/" + fileName);
			List<String[]> trips = DataHandler.read_routes(trips_path + "/" + fileName);

			System.err.println("Routes size:\t" + routes.size() + "\ttime stamps size:\t" + ts.size()
					+ "\ttrips size:\t" + trips.size());
			// fileName = fileName.replace("viterbi.day.",
			// "trips.day.").replace(".CSV.", "_");
			System.out.println(fileName);
			// Hashtable<String, Hashtable<Integer, Obs>> obs =
			// ObsTripsBuilder.readObsDUT(subsetPath);

			// v2od.writeTrips(v2od.handleFlow(obs), path, from, to);
			// v2od.writeTripsWDepart(v2od.handleMFlowWDepart(obs), path);
			/**
			 * Thu 23 Apr 2015 01:06:49 PM JST Write OD without removing the
			 * interpolation parts for the road network segments.
			 */
			// v2od.writeTripsWDepart(v2od.handleFlowWDepart(obs), path);
			/**
			 * Thu 23 Apr 2015 01:06:49 PM JST write OD without interpolations.
			 */
			v2od.writeTripsWDepart_1(construct_trips(routes, ts), routes + "/" + fileName);
			// break;
		}
	}
}
