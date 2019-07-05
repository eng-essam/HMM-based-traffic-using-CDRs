package social_pulse;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Density.Plot;
import diewald_shapeFile.shapeFile.ShapeFile;
import geoshape.shapefile_utils;
import utils.DataHandler;
import utils.Edge;
import utils.StdDraw;

class GeoPoint {
	public double lon;
	public double lat;

	public double x;
	public double y;
}

class GPSRec {
	public long timestamp;
	public GeoPoint p = new GeoPoint();
}

class FCDTrip {
	public GPSRec origin;
	public GPSRec destination;
	public int vehicle_type;
	public List<GPSRec> trace = new ArrayList<GPSRec>();

	/**
	 * set the first and last points as the OD of the current trip.
	 */
	public void set_OD() {
		origin = trace.get(0);
		destination = trace.get(trace.size() - 1);
	}
}

class Cluster {
	public GeoPoint centroid = new GeoPoint();
	private List<GeoPoint> elements = new ArrayList<GeoPoint>();

	public void add(GeoPoint p) {
		elements.add(p);
		if (elements.isEmpty()) {
			centroid.x = p.x;
			centroid.y = p.y;
		} else {
			centroid.x = 0;
			centroid.y = 0;
			elements.parallelStream().forEach(op -> {
				centroid.x += op.x;
				centroid.y += op.y;
			});
			centroid.x = centroid.x / elements.size();
			centroid.y = centroid.y / elements.size();
		}
	}

	public int size() {
		return elements.size();
	}

	public List<GeoPoint> get_elements() {
		return elements;
	}

}

class DBScan {
	Hashtable<String, List<Cluster>> pois;
	final double SPATIAL_GPS_THRESHOLD;
	// final double TEMPORAL_GPS_THRESHOLD;
	final double DBSCAN_THRESHOLD;

	public DBScan(double spatial_gps_thr, double dbscan_thr) {
		this.SPATIAL_GPS_THRESHOLD = spatial_gps_thr;
		// this.TEMPORAL_GPS_THRESHOLD = temporal_gps_thr;
		this.DBSCAN_THRESHOLD = dbscan_thr;
		pois = new Hashtable<>();
	}

	public void scan(Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips) {

		matched_chkins_trips.entrySet().stream().forEach(u_entry -> {
			String usr = u_entry.getKey();
			List<Cluster> cs = new ArrayList<>();
			u_entry.getValue().entrySet().stream().forEach(p_entry -> {
				GPSRec rec = p_entry.getKey();
				p_entry.getValue().entrySet().stream().forEach(t_entry -> {
					GPSRec orgn = t_entry.getValue().origin;
					GPSRec dest = t_entry.getValue().destination;

					if (DataHandler.euclidean(rec.p.x, rec.p.y, orgn.p.x, orgn.p.y) < SPATIAL_GPS_THRESHOLD) {
						add_element(cs, dest.p);
					} else {
						add_element(cs, orgn.p);
					}
				});
			});
			pois.put(usr, cs);
		});
	}

	public void add_element(List<Cluster> cs, GeoPoint p) {
		boolean flag = true;
		for (Cluster c : cs) {
			double dist = DataHandler.euclidean(p.x, p.y, c.centroid.x, c.centroid.y);
			if (dist < DBSCAN_THRESHOLD) {
				c.add(p);
				flag = false;
			}
		}
		if (flag) {
			Cluster c = new Cluster();
			c.add(p);
			cs.add(c);
		}

	}
}

class GeoRelations {

	DBScan scan;
	final double SPATIAL_GPS_THRESHOLD;
	final double TEMPORAL_GPS_THRESHOLD;
	final double DBSCAN_THRESHOLD;
	Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips;

	public GeoRelations(double spatial_gps_thr, double temporal_gps_thr, double dbscan_thr) {
		this.SPATIAL_GPS_THRESHOLD = spatial_gps_thr;
		this.TEMPORAL_GPS_THRESHOLD = temporal_gps_thr;
		this.DBSCAN_THRESHOLD = dbscan_thr;

	}

	public Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> get_chk_matchs(
			String social_pulse_file_path, String fcd_files_dir) {
		boolean sample_flag = true;
		Hashtable<String, List<GPSRec>> chkins = Utils.read_checkins(social_pulse_file_path);
		Hashtable<String, List<GPSRec>> chkins_sample = new Hashtable<String, List<GPSRec>>();
		if (sample_flag) {
			for (Entry<String, List<GPSRec>> entry : chkins.entrySet()) {
				if (entry.getValue().size() > 10 && chkins_sample.size() < 300) {
					chkins_sample.put(entry.getKey(), entry.getValue());
				}
			}

		}
		System.out.println("sample size: " + chkins_sample.size());

		matched_chkins_trips = new Hashtable<>();
		List<String> files = DataHandler.listFilesForFolder(new File(fcd_files_dir));
		files.stream().forEach(file -> {
			String file_name = file.substring(file.lastIndexOf("/") + 1);
			if (file.endsWith("txt")) {
				System.out.println(file_name);
				Hashtable<String, FCDTrip> fcds = Utils.read_fcd(file);

				Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> day;
				if (sample_flag) {
					day = get_day_chk_matchs(fcds, chkins_sample);
				} else {
					day = get_day_chk_matchs(fcds, chkins);
				}

				// append tables ..

				day.entrySet().stream().forEach(entry -> {
					String usr = entry.getKey();

					if (matched_chkins_trips.containsKey(usr)) {

						Hashtable<GPSRec, Hashtable<String, FCDTrip>> stored_gps_trips = matched_chkins_trips.get(usr);
						Hashtable<GPSRec, Hashtable<String, FCDTrip>> gps_trips = entry.getValue();

						gps_trips.entrySet().stream().forEach(gps_entry -> {
							GPSRec rec = gps_entry.getKey();
							if (stored_gps_trips.containsKey(rec)) {
								// System.out.println("Error: GPS record
								// previously matched by another day");
								Hashtable<String, FCDTrip> stored_trips = stored_gps_trips.get(rec);
								Hashtable<String, FCDTrip> trips = gps_entry.getValue();
								stored_trips.putAll(trips);
								stored_gps_trips.replace(rec, stored_trips);
							} else {
								stored_gps_trips.put(rec, gps_entry.getValue());
							}

						});
						matched_chkins_trips.replace(usr, stored_gps_trips);
					} else {
						matched_chkins_trips.put(usr, entry.getValue());
					}

				});
			}

		});

		return matched_chkins_trips;
	}

	public Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> get_day_chk_matchs(
			Hashtable<String, FCDTrip> fcds, Hashtable<String, List<GPSRec>> chkins) {
		// Hashtable<String, FCDTrip> fcds = Utils.read_fcd(fcd_file_path);
		// Hashtable<String, List<GPSRec>> chkins =
		// Utils.read_checkins(social_pulse_file_path);
		Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips = new Hashtable<>();
		chkins.entrySet().parallelStream().forEach(entry -> {
			String usr = entry.getKey();
			List<GPSRec> rec = entry.getValue();
			Hashtable<GPSRec, Hashtable<String, FCDTrip>> mt_list = new Hashtable<>();
			rec.parallelStream().forEach(chkin -> {
				Hashtable<String, FCDTrip> mt = find_trips(fcds, chkin);
				// add even empty trips
				if (!mt.isEmpty())
					mt_list.put(chkin, mt);
			});
			// System.out.println(usr);
			if (!mt_list.isEmpty())
				matched_chkins_trips.put(usr, mt_list);
		});
		return matched_chkins_trips;
	}

	public Hashtable<String, FCDTrip> find_trips(Hashtable<String, FCDTrip> fcds, GPSRec rec) {

		Hashtable<String, FCDTrip> matched_trips = new Hashtable<>();

		// double[] xy = DataHandler.proj_coordinates(rec.lat, rec.lon);
		fcds.entrySet().parallelStream().forEach(entry -> {
			String usr = entry.getKey();
			FCDTrip t = entry.getValue();
			if ((Math.abs(t.origin.timestamp - rec.timestamp) / 60) <= this.TEMPORAL_GPS_THRESHOLD) {
				// double[] xy1 = DataHandler.proj_coordinates(t.origin.lat,
				// t.origin.lon);
				if (DataHandler.euclidean(rec.p.x, rec.p.y, t.origin.p.x, t.origin.p.y) <= this.SPATIAL_GPS_THRESHOLD) {
					// System.out.println("match");
					matched_trips.put(usr, t);
				}
			} else if ((Math.abs(t.destination.timestamp - rec.timestamp) / 60) <= this.TEMPORAL_GPS_THRESHOLD) {
				// double[] xy1 =
				// DataHandler.proj_coordinates(t.destination.lat,
				// t.destination.lon);
				if (DataHandler.euclidean(rec.p.x, rec.p.y, t.destination.p.x,
						t.destination.p.y) <= this.SPATIAL_GPS_THRESHOLD) {
					// System.out.println("match");
					matched_trips.put(usr, t);
				}
			}
		});
		return matched_trips;
	}

	public Hashtable<String, List<Cluster>> get_Pois() {
		scan = new DBScan(SPATIAL_GPS_THRESHOLD, DBSCAN_THRESHOLD);
		scan.scan(matched_chkins_trips);
		return scan.pois;
	}

}

public class Utils {
	private static final Pattern TAG_REGEX = Pattern.compile(
			"\\{geometry:\\{type:Point,coordinates:\\[(\\d*\\.?\\d*),(\\d*\\.?\\d*)\\]\\},timestamp:(\\d+),user:[0-9a-zA-Z]+\\}");

	private static final Pattern TAG_REGEX_1 = Pattern
			.compile("\\[(\\d*\\.?\\d*),(\\d*\\.?\\d*)\\]\\},timestamp:(\\d+),user:[0-9a-zA-Z]+");

	public static enum TimeField {
		DAY, HOUR, MINUTE, SECOND, MILLISECOND;
	}

	public static void connvert_social_pulse(String in_path, String out_path) {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));

		List<String[]> segments = new ArrayList<>();
		Hashtable<String, Hashtable<Long, String[]>> usrs = new Hashtable<>();
		BufferedReader reader;
		try {
			Reader isreader = new InputStreamReader(new FileInputStream(in_path), "utf-8");
			reader = new BufferedReader(isreader);

			String line;
			while ((line = reader.readLine()) != null) {
				// line.replaceAll("[", "\"").replaceAll("]", "\"");

				// if (line.isEmpty()) {
				// continue;
				// }
				// segments.add(line.trim().split(seperator));
				final Matcher matcher = TAG_REGEX_1.matcher(line);
				while (matcher.find()) {

					String[] s = matcher.group(0).replaceAll("\\[", "").replaceAll("\\]", "")
							.replaceAll("timestamp:", "").replaceAll("user:", "").replaceAll("\\}", "")
							.replaceAll("\\}", "").split(",");

					String rec = s[3] + s[2] + s[0] + s[1];
					long unix_epoch_date = Long.parseLong(s[2]);
					Date ts = new Date(unix_epoch_date * 1000);
					String d = df.format(ts).toString();
					if (usrs.containsKey(s[3])) {
						Hashtable<Long, String[]> t = usrs.get(s[3]);
						t.put(unix_epoch_date, new String[] { s[3], d, s[1], s[0] });
						usrs.replace(s[3], t);
					} else {
						Hashtable<Long, String[]> t = new Hashtable<>();

						t.put(unix_epoch_date, new String[] { s[3], d, s[1], s[0] });
						usrs.put(s[3], t);
					}
					// segments.add(new String[] { s[3], s[2], s[0], s[1] });
					// System.out.println(rec);
				}

			}

		} catch (FileNotFoundException ex) {
			Logger.getLogger(main_split_social_data.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(main_split_social_data.class.getName()).log(Level.SEVERE, null, ex);
		}

		System.out.println("number of users in study:\t" + usrs.size());
		usrs.entrySet().stream().forEach((entry) -> {
			String usr = entry.getKey();
			Hashtable<Long, String[]> t = entry.getValue();
			// average one check-in per week = 8 per 2 months
			List<Long> v = new ArrayList<Long>(t.keySet());
			Collections.sort(v);

			for (Long d : v) {
				String[] chckin = t.get(d);
				segments.add(chckin);
			}
			// if (t.size() > 8) {
			// System.out.println(usr + "\t" + t.size());
			// segments.addAll(t);
			// }
		});
		DataHandler.write_csv(segments, out_path);
	}

	public static Hashtable<String, FCDTrip> read_fcd(String path) {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		// Infoblu provides traffic information on motorways, ring roads and all
		// the major roads of Italy, namely the whole TMC location DB network

		// Each record has the following structure:
		// TravelID: A unique string identifying a trip. A trip starts when the
		// engine is switched on and lasts until the engine is switched of for
		// at least 30 minutes. The length of the field is 32 char.
		// Timestamp: Timestamp indicating the exact time in which GPS was
		// acquired. The time is in UTC time and its format is "yyyy-mm-dd
		// hh:mm:ss". No time zone specification is used.
		// Latitude: Latitude in WGS84 coordinates of the acquired GPS position.
		// Longitude: Latitude in WGS84 coordinates of the acquired GPS
		// position.
		// Vehicle category: Kind of vehicle generating the GPS position. The
		// value can be empty because for some vehicle the category is not
		// available.
		// Speed: Instant speed while the GPS position has been acquired. The
		// speed is expressed in km/h.
		Hashtable<String, FCDTrip> trips = new Hashtable<>();
		List<String[]> data = DataHandler.read_csv(path, ";");

		String prev_usr = "";
		FCDTrip t = new FCDTrip();
		// 4d7cc82e33d042fdf13b9149bcdacee1;2015-03-01
		// 05:21:52;45.631616;9.2073;20;0
		for (String[] rec : data) {
			if (prev_usr.compareTo(rec[0]) != 0) {

				if (t.trace.isEmpty()) {
					prev_usr = rec[0];
				} else {

					t.set_OD();
					// System.out.println(prev_usr + "\t"
					// + Utils.getTimeDifferenceMinutes(t.origin.timestamp,
					// t.destination.timestamp));
					if (trips.containsKey(prev_usr)) {
						trips.replace(prev_usr, t);
					} else {
						trips.put(prev_usr, t);
					}
					prev_usr = rec[0];
					if (trips.containsKey(prev_usr)) {
						t = trips.get(rec[0]);
					} else {
						t = new FCDTrip();
					}
					t.vehicle_type = Integer.parseInt(rec[4]);
				}

				GPSRec r = new GPSRec();
				try {
					r.timestamp = df.parse(rec[1]).getTime() / 1000;
					r.p.lat = Double.parseDouble(rec[2]);
					r.p.lon = Double.parseDouble(rec[3]);
					double xy[] = DataHandler.proj_coordinates(r.p.lat, r.p.lon);
					r.p.x = xy[0];
					r.p.y = xy[1];
					t.trace.add(r);
				} catch (Exception e) {
					System.out.println("Error parsing GPS record");
				}

			} else {
				GPSRec r = new GPSRec();
				try {
					r.timestamp = df.parse(rec[1]).getTime() / 1000;
					r.p.lat = Double.parseDouble(rec[2]);
					r.p.lon = Double.parseDouble(rec[3]);
					double xy[] = DataHandler.proj_coordinates(r.p.lat, r.p.lon);
					r.p.x = xy[0];
					r.p.y = xy[1];
					t.trace.add(r);
				} catch (Exception e) {
					System.out.println("Error parsing GPS record");
				}
			}
		}
		return trips;
	}

	public static Hashtable<String, FCDTrip> read_fcd(String path, String proj_wkt_file_path) {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		// Infoblu provides traffic information on motorways, ring roads and all
		// the major roads of Italy, namely the whole TMC location DB network

		// Each record has the following structure:
		// TravelID: A unique string identifying a trip. A trip starts when the
		// engine is switched on and lasts until the engine is switched of for
		// at least 30 minutes. The length of the field is 32 char.
		// Timestamp: Timestamp indicating the exact time in which GPS was
		// acquired. The time is in UTC time and its format is "yyyy-mm-dd
		// hh:mm:ss". No time zone specification is used.
		// Latitude: Latitude in WGS84 coordinates of the acquired GPS position.
		// Longitude: Latitude in WGS84 coordinates of the acquired GPS
		// position.
		// Vehicle category: Kind of vehicle generating the GPS position. The
		// value can be empty because for some vehicle the category is not
		// available.
		// Speed: Instant speed while the GPS position has been acquired. The
		// speed is expressed in km/h.
		Hashtable<String, FCDTrip> trips = new Hashtable<>();
		List<String[]> data = DataHandler.read_csv(path, ";");

		String wkt = "";
		try {
			wkt = new String(Files.readAllBytes(Paths.get(proj_wkt_file_path)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}

		String prev_usr = "";
		FCDTrip t = new FCDTrip();
		// 4d7cc82e33d042fdf13b9149bcdacee1;2015-03-01
		// 05:21:52;45.631616;9.2073;20;0
		for (String[] rec : data) {
			if (prev_usr.compareTo(rec[0]) != 0) {

				if (t.trace.isEmpty()) {
					prev_usr = rec[0];
				} else {

					t.set_OD();
					// System.out.println(prev_usr + "\t"
					// + Utils.getTimeDifferenceMinutes(t.origin.timestamp,
					// t.destination.timestamp));
					if (trips.containsKey(prev_usr)) {
						trips.replace(prev_usr, t);
					} else {
						trips.put(prev_usr, t);
					}
					prev_usr = rec[0];
					if (trips.containsKey(prev_usr)) {
						t = trips.get(rec[0]);
					} else {
						t = new FCDTrip();
					}
					t.vehicle_type = Integer.parseInt(rec[4]);
				}

				GPSRec r = new GPSRec();
				try {
					r.timestamp = df.parse(rec[1]).getTime() / 1000;
					r.p.lat = Double.parseDouble(rec[2]);
					r.p.lon = Double.parseDouble(rec[3]);
					// double xy[] = DataHandler.proj_coordinates(r.p.lat,
					// r.p.lon);
					double xy[] = DataHandler.proj_lat_long_from_wkt(wkt, r.p.lat, r.p.lon);
					r.p.x = xy[0];
					r.p.y = xy[1];
					t.trace.add(r);
				} catch (Exception e) {
					System.out.println("Error parsing GPS record");
				}

			} else {
				GPSRec r = new GPSRec();
				try {
					r.timestamp = df.parse(rec[1]).getTime() / 1000;
					r.p.lat = Double.parseDouble(rec[2]);
					r.p.lon = Double.parseDouble(rec[3]);
					// double xy[] = DataHandler.proj_coordinates(r.p.lat,
					// r.p.lon);
					double xy[] = DataHandler.proj_lat_long_from_wkt(wkt, r.p.lat, r.p.lon);
					r.p.x = xy[0];
					r.p.y = xy[1];
					t.trace.add(r);
				} catch (Exception e) {
					System.out.println("Error parsing GPS record");
				}
			}
		}
		return trips;
	}

	public static Hashtable<String, List<GPSRec>> read_checkins(String path) {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));

		Hashtable<String, List<GPSRec>> chkins = new Hashtable<>();
		List<String[]> data = DataHandler.read_csv(path, DataHandler.COMMA_SEP);
		String prev_usr = data.get(0)[0];
		List<GPSRec> gps_recs = new ArrayList<>();
		for (String[] rec : data) {
			if (prev_usr.compareTo(rec[0]) != 0) {

				if (!gps_recs.isEmpty()) {
					chkins.put(prev_usr, gps_recs);
					gps_recs = new ArrayList<>();
					prev_usr = rec[0];
				} else {
					System.out.println("Error: empty list");
				}
			}

			GPSRec r = new GPSRec();
			try {
				r.timestamp = df.parse(rec[1]).getTime() / 1000;
				r.p.lat = Double.parseDouble(rec[2]);
				r.p.lon = Double.parseDouble(rec[3]);
				double xy[] = DataHandler.proj_coordinates(r.p.lat, r.p.lon);
				r.p.x = xy[0];
				r.p.y = xy[1];
				gps_recs.add(r);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return chkins;
	}

	public static Hashtable<String, List<GPSRec>> read_checkins(String path, String proj_wkt_file_path) {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String wkt = "";
		try {
			wkt = new String(Files.readAllBytes(Paths.get(proj_wkt_file_path)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}

		Hashtable<String, List<GPSRec>> chkins = new Hashtable<>();
		List<String[]> data = DataHandler.read_csv(path, DataHandler.COMMA_SEP);
		String prev_usr = data.get(0)[0];
		List<GPSRec> gps_recs = new ArrayList<>();
		for (String[] rec : data) {
			if (prev_usr.compareTo(rec[0]) != 0) {

				if (!gps_recs.isEmpty()) {
					chkins.put(prev_usr, gps_recs);
					gps_recs = new ArrayList<>();
					prev_usr = rec[0];
				} else {
					System.out.println("Error: empty list");
				}
			}

			GPSRec r = new GPSRec();
			try {
				r.timestamp = df.parse(rec[1]).getTime() / 1000;
				r.p.lat = Double.parseDouble(rec[2]);
				r.p.lon = Double.parseDouble(rec[3]);
				// double xy[] = DataHandler.proj_coordinates(r.p.lat, r.p.lon);
				double xy[] = DataHandler.proj_lat_long_from_wkt_str(wkt, r.p.lat, r.p.lon);
				r.p.x = xy[0];
				r.p.y = xy[1];
				gps_recs.add(r);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return chkins;
	}

	public static void write_matched_chkins_trips(
			Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips, String path) {

		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));

		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));
			for (Entry<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> mt_entry : matched_chkins_trips
					.entrySet()) {
				writer.write("usr: " + mt_entry.getKey() + "\n");
				for (Entry<GPSRec, Hashtable<String, FCDTrip>> chk_entry : mt_entry.getValue().entrySet()) {
					GPSRec rec = chk_entry.getKey();
					writer.write("\tchkin: " + rec.p.lat + "," + rec.p.lon + ","
							+ df.format(new Date(rec.timestamp * 1000)) + "\n");
					for (Entry<String, FCDTrip> t_entry : chk_entry.getValue().entrySet()) {
						String trip_id = t_entry.getKey();
						writer.write("\t\t" + t_entry.getKey() + "\n");
						FCDTrip t = t_entry.getValue();
						writer.write("\t\t\t" + t.origin.p.lat + "," + t.origin.p.lon + "," + t.origin.p.lat + ","
								+ df.format(new Date(t.origin.timestamp * 1000)) + "\n");
						writer.write(
								"\t\t\t" + t.destination.p.lat + "," + t.destination.p.lon + "," + t.destination.p.lat
										+ "," + df.format(new Date(t.destination.timestamp * 1000)) + "\n");

					}
				}

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

	public static void write_POIs(Hashtable<String, List<Cluster>> pois, String path) {
		BufferedWriter writer = null;
		try {
			File logFile = new File(path);
			writer = new BufferedWriter(new FileWriter(logFile));

			for (Entry<String, List<Cluster>> entry : pois.entrySet()) {
				writer.write(entry.getKey() + "\n");
				for (Cluster c : entry.getValue()) {
					if (c.size() > 1)
						writer.write("\t" + c.centroid.x + "," + c.centroid.x + "\t" + c.size() + "\n");
				}
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

	public static Hashtable<String, FCDTrip> find_trips(Hashtable<String, FCDTrip> fcds, GPSRec rec) {

		Hashtable<String, FCDTrip> matched_trips = new Hashtable<>();

		// double[] xy = DataHandler.proj_coordinates(rec.lat, rec.lon);
		fcds.entrySet().parallelStream().forEach(entry -> {
			String usr = entry.getKey();
			FCDTrip t = entry.getValue();
			if ((Math.abs(t.origin.timestamp - rec.timestamp) / 60) <= 60) {
				// double[] xy1 = DataHandler.proj_coordinates(t.origin.lat,
				// t.origin.lon);
				if (DataHandler.euclidean(rec.p.x, rec.p.y, t.origin.p.x, t.origin.p.y) <= 1500) {
					// System.out.println("match");
					matched_trips.put(usr, t);
				}
			} else if ((Math.abs(t.destination.timestamp - rec.timestamp) / 60) <= 60) {
				// double[] xy1 =
				// DataHandler.proj_coordinates(t.destination.lat,
				// t.destination.lon);
				if (DataHandler.euclidean(rec.p.x, rec.p.y, t.destination.p.x, t.destination.p.y) <= 1000) {
					// System.out.println("match");
					matched_trips.put(usr, t);
				}
			}
		});
		return matched_trips;
	}

	public static Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> get_chk_matchs(
			String social_pulse_file_path, String fcd_files_dir) {
		boolean sample_flag = true;

		Hashtable<String, List<GPSRec>> chkins = Utils.read_checkins(social_pulse_file_path);

		Hashtable<String, List<GPSRec>> chkins_sample = new Hashtable<String, List<GPSRec>>();
		if (sample_flag) {
			for (Entry<String, List<GPSRec>> entry : chkins.entrySet()) {
				if (entry.getValue().size() > 100 && chkins_sample.size() < 4) {
					chkins_sample.put(entry.getKey(), entry.getValue());
				}
			}

		}
		System.out.println("sample size: " + chkins_sample.size());

		Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips = new Hashtable<>();
		List<String> files = DataHandler.listFilesForFolder(new File(fcd_files_dir));
		files.stream().forEach(file -> {
			String file_name = file.substring(file.lastIndexOf("/") + 1);
			if (file.endsWith("txt")) {
				System.out.println(file_name);
				Hashtable<String, FCDTrip> fcds = Utils.read_fcd(file);

				Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> day;
				if (sample_flag) {
					day = get_day_chk_matchs(fcds, chkins_sample);
				} else {
					day = get_day_chk_matchs(fcds, chkins);
				}

				// append tables ..

				day.entrySet().stream().forEach(entry -> {
					String usr = entry.getKey();
					if (matched_chkins_trips.containsKey(usr)) {

						Hashtable<GPSRec, Hashtable<String, FCDTrip>> stored_gps_trips = matched_chkins_trips.get(usr);
						Hashtable<GPSRec, Hashtable<String, FCDTrip>> gps_trips = entry.getValue();

						gps_trips.entrySet().stream().forEach(gps_entry -> {
							GPSRec rec = gps_entry.getKey();
							if (stored_gps_trips.containsKey(rec)) {
								// System.out.println("Error: GPS record
								// previously matched by another day");
								Hashtable<String, FCDTrip> stored_trips = stored_gps_trips.get(rec);
								Hashtable<String, FCDTrip> trips = gps_entry.getValue();
								stored_trips.putAll(trips);
								stored_gps_trips.replace(rec, stored_trips);
							} else {
								stored_gps_trips.put(rec, gps_entry.getValue());
							}

						});
						matched_chkins_trips.replace(usr, stored_gps_trips);
					} else {
						matched_chkins_trips.put(usr, entry.getValue());
					}
				});
				/**
				 * not completed
				 */
			}

		});
		return matched_chkins_trips;
	}

	public static Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> get_chk_matchs(
			String social_pulse_file_path, String fcd_files_dir, String proj_wkt_file_apth) {
		boolean sample_flag = true;

		Hashtable<String, List<GPSRec>> chkins = Utils.read_checkins(social_pulse_file_path, proj_wkt_file_apth);

		Hashtable<String, List<GPSRec>> chkins_sample = new Hashtable<String, List<GPSRec>>();
		if (sample_flag) {
			for (Entry<String, List<GPSRec>> entry : chkins.entrySet()) {
				if (entry.getValue().size() > 100 && chkins_sample.size() < 4) {
					chkins_sample.put(entry.getKey(), entry.getValue());
				}
			}

		}
		System.out.println("sample size: " + chkins_sample.size());

		Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips = new Hashtable<>();
		List<String> files = DataHandler.listFilesForFolder(new File(fcd_files_dir));
		files.stream().forEach(file -> {
			String file_name = file.substring(file.lastIndexOf("/") + 1);
			if (file.endsWith("txt")) {
				System.out.println(file_name);
				Hashtable<String, FCDTrip> fcds = Utils.read_fcd(file, proj_wkt_file_apth);

				Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> day;
				if (sample_flag) {
					day = get_day_chk_matchs(fcds, chkins_sample);
				} else {
					day = get_day_chk_matchs(fcds, chkins);
				}

				// append tables ..

				day.entrySet().stream().forEach(entry -> {
					String usr = entry.getKey();
					if (matched_chkins_trips.containsKey(usr)) {

						Hashtable<GPSRec, Hashtable<String, FCDTrip>> stored_gps_trips = matched_chkins_trips.get(usr);
						Hashtable<GPSRec, Hashtable<String, FCDTrip>> gps_trips = entry.getValue();

						gps_trips.entrySet().stream().forEach(gps_entry -> {
							GPSRec rec = gps_entry.getKey();
							if (stored_gps_trips.containsKey(rec)) {
								// System.out.println("Error: GPS record
								// previously matched by another day");
								Hashtable<String, FCDTrip> stored_trips = stored_gps_trips.get(rec);
								Hashtable<String, FCDTrip> trips = gps_entry.getValue();
								stored_trips.putAll(trips);
								stored_gps_trips.replace(rec, stored_trips);
							} else {
								stored_gps_trips.put(rec, gps_entry.getValue());
							}

						});
						matched_chkins_trips.replace(usr, stored_gps_trips);
					} else {
						matched_chkins_trips.put(usr, entry.getValue());
					}
				});
				/**
				 * not completed
				 */
			}

		});
		return matched_chkins_trips;
	}

	public static Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> get_day_chk_matchs(
			Hashtable<String, FCDTrip> fcds, Hashtable<String, List<GPSRec>> chkins) {
		// Hashtable<String, FCDTrip> fcds = Utils.read_fcd(fcd_file_path);
		// Hashtable<String, List<GPSRec>> chkins =
		// Utils.read_checkins(social_pulse_file_path);
		Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips = new Hashtable<>();
		chkins.entrySet().parallelStream().forEach(entry -> {
			String usr = entry.getKey();
			List<GPSRec> rec = entry.getValue();
			Hashtable<GPSRec, Hashtable<String, FCDTrip>> mt_list = new Hashtable<>();
			rec.parallelStream().forEach(chkin -> {
				Hashtable<String, FCDTrip> mt = Utils.find_trips(fcds, chkin);
				// add even empty trips
				if (!mt.isEmpty())
					mt_list.put(chkin, mt);
			});
			// System.out.println(usr);
			if (!mt_list.isEmpty())
				matched_chkins_trips.put(usr, mt_list);
		});
		return matched_chkins_trips;
	}

	public static void plot_mtched_trips(double xmin, double ymin, double xmax, double ymax, ArrayList<Edge> edges,
			Hashtable<GPSRec, Hashtable<String, FCDTrip>> mt, String image_file_path) {
		double sxmin, symin, sxmax, symax;
		sxmin = 0;
		sxmax = xmax - xmin;
		symin = 0;
		symax = ymax - ymin;

		Plot plotter = new Plot(edges, image_file_path);
		plotter.scale(sxmin, symin, sxmax, symax);
		//// plotter.plotMapData(map_path);
		plotter.plotMapEdges();
		// render output
		mt.entrySet().stream().forEach(entry -> {
			double xy[] = DataHandler.proj_coordinates(entry.getKey().p.lat, entry.getKey().p.lon, xmin, ymin);
			StdDraw.setPenColor(Color.ORANGE);
			StdDraw.filledSquare(xy[0], xy[1], 30);
			entry.getValue().entrySet().stream().forEach(tentry -> {
				StdDraw.setPenColor(Color.RED);
				GPSRec d = tentry.getValue().destination;
				double[] dxy = DataHandler.proj_coordinates(d.p.lat, d.p.lon, xmin, ymin);
				StdDraw.filledCircle(dxy[0], dxy[1], 20);
				GPSRec o = tentry.getValue().origin;
				double[] oxy = DataHandler.proj_coordinates(o.p.lat, o.p.lon, xmin, ymin);
				StdDraw.filledCircle(oxy[0], oxy[1], 20);
				StdDraw.setPenColor(Color.BLUE);
				StdDraw.arrow(oxy[0], oxy[1], dxy[0], dxy[1], 3);
			});
		});
		plotter.display_save();
	}

	public static void plot_gps_clusters(double xmin, double ymin, double xmax, double ymax, ArrayList<Edge> edges,
			int min_occurance, List<Cluster> gps_cs, List<GPSRec> locs, String image_file_path) {
		double sxmin, symin, sxmax, symax;
		sxmin = 0;
		sxmax = xmax - xmin;
		symin = 0;
		symax = ymax - ymin;

		Plot plotter = new Plot(edges, image_file_path);
		plotter.scale(sxmin, symin, sxmax, symax);
		//// plotter.plotMapData(map_path);
		plotter.plotMapEdges();
		// render output
		gps_cs.stream().forEach(c -> {
			StdDraw.setPenColor(Color.ORANGE);
			if (c.size() > min_occurance)
				StdDraw.filledSquare(c.centroid.x - xmin, c.centroid.y - ymin, 150);
		});
		locs.stream().forEach(l -> {
			StdDraw.setPenColor(Color.RED);
			StdDraw.filledCircle(l.p.x - xmin, l.p.y - ymin, 160);
		});
		plotter.display_save();
	}

	public static void plot_mtched_trips_shapefile(ShapeFile shapefile,
			Hashtable<GPSRec, Hashtable<String, FCDTrip>> mt, String image_file_path) {

		double[][] bb = shapefile.getSHP_boundingBox();
		// bb [x, y, z][min,max]

		double xmin = bb[0][0], xmax = bb[0][1], ymin = bb[1][0], ymax = bb[1][1];
		Plot p = new Plot(image_file_path);
		p.scale(xmin, ymin, xmax, ymax);

		shapefile_utils.plot_shapefile(shapefile);

		// render output
		mt.entrySet().stream().forEach(entry -> {
			double xy[] = DataHandler.proj_coordinates(entry.getKey().p.lat, entry.getKey().p.lon);
			StdDraw.setPenColor(Color.ORANGE);
			StdDraw.filledSquare(xy[0], xy[1], 30);
			entry.getValue().entrySet().stream().forEach(tentry -> {
				StdDraw.setPenColor(Color.RED);
				GPSRec d = tentry.getValue().destination;
				double[] dxy = DataHandler.proj_coordinates(d.p.lat, d.p.lon);
				StdDraw.filledCircle(dxy[0], dxy[1], 20);
				GPSRec o = tentry.getValue().origin;
				double[] oxy = DataHandler.proj_coordinates(o.p.lat, o.p.lon);
				StdDraw.filledCircle(oxy[0], oxy[1], 20);
				StdDraw.setPenColor(Color.BLUE);
				StdDraw.arrow(oxy[0], oxy[1], dxy[0], dxy[1], 3);
			});
		});
		p.display_save();
	}

	/**
	 * Calculate the absolute difference between two Date without regard for
	 * time offsets
	 *
	 * @param d1
	 *            Date one
	 * @param d2
	 *            Date two
	 * @param field
	 *            The field we're interested in out of day, hour, minute,
	 *            second, millisecond
	 *
	 * @return The value of the required field
	 */
	public static long getTimeDifference(Date d1, Date d2, TimeField field) {
		return Utils.getTimeDifference(d1, d2)[field.ordinal()];
	}

	/**
	 * Calculate the absolute difference between two Date without regard for
	 * time offsets
	 *
	 * @param d1
	 *            Date one
	 * @param d2
	 *            Date two
	 * @return The fields day, hour, minute, second and millisecond
	 */
	public static long[] getTimeDifference(Date d1, Date d2) {
		long[] result = new long[5];
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime(d1);

		long t1 = cal.getTimeInMillis();
		cal.setTime(d2);

		long diff = Math.abs(cal.getTimeInMillis() - t1);
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		final int ONE_SECOND = ONE_MINUTE / 60;

		long d = diff / ONE_DAY;
		diff %= ONE_DAY;

		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;

		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;

		long s = diff / ONE_SECOND;
		long ms = diff % ONE_SECOND;
		result[0] = d;
		result[1] = h;
		result[2] = m;
		result[3] = s;
		result[4] = ms;

		return result;
	}

	/**
	 * Calculate the absolute difference between two Date without regard for
	 * time offsets
	 *
	 * @param d1
	 *            Date one
	 * @param d2
	 *            Date two
	 * @return The fields day, hour, minute, second and millisecond
	 */
	public static long getTimeDifferenceMinutes(Date d1, Date d2) {
		long[] result = new long[5];
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime(d1);

		long t1 = cal.getTimeInMillis();
		cal.setTime(d2);

		long diff = Math.abs(cal.getTimeInMillis() - t1);
		final int ONE_Minute = 1000 * 60;

		return diff / ONE_Minute;
	}

}
