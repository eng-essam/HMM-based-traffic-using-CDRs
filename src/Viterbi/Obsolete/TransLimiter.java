/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author essam
 */
public class TransLimiter {

    Hashtable<String, ArrayList<String>> nodesMap;
    Hashtable<String, Hashtable<String, Double>> ems;
    Hashtable<String, Hashtable<String, Double>> trns;
    Hashtable<String, Hashtable<String, Double>> newEms;
    Hashtable<String, Hashtable<String, Double>> newTrns;
    String[] states;
    Hashtable<String, Double> strt;
    ArrayList<String> exts;
    String[] userObs;

    public TransLimiter(Hashtable<String, Hashtable<String, Double>> ems, Hashtable<String, Hashtable<String, Double>> Trns) {
        this.ems = ems;
        this.trns = Trns;
        nodesMap = new Hashtable<>();
        initMapper();
    }

    public ArrayList<String> calcNewExts(String[] userObs) {
        exts = new ArrayList<>();
        for (int i = 0; i < userObs.length; i++) {
            String zone = userObs[i];
            if (nodesMap.containsKey(zone)) {
                ArrayList<String> zExts = nodesMap.get(zone);
//                System.out.printf("Zone\t%s,\t%d\n", zone, zExts.size());
                for (Iterator<String> iterator = zExts.iterator(); iterator.hasNext();) {
                    String next = iterator.next();
                    /**
                     * Incorrect step
                     */
                    if (trns.containsKey(next) && ems.containsKey(next)) {
                        exts.add(next);
                    }

                }

            }

        }
//        System.out.println("exts\t" + exts.size());
        return exts;
    }

    public Hashtable<String, Hashtable<String, Double>> getEms() {
        Hashtable<String, Hashtable<String, Double>> newEms = new Hashtable<>();
        for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
            String ext = iterator.next();
            newEms.put(ext, ems.get(ext));
        }
        return newEms;
    }

    public String[] getStates() {
        return exts.toArray(new String[exts.size()]);
    }

    public Hashtable<String, Double> getStrt() {
        Hashtable<String, Double> strt = new Hashtable<>();
        double sum = 0;
        for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
            String ext = iterator.next();
            double prob = 1.0/exts.size();//Math.random();
            sum += prob;
            strt.put(ext, prob);
        }

        for (Map.Entry<String, Double> entrySet : strt.entrySet()) {
            String key = entrySet.getKey();
            Double value = entrySet.getValue();
            strt.replace(key, value / sum);

        }
        return strt;
    }

    public Hashtable<String, Hashtable<String, Double>> getTrns() {
        Hashtable<String, Hashtable<String, Double>> newTrns = new Hashtable<>();
        for (Iterator<String> iterator = exts.iterator(); iterator.hasNext();) {
            String ext = iterator.next();
            newTrns.put(ext, trns.get(ext));
        }
        return newTrns;
    }

    private void initMapper() {
        for (Map.Entry<String, Hashtable<String, Double>> entrySet : ems.entrySet()) {
            String key = entrySet.getKey();
            Hashtable<String, Double> value = entrySet.getValue();
            for (Map.Entry<String, Double> zonesEntry : value.entrySet()) {
                String zone = zonesEntry.getKey();
                double prob = zonesEntry.getValue();
                if (prob == 0) {
                    continue;
                }
                if (nodesMap.containsKey(zone)) {
                    ArrayList<String> exts = nodesMap.get(zone);
                    if (!exts.contains(key)) {
                        exts.add(key);
                        nodesMap.replace(zone, exts);
                    }

                } else {
                    ArrayList<String> exts = new ArrayList<>();
                    exts.add(key);
                    nodesMap.put(zone, exts);
                }

            }

        }

//        System.out.println("Mapper \t" + nodesMap.size());
    }
}
