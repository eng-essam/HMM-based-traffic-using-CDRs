/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validation;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import Observations.Obs;

/**
 *
 * @author essam
 */
public class RoutesDensity {

    final static String RLM = "/";
    final static String CLM = ",";
    /**
     * Colors gumets dimensions.
     */
    final double gxmin = 228200.06;
    final double gxmax = 240000.96;
    final double gymin = 1620000.93;
    final double gymax = 1620800.30;

    /**
     * Map dimensions
     */
    final double xmin = 227761.06;
    final double xmax = 240728.96;
    final double ymin = 1619000.93;
    final double ymax = 1635128.30;

    /**
     * Write the density map to XML file.
     *
     * @param density which is Hashtable<exit id,
     * Hashtable<exit id, counted vehicles>> @param path
     */
    public RoutesDensity() {
    }

    /**
     * Accumulate densities
     *
     * @param density
     * @return
     */
    public Hashtable<String, Double> accumulate(Hashtable<String, Hashtable<String, Double>> density) {
        Hashtable<String, Double> acc = new Hashtable<>();
        for (Map.Entry<String, Hashtable<String, Double>> entrySet : density.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<String, Double> value = entrySet.getValue();
            double sum = 0;
            for (Map.Entry<String, Double> toEntry : value.entrySet()) {
                String tokey = toEntry.getKey();
                if (key.equals(tokey)) {
                    continue;
                }
                sum += toEntry.getValue().doubleValue();

            }
            acc.put(key, sum);
        }

        return acc;
    }

    /**
     * Does not allow self emissions, or just ignore it. It is useful in
     * validation process.
     *
     * @param seq_hour
     * @return
     */
    private Hashtable<String, Hashtable<String, Double>> density_val(ArrayList<String> seq_hour) {
        Hashtable<String, Hashtable<String, Double>> densityTable = new Hashtable<>();
        for (int i = 0; i < seq_hour.size(); i++) {
            String[] pnts = seq_hour.get(i).split(CLM);
//            if (pnts.length==1) {
//                pnts = new String[]{pnts[0],pnts[0]};
////                System.out.println("add self transition\t"+pnts[0]+"\t"+pnts[1]);
//            }
            for (int j = 0; j < pnts.length - 1; j++) {

                String fromPnt = pnts[j];
                String toPnt = pnts[j + 1];

                /**
                 * Remove nodes that match in to and from for one transition
                 */
                if (fromPnt.equals(toPnt)) {
                    continue;
                }
                
                if (densityTable.containsKey(fromPnt)) {
                    Hashtable<String, Double> tos = densityTable.get(fromPnt);
                    if (tos.containsKey(toPnt)) {
                        tos.replace(toPnt, (tos.get(toPnt).doubleValue() + 1));
                    } else {
                        tos.put(toPnt, 1.0);
                    }
                    densityTable.replace(fromPnt, tos);

                } else {
                    Hashtable<String, Double> tos = new Hashtable<>();
                    tos.put(toPnt, 1.0);
                    densityTable.put(fromPnt, tos);
                }

            }

        }
        return densityTable;
    }

    public Hashtable<Double, ArrayList<Double>> getdensities(Hashtable<String, Double> d_vit, Hashtable<String, Double> d_dua) {
        Hashtable<Double, ArrayList<Double>> densities = new Hashtable<>();
        for (Map.Entry<String, Double> entrySet : d_vit.entrySet()) {
            String key = entrySet.getKey();
            double vit_d_val = entrySet.getValue();
            double rou_d_val = 0;
            if (d_dua.containsKey(key)) {
                rou_d_val = d_dua.get(key);
            }
            ArrayList<Double> list = null;

            if (densities.containsKey(rou_d_val)) {
                list = densities.get(rou_d_val);
                list.add(vit_d_val);
                densities.replace(rou_d_val, list);
            } else {
                list = new ArrayList<>();
                list.add(vit_d_val);
                densities.put(rou_d_val, list);
            }

        }
        return densities;
    }

    /**
     * Get the sum counted number of vehicles
     *
     * @param accum
     * @return
     */
    public int getMax(Hashtable<String, Integer> accum) {
        int max = 0;
        for (Map.Entry<String, Integer> entrySet : accum.entrySet()) {
            String key = entrySet.getKey();
            int val = entrySet.getValue();
            if (val > max) {
                max = val;
            }

        }
        return max;
    }

    /**
     * Get the mean number of accumulated vehicles
     *
     * @param accum
     * @return
     */
    public int getMean(Hashtable<String, Integer> accum) {
        int sum = 0;

        for (Map.Entry<String, Integer> entrySet : accum.entrySet()) {
            String key = entrySet.getKey();
            int val = entrySet.getValue();
            sum += val;

        }
        return sum / accum.size();
    }

    /**
     * Calculate the density from user equilibrium paths
     *
     * @param seq_hour
     * @return
     */
    public Hashtable<String, Hashtable<String, Double>> getRoutesDensity(ArrayList<String> seq_hour) {
        return density_val(seq_hour);
    }

    /**
     *
     * @param d_vit
     * @param d_dua
     * @return
     */
    public double getRSE(Hashtable<String, Double> d_vit, Hashtable<String, Double> d_dua) {
        double sum = 0;
        System.out.println("key,vit_d_val,rou_d_val");

        for (Map.Entry<String, Double> entrySet : d_vit.entrySet()) {
            String key = entrySet.getKey();
            double vit_d_val = entrySet.getValue();
            double rou_d_val = 0;
            if (d_dua.containsKey(key)) {
                rou_d_val = d_dua.get(key);
            }
            System.out.printf("%s,%f,%f\n", key, vit_d_val, rou_d_val);
            double diff = vit_d_val - rou_d_val;
            sum += Math.pow(diff, 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * get the density of the calculated viterbi paths.
     *
     * @param vit_out
     * @return
     */
    public Hashtable<String, Hashtable<String, Double>> getVitDensity(Hashtable<String, Hashtable<Integer, Obs>> vit_out) {
        ArrayList<String> vitList = new ArrayList<>();
        for (Map.Entry<String, Hashtable<Integer, Obs>> entrySet : vit_out.entrySet()) {
            Hashtable<Integer, Obs> value = entrySet.getValue();
            for (Map.Entry<Integer, Obs> tosEntry : value.entrySet()) {
                Obs obs = tosEntry.getValue();
                String vit = obs.getVitPath();
                String[] paths = vit.split(RLM);
                for (int i = 0; i < paths.length; i++) {
                    String path = paths[i];
                    if (path.isEmpty() || path.equals("-")) {
                        continue;
                    }
                    vitList.add(path);
                }

            }

        }
        return density_val(vitList);
    }

    public Hashtable<String, Double> remove_interpolation(Hashtable<String, Double> vit_rou) {
        Hashtable<String, Double> tmp = new Hashtable<>();
        for (Map.Entry<String, Double> entrySet : vit_rou.entrySet()) {
            String edge = entrySet.getKey();
            double value = entrySet.getValue();

            if (edge.contains("_interpolated")) {
                edge = edge.substring(0, edge.indexOf("_interpolated"));
            }

            if (tmp.containsKey(edge)) {
                double tmp_val = tmp.get(edge);
                tmp.replace(edge, tmp_val + value);
            } else {
                tmp.put(edge, value);
            }

        }
        return tmp;
    }

    public Hashtable<String, Hashtable<String, Double>> sumDensities(Hashtable<String, Hashtable<String, Double>> den0, Hashtable<String, Hashtable<String, Double>> den1) {

        for (Map.Entry<String, Hashtable<String, Double>> entrySet : den1.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<String, Double> value = entrySet.getValue();
            if (den0.containsKey(key)) {
                Hashtable<String, Double> den0Value = den0.get(key);
                for (Map.Entry<String, Double> tosEntry : value.entrySet()) {
                    String toKey = tosEntry.getKey();
                    Double count = tosEntry.getValue();
                    if (den0Value.containsKey(toKey)) {
                        den0Value.replace(toKey, den0Value.get(toKey) + count);

                    } else {
                        den0Value.put(toKey, count);
                    }

                }
                den0.replace(key, den0Value);
            } else {
                den0.put(key, value);
            }

        }
        return den0;
    }

    /**
     * Accumulate vehicles per exit points
     *
     * @param density
     * @return
     */
    public Hashtable<String, Integer> sumVehicles(Hashtable<String, Hashtable<String, Double>> density) {
        Hashtable<String, Integer> sv = new Hashtable<>();

        for (Map.Entry<String, Hashtable<String, Double>> entrySet : density.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<String, Double> value = entrySet.getValue();
            int sum = 0;
            for (Map.Entry<String, Double> toentry : value.entrySet()) {
                sum += toentry.getValue();

            }
            sv.put(key, sum);

        }
        return sv;
    }
}
