package social_pulse;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;

public class main_social_fcd {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		double minlat = Double.parseDouble(args[0]);
		double minlon = Double.parseDouble(args[1]);
		double maxlat = Double.parseDouble(args[2]);
		double maxlon = Double.parseDouble(args[3]);
		String social_pulse_file_path = args[4];
		String fcd_file_path = args[5];
		String matched_chk_trips_file_path = args[6];
		String edges_file_path = args[7];
		String image_file_path = args[8];

		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();

		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		// Hashtable<String, FCDTrip> fcds = Utils.read_fcd(fcd_file_path);
		// Hashtable<String, List<GPSRec>> chkins =
		// Utils.read_checkins(social_pulse_file_path);
		// Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>>
		// matched_chkins_trips = new Hashtable<>();
		// chkins.entrySet().parallelStream().forEach(entry -> {
		// String usr = entry.getKey();
		// List<GPSRec> rec = entry.getValue();
		// Hashtable<GPSRec, Hashtable<String, FCDTrip>> mt_list = new
		// Hashtable<>();
		// rec.stream().forEach(chkin -> {
		// Hashtable<String, FCDTrip> mt = Utils.find_trips(fcds, chkin);
		// // add even empty trips
		// if (!mt.isEmpty())
		// mt_list.put(chkin, mt);
		// });
		// System.out.println(usr);
		// if(!mt_list.isEmpty())
		// matched_chkins_trips.put(usr, mt_list);
		// });

		double spatial_gps_thr = 500;
		double temporal_gps_thr = 60;
		double dbscan_thr = 500;
		int min_occurance = 5;
		GeoRelations relations = new GeoRelations(spatial_gps_thr, temporal_gps_thr, dbscan_thr);
		Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips = relations
				.get_chk_matchs(social_pulse_file_path, fcd_file_path);
		Utils.write_matched_chkins_trips(matched_chkins_trips, matched_chk_trips_file_path + "/matchedchk trips.csv");

		Hashtable<String, List<Cluster>> pois = relations.get_Pois();
		Utils.write_POIs(pois, matched_chk_trips_file_path + "/matchedchk trips poi.csv");

		pois.entrySet().stream().forEach(entry -> {
			String output = image_file_path + "/" + entry.getKey() + ".png";
			List<GPSRec> locs = new ArrayList<>(matched_chkins_trips.get(entry.getKey()).keySet());
			Utils.plot_gps_clusters(xymin[0], xymin[1], xymax[0], xymax[1], edges, min_occurance, entry.getValue(),
					locs, output);
		});

	}

}
