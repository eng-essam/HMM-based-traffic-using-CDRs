/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author essam
 */
public class Viterbi {

	/**
	 * Calculate the Viterbi path for a given observation.
	 *
	 * @param obs
	 * @param states
	 * @param start_p
	 * @param trans_p
	 * @param emit_p
	 * @return
	 */
	public static Object[] forward_viterbi(String[] obs, String[] states, Hashtable<String, Double> start_p,
			Hashtable<String, Hashtable<String, Double>> trans_p, Hashtable<String, Hashtable<String, Double>> emit_p) {

		Hashtable<String, Object[]> T = new Hashtable<String, Object[]>();
		for (String state : states) {
			T.put(state, new Object[] { start_p.get(state), state, start_p.get(state) });
		}
		long count = 0;
		long sec_count = 0;
		long inner_count = 0;
		for (String output : obs) {
			Hashtable<String, Object[]> U = new Hashtable<String, Object[]>();

			for (String next_state : states) {
				sec_count++;
				double total = 0;
				String argmax = "";
				double valmax = 0;

				double prob = 1;
				String v_path = "";
				double v_prob = 1;
				for (String source_state : states) {
					inner_count++;
					Object[] objs = T.get(source_state);
					prob = ((Double) objs[0]).doubleValue();
					v_path = (String) objs[1];
					v_prob = ((Double) objs[2]).doubleValue();

					double p = 0;
					// double em = emit_p.get(source_state).get(output);
					// double trns =trans_p.get(source_state).get(next_state);

					// try {
					p = emit_p.get(source_state).get(output) * trans_p.get(source_state).get(next_state);
					double trans = trans_p.get(source_state).get(next_state);
					if (trans == 0) {
						count++;
					}
					// } catch (NullPointerException e) {
					//// System.out.println(" ** Null **");
					//// System.out.println("emission\t " +
					// emit_p.get(source_state));
					//// System.out.println("zone\t " +
					// emit_p.get(source_state).get(output));
					//// return new Object[]{total, argmax, valmax};
					// continue;
					// }

					prob *= p;
					v_prob *= p;
					total += prob;
					if (v_prob > valmax) {
						argmax = v_path + "," + next_state;
						valmax = v_prob;
					}
				}

				U.put(next_state, new Object[] { total, argmax, valmax });
			}
			T = U;
		}

		double total = 0;
		String argmax = "";
		double valmax = 0;

		double prob;
		String v_path;
		double v_prob;

		for (String state : states) {
			Object[] objs = T.get(state);
			prob = ((Double) objs[0]).doubleValue();
			v_path = (String) objs[1];
			v_prob = ((Double) objs[2]).doubleValue();
			total += prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}
		// System.out.println("States:\t" + states.length);
		// System.out.println("Second loop counter:\t" + sec_count);
		//
		// System.out.println("Zeros:\t" + count);
		// System.out.println("Non-Zeros:\t" + (inner_count - count));
		return new Object[] { total, argmax, valmax };
	}

	/**
	 * object oriented variable, not class
	 */
	int count = 0;

	public Object[] forward_vit_subset(Hashtable<String, ArrayList<String>> mapt2f, String[] obs, String[] states,
			Hashtable<String, Double> start_p, Hashtable<String, Hashtable<String, Double>> trans_p,
			Hashtable<String, Hashtable<String, Double>> emit_p) {
		final long stime = System.currentTimeMillis();

		// Initialise the states with start probabilities
		Hashtable<String, Object[]> T = new Hashtable<String, Object[]>();
		for (String state : states) {
			T.put(state, new Object[] { start_p.get(state), state, start_p.get(state) });
		}
		// long count = 0;
		// long sec_count = 0;
		// long inner_count = 0;
		for (String output : obs) {
			Hashtable<String, Object[]> U = new Hashtable<String, Object[]>();

			for (String next_state : states) {
				// sec_count++;
				double total = 0;
				String argmax = "";
				double valmax = 0;

				double prob = 1;
				String v_path = "";
				double v_prob = 1;
				ArrayList<String> subset = mapt2f.get(next_state);
				// if (subset==null || subset.isEmpty()) {
				// System.out.println(" null subset");
				// continue;
				// }
				String[] sub_states = subset.toArray(new String[subset.size()]);
				// System.out.println("sub_states \t"+sub_states.length);
				for (String source_state : sub_states) {
					// inner_count++;
					// if (!T.containsKey(source_state)) {
					// continue;
					// }
					Object[] objs = T.get(source_state);
					prob = ((Double) objs[0]).doubleValue();
					v_path = (String) objs[1];
					v_prob = ((Double) objs[2]).doubleValue();

					double p = 0;
					// double em = emit_p.get(source_state).get(output);
					// double trns =trans_p.get(source_state).get(next_state);

					// try {
					p = emit_p.get(source_state).get(output) * trans_p.get(source_state).get(next_state);
					double trans = trans_p.get(source_state).get(next_state);
					// if (trans == 0) {
					// count++;
					// }
					// } catch (NullPointerException e) {
					// System.out.println(" ** Null **");
					// System.out.println("emission\t " +
					// emit_p.get(source_state));
					// System.out.println("zone\t " +
					// emit_p.get(source_state).get(output));
					// continue;
					// }

					prob *= p;
					v_prob *= p;
					total += prob;
					if (v_prob > valmax) {
						argmax = v_path + "," + next_state;
						valmax = v_prob;
					}
				}

				U.put(next_state, new Object[] { total, argmax, valmax });
			}
			T = U;
		}

		double total = 0;
		String argmax = "";
		double valmax = 0;

		double prob;
		String v_path;
		double v_prob;

		for (String state : states) {
			Object[] objs = T.get(state);
			prob = ((Double) objs[0]).doubleValue();
			v_path = (String) objs[1];
			v_prob = ((Double) objs[2]).doubleValue();
			total += prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}
		// System.out.println("States:\t" + states.length);
		// System.out.println("Second loop counter:\t" + sec_count);
		//
		// System.out.println("Zeros:\t" + count);
		// System.out.println("Non-Zeros:\t" + (inner_count - count));
		final long etime = System.currentTimeMillis();
		System.out.println("Viterbi Finished in\t" + (etime - stime));
		return new Object[] { total, argmax, valmax };
	}

	public Object[] forward_viterbi_linkedlist(String[] obs, String[] states,
			// Hashtable<String, Double> start_p,
			// Hashtable<String, Hashtable<String, Double>> trans_p,
			Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfo> T) {

		// Hashtable<String, StateInfo> T = new Hashtable<String, Object[]>();
		// for (String state : states) {
		// T.put(state, new Object[]{start_p.get(state), state,
		// start_p.get(state)});
		// }
		final long stime = System.currentTimeMillis();

		Hashtable<String, StateInfo> Tnew = new Hashtable<String, StateInfo>();
		Hashtable<String, StateInfo> Ttmp;
		for (String output : obs) {
			// Hashtable<String, Object[]> U = new Hashtable<String,
			// Object[]>();

			for (String next_state : states) {
				double total = 0;
				String argmax = "";
				double valmax = 0;

				double prob = 1;
				String v_path = "";
				double v_prob = 1;
				StateInfo ns = T.get(next_state);
				TransNode cn = ns.first_node;

				while (cn != null) {
					// System.out.println("cn.source_state="+cn.source_state+"\n");
					String source_state = cn.source_state;
					// for (String source_state : states) {
					// Object[] objs = T.get(source_state);
					StateInfo src_si = T.get(source_state);
					prob = src_si.prob;
					v_path = src_si.v_path;
					v_prob = src_si.v_prob;

					// prob = ((Double) objs[0]).doubleValue();
					// v_path = (String) objs[1];
					// v_prob = ((Double) objs[2]).doubleValue();
					double p = 0;
					// double em = emit_p.get(source_state).get(output);
					// double trns =trans_p.get(source_state).get(next_state);
					double trans_prob = cn.trans_prob;
					try {
						// double em = emit_p.get(source_state).get(output);
						p = emit_p.get(source_state).get(output) * trans_prob;

						// if(em!=0)
						// System.out.println("emission prob = "+em+ "trans =
						// "+trans_prob);
						// * trans_p.get(source_state).get(next_state);
					} catch (NullPointerException e) {
						// System.out.println(" ** Null **");
						// System.out.println("emission\t
						// "+emit_p.get(source_state));
						// System.out.println("zone\t
						// "+emit_p.get(source_state).get(output));
						p = 0;
					}

					// if(p!=0)
					// System.out.println("prob = "+prob);
					prob *= p;
					v_prob *= p;
					total += prob;
					if (v_prob > valmax) {
						argmax = v_path + "," + next_state;
						valmax = v_prob;
					}
					cn = cn.next_node;
				}
				// prob = si.prob;
				// v_path = si.vpath;
				// v_prob = si.v_prob;

				// ns.prob = total;
				// ns.v_path = argmax;
				// ns.v_prob = valmax;
				Tnew.put(next_state, new StateInfo(total, argmax, valmax, ns.first_node));
				// U.put(next_state, new Object[]{total, argmax, valmax});

			}
			Ttmp = Tnew;
			Tnew = T;
			T = Ttmp;

			// T = U;
		}

		double total = 0;
		String argmax = "";
		double valmax = 0;

		double prob;
		String v_path;
		double v_prob;

		for (String state : states) {
			// Object[] objs = T.get(state);
			StateInfo si = T.get(state);
			prob = si.prob;
			v_path = si.v_path;
			v_prob = si.v_prob;

			// prob = ((Double) objs[0]).doubleValue();
			// v_path = (String) objs[1];
			// v_prob = ((Double) objs[2]).doubleValue();
			total += prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}

		final long etime = System.currentTimeMillis();
		System.out.println("Finished  viterbi list in " + (etime - stime));
		return new Object[] { total, argmax, valmax };
	}

	/**
	 *
	 * @param obs
	 * @param states
	 * @param emit_p
	 * @param T
	 * @param oi
	 * @return
	 */
	public String[] forward_viterbi_linkedlist_indexed(String[] obs, String[] states,
			// Hashtable<String, Double> start_p,
			// Hashtable<String, Hashtable<String, Double>> trans_p,
			Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfoTimeStamp> T, ObsIndex oi) {

		// Hashtable<String, StateInfo> T = new Hashtable<String, Object[]>();
		// for (String state : states) {
		// T.put(state, new Object[]{start_p.get(state), state,
		// start_p.get(state)});
		// }
		final long stime = System.currentTimeMillis();

		// System.out.println("count=" + count);
		// Initialise all first states
		if (obs.length <= 1) {
			System.out.println("Error, obs can't be <=1");
			return new String[] { "-1", "", "-1" };
			// System.exit(0);
		}
		// get all corresponding states
		ArrayList<String> substates = oi.get(obs[0]);
		if (substates == null || substates.isEmpty()) {
			// System.out.println("output\t" + obs[0]);
			return new String[] { "-1", "", "-1" };
			// continue;
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
		count++;

		for (int i = 1; i < obs.length; i++) {
			String output = obs[i];
			// System.out.println("handling obs = " + output + "id =" + i);

			// Hashtable<String, Object[]> U = new Hashtable<String,
			// Object[]>();
			// problem with previous states... needs to be at individual basis?
			substates = oi.get(output);
			if (substates == null) {
				// System.out.println("output\t" + obs[0]);
				return new String[] { "-1", "", "-1" };
				// continue;
			}
			// System.out.println("size of substates = " + substates.size());
			int actual_count = 0;

			// try {
			// String st = substates.get(0);
			// } catch (Exception e) {
			// System.out.println("substates size:\t" + substates.size());
			// System.out.println("Current Observation:\t" + output);
			// System.out.println("Previous observation:\t" + obs[i - 1]);
			// }
			// try {
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
					// System.out.println("cn.source_state="+cn.source_state+"\n");
					String source_state = cn.source_state;
					// for (String source_state : states) {
					// Object[] objs = T.get(source_state);
					StateInfoTimeStamp src_si = T.get(source_state);
					StateInfoTimeStamp.ViterbiStateInfo v = src_si.Read(count);
					prob = v.prob;
					v_path = v.v_path;
					v_prob = v.v_prob;

					// System.out.println("reading prob="+prob+"v_path
					// ="+"v_prob"+v_prob);
					// prob = ((Double) objs[0]).doubleValue();
					// v_path = (String) objs[1];
					// v_prob = ((Double) objs[2]).doubleValue();
					double p;// = 0;
					// double em = emit_p.get(source_state).get(output);
					// double trns =trans_p.get(source_state).get(next_state);
					double trans_prob = cn.trans_prob;
					try {
						// double em = emit_p.get(source_state).get(output);
						// if(em!=0)
						// actual_count++;
						p = em * trans_prob;// * v_prob;

						// if(em!=0)
						// System.out.println("emission prob = "+em+ "trans =
						// "+trans_prob+"v_prob="+v_prob);
						// * trans_p.get(source_state).get(next_state);
					} catch (NullPointerException e) {
						// System.out.println(" ** Null **");
						// System.out.println("emission\t
						// "+emit_p.get(source_state));
						// System.out.println("zone\t
						// "+emit_p.get(source_state).get(output));
						p = 0;
					}

					// if(p!=0)
					// System.out.println("prob = "+prob);
					// prob *= p;
					v_prob *= p;
					// if (v_prob != 0) {
					//
					// if ((prob + 1) != count) {
					// System.out.println("prob = " + prob + "count=" + count +
					// "written ts=" + v.ts);
					//// System.exit(0);
					// return new Object[]{0.0, "", 0.0};
					// }
					// }
					// total += prob;
					if (v_prob > valmax) {
						argmax = v_path + "," + cr_state;
						valmax = v_prob;
						total = prob;
					}
					cn = cn.next_node;
				}
				// prob = si.prob;
				// v_path = si.vpath;
				// v_prob = si.v_prob;

				// ns.prob = total;
				// ns.v_path = argmax;
				// ns.v_prob = valmax;
				// System.out.println("Writing for state = " + cr_state + "total
				// =" + total + "argmax = " + argmax + " valmax = " + valmax);
				boolean flag = ns.Write(total + 1, argmax, valmax, count);
				if (!flag) {
					return new String[] { "-1", "", "-1" };
				}
				// Tnew.put(next_state, new StateInfo(total, argmax, valmax,
				// ns.first_node));
				// U.put(next_state, new Object[]{total, argmax, valmax});

			}
			// } catch (NullPointerException e) {
			// System.out.println("substates size:\t" + substates.size());
			// System.out.println("Current Observation:\t" + output);
			// System.out.println("Previous observation:\t" + obs[i - 1]);
			// }
			count++;
			// System.out.println("actual = "+actual_count);
			// Ttmp = Tnew;
			// Tnew = T;
			// T = Ttmp;

			// T = U;
		}

		double total = 0;
		String argmax = "";
		double valmax = 0;

		double prob;
		String v_path;
		double v_prob;

		substates = oi.get(obs[obs.length - 1]);

		for (String state : substates) {
			// Object[] objs = T.get(state);
			StateInfoTimeStamp si = T.get(state);
			StateInfoTimeStamp.ViterbiStateInfo v = si.Read(count);
			prob = v.prob;
			v_path = v.v_path;
			v_prob = v.v_prob;

			// System.out.println("v_prob:\t"+v_prob+"\tprob\t"+prob+"\tv_path:\t"+v_path);
			// System.out.println(",\""+v_path+"\"");
			// prob = ((Double) objs[0]).doubleValue();
			// v_path = (String) objs[1];
			// v_prob = ((Double) objs[2]).doubleValue();
			total += prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}

		final long etime = System.currentTimeMillis();
		// System.out.println("Finished viterbi list indexed in " + (etime -
		// stime)+" total ="+total+"valmax="+valmax);
		return new String[] { Double.toString(total), argmax, Double.toString(valmax) };
	}

	/**
	 *
	 * @param obs
	 * @param states
	 * @param emit_p
	 * @param T
	 * @param oi
	 * @return
	 */
	public String[] forward_viterbi_linkedlist_indexed(String[] obs, List<String> states,
			// Hashtable<String, Double> start_p,
			// Hashtable<String, Hashtable<String, Double>> trans_p,
			Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfoTimeStamp> T, ObsIndex oi) {

		// Hashtable<String, StateInfo> T = new Hashtable<String, Object[]>();
		// for (String state : states) {
		// T.put(state, new Object[]{start_p.get(state), state,
		// start_p.get(state)});
		// }
		final long stime = System.currentTimeMillis();

		// System.out.println("count=" + count);
		// Initialise all first states
		if (obs.length <= 1) {
			System.out.println("Error, obs can't be <=1");
			return new String[] { "-1", "", "-1" };
			// System.exit(0);
		}
		// get all corresponding states
		ArrayList<String> substates = oi.get(obs[0]);
		if (substates == null || substates.isEmpty()) {
			// System.out.println("output\t" + obs[0]);
			return new String[] { "-1", "", "-1" };
			// continue;
		}

		for (String cr_state : substates) {
			double em = emit_p.get(cr_state).get(obs[0]);
			double v_prob = 1.0 / states.size() * em;
			StateInfoTimeStamp cr_si = T.get(cr_state);
			boolean flag = cr_si.Write(0, cr_state, v_prob, count);
			if (!flag) {
				return new String[] { "-1", "", "-1" };
			}
			// System.out.println("initial value for state path ="+cr_state);
		}
		count++;

		for (int i = 1; i < obs.length; i++) {
			String output = obs[i];
			// System.out.println("handling obs = " + output + "id =" + i);

			// Hashtable<String, Object[]> U = new Hashtable<String,
			// Object[]>();
			// problem with previous states... needs to be at individual basis?
			substates = oi.get(output);
			if (substates == null) {
				// System.out.println("output\t" + obs[0]);
				return new String[] { "-1", "", "-1" };
				// continue;
			}
			// System.out.println("size of substates = " + substates.size());
			int actual_count = 0;

			// try {
			// String st = substates.get(0);
			// } catch (Exception e) {
			// System.out.println("substates size:\t" + substates.size());
			// System.out.println("Current Observation:\t" + output);
			// System.out.println("Previous observation:\t" + obs[i - 1]);
			// }
			// try {
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
					// System.out.println("cn.source_state="+cn.source_state+"\n");
					String source_state = cn.source_state;
					// for (String source_state : states) {
					// Object[] objs = T.get(source_state);
					StateInfoTimeStamp src_si = T.get(source_state);
					StateInfoTimeStamp.ViterbiStateInfo v = src_si.Read(count);
					prob = v.prob;
					v_path = v.v_path;
					v_prob = v.v_prob;

					// System.out.println("reading prob="+prob+"v_path
					// ="+"v_prob"+v_prob);
					// prob = ((Double) objs[0]).doubleValue();
					// v_path = (String) objs[1];
					// v_prob = ((Double) objs[2]).doubleValue();
					double p;// = 0;
					// double em = emit_p.get(source_state).get(output);
					// double trns =trans_p.get(source_state).get(next_state);
					double trans_prob = cn.trans_prob;
					try {
						// double em = emit_p.get(source_state).get(output);
						// if(em!=0)
						// actual_count++;
						p = em * trans_prob;// * v_prob;

						// if(em!=0)
						// System.out.println("emission prob = "+em+ "trans =
						// "+trans_prob+"v_prob="+v_prob);
						// * trans_p.get(source_state).get(next_state);
					} catch (NullPointerException e) {
						// System.out.println(" ** Null **");
						// System.out.println("emission\t
						// "+emit_p.get(source_state));
						// System.out.println("zone\t
						// "+emit_p.get(source_state).get(output));
						p = 0;
					}

					// if(p!=0)
					// System.out.println("prob = "+prob);
					// prob *= p;
					v_prob *= p;
					// if (v_prob != 0) {
					//
					// if ((prob + 1) != count) {
					// System.out.println("prob = " + prob + "count=" + count +
					// "written ts=" + v.ts);
					//// System.exit(0);
					// return new Object[]{0.0, "", 0.0};
					// }
					// }
					// total += prob;
					if (v_prob > valmax) {
						argmax = v_path + "," + cr_state;
						valmax = v_prob;
						total = prob;
					}
					cn = cn.next_node;
				}
				// prob = si.prob;
				// v_path = si.vpath;
				// v_prob = si.v_prob;

				// ns.prob = total;
				// ns.v_path = argmax;
				// ns.v_prob = valmax;
				// System.out.println("Writing for state = " + cr_state + "total
				// =" + total + "argmax = " + argmax + " valmax = " + valmax);
				boolean flag = ns.Write(total + 1, argmax, valmax, count);
				if (!flag) {
					return new String[] { "-1", "", "-1" };
				}
				// Tnew.put(next_state, new StateInfo(total, argmax, valmax,
				// ns.first_node));
				// U.put(next_state, new Object[]{total, argmax, valmax});

			}
			// } catch (NullPointerException e) {
			// System.out.println("substates size:\t" + substates.size());
			// System.out.println("Current Observation:\t" + output);
			// System.out.println("Previous observation:\t" + obs[i - 1]);
			// }
			count++;
			// System.out.println("actual = "+actual_count);
			// Ttmp = Tnew;
			// Tnew = T;
			// T = Ttmp;

			// T = U;
		}

		double total = 0;
		String argmax = "";
		double valmax = 0;

		double prob;
		String v_path;
		double v_prob;

		substates = oi.get(obs[obs.length - 1]);

		for (String state : substates) {
			// Object[] objs = T.get(state);
			StateInfoTimeStamp si = T.get(state);
			StateInfoTimeStamp.ViterbiStateInfo v = si.Read(count);
			prob = v.prob;
			v_path = v.v_path;
			v_prob = v.v_prob;

			// System.out.println("v_prob:\t"+v_prob+"\tprob\t"+prob+"\tv_path:\t"+v_path);
			// System.out.println(",\""+v_path+"\"");
			// prob = ((Double) objs[0]).doubleValue();
			// v_path = (String) objs[1];
			// v_prob = ((Double) objs[2]).doubleValue();
			total += prob;
			if (v_prob > valmax) {
				argmax = v_path;
				valmax = v_prob;
			}
		}

		final long etime = System.currentTimeMillis();
		// System.out.println("Finished viterbi list indexed in " + (etime -
		// stime)+" total ="+total+"valmax="+valmax);
		return new String[] { Double.toString(total), argmax, Double.toString(valmax) };
	}
	
	public Hashtable<String, StateInfo> trans_mat_to_list(String[] states, Hashtable<String, Double> start_p,
			Hashtable<String, Hashtable<String, Double>> trans_p) {
		Hashtable<String, StateInfo> T = new Hashtable<String, StateInfo>();
		// System.out.println("Inside trans_mat_to_list\n");
		final long stime = System.currentTimeMillis();

		for (String state : states) {
			TransNode cr_node = null;
			// StateInfo csi = new StateInfo(start_p.get(state), state,
			// start_p.get(state));
			StateInfo csi = new StateInfo(1.0 / 6000.0, state, 1.0 / 6000.0, null);
			T.put(state, csi);
			// System.out.println("state="+state+"state_p="+start_p.get(state));
			for (String source_state : states) {
				double trans_prob;
				if (!(trans_p.containsKey(source_state) && trans_p.get(source_state).containsKey(state))) {
					trans_prob = 0;
				} else {
					trans_prob = trans_p.get(source_state).get(state);
				}
				// System.out.println("trans_prob="+trans_prob);
				if (trans_prob > 0.0) {// if trans prob from source to state is
										// not zero, save it
					TransNode mn = new TransNode(source_state, trans_prob);
					// System.out.println("inserting new node\n");
					if (cr_node == null) {
						csi.first_node = mn;
					} else {
						cr_node.next_node = mn;
					}

					cr_node = mn;
				}
			}

		}

		final long etime = System.currentTimeMillis();
		// System.out.println("Finished converting to list in " + (etime -
		// stime)+"\n");
		return T;
	}

	public Hashtable<String, StateInfoTimeStamp> trans_mat_to_list_indexed(String[] states,
			Hashtable<String, Double> start_p, Hashtable<String, Hashtable<String, Double>> trans_p) {
		Hashtable<String, StateInfoTimeStamp> T = new Hashtable<String, StateInfoTimeStamp>();
		// System.out.println("Inside trans_mat_to_list\n");
		final long stime = System.currentTimeMillis();

		for (String state : states) {
			TransNode cr_node = null;
			// StateInfo csi = new StateInfo(start_p.get(state), state,
			// start_p.get(state));
			StateInfoTimeStamp csi = new StateInfoTimeStamp((0.0), state, (0.0), null);
			T.put(state, csi);
			// System.out.println("state="+state+"state_p="+start_p.get(state));
			for (String source_state : states) {
				double trans_prob;
				if (!(trans_p.containsKey(source_state) && trans_p.get(source_state).containsKey(state))) {
					trans_prob = 0;
				} else {
					trans_prob = trans_p.get(source_state).get(state);
				}
				// System.out.println("trans_prob="+trans_prob);
				if (trans_prob > 0.0) {// if trans prob from source to state is
										// not zero, save it
					TransNode mn = new TransNode(source_state, trans_prob);
					// System.out.println("inserting new node\n");
					if (cr_node == null) {
						csi.first_node = mn;
					} else {
						cr_node.next_node = mn;
					}

					cr_node = mn;
				}
			}

		}

		final long etime = System.currentTimeMillis();
		// System.out.println("Finished converting to list in " + (etime -
		// stime)+"\n");
		return T;
	}

	public Hashtable<String, StateInfoTimeStamp> trans_mat_to_list_indexed(List<String> states,
			Hashtable<String, Double> start_p, Hashtable<String, Hashtable<String, Double>> trans_p) {
		Hashtable<String, StateInfoTimeStamp> T = new Hashtable<String, StateInfoTimeStamp>();
		// System.out.println("Inside trans_mat_to_list\n");
//		final long stime = System.currentTimeMillis();

		states.parallelStream().forEach(state -> {

			TransNode cr_node = null;
			// StateInfo csi = new StateInfo(start_p.get(state), state,
			// start_p.get(state));
			StateInfoTimeStamp csi = new StateInfoTimeStamp((0.0), state, (0.0), null);
			T.put(state, csi);
			// System.out.println("state="+state+"state_p="+start_p.get(state));
			for (String source_state : states) {
				double trans_prob;
				if (!(trans_p.containsKey(source_state) && trans_p.get(source_state).containsKey(state))) {
					trans_prob = 0;
				} else {
					trans_prob = trans_p.get(source_state).get(state);
				}
				// System.out.println("trans_prob="+trans_prob);
				if (trans_prob > 0.0) {// if trans prob from source to state is
										// not zero, save it
					TransNode mn = new TransNode(source_state, trans_prob);
					// System.out.println("inserting new node\n");
					if (cr_node == null) {
						csi.first_node = mn;
					} else {
						cr_node.next_node = mn;
					}

					cr_node = mn;
				}
			}


		});

//		final long etime = System.currentTimeMillis();
		// System.out.println("Finished converting to list in " + (etime -
		// stime)+"\n");
		return T;
	}
}
