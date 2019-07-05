/*
 * java -d64 -XX:+UseG1GC -Xms12g -Xmx12g -XX:MaxGCPauseMillis=500 trajectory.importance.test.main_write_hmm 1666 ~/traffic/models/senegal/ senegal ~/traffic/models/senegal/map.xy.dist.vor.xml ~/traffic/models/senegal/transition.xml ~/traffic/models/senegal/emission.xml
 */
package trajectory.importance.test;

import utils.DataHandler;

/**
 *
 * @author essam
 */
public class main_write_hmm {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int zones = Integer.parseInt(args[0]);
        String output = args[1];
        String name = args[2];
        String network_path = args[3];
        String trans_path = args[4];
        String emit_path = args[5];

        DataHandler.write_hmm(output, name, zones, network_path, trans_path, emit_path);
        
    }

}
