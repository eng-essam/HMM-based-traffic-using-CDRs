/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import Density.Plot;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class PlotRoutes {

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

//        int threshold = 1000;
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        Plot ploter = new Plot(edges, image);
        ploter.scale(xmin, ymin, xmax, ymax);
        ploter.plotMapData(mapRoutesPath);

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
//        Hashtable<String, Hashtable<String, Double>> transition_probability
//                = tbuilder.getTransitionProb(threshold, exts, towersPath);
//        System.out.println("Writing transitions");
//        adaptor.writeXmlTable(transition_probability, transPath);

        Hashtable<String, Hashtable<String, Double>> trans_p
                = adaptor.readProbXMLTable(transPath);

//        tbuilder.writeZonesTrans(transition_probability, transPath.substring(0, transPath.lastIndexOf(".")) + ".zones.xml");
        String[] states = exts.toArray(new String[exts.size()]);

//        Hashtable<String, Hashtable<String, Double>> emission_probability
//                = tbuilder.getEmissionProb(exts);
//        emission_probability = tbuilder.emitNeighbors(emission_probability);
//        System.out.println("Writing emissions");
//        adaptor.writeXmlTable(emission_probability, emissionPath);
        Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
//        Viterbi viterbi = new Viterbi();
//        int[][] densityMap = new int[exts.size()][exts.size()];
//        Hashtable<Integer, Hashtable<Integer, Obs>> obsTable = adaptor.readObsDUT(obsPath);
        Hashtable<Integer, String> obsTable = new Hashtable<>();
//        obsTable.put(52, "104,94,113,101,105,125,114,127,103,122,108,100");
        obsTable.put(1, "249,228,211,201,188,191,183,206,204,205,210,181,166,137");
        obsTable.put(2, "27,30,33,39,59,60,87,89,110");
        obsTable.put(21, "110,117,121");
        obsTable.put(3, "60,59,45,52");
        obsTable.put(31, "70,73,84,81");
        obsTable.put(4, "105,101,128,139,132,110,140,163");
        obsTable.put(5, "165,179,193,200,214,215");
        
//        obsTable.put(1, "249,228,211,201,188,191,183,206,204,205,210,181,166,137");
//        obsTable.put(2, "27,30,33,41,60,87,89,110,117,121");
//        obsTable.put(3, "60,59,45,52,70,79,85,113,110");
//        obsTable.put(4, "105,101,128,139,132,110,140,163,165,179,193,200,214,215");
        System.out.println("observation table" + obsTable.size());
        Hashtable<String, Double> start = adaptor.getStartProb(trans_p);

        Viterbi viterbi = new Viterbi();

        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

        ObsIndex oi = new ObsIndex();
        oi.Initialise(states, emit_p);

        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
            Integer key = entrySet.getKey();
            String[] usrObs = entrySet.getValue().split(",");
//            Object[] ret =viterbi.forward_viterbi(usrObs, states, start, trans_p, emit_p);
            Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs,
                    states,
                    emit_p,
                    si,
                    oi);
            String vit_out = (String) ret[1];
            if (vit_out != null) {
                System.out.println(key+"\t>>\t"+vit_out);
                ploter.plotPath(vit_out, Color.BLUE);
            }
        }
        ploter.display_save();
    }

}
