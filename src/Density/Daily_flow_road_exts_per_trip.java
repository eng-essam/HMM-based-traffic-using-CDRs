package Density;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import Observations.Obs;
import Observations.ObsTripsBuilder;

public class Daily_flow_road_exts_per_trip {

	private static List<String> edges = new ArrayList<>();

	private static void append_edges(Hashtable<String, Integer> hourly_density) {

		hourly_density.entrySet().stream().map((entry) -> entry.getKey()).filter((key) -> (!edges.contains(key)))
				.forEach((key) -> {
					edges.add(key);
				});

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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String vit_data = args[0];
		String daily_flow_file_path = args[1];

		List<String> trips = new ArrayList<>();

		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vit_data));
		Hashtable<String, Hashtable<String, Integer>> total_flow = new Hashtable<String, Hashtable<String, Integer>>();

		files.parallelStream().forEachOrdered(vit_day -> {
			// String vit_day = (String) iterator.next();

			if (vit_day.contains(".viterbi.day")) {
				Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);
				Hashtable<String, Integer> day_flow = new Hashtable<String, Integer>();
				String day = "";
				for (Map.Entry<String, Hashtable<Integer, Obs>> obs_entry : obs.entrySet()) {
//					day = obs_entry.getKey();
					day = vit_day.substring(vit_day.lastIndexOf("viterbi.day.") + "viterbi.day.".length(),
							vit_day.lastIndexOf("_"));
					Hashtable<Integer, Obs> day_obs = obs_entry.getValue();
					for (Map.Entry<Integer, Obs> u_obs : day_obs.entrySet()) {
						Obs O = u_obs.getValue();
						String seq = O.getSeq();
						String vit = O.getVitPath();
						// skip undecoded routes
						if (vit.equals("-"))
							continue;

						// remove interpolation
						if (vit.contains("_interpolated_")) {
							vit = vit.replaceAll("_interpolated_\\[[0-9]+\\]", "");
						}
						// remove edges on the same road
						if (vit.contains("#")) {
							vit = vit.replaceAll("#[0-9]+", "");
						}

						// String[] s_arr = seq.split("/");
						String[] v_arr = vit.split("/");
						for (int i = 0; i < v_arr.length; i++) {
							String t = v_arr[i];

							// String t_obs[] = s_arr[i].split(",");

							// if (Arrays.stream(t_obs).distinct().count() != 1)
							// {
							trips.add(t);
							List<String> segs = Arrays.asList(t.split(","));
							Set<String> uniqueSet = new HashSet<String>(segs);
							for (String temp : uniqueSet) {
								///////
								if (day_flow.containsKey(temp)) {
									int freq = day_flow.get(temp);
									day_flow.replace(temp, freq + 1);
								} else {
									int freq = 1;
									day_flow.put(temp, freq);
								}
							}
							// }
						}

					}
				}
				append_edges(day_flow);
				total_flow.put(day, day_flow);
			}
		});
		utils.DataHandler.write_csv(convert_table_list(total_flow), daily_flow_file_path);

	}

}
