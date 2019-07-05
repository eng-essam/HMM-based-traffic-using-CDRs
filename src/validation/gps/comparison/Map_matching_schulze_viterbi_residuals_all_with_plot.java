package validation.gps.comparison;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.DoubleStream;

import Density.Plot;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;
import Viterbi.WieghtedGraph;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.GPSTrkPnt;
import utils.Vertex;

/**
 * 
 * @author essam
 * @date Thu Aug 4 23:58:55 EEST 2016
 * 
 */
public class Map_matching_schulze_viterbi_residuals_all_with_plot {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double scale = Double.parseDouble(args[0]);
		double minlat = Double.parseDouble(args[1]);
		double minlon = Double.parseDouble(args[2]);
		double maxlat = Double.parseDouble(args[3]);
		double maxlon = Double.parseDouble(args[4]);
		String edges_file_path = args[5];
		String towers_file_path = args[6];
		String gpx_file_path = args[7];
		String voronoi_file_path = args[8];
		String network_file_path = args[9];
		String transition_file_path = args[10];
		String emission_file_path = args[11];
		String output_file_path = args[12];

		float distance_thrshd = 10;
		double max_search_dist = 1000;
		double[] alignment_threshold_arr = { 50, 100, 150 };
		double[] sampling_time_threshold_arr = { 0, 180, 300, 600, 900 };
		double[] radius = { 1000, 3000, 5000 };

		// double[] alignment_threshold_arr = { 20, 50, 100, 150 };
		// double[] sampling_time_threshold_arr = { 0 };
		// double[] radius = { 3000 };

		Hashtable<Integer, Vertex> twrs = DataHandler.readTowers(towers_file_path);
		ArrayList<Edge> edges = new NetConstructor(edges_file_path).readedges();

		Map_matching_schulze mms = new Map_matching_schulze();
		mms.index_edges(edges);
		// read GPX format file ..
		List<List<GPSTrkPnt>> trk_segs_list = mms.read_gpx_data_no_proj(gpx_file_path, scale);
		// trk_segs_list.remove(2);
		// trk_segs_list.remove(3);
		// trk_segs_list.remove(7);
		// trk_segs_list.remove(8);

		List<List<GPSTrkPnt>> gps_pnt_map_zones = mms.map_gps_pnt_zones(trk_segs_list, voronoi_file_path);

		// viterbi preprocessing
		DataHandler adaptor = new DataHandler();

		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emission_file_path);
		Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transition_file_path);
		ArrayList<String> exts = adaptor.getExts(trans_p);

		String[] states = exts.toArray(new String[exts.size()]);

		Hashtable<String, Double> start = adaptor.getStartProb(trans_p);
		// states = exts.toArray(new String[exts.size()]);
		Viterbi viterbi = new Viterbi();

		Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);

		ObsIndex oi = new ObsIndex();
		oi.Initialise(states, emit_p);

		// iterate over different radius for Schulze's map-matching algorithm
		for (int kk = 0; kk < radius.length; kk++) {
			double mmsradius = radius[kk];

			// iterate over different time sampling periods
			for (int jj = 0; jj < sampling_time_threshold_arr.length; jj++) {
				double sampling_time_threshold = sampling_time_threshold_arr[jj];

				List<String> distances = new ArrayList<>();
				List<String> divergences = new ArrayList<>();
				List<String> alignments = new ArrayList<>();
				List<String> accuracy = new ArrayList<>();
				List<String> running_time = new ArrayList<>();

				// map cellular observations ..
				List<List<GPSTrkPnt>> gps_pnt_zones;
				// if (sampling_time_threshold == 0)
				// gps_pnt_zones = mms.map_gps_pnt_zones(trk_segs_list,
				// voronoi_file_path);
				// else
				// gps_pnt_zones = mms.map_gps_pnt_zones(trk_segs_list,
				// voronoi_file_path, sampling_time_threshold);

				if (sampling_time_threshold != 0) {
					gps_pnt_zones = mms.sample_obs_time(gps_pnt_map_zones, sampling_time_threshold);
				} else {
					gps_pnt_zones = new ArrayList<List<GPSTrkPnt>>(gps_pnt_map_zones);
				}

				double[] avg_v_precision = new double[alignment_threshold_arr.length],
						avg_v_recall = new double[alignment_threshold_arr.length],
						avg_v_fmeasure = new double[alignment_threshold_arr.length];
				Arrays.fill(avg_v_precision, 0);
				Arrays.fill(avg_v_recall, 0);
				Arrays.fill(avg_v_fmeasure, 0);

				double[] avg_mms_precision = new double[alignment_threshold_arr.length],
						avg_mms_recall = new double[alignment_threshold_arr.length],
						avg_mms_fmeasure = new double[alignment_threshold_arr.length];

				Arrays.fill(avg_mms_precision, 0);
				Arrays.fill(avg_mms_recall, 0);
				Arrays.fill(avg_mms_fmeasure, 0);

				gps_pnt_zones.stream().forEach(gps_seg -> {
					int index = gps_pnt_zones.indexOf(gps_seg);
					System.out.println("Current track segment is " + index);
					// these skipped segments contains missing end, which cause
					// incomplete. trajectories calculated by schulze method.
					// but
					// they
					// return good
					// fit within our method ...
					if (index != 2 && index != 3 && index != 7 && index != 8) {
						// if (index == 0) {
						String obs[] = mms.get_observations(gps_seg);
						// viterbi
						long stime = System.currentTimeMillis();
						Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(obs, states, emit_p, si, oi);
						long etime = System.currentTimeMillis();
						long vit_time = (etime - stime);
						String vedges[] = mms.remove_duplicated_edges(((String) ret[1]).split(","));

						Plot plotter = new Plot(edges, output_file_path.replace(".csv", "_" + index + "_sampling_"
								+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.png"));
						plotter.scale(minlat * scale, minlon * scale, maxlat * scale, maxlon * scale);
						plotter.plotMapEdges();
						plotter.plot_towers(twrs);

						List<String> g_edges = new ArrayList<>();
						stime = System.currentTimeMillis();
						// construct traffic graph
						WieghtedGraph graph = mms.construct_search_graph(network_file_path, twrs, obs, 1, mmsradius,
								scale, g_edges);

						// find origin/destinations alternatives based on the
						// closest
						// five
						// edges
						// to the cellular towers ....
						Hashtable<Integer, List<String>> close_twrs_edges = mms.get_start_end_egdes(twrs, edges,
								gps_seg, g_edges, distance_thrshd);

						// // find routes using map matching approach proposed
						// by
						// schulze 2015
						List<String> route = mms.find_route(graph, close_twrs_edges, max_search_dist);
						etime = System.currentTimeMillis();
						long mms_time = (etime - stime);

						running_time.add(vit_time + "," + mms_time);

						// String vedges[] = {};
						// get points ..
						double increment = 0.1; // meters

						double[] ls = new double[6];
						List<Vertex> gps_pnts = mms.get_gps_points(trk_segs_list.get(index), scale, increment, ls);
						// System.out.println("Viterbi\n==================================================");
						List<Vertex> viterbi_pnts = mms.get_vpath_points(vedges, scale, increment, ls);
						// System.out.println("MM_S\n==================================================");
						List<Vertex> mms_pnts = mms.get_mm_points(route, scale, increment, ls);

						distances.add(ls[0] + "," + ls[2] + "," + ls[4]);
						// System.out.println(length);

						double[] viterbi_residuals = mms.georesiduals(gps_pnts, viterbi_pnts, scale);
						// Diveregence is the average alignment distances ...
						double viterbi_div = DoubleStream.of(viterbi_residuals).sum() / viterbi_residuals.length;

						mms.write_csv(viterbi_residuals,
								output_file_path.replace(".csv", "_" + index + "_viterbi_residuals_sampling_"
										+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));

						double[] mms_residuals = mms.georesiduals(gps_pnts, mms_pnts, scale);
						double mms_div = DoubleStream.of(mms_residuals).sum() / mms_residuals.length;
						mms.write_csv(mms_residuals,
								output_file_path.replace(".csv", "_" + index + "_mms_residuals_sampling_"
										+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));

						divergences.add(viterbi_div + "," + mms_div);

						for (int ii = 0; ii < alignment_threshold_arr.length; ii++) {
							double alignment_threshold = alignment_threshold_arr[ii];
							double viterbi_align_ratio = mms.get_alignment_ratio(viterbi_residuals,
									alignment_threshold);
							double viterbi_matched_segments_length = mms.geomatched_routed_length(viterbi_pnts,
									viterbi_residuals, scale, alignment_threshold, increment);

							double vprecision = viterbi_matched_segments_length / ls[3];
							double vrecall = viterbi_matched_segments_length / ls[1];
							double vf_measure = 2 * (vprecision * vrecall) / (vprecision + vrecall);

							double mms_align_ratio = mms.get_alignment_ratio(mms_residuals, alignment_threshold);
							double mms_matched_segments_length = mms.geomatched_routed_length(mms_pnts, mms_residuals,
									scale, alignment_threshold, increment);

							double mprecision = mms_matched_segments_length / ls[5];
							double mrecall = mms_matched_segments_length / ls[1];
							double mf_measure = 2 * (mprecision * mrecall) / (mprecision + mrecall);

							alignments.add(alignment_threshold + "," + viterbi_align_ratio + "," + mms_align_ratio);

							accuracy.add(alignment_threshold + "," + vprecision + "," + mprecision + "," + vrecall + ","
									+ mrecall + "," + vf_measure + "," + mf_measure);

							avg_v_precision[ii] += vprecision;
							avg_v_recall[ii] += vrecall;
							avg_v_fmeasure[ii] += vf_measure;

							avg_mms_precision[ii] += mprecision;
							avg_mms_recall[ii] += mrecall;
							avg_mms_fmeasure[ii] += mf_measure;
						}
						plotter.plot_gpx_traces(trk_segs_list.get(index), Color.RED);
						plotter.mark_map_edges(route, Color.BLUE, 0.001);
						plotter.plotPath(vedges, Color.GREEN, 0.001);
						plotter.display_save();
					}
				});

				List<String> accuracy_summary = new ArrayList<>();
				double nseg = 15;
				for (int ii = 0; ii < alignment_threshold_arr.length; ii++) {
					double alignment_threshold = alignment_threshold_arr[ii];

					accuracy_summary.add(
							alignment_threshold + "," + avg_v_precision[ii] / nseg + "," + avg_mms_precision[ii] / nseg
									+ "," + avg_v_recall[ii] / nseg + "," + avg_mms_recall[ii] / nseg + ","
									+ avg_v_fmeasure[ii] / nseg + "," + avg_mms_fmeasure[ii] / nseg);
				}
				mms.write_csv(distances, output_file_path.replace(".csv", "_travelling_distances_sampling_"
						+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));
				mms.write_csv(divergences, output_file_path.replace(".csv", "_divergence_sampling_"
						+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));
				mms.write_csv(alignments, output_file_path.replace(".csv", "_alignment_ratio_sampling_"
						+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));
				mms.write_csv(accuracy, output_file_path.replace(".csv", "_accuracy_sampling_" + sampling_time_threshold
						+ "_sec_mms_radius_" + mmsradius + "_meter.csv"));
				mms.write_csv(accuracy_summary, output_file_path.replace(".csv", "_accuracy_summary_sampling_"
						+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));
				mms.write_csv(running_time, output_file_path.replace(".csv", "_running_time_sampling_"
						+ sampling_time_threshold + "_sec_mms_radius_" + mmsradius + "_meter.csv"));
			}
			System.out.println("Finished ...");

		}
	}

}
