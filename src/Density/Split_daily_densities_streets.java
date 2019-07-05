/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Observations.ObsTripsBuilder;

/**
 *
 * @author essam
 */
public class Split_daily_densities_streets {

	private static List<String> edges = new ArrayList<>();

	private static void append_edges(Hashtable<String, Integer> hourly_density) {

		hourly_density.entrySet().stream().map((entry) -> entry.getKey()).filter((key) -> (!edges.contains(key)))
				.forEach((key) -> {
					edges.add(key);
				});

	}

	/**
	 * Append street segments ..
	 * 
	 * @param flow
	 * @return
	 */
	public static Hashtable<String, Integer> append_street_segments(Hashtable<String, Integer> flow) {
		Hashtable<String, Integer> strt_flow = new Hashtable<String, Integer>();

		int sum = 0;

		for (Map.Entry<String, Integer> hd_entry : flow.entrySet()) {
			String edge_key = hd_entry.getKey().replace("#", "").replace("-", "");
			int density_val = hd_entry.getValue();
			if (strt_flow.containsKey(edge_key)) {
				strt_flow.replace(edge_key, strt_flow.get(edge_key) + density_val);
			} else {
				strt_flow.put(edge_key, density_val);

			}
		}
		return strt_flow;
	}

	private static List<String[]> convert_table_list(Hashtable<String, Hashtable<String, Integer>> acc_densities) {

		List<String[]> densities = new ArrayList<>();
		int size = edges.size() + 1;

		String[] header = new String[size];
		header[0] = "date";
		for (int i = 0; i < edges.size(); i++) {
			String edge = edges.get(i);
			header[i + 1] = "x" + edge.replace("#", "_").replace("-", "_");
			// header[i + 1] = "x" + edge;
		}

		densities.add(header);

		System.out.println(Arrays.toString(header));
		String[] data;
		for (Map.Entry<String, Hashtable<String, Integer>> entry : acc_densities.entrySet()) {
			String date_str = entry.getKey();
			Hashtable<String, Integer> hourly_density = entry.getValue();
			data = new String[size];
			Arrays.fill(data, "0");
			data[0] = date_str;
			for (Map.Entry<String, Integer> hd_entry : hourly_density.entrySet()) {
				String edge_key = hd_entry.getKey();
				int density_val = hd_entry.getValue();
				data[edges.indexOf(edge_key) + 1] = Integer.toString(density_val);
			}
			// System.out.println(Arrays.toString(data));
			densities.add(data);
		}
		return densities;
	}

	public static void main(String[] args) throws ParseException {

		String density_dir_path = args[0];
		String daily_flow_file_path = args[1];

		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(density_dir_path));
		files.sort(null);

		Hashtable<String, Hashtable<String, Integer>> total_flow = new Hashtable<String, Hashtable<String, Integer>>();
		Density density = new Density();

		files.parallelStream().forEachOrdered(subsetPath -> {
			if (subsetPath.contains(".density.day.")) {

				Hashtable<String, Hashtable<String, Double>> dtable = Density.readDensity(subsetPath);
				Hashtable<String, Integer> daily_flow = append_street_segments(density.sumVehicles(dtable));

				// create a list of edges
				append_edges(daily_flow);

				String day = subsetPath.substring(subsetPath.lastIndexOf("density.day.") + "density.day.".length(),
						subsetPath.lastIndexOf("_"));

				total_flow.put(day, daily_flow);
			}
		});

		utils.DataHandler.write_csv(convert_table_list(total_flow), daily_flow_file_path);
	}

}
