/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations.samples_selection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import utils.CDR;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_sample_selection_without_handover {

	/**
	 * @param args
	 *            the command line arguments
	 */
	private static final SimpleDateFormat date_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static Map<Integer, Integer> dataset_stats(List<CDR> cdrs) {
		Map<Integer, Integer> stats = new HashMap<>();

		for (Iterator<CDR> iterator = cdrs.iterator(); iterator.hasNext();) {
			CDR cdr = iterator.next();
			int usr = cdr.getId();
			if (stats.containsKey(usr)) {
				int count = stats.get(usr);
				count++;
				stats.replace(usr, count);
			} else {
				stats.put(usr, 1);
			}

		}
		return DataHandler.sort_int_map(stats, DataHandler.DESC);
	}

	public static void main(String[] args) {
		// String data_set_path = "/home/essam/traffic/SET2/SET2_P03.CSV";
		// String towers_path = "/home/essam/traffic/SITE_ARR_LONLAT.CSV";
		// String subset_path = "/home/essam/traffic/SET2/SET2_P03_subset.CSV";

		String data_set_path = "/home/algizawy/traffic/SET2/SET2_P03.CSV";
		String towers_path = "/home/algizawy/traffic/SITE_ARR_LONLAT.CSV";
		String subset_path = "/home/algizawy/traffic/SET2/SET2_P03_subset.CSV";
		String usr_oriented_data_path = "/home/algizawy/traffic/SET2/SET2_P03_usr_oriented.CSV";

		List<CDR> cdrs = DataHandler
				.remove_repeated_obs(DataHandler.remove_handover(DataHandler.read_dataset(data_set_path)));

		int[] t_indices = { 0, 2, 3 };
		Hashtable<Integer, Vertex> towers = DataHandler
				.list_towers(DataHandler.extract_info(DataHandler.read_csv(towers_path, ","), t_indices));

		Map<Integer, Integer> stats = dataset_stats(cdrs);
		List<Integer> high_freq_usrs = new ArrayList<>();
		int i = 0;
		for (Map.Entry<Integer, Integer> entry : stats.entrySet()) {
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			System.out.println(key + "\t" + (value / 14));
			high_freq_usrs.add(key);
			i++;
			if (i == 1000) {
				break;
			}
		}
		List<String[]> obs = subset(cdrs, towers, high_freq_usrs);
		DataHandler.write_csv(obs, subset_path);

		List<String[]> usr_oriented_data_obs = usr_oriented_data(obs);
		DataHandler.write_csv(usr_oriented_data_obs, usr_oriented_data_path);
	}

	public static List<String[]> subset(List<CDR> cdrs, Hashtable<Integer, Vertex> towers, List<Integer> usrs) {

		List<String[]> subset_cdrs = new ArrayList<>();

		String[] header = { "usr_id", "date_time", "cell_id", "Lat", "Lan" };
		subset_cdrs.add(header);

		for (Iterator<CDR> iterator = cdrs.iterator(); iterator.hasNext();) {
			CDR cdr = iterator.next();
			int usr = cdr.getId();
			if (usrs.contains(usr)) {
				String[] n_cdr = new String[5];
				n_cdr[0] = Integer.toString(usr);

				n_cdr[1] = date_time.format(cdr.getDate());
				n_cdr[2] = Integer.toString(cdr.getTwr_id());

				int t = cdr.getTwr_id();
				Vertex v = towers.get(t);
				String lat = Double.toString(v.getX());
				String lan = Double.toString(v.getY());

				n_cdr[3] = lat;
				n_cdr[4] = lan;

				subset_cdrs.add(n_cdr);
			}
		}
		return subset_cdrs;
	}

	public static List<String[]> usr_oriented_data(List<String[]> data) {
		// remove header ...
		data.remove(0);

		List<String[]> usr_data = new ArrayList<>();
		List<String> l = new ArrayList<>();
		int prev_usr = -1;
		for (String[] rec : data) {
			int usr = Integer.parseInt(rec[0]);

			if (prev_usr != usr) {
				if (!l.isEmpty()) {
					String[] tmp = l.toArray(new String[l.size()]);
					usr_data.add(tmp);
				}
				l = new ArrayList<>();
				l.add("u" + rec[0]);
				l.add(rec[2]);
				prev_usr = usr;
			} else {

				l.add(rec[2]);
			}
		}
		return usr_data;
	}
}
