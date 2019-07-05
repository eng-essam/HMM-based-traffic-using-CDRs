/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.IntStream;

//import Viterbi.*;
import Density.Density;
import Observations.Obs;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class parallel_viterbi_hmm {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String states_file = args[0];
        String hmm_file = args[1];
        String obsPath = args[2];
        String densityPath = args[3];

        DataHandler adaptor = new DataHandler();

        IO io = new IO();
        String[] states = io.read_states(states_file);

//        System.out.println("state 2\t" + states[1]);
        Hashtable<String, Double> strt = new Hashtable<>();
        Hashtable<String, Hashtable<String, Double>> trans_p = new Hashtable<>();
        Hashtable<String, Hashtable<String, Double>> emit_p = new Hashtable<>();

        io.readHMM(states, hmm_file, strt, trans_p, emit_p);

        Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obsPath);
        System.out.println("observation table" + obsTable.size());

        ArrayList<String> exts = new ArrayList<>(Arrays.asList(states));

        Recursive_Viterbi rdva = new Recursive_Viterbi(null, densityPath, exts, obsTable, emit_p, trans_p, strt);

        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rdva);

    }

}

//class RecursiveDVAT extends RecursiveTask<int[][]> {
class Recursive_Viterbi extends RecursiveAction {

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

    public Recursive_Viterbi(String dayKey,
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
        long si_stime = System.currentTimeMillis();
        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);
        long si_etime = System.currentTimeMillis();
        System.out.println("Time for generating StateInfoTimeStamp: " + (si_etime - si_stime));
        ObsIndex oi = new ObsIndex();
        oi.Initialise(states, emit_p);

        /**
         * initialize all sequences of observations
         */
        Hashtable<Integer, Obs> obsDayTable = obsTable.get(dayKey);

        long stime = System.currentTimeMillis();

        //modified part ...
        List<Integer> keys_list = new ArrayList<>(obsDayTable.size());
        List<Obs> obs_list = new ArrayList<>(obsDayTable.size());
//        int i = 0;
        for (Map.Entry<Integer, Obs> entrySet : obsDayTable.entrySet()) {
            keys_list.add(entrySet.getKey());
            obs_list.add(entrySet.getValue());
        }

        obs_list.parallelStream().forEach((o) -> {
//            Hashtable<String, StateInfoTimeStamp> local_si = deep_clone(si);
            String vitPath = computeVit(o.getSeq(), viterbi.trans_mat_to_list_indexed(states, start, trans_p), oi);
            int index = obs_list.indexOf(o);
            Obs nobs = new Obs(o.getSeq(), o.getTimeStamp(), vitPath);
            obsDayTable.replace(keys_list.get(index), o, nobs);
        });

//        for (Map.Entry<Integer, Obs> entrySet : obsDayTable.entrySet()) {
//            Integer key = entrySet.getKey();
//            Obs obseq = entrySet.getValue();
//            String seq = obseq.getSeq();
//            String vitPath = computeVit(seq, si, oi);
//            if (!vitPath.isEmpty()) {
//                obsCounter++;
//            }
//            obseq.setVitPath(vitPath);
//            obsDayTable.replace(key, obseq);
//        }
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
            List<Recursive_Viterbi> jobs = new ArrayList<Recursive_Viterbi>();
//            System.out.println(obsTable.size());
            for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
                String key = entrySet.getKey();
                String path = densityPath.substring(0, densityPath.lastIndexOf(".")) + ".density.day." + key + ".xml";
                System.out.println(path);
//                RecursiveDVAT tmp = new RecursiveDVAT(key, path, exts, obsTable, emit_p, si, oi);
//                tmp.compute();
//                tmp.fork();
                jobs.add(new Recursive_Viterbi(key, path, exts, obsTable, emit_p, trans_p, start));

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

    private Hashtable<String, StateInfoTimeStamp> deep_clone(Hashtable<String, StateInfoTimeStamp> si) {
        Hashtable<String, StateInfoTimeStamp> local_si = new Hashtable<String, StateInfoTimeStamp>();
        for (Map.Entry<String, StateInfoTimeStamp> entry : si.entrySet()) {
            String key = entry.getKey();
            StateInfoTimeStamp sci_v = entry.getValue();

            local_si.put(key, sci_v);
        }
        return local_si;
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
