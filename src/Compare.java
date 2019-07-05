
import java.util.Hashtable;
import java.util.Map;

import Observations.Obs;
import Observations.ObsTripsBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author essam
 */
public class Compare {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String path = "/home/essam/traffic/stops/data_0/SET2.2013-01-08_Tuesday.xml";
        String path_iterative = "/home/essam/traffic/stops/data/SET2.2013-01-08_Tuesday.xml";

        Hashtable<String, Hashtable<Integer, Obs>> obs_stops = ObsTripsBuilder.readObsDUT(path);
        Hashtable<String, Hashtable<Integer, Obs>> obs_stops_iter = ObsTripsBuilder.readObsDUT(path_iterative);
        double count = 0;

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obs_stops.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<Integer, Obs> obs_stops_val = entrySet.getValue();
            Hashtable<Integer, Obs> obs_stops_iter_val = obs_stops_iter.get(key);

            for (Map.Entry<Integer, Obs> entrySet1 : obs_stops_val.entrySet()) {
                Integer key1 = entrySet1.getKey();
                String seq = entrySet1.getValue().getSeq();
                String seq_iter = obs_stops_iter_val.get(key1).getSeq();
                if (!seq.equals(seq_iter)) {
                    count++;
                    System.out.println(seq + " // " + seq_iter);
                }

            }
            System.out.println("avg diff:" + count / obs_stops_iter_val.size());
        }

    }

}
