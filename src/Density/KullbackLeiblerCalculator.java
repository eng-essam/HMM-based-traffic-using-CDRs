/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Observations.ObsTripsBuilder;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class KullbackLeiblerCalculator {

    static Hmm buildHmm(double[][] trans, double[][] emission) {

        double[] pi = new double[trans.length];
        for (int i = 0; i < pi.length; i++) {
            pi[i] = 1.0 / (trans.length);

        }
        List<OpdfInteger> opdfs = new ArrayList<>();

        for (int i = 0; i < trans.length; i++) {
            opdfs.add(i, new OpdfInteger(emission[i]));
        }

        for (int i = 0; i < trans.length; i++) {
            if (Double.isNaN(trans[i][i])) {
                trans[i][i] = getSmallest(trans[i]);
            }
//            System.out.printf("trans[%d][%d]\t%f\n", i, i, trans[i][i]);
        }
        return new Hmm(pi, trans, opdfs);
    }

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

    public static double getSmallest(double[] arr) {
        double smallest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < arr.length; i++) {
            double b = arr[i];
            if (b < smallest) {
                smallest = b;
            }
        }
        return smallest;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String updates_dir = args[0];
        String emissionPath = args[1];

        DataHandler adaptor = new DataHandler();
        int index = 1;
        double scale = 1e3;
        int towers = 501;

        Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
        ArrayList<String> exts = new ArrayList<>(emit_p.keySet());

        double[][] emiss = adaptor.adaptEmission(emit_p, exts,  exts.size(),towers);

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(updates_dir));

        String path = updates_dir + "/transition.day.00.xml";
        double[][] trans = adaptor.adaptTrans(adaptor.readProbXMLTable(path), exts, exts.size());
        Hmm hmm = buildHmm(trans, emiss);
        
        KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();
        
        for (int i = 1; i < files.size() - 1; i++) {

            String upath = updates_dir + "/transition.day." + String.format("%02d", i + 1) + ".xml";

            double[][] utrans = adaptor.adaptTrans(adaptor.readProbXMLTable(upath), exts, exts.size());
            Hmm uhmm = buildHmm(utrans, emiss);
            
            double dist = klc.distance(hmm, uhmm);
            System.out.printf("KLC %d - %d updates: %f\n",i-1, i, dist);
            
            hmm = uhmm;
            trans = utrans;
        }

    }

}
