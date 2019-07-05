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
import Viterbi.TransBuilder;
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
public class VerfyViterbiPaths1 {

    static ArrayList<FromNode> map;

    final static String RLM = "/";

    public static Hashtable<Integer, String> get_usr_trips(Hashtable<String, Hashtable<Integer, Obs>> obsTable, int userid) {
        Hashtable<Integer, String> obs = new Hashtable<>();
        int index = 0;
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
            Hashtable<Integer, Obs> value = entrySet.getValue();
            for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
                int id = entrySet1.getKey().intValue();
                if (id == userid) {
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
                }
            }

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
        String vor = "/home/essam/traffic/Dakar/R_Dakar_1/Dakar.vor.csv";
        String probXml = "/home/essam/traffic/Dakar/R_Dakar_1/Dakar.xy.dist.vor.xml";
        String obsPath = "/home/essam/traffic/SET2/SET2_P01.CSV.0_500_th-dist_1000_th-time_60.xml";
        String transPath = "/home/essam/traffic/Dakar/R_Dakar_1/transition.day.00.xml";
        String emissionPath = "/home/essam/traffic/Dakar/R_Dakar_1/emission.xml";
        String vorNeighborsPath = "/home/essam/traffic/Dakar/R_Dakar_1/Dakar.vor.neighborSitesForSite.org.csv";
//        String mapRoutesPath = args[6];
        String image = "/home/essam/traffic/Dakar/R_Dakar_1/without_training_image.png";
//        String edgesPath = args[8];
        String towersPath = "/home/essam/traffic/Dakar/R_Dakar_1/Dakar.vor.towers.csv";
//        String densityPath = args[10];
        String edgesPath = "/home/essam/traffic/Dakar/R_Dakar_1/edges.xml";
        String map_path = "/home/essam/traffic/Dakar/R_Dakar_1/map";

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        System.out.println("edges\t" + edges.size());
        int threshold = 2000;
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;
        Plot plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();

        DataHandler adaptor = new DataHandler();
        map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);

        /**
         * plot voronnoi
         */
        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor);
        Hashtable<Integer, ArrayList<Integer>> voronoiNeibors
                = converter.readVorNeighbors(vorNeighborsPath);

        StdDraw.setPenColor(Color.GREEN);
        // turn on animation mode to defer displaying all of the points
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
            StdDraw.text(cXPnt, cYPnt, String.valueOf(region.getId()));

            if (voronoiNeibors.containsKey(region.getId())) {
                StdDraw.setPenColor(Color.BLUE);
                StdDraw.polygon(xPnts, yPnts);

            } else {
                StdDraw.setPenColor(Color.RED);
                StdDraw.polygon(xPnts, yPnts);
            }
        }

        String[] obs = {"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,-108821791#0,-108821791#0[4]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],-233379470#1"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],-25860752"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],-25860772[1]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],108176246[2]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],108176288"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,-108821791#0,108821767[3]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],108821791#0"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],108821791#0[3]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,-108821791#0,108821813#0[1]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],217821811[3]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],218927257#1[7]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],218927260#1[6]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],218927261[1]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],233379470#1"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],25860686[1]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],25860752"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],25860929[2]"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],27087877"
,"-293115888#0,-26927127,-28271412#0[1],8598094,44070480,-8598145#0,108160067#2[6],8598093"};

        for (int i = 0; i < obs.length; i++) {
            String out = obs[i];
            plotter.plotColoredPath(out);
        }

        plotter.display_save();
    }
}
