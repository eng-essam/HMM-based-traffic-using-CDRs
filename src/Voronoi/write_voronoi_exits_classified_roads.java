/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.util.ArrayList;

import mergexml.NetConstructor;
import utils.Edge;
import utils.FromNode;

/**
 * Determine exists of the Voronoi tessilation based on the roads class: class
 * type 0: primary roads class class type 1: main roads class including
 * unclassified roads class type 2: any road type ....
 *
 * @author essam Date: Wed Mar 16 10:05:18 EET 2016
 */
public class write_voronoi_exits_classified_roads {

	/**
	 * @param args
	 *            the command line arguments
	 *
	 */
	public static void main(String[] args) {
		// TODO code application logic here
		int roads_flag = Integer.parseInt(args[0]);
		String voronoiPath = args[1];
		String edgesPath = args[2];
		String probXml = args[3];
		String newProbXml = args[4];

		// String[] main_roads = {"motorway", "motorway_link", "trunk",
		// "trunk_link", "primary", "primary_link", "secondary",
		// "secondary_link", "tertiary"};
		// String[] all_roads = {"motorway", "motorway_link", "trunk",
		// "trunk_link", "primary", "primary_link", "secondary",
		// "secondary_link", "tertiary", "unclassified", "residential",
		// "service", "road"};
		// List<String> rt;
		// if (roads_flag == 0) {
		// rt = Arrays.asList(main_roads);
		// } else{
		// rt = Arrays.asList(all_roads);
		// }

		/**
		 * java -cp .:../diva.jar Voronoi.WriteVoronoi voronoiPath vorXmlPath
		 * probXml newProbXml
		 */
		ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
		VoronoiConverter converter = new VoronoiConverter();
		// converter.writeVoronoiFile(voronoiPath, vorXmlPath);
		// ArrayList<FromNode> map = converter.DetermineExits(probXml,
		// voronoiPath);
		// ArrayList<FromNode> map =
		// converter.determine_edges_cut_zones(probXml, voronoiPath, edges, rt);

		ArrayList<FromNode> map = converter.determine_edges_cut_zones(probXml, voronoiPath, edges, roads_flag);
		// System.out.println("writing");
		System.out.format("network reading completed with %d node(s)\n", map.size());

		converter.writeProbFile(newProbXml, map);

	}

}
