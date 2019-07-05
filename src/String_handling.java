/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author essam
 */
public class String_handling {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String vit = "27091182#0,233379474_interpolated_[0],197286092_interpolated_[3],218927270#1-AddedOffRampEdge/218927258#0_interpolated_[0],108176277#0,218927254_interpolated_[0],233379474_interpolated_[5]";
        System.out.println(vit);
        System.out.println(vit.replaceAll("_interpolated_\\[([0-9]+)\\]", ""));
    }
    
}
