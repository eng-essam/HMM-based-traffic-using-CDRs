/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import Voronoi.VoronoiConverter;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_distance_between_zones {

	/**
	 * Calculate distance between two cartesian points
	 *
	 * @param x
	 * @param y
	 * @param x1
	 * @param y1
	 * @return
	 */
	public static double eculidean(double x, double y, double x1, double y1) {
		return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		// TODO code application logic here
		String towersPath = args[0];
		String vorNeighborsPath = args[1];

		// DataHandler adaptor = new DataHandler();
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towersPath);
//		Hashtable<Integer, Vertex> towers = DataHandler.read_scaled_towers(towersPath, 1000);

		Voronoi.VoronoiConverter vorCon = new VoronoiConverter();
		Hashtable<Integer, ArrayList<Integer>> voronoiNeibors = vorCon.readVorNeighbors(vorNeighborsPath);

		double max = Double.MIN_VALUE;
		double sum = 0;
		int count = 0;
		for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibors.entrySet()) {
			Integer key = entrySet.getKey();
			Vertex v0 = towers.get(key);
			ArrayList<Integer> value = entrySet.getValue();

			for (Iterator<Integer> iterator = value.iterator(); iterator.hasNext();) {
				Integer next = iterator.next();
				Vertex v1 = towers.get(next);
				double dist = eculidean(v0.getX(), v0.getY(), v1.getX(), v1.getY());
//				System.out.printf("%f\t", dist);
				sum += dist;
				count++;
				if (dist > max) {
					max = dist;
				}

			}
//			System.out.println();

		}
		System.out.printf("max: %f\tavg: %f\n", max, sum / count);
	}

}
