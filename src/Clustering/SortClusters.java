/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author essam
 */
public class SortClusters {

    final static String CLM = ",";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String raw_path = "/home/essam/traffic/SET2/Results/all.txt.membership";
        String cluster_path = "/home/essam/traffic/SET2/Results/all.txt.membership.clusters";
        int no_cluster = 512;
        writeClusters(cluster_path, readClusters(raw_path, no_cluster));
    }

    public static Hashtable<Integer, ArrayList<Integer>> readClusters(String path, int no_cluster) {
        Hashtable<Integer, ArrayList<Integer>> clusters = new Hashtable<>();

        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                String[] records = line.split(" ");
                if (clusters.containsKey(Integer.parseInt(records[1]))) {
                    ArrayList<Integer> usrs = clusters.get(Integer.parseInt(records[1]));
                    usrs.add(Integer.parseInt(records[0]));
                    clusters.replace(Integer.parseInt(records[1]), usrs);
                } else {
                    ArrayList<Integer> usrs = new ArrayList<>();
                    usrs.add(Integer.parseInt(records[0]));
                    clusters.put(Integer.parseInt(records[1]), usrs);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done");
        return clusters;

    }

    public static void writeClusters(String path, Hashtable<Integer, ArrayList<Integer>> clusters) {
        try {
            File file = new File(path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Map.Entry<Integer, ArrayList<Integer>> entrySet : clusters.entrySet()) {
                Integer key = entrySet.getKey();
                bw.append(Integer.toString(key));
                ArrayList<Integer> usrs = entrySet.getValue();
                for (Iterator<Integer> iterator = usrs.iterator(); iterator.hasNext();) {
                    Integer usr = iterator.next();
                    bw.append(CLM + Integer.toString(usr));
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
