/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.DoubleStream;

import Density.Plot;
import Observations.Obs;
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
public class PlotStatistics {

    static ArrayList<FromNode> map;
    static Plot plotter;
    final static String CLM = ",";

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
        String vorNeighborsPath = "/home/essam/traffic/Dakar/senegal/senegal.vor.neighborSitesForSite.org.csv";
        String image = "/home/essam/traffic/Dakar/senegal/image1.png";
        String edgesPath = "/home/essam/traffic/Dakar/senegal/edges.interpolated.xml";

        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        System.out.println("edges\t" + edges.size());
        int threshold = 1000;
        double xmin, ymin, xmax, ymax;
        xmin = 227761.06;
        xmax = 906039.78;
        ymin = 1319156.13;
        ymax = 1861444.75;

//        xmin = 227761.06;
//        xmax = 240728.96;
//        ymin = 1620859.93;
//        ymax = 1635128.30;
        plotter = new Plot(edges, image);
        plotter.scale(xmin, ymin, xmax, ymax);
//        plotter.plotMapData(map_path);
        plotter.plotMapEdges();

        DataHandler adaptor = new DataHandler();
        map = adaptor.readNetworkDist(probXml);
        ArrayList<String> exts = adaptor.getExts();
//        TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);

//        plotter.plot_exts(exts);
        /**
         * plot voronnoi
         */
        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor);
        Hashtable<Integer, ArrayList<Integer>> voronoiNeibors
                = converter.readVorNeighbors(vorNeighborsPath);
        plotVor(voronoiRegions, voronoiNeibors);

        /**
         * Read clusters and user frequently visited places and plot it to the
         * map
         */
        plotter.display_save();

    }

    public static void plot_exts() {
    }
    public static void plotVor(ArrayList<Region> voronoiRegions, Hashtable<Integer, ArrayList<Integer>> voronoiNeibors) {
        StdDraw.setPenRadius();
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
    }

    public static Hashtable<Integer, ArrayList<Integer>> readClusters(String path) {

        Hashtable<Integer, ArrayList<Integer>> clusters = new Hashtable<>();
        
        BufferedReader br = null;
        String line = "";
        try {

            br = new BufferedReader(new FileReader(path));
            while ((line = br.readLine()) != null) {
                String[] records = line.split(CLM);
                
                if (clusters.containsKey(Integer.parseInt(records[0]))) {
                    ArrayList<Integer> usrs = clusters.get(Integer.parseInt(records[0]));
                    
                    
                } else {
                    ArrayList<Integer> usrs = new ArrayList<>();
                    
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done");
        return clusters;

    }
}
