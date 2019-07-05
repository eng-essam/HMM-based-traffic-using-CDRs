/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import Density.Plot;
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
public class RunAll01 {

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
        String edgesPath =args[8];

        /**
         * Construct network distribution transition map using voronoi regions
         * "227761.06,1620859.93,240728.96,1635128.30"
         */
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;

        int towers = 500;
//        complete dakar map
//        xmin = 227761.06;
//        xmax = 270728.96;
//        ymin = 1618439.13;
//        ymax = 1645065.55;
        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Integer> regs = converter.getZones(converter.readVoronoi(vor));
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
        
        Plot ploter = new Plot(edges, image);
        ploter.scale(xmin, ymin, xmax, ymax);
        ploter.plotMapData(mapRoutesPath);

        /**
         * Adapt network distribution to be used in Viterbi
         */
//        AdaptData adaptData = new AdaptData(map);
        System.out.format("network reading completed with %d node(s)\n", map.size());
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = adaptData.getTransitionProb();
        TransBuilder tbuilder
                = new TransBuilder(map, vorNeighborsPath);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getTransitionProb2();
//
//        System.out.println("Writing transitions");
//        adaptor.writeXmlTable(transition_probability, transPath);
//        tbuilder.writeZonesTrans(transition_probability, transPath.substring(0, transPath.lastIndexOf(".")) + ".zones.xml");
         Hashtable<String, Hashtable<String, Double>> transition_probability
                = adaptor.readProbXMLTable(transPath);
        System.out.printf("transitions completed with nodes:\t%d\n", transition_probability.size());
        transition_probability = adaptor.adaptTrans(transition_probability);

        ArrayList<String> exts = adaptor.getExts();
//        String[] states =exts.toArray(new String[exts.size()]);

//        UTransitions tbuilder
//                = new UTransitions(netEdges);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getUTransitions(tbuilder.getTransitionProb());
        Hashtable<String, Hashtable<String, Double>> emission_probability_old
                = tbuilder.getEmissionProb(exts);

        String[] states = adaptor.getStates(emission_probability_old);
        System.out.println("states " + states.length);

//        System.out.println("Writing CSV Files");
//        adaptor.WriteCSVFile(
//                adaptor.adaptTrans(transition_probability, exts),
//                transPath.substring(0, transPath.lastIndexOf(".")) + ".csv");
//        
//        adaptor.WriteCSVFile(
//                adaptor.adaptEmission(emission_probability_old, exts, towers),
//                emissionPath.substring(0, emissionPath.lastIndexOf(".")) + ".csv");
//        
//        System.out.println("Writing CSV Files - Completed");
        System.out.println("Writing emission");
        adaptor.writeXmlTable(emission_probability_old, emissionPath);
        System.out.printf("emissions completed with nodes:\t%d\n", emission_probability_old.size());

        Hashtable<String, Hashtable<String, Double>> emission_probability = adaptor.adaptEmission(emission_probability_old, regs);

//        adaptData.printTable(emission_probability);
//        Hashtable<String, Double> start_probability
//                    = adaptor.getStartProb(emission_probability_old);
        /**
         * Observations handling
         */
//        Hashtable<Integer, String> obsTable = adaptData.readObservations(obsPath);
        Hashtable<Integer, String> obsTable = new Hashtable<>();
//        obsTable.put(1, "164,164,182,180,154,119,121");
//        obsTable.put(29, "117,108,89,87,103,127,141,125,114,125");
//        obsTable.put(2, "125,141,125,125,105,73,52,40,37");
//        obsTable.put(30, "33,25,27,34,34,41,60,59,46,73,105,125,114,125");
//        obsTable.put(3, "141,141,149,144,144,168,187,204,205");
//        obsTable.put(31, "160,162,181,195,213,182,152,154,137,121,97,92");
//        obsTable.put(4, "100,89,87,76,74,64,46,52,70,65,62,77");
//        obsTable.put(32, "94,110,132,139,148,131,125,141,141,159,157,157,125");
//        obsTable.put(5, "141,141,125,157,157,192,191,232");
//        obsTable.put(33, "265,289,289,265,232,188,201,211,202,179,175,200,217,227");
//        obsTable.put(6, "226,208,221,254,261,283,271,250,257");
//        obsTable.put(34, "257,250,271,271,271,250,250,247,253,246,225,208,197,175");
//        obsTable.put(7, "150,130,124,140,110,113,101,91,70");
//        obsTable.put(35, "73,84,81,87,89,108,117,137,166,181,210,205,244,234,206");
//        obsTable.put(8, "171,159,141,141,141,125,157,157,125");
//        obsTable.put(36, "141,141,158,171,187,168,142,142,142,160,174,204,206,224,232");
//        obsTable.put(9, "191,170,139,128,113,85,79,70,73,105");
//        obsTable.put(37, "131,101,131,125,125,141,149,168,187,171,183,192,191,192,224");
//        obsTable.put(10, "232,232,224,232,232,265,289,265,289,265,289");
//        obsTable.put(38, "265,289,289,265,232,191,148,139,132,155,185,201,237");
//        obsTable.put(11, "249,250,257,257,267,271,250,271,271,267,257,247");
//        obsTable.put(39, "235,219,228,237,252,232,265,289,289,265,252,252");
//        obsTable.put(12, "188,170,161,132,113,94,104,124,147");
//        obsTable.put(40, "165,163,185,211,228,230,250,271,271,271,267,257,257,250,271");
//        obsTable.put(13, "283,261,254,221,208,197,200,193,179");
//        obsTable.put(41, "150,130,109,104,94,113,85,79,91,105,125,141,141,127,142,160");
//        obsTable.put(14, "126,142,144,144,127,141,141,141,141");
//        obsTable.put(42, "141,159,157,157,125,141,141,158,172,168,144,144,168,172,171");
//        obsTable.put(15, "183,157,125,106,106,114,141,141,125,157,192,191");
//        obsTable.put(43, "170,139,132,140,120,104,109,130,147,179,193,219");
//        obsTable.put(16, "230,250,271,271,250,249,237,188,191,192,183,171");
//        obsTable.put(44, "171,172,168,144,142,142,127,141,141,125,157,192");
//        obsTable.put(17, "192,224,232,232,191,148,131,157,157,159,171");
//        obsTable.put(45, "206,224,232,252,265,232,224,224,224,232,265,265,265");
//        obsTable.put(18, "252,188,191,191,148,131,125,141,141,159");
//        obsTable.put(46, "183,206,204,174,168,144,144,127,141,141,159,171,171,206");
//        obsTable.put(19, "224,232,252,237,237,252,265,265,232,191");
//        obsTable.put(47, "148,139,161,177,201,237,249,250,271,271,271,271,283,261");
//        obsTable.put(20, "253,242,226,216,197,175,179,202,219,235");
//        obsTable.put(48, "230,230,250,249,249,250,230,228,202,185,177,188,252,265");
//        obsTable.put(21, "252,252,237,249,250,271,271,267,261,254");
//        obsTable.put(49, "221,208,216,227,235,219,202,185,155,161,139,128,131,157");
//        obsTable.put(22, "192,224,232,224,224,232,232,188,177");
//        obsTable.put(50, "185,202,228,230,250,249,249,250,271,283,261,254,225,243,242");
//        obsTable.put(23, "227,214,200,193,179,185,163,140,120");
//        obsTable.put(51, "104,94,113,101,105,125,114,127,103,122,108,100");
//        obsTable.put(52, "100,92,97,93");
//        obsTable.put(24, "88,95,118,138");
//        obsTable.put(25, "182,180,166");
//        obsTable.put(26, "162,146,126,122");
//        obsTable.put(27, "103,81,74,64,46,52,40,37,32,25");
//        obsTable.put(28, "40,37,32,25");
//        obsTable.put(281, "33,30,27,24,19");
        obsTable.put(52, "104,94,113,101,105,125,114,127,103,122,108,100");
        obsTable.put(53, "249,228,211,201,188,191,183,206,204,205,210,181,166,137");

        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
            String[] userObs = entrySet.getValue().split(",");
            System.out.println(Arrays.toString(userObs));

            Hashtable<String, Double> start_probability
                    = adaptor.getStartProb(userObs, emission_probability_old);

            Viterbi viterbi = new Viterbi();

            Object[] ret = Viterbi.forward_viterbi(userObs,
                    states,
                    start_probability,
                    transition_probability,
                    emission_probability);
            System.out.println("-->" + (String) ret[1]);
            System.out.printf("Valmax :%f\t\n", ret[2]);
            ploter.plotPath((String) ret[1]);

        }
        ploter.display_save();
    }

}
