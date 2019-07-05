// This file implements indexing for the observations, to exploit the fact that observations are sparse accross the states
package Viterbi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ObsIndex extends Hashtable<String, ArrayList<String>> {

	public ObsIndex() {
	}

	public ObsIndex(Map<? extends String, ? extends ArrayList<String>> t) {
		super(t);
	}

	public void Initialise(String[] states, // array of observations
			// Hashtable<String, StateInfo> state_lkp_table, // table of all
			// states and thier transistions
			Hashtable<String, Hashtable<String, Double>> emit_p) {// emit
																	// probabilities

		// StateInfo cr_state_info;
		// Iterate over all states,

		for (String state : states) {

			// get current state info
			// cr_state_info = state_lkp_table.get(state);
			// get all non zero possible emissions
			Hashtable<String, Double> emissions = emit_p.get(state); // get all
																		// emissions
																		// for
																		// current
																		// state
			if (emissions == null) {
				System.out.println("no emissions for state = " + state);
				continue;
			}
			for (String cr_emission : emissions.keySet()) {
				// System.out.println("cr_emission \t"+cr_emission);
				// if (cr_emission.equals("56")) {
				// System.out.println("cr_emission \t"+cr_emission);
				// }
				// check of the emission prob is not zero, if so add
				if (emissions.get(cr_emission) != 0) {
					// check if current ObsIndex doesn't include an entry for
					// the current emission
					if (!(containsKey(cr_emission))) {
						// make a new list of StateInfo
						ArrayList<String> al = new ArrayList<String>();
						al.add(state);
						put(cr_emission, al);
					} else {
						ArrayList<String> al = get(cr_emission);
						al.add(state);
					}
				}
			}

		}

	}

	public void Initialise(List<String> states, // array of observations
			// Hashtable<String, StateInfo> state_lkp_table, // table of all
			// states and thier transistions
			Hashtable<String, Hashtable<String, Double>> emit_p) {// emit
																	// probabilities

		// StateInfo cr_state_info;
		// Iterate over all states,
		states.parallelStream().forEach(state -> {

			// get current state info
			// cr_state_info = state_lkp_table.get(state);
			// get all non zero possible emissions
			Hashtable<String, Double> emissions = emit_p.get(state); // get all
																		// emissions
																		// for
																		// current
																		// state
			if (emissions != null) {
				for (String cr_emission : emissions.keySet()) {
					// System.out.println("cr_emission \t"+cr_emission);
					// if (cr_emission.equals("56")) {
					// System.out.println("cr_emission \t"+cr_emission);
					// }
					// check of the emission prob is not zero, if so add
					if (emissions.get(cr_emission) != 0) {
						// check if current ObsIndex doesn't include an entry
						// for
						// the current emission
						if (!(containsKey(cr_emission))) {
							// make a new list of StateInfo
							ArrayList<String> al = new ArrayList<String>();
							al.add(state);
							put(cr_emission, al);
						} else {
							ArrayList<String> al = get(cr_emission);
							al.add(state);
						}
					}
				}
			}
		});
		for (String state : states) {
		}

	}

}
