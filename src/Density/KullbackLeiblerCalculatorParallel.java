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

import Observations.ObsTripsBuilder;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class KullbackLeiblerCalculatorParallel {

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

        ArrayList<String> files = adaptor.filter(ObsTripsBuilder.list_dir_files(new File(updates_dir)), "transition.day.");
        

        for (int i = 0; i < files.size() - 1; i++) {
            String path = updates_dir + "/transition.day." + String.format("%02d", i) + ".xml";
            String upath = updates_dir + "/transition.day." + String.format("%02d", i + 1) + ".xml";
            RunnableKLC klc = new RunnableKLC(i+1, path, upath, emiss, exts);
            klc.start();
        }

    }

}

class RunnableKLC implements Runnable {

    Thread t;
    Hmm hmm, uhmm;
    int index;

    public RunnableKLC(int iter, String trans_path, String utrans_path, double[][] emission, ArrayList<String> exts) {
        DataHandler adaptor = new DataHandler();
        double[][] trans = adaptor.adaptTrans(adaptor.readProbXMLTable(trans_path), exts, exts.size());
        hmm = buildHmm(trans, emission);

        double[][] utrans = adaptor.adaptTrans(adaptor.readProbXMLTable(utrans_path), exts, exts.size());
        uhmm = buildHmm(utrans, emission);
        this.index = iter;
    }

    public Hmm buildHmm(double[][] trans, double[][] emission) {

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

    public double getSmallest(double[] arr) {
        double smallest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < arr.length; i++) {
            double b = arr[i];
            if (b < smallest) {
                smallest = b;
            }
        }
        return smallest;
    }

    @Override
    public void run() {
        KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();
        double dist = klc.distance(hmm, uhmm);
        System.out.printf("KLC %d updates: %f\n", index, dist);
    }

    public void start() {
        System.out.println("Starting " + index);
        if (t == null) {
            t = new Thread(this, Integer.toString(index));
            t.start();
        }
    }
}
