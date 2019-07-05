/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Stops;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import Observations.Obs;
import utils.DataHandler;

//class RecursiveDVAT extends RecursiveTask<int[][]> {
class RecursiveDVA extends RecursiveAction {

    String dayKey;
    String densityPath;
    Hashtable<String, Hashtable<Integer, Obs>> obsTable;
    ArrayList<String> obs;

    public RecursiveDVA(String dayKey,
            String densityPath,
            Hashtable<String, Hashtable<Integer, Obs>> obsTable
    ) {
        this.dayKey = dayKey;
        this.densityPath = densityPath;
        this.obsTable = obsTable;
        obs = new ArrayList<>();

    }

    public void calcDayDensity() {

        Hashtable<Integer, Obs> obsDayTable = obsTable.get(dayKey);

        for (Map.Entry<Integer, Obs> entrySet : obsDayTable.entrySet()) {
            Integer key = entrySet.getKey();
            Obs obseq = entrySet.getValue();
            String seq = obseq.getSeq();
            obseq.setVitPath(seq);
            obsDayTable.replace(key, obseq);
        }
        Hashtable<String, Hashtable<Integer, Obs>> tmp = new Hashtable<>();
        tmp.put(dayKey, obsDayTable);

//        String path = densityPath.replace(".density.day.", ".viterbi.day.");

        Observations.ObsTripsBuilder.writeObsDUT(tmp, densityPath);

    }

    @Override
    protected void compute() {
        if (dayKey != null) {
            calcDayDensity();

        } else {
            List<RecursiveDVA> jobs = new ArrayList<RecursiveDVA>();
            for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
                String key = entrySet.getKey();
                String path = densityPath.substring(0, densityPath.lastIndexOf("/")+1) + key + ".xml";
                System.out.println(path);
                jobs.add(new RecursiveDVA(key, path, obsTable));

            }
            invokeAll(jobs);
        }
    }

}

/**
 *
 * @author essam
 */
public class SplitdaysOD {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String obsPath = args[0];
        String densityPath = args[1];
        
        DataHandler adaptor = new DataHandler();
        Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obsPath);

        RecursiveDVA rdva = new RecursiveDVA(null, densityPath, obsTable);
//        rdva.compute();
//        rdva.invoke();
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(rdva);

    }

}
