/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import Observations.ObsTripsBuilder;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class UpdateTrans3 {

    /**
     * Get the mean square error between the old transitions probabilities and
     * new updated transitions.
     *
     * This distance is equivalent to the euclidian distance between two
     * matrices.
     *
     * @param transitions
     * @param update
     * @return
     */
    public static double getError(Hashtable<String, Hashtable<String, Double>> transitions,
            Hashtable<String, Hashtable<String, Double>> update) {
        double sum = 0.0;
//        NumberFormat formatter = new DecimalFormat("0.#######E0");
        for (Map.Entry<String, Hashtable<String, Double>> entrySet : transitions.entrySet()) {
            String fromKey = entrySet.getKey();
            Hashtable<String, Double> tosTrans = entrySet.getValue();
            Hashtable<String, Double> uTosTrans = update.get(fromKey);
            
            for (Map.Entry<String, Double> tosEntry : tosTrans.entrySet()) {
                
                String toKey = tosEntry.getKey();
                double prob = tosEntry.getValue().doubleValue();
                double uprob = uTosTrans.get(toKey).doubleValue();
                double diff = Math.abs(prob - uprob);
                if (Double.isNaN(diff)) {
                    continue;
                }
                sum += diff * diff;

            }
        }

        return Math.sqrt(sum);
//        return sum;
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String denisty_dir = "/home/essam/traffic/filtered_trips/updates/alpha-0.8_beta-.2";
        DataHandler adaptor = new DataHandler();
        int index = 1;
        double scale = 1e3;
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(denisty_dir));
        
        for (int i = 0; i < files.size() - 1; i++) {
            String path = denisty_dir + "/transition.day." + String.format("%02d", i) + ".xml";
            String upath = denisty_dir + "/transition.day." + String.format("%02d", i + 1) + ".xml";
            Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(path);
            Hashtable<String, Hashtable<String, Double>> utrans_p = adaptor.readProbXMLTable(upath);

//            System.out.println(path+"\t"+ upath+"\t" + TransBuilder.getError(trans_p, utrans_p));
            System.out.println(i+"\t"+getError(trans_p, utrans_p));
//            break;
        }
        
    }
    
}
