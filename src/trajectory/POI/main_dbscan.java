/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;

import utils.DataHandler;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class main_dbscan {

    public static void main(String[] args) {
        double theta = 700;
        int min_pnts = 14;
        String dataset_path = "/home/essam/traffic/SET2/poi";
        String towers_file = "/home/essam/traffic/Dakar/towers.csv";
        String poi_file = "/home/essam/traffic/SET2/poi/poi.csv";
        POI poi = new POI();

//        DBSCANClusterer dbscan = new DBSCANClusterer(1400, 14);
//        Hashtable<Integer, List<DoublePoint>> u_data = poi.unlimited_combine(poi.getObs(dataset_path), tower_file);
        //test
        int u_id = 1001;
//        List<DoublePoint> pnts = u_data.get(u_id);
//        for (Iterator<DoublePoint> iterator1 = pnts.iterator(); iterator1.hasNext();) {
//            DoublePoint next = iterator1.next();
//            System.out.println(next.toString());
//        }
//        System.out.println("==========================================");
        Hashtable<Integer, Vertex> towers = DataHandler.readTowers(towers_file);
        
            
            Hashtable<Integer, List<Cluster<DoublePoint>>> u_poi = poi.get_poi_clusters(dataset_path, towers, theta, min_pnts);
            Hashtable<DoublePoint, Integer> m_towers = poi.mirror_towers(towers);
            poi.write_poi(u_poi, m_towers, poi_file);
//            
            
//            poi.write_obs_coordinates(dataset_path, towers, dataset_path+"/ver_hmm");



//        for (Map.Entry<Integer, List<DoublePoint>> entrySet : u_data.entrySet()) {
//            Integer usr_key = entrySet.getKey();
//            List<Cluster<DoublePoint>> cluster = dbscan.cluster(entrySet.getValue());
//            u_poi.put(usr_key, cluster);
//        }
//        System.out.print("nusers\n");
//        System.out.print(u_poi.size() + "\n");
//        for (Map.Entry<Integer, List<Cluster<DoublePoint>>> entrySet : u_poi.entrySet()) {
//            Integer key = entrySet.getKey();
//            System.out.print("user\n");
//            System.out.print(key + "\n");
//            List<Cluster<DoublePoint>> value = entrySet.getValue();
//            System.out.print("nclusters\n" + value.size() + "\n");
//            for (Iterator<Cluster<DoublePoint>> iterator = value.iterator(); iterator.hasNext();) {
//                List<DoublePoint> pnts = iterator.next().getPoints();
////                    DoublePoint c = get_centriod(pnts);
//                System.out.print(m_towers.get(poi.get_centriod(pnts)));
//                for (Iterator<DoublePoint> iterator1 = pnts.iterator(); iterator1.hasNext();) {
//                    DoublePoint next = iterator1.next();
//                    System.out.print("," + m_towers.get(next));
//                }
//                System.out.println();
//            }
//
//        }

//        List<Cluster<DoublePoint>> cluster = u_poi.get(u_id);
//        for (Iterator<Cluster<DoublePoint>> iterator = cluster.iterator(); iterator.hasNext();) {
//            List<DoublePoint> pnts = iterator.next().getPoints();
//            DoublePoint centroid = poi.get_centriod(pnts);
//            System.out.println(m_towers.get(centroid));
//            for (Iterator<DoublePoint> iterator1 = pnts.iterator(); iterator1.hasNext();) {
//                DoublePoint next = iterator1.next();
//                System.out.println("\t" + m_towers.get(next));
//            }
//            System.out.println("==========================================");
//        }
    }

}
