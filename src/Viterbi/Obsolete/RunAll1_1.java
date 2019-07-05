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
import java.util.stream.IntStream;

import Density.Density;
import Observations.ObservationHandler;
import Viterbi.TransBuilder;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunAll1_1 {

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
//        String densityPath = args[10];
        int threshold = 1000;
        int zones = 300;
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

//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getTransitionProb(threshold, exts, towersPath);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getTransitionProb2();
        Hashtable<String, Hashtable<String, Double>> transition_probability
                = adaptor.readProbXMLTable(transPath);
        transition_probability = adaptor.adaptTrans(transition_probability);

//        System.out.println("Writing transitions");
//        adaptor.writeXmlTable(transition_probability, transPath);
//        tbuilder.writeZonesTrans(transition_probability, transPath.substring(0, transPath.lastIndexOf(".")) + ".zones.xml");
        System.out.printf("transitions completed with nodes:\t%d\n", transition_probability.size());
//        transition_probability = adaptor.adaptTrans(transition_probability);

//        String[] states =exts.toArray(new String[exts.size()]);
//        UTransitions tbuilder
//                = new UTransitions(netEdges);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getUTransitions(tbuilder.getTransitionProb());
        Hashtable<String, Hashtable<String, Double>> emission_probability_old
                //                = adaptor.readProbXMLTable(emissionPath);
                = tbuilder.getEmissionProb(exts);
        emission_probability_old = tbuilder.emitNeighbors(emission_probability_old);

//        System.out.println("states " + states.length);
        System.out.println("Writing emission");
        adaptor.writeXmlTable(emission_probability_old, emissionPath);
        System.out.printf("emissions completed with nodes:\t%d\n", emission_probability_old.size());

        Hashtable<String, Hashtable<String, Double>> emission_probability = adaptor.adaptEmission(emission_probability_old, regs);
        int[][] densityMap = new int[exts.size()][exts.size()];

//        adaptData.printTable(emission_probability);
//        Hashtable<String, Double> start_probability
//                    = adaptor.getStartProb(emission_probability_old);
        /**
         * Observations handling
         */
        Hashtable<Integer, String> obsTable_org = adaptor.readObservations(obsPath);

        int index = 0;
        int count = 0;
        Hashtable<Integer, Hashtable<Integer, String>> obsTableParts = new Hashtable<>();
        Hashtable<Integer, String> tmp = new Hashtable<>();
        for (Map.Entry<Integer, String> entry : obsTable_org.entrySet()) {
            Integer integer = entry.getKey();
            String obs = entry.getValue();
            tmp.put(integer, obs);
            count++;
            if (count >= 1000) {
                obsTableParts.put(index, tmp);
                tmp = new Hashtable<>();
                index++;
                count = 0;
            }
            
            if (index>=5) {
                break;
            }

        }
        System.out.println("obsTableParts\t" + obsTableParts.size());
//        Hashtable<Integer, String> obsTable = new Hashtable<>();
//        obsTable.put(52, "249,228,211,201,188,191,192,183,206,204,205,210,181,166,137");
//        obsTable.put(53, "249,228,211,201,188,191,183,206,204,205,210,181,166,137");
//        TransLimiter limiter = new TransLimiter(emission_probability, transition_probability);
//        ObservationHandler handler = new ObservationHandler(voronoiNeibors);

        for (Map.Entry<Integer, Hashtable<Integer, String>> entry : obsTableParts.entrySet()) {
            Integer set_index = entry.getKey();
            Hashtable<Integer, String> obsTable = entry.getValue();
            System.out.println("Observation size\t" + obsTable.size());
            RunnableObs rObs = new RunnableObs(transition_probability, emission_probability, obsTable, exts, adaptor, set_index, obsPath, regs);
            rObs.start();
//        ploter.display_save();
        }

    }
}

class RunnableObs implements Runnable {

    private Thread t;
    private String threadName;
    TransLimiter limiter;
    Hashtable<Integer, String> obsTable;
    int[][] densityMap;
    ArrayList<String> exts;
    DataHandler adaptor;
    int set_index;
    String obsPath;
    ArrayList<Integer> regs;

    public RunnableObs(Hashtable<String, Hashtable<String, Double>> transition_probability, Hashtable<String, Hashtable<String, Double>> emission_probability, Hashtable<Integer, String> obsTable, ArrayList<String> exts, DataHandler adaptor, int set_index, String obsPath, ArrayList<Integer> regs) {
        this.limiter = limiter;
        this.obsTable = obsTable;
        this.exts = exts;
        this.adaptor = adaptor;
        this.set_index = set_index;
        this.obsPath = obsPath;
        this.regs = regs;
        threadName = String.valueOf(set_index);
        limiter = new TransLimiter(emission_probability, transition_probability);
    }

    @Override

    public void run() {

        ExecutorService executor = Executors.newFixedThreadPool(obsTable.size());
        List<Future<Object[]>> list = new ArrayList<Future<Object[]>>();
        final long stime = System.currentTimeMillis();
        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
            System.out.println(entrySet.getKey());
            String[] userObs = entrySet.getValue().split(",");
            System.out.println("User observation length\t" + userObs.length);
            ArrayList<ArrayList<String>> obsers = ObservationHandler.handleObs(userObs, regs);
//            System.out.println("User observation subsets\t" + obsers.size());
            for (Iterator<ArrayList<String>> it = obsers.iterator(); it.hasNext();) {
                ArrayList<String> arrayList = it.next();
                if (arrayList.isEmpty()) {
                    continue;
                }
                String[] subObs = arrayList.toArray(new String[arrayList.size()]);
                limiter.calcNewExts(subObs);
//                System.out.println("New Emissions\t");
                Hashtable<String, Hashtable<String, Double>> newEms = limiter.getEms();
//                System.out.println("New Emissions\t" + newEms.size());
                Hashtable<String, Hashtable<String, Double>> newTrns = limiter.getTrns();
//                System.out.println("New Transitions\t" + newTrns.size());
                Hashtable<String, Double> strt = limiter.getStrt();
                String[] states = limiter.getStates();
                RunnableVA rva = new RunnableVA(subObs, states, strt, newTrns, newEms);
                Future<Object[]> obsTask = executor.submit(rva);
                list.add(obsTask);
            }

        }

        for (Iterator<Future<Object[]>> iterator = list.iterator(); iterator.hasNext();) {
            Future<Object[]> future = iterator.next();
            try {
                Object[] vit = future.get();
                String vit_path = (String) vit[1];
                System.out.println("-->" + vit_path);
                System.out.printf("Valmax :%f\t\n", vit[2]);

                /**
                 * update density maps
                 */
                String[] out = vit_path.split(",");
                for (int i = 0; i < out.length - 1; i++) {
                    densityMap[exts.indexOf(out[i])][exts.indexOf(out[i + 1])] += 1;

                }
//                    ploter.plotPath(vit_path);
            } catch (InterruptedException | ExecutionException e) {
            }
        }
        executor.shutdown();
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
//            adaptor.writeDensity(denseTable, densityPath);
        Density.writeDensity(denseTable, obsPath.substring(0, obsPath.lastIndexOf(".")) + ".denisty."
                + set_index + ".xml");
        System.out.println("Finished in\t" + (etime - stime));
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
