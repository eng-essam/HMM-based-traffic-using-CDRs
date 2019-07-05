/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author essam
 */
public class SplitDaily_WeeklyData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String path = args[0];
        ObsTripsBuilder builder = new ObsTripsBuilder();
        ArrayList<String> files = ObsTripsBuilder.list_dir_files(new File(path));
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
            String subsetPath = iterator.next();
            String fileName = subsetPath.substring(subsetPath.lastIndexOf("/") + 1);
            if (fileName.startsWith(".") || subsetPath.contains("WEEK") || subsetPath.contains("_Day_")) {
                continue;
            }
            if (subsetPath.endsWith("CSV") || subsetPath.endsWith("csv")) {
//                builder.split_weeky_data(subsetPath);
                builder.split_daily_data(subsetPath);
            }
        }
    }

}
