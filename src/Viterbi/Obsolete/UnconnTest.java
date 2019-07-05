/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Viterbi.Obsolete;

import java.util.ArrayList;

import Viterbi.TransBuilder;
import utils.DataHandler;
import utils.FromNode;

/**
 *
 * @author essam
 */
public class UnconnTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String probXml =  "/home/essam/traffic/Dakar/Dakar_edge-200_jan/dakar.xy.dist.vor.xml";
        String vorPath = "/home/essam/traffic/Dakar/Dakar_edge-200_jan/dakar.vor.neighborSitesForSite.org.csv";
        DataHandler adaptor = new DataHandler();
        ArrayList<FromNode> map = adaptor.readNetworkDist(probXml);
//        VConnectivityInspector inspector = new VConnectivityInspector();
//        inspector.removeUnconneced(map, vorPath);
        TransBuilder tbuilder = new TransBuilder(map, vorPath);
        tbuilder.removeUnconneced();
                
    }
    
}
