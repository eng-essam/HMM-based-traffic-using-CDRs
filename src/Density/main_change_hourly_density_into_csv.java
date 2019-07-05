package Density;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Observations.ObsTripsBuilder;
import utils.DataHandler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class main_change_hourly_density_into_csv {

    /**
     * @param args the command line arguments
     */
    private static String density_regex = "density_[0-9]+:0:0_[0-9]+:0:0.xml";
    private static String daily_density_regex = "[0-9]+-[0-9]+-[0-9]+.xml";
    private static List<String> edges = new ArrayList<>();

    private static void append_edges(Hashtable<String, Integer> hourly_density) {

        hourly_density.entrySet().stream().map((entry) -> entry.getKey()).filter((key) -> (!edges.contains(key))).forEach((key) -> {
            edges.add(key);
        });

    }

    private static List<String[]> convert_table_list(Hashtable<String, Hashtable<String, Integer>> acc_densities) {

        List<String[]> densities = new ArrayList<>();
        int size = edges.size() + 1;

        String[] header = new String[size];
        header[0] = "date";
        for (int i = 0; i < edges.size(); i++) {
            String edge = edges.get(i);
            header[i + 1] = "x" + edge.replace("#", "_").replace("-", "_");
//            header[i + 1] = "x" + edge;
        }

        densities.add(header);

        System.out.println(Arrays.toString(header));
        String[] data;
        for (Map.Entry<String, Hashtable<String, Integer>> entry : acc_densities.entrySet()) {
            String date_str = entry.getKey();
            Hashtable<String, Integer> hourly_density = entry.getValue();
            data = new String[size];
            Arrays.fill(data, "0");
            data[0] = date_str;
            for (Map.Entry<String, Integer> hd_entry : hourly_density.entrySet()) {
                String edge_key = hd_entry.getKey();
                int density_val = hd_entry.getValue();
                data[edges.indexOf(edge_key) + 1] = Integer.toString(density_val);
            }
            System.out.println(Arrays.toString(data));
            densities.add(data);
        }
        return densities;
    }

    private static void daily_densities(String density_dir,String output) {
//        String density_dir = "/home/essam/Dropbox/viterbi/results_dec-2015/daily_density";
//        String output = "/home/essam/Dropbox/viterbi/results_dec-2015/dd.csv";

        Density density = new Density();
        Hashtable<String, Hashtable<String, Integer>> acc_densities = new Hashtable<>();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(density_dir));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String file = iterator.next();
            String file_name = file.substring(file.lastIndexOf("/") + 1);
            if (file_name.matches(daily_density_regex)) {
                String day = file_name.substring(0, file_name.lastIndexOf("."));

                Hashtable<String, Integer> hourly_density = density.sumVehicles(Density.readDensity(file));
                acc_densities.put(day, hourly_density);

                append_edges(hourly_density);
            }
        }
        DataHandler.write_csv(convert_table_list(acc_densities), output);
    }

    private static void hourly_densities(String density_dir,String output) {
//        String density_dir = "/home/essam/Dropbox/viterbi/results_dec-2015/hd.1";
//        String output = "/home/essam/Dropbox/viterbi/results_dec-2015/hd.1.csv";

        Density density = new Density();

        Hashtable<String, Hashtable<String, Integer>> acc_densities = new Hashtable<>();

        ArrayList<String> dirs = ObsTripsBuilder.list_directories(new File(density_dir));

        for (Iterator<String> iterator = dirs.iterator(); iterator.hasNext();) {
            String dir = iterator.next();
            String dir_name = dir.substring(dir.lastIndexOf("/") + 1);
//            System.out.println(dir_name);
            ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dir));
            for (Iterator<String> density_files_it = files.iterator(); density_files_it.hasNext();) {
                String file = density_files_it.next();
                String file_name = file.substring(file.lastIndexOf("/") + 1);
                String hour_str = file_name.substring(file_name.indexOf("_") + 1, file_name.lastIndexOf("_"));
                if (file_name.matches(density_regex)) {
                    String date_str = dir_name + " " + hour_str;
                    System.out.println(date_str);
                    Hashtable<String, Integer> hourly_density = density.readAccumDensity(file);
                    acc_densities.put(date_str, hourly_density);

                    append_edges(hourly_density);
                }

            }

        }

        DataHandler.write_csv(convert_table_list(acc_densities), output);
    }

    public static void main(String[] args) {
    	String density_dir=args[0];
    	String output_file_path=args[1];
//        daily_densities(density_dir,output_file_path);
        hourly_densities(density_dir,output_file_path);
    }

}
