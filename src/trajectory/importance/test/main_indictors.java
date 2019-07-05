/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

import java.util.Hashtable;
import java.util.List;

import utils.DataHandler;

/**
 *
 * @author essam
 */
public class main_indictors {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dataset_path = args[0];
        String output_path = args[1];
        int[] indices = {0, 7, 26, 29};
        List<String[]> segments = DataHandler.read_multiple_csv(dataset_path, ",");

//        List<String[]> t_segments = DataHandler.extract_info(segments, indices);
//        for (Iterator<String[]> iterator = t_segments.iterator(); iterator.hasNext();) {
//            String[] l_elements = iterator.next();
//            for (int i = 0; i < l_elements.length; i++) {
//                String l_element = l_elements[i];
//                System.out.print(l_element + "\t");
//            }
//            System.out.println("");
//
//        }

        double[] entropy = DataHandler.to_double(DataHandler.extract_info_field(segments, 7));
        double[] home = DataHandler.to_double(DataHandler.extract_info_field(segments, 26));
        double[] gyration = DataHandler.round_list(DataHandler.to_double(DataHandler.extract_info_field(segments, 29)), 100);

        Hashtable<Double, Double> rg_freq = DataHandler.get_frequency(gyration);
        Hashtable<Double, Double> home_percent_dist = DataHandler.get_distribution(home);
        Hashtable<Double, Double> entropy_dist = DataHandler.get_distribution(entropy);

        DataHandler.write_csv(rg_freq, "Rarius_of_gyration,Frequency", output_path + "/rg_freq.csv");
        DataHandler.write_csv(home_percent_dist, "Home_spent_percentage,Distribution", output_path + "/home_percent_dist.csv");
        DataHandler.write_csv(entropy_dist, "Entropy_movement,Distribution", output_path + "/entropy_dist.csv");

    }

}
