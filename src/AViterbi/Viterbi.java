/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author essam
 */
public class Viterbi {
    
    /**
 * Wed Sep 23 12:55:55 JST 2015
 * Best implementation 
 * 
 * @param path
 * @param nstates
 * @param nseq
 * @param seq
 * @param init
 * @param trans_ap
 * @param emis_ap
 * @return 
 */
double forward_viterbi(int[] path, int nstates, int nseq, int[] seq, double[] init, mnode[] trans_ap, mnode[] emis_ap) {

    double T[]=new double[nstates];
    Arrays.fill(T, Math.log(0));
    
    int TV[][]= new int[nstates][nseq];

    List<node> substates = emis_ap[seq[0]].nodes;
    // initialize states emitted by the first observation ...
    for (int i = 0; i < substates.size(); i++) {
        node snode = substates.get(i);
        int index = snode.index;
        double v_prob = init[index] + snode.prob;
        T[index] = v_prob;
        TV[index][0] = index;
    }

    for (int i = 1; i < nseq; i++) {
        substates = emis_ap[seq[i]].nodes;
        int size = substates.size();
        if (substates.isEmpty())
            return Math.log(0);

        double U[] =new double[nstates];
        Arrays.fill(U, Math.log(0));

        int UTV[][]= new int[nstates][nseq];

        for (int j = 0; j < size; j++) {
            node nxt_state = substates.get(j);
            int nxt_index = nxt_state.index;
            double em = nxt_state.prob;
            //string s_nxt_index = to_string(nxt_index);

            //            string argmax = "";
            int argmax[]=new int[nseq];

            //            int mark;
            double valmax = Math.log(0);

            List<node> r_nodes = trans_ap[nxt_index].nodes;
            if (r_nodes.isEmpty()) return 0;
            for (int k = 0; k < r_nodes.size(); k++) {
                node state = r_nodes.get(k);
                //                if(T[state.index]==log2(0))continue;
                double v_prob = T[state.index] + state.prob;
                //debug
                //                if (i == 6 && nxt_index == 895) {
                //                    printf("next id: %d \t current id: %d\ttransition: %f \t viterbi path:%f\n", nxt_index, state.index, state.prob, T[state.index]);
                //                }
                if (v_prob > valmax) {
                    //                    for (int jj = 0; jj < nseq; jj++)argmax[jj] = TV[state.index][jj];
                    argmax= TV[state.index];
                    argmax[i] = nxt_index;
                    //                    mark=state.index;
                    //                    argmax = TS[state.index];
                    valmax = v_prob;
                }
            }
            // copy array contents ...
            //            for (int jj = 0; jj <= i; jj++) TV[nxt_index][jj] = argmax[jj];
            //            if (i == 6 && nxt_index == 895) {
            //                printf("Valmax:%f\t path: ", valmax);
            //                print(argmax, nseq);
            //            }
            UTV[nxt_index]= argmax;
            U[nxt_index] = valmax + em;
        }

        T= U;
        TV= UTV;

    }

    double valmax = Math.log(0);
    //    int argmax[nseq];
//    int mark;
    substates = emis_ap[seq[nseq - 1]].nodes;
    int size = substates.size();
    for (int i = 0; i < size; i++) {
        node lnode = substates.get(i);
        double v_prob = T[lnode.index];
        //                print(TV[lnode.index], nseq);
        if (v_prob > valmax) {

//            mark = lnode.index;
            path= TV[lnode.index];
            //            argmax = TS[lnode.index];
            valmax = v_prob;

        }
    }
    return valmax;
}
}
