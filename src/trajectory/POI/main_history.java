/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author essam
 */
public class main_history {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dataset_path = args[0];
        String out_file = args[1];
        
        POI poi = new POI();
        Hashtable<Integer, List<Integer>> obs_tbl = poi.unlimited_combine_twr(poi.getObs(dataset_path));
        poi.write_history(obs_tbl, out_file);

    }

}
