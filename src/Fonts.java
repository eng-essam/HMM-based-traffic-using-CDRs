
import java.awt.GraphicsEnvironment;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author essam
 */
public class Fonts {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String fonts[] = 
      GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        

    for ( int i = 0; i < fonts.length; i++ )
    {
      System.out.println(fonts[i]);
    }
    }
    
}
