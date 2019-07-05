
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import Density.Density;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunAll1 {

//    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors(); 
//    private static Semaphore accessControl = new Semaphore(MAX_THREADS);
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
        int zones = 300;

        final int MAX_THREADS = 2 * Runtime.getRuntime().availableProcessors();
        Semaphore thrdsCtrl = new Semaphore(MAX_THREADS);

        /**
         * Construct network distribution transition map using voronoi regions
         * "227761.06,1620859.93,240728.96,1635128.30"
         */
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;

//        int towers = 500;
//        complete dakar map
//        xmin = 227761.06;
//        xmax = 270728.96;
//        ymin = 1618439.13;
//        ymax = 1645065.55;
        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Integer> regs = converter.getZones(converter.readVoronoi(vor));
//        ArrayList<Integer> regs = new ArrayList<>();
//        for (int i = 1; i <= zones; i++) {
//            regs.add(i);
//        }
        Hashtable<Integer, ArrayList<Integer>> voronoiNeibors
                = converter.readVorNeighbors(vorNeighborsPath);
//        converter.writeVoronoiFile(voronoiPath, vorXmlPath);
//        ArrayList<FromNode> map = converter.DetermineExits(probXml, voronoiPath);
//        converter.writeProbFile(
//                probXml.substring(0, probXml.lastIndexOf(".")) + ".vor.xml",
//                map);
        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();

//        Plot ploter = new Plot(edges, image);
//        ploter.scale(xmin, ymin, xmax, ymax);
//        ploter.plotMapData(mapRoutesPath);
        /**
         * Adapt network distribution to be used in Viterbi
         */
//        AdaptData adaptData = new AdaptData(map);
        System.out.format("network reading completed with %d node(s)\n", map.size());
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = adaptData.getTransitionProb();
        ArrayList<String> exts = adaptor.getExts();
        TransBuilder tbuilder
                = new TransBuilder(map, vorNeighborsPath);
//        Hashtable<String, Hashtable<String, Double>> transition_probability 
//                = tbuilder.getTransitionProb(exts);

        Hashtable<String, Hashtable<String, Double>> transition_probability
                = tbuilder.getTransitionProb(threshold, exts, towersPath);
//     
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = adaptor.readProbXMLTable(transPath);
//        
//        adaptor.writeXmlTable(transition_probability, transPath);
//        tbuilder.writeZonesTrans(transition_probability, transPath.substring(0, transPath.lastIndexOf(".")) + ".zones.xml");
        System.out.printf("transitions completed with nodes:\t%d\n", transition_probability.size());
//        transition_probability = adaptor.adaptTrans(transition_probability);

        String[] states = exts.toArray(new String[exts.size()]);
//        UTransitions tbuilder
//                = new UTransitions(netEdges);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getUTransitions(tbuilder.getTransitionProb());
        Hashtable<String, Hashtable<String, Double>> emission_probability
                //                        = adaptor.readProbXMLTable(emissionPath);
                = tbuilder.getEmissionProb(exts);
        emission_probability = tbuilder.emitNeighbors(emission_probability);

//        System.out.println("states " + states.length);
//        System.out.println("Writing emission");
        adaptor.writeXmlTable(emission_probability, emissionPath);
        System.out.printf("emissions completed with nodes:\t%d\n", emission_probability.size());

//        Hashtable<String, Hashtable<String, Double>> emission_probability = adaptor.adaptEmission(emission_probability_old, regs);
        int[][] densityMap = new int[exts.size()][exts.size()];

//        adaptData.printTable(emission_probability);
//        Hashtable<String, Double> start_probability
//                = adaptor.getStartProb(transition_probability);
        /**
         * Observations handling
         */
//        Hashtable<Integer, String> obsTable = adaptor.readObservations(obsPath);
//
//        int index = 0;
//        int count = 0;
//        Hashtable<Integer, Hashtable<Integer, String>> obsTableParts = new Hashtable<>();
//        Hashtable<Integer, String> tmp = new Hashtable<>();
//        for (Map.Entry<Integer, String> entry : obsTable.entrySet()) {
//            Integer integer = entry.getKey();
//            String obs = entry.getValue();
//            tmp.put(integer, obs);
//            count++;
//            if (count >= 1000) {
//                obsTableParts.put(index, tmp);
//                tmp = new Hashtable<>();
//                index++;
//            }
//            if(index >=2) break;
//
//        }
        Hashtable<Integer, String> obsTable = new Hashtable<>();
                obsTable.put(52, "56,56,56,55,55,55,44,55,17,14,71,71,56,55,56,16,55,55,55,55,55,56,55,65,65,65,109,51,42,50,55,55,55,55,55,55,55,45,55,55,55,55,55,55,55,55,55,55,55,55,55,5,16,24,56,55,109,109,109,147,140,140,49,2,56,55,55,55,190,190,190,190,190,190,190,190,56,55,55,55,55,55,55,55,55,55,26,71,71,55,55,56,56,55,55,46,52,55,55,55,55,16,56,55,55,55,55,55,55,55,55,55,56,56,55,55,55,24,55,55,55,55,55,80,55,55,55,55,55,55,55,56,56,56,55,51,56");
//        obsTable.put(53, "249,228,211,201,188,191,183,206,204,205,210,181,166,137");
        obsTable.put(50, "185,202,228,230,250,249,249,250,271,283,261,254,225,243,242");
//        obsTable.put(23, "227,214,200,193,179,185,163,140,120");
//        obsTable.put(51, "104,94,113,101,105,125,114,127,103,122,108,100");
//        obsTable.put(52, "100,92,97,93");
//        obsTable.put(24, "88,95,118,138");
//        obsTable.put(25, "164,182,180,166");
//        obsTable.put(26, "162,146,126,122");
//        obsTable.put(27, "103,81,64,46,52");
//        obsTable.put(28, "40,37,32,25,27,24");
//
//        TransLimiter limiter = new TransLimiter(emission_probability, transition_probability);
//        ObservationHandler handler = new ObservationHandler(voronoiNeibors);

        final long stime = System.currentTimeMillis();
        Viterbi viterbi = new Viterbi();
        final long stime1 = System.currentTimeMillis();
        int count = 0;
        Hashtable<String, StateInfoTimeStamp> si_org = viterbi.trans_mat_to_list_indexed(states, adaptor.getStartProb(transition_probability), transition_probability);

        ObsIndex oi_org = new ObsIndex();
        oi_org.Initialise(states, emission_probability);

        final long etime1 = System.currentTimeMillis();
        System.out.println("Initialization Finished in\t" + (etime1 - stime1));

//        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        ExecutorService executor = Executors.newCachedThreadPool();
//        List<Future<Object[]>> list = new ArrayList<Future<Object[]>>();
        ArrayList<String> allSeq = new ArrayList<>();
        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
            String[] userObs = entrySet.getValue().split("/");
            for (String seq : userObs) {
                allSeq.add(seq);
            }
        }

        /**
         * Limit the number of threads called
         */
        int noth = 500;
        for (int i = 0; i < (allSeq.size() / noth); i++) {
            int stop = (i + 1) * noth;
            List<Future<Object[]>> list = new ArrayList<Future<Object[]>>();

            for (int j = (i * noth); j < stop; j++) {
                String[] subObs = allSeq.get(j).split(",");
                if (subObs.length < 2) {
                    continue;
                }
                System.out.println(count++);
                RunnableVA rva = new RunnableVA(subObs, states, emission_probability, si_org, oi_org);
                Future<Object[]> obsTask = executor.submit(rva);
//                Thread t = new Thread(obsTask);
//                t.start();
                list.add(obsTask);
//                executor.execute(rva);
            }

            for (Iterator<Future<Object[]>> iterator = list.iterator(); iterator.hasNext();) {
                Future<Object[]> future = iterator.next();
                try {
                    Object[] vit = future.get();
                    String vit_path = (String) vit[1];
                    System.out.println("-->\t" + vit_path);
//                System.out.printf("Valmax :%f\t\n", vit[2]);

                    /**
                     * update density maps
                     */
                    String[] out = vit_path.split(",");
                    for (int k = 0; k < out.length - 1; k++) {
                        if (exts.contains(out[k]) && exts.contains(out[k + 1])) {
                            densityMap[exts.indexOf(out[k])][exts.indexOf(out[k + 1])] += 1;
                        }

                    }
                    System.err.println(count--);
//                ploter.plotPath(vit_path, Color.BLUE);
                } catch (InterruptedException | ExecutionException e) {
                }
            }

        }

//        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
//            String[] userObs = entrySet.getValue().split("/");
//
//            for (String seq : userObs) {
//                System.out.println(count++);
//                String[] subObs = seq.split(",");
//                if (subObs.length < 2) {
//                    continue;
//                }
//
//                RunnableVA rva = new RunnableVA(subObs, states, emission_probability, si_org, oi_org);
//                Future<Object[]> obsTask = executor.submit(rva);
////                Thread t = new Thread(obsTask);
////                t.start();
//                list.add(obsTask);
////                executor.execute(rva);
//            }
//
//        }
//
//        for (Iterator<Future<Object[]>> iterator = list.iterator(); iterator.hasNext();) {
//            Future<Object[]> future = iterator.next();
//            try {
//                Object[] vit = future.get();
//                String vit_path = (String) vit[1];
//                System.out.println("-->\t" + vit_path);
////                System.out.printf("Valmax :%f\t\n", vit[2]);
//
//                /**
//                 * update density maps
//                 */
//                String[] out = vit_path.split(",");
//                for (int i = 0; i < out.length - 1; i++) {
//                    densityMap[exts.indexOf(out[i])][exts.indexOf(out[i + 1])] += 1;
//
//                }
//                System.err.println(count--);
////                ploter.plotPath(vit_path, Color.BLUE);
//            } catch (InterruptedException | ExecutionException e) {
//            }
//        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(RunAll1.class.getName()).log(Level.SEVERE, null, ex);
        }
//        }
        final long etime = System.currentTimeMillis();
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
//        adaptor.writeDensity(denseTable, obsPath.substring(0, obsPath.lastIndexOf(".")) + ".denisty.xml");
//        ploter.display_save();
        System.out.println("Finished in\t" + (etime - stime));
    }

}
