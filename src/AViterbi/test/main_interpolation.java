/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import AViterbi.Interpolate.Interpolation;
import Voronoi.VoronoiConverter;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_interpolation {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		String regions_path = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/dakar.vor.csv";
		String neighbors_path = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/dakar.vor.neighborSitesForSite.csv";
		String towers_path = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/towers.csv";

		VoronoiConverter converter = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoi_neighbors = converter.readVorNeighbors(neighbors_path);
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);

		List<Integer> seq = new ArrayList<>();
		seq.add(461);
		seq.add(454);
		seq.add(314);
		seq.add(323);
		// int t1 = 105;
		// int t2 = 117;

		Interpolation interpolate = new Interpolation(voronoi_neighbors, towers);
		List<Integer> list = interpolate.linear_interpolator(seq);

		// List<Integer> list = interpolate.hermite_interpolator(seq);
		for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext();) {
			Integer next = iterator.next();
			System.out.print(next + DataHandler.COMMA_SEP);
		}
		System.out.println("");

	}

}
