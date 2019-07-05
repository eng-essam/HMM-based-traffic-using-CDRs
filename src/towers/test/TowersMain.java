/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

import towers.TowerTags;

/**
 *
 * @author essam
 */
public class TowersMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        double ax=14.137;
        double ay=-16.106;
        double bx=15.517;
        double by=-17.608;
        String path="C:\\Users\\Essam\\Dropbox\\viterbi\\Java\\MergeXML\\src\\SITE_ARR_LONLAT.CSV";
        
        TowerTags tags = new TowerTags(path);
//        TowerTags tags = new TowerTags(args[0]);
//        tags.generateTags();
        tags.getTowers(ax, ay, bx, by);
    }
    
}
