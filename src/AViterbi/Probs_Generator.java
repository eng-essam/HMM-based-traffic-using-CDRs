/*
 javac -cp .:../jahmm.jar AViterbi/*.java

 java -cp .:../diva.jar:../jgrapht.jar  \
 -Xss1g \
 -d64 \
 -XX:+UseG1GC \
 -Xms50g \
 -Xmx50g \
 -XX:MaxGCPauseMillis=500 \
 AViterbi.Probs_Generator 
 */
package AViterbi;

import java.util.ArrayList;
import java.util.Hashtable;

import Viterbi.TransBuilder;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class Probs_Generator {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// String probXml = "/home/essam/traffic/Dakar_1/map.xy.dist.vor.xml";
		// String transPath = "/home/essam/traffic/Dakar_1/transition.xml";
		// String emissionPath = "/home/essam/traffic/Dakar_1/emission.xml";
		// String vorNeighborsPath =
		// "/home/essam/traffic/Dakar_1/neighbors.csv";
		// String towersPath = "/home/essam/traffic/Dakar_1/towers.csv";
		// String edgesPath = "/home/essam/traffic/Dakar_1/edges.xml";

		int threshold = Integer.parseInt(args[0]);
		String probXml = args[1];
		String transPath = args[2];
		String emissionPath = args[3];
		String vorNeighborsPath = args[4];
		String towersPath = args[5];
		String edgesPath = args[6];

		// max distance between two adjacent zones centers = 4900 and avg= 1400
		// -- Senegal
		// int threshold = 6000;
		// int scaling = 1000;
		ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
		System.out.println("edges\t" + edges.size());

		DataHandler adaptor = new DataHandler();
		ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
		ArrayList<String> exts = adaptor.getExts();
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towersPath);

		TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
		/**
		 * Calculate transitions and emission probabilities
		 */
		// transitions with in adjacent zones only ..

		// Hashtable<String, Hashtable<String, Double>> trans_p =
		// tbuilder.generate_transition_prob(threshold);

		/**
		 * date Sun Jul 31 07:55:55 EET 2016
		 * to save time, I changed the real distances with euclidean distances ..
		 */

		Hashtable<String, Hashtable<String, Double>> trans_p = tbuilder.generate_transition_prob_eculidain(threshold,
				exts, edges);
		/**
		 * @date Sun Mar 20 19:31:17 EET 2016
		 * @target Alex maps - transitions within a specific threshold of all
		 *         zones
		 */
		// Hashtable<String, Hashtable<String, Double>> trans_p =
		// tbuilder.generate_transition_prob(threshold, exts);

		System.out.println("Writing transitions");
		adaptor.writeXmlTable(trans_p, transPath);
		System.out.println("Finish writing transitions");
		// tbuilder.writeZonesTrans(trans_p, transPath.substring(0,
		// transPath.lastIndexOf(".")) + ".zones.xml");
		// String[] states = exts.toArray(new String[exts.size()]);
		//
		/**
		 * emit to adjacent zones
		 * 
		 * @date Tue Mar 22 21:32:05 EET 2016
		 */
		// Hashtable<String, Hashtable<String, Double>> emit_p =
		// tbuilder.generate_emission_prob(towers);
		// emit_p = tbuilder.emitNeighbors_3(emit_p, towers);

		// double emthreshold = 1000;
		// Hashtable<String, Hashtable<String, Double>> emit_p =
		// tbuilder.generate_emission_prob(towers, emthreshold);

		/**
		 * emit adjacent and adjacent of adjacents ...
		 */
		Hashtable<String, Hashtable<String, Double>> emit_p = tbuilder.generate_emission_prob_adj_adj_zones(towers);

		System.out.println("Writing emissions");
		adaptor.writeXmlTable(emit_p, emissionPath);
		System.out.println("Finish writing emissions");

	}
}
