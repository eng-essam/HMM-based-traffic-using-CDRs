/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import Voronoi.VoronoiConverter;
import trajectory.importance.HPopulation;
import trajectory.importance.Leader;
import trajectory.importance.TCluster;
import trajectory.importance.Tower;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_important_places_parallel_test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int start_id = Integer.parseInt(args[0]);
        int end_id = Integer.parseInt(args[1]);
        boolean weekend_days_flag = Boolean.parseBoolean(args[2]);
//        int ndays = Integer.parseInt(args[0]);
        String dataset_path = args[3];
        String n_vor_file = args[4];
        String towers_path = args[5];

        VoronoiConverter converter = new VoronoiConverter();
//        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor_r_file);
        Hashtable<Integer, ArrayList<Integer>> nvor = converter.readVorNeighbors(n_vor_file);
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);

        HPopulation hp = new HPopulation();
        long lStartTime = System.nanoTime();
        Hashtable<Integer, List<Tower>> usrs_obs = hp.sort_towers(hp.parallel_populate(dataset_path, start_id, end_id, weekend_days_flag));
        long lEndTime = System.nanoTime();
        long difference = lEndTime - lStartTime;
        System.out.println("Elapsed for parallel execuation milliseconds: " + difference / 1000000);

//        usrs_obs.clear();

//        lStartTime = System.nanoTime();
//        usrs_obs = hp.sort_towers(hp.populate(start_id, end_id, weekend_days_flag));
//        lEndTime = System.nanoTime();
//        difference = lEndTime - lStartTime;
//        System.out.println("Elapsed for serial execuation milliseconds: " + difference / 1000000);

        int ndays = hp.get_total_days();
        System.out.println("The total number of days used in the study is: " + ndays);
        Leader leader = new Leader(ndays, nvor, towers, usrs_obs);
        Hashtable<Integer, List<TCluster>> usrs_clusters = leader.parallel_scan();

        /*for (Map.Entry<Integer, List<Tower>> entrySet : usrs_obs.entrySet()) {
         Integer key = entrySet.getKey();
         List<Tower> usr_cdrs_list = entrySet.getValue();
         for (Iterator<Tower> iterator = usr_cdrs_list.iterator(); iterator.hasNext();) {
         Tower t = iterator.next();
         System.out.printf("id: %d\tdays: %d\thome hour events: %d\twork hour events: %d\n",t.get_id(),t.days(),t.home_hour_event(),t.work_hour_event());
        
         }
        
         }
         */
        Hashtable<Double, Double> important_places = new Hashtable<>();
        double[] important_places_stat = new double[usrs_clusters.size()];
        int i = 0;
        for (Map.Entry<Integer, List<TCluster>> entrySet : usrs_clusters.entrySet()) {
            double usr_key = entrySet.getKey();
//            System.out.printf("===================== %d =======================\n", key);
            List<TCluster> clusters = entrySet.getValue();
            double num_clusters = clusters.size();
            important_places.put(usr_key, num_clusters);
            important_places_stat[i++] = num_clusters;

//            int i = 1;
//            System.out.println(" \tDays\tTower_days\tDuration\tHome_events\tWork_events");
//            for (Iterator<TCluster> iterator = clusters.iterator(); iterator.hasNext();) {
//                TCluster tc = iterator.next();
//
////                System.out.printf("%d\t%d\t%d\t%d\t%d\t%d\n",
////                        i++,
////                        tc.get_days(),
////                        tc.get_tower_days(),
////                        tc.get_duration(),
////                        tc.get_home_hour_events(),
////                        tc.get_work_hour_events());
////                
////                System.out.println("");
////
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
//
//            }
        }

        DataHandler.write_xml_poi(usrs_clusters, dataset_path + "/results/clusters_" + Integer.toString(start_id) + "_" + Integer.toString(end_id) + ".xml");
        DataHandler.write_csv(important_places, "user_id,num_important_places", dataset_path + "/results/num_cluster_" + Integer.toString(start_id) + "_" + Integer.toString(end_id) + ".csv");
        DataHandler.write_csv(DataHandler.get_distribution(important_places_stat), "num_important_places,distribution", dataset_path + "/results/num_cluster_distribution_" + Integer.toString(start_id) + "_" + Integer.toString(end_id) + ".csv");
    }

}
