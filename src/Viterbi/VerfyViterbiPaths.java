/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Density.Plot;
import Observations.Obs;
import Voronoi.VoronoiConverter;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.Region;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class VerfyViterbiPaths {

	static ArrayList<FromNode> map;
	static Plot plotter;

	final static String RLM = "/";

	public static Hashtable<Integer, String> get_usr_trips(Hashtable<String, Hashtable<Integer, Obs>> obsTable,
			int userid) {
		Hashtable<Integer, String> obs = new Hashtable<>();
		int index = 0;
		for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
			Hashtable<Integer, Obs> value = entrySet.getValue();
			for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
				int id = entrySet1.getKey().intValue();
				// if (id == userid) {
				String utrips = entrySet1.getValue().getSeq();
				String trips[] = { utrips };
				if (utrips.contains(RLM)) {
					trips = utrips.split(RLM);
				}
				for (int i = 0; i < trips.length; i++) {
					String trip = trips[i];
					if (trip.contains(",")) {
						obs.put(index++, trip);
					}

				}
				// }
			}
			/**
			 * one day trips
			 */
			break;
		}
		return obs;
	}

	public static double[] getCoordinates(String id) {
		double[] coord = { -1, -1 };
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
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String voronoi = "/home/essam/traffic/models/dakar/main/voronoi.csv";
		String probXml = "/home/essam/traffic/models/dakar/main/map.xy.dist.vor.xml";
		// String obsPath =
		// "/home/essam/traffic/SET2/SET2_P01.CSV.0_300_th-dist_1000_th-time_60.xml";
		String transPath = "/home/essam/traffic/models/dakar/main/transition.xml";
		String emissionPath = "/home/essam/traffic/models/dakar/main/emission.xml";
		String vorNeighborsPath = "/home/essam/traffic/models/dakar/main/neighbors.csv";
		// String mapRoutesPath = args[6];
		String image = "/home/essam/traffic/models/dakar/main/image1.png";
		// String edgesPath = args[8];
		String towersPath = "/home/essam/traffic/models/dakar/main/towers.csv";
		// String densityPath = args[10];
		String edgesPath = "/home/essam/traffic/models/dakar/main/edges.xml";
		// String map_path = "/home/essam/traffic/models/dakar/main/map";

		ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
		System.out.println("edges\t" + edges.size());
		// int threshold = 6000;

		double minlat = 14.5597, minlon = -17.5616, maxlat = 14.9036, maxlon = -17.1098;
		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		double xmin, ymin, xmax, ymax;
		xmin = 0;
		xmax = xymax[0] - xymin[0];
		ymin = 0;
		ymax = xymax[1] - xymin[1];

		/**
		 * Complete Dakar map
		 */
		// xmin = 227761.06;
		// xmax = 270728.96;
		// ymin = 1618439.13;
		// ymax = 1645065.55;
		plotter = new Plot(edges, image);
		plotter.scale(xmin, ymin, xmax, ymax);
		//// plotter.plotMapData(map_path);
		plotter.plotMapEdges();
		DataHandler adaptor = new DataHandler();
		map = adaptor.readNetworkDist(probXml);
		// ArrayList<String> exts = adaptor.getExts();
		// System.out.println("Exits:\t" + exts.size());
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towersPath);

		plotter.plot_towers(towers);
		// TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
		/**
		 * plot voronnoi
		 */
		VoronoiConverter converter = new VoronoiConverter();
		ArrayList<Region> voronoiRegions = converter.readVoronoi(voronoi);
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = converter.readVorNeighbors(vorNeighborsPath);

		plotter.plot_voronoi(voronoiRegions, voronoiNeibors);
		// ======================================================================
		/**
		 * Read transitions and emission probabilities from stored data
		 */
		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
		Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
		ArrayList<String> exts = adaptor.getExts(trans_p);
		System.out.println("Exits:\t" + exts.size());
//		plotter.plot_exts(exts);
		// ======================================================================
		/**
		 * Calculate transitions and emission probabilities
		 */
		// Hashtable<String, Hashtable<String, Double>> trans_p =
		// tbuilder.generate_transition_prob(threshold);
		//// tbuilder.getTransitionProb_4(threshold, exts);
		// System.out.println("Writing transitions");
		// adaptor.writeXmlTable(trans_p, transPath);
		// tbuilder.writeZonesTrans(trans_p, transPath.substring(0,
		// transPath.lastIndexOf(".")) + ".zones.xml");
		String[] states = exts.toArray(new String[exts.size()]);
		//
		// Hashtable<String, Hashtable<String, Double>> emit_p =
		// tbuilder.generate_emission_prob(towers);
		//// tbuilder.getEmissionProb(exts);
		//// emit_p = tbuilder.emitNeighbors_3(emit_p, towers);
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

		// Hashtable<Integer, String> obsTable =
		// get_usr_trips(adaptor.readObsDUT(obsPath), 9000);
		Hashtable<Integer, Color> obsColors = new Hashtable<>();
		obsColors.put(0, Color.decode("#E6ED13"));
		obsColors.put(1, Color.decode("#EDCC13"));
		obsColors.put(2, Color.decode("#ED8713"));
		obsColors.put(3, Color.decode("#ED5F13"));
		obsColors.put(4, Color.decode("#ED3013"));
		obsColors.put(5, Color.decode("#E6ED13"));
		obsColors.put(5, Color.decode("#B3ED13"));

		Hashtable<Integer, String> obsTable = new Hashtable<>();
		obsTable.put(0, "454,454,453,448,443,440,433,424,423,419,416,410,399,397,392,380,378,368,369,362,354,340,342,332,328,319,309,301,297,295,291,278,270,244,204,206,224,191,188,237,228,249");
		// obsTable.put(1,
		// "211,201,188,191,224,206,204,205,210,195,181,166,137,121,93,88");
		// obsTable.put(2,
		// "241,225,226,214,197,200,175,179,165,163,140,120,124,109,104,110,113,128");
		// obsTable.put(3, "254,225,243,227,217");
		// obsTable.put(5, "180,154,137,121,117,100,89,87,60,59");
		// obsTable.put(6, "75,97,137,166,181,195,181,184,160,142,127,141,125");
		// obsTable.put(7, "228,230,235,227,226");
		// obsTable.put(8, "254,225,243,226,227,214,200,179,163,140");
		// obsTable.put(9, "150,130,109,104,94,113,101,105,73");
		// obsTable.put(0, "254,225,243,227,217");
		/**
		 * Back to the old implementation
		 */
		// ArrayList<Integer> regs =
		// converter.getZones(converter.readVoronoi(vor));
		// emit_p = adaptor.adaptEmission(emit_p, regs);
		// trans_p = adaptor.adaptTrans(trans_p);
		//
		long stime = System.currentTimeMillis();
		for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
			int key = entrySet.getKey();
			String seq = entrySet.getValue();

			String[] usrObs = seq.split(",");

			// Hashtable<String, Double> start_probability
			// = adaptor.getStartProb(usrObs, emit_p);
			// Object[] ret = viterbi.forward_viterbi(usrObs,
			// states,
			// start_probability,
			// trans_p,
			// emit_p);
			//
			Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs, states, emit_p, si, oi);
			String vit_out = (String) ret[1];
			if (vit_out != null) {
				System.out.printf("%d \t %s\n", key, vit_out);
				// plotter.plotPath(vit_out, Color.RED);
				plotter.plotPath(vit_out, obsColors.get(key));
				// plotter.plotColoredPath(vit_out);
			}
		}
		long etime = System.currentTimeMillis();
		System.out.println("Time taken:\t" + (etime - stime));
		plotter.display_save();
	}

	public static void plot_exts() {
	}

}
