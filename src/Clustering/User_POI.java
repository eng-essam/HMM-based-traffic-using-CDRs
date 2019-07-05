/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;

/**
 *
 * @author essam
 */
public class User_POI {

    final static String CLM = ",";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final int stower = 0;
        final int etower = 1666;
        final int top_poi = 4;
        final int usr_id = 210;
        //        String dSPath = "E:\\D4D\\SET2";
        String dSPath = "/home/essam/traffic/SET2/all";
        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        //        String dSPath = "/media/essam/Files/D4D/SET2";
        ObsTripsBuilder builder = new ObsTripsBuilder();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dSPath));
        Hashtable<Integer, Hashtable<String, Obs>> obsTable = new Hashtable<>();
        /**
         * For the typical Senegalese business Saturday and Sunday are days off.
         *
         * Buffer all work days frequent location visited by a user.
         */
        Map<String, Integer> workdays_freq = new HashMap<>();
        /**
         * Buffer weekends frequent location visited.
         */
        Map<String, Integer> weekend_freq = new HashMap<>();

        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                Hashtable<Integer, Hashtable<String, Obs>> tmp_obsTable = builder.buildObsDUT(subsetPath, usr_id, stower, etower);
                /**
                 * Append current records to the observation table, to study a
                 * larger dataset.
                 */
                for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : tmp_obsTable.entrySet()) {
                    Integer key = entrySet.getKey();
                    Hashtable<String, Obs> tmp_value = entrySet.getValue();
                    if (!obsTable.containsKey(key)) {
                        obsTable.put(key, tmp_value);
                    } else {
                        Hashtable<String, Obs> all_value = obsTable.get(key);
                        for (Map.Entry<String, Obs> tmp_entrySet : tmp_value.entrySet()) {
                            all_value.put(tmp_entrySet.getKey(), tmp_entrySet.getValue());
                        }
                        obsTable.put(key, all_value);
                    }

                }
            }
        }

        POI poi = new POI();
        Hashtable<String, ArrayList<String>> poi_list = poi.get_d2d_poi(obsTable, usr_id, top_poi);

        /**
         * Print the most visited placed on a specific day, and the most visited
         * tower over weeks on the same day
         */
        String places = "";
        for (int i = 0; i < weekdays.length; i++) {
            String weekday = weekdays[i];
            for (Map.Entry<String, ArrayList<String>> entrySet : poi_list.entrySet()) {
                String day_key = entrySet.getKey();
                if (!day_key.endsWith(weekday)) {
                    continue;
                }

                System.out.print(day_key);
                ArrayList<String> twrs = entrySet.getValue();
                for (String twr : twrs) {
                    if (!places.isEmpty()) {
                        places += CLM;
                    }
                    places += twr;
                    System.out.print(CLM + twr);
                }
                System.out.println();
            }
            /**
             * Find frequencies of a week day over weeks of most visited places.
             */
            System.out.println("==========================================");
            Map<String, Integer> wd_freq = poi.get_frequencies(places);
            System.out.print(weekday + CLM);
            String freq_str = weekday + CLM;
            int cnt = 0;
            for (Map.Entry<String, Integer> wd_freq_entry : wd_freq.entrySet()) {
                if (cnt < top_poi) {
                    String key = wd_freq_entry.getKey();
                    int freq = wd_freq_entry.getValue();
                    System.out.print(key + CLM);
                    freq_str += freq + CLM;
                    /**
                     * Cumulate visited locations.
                     */
                    if (weekday.equals(weekdays[weekdays.length - 2]) || weekday.equals(weekdays[weekdays.length - 1])) {
                        /* weekends locations.*/
                        if (weekend_freq.containsKey(key)) {
                            int total = freq + weekend_freq.get(key);
                            weekend_freq.replace(key, total);
                        } else {
                            weekend_freq.put(key, freq);
                        }

                    } else {
                        if (workdays_freq.containsKey(key)) {
                            int total = freq + workdays_freq.get(key);
                            workdays_freq.replace(key, total);
                        } else {
                            workdays_freq.put(key, freq);
                        }
                    }

                    cnt++;
                }
            }
            System.out.println("\n" + freq_str
                    + "\n==========================================");

        }

        Map<String, Integer> sortedMapDesc = POI.sortByComparator(workdays_freq, POI.DESC);
        System.out.println("\n==========================================\nWorkdays frequent locations:");
//        poi.print_wtime_map(poi.get_poi_wtime(obsTable.get(usr_id), sortedMapDesc, POI.WORDDAYS));
        POI.printMap(sortedMapDesc);

        System.out.println("\n==========================================\nWeekend days frequent locations:");
        sortedMapDesc = POI.sortByComparator(weekend_freq, POI.DESC);
        POI.printMap(sortedMapDesc);
        POI.print_wtime_map(poi.get_poi_wtime(obsTable.get(usr_id), sortedMapDesc, POI.WEEKEND));

//        poi.segment_obs(obsTable.get(usr_id), sortedMapDesc);
        

    }

}
