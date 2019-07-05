/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Density;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import Observations.ObsTripsBuilder;
import Viterbi.TransBuilder;
import utils.DataHandler;

/**
 *
 * @author essam
 */
public class UpdateTransitions {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String denisty_dir = "/home/essam/traffic/SET2.2/density";
        String trans_path = "/home/essam/traffic/Dakar/N_Darak/transitions-1km.xml";
        DataHandler adaptor = new DataHandler();
        int index = 1;
        Hashtable<String, Hashtable<String, Double>> transitions = adaptor.readProbXMLTable(trans_path);
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(denisty_dir));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String path = iterator.next();
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            if (fileName.startsWith(".")) {
                continue;
            }

            Hashtable<String, Hashtable<String, Double>> density = Density.readDensity(path);
            transitions = TransBuilder.updateTrans(transitions, density,1.0,1.0);
            String upath = denisty_dir + "/transition.update." + index++ + ".xml";
            adaptor.writeXmlTable(transitions, upath);
        }

    }

}
