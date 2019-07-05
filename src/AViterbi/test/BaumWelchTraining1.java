/**
 * javac -cp .:../jgrapht.jar:../jahmm.jar AViterbi/test/*.java java -cp
 * .:../jahmm.jar -Xss1g -d64 -XX:+UseG1GC -Xms50g -Xmx50g
 * -XX:MaxGCPauseMillis=500 AViterbi.test.BaumWelchTraining1
 */
package AViterbi.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

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
public class BaumWelchTraining1 {

    final static String CLM = ",";

    /* The HMM this example is based on */
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
        String transPath = "/home/essam/traffic/DI/Dakar-2.1/transition.xml";
        String emissionPath = "/home/essam/traffic/DI/Dakar-2.1/emission.xml";
        String probXml = "/home/essam/traffic/DI/Dakar-2.1/dakar.xy.dist.vor.xml";

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

        Hashtable<Integer, String> obsTable = new Hashtable<>();
        obsTable.put(0, "253,224,242,226,216");
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

//        (new GenericHmmDrawerDot()).write(hmm, "learntHmm.dot");
//        (new HmmIntegerDrawer()).write(hmm, "learnt1Hmm.dot");
        List<List<ObservationInteger>> sequences = new ArrayList<>();
        List<ObservationInteger> sequence;
        for (Map.Entry<Integer, String> entrySet : obsTable.entrySet()) {
            sequence = new ArrayList<>();
            String[] seq = entrySet.getValue().split(CLM);
            for (String userOb : seq) {
                sequence.add(new ObservationInteger(Integer.parseInt(userOb)));
            }
            sequences.add(sequence);

            ViterbiCalculator vit = vit = new ViterbiCalculator(sequence, hmm);
            double prob = vit.lnProbability();
            int[] states_out = vit.stateSequence();

            String path = "";
            for (int i = 0; i < states_out.length; i++) {
                if (!path.isEmpty()) {
                    path += CLM;
                }
                path += exts.get(states_out[i]);
            }
            System.out.format("ViterbiCalculator\t%f\t%s\n", prob, path);

//            ForwardBackwardCalculator fb = new ForwardBackwardCalculator(sequence, hmm, EnumSet.of(ForwardBackwardCalculator.Computation.BETA));
//            System.out.format(" Forward-Backward Calculator \t %f\n", fb.probability());
//            ForwardBackwardScaledCalculator fbs = new ForwardBackwardScaledCalculator(sequence, hmm);
//            System.out.format(" Forward Scaled Calculator \t %f\n", fbs.lnProbability());
//
//            fbs = new ForwardBackwardScaledCalculator(sequence, hmm, EnumSet.of(ForwardBackwardCalculator.Computation.BETA));
//            System.out.format("Backward Scaled Calculator \t %f\n", fbs.lnProbability());
        }

//        System.out.println("====================");
//        
        // iterate .. just one iteration while learn do learning  ..
//        KMeansLearner kml = new KMeansLearner(exts.size(), factory, sequences);
//        Hmm<ObservationInteger> learntHmm = kml.iterate();
        BaumWelchLearner bwl = new BaumWelchScaledLearner();
        stime = System.currentTimeMillis();

        Hmm<ObservationInteger> bwl_learntHmm = bwl.iterate(hmm, sequences);
//                bwl.learn(hmm, sequences);
        etime = System.currentTimeMillis();
        System.out.println("Finished learning the first epoch in\t" + (etime - stime));

        ViterbiCalculator vit = vit = new ViterbiCalculator(sequences.get(0), hmm);
        double prob = vit.lnProbability();
        int[] states_out = vit.stateSequence();

        String path = "";
        for (int i = 0; i < states_out.length; i++) {
            if (!path.isEmpty()) {
                path += CLM;
            }
            path += exts.get(states_out[i]);
        }
        System.out.format("ViterbiCalculator\t%f\t%s\n", prob, path);

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
