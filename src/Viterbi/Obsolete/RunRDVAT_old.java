/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

import Density.Density;
import Observations.Obs;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import utils.DataHandler;
import utils.FromNode;

//class RecursiveDVAT extends RecursiveTask<int[][]> {
class RecursiveDVA_old extends RecursiveAction {

    String dayKey;
    Viterbi viterbi;
    int[][] densityMap;
//    int obsCounter;
    String densityPath;
    ArrayList<String> exts;
    Hashtable<String, Hashtable<Integer, Obs>> obsTable;
    String[] states;
    Hashtable<String, Hashtable<String, Double>> emit_p;
    ArrayList<String> obs;
    Hashtable<String, Hashtable<String, Double>> trans_p;
    Hashtable<String, Double> start;
    final String RLM = "/";

    public RecursiveDVA_old(String dayKey,
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

        /**
         * initialize all sequences of observations
         */
        Hashtable<Integer, Obs> obsDayTable = obsTable.get(dayKey);

        long stime = System.currentTimeMillis();
        for (Map.Entry<Integer, Obs> entrySet : obsDayTable.entrySet()) {
            Integer key = entrySet.getKey();
            Obs obseq = entrySet.getValue();
            String seq = obseq.getSeq();
            String vitPath = computeVit(seq);
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
                obsDayTable.size(), (etime - stime));
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
            List<RecursiveDVA_old> jobs = new ArrayList<RecursiveDVA_old>();
//            System.out.println(obsTable.size());
            for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
                String key = entrySet.getKey();
                String path = densityPath.substring(0, densityPath.lastIndexOf(".")) + ".density.day." + key + ".xml";
                System.out.println(path);
//                RecursiveDVAT tmp = new RecursiveDVAT(key, path, exts, obsTable, emit_p, si, oi);
//                tmp.compute();
//                tmp.fork();
                jobs.add(new RecursiveDVA_old(key, path, exts, obsTable, emit_p, trans_p, start));

            }
            invokeAll(jobs);
        }
//        return densityMap;
    }

    public String computeVit(String seq) {
        String complete_vit = "";
        if (seq.contains(RLM)) {
            String[] tmp = seq.split(RLM);
            for (int i = 0; i < tmp.length; i++) {
                String[] usrObs = tmp[i].split(",");
                if (usrObs.length > 1) {

                    Object[] ret = Viterbi.forward_viterbi(usrObs,
                            states,
                            start,
                            trans_p,
                            emit_p);

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

                Object[] ret = Viterbi.forward_viterbi(usrObs,
                        states,
                        start,
                        trans_p,
                        emit_p);
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
public class RunRDVAT_old {

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

        int threshold = 1000;

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
        Hashtable<String, Hashtable<String, Double>> transition_probability
                = adaptor.readProbXMLTable(transPath);
        String[] states = exts.toArray(new String[exts.size()]);
        Hashtable<String, Hashtable<String, Double>> emission_probability = adaptor.readProbXMLTable(emissionPath);
        Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obsPath);
        System.out.println("observation table" + obsTable.size());
        Hashtable<String, Double> start = adaptor.getStartProb(transition_probability);
        RecursiveDVA_old rdva = new RecursiveDVA_old(null, densityPath, exts, obsTable, emission_probability, transition_probability, start);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rdva);

    }

}
