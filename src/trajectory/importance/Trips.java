/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import AViterbi.Interpolate.Interpolation;
import Observations.Obs;
import utils.CDR;
import utils.DataHandler;
import utils.Region;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class Trips {

	// table of important places for all users ..
	Hashtable<Integer, List<Integer>> ip;
	Hashtable<Integer, Integer> usrs_home;
	Hashtable<Integer, Integer> usrs_work;
	List<CDR> datasets;

	Hashtable<Integer, List<TCluster>> usrs_clusters;
	Hashtable<String, Hashtable<Integer, List<CDR>>> splitted_dataset;
	Hashtable<Integer, Vertex> towers;

	// private int s_twrs = 0;
	// private int e_twrs = Integer.MAX_VALUE;

	public Trips(List<CDR> datasets, Hashtable<Integer, Vertex> twrs, Hashtable<Integer, List<TCluster>> uc) {
		this.datasets = datasets;
		ip = new Hashtable<>();
		usrs_home = new Hashtable<>();
		usrs_work = new Hashtable<>();
		usrs_clusters = uc;
		towers = twrs;
		// this.s_twrs = s_twrs;
		// this.e_twrs = e_twrs;

		// System.out.println("Original data:\t" +
		// Utils.to_string_cdr(datasets));
		get_important_places();

		filter_out_cdr();
		// System.out.println("Data after filtering it:\t" +
		// Utils.to_string_cdr(datasets));

		split_data();
		// System.out.println("After splitting data:");
		// for (Map.Entry<String, Hashtable<Integer, List<CDR>>> entry :
		// splitted_dataset.entrySet()) {
		// String key = entry.getKey();
		// Hashtable<Integer, List<CDR>> value = entry.getValue();
		// for (Map.Entry<Integer, List<CDR>> entry1 : value.entrySet()) {
		// Integer key1 = entry1.getKey();
		// List<CDR> value1 = entry1.getValue();
		// System.out.printf("Day %s\t User: %d\t Obs: %s\n", key, key1,
		// Utils.to_string_cdr(value1));
		//
		// }
		//
		// }

		// handle_clusters_data();
		//
		// System.out.println("After handling data:");
		// for (Map.Entry<String, Hashtable<Integer, List<CDR>>> entry :
		// splitted_dataset.entrySet()) {
		// String key = entry.getKey();
		// Hashtable<Integer, List<CDR>> value = entry.getValue();
		// for (Map.Entry<Integer, List<CDR>> entry1 : value.entrySet()) {
		// Integer key1 = entry1.getKey();
		// List<CDR> value1 = entry1.getValue();
		// System.out.printf("Day %s\t User: %d\t Obs: %s\n", key, key1,
		// Utils.to_string_cdr(value1));
		//
		// }
		//
		// }

	}

	public Trips(List<CDR> datasets, Hashtable<Integer, List<TCluster>> uc) {
		this.datasets = datasets;
		ip = new Hashtable<>();
		usrs_home = new Hashtable<>();
		usrs_work = new Hashtable<>();
		usrs_clusters = uc;

		get_important_places();

		split_data();
		handle_clusters_data();
	}

	private void convert_cdrs(List<List<CDR>> usr_trips, List<List<Integer>> trips, List<List<Date>> trips_timestamp) {
		for (int i = 0; i < usr_trips.size(); i++) {
			List<CDR> t = usr_trips.get(i);
			List<Integer> int_t = new ArrayList<>();
			List<Date> int_ts = new ArrayList<>();
			for (int j = 0; j < t.size(); j++) {
				CDR obs = t.get(j);
				int_t.add(obs.twr_id);
				int_ts.add(obs.date);
			}
			trips.add(int_t);
			trips_timestamp.add(int_ts);
		}

	}

	public void filter_out_cdr() {
		List<CDR> cdrs = new ArrayList<>();

		datasets.stream().filter((rec) -> (towers.containsKey(
				rec.twr_id)/* rec.twr_id >= s_twrs && rec.twr_id <= e_twrs */)).forEach((rec) -> {
					cdrs.add(rec);
				});

		System.out.println("Dataset after filtering:\t" + cdrs.size());
		this.datasets = new ArrayList<>(cdrs);
	}

	/**
	 * Get user info from users clusters ..
	 *
	 * @param usrs_clusters
	 *
	 */
	private void get_important_places() {

		for (Map.Entry<Integer, List<TCluster>> entry : usrs_clusters.entrySet()) {
			Integer usr_key = entry.getKey();
			List<TCluster> clusters = entry.getValue();
			List<Integer> pois = new ArrayList<>();

			clusters.stream().map((tc) -> {
				pois.add(tc.centroid);
				pois.addAll(tc.get_cluster_members());
				return tc;
			}).forEach((tc) -> {
				if (tc.is_home_loc()) {
					usrs_home.put(usr_key, tc.centroid);
				} else if (tc.is_work_loc()) {
					usrs_work.put(usr_key, tc.centroid);
				}
			});
			ip.put(usr_key, pois);
		}
	}

	/**
	 * Wed Jan 6 13:06:43 EET 2016
	 *
	 * Replace the occurrence of clusters elements with the centroid of the
	 * cluster as a representative ..
	 *
	 * @param datasets
	 * @return
	 */
	// private void handle_clusters_data() {
	// for (Map.Entry<Integer, List<TCluster>> entry : usrs_clusters.entrySet())
	// {
	// int usr_key = entry.getKey();
	// List<TCluster> clusters = entry.getValue();
	// for (TCluster tc : clusters) {
	// int centroid = tc.get_centroid();
	// List<Integer> members = tc.get_cluster_members();
	//
	// for (Map.Entry<String, Hashtable<Integer, List<CDR>>> dataset_entry :
	// splitted_dataset.entrySet()) {
	// String day_key = dataset_entry.getKey();
	// Hashtable<Integer, List<CDR>> day_data = dataset_entry.getValue();
	// if (day_data.containsKey(usr_key)) {
	// List<CDR> usr_data = day_data.get(usr_key);
	// List<CDR> update_usr_data = new ArrayList<>();
	// usr_data.stream().map((cdr) -> {
	// if (members.contains(cdr.twr_id)) {
	// cdr.set_twr_id(centroid);
	// }
	// return cdr;
	// }).forEach((cdr) -> {
	// update_usr_data.add(cdr);
	// });
	//
	// day_data.replace(usr_key,
	// Utils.remove_repeated_obs(Utils.remove_handover(update_usr_data)));
	// splitted_dataset.replace(day_key, day_data);
	// }
	// }
	// }
	//
	// }
	// }
	//
	// /**
	// *
	// */
	private void handle_clusters_data() {

		splitted_dataset.entrySet().stream().forEach((entry) -> {
			String day_key = entry.getKey();
			Hashtable<Integer, List<CDR>> day_data = entry.getValue();
			day_data.entrySet().stream().forEach((day_it) -> {
				Integer usr_key = day_it.getKey();
				List<CDR> usr_data = day_it.getValue();
				day_data.replace(usr_key, replace_usr_clusters(usr_key, usr_data));
			});
			splitted_dataset.replace(day_key, day_data);
		});
		// System.out.println("Finish handle cluster data");
	}

	private Hashtable<Integer, Obs> interpolate_day_trips(Hashtable<Integer, Vertex> towers,
			Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors, Hashtable<Integer, Double> rg,
			Hashtable<Integer, List<List<CDR>>> day_trips) {

		Interpolation interpolate = new Interpolation(voronoi_neighbors, towers);

		Hashtable<Integer, Obs> day_obs = new Hashtable<>();

		day_trips.entrySet().stream().forEach((entry) -> {
			int usr_key = entry.getKey();
			double u_rg = rg.get(usr_key) / 1000;
			List<List<CDR>> usr_trips = entry.getValue();
			if (!(usr_trips.isEmpty())) {
				List<List<Integer>> u_trips = new ArrayList<>();
				List<List<Date>> u_trips_timestamp = new ArrayList<>();
				convert_cdrs(usr_trips, u_trips, u_trips_timestamp);
				String str_trips = "";
				String str_ts = "";
				for (int i = 0; i < u_trips.size(); i++) {

					List<Integer> t = u_trips.get(i);
					if (t.isEmpty()) {
						continue;
					}
					List<Integer> it;
					if (u_rg < 11) {
						it = interpolate.linear_interpolator(t);
					} else {
						// System.out.println("Cubic interpolated
						// trip:\t"+Utils.to_string(t));
						it = interpolate.hermite_interpolator(t);
					}
					// System.out.println(Utils.to_string(t));
					if (!str_trips.isEmpty()) {
						str_trips += DataHandler.SLASH_SEP;
						str_ts += DataHandler.SLASH_SEP;
					}
					str_trips += DataHandler.to_string(it);
					str_ts += DataHandler.to_string_date(u_trips_timestamp.get(i));

				}
				if (!str_trips.isEmpty()) {
					day_obs.put(usr_key, new Obs(str_trips, str_ts));
				}
			}
		});

		return day_obs;
	}

	public Hashtable<String, Hashtable<Integer, Obs>> interpolate_trips(
			Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors, Hashtable<Integer, Double> rg,
			Hashtable<String, Hashtable<Integer, List<List<CDR>>>> trips) {
		Hashtable<String, Hashtable<Integer, Obs>> obs = new Hashtable<>();

		trips.entrySet().stream().forEach((entry) -> {
			String day_key = entry.getKey();
			Hashtable<Integer, List<List<CDR>>> day_trips = entry.getValue();
			obs.put(day_key, interpolate_day_trips(towers, voronoi_neighbors, rg, day_trips));
		});
		return obs;
	}

	/**
	 *
	 * @param day
	 * @param voronoi_regions
	 * @param towers
	 * @param voronoi_neighbors
	 * @param usrs_trips
	 * @param trips
	 * @param trips_timestamp
	 * @param interpolated_trips
	 * @return
	 */
	public Hashtable<String, Hashtable<Integer, Obs>> interpolate_trips(String day, ArrayList<Region> voronoi_regions,
			Hashtable<Integer, Vertex> towers, Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors,
			Hashtable<Integer, Double> rg, Hashtable<Integer, List<List<CDR>>> usrs_trips, List<List<Integer>> trips,
			List<List<Date>> trips_timestamp, List<List<Integer>> interpolated_trips) {

		Interpolation interpolate = new Interpolation(voronoi_neighbors, towers);

		Hashtable<String, Hashtable<Integer, Obs>> obs = new Hashtable<>();
		Hashtable<Integer, Obs> day_obs = new Hashtable<>();

		for (Map.Entry<Integer, List<List<CDR>>> entry : usrs_trips.entrySet()) {
			int usr_key = entry.getKey();

			double u_rg = rg.get(usr_key) / 1000;
			List<List<CDR>> usr_trips = entry.getValue();

			if (usr_trips.isEmpty()) {
				continue;
			}
			List<List<Integer>> u_trips = new ArrayList<>();
			List<List<Date>> u_trips_timestamp = new ArrayList<>();

			convert_cdrs(usr_trips, u_trips, u_trips_timestamp);

			String str_trips = "";
			String str_ts = "";

			for (int i = 0; i < u_trips.size(); i++) {

				List<Integer> t = u_trips.get(i);
				if (t.isEmpty()) {
					continue;
				}
				List<Integer> it;
				if (u_rg < 11) {
					it = interpolate.linear_interpolator(t);
				} else {
					// System.out.println("Cubic interpolated
					// trip:\t"+DataHandler.to_string(t));
					it = interpolate.hermite_interpolator(t);
				}
				// System.out.println(DataHandler.to_string(t));
				if (!str_trips.isEmpty()) {
					str_trips += DataHandler.SLASH_SEP;
					str_ts += DataHandler.SLASH_SEP;
				}
				str_trips += DataHandler.to_string(it);
				str_ts += DataHandler.to_string_date(u_trips_timestamp.get(i));
				interpolated_trips.add(it);
				trips.add(t);
				trips_timestamp.add(u_trips_timestamp.get(i));
			}
			if (!str_trips.isEmpty()) {
				day_obs.put(usr_key, new Obs(str_trips, str_ts));
			}
		}
		obs.put(day, day_obs);
		return obs;
	}

	private List<CDR> replace_usr_clusters(int usr_id, List<CDR> cdrs) {
		List<TCluster> clusters = usrs_clusters.get(usr_id);
		List<CDR> update_usr_data = new ArrayList<>(cdrs);

		clusters.stream().forEach((tc) -> {
			int centroid = tc.get_centroid();
			if (towers.containsKey(
					centroid) /* centroid >= s_twrs && centroid <= e_twrs */) {
				List<Integer> members = tc.get_cluster_members();

				for (int i = 0; i < cdrs.size(); i++) {
					CDR cdr = cdrs.get(i);
					if (members.contains(cdr.twr_id)) {
						cdr.set_twr_id(centroid);
					}
					update_usr_data.set(i, cdr);
				}
			}
		});
		// System.out.println("replace_usr_clusters - Org
		// data:\t"+DataHandler.to_string_cdr(cdrs)+"\tnew
		// one:\t"+DataHandler.to_string_cdr(update_usr_data));
		return DataHandler.remove_repeated_obs(update_usr_data);
	}

	/**
	 * Split data per day per user
	 *
	 * @return
	 */
	public Hashtable<String, Hashtable<Integer, List<CDR>>> split_data() {
		splitted_dataset = new Hashtable<>();
		System.out.println("Dataset size (split_data function):\t" + datasets.size());
		datasets.stream().forEach((cdr) -> {
			String day = DataHandler.get_day_date(cdr.getDate());
			if (splitted_dataset.containsKey(day)) {
				Hashtable<Integer, List<CDR>> day_data = splitted_dataset.get(day);
				if (day_data.containsKey(cdr.id)) {
					List<CDR> usr_data = day_data.get(cdr.id);
					usr_data.add(cdr);
					day_data.replace(cdr.id, usr_data);
				} else {
					List<CDR> usr_data = new ArrayList<>();
					usr_data.add(cdr);
					day_data.put(cdr.id, usr_data);
				}
				splitted_dataset.replace(day, day_data);
			} else {

				Hashtable<Integer, List<CDR>> day_data = new Hashtable<>();
				List<CDR> usr_data = new ArrayList<>();
				usr_data.add(cdr);
				day_data.put(cdr.id, usr_data);
				splitted_dataset.put(day, day_data);
			}
		});
		return splitted_dataset;
	}

	/**
	 * construct trips for all users for a single day ..
	 *
	 * @param s_twrs
	 * @param e_twrs
	 * @return
	 */
	public Hashtable<Integer, List<List<CDR>>> split_day_trips(Hashtable<Integer, List<CDR>> day_data) {
		Hashtable<Integer, List<List<CDR>>> trips = new Hashtable<>();

		day_data.entrySet().stream().forEach((entry) -> {
			int usr_key = entry.getKey();
			List<CDR> value = entry.getValue();
			if (/* !(value.isEmpty()) */value.size() > 1) {
				if (usrs_home.containsKey(usr_key)) {
					if (towers.containsKey(usrs_home.get(
							usr_key))/*
										 * usrs_home.get(usr_key) > s_twrs &&
										 * usrs_home.get(usr_key) < e_twrs
										 */) {
						// System.out.println("User " + usr_key + "\thome
						// location: " + usrs_home.get(usr_key));
						// System.out.println("Records:\t" +
						// DataHandler.to_string_cdr(value));
						trips.put(usr_key, split_usr_day_trips(usr_key, value));
					}
				}
			}
		});
		return trips;
	}

	public Hashtable<String, Hashtable<Integer, List<List<CDR>>>> split_trips() {

		Hashtable<String, Hashtable<Integer, List<List<CDR>>>> trips = new Hashtable<>();

		splitted_dataset.entrySet().stream().forEach((entry) -> {
			String day_key = entry.getKey();
			Hashtable<Integer, List<CDR>> day_data = entry.getValue();
			trips.put(day_key, split_day_trips(day_data));
		});
		return trips;
	}

	/**
	 * Split records of a single day into trips based on the extracted important
	 * places ..
	 *
	 * @param usr_id
	 * @param ucdr
	 * @param s_towers
	 * @param e_towers
	 * @return
	 */
	private List<List<CDR>> split_usr_day_trips(int usr_id, List<CDR> ucdr) {

		List<List<CDR>> trips = new ArrayList<>();
		List<Integer> poi = ip.get(usr_id);

		int home = usrs_home.get(usr_id);
		List<CDR> trip = new ArrayList<>();
		CDR rec = ucdr.get(0);

		// first record of the day must be from the home ..
		if (rec.twr_id == home) {
			trip.add(rec);
		} else {
			CDR t_cdr = new CDR();
			t_cdr.id = rec.id;
			t_cdr.date = rec.date;
			t_cdr.twr_id = home;

			trip.add(t_cdr);
			trip.add(rec);
		}

		for (int i = 1; i < ucdr.size(); i++) {
			rec = ucdr.get(i);

			/**
			 * if the current record from one of the important place; end the
			 * current trip and start new one from the same point ..
			 */
			if (poi.contains(rec.twr_id) /* && trip.size() > 1 */) {
				trip.add(rec);
				trips.add(trip);
				trip = new ArrayList<>();
				// if (i != ucdr.size() - 1) {
				trip.add(rec);
				// }
			} else {
				trip.add(rec);
			}
		}
		// add the last trip to the trips sequences .....
		if (trip.size() > 1) {
			trips.add(trip);
		}
		return trips;
	}

	public void write_xml_obs(List<List<Integer>> trips, List<List<Date>> trips_timestamp,
			List<List<Integer>> interpolated_trips) {
		Hashtable<String, Hashtable<Integer, Obs>> obs = new Hashtable<>();

	}

	public static void write_timestamp_csv(List<List<Date>> data, String path) {

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write("nSequences\n");
			writer.write(Integer.toString(data.size()));
			writer.newLine();

			writer.write("sequences\n");
			for (Iterator<List<Date>> iterator = data.iterator(); iterator.hasNext();) {
				String l = DataHandler.to_string_date(iterator.next());
				writer.newLine();
				writer.write(l);

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
	 * Write CSV file ...
	 *
	 * @param <T>
	 * @param <K>
	 * @param data
	 * @param header
	 * @param path
	 */
	public static void write_trips_csv(List<List<Integer>> data, String path) {

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));

			writer.write("nSequences\n");
			writer.write(Integer.toString(data.size()));
			writer.newLine();

			writer.write("sequences\n");

			for (Iterator<List<Integer>> iterator = data.iterator(); iterator.hasNext();) {
				String l = DataHandler.to_string(iterator.next());
				// System.out.println(l);
				writer.newLine();
				writer.write(l);

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

}
