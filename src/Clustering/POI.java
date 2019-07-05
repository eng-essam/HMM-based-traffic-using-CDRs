/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clustering;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import Observations.Obs;

/**
 * Sat 25 Apr 2015 10:39:02 AM JST
 *
 * Extract points of interest for different users to be used in analyze
 * individuals behaviors.
 *
 * @author essam
 */
public class POI {

    final static String RLM = "/";
    final static String CLM = ",";
    public static boolean WEEKEND = true;
    public static boolean WORDDAYS = false;
    public static boolean ASC = true;
    public static boolean DESC = false;
    public static String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    /**
     * print map with frequency of towers and working time percentage.
     *
     * @param map
     */
    public static void print_wtime_map(Map<String, ArrayList<Integer>> map) {
        Map<String, ArrayList<Integer>> tmp_map = sortMapByComparator(map, DESC);
        String val = "";
        for (Map.Entry<String, ArrayList<Integer>> entry : tmp_map.entrySet()) {
            System.out.print(entry.getKey() + CLM);
            ArrayList<Integer> vals = entry.getValue();
            val += Arrays.toString(vals.toArray()) + CLM;
        }
        System.out.println();
        System.out.println(val);

    }

    /**
     * print map
     *
     * @param map
     */
    public static void printMap(Map<String, Integer> map) {
        String val = "";
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.print(entry.getKey() + CLM);
            val += entry.getValue().toString() + CLM;
        }
        System.out.println();
        System.out.println(val);

    }

    /**
     * Sort map based on its values ascending or descending ordering.
     *
     * @param unsortMap
     * @param order
     * @return
     */
    public static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order) {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
			public int compare(Map.Entry<String, Integer> o1,
                    Map.Entry<String, Integer> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static Map<String, ArrayList<Integer>> sortMapByComparator(Map<String, ArrayList<Integer>> unsortMap, final boolean order) {

        List<Map.Entry<String, ArrayList<Integer>>> list = new LinkedList<Map.Entry<String, ArrayList<Integer>>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, ArrayList<Integer>>>() {
            @Override
			public int compare(Map.Entry<String, ArrayList<Integer>> o1,
                    Map.Entry<String, ArrayList<Integer>> o2) {
                if (order) {
                    return o1.getValue().get(0).compareTo(o2.getValue().get(0));
                } else {
                    return o2.getValue().get(0).compareTo(o1.getValue().get(0));

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, ArrayList<Integer>> sortedMap = new LinkedHashMap<String, ArrayList<Integer>>();
        for (Map.Entry<String, ArrayList<Integer>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    final int HOUR = 60;

    public POI() {
    }

    /**
     * Get the working time observation count
     *
     * @param tstamps
     * @return
     */
    public int decide_wtime(String tstamps) {
        String tstamp_arr[] = tstamps.split(CLM);
        int decision = 0; //decision container
        int wtime = 1; //work time wieght
        int ntime = -1; //night time wieght
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar stime_cal = Calendar.getInstance();
        Calendar etime_cal = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        try {
            final Date sTime = formatter.parse("08:00:00");
            final Date eTime = formatter.parse("24:00:00");
            stime_cal.setTime(sTime);
            etime_cal.setTime(eTime);

            for (int i = 0; i < tstamp_arr.length; i++) {
                String tstamp = tstamp_arr[i];
                Date time = null;
                try {
                    time = formatter.parse(tstamp);
                } catch (ParseException parseException) {
                    System.out.println("--> tstamp" + tstamp);
                    System.exit(0);
                }
                cal.setTime(time);
                /**
                 * Working time
                 */
                if (stime_cal.before(cal) && etime_cal.after(cal)) {
                    decision += wtime;
//                    System.out.println(tstamp+"\t"+wtime);
                }
//                else {
//                    /**
//                     * night time / home time
//                     */
//                    decision += ntime;
////                    System.out.println(tstamp+"\t"+ntime);
//                }

            }
//            System.out.println(decision);
        } catch (ParseException ex) {
            Logger.getLogger(POI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return decision;
    }

    private Obs eliminate_0_time_handover(Obs obs) {

        String obs_array[] = obs.getSeq().split(CLM);
        String tstamps[] = obs.getTimeStamp().split(CLM);

        String tmp_obs = obs_array[0]; // new observation sequence.
        String tmp_time = tstamps[0]; // new time stamps

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        /**
         * If the time difference between two successive observations is equal
         * to zero and observations are different then it is a hand-over event.
         */
        for (int i = 1; i < tstamps.length; i++) {
            try {
                Date sTime = formatter.parse(tstamps[i - 1]);
                Date eTime = formatter.parse(tstamps[i]);
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                int diff = (ehour - hour) * HOUR + (eminute - minute);

                /**
                 * We can keep similar observation or just remove it.
                 *
                 * So, for now, with zero difference between observation, I'll
                 * remove both similar and different observations.
                 */
                if (diff != 0) {
                    if (!tmp_obs.isEmpty()) {
                        tmp_obs += CLM;
                        tmp_time += CLM;
                    }
                    tmp_obs += obs_array[i];
                    tmp_time += tstamps[i];
                }
//                else {
//                    //handover occure 
//                    System.out.println(tstamps[i - 1] + "\t" + tstamps[i]);
//                }
            } catch (ParseException ex) {
                Logger.getLogger(POI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Obs(tmp_obs, tmp_time);
    }

    /**
     * Remove the repeated observations to be used in the counting process of
     * observations.
     *
     * @param obs
     * @return
     */
    private Obs eliminate_repeated_obs(Obs obs) {
        String[] obs_set = obs.getSeq().split(CLM);
        String[] tstamps = obs.getTimeStamp().split(CLM);
        String tmp_obs = obs_set[0];
        String tmp_time = tstamps[0];

        String prv = obs_set[0];

        for (int i = 1; i < obs_set.length; i++) {
            String tower = obs_set[i];
            if (!tower.equals(prv)) {
                tmp_obs += CLM + tower;
                tmp_time += CLM + tstamps[i];
                prv = tower;
            }

        }
        return new Obs(tmp_obs, tmp_time);
    }

    /**
     * Find time stamps corresponding to a POI of a user from the observation
     * list.
     *
     * @param day_key
     * @param obs_val
     * @param twr
     * @param weekend
     * @return
     */
    public String find_poi_tstamps(String day_key, Obs obs_val, String twr, boolean weekend) {
        String tstamp = "";

        /**
         * Find a time stamp list corresponding to a user POI points.
         */
        if (weekend) {
            /**
             * Week end days
             */
            if (day_key.contains(weekdays[weekdays.length - 2]) || day_key.contains(weekdays[weekdays.length - 1])) {
                if (obs_val.getSeq().contains(twr)) {
                    String twrs[] = obs_val.getSeq().split(CLM);
                    String tstamps[] = obs_val.getTimeStamp().split(CLM);
                    for (int i = 0; i < twrs.length; i++) {
                        if (twr.equals(twrs[i])) {
                            if (!tstamp.isEmpty()) {
                                tstamp += CLM;
                            }
                            tstamp += tstamps[i];
                        }

                    }
                }
            }

        } else {
            /**
             * Working days
             */
            if (!(day_key.contains(weekdays[weekdays.length - 2]) || day_key.contains(weekdays[weekdays.length - 1]))) {
                if (obs_val.getSeq().contains(twr)) {
                    String twrs[] = obs_val.getSeq().split(CLM);
                    String tstamps[] = obs_val.getTimeStamp().split(CLM);
                    for (int i = 0; i < twrs.length; i++) {
                        if (twr.equals(twrs[i])) {
                            if (!tstamp.isEmpty()) {
                                tstamp += CLM;
                            }
                            tstamp += tstamps[i];
                        }

                    }
                }
            }
        }

        return tstamp;
    }

    /**
     * Get user frequencies POI for a specific user.
     *
     * @param obs
     * @param usr_id
     * @param poi_cnt
     * @return Hashtable< Day key, ArrayList< list of towers >>
     */
    public Hashtable<String, ArrayList<String>> get_d2d_poi(Hashtable<Integer, Hashtable<String, Obs>> obs, int usr_id, int poi_cnt) {
        Hashtable<String, ArrayList<String>> poi_list = new Hashtable<>();
        Hashtable<String, Obs> usr_obs = obs.get(usr_id);

        for (Map.Entry<String, Obs> entrySet : usr_obs.entrySet()) {
            String day_key = entrySet.getKey();
            String day_obs_seq = eliminate_repeated_obs(eliminate_0_time_handover(entrySet.getValue())).getSeq();
            Map<String, Integer> sortedMapDesc = get_frequencies(day_obs_seq);
//            System.out.println("sorted map\t" + sortedMapDesc.size());
            ArrayList<String> list = new ArrayList<>();
            int cnt = 0;
            for (Map.Entry<String, Integer> freq_entry : sortedMapDesc.entrySet()) {
                if (cnt < poi_cnt) {
                    String twr_key = freq_entry.getKey();
                    list.add(twr_key);
                    /**
                     * add only top POI towers
                     */
                    cnt++;
                }
            }
            poi_list.put(day_key, list);
        }
        return poi_list;
    }

    /**
     * Get the frequencies of towers.
     *
     * @param obs_seq
     * @return sorted frequencies descending.
     */
    public Map<String, Integer> get_frequencies(String obs_seq) {
        String twrs[] = obs_seq.split(CLM);
        List asList = Arrays.asList(twrs);
        Set<String> mySet = new HashSet<String>(asList);
        Map<String, Integer> unsortMap = new HashMap<String, Integer>();
        for (String zone : mySet) {
            int freq = Collections.frequency(asList, zone);
            /**
             * Add zones frequency to the unsorted map to be used in time
             * analysis of the highest observed zones.
             */
            unsortMap.put(zone, freq);

        }
        /**
         * Get times of the highest observed zones
         */
        Map<String, Integer> sortedMapDesc = sortByComparator(unsortMap, DESC);
        return sortedMapDesc;
    }

    /**
     * Get the working time corresponding to a user POI from the whole
     * observations.
     *
     * @param obsTable
     * @param sorted_poi
     * @param weekend
     * @return
     */
    public Map<String, ArrayList<Integer>> get_poi_wtime(Hashtable<String, Obs> obsTable, Map<String, Integer> sorted_poi, boolean weekend) {
        Map<String, ArrayList<Integer>> wtime_map = new HashMap();

        /**
         * for each element in the sorted POI map, find the time stamp list
         * corresponding to each one and decide the working time percentage.
         */
        for (Map.Entry<String, Integer> poi_entrySet : sorted_poi.entrySet()) {
            String twr = poi_entrySet.getKey();
            Integer twr_freq = poi_entrySet.getValue();
            String poi_tstamps = "";

            for (Map.Entry<String, Obs> obs_entrySet : obsTable.entrySet()) {
                String day_key = obs_entrySet.getKey();
                Obs obs_val = eliminate_repeated_obs(eliminate_0_time_handover(obs_entrySet.getValue()));
                String tmp = find_poi_tstamps(day_key, obs_val, twr, weekend);
                if (tmp.isEmpty()) {
                    continue;
                }
                if (!poi_tstamps.isEmpty()) {
                    poi_tstamps += CLM;
                }

                poi_tstamps += tmp;
            }
            ArrayList<Integer> freq_list = new ArrayList<>();
            freq_list.add(twr_freq);
            System.out.println(poi_tstamps);
            freq_list.add(decide_wtime(poi_tstamps));
            wtime_map.put(twr, freq_list);

        }
        return wtime_map;
    }

    public ArrayList<Obs> segment_obs(Hashtable<String, Obs> usr_obs, Map<String, Integer> poi) {
        ArrayList<Obs> segments = new ArrayList<>();

        for (Map.Entry<String, Obs> entrySet : usr_obs.entrySet()) {
            String key = entrySet.getKey();
            Obs obs_val = entrySet.getValue();
            System.out.println(key);
            
            String[] twrs = obs_val.getSeq().split(CLM);
            String[] tstamps = obs_val.getTimeStamp().split(CLM);

            for (int i = 0; i < twrs.length; i++) {
                String twr = twrs[i];
                if (poi.containsKey(Integer.parseInt(twr))) {
                    System.out.println("");
                }
                System.out.print(twr + CLM);
            }

            System.out.println("\n================================================");
        }
        return segments;
    }

    /**
     * Write observation CSV file per user
     *
     * @param obs
     * @param csv_path
     */
    public Hashtable<String, Hashtable<String, Double>> writeCSVObs_User(Hashtable<Integer, Hashtable<String, Obs>> obs, String csv_path) {
        final int BEST_N_ZONES = 4;
        Hashtable<String, Hashtable<String, Double>> zones_decision = new Hashtable<>();
        try {
            File file = new File(csv_path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : obs.entrySet()) {
                String all_obs_usr = "";
                String all_tstamps = "";
                Integer user_key = entrySet.getKey();
                bw.append(Integer.toString(user_key));
                bw.newLine();
                bw.append("---------------------------------------------");
                bw.newLine();
                Hashtable<String, Obs> user_obs = entrySet.getValue();
                for (Map.Entry<String, Obs> obs_handling_entrySet : user_obs.entrySet()) {
                    String day_key = obs_handling_entrySet.getKey();
//                    if ((day_key.contains("Friday") || day_key.contains("Saturday") || day_key.contains("Sunday"))) {
//                        continue;
//                    }
//                    Obs day_obs = eliminate_0_time_handover(obs_handling_entrySet.getValue());
                    Obs day_obs = eliminate_repeated_obs(eliminate_0_time_handover(obs_handling_entrySet.getValue()));
                    /**
                     * write dayly observations to the log file
                     */
                    bw.append(day_key + CLM + day_obs.getSeq());
                    bw.newLine();
                    if (!all_obs_usr.isEmpty()) {
                        all_obs_usr += CLM;
                        all_tstamps += CLM;
                    }
                    all_obs_usr += day_obs.getSeq();
                    all_tstamps += day_obs.getTimeStamp();

                }
                bw.append("---------------------------------------------");
                bw.newLine();
                /**
                 * Add frequencies to the observations file.
                 */
                String twrs[] = all_obs_usr.split(CLM);
                List asList = Arrays.asList(twrs);
                Set<String> mySet = new HashSet<String>(asList);
                Map<String, Integer> unsortMap = new HashMap<String, Integer>();
                for (String zone : mySet) {
                    int freq = Collections.frequency(asList, zone);
                    /**
                     * Write frequencies to the log file
                     */
                    bw.append(zone + "\t " + freq);
                    bw.newLine();
                    /**
                     * Add zones frequency to the unsorted map to be used in
                     * time analysis of the highest observed zones.
                     */
                    unsortMap.put(zone, freq);

                }
                bw.append("---------------------------------------------");
                bw.newLine();
                /**
                 * Get times of the highest observed zones
                 */
                Map<String, Integer> sortedMapDesc = sortByComparator(unsortMap, DESC);

                int count = 0;
                Hashtable<String, Double> freq_table = new Hashtable<>();
                for (Map.Entry<String, Integer> freqEntrySet : sortedMapDesc.entrySet()) {
                    String zone_key = freqEntrySet.getKey();
                    int freq = freqEntrySet.getValue();
                    String tmp_obs_tstamps = "";
                    /**
                     * Find only the time of the BEST_N_ZONES highest records
                     */
                    String obs_twrs[] = all_obs_usr.split(CLM);
                    String obs_tstamps[] = all_tstamps.split(CLM);

                    for (int i = 0; i < obs_twrs.length; i++) {
                        if (obs_twrs[i].equals(zone_key)) {
                            if (!tmp_obs_tstamps.isEmpty()) {
                                tmp_obs_tstamps += CLM;
                            }
                            tmp_obs_tstamps += obs_tstamps[i];
                        }

                    }

//                    for (Map.Entry<String, Obs> obs_handling_entrySet : user_obs.entrySet()) {
//                        Obs day_obs = obs_handling_entrySet.getValue();
//                        String obs_twrs[] = day_obs.seq.split(CLM);
//                        String obs_tstamps[] = day_obs.timeStamp.split(CLM);
//                        for (int i = 0; i < obs_twrs.length; i++) {
//                            if (obs_twrs[i].equals(zone_key)) {
//                                if (!tmp_obs_tstamps.isEmpty()) {
//                                    tmp_obs_tstamps += CLM;
//                                }
//                                tmp_obs_tstamps += obs_tstamps[i];
//                            }
//
//                        }
//                    }
//                    count++;
                    if (count++ == BEST_N_ZONES) {
                        break;
                    }
                    int decision = decide_wtime(tmp_obs_tstamps);
                    double w_ratio = (double) decision / (double) freq;
                    freq_table.put(zone_key, w_ratio);
                    bw.append(zone_key + "\t " + freq + "\t " + w_ratio);
                    bw.newLine();

                }
                zones_decision.put(Integer.toString(user_key), freq_table);
                bw.append("=============================================");
                bw.newLine();
            }
            bw.close();
            System.out.println("File Saved");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return zones_decision;
    }

}
