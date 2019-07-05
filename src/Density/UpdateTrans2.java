/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import Observations.Obs;
import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransBuilder;
import Viterbi.Viterbi;
import utils.DataHandler;
import utils.FromNode;

class SerialDVA {

	int dayKey;
	Viterbi viterbi;
	int[][] densityMap;
	List<String> exts;
	Hashtable<Integer, Obs> obsTable;
	String[] states;
	Hashtable<String, Hashtable<String, Double>> emit_p;
	Hashtable<String, StateInfoTimeStamp> si;
	ObsIndex oi;
	ArrayList<String> obs;
	Hashtable<String, Double> start;
	final String rlm = "/";

	public SerialDVA(int dayKey, List<String> exts, Hashtable<Integer, Obs> obsTable,
			Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfoTimeStamp> si, ObsIndex oi,
			Hashtable<String, Double> start) {
		this.dayKey = dayKey;
		this.exts = exts;
		this.obsTable = obsTable;
		this.emit_p = emit_p;
		this.start = start;
		this.si = si;
		this.oi = oi;
		obs = new ArrayList<>();

	}

	public Hashtable<String, Hashtable<String, Double>> calcDayDensity() {

		// Sysjavatem.out.println("Day \t" + dayKey);
		int obsCounter = 0;
		states = exts.toArray(new String[exts.size()]);
		densityMap = new int[exts.size()][exts.size()];
		viterbi = new Viterbi();

//		obsTable.entrySet().parallelStream().forEach(o -> {
//			Integer key = o.getKey();
//			Obs obseq = o.getValue();
//			String seq = obseq.getSeq();
//			String vitPath = computeVit(seq, si, oi);
//			// if (!vitPath.isEmpty()) {
//			// obsCounter++;
//			// }
//			obseq.setVitPath(vitPath);
//			obsTable.replace(key, obseq);
//		});
		/**
		 * initialize all sequences of observations
		 */
		 for (Map.Entry<Integer, Obs> entrySet : obsTable.entrySet()) {
		 Integer key = entrySet.getKey();
		 Obs obseq = entrySet.getValue();
		 String seq = obseq.getSeq();
		 String vitPath = computeVit(seq, si, oi);
		 if (!vitPath.isEmpty()) {
		 obsCounter++;
		 }
		 obseq.setVitPath(vitPath);
		 obsTable.replace(key, obseq);
		 }

		System.out.printf("Day %s has ->>> %d\t Viterbi paths out of %d\t obs ", dayKey, obsCounter, obsTable.size());

		return convertDensity();

	}

	public String computeVit(String seq, Hashtable<String, StateInfoTimeStamp> si, ObsIndex oi) {
		String vitPath = "";
		if (seq.contains(rlm)) {
			String[] tmp = seq.split(rlm);
			for (int i = 0; i < tmp.length; i++) {
				String[] usrObs = tmp[i].split(",");
				if (usrObs.length > 1) {
					Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs, states, emit_p, si, oi);
					String vit_path = (String) ret[1];

					if (vit_path == null) {
						vit_path = "";
					}
					/**
					 * If the Viterbi path is not empty add observations
					 * seperator.
					 */
					if (!vitPath.isEmpty()) {
						vitPath += rlm;
					}
					vitPath += vit_path;
					/**
					 * If viterbi calc path, update the density map and
					 * increment paths counter.
					 */
					if (!vit_path.isEmpty()) {
						updateDenisty(vit_path);

					}
				}

			}
		} else {
			String[] usrObs = seq.split(",");
			if (usrObs.length > 1) {
				Object[] ret = viterbi.forward_viterbi_linkedlist_indexed(usrObs, states, emit_p, si, oi);
				String vit_path = (String) ret[1];

				if (vit_path == null) {
					vit_path = "";
				}
				/**
				 * If the Viterbi path is not empty add observations seperator.
				 */
				if (!vitPath.isEmpty()) {
					vitPath += rlm;
				}
				vitPath += vit_path;
				/**
				 * If viterbi calc path, update the density map and increment
				 * paths counter.
				 */
				if (!vit_path.isEmpty()) {
					updateDenisty(vit_path);

				}
			}
		}
		return vitPath;
	}

	/**
	 *
	 * @return converted array[][] into Hashtable<String, Hashtable<String,
	 *         Double>>
	 */
	private Hashtable<String, Hashtable<String, Double>> convertDensity() {

		Hashtable<String, Hashtable<String, Double>> denseTable = new Hashtable<>();
		for (int i = 0; i < densityMap.length; i++) {
			int[] dens = densityMap[i];
			int sum = IntStream.of(dens).sum();
			if (sum > 0) {
				Hashtable<String, Double> toTab = new Hashtable<>();
				for (int j = 0; j < dens.length; j++) {
					int den = dens[j];
					toTab.put(exts.get(j), (double) den);

				}
				denseTable.put(exts.get(i), toTab);
			}

		}
		System.out.println("Writing Density Map");
		return denseTable;
	}

	public void updateDenisty(String vit_path) {
		String[] out = vit_path.split(",");
		for (int k = 0; k < out.length - 1; k++) {
			if (exts.contains(out[k]) && exts.contains(out[k + 1])) {
				densityMap[exts.indexOf(out[k])][exts.indexOf(out[k + 1])] += 1;
			}

		}
	}
}

/**
 *
 * @author essam
 */
public class UpdateTrans2 {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String vor = args[0];
		String probXml = args[1];
		String obsPath = args[2];
		String transPath = args[3];
		String emissionPath = args[4];
		String vorNeighborsPath = args[5];
		String mapRoutesPath = args[6];
		String image = args[7];
		String edgesPath = args[8];
		String towersPath = args[9];
		String density_dir = args[10];
		double alpha = Double.parseDouble(args[11]);
		double beta = Double.parseDouble(args[12]);
		int index = Integer.parseInt(args[13]);

		int threshold = 1000;

		DataHandler adaptor = new DataHandler();
		ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
		List<String> states = adaptor.getExts();
		TransBuilder tbuilder = new TransBuilder(map, vorNeighborsPath);
		Hashtable<String, Hashtable<String, Double>> trans_p
		// = tbuilder.getTransitionProb(threshold, exts, towersPath);
				= adaptor.readProbXMLTable(transPath);
		// System.out.println("Writing transitions");
		// adaptor.writeXmlTable(trans_p, transPath);
		// String[] states = exts.toArray(new String[exts.size()]);

		Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
		// = tbuilder.getEmissionProb(exts);
		// emit_p = tbuilder.emitNeighbors(emit_p);
		// System.out.println("Writing emissions");
		// adaptor.writeXmlTable(emit_p, emissionPath);
		Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obsPath);
		/**
		 * Sort days
		 */
		List<String> keyDaysList = new ArrayList<String>(obsTable.keySet());
		Collections.sort(keyDaysList);

		System.out.println("observation table" + obsTable.size());
		Hashtable<String, Double> start = adaptor.getStartProb(trans_p);

		ObsIndex oi = new ObsIndex();
		oi.Initialise(states, emit_p);

		// Viterbi viterbi = new Viterbi();
		// Hashtable<String, StateInfoTimeStamp> si =
		// viterbi.trans_mat_to_list_indexed(states, start, trans_p);

		SerialDVA sdva;
		for (String key : keyDaysList) {
			// for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet :
			// obsTable.entrySet()) {
			// String key = entrySet.getKey();
			/**
			 * if this day is a weekend, do not use in the transitions updates
			 */
			if (key.contains("Saturday") || key.contains("Sunday")) {
				System.out.println("#-#\t" + key);
				continue;
			} else {
				System.out.println("#+#\t" + key);

			}
			int dayid = index;
			Hashtable<Integer, Obs> dayObs = obsTable.get(key);
			Viterbi viterbi = new Viterbi();
			Hashtable<String, StateInfoTimeStamp> si = viterbi.trans_mat_to_list_indexed(states, start, trans_p);
			sdva = new SerialDVA(dayid, states, dayObs, emit_p, si, oi, start);
			Hashtable<String, Hashtable<String, Double>> density = sdva.calcDayDensity();
			Hashtable<String, Hashtable<String, Double>> transitions = TransBuilder.updateTrans(trans_p, density, alpha,
					beta);

			// if (trans_p.equals(transitions)) {
			// System.out.println("They are equals");
			// }
			/**
			 * Save files
			 */
			String upath = density_dir + "/transition.day." + String.format("%02d", index) + ".xml";
			String densityPath = density_dir + "/density.day." + String.format("%02d", index) + ".xml";

			Density.writeDensity(density, densityPath);
			adaptor.writeXmlTable(transitions, upath);

			/**
			 * Get difference between current transitions probability and the
			 * updated one.
			 *
			 * Write then read as when i compare the transitions tables after
			 * calculations I found that both the updated version and the old
			 * one are identical to each other.
			 */
			String path = density_dir + "/transition.day." + String.format("%02d", index - 1) + ".xml";
			// Hashtable<String, Hashtable<String, Double>> trans_prob =
			// adaptor.readProbXMLTable(path);
			// Hashtable<String, Hashtable<String, Double>> utrans_prob =
			// adaptor.readProbXMLTable(upath);

			// System.out.println(path+"\t"+ upath+"\t" +
			// TransBuilder.getError(trans_p, utrans_p));
			// System.out.println(TransBuilder.getError(trans_prob,
			// utrans_prob));

			/**
			 * Update the old transitions with the new transitions.
			 */
			trans_p = transitions;
			index++;
		}
	}

}
