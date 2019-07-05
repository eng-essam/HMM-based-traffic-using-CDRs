/**
 * 
 */
package stays.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Observations.Obs;
import mergexml.MapDetails;
import stays.trips_semantic_handler;
import utils.Vertex;
import validation.GravityModel;

/**
 * @author essam
 *
 */
public class gravity_validation_stays_single_day {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String ac_trips_file_name= args[0];
		String towers_file_name = args[1];
		
		final double threshold = 100f;
//      double flow_limit = 4;
      boolean verbose = false;
      boolean intercept = true;
      
		// read stays trips 
		trips_semantic_handler tsh = new trips_semantic_handler();
		Hashtable<String, Hashtable<Integer, Obs>> obs= tsh.read_trips(ac_trips_file_name);
		
		MapDetails details = new MapDetails();
        Hashtable<Integer, Vertex> towers = details.readTowers(towers_file_name);
        
        GravityModel model = new GravityModel();
        Hashtable<String, Hashtable<String, Double>> distances = model.calcDistances(towers);
        
        int days_cnt = 0;
        for(Map.Entry<String, Hashtable<Integer, Obs>> entry:obs.entrySet()){
        	String day = entry.getKey();
        	List<String[]> trips = tsh.list_trips(entry.getValue());
        	model = new GravityModel();
        	model.setDistances(distances);
        	model.handle_zones_Flow(trips);
        	
        	/**
             * Avg flow
             */
//            model.average_flow(days_cnt);

            Hashtable<Double, ArrayList<Double>> fgod;

            if (verbose) {
                /**
                 * Print verbose flow data
                 */
                System.out.println("-------------------------------------------------");
                System.out.println("Number of days" + days_cnt);
                System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\n", "From-zone", "To-zone", "Dist", "Flow", "Outs", "Ins", "GOD");
                fgod = model.computeGOD(threshold, verbose);
                System.out.println("-------------------------------------------------");
            } else {
                fgod = model.computeGOD(threshold, verbose);
            }
            
            String fgod_csv_path = ac_trips_file_name.substring(0, ac_trips_file_name.lastIndexOf('/')) + "/data";
            File dir = new File(fgod_csv_path);
            dir.mkdirs();

            String fgod_path = fgod_csv_path + "/fgod.day_" + day + ".csv";
            model.writeFGOD(fgod_path, fgod);
            Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
            fgod_path = fgod_csv_path + "/avg.fgod.day_" + day + ".csv";
            model.writeAvgGOD(fgod_path, avgGOD);
            System.out.printf("Day %s\tGravity vs. flow R^2=%f\tAvg Gravity vs. flow R^2=%f\n", day, model.find_all_linear_regression(fgod, intercept), model.find_avg_linear_regression(avgGOD, intercept));

        }

	}

}
