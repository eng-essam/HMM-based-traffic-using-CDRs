package social_pulse;

import java.util.Hashtable;

import diewald_shapeFile.shapeFile.ShapeFile;
import geoshape.shapefile_utils;

public class main_social_fcd_shapefile {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String social_pulse_file_path = args[0];
		String fcd_file_path = args[1];
		String matched_chk_trips_file_path = args[2];
		String shapefile_dir = args[3];
		String shapefile_name = args[4];
		String proj_file_path = args[5];
		String image_file_path = args[6];

		Hashtable<String, Hashtable<GPSRec, Hashtable<String, FCDTrip>>> matched_chkins_trips = Utils
				.get_chk_matchs(social_pulse_file_path, fcd_file_path,proj_file_path);
		Utils.write_matched_chkins_trips(matched_chkins_trips, matched_chk_trips_file_path);

		ShapeFile shapefile = shapefile_utils.read_shapefile(shapefile_dir, shapefile_name);

		matched_chkins_trips.entrySet().stream().forEach(entry -> {
			String output = image_file_path + "/" + entry.getKey() + ".png";
			Utils.plot_mtched_trips_shapefile(shapefile, entry.getValue(), output);
		});

	}

}
