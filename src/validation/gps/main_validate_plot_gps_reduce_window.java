/**
 * 
 */
package validation.gps;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Density.Plot;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.StdDraw;
import utils.Vertex;

/**
 * @author essam
 * @date: Fri Mar 18 21:41:27 EET 2016
 * 
 *        Validate HMM decoded path with propped mobile calls
 * 
 *        java -cp
 *        .:../lib/jcoord-1.0.jar:../lib/diva.jar:../lib/jgrapht.jar:../lib/
 *        guava.jar:../lib/commons-math3-3.5.jar
 *        validation.gps.main_validate_plot_gps_reduce_window
 *        ~/traffic/gnettrack\ validation/Mobinil_2013.12.10_17.02.25.txt
 *        ~/traffic/models/alex/all/mobinil/edges.xml
 *        ~/traffic/models/alex/all/mobinil/map.xy.dist.vor.xml
 *        ~/traffic/models/alex/all/mobinil/transition.xml
 *        ~/traffic/models/alex/all/mobinil/emission.xml
 *        ~/traffic/models/alex/all/mobinil/towers.csv
 *        ~/traffic/models/alex/all/mobinil/gps_validation.png
 * 
 */
public class main_validate_plot_gps_reduce_window {

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
		String rss_file_path = args[7];
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
		 * 
		 */
		Plot plotter = new Plot(20480, edges, image_file_path);
		plotter.scale(xmin, ymin, xmax, ymax);
		plotter.plotMapEdges();

		/**
		 * GPS data are organized as the following:
		 * Timestamp,Longitude,Latitude,CellID
		 */
		List<String[]> gps_data = DataHandler.read_csv(gps_file_path, DataHandler.COMMA_SEP);
		List<String[]> gps_data_rss = new ArrayList<>();
		List<String[]> gps_data_rss_updt = new ArrayList<>();
		String[] rec = gps_data.get(0);
		String[] urec = new String[rec.length + 3];
		System.arraycopy(rec, 0, urec, 0, rec.length);
		urec[5] = "RSS Quality";
		urec[6] = "Distance";
		urec[7] = "Estimated power";
		gps_data_rss.add(urec);

		// remove header from the data ...
		gps_data.remove(0);
		List<Vertex> tmp_vl = new ArrayList<>();
		// Vertex[] vl = new Vertex[gps_data.size()];

		List<String> tmp_obs = new ArrayList<>();
		tmp_obs.add(gps_data.get(0)[3]);
		Map<String, Integer> gps_obs = new HashMap<>();
		int count = 0;
		int window = 5;
		// Parse GPS records ..
		for (int i = 0; i < gps_data.size(); i++) {
			rec = gps_data.get(i);
			urec = new String[rec.length + 3];
			double[] xy = DataHandler.proj_coordinates(Double.parseDouble(rec[2]), Double.parseDouble(rec[1]));
			tmp_vl.add(new Vertex((xy[0] - xymin[0]), (xy[1] - xymin[1])));
			int cellid = Integer.parseInt(rec[3]);
			Vertex cell_loc = towers.get(cellid);
			// quality = 2 * (dBm + 100) where dBm: [-100 to -50]
			int rss = Integer.parseInt(rec[4]);
			double q_rss = Math.min(Math.max(2 * (rss + 100), 0), 100);
			double dist = DataHandler.euclidean(tmp_vl.get(i).x, tmp_vl.get(i).y, cell_loc.x, cell_loc.y) / 1000.0;
			double org_pwr = Math.pow(dist, 2) * q_rss;
			// far distance*low rss == near distance* high rss
			if (q_rss > 75 || dist < 2) {
				// System.out.println(org_pwr);
				gps_data_rss_updt.add(rec);
			}
			System.arraycopy(rec, 0, urec, 0, rec.length);
			urec[5] = Double.toString(q_rss);
			urec[6] = Double.toString(dist);
			urec[7] = Double.toString(org_pwr);
			gps_data_rss.add(urec);

		}
		DataHandler.write_csv(gps_data_rss, rss_file_path);

		for (int i = 0; i < gps_data_rss_updt.size(); i++) {
			if (count++ == window) {
				String key = "";
				int max = -1;
				for (Map.Entry<String, Integer> entry : gps_obs.entrySet()) {
					if (entry.getValue() > max)
						key = entry.getKey();
				}
				if (Integer.parseInt(tmp_obs.get(tmp_obs.size() - 1)) != Integer.parseInt(key)
						&& towers.containsKey(Integer.parseInt(key))) {

					tmp_obs.add(key);
					System.out.print(key + ",");
					// get i
					i = get_index(Integer.parseInt(key), i, window, gps_data_rss_updt);

				}
				// reset values
				gps_obs = new HashMap<>();
				count = 0;
			}

			rec = gps_data_rss_updt.get(i);
			// discard repeated observations ...

			if (gps_obs.containsKey(rec[3])) {
				gps_obs.replace(rec[3], (gps_obs.get(rec[3]).intValue() + 1));
			} else {
				gps_obs.put(rec[3], 1);
			}

		}
		System.out.println();
		Vertex[] vl = tmp_vl.toArray(new Vertex[tmp_vl.size()]);

		plotter.plot_lines(vl, Color.ORANGE);
		// plot towers ..
		plotter.plot_towers(towers);

		// split_sub_trips(tmp_obs, towers);
		// display distance between towers ..
		// for (int i = 0; i < tmp_obs.size() - 1; i++) {
		// Vertex v0 = towers.get(Integer.parseInt(tmp_obs.get(i)));
		// Vertex v1 = towers.get(Integer.parseInt(tmp_obs.get(i + 1)));
		// System.out.printf("%s -> %s\t%f\n", tmp_obs.get(i), tmp_obs.get(i +
		// 1),
		// DataHandler.euclidean(v0.x, v0.y, v1.x, v1.y));
		// }
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

		// tmp_obs.stream().skip(10);
		String vit_out = "";
		List<String> p_obs = new ArrayList<>();
		for (int i = 0; i < tmp_obs.size(); i++) {
			// copy first 10 elements ..
			if (p_obs.size() < 3)
				p_obs.add(tmp_obs.get(i));
			else {
				String[] obs = p_obs.toArray(new String[p_obs.size()]);
				// Calculate viterbi for propped cdrs
				String[] ret = viterbi.forward_viterbi_linkedlist_indexed(obs, states, emit_p, si, oi);
				String n_vit_out = ret[1];
				if (!n_vit_out.isEmpty()) {
					vit_out = new String(n_vit_out);
					p_obs.add(tmp_obs.get(i));
				} else {
					System.out.println();
					System.out.println("size\t" + tmp_obs.size());
					p_obs = new ArrayList<>();
					p_obs.add(tmp_obs.get(i));

					System.out.printf("%s\n", vit_out);
					plotter.plotPath(vit_out, Color.RED);
					vit_out = "";
				}
				System.out.printf("%s\n", vit_out);
				// if (n_vit_out == null)
				// n_vit_out = "";
				// if (n_vit_out.isEmpty()) {
				// // tmp_obs.stream().skip(i).collect();
				// System.out.println();
				// System.out.println("size\t" + tmp_obs.size());
				// p_obs = new ArrayList<>();
				// p_obs.add(tmp_obs.get(i));
				//
				// System.out.printf("%s\n", vit_out);
				// plotter.plotPath(vit_out, Color.RED);
				// vit_out = "";
				// } else {
				// vit_out = n_vit_out;
				// p_obs.add(tmp_obs.get(i));
				// }
			}
		}

		if (!vit_out.isEmpty()) {
			System.out.printf("%s\n", vit_out);
			plotter.plotPath(vit_out, Color.RED);
			vit_out = "";
		}
		// plot observations ....
		StdDraw.setFont(new Font("DS-Digital", Font.BOLD, 11));
		for (int i = 0; i < tmp_obs.size(); i++) {
			StdDraw.setPenColor(Color.BLUE);
			Vertex v = towers.get(Integer.parseInt(tmp_obs.get(i)));
			StdDraw.filledCircle(v.x, v.y, 10);
			StdDraw.setPenColor(Color.BLACK);
			StdDraw.textLeft(v.x, v.y, tmp_obs.get(i));
		}

		// render final image ..
		plotter.display_save();

	}

	public static int get_index(int freq_twr, int cur_index, int window, List<String[]> gps_data) {

		int obs_i = cur_index;
		for (int i = cur_index - window; i < cur_index; i++) {
			int t = Integer.parseInt(gps_data.get(i)[3]);
			if (t == freq_twr)
				obs_i = i;
		}
		return obs_i;
	}

	/**
	 * split trips based on the monotonicity of x-axis
	 * 
	 * @param obs
	 * @param towers
	 * @return
	 */
	public static List<String[]> split_sub_trips(List<String> obs, Hashtable<Integer, Vertex> towers) {

		List<String[]> trips = new ArrayList<>();
		List<String> t = new ArrayList<>();
		// monoton flag 0 means increasing x-axis values , 0 decreasing, -1
		// not determine yet ..
		int monoton = 0;
		boolean changed = false;
		int count = 0;
		int window_size = 3;
		int window[] = new int[window_size];

		for (int i = 0; i < obs.size() - 1; i++) {
			// Vertex v0 = towers.get(Integer.parseInt(obs.get(i)));
			// Vertex v1 = towers.get(Integer.parseInt(obs.get(i + 1)));
			//
			// if (v0.x > v1.x) {
			// if (monoton == 0) {
			// t.add(obs.get(i));
			// } else {
			//
			// }
			// } else {
			// if (monoton == 1) {
			//
			// } else {
			//
			// }
			// }
			if (count == window_size) {
				check_monotoncity_window(window, towers);
				// System.out.println();
				count = 0;
				window[count++] = Integer.parseInt(obs.get(i));
			} else
				window[count++] = Integer.parseInt(obs.get(i));

		}
		return trips;
	}

	/**
	 * 
	 * @param w
	 * @param towers
	 * @return 1 increasing, -1 decreasing, 0 changed
	 */
	public static int check_monotoncity_window(int[] w, Hashtable<Integer, Vertex> towers) {
		int trend = 0;
		// System.out.println(Arrays.toString(w));
		for (int i = 0; i < w.length - 1; i++) {
			Vertex v0 = towers.get(w[i]);
			Vertex v1 = towers.get(w[i + 1]);

			if (v0.x > v1.x) {
				trend++;
			} else {
				trend--;
			}
		}

		if (trend == w.length - 1) {
			System.out.println(trend + "\t1");
			return 1;
		} else if (trend == (-1 * (w.length - 1))) {
			System.out.println(trend + "\t-1");
			return -1;
		} else {
			System.out.println(trend + "\t0");
			return 0;
		}
	}

}
