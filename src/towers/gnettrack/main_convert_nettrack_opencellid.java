/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.gnettrack;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Observations.ObsTripsBuilder;
import utils.DataHandler;

/**
 *
 * @author essam
 * 
 * java -Xss1g -d64 -XX:+UseG1GC -Xms7g -Xmx7g -XX:MaxGCPauseMillis=500 -cp .:../lib/jcoord-1.0.jar:../lib/diva.jar:../lib/jgrapht.jar:../lib/guava.jar:../lib/commons-math3-3.5.jar towers.gnettrack.main_convert_nettrack_opencellid
 */
public class main_convert_nettrack_opencellid {

    /**
     * @param args the command line arguments
     */
    public static String header = "mcc,mnc,lac,cellid,lon,lat,signal,act";
    public static String fields[] = {"Operator", "LAC", "CellID", "Longitude", "Latitude", "Level", "NetworkMode"};

    public static void main(String[] args) {
        // mcc,mnc,lac,cellid,lon,lat,signal,act
//        String g_net_log_path = "/home/essam/Vodafone_2016.01.16_20.34.19/Vodafone_2016.01.16_20.34.19.txt";
//        String output_path = "/home/essam/Vodafone_2016.01.16_20.34.19/Vodafone_2016.01.16_20.34.19_opencellid.txt";

        String g_net_log_path = "/home/essam/traffic/tmp/";
        String output_path = "/home/essam/traffic/log.csv";
        String towers_path = "/home/essam/traffic/models/cell_towers.csv";

        String[] towers = DataHandler.extract_info_field(DataHandler.read_csv(towers_path, DataHandler.COMMA_SEP), 0);

        multiple_file_hits(towers, g_net_log_path);
        List<String[]> opencell_fields = subset(multiple_file(g_net_log_path));
        DataHandler.write_csv(opencell_fields, header, output_path);

//        String[] obs = Utils.extract_info_field(opencell_fields, 3);
//        opencellid_database_hits(obs, towers);
    }

    public static List<String[]> multiple_file(String g_net_log_path) {
        List<String[]> opencell_fields = new ArrayList<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(g_net_log_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            List<String[]> log = DataHandler.read_csv(subsetPath, DataHandler.TAB_SEP);
            int[] indices = DataHandler.find_indices(log, fields);
            log.remove(0);

            opencell_fields.addAll(split_string_chars(DataHandler.extract_info(log, indices)));
        }

        return opencell_fields;
    }

    public static void multiple_file_hits(String[] towers, String g_net_log_path) {
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(g_net_log_path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            System.out.println(fileName);
            List<String[]> log = DataHandler.read_csv(subsetPath, DataHandler.TAB_SEP);
            int[] indices = DataHandler.find_indices(log, fields);
            log.remove(0);

            String[] obs = DataHandler.extract_info_field(log, indices[2]);
            opencellid_database_hits(obs, towers);
            System.out.println("-----------------------------------");
        }

    }

    public static void opencellid_database_hits(String[] obs, String[] o_id) {
        double[] open_ids = DataHandler.to_double(o_id);
        double[] t_obs = DataHandler.to_double(obs);

        List<Integer> id_list = new ArrayList<>();
        List<Integer> unique_obs = new ArrayList<>();

        int total = 0;
        int hits = 0;

        // mod cell ids
        for (int i = 0; i < open_ids.length; i++) {
            int id = (int) (open_ids[i] % 65536);
            if (!id_list.contains(id)) {
                id_list.add(id);
            }
        }

        System.out.println("The total number of observaions is: " + t_obs.length);
        for (int i = 0; i < t_obs.length; i++) {
            int t_ob = (int) t_obs[i];
            if (!unique_obs.contains(t_ob)) {
                unique_obs.add(t_ob);
            }

        }

        total = unique_obs.size();

        for (int i = 0; i < unique_obs.size(); i++) {
            int id = unique_obs.get(i);
            if (id_list.contains(id)) {
                hits++;
            }
        }

        System.out.printf("The tatal number of towers:%d \t hits: %d \t ratio: %f\n", total, hits, ((double) hits / (double) total));

    }

    public static void single_file(String g_net_log_path, String output_path) {
        List<String[]> log = DataHandler.read_csv(g_net_log_path, DataHandler.TAB_SEP);
        int[] indices = DataHandler.find_indices(log, fields);
        log.remove(0);
        List<String[]> opencell_fields = split_string_chars(DataHandler.extract_info(log, indices));
        DataHandler.write_csv(opencell_fields, header, output_path);
    }

    /**
     * split operator id into mcc and mnc
     *
     * @param opencell_fields
     * @return
     */
    public static List<String[]> split_string_chars(List<String[]> opencell_fields) {
        List<String[]> updated_opencell_fields = new ArrayList<>();

        for (Iterator<String[]> iterator = opencell_fields.iterator(); iterator.hasNext();) {
            String[] fields = iterator.next();
            String[] nfields = new String[fields.length + 1];

            char operator[] = fields[0].toCharArray();
            String mcc = "", mnc = "";

            for (int i = 0; i < operator.length; i++) {
                if (i < operator.length - 2) {
                    mcc += operator[i];
                } else {
                    mnc += operator[i];
                }
            }

            nfields[0] = mcc;
            nfields[1] = mnc;

            for (int i = 1; i < fields.length; i++) {
                nfields[i + 1] = fields[i];
            }
            // if there is a lon/lat records ...
            if (nfields[4].compareToIgnoreCase("--") != 0 && nfields[5].compareToIgnoreCase("--") != 0 && nfields[3].compareToIgnoreCase("--") != 0) {
                updated_opencell_fields.add(nfields);
            }
        }
        return updated_opencell_fields;
    }

    private static List<String[]> subset(List<String[]> opencell_fields) {
        Hashtable<String, List<String[]>> subset_towers = new Hashtable<>();

        for (Iterator<String[]> iterator = opencell_fields.iterator(); iterator.hasNext();) {
            String[] tower_data = iterator.next();
            String tower_id = tower_data[3];
            if (subset_towers.containsKey(tower_id)) {
                List<String[]> list = subset_towers.get(tower_id);
                if (list.size() < 100) {
                    list.add(tower_data);
                    subset_towers.replace(tower_id, list);
                }
            } else {
                List<String[]> list = new ArrayList<>();
                list.add(tower_data);
                subset_towers.put(tower_id, list);
            }

        }
        List<String[]> r_subset_obs = new ArrayList<>();
        for (Map.Entry<String, List<String[]>> entry : subset_towers.entrySet()) {
            String key = entry.getKey();
            List<String[]> value = entry.getValue();
            r_subset_obs.addAll(value);
        }
        return r_subset_obs;
    }

}
