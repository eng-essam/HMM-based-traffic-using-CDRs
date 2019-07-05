/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author essam
 */
public class main_read_poi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String poi_file = "/home/essam/traffic/SET2/poi/poi_sample_log.xml";
        Scan s = new Scan();
        Hashtable<Integer, List<Cluster>> clusters = s.read_xml_poi(poi_file);
        
        for (Map.Entry<Integer, List<Cluster>> entrySet : clusters.entrySet()) {
            Integer usr_key = entrySet.getKey();
            System.out.println("==========================================");
            System.out.println("User\n" + usr_key + "\n");
            
            List<Cluster> value = entrySet.getValue();
            for (Iterator<Cluster> iterator = value.iterator(); iterator.hasNext();) {
                Cluster n_c = iterator.next();
                n_c.print();
                System.out.println("------------------------------------------");
                
            }
            
        }

    }

}
