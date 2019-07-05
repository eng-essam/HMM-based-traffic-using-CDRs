/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class ManageObsStops {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        int stower = Integer.parseInt(args[0]);
//        int etower = Integer.parseInt(args[1]);
//        int dist_th = Integer.parseInt(args[2]);
//        int time_th = Integer.parseInt(args[3]);
//        String dSPath = args[4];
//        String towerPath = args[5];

        int stower = 0;
        int etower = 500;
        int dist_th = 1000;
        int time_th = 60;
        String dSPath = "/home/essam/traffic/SET2/";
        String towerPath = "/home/essam/traffic/Dakar/N_Darak/Dakar.vor.towers.csv";

        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towerPath);

        ObsStopsBuilder builder = new ObsStopsBuilder();

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(dSPath));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
//                 subsetPath = subsetPath.replace("CSV", "_");
                String dist = subsetPath + "." + stower + "_" + etower + "_th-dist_" + dist_th + "_th-time_" + time_th + ".xml";
                /**
                 * Fix error here before running the code .....
                 */
                
//                builder.writeObsDUT(
//                        builder.transposeWUT(
//                                builder.adaptiveTripStops(
//                                        builder.buildObsDUT(subsetPath, stower, etower),
//                                        towers, dist_th, time_th)), dist);

//                builder.writeObsTU(builder.transposeWU(builder.buildObsTU(subsetPath, stower, etower)), dist);
//                builder.writeObsTU(builder.buildObsTU(subsetPath, stower, etower), dist);
//                ObsTripsBuilder.buildObs_Time(subsetPath, subsetPath + "." + stower + "_" + etower + ".xml", stower, etower);
            }
//            break;
        }
    }

}
