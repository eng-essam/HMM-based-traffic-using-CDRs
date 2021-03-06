/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.DoubleStream;

import Density.Plot;
import Observations.Obs;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.Region;
import utils.StdDraw;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class VerfyViterbiPaths_plot {

    static ArrayList<FromNode> map;
    static Plot plotter;

    final static String RLM = "/";

    public static Hashtable<Integer, String> get_usr_trips(Hashtable<String, Hashtable<Integer, Obs>> obsTable, int userid) {
        Hashtable<Integer, String> obs = new Hashtable<>();
        int index = 0;
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
            Hashtable<Integer, Obs> value = entrySet.getValue();
            for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
                int id = entrySet1.getKey().intValue();
//                if (id == userid) {
                String utrips = entrySet1.getValue().getSeq();
                String trips[] = {utrips};
                if (utrips.contains(RLM)) {
                    trips = utrips.split(RLM);
                }
                for (int i = 0; i < trips.length; i++) {
                    String trip = trips[i];
                    if (trip.contains(",")) {
                        obs.put(index++, trip);
                    }

                }
//                }
            }
            /**
             * one day trips
             */
            break;
        }
        return obs;
    }

    public static double[] getCoordinates(String id) {
        double[] coord = {-1, -1};
        for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
            FromNode next = iterator.next();
            if (next.getID().equals(id)) {
                coord[0] = next.getX();
                coord[1] = next.getY();
                break;
            }
        }
        return coord;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String vor = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.csv";
        String probXml = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.vor.xml";
        String obsPath = "/home/essam/traffic/SET2/SET2_P01.CSV.0_500_th-dist_1000_th-time_60.xml";
        String transPath = "/home/essam/traffic/Dakar/Dakar_edge-200/transition.day.00.xml";
        String emissionPath = "/home/essam/traffic/Dakar/Dakar_edge-200/emission.xml";
        String vorNeighborsPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.neighborSitesForSite.org.csv";
//        String mapRoutesPath = args[6];
        String image = "/home/essam/traffic/Dakar/Dakar_edge-200/image_routes.png";
//        String edgesPath = args[8];
        String towersPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.towers.csv";
//        String densityPath = args[10];
        String edgesPath = "/home/essam/traffic/Dakar/Dakar_edge-200/edges.interpolated.xml";
        String map_path = "/home/essam/traffic/Dakar/Dakar_edge-200/map";

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        System.out.println("edges\t" + edges.size());
        int threshold = 1000;
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;
        plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();

        DataHandler adaptor = new DataHandler();
        map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);

//        plotter.plot_exts(exts);
        
        /**
         * plot voronnoi
         */
        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor);
        Hashtable<Integer, ArrayList<Integer>> voronoiNeibors
                = converter.readVorNeighbors(vorNeighborsPath);
        plotVor(voronoiRegions, voronoiNeibors);
        //======================================================================
        /**
         * Read transitions and emission probabilities from stored data
         */
        Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
        Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
        //======================================================================
        /**
         * Calculate transitions and emission probabilities
         */
//        Hashtable<String, Hashtable<String, Double>> trans_p
//                = tbuilder.getTransitionProb(threshold, exts, towersPath);
        
//        Hashtable<String, Hashtable<String, Double>> trans_p = tbuilder.getTransitionProb_mod(threshold, exts);
//        System.out.println("Writing transitions");
//        adaptor.writeXmlTable(trans_p, transPath);
//        tbuilder.writeZonesTrans(trans_p, transPath.substring(0, transPath.lastIndexOf(".")) + ".zones.xml");
        
        String[] states = exts.toArray(new String[exts.size()]);
 
//        Hashtable<String, Hashtable<String, Double>> emit_p
//                = tbuilder.getEmissionProb(exts);
//        emit_p = tbuilder.emitNeighbors_modified(emit_p);
//        System.out.println("Writing emissions");
//        adaptor.writeXmlTable(emit_p, emissionPath);
        //======================================================================

        /**
         * Wed 15 Apr 2015 03:38:09 PM JST
         *
         * Use old transition and emission probs.
         */
//        Hashtable<String, Hashtable<String, Double>> emit_p
//                = tbuilder.getEmissionProb(exts);
//        emit_p = tbuilder.emitNeighbors(emit_p);
//        System.out.println("Writing emissions");
//        adaptor.writeXmlTable(emit_p, emissionPath);
//        
//        
//        Hashtable<String, Hashtable<String, Double>> trans_p
//                = tbuilder.getTransitionProb(threshold, exts, towersPath);
//        System.out.println("Writing transitions");
//        adaptor.writeXmlTable(trans_p, transPath);
        //======================================================================
        
//        Viterbi viterbi = new Viterbi();
//        int[][] densityMap = new int[exts.size()][exts.size()];
//        Hashtable<String, Hashtable<Integer, Obs>> obsTable = adaptor.readObsDUT(obsPath);
//        System.out.println("observation table" + obsTable.size());
        Hashtable<String, Double> start = adaptor.getStartProb(trans_p);
//        states = exts.toArray(new String[exts.size()]);
        Viterbi viterbi = new Viterbi();

        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

        ObsIndex oi = new ObsIndex();
        oi.Initialise(states, emit_p);

//        Hashtable<Integer, String> obsTable = get_usr_trips(adaptor.readObsDUT(obsPath), 9000);
        Hashtable<Integer, Color> obsColors = new Hashtable<>();
        obsColors.put(0, Color.decode("#E6ED13"));
        obsColors.put(1, Color.decode("#EDCC13"));
        obsColors.put(2, Color.decode("#ED8713"));
        obsColors.put(3, Color.decode("#ED5F13"));
        obsColors.put(4, Color.decode("#ED3013"));
        obsColors.put(5, Color.decode("#E6ED13"));
        obsColors.put(5, Color.decode("#B3ED13"));

        Hashtable<Integer, String> obsTable = new Hashtable<>();
//        obsTable.put(0, "302,287,266,255,245,220,190,156,136,112,83,80,56,55,43");
//        obsTable.put(1, "228,211,201,188,224,206,204,205,210,233");
//        obsTable.put(2, "203,173,143,138,119,97");
//        obsTable.put(3, "300,298,296,288,263,275,264,251,218,215,194,178");
//        obsTable.put(4, "241,254,225,226,216,197,200,175,179,163,140,124,109,104,94,113,128");
//        obsTable.put(5, "180,154,137,121,117,100,89,87,60,59");
////        obsTable.put(6, "75,97,137,166,181,195,181,184,160,142,127,141,125");
//        obsTable.put(6, "75,97,137,166,181,195,181");

        
//        obsTable.put(0, "-27294527#3[2],-27294527#3[3],-27294527#3[4],108058762#6,108058762#6[1],108058762#[2],27294519#2,184916442#1,263082175#0,263082175#1,263082175#1[1],263082175#1[2],263082175#1[3]");
        obsTable.put(1, "37379596#4[6],37379596#4[7],37379596#4[8],37379596#4[9],37379596#4[10],37379596#4[11],37379596#4[12],37379596#5,37379596#6,37379596#7,37379596#8,108058762#1,108058762#2,108058762#2[1],108058762#3,108058762#5,108058762#5[1],108058762#5[2],-27294527#2,-27294527#2[1],-27294527#0,28107495#4,28107495#6,-108059461,-108059461[1]");
//        obsTable.put(2, "107897592#1[1],107897592#2,28107495#3,28107495#4,28107495#6,-108059461,-108059461[1],-108059461[2],-108059461[3],-108059461[4],-108059461[5],-108059461[6]");
//        obsTable.put(3, "27294527#4[1],-27294527#4,-27294527#4[1],-27294527#3,-27294527#3[1],-27294527#3[2],-27294527#3[3],-27294527#3[4],108058762#6,108058762#6[1],108058762#6[2],27294519#2,184916442#1,263082175#0,263082175#1,263082175#1[1],263082175#1[2],263082175#1[3]");
        
//        obsTable.put(1, "228,230,235,227,226");
//        obsTable.put(23, "254,225,243,226,227,214,200,179,163,140");
//        obsTable.put(51, "150,130,109,104,94,113,101,105,73");
//        obsTable.put(52, "100,92,97,93");
//        obsTable.put(24, "88,95,118,138");
//        obsTable.put(25, "164,182,180,166");
//        obsTable.put(26, "162,146,126,122");
//        obsTable.put(27, "103,81,64,46,52");
//        obsTable.put(28, "40,37,32,25,27,24");

        /**
         * Back to the old implementation
         */
//        ArrayList<Integer> regs = converter.getZones(converter.readVoronoi(vor));
//        Hashtable<String, Hashtable<String, Double>> emission_probability = adaptor.adaptEmission(emit_p, regs);
//        trans_p = adaptor.adaptTrans(trans_p);
//        
        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
            int key = entrySet.getKey();
            String seq = entrySet.getValue();
//
//            String[] usrObs = seq.split(",");

//            Hashtable<String, Double> start_probability
//                    = adaptor.getStartProb(usrObs, emit_p);
//            Object[] ret = viterbi.forward_viterbi(usrObs,
//                    states,
//                    start_probability,
//                    trans_p,
//                    emission_probability);
//            
            
            plotter.plotPath(seq, obsColors.get(key));
//            
//            Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs,
//                    states,
//                    emit_p,
//                    si,
//                    oi);
//            String vit_out = (String) ret[1];
//            if (vit_out != null) {
//                System.out.printf("%d \t %s\n", key, vit_out);
//                //                plotter.plotPath(vit_out, Color.ORANGE);
//                plotter.plotPath(vit_out, obsColors.get(key));
////                plotter.plotColoredPath(vit_out);
//            }
        }
        plotter.display_save();
    }
    public static void plot_exts() {
    }

    public static void plotVor(ArrayList<Region> voronoiRegions, Hashtable<Integer, ArrayList<Integer>> voronoiNeibors) {
        
        
        
        for (Region region : voronoiRegions) {
            if (region == null) {
                continue;
            }

            //            System.out.println("voronio" + region.id);
            ArrayList<Vertex> vertices = region.getVertices();

            double[] xPnts = new double[vertices.size()];
            double[] yPnts = new double[vertices.size()];

            int i = 0;
            for (Vertex vert : vertices) {
                //                StdDraw.filledCircle(vert.x, vert.y, 500);
                //                if()

                xPnts[i] = vert.getX();
                yPnts[i] = vert.getY();
                i++;
            }
            double cXPnt = DoubleStream.of(xPnts).average().getAsDouble();
            double cYPnt = DoubleStream.of(yPnts).average().getAsDouble();
            --i;
//            StdDraw.setPenRadius(0.3);
            StdDraw.text(cXPnt, cYPnt, String.valueOf(region.getId()));
            StdDraw.setPenRadius();
            if (voronoiNeibors.containsKey(region.getId())) {
                StdDraw.setPenColor(Color.BLUE);
                StdDraw.polygon(xPnts, yPnts);

            } else {
                StdDraw.setPenColor(Color.RED);
                StdDraw.polygon(xPnts, yPnts);
            }
        }
    }
}
