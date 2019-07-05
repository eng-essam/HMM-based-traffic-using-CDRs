/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import Viterbi.ObsIndex;
import Viterbi.StateInfo;
import Viterbi.StateInfoTimeStamp;
import Viterbi.Viterbi;

/**
 *
 * @author essam
 */
public class RunnableVA implements Callable<Object[]> {

    static    Viterbi myvit = new Viterbi();
    private Thread t;
    private String threadName;
    String[] obs;
    String[] states;
    Hashtable<String, Double> start_p;
    Hashtable<String, Hashtable<String, Double>> trans_p;
    Hashtable<String, Hashtable<String, Double>> emit_p;
    Object[] vals;
    Hashtable<String, StateInfoTimeStamp> si_org;

    ObsIndex oi_org;

    /**
     * Runnable constructor that call the viterbi with both matrix reduction and
     * internal loop reduction
     *
     * @param mapt2f
     * @param obs
     * @param states
     * @param start_p
     * @param trans_p
     * @param emit_p
     */
    public RunnableVA(Hashtable<String, ArrayList<String>> mapt2f, String[] obs, String[] states, Hashtable<String, Double> start_p, Hashtable<String, Hashtable<String, Double>> trans_p, Hashtable<String, Hashtable<String, Double>> emit_p) {
        this.obs = obs;
        this.states = states;
        this.start_p = start_p;
        this.trans_p = trans_p;
        this.emit_p = emit_p;

        Viterbi viterbi = new Viterbi();

//        vals = viterbi.forward_viterbi(obs,
//                states,
//                start_p,
//                trans_p,
//                emit_p);
//	Viterbi viterbi = new Viterbi();                                                                                             
        System.out.println("invoking runnable");

        Hashtable<String, StateInfo> si = viterbi.trans_mat_to_list(states,
                start_p,
                trans_p);

//        Object[] ret = viterbi.forward_viterbi_linkedlist(obs, states,
//                emit_p,
//                si);
//
//        System.out.println("-->" + (String) ret[1]);
//
//        vals = viterbi.forward_vit_subset(mapt2f, obs, states, start_p, trans_p, emit_p);
    }

    /**
     * Initial Runnable constructor that depend only the reduction of the
     * matrix.
     *
     * @param obs
     * @param states
     * @param start_p
     * @param trans_p
     * @param emit_p
     */
    public RunnableVA(String[] obs, String[] states, Hashtable<String, Double> start_p, Hashtable<String, Hashtable<String, Double>> trans_p, Hashtable<String, Hashtable<String, Double>> emit_p) {
        this.obs = obs;
        this.states = states;
        this.start_p = start_p;
        this.trans_p = trans_p;
        this.emit_p = emit_p;

        Viterbi viterbi = new Viterbi();

        vals = Viterbi.forward_viterbi(obs,
                states,
                start_p,
                trans_p,
                emit_p);

    }
public RunnableVA(String[] obs, String[] states, Hashtable<String, Hashtable<String, Double>> emit_p, Hashtable<String, StateInfoTimeStamp> si_org, ObsIndex oi_org) {

    this.obs = obs;
    this.states = states;
    this.emit_p = emit_p;
    this.si_org = si_org;
    this.oi_org = oi_org;

//        ObsIndex oi = new ObsIndex(oi_org);
//        Hashtable<String, StateInfoTimeStamp> si = new Hashtable<>(si_org);
//
//        Viterbi viterbi = new Viterbi();
//        System.out.println("Using observation indexing method");
//
//        vals = viterbi.forward_viterbi_linkedlist_indexed(obs, states, emit_p, si, oi);
//        String vit_path = (String) ret[1];
//        System.out.println("####\t" + (String) ret[1]);
}
    @Override
    public Object[] call() throws Exception {
        /**
         * Invoke the actual code with threads not with the initialization
         * serial part.
         */
	
        ObsIndex oi = oi_org; //new ObsIndex(oi_org);
        Hashtable<String, StateInfoTimeStamp> si = si_org;//new Hashtable<>(si_org);

        Viterbi viterbi = myvit;//new Viterbi();
        System.out.println("Using observation indexing method");
        return viterbi.forward_viterbi_linkedlist_indexed(obs, states, emit_p, si, oi);

    }

}
