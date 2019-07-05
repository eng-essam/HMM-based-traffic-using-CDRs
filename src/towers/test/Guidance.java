/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.stream.DoubleStream;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import Density.Plot;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import towers.ObservablesHandler;
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
public class Guidance {

	static ArrayList<FromNode> map;
	static Plot plotter;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String vor = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.csv";
		String probXml = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.xy.dist.vor.xml";
		String obsPath = "/home/essam/traffic/SET2/SET2_P01.CSV.0_500_th-dist_1000_th-time_60.xml";
		String transPath = "/home/essam/traffic/Dakar/Dakar_edge-200/transition.day.00.xml";
		String emissionPath = "/home/essam/traffic/Dakar/Dakar_edge-200/emission.xml";
		String vorNeighborsPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.neighborSitesForSite.org.csv";
		// String mapRoutesPath = args[6];
		String image = "/home/essam/traffic/Dakar/Dakar_edge-200/image1.png";
		// String edgesPath = args[8];
		String towersPath = "/home/essam/traffic/Dakar/Dakar_edge-200/Dakar.vor.towers.csv";
		// String densityPath = args[10];
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
		// plotter.plotMapData(map_path);
		plotter.plotMapEdges();

		DataHandler adaptor = new DataHandler();
		map = adaptor.readNetworkDist(probXml);
		ArrayList<String> exts = adaptor.getExts();
		TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);

		plotter.plot_exts(exts);

		/**
		 * plot voronnoi
		 */
		VoronoiConverter converter = new VoronoiConverter();
		ArrayList<Region> voronoiRegions = converter.readVoronoi(vor);
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = converter.readVorNeighbors(vorNeighborsPath);
		plotVor(voronoiRegions, voronoiNeibors);
		// ======================================================================
		/**
		 * Read transitions and emission probabilities from stored data
		 */
		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
		Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
		// ======================================================================
		/**
		 * Calculate transitions and emission probabilities
		 */
		// Hashtable<String, Hashtable<String, Double>> trans_p
		// = tbuilder.getTransitionProb(threshold, exts, towersPath);

		// Hashtable<String, Hashtable<String, Double>> trans_p =
		// tbuilder.getTransitionProb_mod(threshold, exts);
		// System.out.println("Writing transitions");
		// adaptor.writeXmlTable(trans_p, transPath);
		// tbuilder.writeZonesTrans(trans_p, transPath.substring(0,
		// transPath.lastIndexOf(".")) + ".zones.xml");
		String[] states = exts.toArray(new String[exts.size()]);

		// Hashtable<String, Hashtable<String, Double>> emit_p
		// = tbuilder.getEmissionProb(exts);
		// emit_p = tbuilder.emitNeighbors_modified(emit_p);
		// System.out.println("Writing emissions");
		// adaptor.writeXmlTable(emit_p, emissionPath);
		// ======================================================================
		/**
		 * Wed 15 Apr 2015 03:38:09 PM JST
		 *
		 * Use old transition and emission probs.
		 */
		// Hashtable<String, Hashtable<String, Double>> emit_p
		// = tbuilder.getEmissionProb(exts);
		// emit_p = tbuilder.emitNeighbors(emit_p);
		// System.out.println("Writing emissions");
		// adaptor.writeXmlTable(emit_p, emissionPath);
		//
		//
		// Hashtable<String, Hashtable<String, Double>> trans_p
		// = tbuilder.getTransitionProb(threshold, exts, towersPath);
		// System.out.println("Writing transitions");
		// adaptor.writeXmlTable(trans_p, transPath);
		// ======================================================================
		// Viterbi viterbi = new Viterbi();
		// int[][] densityMap = new int[exts.size()][exts.size()];
		// Hashtable<String, Hashtable<Integer, Obs>> obsTable =
		// adaptor.readObsDUT(obsPath);
		// System.out.println("observation table" + obsTable.size());
		Hashtable<String, Double> start = adaptor.getStartProb(trans_p);
		// states = exts.toArray(new String[exts.size()]);
		Viterbi viterbi = new Viterbi();

		Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

		ObsIndex oi = new ObsIndex();
		oi.Initialise(states, emit_p);

		/**
		 * Get observables sequences
		 */
		ObservablesHandler obs_handler = new ObservablesHandler();

		Hashtable<Integer, Vertex> towers = obs_handler.readTowers(towersPath);
		Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = obs_handler.readVoronoiNeibors(vorNeighborsPath);

		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> graph = obs_handler.construct_zone_graph(towers,
				voronoi_neighbors);

		String dst = "231";
		String src = "55";

		String path = obs_handler.get_weight_path(graph, src, dst);
		String[] usrObs = path.split(",");
		Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs, states, emit_p, si, oi);
		String vit_out = (String) ret[1];
		if (vit_out != null) {
			System.out.printf("%s\n", vit_out);
			plotter.plotPath(vit_out, Color.RED);
			// plotter.plotPath(vit_out, obsColors.get(key));
			// plotter.plotColoredPath(vit_out);
		}
		plotter.display_save();

	}

	public static void plotVor(ArrayList<Region> voronoiRegions,
			Hashtable<Integer, ArrayList<Integer>> voronoiNeibors) {

		for (Region region : voronoiRegions) {
			if (region == null) {
				continue;
			}

			// System.out.println("voronio" + region.id);
			ArrayList<Vertex> vertices = region.getVertices();

			double[] xPnts = new double[vertices.size()];
			double[] yPnts = new double[vertices.size()];

			int i = 0;
			for (Vertex vert : vertices) {
				// StdDraw.filledCircle(vert.x, vert.y, 500);
				// if()

				xPnts[i] = vert.getX();
				yPnts[i] = vert.getY();
				i++;
			}
			double cXPnt = DoubleStream.of(xPnts).average().getAsDouble();
			double cYPnt = DoubleStream.of(yPnts).average().getAsDouble();
			--i;
			// StdDraw.setPenRadius(0.3);
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
