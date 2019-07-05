/**
 * Interpolate trips splitted by the detected stays ..
 * 
 * Date: Thu Mar 17 12:36:51 EET 2016
 * 
 */

package stays.interpolation;

import java.util.ArrayList;
import java.util.Hashtable;

import Observations.Obs;
import Voronoi.VoronoiConverter;
import stays.trips_semantic_handler;
import utils.DataHandler;
import utils.Vertex;

public class main_interpolate_stays_trips {

	public static void main(String[] args) {
		
		String ac_trips_file_name = args[0];
		String towers_file_name = args[1];
		String neighbors_path = args[2];
		String indicators = args[3];
		String out_ac_interpolated_trips_file_name = args[4];

		trips_semantic_handler tsh = new trips_semantic_handler();
		Hashtable<String, Hashtable<Integer, Obs>> obs = tsh.read_trips(ac_trips_file_name);
		Hashtable<String, Hashtable<Integer, Obs>> interpolated_obs = new Hashtable<>();
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_file_name);

		Hashtable<Integer, Double> rg = DataHandler.extract_gyration_field(indicators);
		Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = new VoronoiConverter()
				.readVorNeighbors(neighbors_path);

		obs.entrySet().stream().forEach((entry) -> {
			String day_key = entry.getKey();
			Hashtable<Integer, Obs> days_obs = tsh.interpolate_stays_trips(entry.getValue(), voronoi_neighbors, towers,
					rg);
			if (!days_obs.isEmpty()) {
				interpolated_obs.put(day_key, days_obs);
			}
		});
		
		tsh.write_interpolated_trips(interpolated_obs, out_ac_interpolated_trips_file_name);

	}

}
