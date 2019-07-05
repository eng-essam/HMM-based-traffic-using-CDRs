/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import AViterbi.Interpolate.Interpolation;
import Observations.Obs;
import Observations.ObsTripsBuilder;
import Voronoi.VoronoiConverter;
import utils.DataHandler;
import utils.Region;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class ManageObs {

    /**
     * Delimeters used in the data representation.
     */
    final static String RLM = "/";
    final static String CLM = ",";

    private static Hashtable<String, List<String>> adaptObs(Hashtable<String, Hashtable<Integer, Obs>> obs) {
        Hashtable<String, List<String>> adapted_obs = new Hashtable<>();
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<Integer, Obs> value = entrySet.getValue();
            List<String> obs_list = new ArrayList<>();
            for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
                Integer key1 = entrySet1.getKey();
                if (entrySet1.getValue().getSeq().split(CLM).length > 4) {
                    String seq = entrySet1.getValue().getSeq().replace(CLM, " ");
                    obs_list.add(seq);
                }

            }
            adapted_obs.put(key, obs_list);
        }
        return adapted_obs;
    }

    public static Hashtable<String, Hashtable<Integer, Obs>> interpolate(Hashtable<String, Hashtable<Integer, Obs>> obs, Interpolation interpolator) {
        Hashtable<String, Hashtable<Integer, Obs>> uobs = new Hashtable<>();

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs.entrySet()) {
            String daykey = entrySet.getKey();
            Hashtable<Integer, Obs> usrs_tab = entrySet.getValue();
            for (Map.Entry<Integer, Obs> entrySet1 : usrs_tab.entrySet()) {
                Integer usrkey = entrySet1.getKey();
                Obs obsval = entrySet1.getValue();
                String seq[] = obsval.getSeq().replace(RLM, CLM).split(CLM);
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < seq.length; i++) {
                    list.add(Integer.parseInt(seq[i]));
                }
                if (list.size() < 3) {
                    continue;
                }
                List<Integer> nlist = interpolator.linear_interpolator(list);
                if (nlist.size() < 3) {
                    continue;
                }
                String r_seq = nlist.get(0).toString();
                for (int i = 1; i < nlist.size(); i++) {
                    r_seq += CLM + nlist.get(i);
                }
//                System.out.println(usrkey +" "+ r_seq);
                obsval.setSeq(r_seq);
                usrs_tab.replace(usrkey, obsval);
            }
            uobs.put(daykey, usrs_tab);

        }
        return uobs;

    }

    public static void main(String[] args) {
//        String dSPath = args[0];
//        String towerPath = args[1];

        final int stower = 0; // start tower
        final int etower = 300; // end tower
        final boolean repeated_obs_flag = false; // include repeated observation flag.

        String dSPath = "/home/essam/traffic/SET2/tmp";
        String regions_path = "/home/essam/traffic/DI/Dakar-2.1/dakar.vor.csv";
        String neighbors_path = "/home/essam/traffic/DI/Dakar-2.1/dakar.vor.neighborSitesForSite.csv";
        String towers_path = "/home/essam/traffic/DI/Dakar-2.1/towers.csv";

        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoi_regions = converter.readVoronoi(regions_path);
        Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = converter.readVorNeighbors(neighbors_path);
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);

        Interpolation interpolator = new Interpolation(voronoi_neighbors, towers);

        ObsTripsBuilder builder = new ObsTripsBuilder();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dSPath));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {

                String dist = subsetPath + "." + stower + "_" + etower + ".xml";

                Hashtable<String, Hashtable<Integer, Obs>> obs_table;
                if (repeated_obs_flag) {
                    obs_table = builder.transposeWUT(builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stower, etower)));
                } else {
                    obs_table = builder.transposeWUT(builder.remove_repeated(builder.remove_handovers(builder.buildObsDUT_stops(subsetPath, stower, etower))));
                }

                obs_table = interpolate(obs_table, interpolator);

                writeObs(obs_table, dSPath + "/hmm");
                ObsTripsBuilder.writeObsDUT(obs_table, dist);

            }
        }
    }

    public static void writeObs(Hashtable<String, Hashtable<Integer, Obs>> obs, String path) {
        Hashtable<String, List<String>> adapted_obs = adaptObs(obs);

        BufferedWriter writer = null;
        try {

            for (Map.Entry<String, List<String>> entrySet : adapted_obs.entrySet()) {
                String daykey = entrySet.getKey();

                File logFile = new File(path + "/" + daykey + ".csv");
                writer = new BufferedWriter(new FileWriter(logFile));

                List<String> obs_list = entrySet.getValue();

                writer.write("nSequences\n");
                writer.write(Integer.toString(obs_list.size()));
                writer.newLine();

                writer.write("sequences\n");

                for (Iterator<String> iterator = obs_list.iterator(); iterator.hasNext();) {
                    String next = iterator.next();
                    writer.write(next);
                    writer.newLine();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

}
