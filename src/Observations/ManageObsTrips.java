/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class ManageObsTrips {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// final int stower = Integer.parseInt(args[0]);
		// final int etower = Integer.parseInt(args[1]);
		// final int dist_th = Integer.parseInt(args[2]);
		// final int time_th = Integer.parseInt(args[3]);
		// final boolean repeated_obs_flag = Boolean.parseBoolean(args[4]);
		// final boolean weekends = Boolean.parseBoolean(args[5]);
		// final boolean trips_flag = Boolean.parseBoolean(args[6]);

		String dSPath = args[0];
		String towerPath = args[1];

		// String dSPath = "/home/essam/traffic/SET2/tmp";
		// String towerPath = "/home/essam/traffic/DI/Dakar-2.1/towers.csv";

		final int stower = 0; // start tower
		final int etower = 1666; // end tower
		final int dist_th = 1000; // trips detection spatial threshold
		final int time_th = 60; // trip detection temporal threshold.
		final boolean repeated_obs_flag = true; // include repeated observation
												// flag.
		final boolean weekends = true; // include weekend data.
		/**
		 * Stop detection algorithm detect only stops not trips so make sure
		 * that you will generate stops for a HMM model.
		 */

		// String dSPath = "/home/essam/traffic/SET2/";
		// String towerPath =
		// "/home/essam/traffic/Dakar/Dakar_edge-200/towers.csv";

		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towerPath);
		ObsTripsBuilder builder = new ObsTripsBuilder();
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dSPath));
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (fileName.startsWith(".")) {
				continue;
			}
			if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {

				String dist = subsetPath + "." + stower + "_" + etower + "_th-dist_" + dist_th + "_th-time_" + time_th
						+ ".xml";

				/**
				 * final implementation (used to provide the current results)
				 */
				// builder.writeObsDUT(
				// builder.transposeWUT(
				// builder.filterTrips(
				// builder.adaptiveTrips(
				// builder.buildObsDUT(subsetPath, stower, etower),
				// towers, dist_th, time_th), f_time_th, trip_length)), dist);
				/**
				 * without repeated observations:
				 */
				// builder.writeObsDUT(
				// builder.transposeWUT(
				// // builder.filterTrips(
				// builder.adaptiveTrips(
				// builder.remove_repeated(
				// builder.remove_handovers(
				// builder.buildObsDUT(subsetPath, stower, etower))),
				// towers, dist_th, time_th)
				// // , f_time_th, trip_length)
				// ), dist);

				Hashtable<String, Hashtable<Integer, Obs>> obs_table;
				if (repeated_obs_flag) {
					obs_table = builder.transposeWUT(
							builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stower, etower)));
				} else {
					obs_table = builder.transposeWUT(builder.remove_repeated(
							builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stower, etower))));
				}

				Hashtable<String, Hashtable<Integer, Obs>> obs_stops = builder.Update_obs_table(obs_table, towers,
						weekends);

				ArrayList<Integer> lngths = builder.get_trips_length(obs_stops);
//				System.out.println(Arrays.deepToString(lngths.toArray()));
				builder.write_trips_length(lngths, subsetPath + "." + stower + "_" + etower + "_th-dist_" + dist_th
						+ "_th-time_" + time_th + ".csv");
				System.out.println(dist);
				ObsTripsBuilder.writeObsDUT(obs_stops, dist);

			}
		}
	}

}
