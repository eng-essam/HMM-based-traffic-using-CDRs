/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import utils.DataHandler;

/**
 *
 * @author essam
 */
public class WriteTransCSV {

    public static double[] generate_init(int ntrans) {

        double[] init = new double[ntrans];
        double sum = 0;
        for (int i = 0; i < ntrans; i++) {
            double num = Math.random();
            sum += num;
            init[i] = num;
        }

        for (int i = 0; i < ntrans; i++) {
            init[i] = init[i] / sum;

        }
        return init;
    }

//    public static double[] generate_init(int ntrans) {
//
//        double[] init = new double[ntrans];
//        double sum = 0;
//        for (int i = 0; i < ntrans; i++) {
//            double num = Math.random();
//            sum += num;
//            init[i] = num;
//        }
//
//        for (int i = 0; i < ntrans; i++) {
//            init[i] = init[i] / sum;
//
//        }
//        return init;
//    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String probXml = "/home/essam/traffic/DI/Dakar-2.1/dakar.xy.dist.vor.xml";
        String transPath = "/home/essam/traffic/DI/Dakar-2.1/transition.xml";
        String emissionPath = "/home/essam/traffic/DI/Dakar-2.1/emission.xml";
//        String edgesPath = "/home/essam/traffic/Dakar-2/edges.interpolated.xml";

        String states_path = "/home/essam/traffic/DI/Dakar-2.1/dakar-2.states";
        String hmm_path = "/home/essam/traffic/DI/Dakar-2.1/dakar-2.hmm";

        int zones = 500;
        int nstates = -1;
//        double scale =1000;
        
        DataHandler adaptor = new DataHandler();
        adaptor.readNetworkDist(probXml);
        //read transitions
        Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
        // read emissions
        Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
        // read states
        ArrayList<String> exts = adaptor.getExts(trans_p);
        nstates = exts.size();
        System.out.println("Transition size: "+nstates);
        
        double[][] trans = adaptor.adaptTrans(trans_p, exts, nstates);
        double[][] emiss = adaptor.adaptEmission(emit_p, exts, nstates, zones);
        double[] init = generate_init(nstates);

//        if (Double.isNaN(trans[2305][532])) {
//            System.out.println(trans[2305][532]);
//        } else {
//            System.out.println("nooooo");
//        }
//
        write_exts(exts, nstates, states_path);
        write_hmm(trans, emiss, init, nstates, zones, hmm_path);

//        write_probs(trans_p, exts, transPath + ".csv");
//        write_probs(emiss, emissionPath + ".csv");
    }

    public static void write_exts(ArrayList<String> exts, int nstates, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));

            writer.write("nStates");
            writer.newLine();
            writer.write(Integer.toString(exts.size()));
            writer.newLine();

            writer.write("states");
//            writer.newLine();

            for (int i = 0; i < nstates; i++) {
                if (i < exts.size()) {
                    String ext_point = exts.get(i);
                    writer.newLine();
                    writer.write(ext_point);
                } else {
                    String ext_point = "-";
                    writer.newLine();
                    writer.write(ext_point);
                }
            }
//            for (Iterator<String> it = exts.iterator(); it.hasNext();) {
//                String ext_point = it.next();
//                writer.newLine();
//                writer.write(ext_point);
////                if (it.hasNext()) {
////                    writer.write(',');
////                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public static void write_hmm(double[][] trans, double[][] emiss, double[] init, int nstates, int nobs, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));

            writer.write("nStates\n");
            writer.write(Integer.toString(nstates));
            writer.newLine();

            writer.write("nObservables\n");
            writer.write(Integer.toString(nobs));
            writer.newLine();

            //write init
            writer.write("initProbs");
            writer.newLine();
            for (int i = 0; i < nstates; i++) {
                double j = init[i];
                writer.write(String.format("%2.4e", j));
                writer.newLine();
            }

            //write transitions
            writer.write("transProbs");
            writer.newLine();
            for (int i = 0; i < nstates; i++) {
                double[] ds = trans[i];
                int k = 0;
                for (int j = 0; j < nstates; j++) {
                    double d = ds[j];
                    if (Double.isNaN(d)) {
                        d = 0;
                    }
                    if (d == 0) {
                        writer.write(Double.toString(d));
                    } else {
                        writer.write(String.format("%2.4e", d));
                    }

                    k = j;
                    if (k++ < ds.length) {
                        writer.write(' ');
                    }
                }
                writer.newLine();
            }

            //write emission
            writer.write("emProbs");
            writer.newLine();
            for (int i = 0; i < nstates; i++) {
                double[] ds = emiss[i];
                int k = 0;
                for (int j = 0; j < nobs; j++) {
                    double d = ds[j];
                    if (Double.isNaN(d)) {
                        d = 0;
                    }
                    if (d == 0) {
                        writer.write(Double.toString(d));
                    } else {
                        writer.write(String.format("%2.4e", d));
                    }
                    k = j;
                    if (k++ < ds.length) {
                        writer.write(' ');
                    }
                }
                writer.newLine();
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

    public static void write_probs(double[][] prob, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));

            for (int i = 0; i < prob.length; i++) {
                double[] ds = prob[i];
                int k = 0;
                for (int j = 0; j < ds.length; j++) {
                    double d = ds[j];
                    writer.write(Double.toString(d));
                    k = j;
                    if (k++ < ds.length) {
                        writer.write(',');
                    }
                }
                //add new line
                writer.newLine();
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

    public static void write_probs(Hashtable<String, Hashtable<String, Double>> prob, ArrayList<String> exts, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));
            for (Iterator<String> it = exts.iterator(); it.hasNext();) {
                String fpnt = it.next();

                Hashtable<String, Double> tos = new Hashtable<>();
                if (prob.containsKey(fpnt)) {
                    tos = prob.get(fpnt);
                }
                for (Iterator<String> to_it = exts.iterator(); to_it.hasNext();) {
                    String tpnt = to_it.next();
                    double p = 0;
                    if (tos.containsKey(tpnt)) {
                        p = tos.get(tpnt);
                    }
                    //write
                    writer.write(Double.toString(p));
                    if (it.hasNext()) {
                        writer.write(',');
                    }
                }
                //add new line
                writer.newLine();
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
