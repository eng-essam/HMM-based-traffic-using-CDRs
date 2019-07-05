/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package towers.test;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.StdDraw;

/**
 *
 * @author essam
 */
public class PlotTowersDAG {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String vorNeighboursPth = args[0];
        String towersPth = args[1];

        Hashtable<Integer, ArrayList<Integer>> voronoiNeibours = readVoronoiNeibors(vorNeighboursPth);
        Hashtable<Integer, Vertex> towers = readTowers(towersPth);

        double xmin, ymin, xmax, ymax;
//        "227761.06,1620859.93,240728.96,1635128.30
        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;
        StdDraw.setXscale(xmin, xmax);
        StdDraw.setYscale(ymin, ymax);
        StdDraw.show(0);
//        StdDraw.setPenRadius(0.008);
//        StdDraw.line(xmin, ymin, xmax, ymax);

        for (Map.Entry<Integer, ArrayList<Integer>> entrySet : voronoiNeibours.entrySet()) {
            Integer fromReg = entrySet.getKey();
            ArrayList<Integer> neighbours = entrySet.getValue();
            Vertex pnt = towers.get(fromReg);
            StdDraw.setPenColor(Color.BLUE);
            
            double fx = pnt.x;
            double fy = pnt.y;
            StdDraw.circle(fx, fy, 100);
//            StdDraw.setPenColor(Color.DARK_GRAY);
//            StdDraw.text(fx+50, fy+50, fromReg.toString());
            
            for (Iterator<Integer> iterator = neighbours.iterator(); iterator.hasNext();) {
                Integer next = iterator.next();
                Vertex tPnt = towers.get(next);
                double tx = tPnt.x;
                double ty = tPnt.y;
                StdDraw.setPenColor(Color.GRAY);
                StdDraw.line(fx, fy, tx, ty);

            }

        }
        StdDraw.show(0);
        StdDraw.save("DAG.png");

    }

    public static Hashtable<Integer, Vertex> readTowers(String path) {
        Hashtable<Integer, Vertex> towers = new Hashtable<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            Vertex pnt = null;
            boolean flag = false;
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");
                pnt = new Vertex();
                int index = Integer.parseInt(lineSplit[0]);
                pnt.x = Double.parseDouble(lineSplit[1]);
                pnt.y = Double.parseDouble(lineSplit[2]);
//                System.out.printf("%f,%f",pnt.x,pnt.y);
                towers.put(index, pnt);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return towers;
    }

    public static Hashtable<Integer, ArrayList<Integer>> readVoronoiNeibors(String path) {
        Hashtable<Integer, ArrayList<Integer>> neighbours = new Hashtable<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            int id = 0;

            while ((line = reader.readLine()) != null) {
                ArrayList<Integer> tmp = new ArrayList<>();
                String lineSplit[] = line.split(",");

                if (!line.startsWith(",")) {
                    id = Integer.parseInt(lineSplit[0].trim());
                } else {
                    for (String lineSplit1 : lineSplit) {
                        String toZone = lineSplit1.trim();
                        if (toZone.equals("") || toZone == null) {
                            continue;
                        }
                        tmp.add(Integer.parseInt(toZone));
                    }
                }
                neighbours.put(id, tmp);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlotTowersDAG.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return neighbours;
    }

}

class Vertex {

    double x;
    Double y;
}
