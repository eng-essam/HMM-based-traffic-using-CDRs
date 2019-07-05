/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import Voronoi.VoronoiConverter;
import trajectory.importance.TCluster;
import trajectory.importance.Trips;
import utils.CDR;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_interpolate_trips {

	private static String output;
	private static String trips_path;
	private static String timestamps_path;
	private static String interpolated_path;
	private static String trips_xml_path;

	public static void main(String[] args) {
		int s_twr = 0;
		int e_twr = 500;
		String poi_path = "/home/essam/traffic/Dakar/Jan_2016/clusters.xml";
		String cdrs_path = "/home/essam/traffic/SET2/tmp";
		String neighbors_path = "/home/essam/traffic/Dakar/Jan_2016/dakar.vor.neighborSitesForSite.csv";
		String towers_path = "/home/essam/traffic/Dakar/Jan_2016/towers.csv";
		String indicators = "/home/essam/traffic/SET2/indictors";
		trips_xml_path = "/home/essam/traffic/Dakar/Jan_2016/results/observations";

		// int s_twr = Integer.parseInt(args[0]);
		// int e_twr = Integer.parseInt(args[1]);
		// String poi_path = args[2];
		// String cdrs_path = args[3];
		// String neighbors_path = args[4];
		// String towers_path = args[5];
		// String indicators = args[6];
		// trips_xml_path = args[7];

		// trips_path = output + "/trips";
		// timestamps_path = output + "/trips_timestamps";
		// interpolated_path = output + "/interpolated_trips";
		// trips_xml_path = output + "/trips_xml";
		//
		// mkdirs();
		File txmld = new File(trips_xml_path);
		if (!txmld.exists()) {

			try {
				txmld.mkdir();
			} catch (SecurityException se) {
				System.err.println("Error creating XML file formate trips director");
			}
		}

		Hashtable<Integer, List<TCluster>> usrs_clusters = DataHandler.read_xml_poi(poi_path);
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(cdrs_path));

		Hashtable<Integer, Double> rg = DataHandler.extract_gyration_field(indicators);

		VoronoiConverter converter = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = converter.readVorNeighbors(neighbors_path);
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);

		files.stream().forEach((subsetPath) -> {
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (!(fileName.startsWith("."))) {
				fileName = fileName.substring(0, fileName.lastIndexOf("."));
				if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
					// List<CDR> datasets =
					// DataHandler.remove_repeated_obs(DataHandler.remove_handover(DataHandler.read_dataset(subsetPath)));
					// String day =
					// DataHandler.get_day_date(datasets.get(0).getDate());
					Trips trips_builder = new Trips(
							DataHandler.remove_repeated_obs(DataHandler.remove_handover(DataHandler.read_dataset(subsetPath))), towers,
							usrs_clusters);
					Hashtable<String, Hashtable<Integer, List<List<CDR>>>> trips = trips_builder.split_trips();
					System.out.println("finish trip splitting");

					// List<List<Integer>> org_trips = new ArrayList<>();
					// List<List<Date>> trips_timestamp = new ArrayList<>();
					// List<List<Integer>> interpolated_trips = new
					// ArrayList<>();
					System.out.println("Start interpolation ...");
					Hashtable<String, Hashtable<Integer, Obs>> obs = trips_builder.interpolate_trips(voronoi_neighbors,
							rg, trips);

					// trips_builder.write_trips_csv(org_trips, trips_path + "/"
					// + day + ".csv");
					// trips_builder.write_trips_csv(interpolated_trips,
					// interpolated_path + "/" + day + ".csv");
					// trips_builder.write_timestamp_csv(trips_timestamp,
					// timestamps_path + "/" + day + ".csv");
					ObsTripsBuilder.writeObsDUT(obs, trips_xml_path + "/" + fileName + ".xml");
				}
			}
		});

	}

	private static void mkdirs() {

		File td = new File(trips_path);
		File tsd = new File(timestamps_path);
		File txmld = new File(trips_xml_path);
		File itd = new File(interpolated_path);

		// if the directory does not exist, create it
		if (!td.exists()) {
			try {
				td.mkdir();
			} catch (SecurityException se) {
				System.err.println("Error creating trips director");
			}
		}

		if (!tsd.exists()) {

			try {
				tsd.mkdir();
			} catch (SecurityException se) {
				System.err.println("Error creating time stamps director");
			}
		}

		if (!txmld.exists()) {

			try {
				txmld.mkdir();
			} catch (SecurityException se) {
				System.err.println("Error creating XML file formate trips director");
			}
		}

		if (!itd.exists()) {
			try {
				itd.mkdir();
			} catch (SecurityException se) {
				System.err.println("Error creating interpolated trips director");
			}
		}

	}
}
