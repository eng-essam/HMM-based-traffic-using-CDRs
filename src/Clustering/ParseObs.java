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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;

/**
 *
 * @author essam
 */
public class ParseObs {

    final static String CLM = ",";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

            int stower = 0;
            int etower = 1666;
    //        String dSPath = "E:\\D4D\\SET2";
            String dSPath = "/home/essam/traffic/SET2";
    //        String dSPath = "/media/essam/Files/D4D/SET2";
            ObsTripsBuilder builder = new ObsTripsBuilder();
            ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dSPath));
            Hashtable<Integer, Hashtable<String, Obs>> obsTable = new Hashtable<>();
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
                Hashtable<Integer, Hashtable<String, Obs>> tmp_obsTable = builder.buildObsDUT(subsetPath, stower, etower);
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
        String dist = dSPath + "/Results/Observation_study.csv";
        System.out.println("Writing CSV");
        Hashtable<String, Hashtable<String, Double>> zones_decision = poi.writeCSVObs_User(obsTable, dist);
        String fdist = dSPath + "/Results/Observation_study.freq.csv";
        write_statistics(zones_decision, fdist, false);
        String fwdist = dSPath + "/Results/Observation_study.freq.weight.csv";
        write_statistics(zones_decision, fwdist, true);
        String work_dest = dSPath + "/Results/work.csv";
        write_work_loc(zones_decision, work_dest, true);
        String home_dest = dSPath + "/Results/home.csv";
        write_work_loc(zones_decision, home_dest, false);
        String all_dest = dSPath + "/Results/all.txt";
        write_statistics_all_zones(zones_decision, all_dest, etower);
    }

    public static void write_statistics(Hashtable<String, Hashtable<String, Double>> statistics, String path, boolean weight_flag) {
        try {
            File file = new File(path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Map.Entry<String, Hashtable<String, Double>> statisticsEntrySet : statistics.entrySet()) {
                String usr_key = statisticsEntrySet.getKey();
                bw.append(usr_key + CLM);
                String weight = "";
                weight += usr_key + CLM;
                Hashtable<String, Double> freq_zones = statisticsEntrySet.getValue();
                for (Map.Entry<String, Double> entrySet : freq_zones.entrySet()) {
                    String zone_key = entrySet.getKey();
                    Double work_presentage = entrySet.getValue();
                    bw.append(zone_key + CLM);
                    weight += work_presentage + CLM;
                }
                bw.newLine();
                if (weight_flag) {
                    bw.append(weight);
                    bw.newLine();
                }
            }
            bw.close();
            System.out.println("File Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write_statistics_all_zones(Hashtable<String, Hashtable<String, Double>> statistics, String path, int end_zone) {
        try {
            File file = new File(path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            double arr[];
            /**
             * write header of the data;
             */
//            bw.append("User_ID");
//            for (int i = 0; i <= end_zone; i++) {
//                bw.append("\t" + i);
//            }
//            bw.newLine();

            /**
             * Write weights
             */
            for (Map.Entry<String, Hashtable<String, Double>> statisticsEntrySet : statistics.entrySet()) {
                arr = new double[end_zone + 1];
                String usr_key = statisticsEntrySet.getKey();
                Hashtable<String, Double> freq_zones = statisticsEntrySet.getValue();
                for (Map.Entry<String, Double> entrySet : freq_zones.entrySet()) {
                    int zone_key = Integer.parseInt(entrySet.getKey());
                    arr[zone_key] = entrySet.getValue();
                }
                String weight = Arrays.toString(arr).replace('[', ' ').replace(']', ' ').replace(",", "\t");
                bw.append(usr_key + "\t" + weight);
                bw.newLine();
            }
            bw.close();
            System.out.println("File Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write_work_loc(Hashtable<String, Hashtable<String, Double>> statistics, String path, boolean work_flag) {
        try {
            File file = new File(path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Map.Entry<String, Hashtable<String, Double>> statisticsEntrySet : statistics.entrySet()) {
                String usr_key = statisticsEntrySet.getKey();
                bw.append(usr_key + CLM);
                Hashtable<String, Double> freq_zones = statisticsEntrySet.getValue();
                for (Map.Entry<String, Double> entrySet : freq_zones.entrySet()) {
                    String zone_key = entrySet.getKey();
                    Double work_presentage = entrySet.getValue();
                    if (work_flag) {
                        if (work_presentage > 0.65) {
                            bw.append(zone_key + CLM);
                        }
                    } else {
                        if (work_presentage < 0.40) {
                            bw.append(zone_key + CLM);
                        }
                    }
                }
                bw.newLine();
            }
            bw.close();
            System.out.println("File Saved");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
