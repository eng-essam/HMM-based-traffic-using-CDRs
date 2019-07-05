
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Observations.ObsTripsBuilder;
import utils.DataHandler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author essam
 */
public class main_test_files {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String updates_dir =args[0];
        
        DataHandler adaptor = new DataHandler();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(updates_dir));
        
        System.out.println(files);
        List<String> nfiles = adaptor.filter(files, "transition.day.");
        System.out.println(nfiles);
    }
    
}
