/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

import Density.Density;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import utils.DataHandler;
import utils.FromNode;

//class RecursiveDVAT extends RecursiveTask<int[][]> {
class RecursiveDVAT extends RecursiveAction {

    int dayKey;
    Viterbi viterbi;
    int[][] densityMap;
//    int obsCounter;
    String densityPath;
    ArrayList<String> exts;
    Hashtable<Integer, Hashtable<Integer, String>> obsTable;
    String[] states;
    Hashtable<String, Hashtable<String, Double>> emit_p;
    Hashtable<String, StateInfoTimeStamp> si;
    ObsIndex oi;
    ArrayList<String> obs;
    Hashtable<String, Hashtable<String, Double>> trans_p;
    Hashtable<String, Double> start;

    public RecursiveDVAT(int dayKey,
            String densityPath,
            ArrayList<String> exts,
            Hashtable<Integer, Hashtable<Integer, String>> obsTable,
            Hashtable<String, Hashtable<String, Double>> emit_p,
            Hashtable<String, Hashtable<String, Double>> trans_p,
            Hashtable<String, Double> start) {
        this.dayKey = dayKey;
        this.densityPath = densityPath;
        this.exts = exts;
        this.obsTable = obsTable;
        this.emit_p = emit_p;
        this.start = start;
//        this.si = si;
//        this.oi = oi;
//        this.obsCounter = 0;
        this.trans_p = trans_p;
        obs = new ArrayList<>();

    }

    public void calcDayDensity() {

        System.out.println("Day \t" + dayKey);
        int obsCounter = 0;
        states = exts.toArray(new String[exts.size()]);
        densityMap = new int[exts.size()][exts.size()];
        viterbi = new Viterbi();

        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

        ObsIndex oi = new ObsIndex();
        oi.Initialise(states, emit_p);

        for (Iterator<String> iterator = obs.iterator(); iterator.hasNext();) {
            String[] usrObs = iterator.next().split(",");
//            if (usrObs.length <= 1) {
//                continue;
//            }
            Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs,
                    states,
                    emit_p,
                    si,
                    oi);
            String vit_path = (String) ret[1];
            /**
             * If viterbi calc path, update the density map and increment paths
             * counter.
             */
            if (!(vit_path == null || vit_path.isEmpty())) {
                updateDenisty(vit_path);
                obsCounter++;
            }
        }
        System.out.printf("Day %s has %d observations and can calculate only %d",
                dayKey,
                obs.size(),
                obsCounter);
        writeDenty();

    }

    //    @Override
//    protected int[][] compute() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    protected void compute() {
//        System.out.println("compute");
        if (dayKey > 0) {
//            System.out.println("day key > 0" + dayKey);
            initializeObs();
            calcDayDensity();

        } else {
//            System.out.println("day key < 0 \t" + dayKey);
            List<RecursiveDVAT> jobs = new ArrayList<RecursiveDVAT>();
//            System.out.println(obsTable.size());
            for (Map.Entry<Integer, Hashtable<Integer, String>> entrySet : obsTable.entrySet()) {
                Integer key = entrySet.getKey();
                Hashtable<Integer, String> value = entrySet.getValue();
                String path = densityPath.substring(0, densityPath.lastIndexOf(".")) + ".denisty.day." + key + ".xml";
                System.out.println(path);
//                RecursiveDVAT tmp = new RecursiveDVAT(key, path, exts, obsTable, emit_p, si, oi);
//                tmp.compute();
//                tmp.fork();
                jobs.add(new RecursiveDVAT(key, path, exts, obsTable, emit_p, trans_p, start));

            }
            invokeAll(jobs);
        }
//        return densityMap;
    }

    public void initializeObs() {
        final String rlm = "/";

        /**
         * initialize all sequences of observations
         */
        Hashtable<Integer, String> obsDayTable = obsTable.get(dayKey);
        for (Map.Entry<Integer, String> entrySet : obsDayTable.entrySet()) {
            Integer key = entrySet.getKey();
            String seq = entrySet.getValue();
            if (seq.contains(rlm)) {
                String[] tmp = seq.split(rlm);
                for (int i = 0; i < tmp.length; i++) {
                    String tmp1 = tmp[i];
                    if (!tmp1.isEmpty() && tmp1.contains(",")) {
                        obs.add(tmp1);
                    }

                }
            } else {
                if (!seq.isEmpty() && seq.contains(",")) {
                    obs.add(seq);
                }
            }

        }

    }

    public void updateDenisty(String vit_path) {
        String[] out = vit_path.split(",");
        for (int k = 0; k < out.length - 1; k++) {
            if (exts.contains(out[k]) && exts.contains(out[k + 1])) {
                densityMap[exts.indexOf(out[k])][exts.indexOf(out[k + 1])] += 1;
            }

        }
    }

private void writeDenty() {

        Hashtable<String, Hashtable<String, Double>> denseTable = new Hashtable<>();
        for (int i = 0; i < densityMap.length; i++) {
            int[] dens = densityMap[i];
            int sum = IntStream.of(dens).sum();
            if (sum > 0) {
                Hashtable<String, Double> toTab = new Hashtable<>();
                for (int j = 0; j < dens.length; j++) {
                    int den = dens[j];
                    toTab.put(exts.get(j), (double) den);

                }
                denseTable.put(exts.get(i), toTab);
            }

        }
        System.out.println("Writing Density Map");
        Density.writeDensity(denseTable, densityPath);
    }

}

/**
 *
 * @author essam
 */
public class RunRDVA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String probXml = args[1];
        String obsPath = args[2];
        String transPath = args[3];
        String vorNeighborsPath = args[5];
        String densityPath = args[10];

        int threshold = 2000;

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getTransitionProb(threshold, exts, towersPath);
//     
        Hashtable<String, Hashtable<String, Double>> transition_probability
                = adaptor.readProbXMLTable(transPath);
        String[] states = exts.toArray(new String[exts.size()]);

        Hashtable<String, Hashtable<String, Double>> emission_probability
                = tbuilder.getEmissionProb(exts);
        emission_probability = tbuilder.emitNeighbors(emission_probability);

        Viterbi viterbi = new Viterbi();
        int[][] densityMap = new int[exts.size()][exts.size()];
        Hashtable<Integer, Hashtable<Integer, String>> obsTable = adaptor.readObsTU(obsPath);
        System.out.println("observation table" + obsTable.size());
        Hashtable<String, Double> start = adaptor.getStartProb(transition_probability);
//        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, transition_probability);
//
//        ObsIndex oi = new ObsIndex();
//        oi.Initialise(states, emission_probability);

//        List<ObsIndex> oiList = new ArrayList<ObsIndex>();
        RecursiveDVAT rdva = new RecursiveDVAT(-1, densityPath, exts, obsTable, emission_probability, transition_probability, start);
//        rdva.compute();
//        rdva.invoke();
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rdva);

    }

}
