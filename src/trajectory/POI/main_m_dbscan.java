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

import Voronoi.VoronoiConverter;

/**
 *
 * @author essam
 */
public class main_m_dbscan {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        final int min_pnts = Integer.parseInt(args[0]);
//        String n_vor_file = args[1];
//        String vor_r_file = args[2];

        final int min_pnts = 18;
        String n_vor_file ="/home/essam/traffic/Dakar/senegal/senegal.vor.neighborSitesForSite.csv";
//                "/home/essam/traffic/DI/Dakar-2/dakar.vor.neighborSitesForSite.csv";
        
        Scan s = new Scan();
        String seq = "202,483,483,483,489,522,526,524,555,555,555,555,526,526,526,526,524,524,526,524,535,589,589,589,589,589,589,589,592,589,589,584,589,587,555,483,483,483,483,483,483,483,483,483,483,483,483,483,483,485,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,489,489,483,483,483,483,483,483,28,28,28,28,28,28,28,28,28,28,28,28,28,127,202,202,60,57,34,28,524,526,526,524,526,526,524,526,524,526,521,584,555,524,524,526,512,515,483,483,483,483,483,483,483,28,114,114,114,114,125,202,202,202,202,179,28,161,202,170,28,483,483,485,485,485,485,483,483,483,483,483,485,508,525,525,526,555,526,524,526,526,515,500,483,483,483,28,28,28,28,483,483,483,483,483,483,483,483,478,1056,515,528,555,587,589,589,589,589,587,587,587,587,587,589,587,587,589,587,587,587,589,589,589,589,587,587,535,483,483,483,483,483,483,485,485,483,525,584,587,587,589,587,587,587,589,587,587,587,587,587,589,587,587,589,589,587,587,587,587,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,483,500,531,526,526,555,526,555,515,493,483,483,483,515,526,526,526,524,524,524,526,555,524,524,526,526,526,526,526,526,555,555,526,555,526,507,483,483,28,28,28,179,28,28,28,114,114,171";
//        int[] i_seq = s.convert(seq);
//        Hashtable<Integer, Integer> freq = s.sort_freq(s.get_freq(i_seq));

        VoronoiConverter converter = new VoronoiConverter();
//        ArrayList<Region> voronoiRegions = converter.readVoronoi(vor_r_file);
        Hashtable<Integer, ArrayList<Integer>> nvor = converter.readVorNeighbors(n_vor_file);

        List<Cluster> clusters = s.scan_clusters(seq, nvor, min_pnts);

        for (Iterator<Cluster> iterator = clusters.iterator(); iterator.hasNext();) {
            iterator.next().print();
            System.out.println("--------------------------------");
        }
//        Hashtable<Integer, Integer> t = new Hashtable<>();
//        t.put(0, 20);
//        t.put(1, 23);
//        t.put(2, 6);
//        t.put(3, 45);
//
//        
//        s.sortValue(t);
    }

}

//class Scan<T> {
//
//    /**
//     * Convert sequence into array of integer observations
//     *
//     * @param seq
//     * @return
//     */
//    public int[] convert(String seq) {
//        String segs[] = seq.split(",");
//        int i_segs[] = new int[segs.length];
//        for (int i = 0; i < segs.length; i++) {
//            int twr = Integer.parseInt(segs[i]);
//            i_segs[i] = twr;
//        }
//        return i_segs;
//    }
//
//    /**
//     * Get frequencies of visited towers ..
//     *
//     * @param segs
//     * @return
//     */
//    public Hashtable<Integer, Integer> get_freq(int[] segs) {
//        Hashtable<Integer, Integer> twr_freq = new Hashtable<>();
//
//        Frequency freq = new Frequency();
//        for (int i = 0; i < segs.length; i++) {
//            freq.addValue(segs[i]);
//
//            if (!twr_freq.containsKey(segs[i])) {
//                twr_freq.put(segs[i], -1);
//            }
//        }
//
//        for (Map.Entry<Integer, Integer> entrySet : twr_freq.entrySet()) {
//            int key = entrySet.getKey();
//            int count = (int) freq.getCount(key);
//            twr_freq.replace(key, count);
//        }
//        return twr_freq;
//    }
//
//    /**
//     * sort hashtable in an ascending order ...
//     *
//     * @param t
//     * @return
//     */
//    public Hashtable<Integer, Integer> sort_freq(Hashtable<Integer, Integer> t) {
//
//        Hashtable<Integer, Integer> sorted = new Hashtable();
//
//        ArrayList<Map.Entry<Integer, Integer>> l = new ArrayList(t.entrySet());
//        Collections.sort(l, new Comparator<Map.Entry<?, Integer>>() {
//
//            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
//                return o2.getValue().compareTo(o1.getValue());
//            }
//        });
//        System.out.println(l);
//        for (Iterator<Map.Entry<Integer, Integer>> iterator = l.iterator(); iterator.hasNext();) {
//            Map.Entry<Integer, Integer> next = iterator.next();
//            sorted.put(next.getKey(), next.getValue());
//            System.out.println(next.getKey() + "\t" + next.getValue());
//        }
//
//        return sorted;
//    }
//
//    /**
//     * Sum frequencies of a specific list of regions
//     *
//     * @param freq
//     * @param neighbours
//     * @return
//     */
//    public long sum_neighbours(Hashtable<Integer, Integer> freq, List<Integer> neighbours) {
//        long sum = 0;
//        for (Iterator<Integer> iterator = neighbours.iterator(); iterator.hasNext();) {
//            int twr = iterator.next();
//            if (freq.containsKey(twr)) {
//                sum += freq.get(twr);
//            }
//
//        }
//        return sum;
//    }
//
//    /**
//     *
//     * @param seq
//     * @param nvor
//     * @param min_pnts
//     * @return
//     */
//    private List<Integer> visited;
//    private List<Integer> noise;
//
//    public List<Cluster> scan_clusters(String seq, Hashtable<Integer, ArrayList<Integer>> nvor, int min_pnts) {
//        List<Cluster> clusters = new ArrayList<>();
//
//        int[] i_seq = convert(seq);
//        Hashtable<Integer, Integer> freq = sort_freq(get_freq(i_seq));
//        visited = new ArrayList<>();
//        noise = new ArrayList<>();
//        for (Map.Entry<Integer, Integer> entrySet : freq.entrySet()) {
//            int zone_key = entrySet.getKey();
//            if (visited.contains(zone_key)) {
//                continue;
//            }
//            long n = 1;
//            List<Integer> neighbours = null;
//            // to avoid null exception for zones outside the targted range ..
//            if (nvor.containsKey(zone_key)) {
//                neighbours = nvor.get(zone_key);
//                neighbours.add(zone_key);
//                // remove visited nodes from the neighbors list ...
//                neighbours = remove_visited(neighbours);
//
//                n = sum_neighbours(freq, neighbours);
//            }
//
//            if (n > min_pnts) {
//                Cluster c = expand_cluster(freq, neighbours);
//                clusters.add(c);
//            } else {
//                visited.add(zone_key);
//                noise.add(zone_key);
//            }
//
//        }
//        return clusters;
//    }
//
//    /**
//     * Add points
//     *
//     * @param freq
//     * @param n
//     * @return
//     */
//    public Cluster expand_cluster(Hashtable<Integer, Integer> freq, List<Integer> n) {
//        Cluster c = new Cluster();
//        int h_freq = Integer.MIN_VALUE;
//        int c_zone = -1;
//        for (Iterator<Integer> iterator = n.iterator(); iterator.hasNext();) {
//            int twr = iterator.next();
//            if (freq.containsKey(twr)) {
//                int f = freq.get(twr);
//                // get the highest frequency, to be the centroid of the cluster ..
//                if (f > h_freq) {
//                    h_freq = f;
//                    c_zone = twr;
//                }
//
//                // mark visited zones 
//                if (!visited.contains(twr)) {
//                    visited.add(twr);
//                }
////                else {
////                    System.out.println("Error: zone visited before by another cluster");
////                }
//                c.addpoint(twr);
//            }
//        }
//        
//        c.set_centroid(c_zone);
//        return c;
//    }
//
//    public List<Integer> remove_visited(List<Integer> neighbours) {
//        List<Integer> updated_ghbrs = new ArrayList<>();
//        for (Iterator<Integer> iterator = neighbours.iterator(); iterator.hasNext();) {
//            int zone = iterator.next();
//            if (!visited.contains(zone)) {
//                updated_ghbrs.add(zone);
//            }
//        }
//        return updated_ghbrs;
//    }
//}
//
//class Cluster<T> {
//
//    private List<T> points;
//    private T centriod;
//
//    public Cluster() {
//        points = new ArrayList<>();
//    }
//
//    public void addpoint(T point) {
//        points.add(point);
//    }
//
//    public void set_centroid(T point) {
//        centriod = point;
//    }
//
//    public List<T> getpoints() {
//        return points;
//    }
//
//    public void print() {
//        System.out.println(centriod.toString());
//        System.out.print(points.get(0).toString());
//        for (int i = 1; i < points.size(); i++) {
//            System.out.print("," + points.get(i).toString());
//        }
//        System.out.println("");
//    }
//}
