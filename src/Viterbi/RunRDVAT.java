/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

import Density.Density;
import Observations.Obs;
import utils.DataHandler;

class RecursiveDVA extends RecursiveAction {

    String dayKey;
    Viterbi viterbi;
    int[][] densityMap;
//    int obsCounter;
    String densityPath;
    ArrayList<String> exts;
    Hashtable<String, Hashtable<Integer, Obs>> obsTable;
    String[] states;
    Hashtable<String, Hashtable<String, Double>> emit_p;
    Hashtable<String, StateInfoTimeStamp> si;
    ObsIndex oi;
    ArrayList<String> obs;
    Hashtable<String, Hashtable<String, Double>> trans_p;
    Hashtable<String, Double> start;
    final String RLM = "/";

    public RecursiveDVA(String dayKey,
            String densityPath,
            ArrayList<String> exts,
            Hashtable<String, Hashtable<Integer, Obs>> obsTable,
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

        /**
         * initialize all sequences of observations
         */
        Hashtable<Integer, Obs> obsDayTable = obsTable.get(dayKey);

       long stime = System.currentTimeMillis();
        for (Map.Entry<Integer, Obs> entrySet : obsDayTable.entrySet()) {
            Integer key = entrySet.getKey();
            Obs obseq = entrySet.getValue();
            String seq = obseq.getSeq();
            String vitPath = computeVit(seq, si, oi);
            if (!vitPath.isEmpty()) {
                obsCounter++;
            }
            obseq.setVitPath(vitPath);
            obsDayTable.replace(key, obseq);
        }
        long etime = System.currentTimeMillis();
        Hashtable<String, Hashtable<Integer, Obs>> tmp = new Hashtable<>();
        tmp.put(dayKey, obsDayTable);

        String path = densityPath.replace(".density.day.", ".viterbi.day.");

        Observations.ObsTripsBuilder.writeObsDUT(tmp, path);
        System.out.println(">>> \t" + path);
        System.out.printf("Day %s has ->>> %d\t Viterbi paths out of %d\t obs in %d Millis\n",
                dayKey,
                obsCounter,
                obsDayTable.size(),(etime-stime));
        writeDenty();

    }

    //    @Override
//    protected int[][] compute() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    protected void compute() {
//        System.out.println("compute");
        if (dayKey != null) {
//            System.out.println("day key > 0" + dayKey);
            calcDayDensity();

        } else {
//            System.out.println("day key < 0 \t" + dayKey);
            List<RecursiveDVA> jobs = new ArrayList<RecursiveDVA>();
//            System.out.println(obsTable.size());
            for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
                String key = entrySet.getKey();
                String path = densityPath.substring(0, densityPath.lastIndexOf(".")) + ".density.day." + key+ ".xml";
                System.out.println(path);
//                RecursiveDVAT tmp = new RecursiveDVAT(key, path, exts, obsTable, emit_p, si, oi);
//                tmp.compute();
//                tmp.fork();
                jobs.add(new RecursiveDVA(key, path, exts, obsTable, emit_p, trans_p, start));

            }
            invokeAll(jobs);
        }
//        return densityMap;
    }

    public String computeVit(String seq, Hashtable<String, StateInfoTimeStamp> si, ObsIndex oi) {
        String complete_vit = "";
        if (seq.contains(RLM)) {
            String[] tmp = seq.split(RLM);
            for (int i = 0; i < tmp.length; i++) {
                String[] usrObs = tmp[i].split(",");
                if (usrObs.length > 1) {
                    Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs,
                            states,
                            emit_p,
                            si,
                            oi);
                    String vit_out = (String) ret[1];

                    if (vit_out == null || vit_out.isEmpty()) {
                        vit_out = "-";
                    }
                    /**
                     * If the Viterbi path is not empty add observations
                     * seperator.
                     */
                    if (!complete_vit.isEmpty()) {
                        complete_vit += RLM;
                    }

//                    else {
//                        vitPath += "++" + RLM;
//                    }
                    complete_vit += vit_out;
                    /**
                     * If viterbi calc path, update the density map and
                     * increment paths counter.
                     */
                    if (!vit_out.isEmpty()) {
                        updateDenisty(vit_out);

                    }
                } else {
                    if (!complete_vit.isEmpty()) {
                        complete_vit += RLM;
                    }
                    complete_vit += "-";
                }

            }
        } else {
            String[] usrObs = seq.split(",");
            if (usrObs.length > 1) {
                Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs,
                        states,
                        emit_p,
                        si,
                        oi);
                String vit_out = (String) ret[1];

                if (vit_out == null || vit_out.isEmpty()) {
                    vit_out = "-";
                }
                /**
                 * If the Viterbi path is not empty add observations seperator.
                 */
                if (!complete_vit.isEmpty()) {
                    complete_vit += RLM;
                }
                complete_vit += vit_out;
                /**
                 * If viterbi calc path, update the density map and increment
                 * paths counter.
                 */
                if (!vit_out.isEmpty()) {
                    updateDenisty(vit_out);

                }
            } else {
                if (!complete_vit.isEmpty()) {
                    complete_vit += RLM;
                }
                complete_vit += "-";
            }
        }
        return complete_vit;
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
public class RunRDVAT {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String probXml = args[1];
        String obsPath = args[0];
        String transPath = args[1];
        String emissionPath = args[2];
        String densityPath = args[3];

//        int threshold = 1000;

        DataHandler adaptor = new DataHandler();
//        ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
//        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getTransitionProb(threshold, exts, towersPath);
//        System.out.println("Writing transitions");
//        adaptor.writeXmlTable(transition_probability, transPath);

        Hashtable<String, Hashtable<String, Double>> transition_probability
                = adaptor.readProbXMLTable(transPath);
        
//        tbuilder.writeZonesTrans(transition_probability, transPath.substring(0, transPath.lastIndexOf(".")) + ".zones.xml");
        String[] states = exts.toArray(new String[exts.size()]);

//        Hashtable<String, Hashtable<String, Double>> emission_probability
//                = tbuilder.getEmissionProb(exts);
//        emission_probability = tbuilder.emitNeighbors(emission_probability);
//        System.out.println("Writing emissions");
//        adaptor.writeXmlTable(emission_probability, emissionPath);
        Hashtable<String, Hashtable<String, Double>> emission_probability = adaptor.readProbXMLTable(emissionPath);
//        Viterbi viterbi = new Viterbi();
//        int[][] densityMap = new int[exts.size()][exts.size()];
        Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obsPath);
        System.out.println("observation table" + obsTable.size());
        Hashtable<String, Double> start = adaptor.getStartProb(transition_probability);
//        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, transition_probability);
//
//        ObsIndex oi = new ObsIndex();
//        oi.Initialise(states, emission_probability);

////        List<ObsIndex> oiList = new ArrayList<ObsIndex>();
        RecursiveDVA rdva = new RecursiveDVA(null, densityPath, exts, obsTable, emission_probability, transition_probability, start);
//        rdva.compute();
//        rdva.invoke();
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rdva);

    }

}
