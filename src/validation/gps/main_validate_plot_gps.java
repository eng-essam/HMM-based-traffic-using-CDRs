/**
 * 
 */
package validation.gps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import Density.Plot;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.Vertex;

/**
 * @author essam
 * @date: Fri Mar 18 21:41:27 EET 2016
 * 
 *        Validate HMM decoded path with propped mobile calls
 * 
 *        java -cp
 *        .:../lib/jcoord-1.0.jar:../lib/diva.jar:../lib/jgrapht.jar:../lib/
 *        guava.jar:../lib/commons-math3-3.5.jar \
 *        validation.gps.main_validate_plot_gps \ ~/traffic/gnettrack\
 *        validation/Mobinil_2013.12.10_17.02.25.txt \
 *        ~/traffic/models/alex/all/mobinil/edges.xml \
 *        ~/traffic/models/alex/all/mobinil/map.xy.dist.vor.xml \
 *        ~/traffic/models/alex/all/mobinil/transition.xml \
 *        ~/traffic/models/alex/all/mobinil/emission.xml \
 *        ~/traffic/models/alex/all/mobinil/towers.csv \
 *        ~/traffic/models/alex/all/mobinil/gps_validation.png
 * 
 */
public class main_validate_plot_gps {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO validate HMM with propped path using G-nettrack

		String gps_file_path = args[0];
		String edge_file_path = args[1];
		String map_file_path = args[2];
		String transitions_file_path = args[3];
		String emission_file_path = args[4];
		String towers_file_path = args[5];
		String image_file_path = args[6];

		// Alex coordinates ...
		double minlat = 30.8362, minlon = 29.4969, maxlat = 31.3302, maxlon = 30.0916;

		// double coords[] = {29.439067, 30.781731, 30.128385, 31.322947};
		double[] xymin = DataHandler.proj_coordinates(minlat, minlon);
		double[] xymax = DataHandler.proj_coordinates(maxlat, maxlon);

		double xmin, ymin, xmax, ymax;
		xmin = 0;
		xmax = xymax[0] - xymin[0];
		ymin = 0;
		ymax = xymax[1] - xymin[1];

		ArrayList<Edge> edges = new NetConstructor(edge_file_path).readedges();
		Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_file_path);
		/**
		 * GPS data are organized as the following:
		 * Timestamp,Longitude,Latitude,CellID
		 */
		List<String[]> gps_data = DataHandler.read_csv(gps_file_path, DataHandler.COMMA_SEP);
		// remove header from the data ...
		gps_data.remove(0);
		List<Vertex> tmp_vl = new ArrayList<>();
		// Vertex[] vl = new Vertex[gps_data.size()];

		
		List<String> tmp_obs = new ArrayList<>();
		tmp_obs.add(gps_data.get(0)[3]);

		for (int i = 0; i < gps_data.size(); i++) {
			String[] rec = gps_data.get(i);
			double[] xy = DataHandler.proj_coordinates(Double.parseDouble(rec[2]), Double.parseDouble(rec[1]));
			tmp_vl.add(i, new Vertex((xy[0] - xymin[0]), (xy[1] - xymin[1])));
			// vl[i] = new Vertex((xy[0] - xymin[0]), (xy[1] - xymin[1]));
			int t = Integer.parseInt(rec[3]);
			// discard repeated observations ...

			if (Integer.parseInt(tmp_obs.get(tmp_obs.size() - 1)) != t) {
				if (!towers.containsKey(t)) {
					System.out.printf("Error tower %d is not in the towers list\n", t);
					break;
				}
				tmp_obs.add(rec[3]);
				//debug ..
				System.out.print(rec[3] + ",");
				if (tmp_obs.size() % 10 == 0)
					System.out.println();
			}

			// debug ..
			 if (tmp_obs.size() == 10) {
			 break;
			 }
		}
		System.out.println();
		Vertex[] vl = tmp_vl.toArray(new Vertex[tmp_vl.size()]);
		String[] obs = tmp_obs.toArray(new String[tmp_obs.size()]);

		Plot plotter = new Plot(20480, edges, image_file_path);
		plotter.scale(xmin, ymin, xmax, ymax);
		plotter.plotMapEdges();
		plotter.plot_lines(vl, Color.ORANGE);

		// plot observations ....
		// StdDraw.setPenColor(Color.BLUE);
		// double min = Double.MAX_VALUE;
		// for (int i = 0; i < obs.length - 1; i++) {
		// Vertex v = towers.get(Integer.parseInt(obs[i]));
		// Vertex v1 = towers.get(Integer.parseInt(obs[i + 1]));
		// // StdDraw.filledCircle(v.x, v.y, 50);
		// double dist = DataHandler.euclidean(v.x, v.y, v1.x, v1.y);
		// if (dist < min)
		// min = dist;
		// System.out.printf("%s -> %s: \t%f\n", obs[i], obs[i + 1], dist);
		// }
		//
		// System.out.println("Min distance: " + min);

		// plot towers ..
		plotter.plot_towers(towers);

		// read network ..
		DataHandler adaptor = new DataHandler();
		ArrayList<FromNode> map = adaptor.readNetworkDist(map_file_path);

		/**
		 * Read transitions and emission probabilities from stored data
		 */

		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emission_file_path);
		Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transitions_file_path);
		ArrayList<String> exts = adaptor.getExts(trans_p);
		// plot exists
		// plotter.plot_exts(exts);

		String[] states = exts.toArray(new String[exts.size()]);

		Hashtable<String, Double> start = adaptor.getStartProb(trans_p);
		// states = exts.toArray(new String[exts.size()]);
		Viterbi viterbi = new Viterbi();

		Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

		ObsIndex oi = new ObsIndex();
		oi.Initialise(states, emit_p);

		// Calculate viterbi for propped cdrs
		Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(obs, states, emit_p, si, oi);

		String vit_out = (String) ret[1];
		if (vit_out != null) {
			System.out.printf("%s\n", vit_out);
			plotter.plotPath(vit_out, Color.RED);
		}

		// render final image ..
		plotter.display_save();

	}

}
