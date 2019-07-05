package Density;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;

public class Main_day_cummulative_calls {

	public static void main(String[] args) throws ParseException {
		String path = args[0];
		ObsTripsBuilder builder = new ObsTripsBuilder();
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
		Hashtable<String, Integer> freq = new Hashtable<String, Integer>();

		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String subsetPath = iterator.next();
			String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
			if (fileName.startsWith(".") || subsetPath.contains("WEEK") || subsetPath.contains("_Day_")) {
				continue;
			}
			if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
				// builder.split_weeky_data(subsetPath);
				List<String[]> data = DataHandler.read_csv(subsetPath, ",");

				data.stream().forEach(rec -> {
					if (Integer.parseInt(rec[2]) < 500) {
						String day = rec[1].split(" ")[0];
						if (freq.containsKey(day)) {
							freq.replace(day, freq.get(day) + 1);
						} else {
							freq.put(day, 1);
						}
					}
				});

			}

		}
		for (Map.Entry<String, Integer> entry : freq.entrySet()) {
			String day = entry.getKey();
			int calls = entry.getValue();

			Date date = new SimpleDateFormat("yyyy-MM-dd").parse(day);
			String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date);

			System.out.println(dayOfWeek + "," + calls);
		}

	}

}
