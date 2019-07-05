package Density;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import validation.GravityModel;

public class RoadExtsPerTrip {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String vit_data = args[0];
		String output_stat = args[1];
		String output_desc_stat = args[2];
		String output_vals = args[3];

		List<String> trips = new ArrayList<>();
		Hashtable<String, List<Integer>> stat = new Hashtable<>();

		ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vit_data));
		for (Iterator iterator = files.iterator(); iterator.hasNext();) {
			String vit_day = (String) iterator.next();

			if (!vit_day.contains(".viterbi.day"))
				continue;

			Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vit_day);

			for (Map.Entry<String, Hashtable<Integer, Obs>> obs_entry : obs.entrySet()) {
				System.out.println(obs_entry.getKey());
				Hashtable<Integer, Obs> day_obs = obs_entry.getValue();
				for (Map.Entry<Integer, Obs> u_obs : day_obs.entrySet()) {
					Obs O = u_obs.getValue();
					String seq = O.getSeq();
					String vit = O.getVitPath();
					// skip undecoded routes
					if (vit.equals("-"))
						continue;

					// remove interpolation
					if (vit.contains("_interpolated_")) {
						vit = vit.replaceAll("_interpolated_\\[[0-9]+\\]", "");
					}
					// remove edges on the same road
					if (vit.contains("#")) {
						vit = vit.replaceAll("#[0-9]+", "");
					}

					String[] s_arr = seq.split("/");
					String[] v_arr = vit.split("/");
					for (int i = 0; i < v_arr.length; i++) {
						String t = v_arr[i];

						String t_obs[] = s_arr[i].split(",");

						if (Arrays.stream(t_obs).distinct().count() != 1) {
							trips.add(t);
							List<String> segs = Arrays.asList(t.split(","));
							Set<String> uniqueSet = new HashSet<String>(segs);
							for (String temp : uniqueSet) {
								// if (Collections.frequency(segs, temp) > 10)
								// continue;

								if (stat.containsKey(temp)) {
									List<Integer> freq = stat.get(temp);
									freq.add(Collections.frequency(segs, temp));
									stat.replace(temp, freq);
								} else {
									List<Integer> freq = new ArrayList<>();
									freq.add(Collections.frequency(segs, temp));
									stat.put(temp, freq);
								}
							}
						}
					}

				}
			}

		}

		GravityModel gm = new GravityModel();

		DescriptiveStatistics stats;

		String rec = "Road, min, max, freq per trips, mean, stddev, variance";
		String drec = "Road, min, max,freq per trips, mean, stddev, variance,skewness,kurtosis";
		String svals = "";
		for (Map.Entry<String, List<Integer>> stat_entry : stat.entrySet()) {
			
			// double[] vals = stat_entry.getValue().stream().mapToDouble(i ->
			// i).toArray();
			List<Integer> values = stat_entry.getValue();
			Collections.sort(values);
			int size = (int) Math.round(values.size() * 0.9);
			double[] vals = new double[size];
			stats = new DescriptiveStatistics();

			for (int j = 0; j < vals.length; j++) {
				vals[j] = values.get(j);
				stats.addValue(vals[j]);
			}

			double upper = stats.getPercentile(75);

			String road = stat_entry.getKey();
			double count = stat_entry.getValue().size();
			drec += "\n" + road + "," + stats.getMin() + "," + stats.getMax() + "," + count + "," + stats.getMean()
					+ "," + stats.getStandardDeviation() + "," + stats.getVariance() + "," + stats.getSkewness() + ","
					+ stats.getKurtosis();

			double min = gm.min(vals);
			double max = gm.max(vals);
			double mean = gm.mean(vals);
			double stddev = gm.stddev(vals);
			double var = gm.var(vals);

			if (Double.isNaN(stddev) || Double.isNaN(var))
				continue;
			rec += "\n" + road + "," + min + "," + max + "," + count + "," + mean + "," + stddev + "," + var;
			svals = road + "\t" + Arrays.toString(stat_entry.getValue().toArray()) + "\n";
			// System.out.println(stat_entry.getKey() + "\t" +
			// Arrays.toString(stat_entry.getValue().toArray()));
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(output_stat));
			writer.write(rec);

			writer = new BufferedWriter(new FileWriter(output_desc_stat));
			writer.write(drec);

			writer = new BufferedWriter(new FileWriter(output_vals));
			writer.write(svals);
		} catch (IOException e) {
		}
		// System.out.println("The number of trips is:\t" + trips.size());

	}

}
