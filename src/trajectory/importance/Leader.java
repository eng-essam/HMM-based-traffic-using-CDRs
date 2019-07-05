/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 * 
 *         It is the implementation of the method proposed by isaacman 2011 in
 *         "S. Isaacman, R. Becker, R. Caceres, S. Kobourov, M. Martonosi, J.
 *         Rowland, and A. Varshavsky, "Identifying important places in people
 *         lives from cellular network data," in Pervasive computing, Springer,
 *         2011, pp. 133 - 151. "
 *         
 *         It depends on the availability of long history to identify important locations 
 */
public class Leader {

	Hashtable<Integer, ArrayList<Integer>> nvor;
	Hashtable<Integer, List<Tower>> usrs_obs;
	Hashtable<Integer, List<TCluster>> usrs_clusters;
	Hashtable<Integer, Vertex> towers_coords;
	// Hashtable<Integer, Vertex> towers = adaptor.readTowers(towersPath);
	/**
	 * Each cluster should have been contacted on more than 5% of the total
	 * number of days in the study ..
	 */
	private final int MIN_CONTACT_DAYS_THRESHOLD;
	private final int MIN_DURATION_THRESHOLD = 14;
	private final int DIST_THRESHOLD = 1609; // 1 MILE

	public Leader(int ndays, Hashtable<Integer, ArrayList<Integer>> nvor, Hashtable<Integer, List<Tower>> usrs_obs) {
		this.nvor = nvor;
		this.usrs_obs = usrs_obs;

		this.usrs_clusters = new Hashtable<>();

		MIN_CONTACT_DAYS_THRESHOLD = (int) (0.05 * ndays);
		// System.out.println("MIN_CONTACT_DAYS_THRESHOLD: " +
		// MIN_CONTACT_DAYS_THRESHOLD);
	}

	public Leader(int ndays, Hashtable<Integer, ArrayList<Integer>> nvor, Hashtable<Integer, Vertex> towers,
			Hashtable<Integer, List<Tower>> usrs_obs) {
		this.nvor = nvor;
		this.usrs_obs = usrs_obs;
		this.towers_coords = towers;
		this.usrs_clusters = new Hashtable<>();

		MIN_CONTACT_DAYS_THRESHOLD = (int) (0.05 * ndays);
		// System.out.println("MIN_CONTACT_DAYS_THRESHOLD: " +
		// MIN_CONTACT_DAYS_THRESHOLD);
	}

	/**
	 * Set the percentage of each element of the observable factors ..
	 */
	private List<TCluster> calc_observable_factors_percentage(List<TCluster> uClusters) {

		int t_days = 0;
		int t_tower_days = 0;
		int t_duration = 0;

		List<TCluster> t_uClusters = new ArrayList(uClusters.size());
		for (Iterator<TCluster> iterator = uClusters.iterator(); iterator.hasNext();) {
			TCluster t = iterator.next();
			t_days += t.get_days();
			t_tower_days += t.get_tower_days();
			t_duration += t.get_duration();

		}

		for (Iterator<TCluster> iterator = uClusters.iterator(); iterator.hasNext();) {
			TCluster t = iterator.next();
			t.set_factors_percent(t_days, t_tower_days, t_duration);
			t_uClusters.add(t);

		}
		return t_uClusters;

	}

	/**
	 * user cluster must have a chance of being important greater than 0.2
	 *
	 * @param uClusters
	 * @return
	 */
	private List<TCluster> check_cluster_importance(List<TCluster> uClusters) {
		List<TCluster> updated_uClusters = new ArrayList<>();
		for (Iterator<TCluster> iterator = uClusters.iterator(); iterator.hasNext();) {
			TCluster t = iterator.next();
			if (t.get_importance() >= 0.2) {
				updated_uClusters.add(t);
			}
		}
		if (updated_uClusters.size() > 2) {
			return updated_uClusters;
		} else {
			return uClusters;
		}
	}

	/**
	 * Check if the distance between the current tower and adjacent less than 1
	 * mile or not ..
	 *
	 * @param t
	 * @param t_ns
	 * @return
	 */
	private ArrayList<Integer> check_dist(int t, ArrayList<Integer> t_ns) {
		ArrayList<Integer> new_t_ns = new ArrayList<>();
		Vertex t_v = towers_coords.get(t);
		for (Iterator<Integer> iterator = t_ns.iterator(); iterator.hasNext();) {
			int cur_t = iterator.next();
			Vertex cur_t_v = towers_coords.get(cur_t);

			double dist = DataHandler.euclidean(t_v.getX(), t_v.getY(), cur_t_v.getX(), cur_t_v.getY());
			if (dist < DIST_THRESHOLD) {
				new_t_ns.add(cur_t);
			}
		}
		return new_t_ns;
	}

	private boolean get_towers(List<Tower> usr_obs, List<Integer> t_ns, List<Tower> exclude_list, List<Tower> towers) {

		for (Iterator<Tower> iterator = usr_obs.iterator(); iterator.hasNext();) {
			Tower t = iterator.next();
			// add the current tower if it is in the neighbor and didn't added
			// before to any cluster ..
			if (t_ns.contains(t.id)) {
				if (!exclude_list.contains(t)) {
					towers.add(t);
					// days += t.days();
				}
			}

		}

		if (!towers.isEmpty()) {
			// get the number of days ..
			TCluster tc = new TCluster(towers.get(0));
			for (int i = 1; i < towers.size(); i++) {
				Tower t = towers.get(i);
				tc.add_tower(t);
			}

			if (tc.get_days() > MIN_CONTACT_DAYS_THRESHOLD && tc.get_duration() > MIN_DURATION_THRESHOLD) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if a tower is an adjacent to any of the currently selected clusters
	 *
	 * @param t_id
	 * @param usr_clusters
	 * @return
	 */
	private boolean is_adjacent_centroid(int t_id, List<TCluster> usr_clusters) {
		for (Iterator<TCluster> iterator = usr_clusters.iterator(); iterator.hasNext();) {
			TCluster tc = iterator.next();
			ArrayList<Integer> t_ns = nvor.get(tc.centroid);
			if (t_ns.contains(t_id)) {
				return false;
			}

		}
		return true;

	}

	public Hashtable<Integer, List<TCluster>> parallel_scan() {

		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);

		List<Future<UPOI>> futures = new ArrayList<Future<UPOI>>();

		for (Map.Entry<Integer, List<Tower>> entrySet : usrs_obs.entrySet()) {
			int usr_key = entrySet.getKey();
			List<Tower> usr_obs_twr_list = entrySet.getValue();

			Callable<UPOI> callable = new Callable<UPOI>() {
				@Override
				public UPOI call() throws Exception {
					UPOI output = new UPOI();
					// process your input here and compute the output

					output.user_clusters = set_home_work_loc(
							check_cluster_importance(calc_observable_factors_percentage(scan_usr(usr_obs_twr_list))));
					output.id = usr_key;

					return output;
				}
			};
			futures.add(service.submit(callable));

			// calc percent of the observable factors of the scaned clusters per
			// user ..
			// cluster should have a higher than 20% of chance of being
			// important ..
		}

		service.shutdown();

		for (Future<UPOI> future : futures) {
			UPOI upoi;
			try {
				upoi = future.get();
				usrs_clusters.put(upoi.id, upoi.user_clusters);
			} catch (InterruptedException ex) {
				Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
			} catch (ExecutionException ex) {
				Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
		return usrs_clusters;
	}

	/**
	 * Cluster users CDRs using leader algorithm
	 *
	 * @return
	 */
	public Hashtable<Integer, List<TCluster>> scan() {
		for (Map.Entry<Integer, List<Tower>> entrySet : usrs_obs.entrySet()) {
			Integer usr_key = entrySet.getKey();
			List<Tower> usr_obs_twr_list = entrySet.getValue();
			// calc percent of the observable factors of the scaned clusters per
			// user ..
			List<TCluster> uClusters = set_home_work_loc(
					check_cluster_importance(calc_observable_factors_percentage(scan_usr(usr_obs_twr_list))));
			// cluster should have a higher than 20% of chance of being
			// important ..
			usrs_clusters.put(usr_key, uClusters);
		}

		return usrs_clusters;
	}

	/**
	 * Scan user clusters ..
	 *
	 * @param usr_obs
	 * @return
	 */
	private List<TCluster> scan_usr(List<Tower> usr_obs) {
		List<TCluster> uClusters = new ArrayList<>();
		List<Tower> added = new ArrayList<>();

		for (Iterator<Tower> iterator = usr_obs.iterator(); iterator.hasNext();) {
			Tower t = iterator.next();
			// cehck adjacency with previously added clusters of the current
			// user ...
			if (!is_adjacent_centroid(t.id, uClusters)) {
				continue;
			}
			// check distance threshold of the current cluster elements ...
			ArrayList<Integer> t_ns = check_dist(t.id, nvor.get(t.id));
			t_ns.add(t.id);

			List<Tower> towers = new ArrayList<>();

			// int days = get_towers(usr_obs, t_ns, added, towers);
			if (get_towers(usr_obs, t_ns, added, towers)) {
				TCluster cur = new TCluster(t);
				// remove from user observation to be added once to the clusters
				// list ..
				// concurrent conflict ..
				// usr_obs.removeAll(towers);
				added.addAll(towers);

				towers.remove(t);
				cur.add_towers(towers);

				uClusters.add(cur);
			}

		}

		return uClusters;
	}

	/**
	 *
	 * @param uClusters
	 * @return
	 */
	private List<TCluster> set_home_work_loc(List<TCluster> uClusters) {

		int[] h_l = new int[uClusters.size()];
		int[] w_l = new int[uClusters.size()];
		int h = -1, w = -1;
		int h_index = -1, w_index = -1;
		for (int i = 0; i < uClusters.size(); i++) {
			TCluster t = uClusters.get(i);
			h_l[i] = t.get_home_hour_events();
			w_l[i] = t.get_work_hour_events();

			if (h_l[i] > h && h_l[i] > w_l[i]) {
				h = h_l[i];
				h_index = i;
			}

			if (w_l[i] > w && w_l[i] > h_l[i]) {
				w = w_l[i];
				w_index = i;
			}
		}

		// set home ..
		TCluster t_h, temp_h = null, t_w, temp_w = null;
		if (h_index != -1) {
			t_h = uClusters.get(h_index);
			temp_h = uClusters.get(h_index);
			t_h.set_home_loc(true);
			uClusters.add(t_h);
		}
		if (w_index != -1) {
			t_w = uClusters.get(w_index);
			temp_w = uClusters.get(w_index);
			t_w.set_work_loc(true);
			uClusters.add(t_w);
		}
		// must be removed by object after retrieving require elements in order
		// not to modify indices ....
		uClusters.remove(temp_h);
		uClusters.remove(temp_w);

		return uClusters;
	}

}

class UPOI {

	int id;
	List<TCluster> user_clusters;
}
