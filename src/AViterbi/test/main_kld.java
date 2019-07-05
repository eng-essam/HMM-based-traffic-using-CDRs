/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import AViterbi.IO;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;

/**
 *
 * @author essam
 */
public class main_kld {

    static Hmm<ObservationInteger> buildHmm(double[] pi, double[][] trans_p, double[][] emis_p) {
        int nobs = emis_p[0].length;
        int nstates = emis_p.length;
        System.out.printf("nObs %d \t nStates %d\n", nobs, nstates);

        OpdfIntegerFactory factory = new OpdfIntegerFactory(nobs);
        Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(nstates, factory);
        // set initial probabilities
        for (int i = 0; i < nstates; i++) {
            hmm.setPi(i, pi[i]);
        }
        // set emission probabilities
        for (int i = 0; i < nstates; i++) {
            hmm.setOpdf(i, new OpdfInteger(emis_p[i]));
        }
        // set transition probabilities
        for (int i = 0; i < nstates; i++) {
            for (int j = 0; j < nstates; j++) {
                hmm.setAij(i, j, trans_p[i][j]);
            }
        }
        return hmm;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int nstates = Integer.parseInt(args[0]);
        int nobs = Integer.parseInt(args[1]);
        String hmm_filename = args[2];
        String uhmm_filename = args[3];

        double init[] = new double[nstates];
        double trans_p[][] = new double[nstates][nstates];
        double emis_p[][] = new double[nstates][nobs];

        IO io = new IO();
        io.readHMM(hmm_filename, init, trans_p, emis_p);
        Hmm<ObservationInteger> hmm = buildHmm(init, trans_p, emis_p);

        System.out.println("Finish building the initial HMM");
        io.readHMM(uhmm_filename, init, trans_p, emis_p);
        Hmm<ObservationInteger> uhmm = buildHmm(init, trans_p, emis_p);

        KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();
        double stime = System.currentTimeMillis();
        double dist = klc.distance(uhmm, hmm);
        double etime = System.currentTimeMillis();
        System.out.println("Baum welch klc: " + dist + " in " + (etime - stime));
        

    }
}
