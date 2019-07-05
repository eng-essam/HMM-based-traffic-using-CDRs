/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;

/**
 *
 * @author essam
 */
public class Split_hourly_densities_street {

	/**
	 * @param args
	 *            the command line arguments
	 */
	final static String t1 = "00:00:00";
	final static String t2 = "24:00:00";
	final static int increment = 1;
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
	 * @param hourly_flow
	 * @return
	 */
	public static Hashtable<String, Integer> append_street_segments(Hashtable<String, Integer> hourly_flow) {
		Hashtable<String, Integer> strt_hourly_flow = new Hashtable<String, Integer>();

		int sum = 0;

		for (Map.Entry<String, Integer> hd_entry : hourly_flow.entrySet()) {
			String edge_key = hd_entry.getKey().replace("#", "").replace("-", "");
			int density_val = hd_entry.getValue();
			if (strt_hourly_flow.containsKey(edge_key)) {
				strt_hourly_flow.replace(edge_key, strt_hourly_flow.get(edge_key) + density_val);
			} else {
				strt_hourly_flow.put(edge_key, density_val);

			}
		}
		return strt_hourly_flow;
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

		// System.out.println(Arrays.toString(header));
		String[] data;
		int sum[] = new int[size];
		Arrays.fill(sum, 0);

		for (Map.Entry<String, Hashtable<String, Integer>> entry : acc_densities.entrySet()) {
			String date_str = entry.getKey();
			Hashtable<String, Integer> hourly_density = entry.getValue();
			// data = new String[size];
			// Arrays.fill(data, "0");
			// data[0] = date_str;
			for (Map.Entry<String, Integer> hd_entry : hourly_density.entrySet()) {
				String edge_key = hd_entry.getKey();
				int density_val = hd_entry.getValue();
				// data[edges.indexOf(edge_key) + 1] =
				// Integer.toString(density_val);
				sum[edges.indexOf(edge_key) + 1] += density_val;
			}
			// System.out.println(Arrays.toString(data));
			// densities.add(data);
		}

		for (Map.Entry<String, Hashtable<String, Integer>> entry : acc_densities.entrySet()) {
			String date_str = entry.getKey();
			Hashtable<String, Integer> hourly_density = entry.getValue();
			data = new String[size];
			Arrays.fill(data, "0");
			data[0] = date_str;
			for (Map.Entry<String, Integer> hd_entry : hourly_density.entrySet()) {
				// String edge_key = hd_entry.getKey();
				int index = edges.indexOf(hd_entry.getKey()) + 1;
				int density_val = hd_entry.getValue();
				if (sum[index] > 0)
					data[index] = Integer.toString(density_val);
				// sum[index] += density_val;
			}
			// System.out.println(Arrays.toString(data));
			densities.add(data);
		}

		return densities;
	}

	public static void main(String[] args) throws ParseException {

		String vit_dir = args[0];
		String hourly_flow_file_path = args[1];

		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vit_dir));
		files.sort(null);

		Hashtable<String, Hashtable<String, Integer>> total_flow = new Hashtable<String, Hashtable<String, Integer>>();

		// files.parallelStream().forEachOrdered(subsetPath -> {
		// if (subsetPath.contains(".viterbi.day")) {
		// String day =
		// subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") +
		// "viterbi.day.".length(),
		// subsetPath.lastIndexOf("_"));
		// total_flow.putAll(split_hourly_flow(subsetPath, day));
		//
		// }
		// });
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			if (!subsetPath.contains(".viterbi.day")) {
				continue;
			}
			String day = subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") + "viterbi.day.".length(),
					subsetPath.lastIndexOf("_"));
			total_flow.putAll(split_hourly_flow(subsetPath, day));

		}
		utils.DataHandler.write_csv(convert_table_list(total_flow), hourly_flow_file_path);
	}

	public static Hashtable<String, Hashtable<String, Integer>> split_hourly_flow(String vit_day, String start_date)
			throws ParseException {

		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");

		DateFormat sdformatter = new SimpleDateFormat("yyyy-mm-dd");

		Calendar scal = Calendar.getInstance();
		scal.setTime(sdformatter.parse(start_date));

		Hashtable<String, Hashtable<String, Integer>> total_flow = new Hashtable<>();

		// Calendar cal1 = Calendar.getInstance();
		// cal1.setTime(formatter.parse(t1));
		// Calendar cal2 = Calendar.getInstance();
		// cal2.setTime(formatter.parse(t2));
		Density density = new Density();

		Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);
		// System.out.println(obs.size());
		int strt_time = 0;
		int end_time = 24;
		while (strt_time < end_time) {
			String from = String.format("%2d", strt_time) + ":00:00";
			Calendar from_calendar_time = Calendar.getInstance();
			from_calendar_time.setTime(formatter.parse(from));

			Calendar to_calendar_time = Calendar.getInstance();
			strt_time++;
			String to = String.format("%2d", strt_time) + ":00:00";
			to_calendar_time.setTime(formatter.parse(to));

			String flow_time = start_date + " " + from;

			Hashtable<String, Integer> hourly_flow = density.get_Hourly_Density_street(from_calendar_time,
					to_calendar_time, obs);
			// create a list of edges
			append_edges(hourly_flow);
			// add flows
			total_flow.put(flow_time, hourly_flow);

			// cal1.add(Calendar.HOUR_OF_DAY, increment);

		}
		//
		// while (cal2.after(cal1)) {
		//// String from = cal1.get(Calendar.HOUR_OF_DAY) + ":" +
		// cal1.get(Calendar.MINUTE) + ":"
		//// + cal1.get(Calendar.SECOND);
		// Calendar tmp = Calendar.getInstance();
		// String to = (cal1.get(Calendar.HOUR_OF_DAY) + increment) + ":" +
		// cal1.get(Calendar.MINUTE) + ":"
		// + cal1.get(Calendar.SECOND);
		// tmp.setTime(formatter.parse(to));
		//
		// String flow_time = start_date + " " +
		// formatter.format(cal1.getTime());
		//
		// Hashtable<String, Hashtable<String, Double>> dtable =
		// density.getHDensity(cal1, tmp, obs);
		// Hashtable<String, Integer> hourly_flow = density.sumVehicles(dtable);
		// //create a list of edges
		// append_edges(hourly_flow);
		// // add flows
		// total_flow.put(flow_time, hourly_flow);
		//
		// cal1.add(Calendar.HOUR_OF_DAY, increment);
		//
		// }
		return total_flow;

	}

}
