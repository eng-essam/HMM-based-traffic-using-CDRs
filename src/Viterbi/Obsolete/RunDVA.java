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
import java.util.stream.IntStream;

import Density.Density;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import utils.DataHandler;
import utils.FromNode;

class RunableDVA implements Runnable {

    private Thread t;
    private String threadName;
    Viterbi viterbi;
    int[][] densityMap;
    int obsCounter;
    String densityPath;
    ArrayList<String> exts;
    Hashtable<Integer, String> obsDayTable;
    String[] states;
    Hashtable<String, Double> start_p;
    Hashtable<String, Hashtable<String, Double>> trans_p;
    Hashtable<String, Hashtable<String, Double>> emit_p;
    Hashtable<String, StateInfoTimeStamp> si;
    ObsIndex oi;
    ArrayList<String> obs;

    public RunableDVA(String threadName, String densityPath, ArrayList<String> exts, Hashtable<Integer, String> obsDayTable, Hashtable<String, Double> start_p, Hashtable<String, Hashtable<String, Double>> trans_p, Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfoTimeStamp> si, ObsIndex oi) {
        this.threadName = threadName;
        this.densityPath = densityPath;
        this.exts = exts;
        this.obsDayTable = obsDayTable;
        this.start_p = start_p;
        this.trans_p = trans_p;
        this.emit_p = emit_p;
        this.si = si;
        this.oi = oi;
        this.obsCounter = 0;
        states = exts.toArray(new String[exts.size()]);
        densityMap = new int[exts.size()][exts.size()];
        obs = new ArrayList<>();
        viterbi = new Viterbi();
        final String rlm = "/";
        /**
         * initialize all sequences of observations
         */
        for (Map.Entry<Integer, String> entrySet : obsDayTable.entrySet()) {
            Integer key = entrySet.getKey();
            String seq = entrySet.getValue();
            if (seq.contains(rlm)) {
                String[] tmp = seq.split(rlm);
                for (int i = 0; i < tmp.length; i++) {
                    String tmp1 = tmp[i];
                    obs.add(tmp1);
                }
            } else {
                obs.add(seq);
            }

        }

    }

    @Override
    public void run() {
        for (Iterator<String> iterator = obs.iterator(); iterator.hasNext();) {
            String[] usrObs = iterator.next().split(",");
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
            if (!(vit_path == null && vit_path.isEmpty())) {
                updateDenisty(vit_path);
                obsCounter++;
            }
        }
        System.out.printf("Day %s has %d observations and can calculate only %d",
                threadName,
                obs.size(),
                obsCounter);
        writeDenty();

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
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
public class RunDVA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String vor = args[0];
        String probXml = args[1];
        String obsPath = args[2];
        String transPath = args[3];
        String emissionPath = args[4];
        String vorNeighborsPath = args[5];
        String mapRoutesPath = args[6];
        String image = args[7];
        String edgesPath = args[8];
        String towersPath = args[9];
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

        Hashtable<String, StateInfoTimeStamp> si_org = viterbi.trans_mat_to_list_indexed(states, adaptor.getStartProb(transition_probability), transition_probability);

        ObsIndex oi = new ObsIndex();
        oi.Initialise(states, emission_probability);

//        List<ObsIndex> oiList = new ArrayList<ObsIndex>();
        List<Hashtable<String, StateInfoTimeStamp>> siList = new ArrayList<Hashtable<String, StateInfoTimeStamp>>();

        for (int i = 0; i < obsTable.size(); i++) {
//            oiList.add(new ObsIndex(oi_org));
            siList.add(new Hashtable<String, StateInfoTimeStamp>(si_org));
        }

        for (Map.Entry<Integer, Hashtable<Integer, String>> entrySet : obsTable.entrySet()) {
            int dayKey = entrySet.getKey();
            Hashtable<Integer, String> obsDayTable = entrySet.getValue();

        }

    }

}
