/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import Observations.ObsTripsBuilder;

/**
 *
 * @author essam
 */
public class AccumDensity {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String transPath = args[0];
        String denistyDir = args[0];
//                String denistyDir ="/home/essam/home/algizawy/D4D/SET2/trips";
//        String nTransPath = args[2];
//        AdaptData adaptor = new AdaptData();
//        Hashtable<String, Hashtable<String, Double>> transition
//                = adaptor.readProbXMLTable(transPath);
//        ObsTripsBuilder builder = new ObsTripsBuilder();
        Density densityHandler = new Density();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(denistyDir));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".") || !fileName.contains("density")) {
                continue;
            }
            Hashtable<String, Hashtable<String, Double>> density = Density.readDensity(subsetPath);
            String accPath = subsetPath.substring(0, subsetPath.lastIndexOf(".")) + ".accumulated.vehicles.xml";
            densityHandler.writeAccumDensity(densityHandler.sumIEVehicles(density), accPath);
//            transition = TransBuilder.updateTrans(transition, density);
        }
    }

}
