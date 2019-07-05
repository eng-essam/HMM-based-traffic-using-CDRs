/**
 * 
 */
package stays;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import AViterbi.Interpolate.Interpolation;
import Observations.Obs;
import utils.DataHandler;
import utils.Vertex;

/**
 * @author essam
 *
 */
public class trips_semantic_handler {

	/**
	 * Trips generated based on the way proposed by Lauren Alexander 2015 in
	 * "Origin destination trips by purpose and time of day inferred from mobile phone data"
	 * {the agglomerative clustering algorithm (Hariharan and Toyama, 2004)} is
	 * organised in the following format:
	 * 
	 * 1- the total number of users that have dailly trips between hw/ho/wo 2-
	 * every user is iidentified by user_id && day of the year followed by daily
	 * trips separated by slash, and commas with in a single trip.
	 * 
	 * such format has to be converted into - Hashtable<String,
	 * Hashtable<Integer, Obs>>
	 */

	public Hashtable<String, Hashtable<Integer, Obs>> read_trips(String file_name) {

		Hashtable<Integer, Hashtable<String, Obs>> trips = new Hashtable<>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file_name));
			String line;
			Hashtable<String, Obs> u_obs = new Hashtable<>();
			int usr = -1;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("user")) {
					if (usr == -1) {
						usr = Integer.parseInt(line.split(",")[1]);
					} else {
						trips.put(usr, u_obs);
						u_obs = new Hashtable<>();
						usr = Integer.parseInt(line.split(",")[1]);
					}

				} else if (line.startsWith("day")) {
					String date = line.split(",")[1];
					Obs o = new Obs(reader.readLine(), reader.readLine());
					u_obs.put(date, o);
				}
			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(trips_semantic_handler.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(trips_semantic_handler.class.getName()).log(Level.SEVERE, null, ex);
		}
		return transposeWUT(trips);

	}

	/**
	 * Write interpolated trips into CSV file day oriented data..
	 * 
	 * @param trips
	 * @param file_name
	 */
	public void write_interpolated_trips(Hashtable<String, Hashtable<Integer, Obs>> trips, String file_name) {

		BufferedWriter writer = null;
		try {
			File file = new File(file_name);
			writer = new BufferedWriter(new FileWriter(file));

			for (Entry<String, Hashtable<Integer, Obs>> entry : trips.entrySet()) {
				writer.write("day," + entry.getKey() + "\n");
				for (Entry<Integer, Obs> u_entry : entry.getValue().entrySet()) {
					// String log = "usr, " + u_entry.getKey().toString() + "\n"
					// + u_entry.getValue().getSeq() + "\n"
					// + u_entry.getValue().getTimeStamp() + "\n";
					writer.write("usr, " + u_entry.getKey().toString() + "\n" + u_entry.getValue().getSeq() + "\n"
							+ u_entry.getValue().getTimeStamp() + "\n");
				}
			}

			// trips.entrySet().stream().forEach((entry) -> {
			// writer.write("day," + entry.getKey() + "\n");
			// entry.getValue().entrySet().stream().forEach(u_entry -> {
			// String log = "usr, " + u_entry.getKey().toString() + "\n" +
			// u_entry.getValue().getSeq() + "\n"
			// + u_entry.getValue().getTimeStamp() + "\n";
			// // writer.write("usr," + usr_id);
			// writer.write(log);
			// });
			// });

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	public java.util.List<String[]> list_trips(Hashtable<Integer, Obs> obs) {
		java.util.List<String[]> l = new ArrayList<>();
		for (Map.Entry<Integer, Obs> entry : obs.entrySet()) {
			String trips[] = entry.getValue().getSeq().split("/");
			for (String t : trips) {
				l.add(t.split(","));
			}
		}
		return l;
	}

	public Hashtable<String, Hashtable<Integer, Obs>> transposeWUT(
			Hashtable<Integer, Hashtable<String, Obs>> obsTable) {
		Hashtable<String, Hashtable<Integer, Obs>> transposed = new Hashtable<>();

		for (Map.Entry<Integer, Hashtable<String, Obs>> entry : obsTable.entrySet()) {
			Integer userID = entry.getKey();
			Hashtable<String, Obs> dayObstable = entry.getValue();
			for (Map.Entry<String, Obs> daysEntry : dayObstable.entrySet()) {
				String weekday = daysEntry.getKey();
				Obs obsStr = daysEntry.getValue();

				if (transposed.containsKey(weekday)) {
					Hashtable<Integer, Obs> usrObstable = transposed.get(weekday);
					usrObstable.put(userID, obsStr);
					transposed.replace(weekday, usrObstable);
				} else {
					Hashtable<Integer, Obs> usrObstable = new Hashtable<>();
					usrObstable.put(userID, obsStr);
					transposed.put(weekday, usrObstable);
				}

			}

		}
		return transposed;
	}

	public Hashtable<Integer, Obs> interpolate_stays_trips(Hashtable<Integer, Obs> day_trips,
			Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors, Hashtable<Integer, Vertex> towers,
			Hashtable<Integer, Double> rg) {

		Interpolation interpolate = new Interpolation(voronoi_neighbors, towers);
		Hashtable<Integer, Obs> day_obs = new Hashtable<>();

		day_trips.entrySet().stream().forEach((entry) -> {
			int usr_key = entry.getKey();
			double u_rg = rg.get(usr_key) / 1000;

			String[] trips = entry.getValue().getSeq().split(DataHandler.SLASH_SEP);
			String[] trips_ts = entry.getValue().getTimeStamp().split(DataHandler.SLASH_SEP);

			String str_trips = "";
			String str_ts = "";

			for (int i = 0; i < trips.length; i++) {
				List<Integer> t = DataHandler.to_integer_list(trips[i].split(DataHandler.COMMA_SEP));

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
				str_ts += trips_ts[i];
			}
			if (!str_trips.isEmpty()) {
				day_obs.put(usr_key, new Obs(str_trips, str_ts));
			}
		});
		return day_obs;
	}

	public boolean is_week_end(String day) {
		SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(parserSDF.parse(day));
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			System.out.println("Error parsing date");
			return false;
			// e.printStackTrace();
		}
		return false;
		// return !workday(day);
	}

	// public boolean workday(String day) {
	// SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd");
	//
	// try {
	// Date nextDate = parserSDF.parse(day);
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(nextDate);
	// int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
	// if (!(dayOfWeek == Calendar.FRIDAY || dayOfWeek == Calendar.SATURDAY ||
	// dayOfWeek == Calendar.SUNDAY)) {
	// return true;
	// }
	// } catch (ParseException ex) {
	// return false;
	// }
	// return false;
	// }

}
