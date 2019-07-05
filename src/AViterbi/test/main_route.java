/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.DoubleStream;

import Density.Plot;
import Observations.Obs;
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
public class main_route {

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
        String vor = "/home/essam/traffic/Dakar/senegal/senegal.vor.csv";
        String probXml = "/home/essam/traffic/Dakar/senegal/senegal.xy.dist.vor.xml";
        String obsPath = "/home/essam/traffic/SET2/SET2_P01.CSV.0_300_th-dist_1000_th-time_60.xml";
        String transPath = "/home/essam/traffic/Dakar/senegal/transition.xml";
        String emissionPath = "/home/essam/traffic/Dakar/senegal/emission.xml";
        String vorNeighborsPath = "/home/essam/traffic/Dakar/senegal/senegal.vor.neighborSitesForSite.csv";
        String image = "/home/essam/traffic/Dakar/senegal/image.png";
        String towersPath = "/home/essam/traffic/Dakar/senegal/towers.csv";
        String edgesPath = "/home/essam/traffic/Dakar/senegal/edges.interpolated.xml";
        String map_path = "/home/essam/traffic/Dakar/senegal/map";
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        System.out.println("edges\t" + edges.size());
//        227761.06
//        ,1319156.13,939691.91,2031584.9
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 939691.91;
        ymin = 1319156.13;
        ymax = 2031584.9;

        plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();

        DataHandler adaptor = new DataHandler();
        map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towersPath);

//        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
        plotter.plot_exts(exts);

        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoi_regions = converter.readVoronoi(vor);
        Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors
                = converter.readVorNeighbors(vorNeighborsPath);
        plotVor(voronoi_regions, voronoi_neighbors);

//        Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
//        Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
//        String[] states = exts.toArray(new String[exts.size()]);
//        
//        Hashtable<String, Double> start = adaptor.getStartProb(trans_p);
//
//        Viterbi viterbi = new Viterbi();
//        Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);
//
//        ObsIndex oi = new ObsIndex();
//        oi.Initialise(states, emit_p);
//
//        List<Integer> seq = new ArrayList<>();
//        seq.add(128);
//        seq.add(101);
//        seq.add(105);
//        seq.add(117);
//        seq.add(121);
//        seq.add(138);
//        Interpolation interpolate = new Interpolation(voronoi_neighbors, towers, voronoi_regions);
//        List<Integer> list = interpolate.interpolate_seq(seq);
//        String s_seq = list.get(0).toString();
//        for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext();) {
//            s_seq += "," + iterator.next();
//
//        }
//
//        String[] usrObs = s_seq.split(",");
//        Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs,
//                states,
//                emit_p,
//                si,
//                oi);
//        String vit_out = (String) ret[1];
//        if (vit_out != null) {
//            System.out.printf("%s\n", vit_out);
////                plotter.plotPath(vit_out, Color.RED);
//            plotter.plotPath(vit_out, Color.ORANGE);
////                plotter.plotColoredPath(vit_out);
//        }
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
