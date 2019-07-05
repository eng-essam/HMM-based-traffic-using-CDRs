/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.MapDetails;
import utils.DataHandler;
import utils.FromNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class RunGravity_Zones_final {

	public static Hashtable<Double, ArrayList<Double>> limit_flow(Hashtable<Double, ArrayList<Double>> flow,
			double limit) {
		Hashtable<Double, ArrayList<Double>> limited = new Hashtable<>();
		for (Map.Entry<Double, ArrayList<Double>> entrySet : flow.entrySet()) {
			double key = entrySet.getKey();
			ArrayList<Double> value = entrySet.getValue();
			if (key <= limit) {
				limited.put(key, value);
			}
		}
		return limited;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String obs_path = args[0];

		String towersPath = args[1];
		// String vitPath =
		// "/home/essam/traffic/sep_2015/Dakar_edge-200/results/set2/SET2_P02.CSV.0_300_th-dist_1000_th-time_60.xml";
		// String mapPath =
		// "/home/essam/traffic/sep_2015/Dakar_edge-200/Dakar.xy.dist.vor.xml";
		// String towersPath =
		// "/home/essam/traffic/sep_2015/Dakar_edge-200/Dakar.vor.towers.csv";

		final double threshold = 1000f;
		// double flow_limit = 4;
		boolean verbose = false;
		boolean intercept = true;
		// final int trip_length = 3;

		MapDetails details = new MapDetails();
		Hashtable<Integer, Vertex> towers = details.readTowers(towersPath);
		// System.out.println("towers\t"+towers.size());
		GravityModel model = new GravityModel();
		int days_cnt = 0;
		
//		Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obs_path);

		String fgod_csv_path = obs_path + "/data";
		File dir = new File(fgod_csv_path);
		dir.mkdir();

		Hashtable<String, Hashtable<String, Double>> distances = model.calcDistances(towers);
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(obs_path));
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1,subsetPath.lastIndexOf(".") );
			// fileName = fileName.substring(0, fileName.lastIndexOf(".csv")+1);
//			if (fileName.startsWith(".")
//					|| fileName.contains(
//							"Sunday") /**
//										 * || fileName.contains("Friday")
//										 */
//					|| fileName.contains("Saturday")) {
//				continue;
//			}

			// String day =
			// subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") +
			// "viterbi.day.".length(), subsetPath.lastIndexOf("."));
			// Hashtable<String, Hashtable<Integer, Obs>> obs =
			// ObsTripsBuilder.readObsDUT(subsetPath);
			if (!subsetPath.contains("CSV.0_300_th-dist_1000_th-time_60"))
				continue;
			Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);

			model = new GravityModel();
			model.setDistances(distances);
			// System.out.println("distances\t"+distances.size());
			List<String[]> trips = DataHandler.get_trips(obs);
			model.handle_zones_Flow(trips);

			Hashtable<Double, ArrayList<Double>> fgod;

			if (verbose) {
				/**
				 * Print verbose flow data
				 */
				System.out.println("-------------------------------------------------");
				// System.out.println("Number of days" + days_cnt);
				System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "From-zone", "To-zone", "Dist", "Flow", "Outs", "Ins",
						"GOD");
				fgod = model.computeGOD(threshold, verbose);
				System.out.println("-------------------------------------------------");
			} else {
				fgod = model.computeGOD(threshold, verbose);
			}

			String fgod_path = fgod_csv_path + "/fgod." + fileName;
			model.writeFGOD(fgod_path, fgod);
			Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
			fgod_path = fgod_csv_path + "/avg.fgod." + fileName;
			model.writeAvgGOD(fgod_path, avgGOD);

			System.out.printf("day %s\tGravity vs. flow R^2=%f\tAvg Gravity vs. flow R^2=%f\n", fileName,
					model.find_all_linear_regression(fgod, intercept),
					model.find_avg_linear_regression(avgGOD, intercept));

		}

		// Hashtable<String, Hashtable<String, Double>> distances = new
		// Hashtable<>();
		//
		// model.setDistances(distances);
		// model.calcDistances(map);
		/**
		 * Avg flow
		 */
		// model.average_flow(days_cnt);
	}
}
