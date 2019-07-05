
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import Density.main_change_hourly_density_into_csv;
import Observations.ObsTripsBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class main_rename_old_results {

	final static String dataset_regex = "SET2_P[0-9]+.CSV";

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here

		String path = "/home/essam/traffic/SET2";
		String density_dir = "/home/essam/Dropbox/viterbi/results_dec-2015/vit5_density";

		// ArrayList<String> dirs = ObsTripsBuilder.list_directories(new
		// File(density_dir));
		// for (Iterator<String> iterator = dirs.iterator();
		// iterator.hasNext();) {
		//// String next = iterator.next();
		// System.out.println(iterator.next());
		//
		// }
		// Hashtable<Integer, String> days = read_csv(path);
		Hashtable<String, Hashtable<Integer, String>> sets_days = read_multiple_datasets_files(path);
		rename_daily_densities(sets_days, density_dir);

		// rename_hourly_densities(sets_days, density_dir);

		// for (Map.Entry<String, Hashtable<Integer, String>> sets_entry :
		// sets_days.entrySet()) {
		// String key = sets_entry.getKey();
		// Hashtable<Integer, String> days = sets_entry.getValue();
		// System.out.println(key);
		// days.entrySet().stream().forEach((entry) -> {
		// System.out.println(entry.getKey() + "\t" + entry.getValue());
		// });
		//
		// }
	}

	public static Hashtable<Integer, String> read_csv(String path) {

		Hashtable<Integer, String> days = new Hashtable<>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));

			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				String day = line.trim().split(",")[1].split(" ")[0];

				int day_num = 0;
				try {
					day_num = Integer.parseInt(day.split("-")[2]);
				} catch (ArrayIndexOutOfBoundsException exec) {
					System.out.println(day);
					System.exit(0);
				}

				if (!days.containsKey(day_num)) {
					days.put(day_num, day);
				}
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(main_change_hourly_density_into_csv.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(main_change_hourly_density_into_csv.class.getName()).log(Level.SEVERE, null, ex);
		}
		return days;
	}

	public static Hashtable<String, Hashtable<Integer, String>> read_multiple_datasets_files(String path) {
		Hashtable<String, Hashtable<Integer, String>> sets_days = new Hashtable<>();

		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			String file_name = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (file_name.matches(dataset_regex)) {
				file_name = file_name.substring(0, file_name.lastIndexOf("."));
				System.out.println(file_name);
				sets_days.put(file_name, read_csv(subsetPath));
			}

		}
		return sets_days;
	}

	public static void rename_daily_densities(Hashtable<String, Hashtable<Integer, String>> sets, String path) {

		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String file = iterator.next();
			String file_name = file.substring(file.lastIndexOf("/") + 1);

			if (file_name.startsWith(".")) {
				continue;
			}
			// SET2_P01.CSV.0_500_th-dist_1000_th-time_60.density.day.7.xml
			String set_file_name = file_name.substring(0, file_name.indexOf("."));
			int day = Integer.parseInt(file_name.substring(file_name.indexOf("day.") + 4, file_name.lastIndexOf(".")));

			if (sets.containsKey(set_file_name)) {
				Hashtable<Integer, String> days = sets.get(set_file_name);
				String full_day = days.get(day);
				rename_file(file, full_day);
			}

		}
	}

	public static void rename_dir(String old_dir, String day_str) {
		String parent_dir = old_dir.substring(0, old_dir.lastIndexOf("/") + 1);
		String new_dir = parent_dir + day_str;

		System.out.println(day_str);

		File dir = new File(old_dir);
		File newName = new File(new_dir);
		if (dir.isDirectory()) {
			dir.renameTo(newName);
		} else {
			dir.mkdir();
			dir.renameTo(newName);
		}

	}

	public static void rename_file(String old_dir, String day_str) {
		String parent_dir = old_dir.substring(0, old_dir.lastIndexOf("/") + 1);
		String new_dir = parent_dir + day_str + ".xml";

		System.out.println(day_str);

		File file = new File(old_dir);
		File newName = new File(new_dir);
		if (file.isFile()) {
			file.renameTo(newName);
		}

	}

	public static void rename_hourly_densities(Hashtable<String, Hashtable<Integer, String>> sets, String path) {
		ArrayList<String> dirs = ObsTripsBuilder.list_directories(new File(path));

		for (Iterator<String> iterator = dirs.iterator(); iterator.hasNext();) {
			String old_dir = iterator.next();
			String file_name = old_dir.substring(old_dir.lastIndexOf("/") + 1);

			if (file_name.startsWith(".")) {
				continue;
			}

			String set_file_name = file_name.substring(0, file_name.indexOf("."));
			int day = Integer.parseInt(file_name.substring(file_name.lastIndexOf(".") + 1));
			if (sets.containsKey(set_file_name)) {
				Hashtable<Integer, String> days = sets.get(set_file_name);
				String full_day = days.get(day);
				rename_dir(old_dir, full_day);
			}

		}

	}
}
