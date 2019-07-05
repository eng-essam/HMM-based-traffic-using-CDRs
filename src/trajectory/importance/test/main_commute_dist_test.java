/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

import java.util.Hashtable;
import java.util.List;

import trajectory.importance.TCluster;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_commute_dist_test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String poi_path = args[0];
        String towers_path = args[1];
        String output_path = args[2];

        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);
                Hashtable<Integer, List<TCluster>> usrs_clusters = DataHandler.read_xml_poi(poi_path);
        double dist[] = DataHandler.get_commute_distances(usrs_clusters, towers);

        DataHandler.write_csv(DataHandler.get_distribution(dist), "commute_distance,distribution", output_path + "/commute_distace_distribution.csv");

        /*for (Map.Entry<Integer, List<TCluster>> entrySet : usrs_clusters.entrySet()) {
        int usr_key = entrySet.getKey();
        System.out.printf("===================== %d =======================\n", usr_key);
        List<TCluster> clusters = entrySet.getValue();
        
        int i = 1;
        System.out.println(" \tDays\tTower_days\tDuration\tHome_events\tWork_events");
        for (Iterator<TCluster> iterator = clusters.iterator(); iterator.hasNext();) {
        TCluster tc = iterator.next();
        
        System.out.printf("%d\t%d\t%d\t%d\t%d\t%d\t%b\t%b\n",
        i++,
        tc.get_days(),
        tc.get_tower_days(),
        tc.get_duration(),
        tc.get_home_hour_events(),
        tc.get_work_hour_events(),
        tc.is_home_loc(),
        tc.is_work_loc());
        //
        //                System.out.println("");
        //
        //                String hw_flag = "-";
        //                if (tc.is_home_loc()) {
        //                    hw_flag = "home";
        //                } else if (tc.is_work_loc()) {
        //                    hw_flag = "work";
        //                }
        //                System.out.printf("Centroid: %d\tDays: %f\tTower days: %f\tDuration: %f\t|| Home events: %d  Work events: %d --> %s\n",
        //                        tc.get_centroid(),
        //                        tc.get_days_percent(),
        //                        tc.get_tower_days_percent(),
        //                        tc.get_duration_percent(),
        //                        tc.get_home_hour_events(),
        //                        tc.get_work_hour_events(),
        //                        hw_flag);
        
        }
        
        }*/
    }

}
