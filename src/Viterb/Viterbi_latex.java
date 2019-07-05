package Viterb;

import java.util.ArrayList;
import java.util.Hashtable;

import Viterbi.ObsIndex;
import Viterbi.StateInfoTimeStamp;
import Viterbi.TransNode;

public class Viterbi_latex {

	public String[] forward_viterbi_linkedlist_indexed(String[] obs, String[] states,
			Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfoTimeStamp> T, ObsIndex oi) {
		if (obs.length <= 1) {
			System.out.println("Error, obs can't be <=1");
			return new String[] { "-1", "", "-1" };
			System.exit(0);
		}
		// get all corresponding states
		ArrayList<String> substates = oi.get(obs[0]);
		if (substates == null || substates.isEmpty()) {
			return new String[] { "-1", "", "-1" };
		}

		for (String cr_state : substates) {
			double em = emit_p.get(cr_state).get(obs[0]);
			double v_prob = 1.0 / states.length * em;
			StateInfoTimeStamp cr_si = T.get(cr_state);
			boolean flag = cr_si.Write(0, cr_state, v_prob, count);
			if (!flag) {
				return new String[] { "-1", "", "-1" };
			}
			// System.out.println("initial value for state path ="+cr_state);
		}

		for (int i = 1; i < obs.length; i++) {
			String output = obs[i];
			substates = oi.get(output);
			if (substates == null) {
				return new String[] { "-1", "", "-1" };
			}
			int actual_count = 0;

			for (String cr_state : substates) {
				double em = emit_p.get(cr_state).get(output);
				double total = 0;
				String argmax = "";
				double valmax = 0;

				double prob = 1;
				String v_path = "";
				double v_prob = 1;
				StateInfoTimeStamp ns = T.get(cr_state);
				TransNode cn = ns.first_node;

				while (cn != null) {
					String source_state = cn.source_state;
					StateInfoTimeStamp src_si = T.get(source_state);
					StateInfoTimeStamp.ViterbiStateInfo v = src_si.Read(count);
					prob = v.prob;
					v_path = v.v_path;
					v_prob = v.v_prob;
					double p;
					double trans_prob = cn.trans_prob;
					try {
						p = em * trans_prob;// * v_prob;
					} catch (NullPointerException e) {
						p = 0;
					}
					v_prob *= p;
					if (v_prob > valmax) {
						argmax = v_path + "," + cr_state;
						valmax = v_prob;
						total = prob;
					}
					cn = cn.next_node;
				}
				boolean flag = ns.Write(total + 1, argmax, valmax, count);
				if (!flag) {
					return new String[] { "-1", "", "-1" };
				}

			}
		}

		double total = 0;
		String argmax = "";
		double valmax = 0;

		double prob;
		String v_path;
		double v_prob;

		substates = oi.get(obs[obs.length - 1]);

		for (String state : substates) {
			StateInfoTimeStamp si = T.get(state);
			StateInfoTimeStamp.ViterbiStateInfo v = si.Read(count);
			prob = v.prob;
			v_path = v.v_path;
			v_prob = v.v_prob;
			total += prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}

		return new String[] { Double.toString(total), argmax, Double.toString(valmax) };
	}

}
