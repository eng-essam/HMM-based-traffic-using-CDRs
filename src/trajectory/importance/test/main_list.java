/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.importance.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author essam
 */
public class main_list {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        List<Integer> nums = new ArrayList<>();
        List<Integer> nums_1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            nums.add(i);
            nums_1.add(i);
        }
        
        nums.addAll(nums_1);
        for (int i = 0; i < nums.size(); i++) {
            System.out.println(nums.get(i));
        }
        
        for (Iterator<Integer> iterator = nums.iterator(); iterator.hasNext();) {
            Integer next = iterator.next();
            System.out.println(next);
        }
        
    }
    
}
