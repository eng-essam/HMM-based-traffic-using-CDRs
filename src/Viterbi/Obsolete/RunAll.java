/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Hashtable;

import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import Voronoi.VoronoiConverter;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunAll {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String voronoiPath = args[0];
        String vorXmlPath = args[1];
        String probXml = args[2];
        String obsPath = args[3];
        String transPath = args[4];
        String emissionPath = args[5];
        String vorNeighborsPath = args[6];

        /**
         * Construct network distribution transition map using voronoi regions
         */
        VoronoiConverter converter = new VoronoiConverter();
        converter.writeVoronoiFile(voronoiPath, vorXmlPath);
        ArrayList<FromNode> map = converter.DetermineExits(probXml, voronoiPath);
        converter.writeProbFile(
                probXml.substring(0, probXml.lastIndexOf(".")) + ".vor.xml",
                map);

        /**
         * Adapt network distribution to be used in Viterbi
         */
        DataHandler adaptData = new DataHandler(map);
        System.out.format("network reading completed with %d node(s)\n", map.size());
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = adaptData.getTransitionProb();
        TransBuilder tbuilder
                = new TransBuilder(map, vorNeighborsPath);
        Hashtable<String, Hashtable<String, Double>> transition_probability
                = tbuilder.getTransitionProb();

        ArrayList<String> exts = adaptData.getExts();
        String[] states =exts.toArray(new String[exts.size()]);
        System.out.println("states " + states.length);
        
        System.out.println("Writing transitions");
        adaptData.writeXmlTable(transition_probability, transPath);
        System.out.printf("transitions completed with nodes:\t%d\n", transition_probability.size());
        transition_probability = adaptData.adaptTrans(transition_probability);
//        UTransitions tbuilder
//                = new UTransitions(netEdges);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getUTransitions(tbuilder.getTransitionProb());
        Hashtable<String, Hashtable<String, Double>> emission_probability
                = tbuilder.getEmissionProb(exts);
        System.out.println("Writing emission");
        adaptData.writeXmlTable(emission_probability, emissionPath);
        System.out.printf("emissions completed with nodes:\t%d\n", emission_probability.size());

//        adaptData.printTable(emission_probability);
//        String[] states = adaptData.getStates(transition_probability);
//        System.out.println("states " + states.length);

        /**
         * Observations handling
         */
//        Hashtable<Integer, String> obsTable = adaptData.readObservations(obsPath);
        Hashtable<Integer, String> obsTable = new Hashtable<>();
        obsTable.put(1, "13,14,9,7,3");
//        obsTable.put(2, "200,176,198,199");
//        obsTable.put(3, "38,26,35,30,112,194,5,66");
//        obsTable.put(4, "134,149,97,214,90,65,133");
//        obsTable.put(5, "81,58,152,28,25,214,82,93,99,88");
//        Hashtable<String, Integer> mapNodes = adaptData.getMapNodes();
        for (int counter = 1; counter <= obsTable.size(); counter++) {
            System.out.println(obsTable.get(counter));
            String[] userObs = obsTable.get(counter).split(",");

//            ArrayList<ArrayList<String>> fUsrObs = handler.handleObrSeq(userObs);
//            ArrayList<String> completeUsrOb = handler.getCompleteUsrOb();
//
//            for (String next : completeUsrOb) {
//                System.out.printf("%s,", next);
//            }
//            System.out.println("");
//
//            for (ArrayList<String> subObs : fUsrObs) {
//                String[] obrSeq = subObs.toArray(new String[subObs.size()]);
            /**
             * apply viterbi
             */
            int zoneID = Integer.parseInt(userObs[0]);

            Hashtable<String, Double> start_probability
                    = adaptData.getStartProb(zoneID, emission_probability);

            Viterbi viterbi = new Viterbi();

//            boolean flag = viterbi.getViterbiPath(userObs,
//                    states,
//                    start_probability,
//                    transition_probability,
//                    emission_probability);

//            if (!flag) {
//                System.out.println("----WRONG-----");
//            }
//            if (!flag) {
//                ArrayList<ArrayList<String>> fUsrObs = handler.handleObrSeq(userObs);
//                for (Iterator<ArrayList<String>> iterator = fUsrObs.iterator(); iterator.hasNext();) {
//                    ArrayList<String> nextObs = iterator.next();
//                    if (nextObs.size() == 1) {
//                        continue;
//                    }
//
//                    String[] mUsrObs = nextObs.toArray(new String[nextObs.size()]);
//                    zoneID = Integer.parseInt(mUsrObs[0]);    
//                    start_probability = adaptData.getStartProb(zoneID, emission_probability);
//
//                    viterbi.getViterbiPath(mUsrObs,
//                            states,
//                            start_probability,
//                            transition_probability,
//                            emission_probability,
//                            Color.BLUE);
//                }
//            }
        }
    }

}
