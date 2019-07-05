/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi.test;

import java.util.Hashtable;
import java.util.Map;

import AViterbi.Gyration;
import Observations.Obs;
import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_gyration {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String towers_path = "/home/essam/traffic/Dakar/towers.csv";
        String obs_path = "/home/essam/traffic/SET2/tmp/SET2_P01.CSV.0_1666_th-dist_1000_th-time_60.xml";
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_path);

        Hashtable<String, Hashtable<Integer, Obs>> obsTable = DataHandler.readObsDUT(obs_path);

        Gyration gyration = new Gyration(towers);

        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<Integer, Obs> value = entrySet.getValue();
            int urban_count = 0;
            int sedentary = 0;
            int total = 0;
            for (Map.Entry<Integer, Obs> users_entryset : value.entrySet()) {
                Integer usr_id = users_entryset.getKey();
                String obs = users_entryset.getValue().getSeq();
                double rg = gyration.calc_rg(Gyration.toList(obs)) / 1000;

                if (rg != 0) {
                    total++;
//                    System.out.printf("User ID: %d\t Gyration radius: %f\n", usr_id, rg);
//                    System.out.println("\t"+obs);
                    if (rg < 11 && rg > 3) {
                        urban_count++;
                    } else if (rg < 3) {
                        sedentary++;
                        
                    }
                }
            }
            
            System.out.printf("Day: %s\tSedentary: %f\t urban: %f\n", key,(double)sedentary/total, (double)urban_count/total);
//            break;

        }

    }

}
