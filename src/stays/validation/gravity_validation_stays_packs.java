/**
 * 
 */
package stays.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import mergexml.MapDetails;
import stays.trips_semantic_handler;
import utils.Vertex;
import validation.GravityModel;

/**
 * @author essam
 *
 */
public class gravity_validation_stays_packs {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ac_trips_dir_name = args[0];
		String towers_file_name = args[1];

		final double threshold = 500f;
		// double flow_limit = 4;
		boolean verbose = false;
		boolean intercept = true;
		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(ac_trips_dir_name));

		// read stays trips
		trips_semantic_handler tsh = new trips_semantic_handler();
		MapDetails details = new MapDetails();
		Hashtable<Integer, Vertex> towers = details.readTowers(towers_file_name);

		GravityModel model = new GravityModel();
		Hashtable<String, Hashtable<String, Double>> distances = model.calcDistances(towers);
		int days_cnt = 0;

		for (Iterator it = files.iterator(); it.hasNext();) {
			String ac_trips_file_name = (String) it.next();
			String file_name = ac_trips_file_name.substring(ac_trips_file_name.lastIndexOf("/") + 1);
			if (file_name.matches("SET2_P[0-9]+.CSV")) {
				System.out.println(file_name);
				Hashtable<String, Hashtable<Integer, Obs>> obs = tsh.read_trips(ac_trips_file_name);
				for (Map.Entry<String, Hashtable<Integer, Obs>> entry : obs.entrySet()) {
					String day = entry.getKey();
					if (!tsh.is_week_end(day)) {
						List<String[]> trips = tsh.list_trips(entry.getValue());
						model.handle_zones_Flow(trips);
						days_cnt++;
					}

				}

			}
		}

		/**
		 * Avg flow
		 */
		// model.average_flow(days_cnt);

		Hashtable<Double, ArrayList<Double>> fgod;

		if (verbose) {
			/**
			 * Print verbose flow data
			 */
			System.out.println("-------------------------------------------------");
			System.out.println("Number of days" + days_cnt);
			System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "From-zone", "To-zone", "Dist", "Flow", "Outs", "Ins",
					"GOD");
			fgod = model.computeGOD(threshold, verbose);
			System.out.println("-------------------------------------------------");
		} else {
			fgod = model.computeGOD(threshold, verbose);
		}

		String fgod_csv_path = ac_trips_dir_name.substring(0, ac_trips_dir_name.lastIndexOf('/')) + "/gravity";
		File dir = new File(fgod_csv_path);
		dir.mkdir();

		String fgod_path = fgod_csv_path + "/fgod." + days_cnt + ".days.csv";
		model.writeFGOD(fgod_path, fgod);
		Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
		fgod_path = fgod_csv_path + "/avg.fgod." + days_cnt + ".days.csv";
		model.writeAvgGOD(fgod_path, avgGOD);
		System.out.printf("Number of days %d\tGravity vs. flow R^2=%f\tAvg Gravity vs. flow R^2=%f\n", days_cnt,
				model.find_all_linear_regression(fgod, intercept), model.find_avg_linear_regression(avgGOD, intercept));

	}

	
}
