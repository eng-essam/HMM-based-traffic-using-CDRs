/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations.samples_selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_sample_selection {

	public static Map<Integer, Integer> dataset_stats(List<String[]> cdrs) {
		Map<Integer, Integer> stats = new HashMap<>();

		for (Iterator<String[]> iterator = cdrs.iterator(); iterator.hasNext();) {
			String[] cdr = iterator.next();
			int usr = Integer.parseInt(cdr[0]);
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

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String data_set_path = "/media/essam/Dell1/traffic/SET2/SET2_P03.CSV";
		String towers_path = "/media/essam/Dell1/traffic/SITE_ARR_LONLAT.CSV";
		String subset_path = "/media/essam/Dell1/traffic/SET2/SET2_P03_subset.CSV";
		List<String[]> cdrs = DataHandler.read_csv(data_set_path, ",");

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
		DataHandler.write_csv(subset(cdrs, towers, high_freq_usrs), subset_path);
	}

	public static List<String[]> subset(List<String[]> cdrs, Hashtable<Integer, Vertex> towers, List<Integer> usrs) {

		List<String[]> subset_cdrs = new ArrayList<>();

		String[] header = { "usr_id", "date_time", "cell_id", "Lat", "Lan" };
		subset_cdrs.add(header);

		for (Iterator<String[]> iterator = cdrs.iterator(); iterator.hasNext();) {
			String[] cdr = iterator.next();
			int usr = Integer.parseInt(cdr[0]);
			if (usrs.contains(usr)) {
				String[] n_cdr = new String[5];
				n_cdr[0] = cdr[0];
				n_cdr[1] = cdr[1];
				n_cdr[2] = cdr[2];

				int t = Integer.parseInt(cdr[2]);
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
}
