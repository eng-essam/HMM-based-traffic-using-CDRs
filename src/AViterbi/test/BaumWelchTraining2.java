/**
 * javac -cp .:../jgrapht.jar:../jahmm.jar AViterbi/test/*.java java -cp
 * .:../jahmm.jar -Xss1g -d64 -XX:+UseG1GC -Xms50g -Xmx50g
 * -XX:MaxGCPauseMillis=500 AViterbi.test.BaumWelchTraining1
 * 
 * cd /home2/essam/DTraffic/src; dir=/home2/essam/traffic/Dakar-2
 * export PATH=~/jdk1.8.0_60/bin:$PATH
 * java -cp .:../jahmm.jar \
-Xss1g \
-d64 \
-XX:+UseG1GC \
-Xms50g \
-Xmx50g \
-XX:MaxGCPauseMillis=500 \
AViterbi.test.BaumWelchTraining2
* 
 */
package AViterbi.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Observations.Obs;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.ObservationReal;
import be.ac.ulg.montefiore.run.jahmm.OpdfGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchScaledLearner;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class BaumWelchTraining2 {

    final static String CLM = ",";

    static Hmm buildHmm(double[][] trans, double[][] emission) {

        double[] pi = new double[trans.length];
        for (int i = 0; i < pi.length; i++) {
            pi[i] = 1.0 / (trans.length);

        }
        List<OpdfInteger> opdfs = new ArrayList<>();

        for (int i = 0; i < trans.length; i++) {
            opdfs.add(i, new OpdfInteger(emission[i]));
        }

        for (int i = 0; i < trans.length; i++) {
            if (Double.isNaN(trans[i][i])) {
                trans[i][i] = getSmallest(trans[i]);
            }
//            System.out.printf("trans[%d][%d]\t%f\n", i, i, trans[i][i]);
        }
        return new Hmm(pi, trans, opdfs);
    }

    public static double getSmallest(double[] arr) {
        double smallest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < arr.length; i++) {
            double b = arr[i];
            if (b < smallest) {
                smallest = b;
            }
        }
        return smallest;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String transPath = "/home2/essam/traffic/DI/Dakar-2.1/transition.xml";
        String emissionPath = "/home2/essam/traffic/DI/Dakar-2.1/emission.xml";
        String probXml = "/home2/essam/traffic/DI/Dakar-2.1/dakar.xy.dist.vor.xml";
        String obsPath = "/home2/essam/traffic/SET2/tmp/SET2_P01.CSV.0_300.xml";

        int towers = 300;

        DataHandler adaptor = new DataHandler();
        adaptor.readNetworkDist(probXml);
//        ArrayList<String> exts = adaptor.getExts();
//        exts.trimToSize();
        //======================================================================
        /**
         * Read transitions and emission probabilities from stored data
         */
        Hashtable<String, Hashtable<String, Double>> emit_p = adaptor.readProbXMLTable(emissionPath);
        ArrayList<String> exts = new ArrayList<>(emit_p.keySet());
        Hashtable<String, Hashtable<String, Double>> trans_p = adaptor.readProbXMLTable(transPath);
        //======================================================================

//        System.out.println(Arrays.toString(exts.toArray(new String[exts.size()])));
        /**
         * Adapt network distribution to be used in Viterbi
         */
        double[][] trans = adaptor.adaptTrans(trans_p, exts, exts.size());
        //zones start from zero instead of 1 so you have to extract one from each observation.....
        double[][] emiss = adaptor.adaptEmission(emit_p, exts, exts.size(), towers);

        Hashtable<String, Hashtable<Integer, Obs>> all_obs = DataHandler.readObsDUT(obsPath);

        /**
         * Initialize HMM
         */
        long stime = System.currentTimeMillis();
        double[] pi = new double[trans.length];
        for (int i = 0; i < pi.length; i++) {
            pi[i] = 1.0 / (trans.length);

        }
        OpdfIntegerFactory factory = new OpdfIntegerFactory(towers);
        Hmm<ObservationInteger> hmm = new Hmm<ObservationInteger>(exts.size(), factory);

//        List<OpdfInteger> opdfs = new ArrayList<>();
        for (int i = 0; i < trans.length; i++) {
            hmm.setOpdf(i, new OpdfInteger(emiss[i]));
        }

        for (int i = 0; i < trans.length; i++) {
            for (int j = 0; j < trans.length; j++) {
                hmm.setAij(i, j, trans[i][j]);
//                double q = trans[i][j];

            }
        }

        //        Hmm hmm = new Hmm(pi, trans, opdfs);
        long etime = System.currentTimeMillis();
        System.out.println("Finish building HMM in:\t" + (etime - stime));

        List<ObservationInteger> tst_seq = new ArrayList<>();
//      128 101 105 106 114 102 103 122 108 117 121 119 138
        tst_seq.add(new ObservationInteger(128));
        tst_seq.add(new ObservationInteger(101));
        tst_seq.add(new ObservationInteger(105));
        tst_seq.add(new ObservationInteger(106));
        tst_seq.add(new ObservationInteger(114));
        tst_seq.add(new ObservationInteger(102));
        tst_seq.add(new ObservationInteger(103));
        tst_seq.add(new ObservationInteger(122));
        tst_seq.add(new ObservationInteger(108));
        tst_seq.add(new ObservationInteger(117));
        tst_seq.add(new ObservationInteger(121));
        tst_seq.add(new ObservationInteger(119));
        tst_seq.add(new ObservationInteger(138));

        ViterbiCalculator vit = vit = new ViterbiCalculator(tst_seq, hmm);
        double prob = vit.lnProbability();
        int[] states_out = vit.stateSequence();

        String path = "";
        for (int i = 0; i < states_out.length; i++) {
            if (!path.isEmpty()) {
                path += CLM;
            }
            path += exts.get(states_out[i]);
        }
        System.out.format("Viterbi Calculator\t%f\t%s\n", prob, path);

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : all_obs.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<Integer, Obs> obsTable = entrySet.getValue();
            System.out.println("Trained with observations from " + key);
            int count = 0;
            List<List<ObservationInteger>> sequences = new ArrayList<>();
            List<ObservationInteger> sequence;
            for (Map.Entry<Integer, Obs> entrySet1 : obsTable.entrySet()) {
                sequence = new ArrayList<>();
                String[] seq = entrySet1.getValue().getSeq().split(CLM);
                if (seq.length < 8) {
                    continue;
                }
                for (String userOb : seq) {
                    sequence.add(new ObservationInteger(Integer.parseInt(userOb)));
                }
                sequences.add(sequence);
                count++;
                // for test ...
                if (count == 10000) {
                    break;
                }

            }

            BaumWelchLearner bwl = new BaumWelchScaledLearner();
            for (int ii = 0; ii < 5; ii++) {
                System.out.println("Iteration: " + ii);
                stime = System.currentTimeMillis();

                hmm = bwl.iterate(hmm, sequences);
//                bwl.learn(hmm, sequences);
                etime = System.currentTimeMillis();
                System.out.println("Finished learning the first epoch in\t" + (etime - stime));

                vit = new ViterbiCalculator(tst_seq, hmm);
                prob = vit.lnProbability();
                states_out = vit.stateSequence();

                path = "";
                for (int i = 0; i < states_out.length; i++) {
                    if (!path.isEmpty()) {
                        path += CLM;
                    }
                    path += exts.get(states_out[i]);
                }
                System.out.format("Viterbi Calculator after training:\t%f\t%s\n", prob, path);
                System.out.println("-----------------------------------");
            }

        }

//        (new GenericHmmDrawerDot()).write(hmm, "learntHmm.dot");
//        (new HmmIntegerDrawer()).write(hmm, "learnt1Hmm.dot");
////        Writer writer = new FileWriter("/home/essam/Documents/initHMM");
////
////        HmmWriter.write(writer, new OpdfIntegerWriter(), hmm);
////        writer = new FileWriter("/home/essam/Documents/learntHmm");
////        HmmWriter.write(writer, new OpdfIntegerWriter(), learntHmm);
//        double dist;
//        KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();
////        stime = System.currentTimeMillis();
////        dist = klc.distance(learntHmm, hmm);
////        etime = System.currentTimeMillis();
////        System.out.println("kmeans klc: " + dist + " in " + (etime - stime));
//
//        stime = System.currentTimeMillis();
//        dist = klc.distance(bwl_learntHmm, hmm);
//        etime = System.currentTimeMillis();
//        System.out.println("bwl klc: " + dist + " in " + (etime - stime));
//        for (int i = 0; i < 10; i++) {
//            stime = System.currentTimeMillis();
//            double dist = klc.distance(learntHmm, hmm);
//            etime = System.currentTimeMillis();
//            System.out.println(i + " " + dist + " in " + (etime - stime));
//
//            learntHmm = bwl.iterate(learntHmm, sequences);
//        }
    }

    public Hmm<ObservationReal> learnBleh(List<List<ObservationReal>> sequences) {
        int numberOfHiddenStates = 12;
        Hmm<ObservationReal> trainedHmm;
        do {

            KMeansLearner<ObservationReal> kml = new KMeansLearner<ObservationReal>(numberOfHiddenStates, new OpdfGaussianFactory(), sequences);

            trainedHmm = kml.learn();
            BaumWelchLearner bwl = new BaumWelchLearner();
            bwl.setNbIterations(20);
            trainedHmm = bwl.learn(trainedHmm, sequences);
            numberOfHiddenStates++;
        } while (Double.isNaN(trainedHmm.getPi(0)) && numberOfHiddenStates
                < 50);

        return trainedHmm;
    }
}
