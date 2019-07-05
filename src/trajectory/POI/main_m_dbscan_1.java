/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Voronoi.VoronoiConverter;

/**
 *
 * @author essam
 */
public class main_m_dbscan_1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        final int min_pnts = Integer.parseInt(args[0]);
//        String n_vor_file = args[1];
//        String dataset_path = args[2];
        final int min_pnts = 18;
        String n_vor_file = "/home/essam/traffic/Dakar/senegal/senegal.vor.neighborSitesForSite.csv";
        String dataset_path = "/home/essam/traffic/SET2/poi";

//        String vor_r_file = args[3];
        
//        String seq = "202,483,483,483,489,522,526,524,555,555,555,555,526,526,526,526,524,524,526,524,535,589,589,589,589,589,589,589,592,589,589,584,589,587,555,483,483,483,483,483,483,483,483,483,483,483,483,483,483,485,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,489,489,483,483,483,483,483,483,28,28,28,28,28,28,28,28,28,28,28,28,28,127,202,202,60,57,34,28,524,526,526,524,526,526,524,526,524,526,521,584,555,524,524,526,512,515,483,483,483,483,483,483,483,28,114,114,114,114,125,202,202,202,202,179,28,161,202,170,28,483,483,485,485,485,485,483,483,483,483,483,485,508,525,525,526,555,526,524,526,526,515,500,483,483,483,28,28,28,28,483,483,483,483,483,483,483,483,478,1056,515,528,555,587,589,589,589,589,587,587,587,587,587,589,587,587,589,587,587,587,589,589,589,589,587,587,535,483,483,483,483,483,483,485,485,483,525,584,587,587,589,587,587,587,589,587,587,587,587,587,589,587,587,589,589,587,587,587,587,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,500,531,526,526,555,526,555,515,493,483,483,483,515,526,526,526,524,524,524,526,555,524,524,526,526,526,526,526,526,555,555,526,555,526,507,483,483,28,28,28,179,28,28,28,114,114,171";
//        int[] i_seq = s.convert(seq);
//        Hashtable<Integer, Integer> freq = s.sort_freq(s.get_freq(i_seq));

        VoronoiConverter converter = new VoronoiConverter();
//        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor_r_file);
        Hashtable<Integer, ArrayList<Integer>> nvor = converter.readVorNeighbors(n_vor_file);
        POI poi = new POI();
        Hashtable<Integer, List<Integer>> obs_tbl = poi.unlimited_combine_twr(poi.getObs(dataset_path));
        
        for (Map.Entry<Integer, List<Integer>> entrySet : obs_tbl.entrySet()) {
            int usr_key = entrySet.getKey();
            System.out.println("==========================================");
            System.out.println("User\n" + usr_key + "\n");
            List<Integer> seq = entrySet.getValue();
            
            Scan s = new Scan();
            List<Cluster> clusters = s.scan_clusters(seq, nvor, min_pnts);
            
            for (Iterator<Cluster> iterator = clusters.iterator(); iterator.hasNext();) {
                iterator.next().print();
                System.out.println("------------------------------------------");
            }
            
        }
        
//        List<Cluster> clusters = s.scan_clusters(seq, nvor, min_pnts);
//
//        for (Iterator<Cluster> iterator = clusters.iterator(); iterator.hasNext();) {
//            iterator.next().print();
//            System.out.println("--------------------------------");
//        }
//        Hashtable<Integer, Integer> t = new Hashtable<>();
//        t.put(0, 20);
//        t.put(1, 23);
//        t.put(2, 6);
//        t.put(3, 45);
//
//        s.sortValue(t);
    }
    
}
