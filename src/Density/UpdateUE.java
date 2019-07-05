/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.util.Hashtable;

import Viterbi.TransBuilder;
import utils.DataHandler;
import validation.RoutesDensity;
import validation.Vit2ODs;

/**
 *
 * @author essam
 */
public class UpdateUE {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String routes_path = "/home/essam/traffic/SET2/routes/2013-01-07_Monday.trips.rou.xml";
//        String trans_path = "/home/essam/traffic/SET2/updates/transition.day.00.xml";
//        String density_dir = "/home/essam/traffic/SET2/updates";
//        double alpha = 0.2;
//        double beta = 0.8;
//        int index = 1;

        String routes_path = args[0];
        String trans_path = args[1];
        String density_dir = args[2];
        double alpha = Double.parseDouble(args[3]);
        double beta = Double.parseDouble(args[4]);
        int index = Integer.parseInt(args[5]);

        DataHandler adaptor = new DataHandler();
        Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(trans_path);

        /**
         * if this day is a weekend, do not use in the transitions updates
         */
//        String file = routes_path.substring(routes_path.lastIndexOf("/") + 1);
//        if (file.contains("Saturday") || file.contains("Sunday")) {
//            System.out.println("#-#\t" + file);
//            return;
//        } else {
//            System.out.println("#+#\t" + file);
//
//        }
        Vit2ODs vod = new Vit2ODs();;

        RoutesDensity dh = new RoutesDensity();
        Hashtable<String, Hashtable<String, Double>> density = dh.getRoutesDensity(vod.readRoutes(routes_path));

        Hashtable<String, Hashtable<String, Double>> transitions = TransBuilder.updateTrans(trans_p, density, alpha, beta);

//            if (trans_p.equals(transitions)) {
//                System.out.println("They are equals");
//            }
        /**
         * Save files
         */
        String upath = density_dir + "/transition.day." + String.format("%02d", index) + ".xml";
        String densityPath = density_dir + "/density.day." + String.format("%02d", index) + ".xml";

        Density.writeDensity(density, densityPath);
        adaptor.writeXmlTable(transitions, upath);

        /**
         * Get difference between current transitions probability and the
         * updated one.
         *
         * Write then read as when i compare the transitions tables after
         * calculations I found that both the updated version and the old one
         * are identical to each other.
         */
        String path = density_dir + "/transition.day." + String.format("%02d", index - 1) + ".xml";
        Hashtable<String, Hashtable<String, Double>> trans_prob = adaptor.readProbXMLTable(path);
        Hashtable<String, Hashtable<String, Double>> utrans_prob = adaptor.readProbXMLTable(upath);

//            System.out.println(path+"\t"+ upath+"\t" + TransBuilder.getError(trans_p, utrans_p));
        System.out.println(TransBuilder.getError(trans_prob, utrans_prob));

        /**
         * Update the old transitions with the new transitions.
         */
        trans_p = transitions;
        index++;
    }

}
