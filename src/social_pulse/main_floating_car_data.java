package social_pulse;

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;

import utils.DataHandler;

public class main_floating_car_data {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = args[0];
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
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
//					System.out.println(prev_usr + "\t"
//							+ Utils.getTimeDifferenceMinutes(t.origin.timestamp, t.destination.timestamp));
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
					r.timestamp = df.parse(rec[1]).getTime();
					r.p.lat = Double.parseDouble(rec[2]);
					r.p.lon = Double.parseDouble(rec[3]);
					t.trace.add(r);
				} catch (Exception e) {
					System.out.println("Error parsing GPS record");
				}

			} else {
				GPSRec r = new GPSRec();
				try {
					r.timestamp = df.parse(rec[1]).getTime();
					r.p.lat = Double.parseDouble(rec[2]);
					r.p.lon = Double.parseDouble(rec[3]);

					t.trace.add(r);
				} catch (Exception e) {
					System.out.println("Error parsing GPS record");
				}
			}
		}
		System.out.println(trips.size());
	}

}
