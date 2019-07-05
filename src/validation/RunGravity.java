/*

    export PATH=~/jdk1.8.0_60/bin:$PATH

    cd /home2/essam/DTraffic/src; dir=/home2/essam/traffic/Dakar-2

    javac -cp .:../diva.jar:../jgrapht.jar:../guava.jar:../commons-math3-3.5.jar Validation/*.java

    java -cp .:../diva.jar:../jgrapht.jar:../guava.jar  \
    -Xss1g \
    -d64 \
    -XX:+UseG1GC \
    -Xms50g \
    -Xmx50g \
    -XX:MaxGCPauseMillis=500 \
    Validation.RunGravity \
    /home2/essam/traffic/Dakar-2/results/ \
    $dir/dakar.xy.dist.vor.xml

 */
package validation;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import Observations.Obs;
import Observations.ObsTripsBuilder;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class RunGravity {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String vitPath = args[0];
        String mapPath = args[1];
//        String vitPath = "/home/essam/traffic/Dakar/Dakar_edge-200-old/Results/without_repeated_obs/alpha-0.8_beta-.2/transitions-20";
//        String mapPath = "/home/essam/traffic/Dakar/Dakar_edge-200-old/Dakar.xy.dist.w.vor.xml";
        final double threshold = 1000f;
        final int trip_length = 3;

        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(mapPath);
//        Hashtable<Integer, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(vitPath);
//        GravityModel model = new GravityModel();
//        model.handleFlow(obs);
//        model.calcDistances(map);
//        String path = vitPath.substring(0, vitPath.lastIndexOf(".")) + ".gravity.xml";
//        model.computeGOD();
//        model.writeFlow(path);

        int count = 1;

        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(vitPath));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".") || !fileName.contains("viterbi.day.")) {
                continue;
            }
            String day = subsetPath.substring(subsetPath.lastIndexOf("viterbi.day.") + "viterbi.day.".length(), subsetPath.lastIndexOf("."));
            String fgodPath = subsetPath.substring(0, subsetPath.lastIndexOf("/") + 1) + "fgod." + threshold + ".threshold." + trip_length + ".trip.length.day."
                    + day + ".csv";
            Hashtable<String, Hashtable<Integer, Obs>> obs = ObsTripsBuilder.readObsDUT(subsetPath);
            GravityModel model = new GravityModel();
            model.handleFlow(obs);
            model.calcDistances(map);

            Hashtable<Double, ArrayList<Double>> fgod = model.computeGOD(threshold,false);
            model.writeFGOD(fgodPath, fgod);

            fgodPath = subsetPath.substring(0, subsetPath.lastIndexOf("/") + 1) + "avg.fgod." + threshold + ".threshold." + trip_length + ".trip.length.day."
                    + day + ".csv";
            Hashtable<Double, ArrayList<Double>> avgGOD = model.avgGOD(fgod);
            model.writeAvgGOD(fgodPath, avgGOD);
//            System.out.println("=================================================");
            System.out.printf("%s,%f,%f\n",day,model.find_all_linear_regression(fgod,false),model.find_avg_linear_regression(avgGOD,false));
//            System.out.println("All values\t" + model.find_all_linear_regression(fgod));
//            System.out.println("Average values\t" + model.find_avg_linear_regression(avgGOD));
//            System.out.println("=================================================");

            String path = subsetPath.replace("viterbi.day.", "gravity.day.");
//                    subsetPath.substring(0, subsetPath.lastIndexOf(".")) + ".gravity.xml";
            model.writeFlow(path);

        }
    }

}
