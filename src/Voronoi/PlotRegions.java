/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Voronoi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.stream.DoubleStream;

import Density.Plot;
import mergexml.NetConstructor;
import utils.DataHandler;
import utils.Edge;
import utils.FromNode;
import utils.Region;
import utils.StdDraw;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class PlotRegions {

    /**
     * @param args the command line arguments
     */
    static ArrayList<FromNode> map;

    public static double[] getCoordinates(String id) {
        double[] coord = {-1, -1};
        for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
            FromNode next = iterator.next();
            if (next.getID().equals(id)) {
                coord[0] = next.getX();
                coord[1] = next.getY();
                break;
            }
        }
        return coord;
    }

    public static void main(String[] args) {
        // TODO code application logic here
        String voronoiPath = args[0];
        String netdist = args[1];
        String vorNeighborsPath = args[2];
        String mapRoutesPath = args[3];
        String edgesPath = args[8];

        VoronoiConverter converter = new VoronoiConverter();
        ArrayList<Region> voronoiRegions = converter.readVoronoi(voronoiPath);
        DataHandler adaptData = new DataHandler();
        map = adaptData.readNetworkDist(netdist);
        Hashtable<Integer, ArrayList<Integer>> voronoiNeibors
                = converter.readVorNeighbors(vorNeighborsPath);
//        converter.writeVoronoiFile(voronoiPath, vorXmlPath);
//        map = converter.DetermineExits(probXml, voronoiPath);

        double xmin, ymin, xmax, ymax;
//        "227761.06,1620859.93,240728.96,1635128.30
//        227761.06,1608439.13,304636.53,1675065.55
//        complete dakar map
//        xmin = 227761.06;
//        xmax = 270728.96;
//        ymin = 1618439.13;
//        ymax = 1645065.55;

        xmin = 227761.06;
        xmax = 240728.96;
        ymin = 1620859.93;
        ymax = 1635128.30;
        ArrayList<Edge> edges = new NetConstructor(edgesPath).readedges();
        Plot ploter = new Plot(edges, "all.png");
        ploter.scale(xmin, ymin, xmax, ymax);
        ploter.plotMapData(mapRoutesPath);

        StdDraw.setPenColor(Color.BLUE);
        // turn on animation mode to defer displaying all of the points
        for (Region region : voronoiRegions) {
            if (region == null) {
                continue;
            }

//            System.out.println("voronio" + region.id);
            ArrayList<Vertex> vertices = region.vertices;

            double[] xPnts = new double[vertices.size()];
            double[] yPnts = new double[vertices.size()];

            int i = 0;
            for (Vertex vert : vertices) {
//                StdDraw.filledCircle(vert.x, vert.y, 500);
//                if()

                xPnts[i] = vert.x;
                yPnts[i] = vert.y;
                i++;
            }
            double cXPnt = DoubleStream.of(xPnts).average().getAsDouble();
            double cYPnt = DoubleStream.of(yPnts).average().getAsDouble();
            --i;
            StdDraw.text(cXPnt, cYPnt, String.valueOf(region.id));

            if (voronoiNeibors.containsKey(region.id)) {
                StdDraw.setPenColor(Color.BLUE);
                StdDraw.polygon(xPnts, yPnts);

            } else {
                StdDraw.setPenColor(Color.RED);
                StdDraw.polygon(xPnts, yPnts);
            }
        }

//        double[] pX = {228134.48,228219.47,228408.87,229076.29,229448.84,229541.05,229650.79,229918.96,230077.33,229899.72,230303.17,230865.84,231082.92,231066.37,231501.78,231370.72,231865.74,231719.07,231747.52,231781.76,232146.58,232250.22,231783.54,232218.74,232133.56,232394.78,232434.61,232880.28,232764.58,232901.67,233118.44,232810.89,232869.66,232869.77,233574.19,233504.41,233311.76,233565.56,233483.07,233719.58,233466.53,233513.38,233372.88,233513.33,233476.72,233804.07,233760.58,233850.07,233988.24,233873.29,233903.88,234005.04,234072.67,234091.28,234176.98,234119.19,233954.43,234113.35,234152.01,234004.56,234060.84,234076.43,234233.64,234282.41,234185.53,234229.63,234372.16,234493.44,234415.78,234468.1,234294.17,234509.76,234459.15,234381.24,234563.66,234523.96,234349.67,234448.1,234693.03,234479.49,234716.63,234689.73,234907.5,234905.7,234771.01,234745.26,234771.66,234758.96,234996.44,234909.61,234849.94,234884.41,234988.63,234903.51,235083.59,235204.25,235086.48,235216.13,235041.46,235004.28,235190.82,235226.95,235242.72,235129.25,235189.09,235330.39,235206.7,235326.78,235248.74,235403.64,235422.48,235423.5,235265.26,235519.73,235526.03,235361.41,235456.7,235375.23,235548.79,235461.64,235657.3,235551.36,235562.39,235506.57,235722.51,235647.13,235560.46,235615.41,235748.33,235811.43,235866.81,235771.92,235827.65,235667.27,235743.16,235872.33,235740.52,235867.74,235914.31,235782.02,235813.84,235863.55,235972.8,235994.57,235998.46,235993.33,236019.11,236187.61,236080.64,236056.58,236108.3,235981.52,236160.59,236058.34,236228.63,236266.08,236166.67,236267.75,236122.28,236229.77,236117.36,236218.74,236177.07,236237.37,236174.51,236302.8,236373.85,236314.76,236461.68,236541.62,236417.74,236374.51,236366.15,236389.66,236417.8,236412.59,236478.56,236569.66,236536.96,236550.81,236653.41,236758.9,236747.33,236639.84,236577.62,236589.6,236602.78,236841.04,236788.6,236766.69,236741.28,236614.19,236842.82,236846.84,236836.89,236917.02,236979.97,236883.79,236937.91,236852.35,236883.78,236878.34,237136.69,236972.86,236894.66,237055.31,237186.11,237004.48,237073.93,237021.37,237131.35,237130.77,237210.23,237164.04,237315.16,237164.64,237256.36,237383.91,237188.61,237397.51,237180.2,237426.28,237243.8,237354.58,237478.35,237451.72,237472.91,237434.85,237374.53,237619.9,237373.21,237356.89,237473.4,237514.47,237595.96,237624.62,237596.49,237759.55,237604.42,237678.56,237893.97,237630.86,237618.6,237708.16,237659.84,237798.43,237823.24,237650.99,237891.53,237978.41,237763.11,237990.14,237999.19,237891.27,237820.71,238099.02,238042,237887.58,238262.01,238141.6,238199.6,238022.5,238274.53,238211.47,238288.86,238245.48,238288.61,238437.93,238551.18,238581.41,238458.14,238390,238382.28,238599.43,238531.34,238614.02,238761.6,238595.17,238871.5,238898.59,238741.59,238834.64,238747.25,238890.62,238847.76,238654.09,238967.45,239011.21,239273.67,239204.05,239119.38,238963.05,239439.32,239409.65,239304.91,239373.73,239669.33,239701.6,239842.11,240003.51,240136.09,239845.66,239987.21,240245.65,240088.66,240410.72,240251.74,240566.62,240530.09,240814.05,240912.7,240955.03,240933.42,241286.49,241436.16,241589.85,241712.75,241764.21,241548.64,242068,241832.86,241849.68,242065.92,241775.54,242005.46,242187.48,242000.49,242270.94,242312,242274.85,242367.35,242512.31,242481.56,242552.91,242765.99,242689.07,243006.68,242757.4,242835.07,242890.91,242930.63,243060.33,243186.31,243433.07,243327.99,243447.98,243610.11,243366.17,243502.49,243740.99,243581.33,243788.32,243898.28,243965.19,243903.46,243987.41,244127.11,244135.37,244329.31,244251.61,244300.77,244350.06,244601.31,244634.45,244649.95,244493.46,244832.33,244986.69,244816.89,244852.68,245067.38,244969.76,245069.2,245243.24,245389.57,245895.98,245329.57,245524.7,245678.63,245640.75,245764.8,245795.48,246054.17,245791.01,246011.25,245942.25,246266.88,246562.91,246187.83,246347.94,246455.61,246473.52,246740.17,246709.25,246953.5,246868.6,247364.81,247200.92,247507.84,247388,247396.82,247535.43,248685.51,248507.67,248642.3,248197.52,248629.07,248722.01,248785.25,249325.94,249566.78,249457.65,249462.67,249987.17,249746.7,250239.21,249942.95,250374.27,250585.64,250625.44,250913.85,250652.68,251179.35,250951.26,251267.5,251441.66,251456.43,251692.64,252306.74,251866.24,252215.25,251819.11,252490.65,252370.31,252522.67,254035.65,253780.92,253580.31,253944.84,253918.92,254107.33,253899.7,254486.57,254551.09,254732.91,254949.55,254074.96,254847.09,254893.7,254588.78,255061.47,255391.14,255228.37,255547.48,255385.19,255780.86,255668.57,255952.16,256262.49,256468.43,256388.44,256370.33,256352.09,256736.42,256819.49,257066.74,257312.32,256797.21,258204.45,258025.51,258992.12,259245.08,259687.76,259595.14,260313.25,260498.19,260488.61,262551.88,264885.16,265483.94,266905.58,266803.61,268196.64,269009.37,268395.52,268482.58,272268.73,269953.67,268117.51,271740.6,271486.12,270676.95,273123.87,273072.33,274073.92,275121.66,273779.69,276109.92,275731.36,276086.94};
//        double[] pY = {1631850.48,1631916.17,1631666.5,1631828.63,1631152.24,1632009.54,1630789.63,1630074.23,1631146.67,1631934.24,1632377.11,1632674.97,1629811.53,1630204.73,1632655.95,1629336.84,1630362.82,1629658.02,1628801.61,1632897.4,1632161.69,1632405.21,1631686.79,1629312.63,1627422.59,1629564.98,1628662.95,1629320.72,1629429.71,1628140.91,1633499.58,1627048.91,1627699.88,1628629.7,1633280.79,1627383.71,1627031.78,1629253.74,1627864.29,1626684.35,1628056.1,1632855.46,1630837.51,1631872.39,1630091.43,1627540.72,1626059.52,1628915.4,1629724.47,1632550.49,1632177.74,1626745.21,1633292.91,1631966.67,1630862,1631613.99,1629284.78,1630021.56,1628082.24,1628481.62,1624112.7,1625557.89,1632010.16,1627661.24,1625965.97,1624785.93,1633195.85,1632191.86,1632842,1626530.69,1630712.48,1629305.6,1627240.4,1627879.69,1629829.97,1628190.54,1624992.3,1631122.61,1626071.24,1631764.8,1627965.69,1624484.55,1632048.94,1627449.74,1625800.61,1633229.28,1628563.89,1630811.56,1628970.6,1632979.5,1626447.03,1629563.87,1630266.93,1625282.39,1631167.69,1632812.32,1629845.18,1632372.37,1628820.44,1629273.74,1626233.75,1627937.21,1628336.01,1624459.62,1626767.81,1627255.69,1630711.68,1628979.62,1624269.12,1625094.95,1631153.69,1631463.87,1625569.71,1627454.45,1632595.26,1632170.95,1629482.01,1631059.77,1630347.93,1624671.87,1629954.64,1628617.6,1633024.39,1624403.48,1627186.99,1629002.14,1627949.73,1625889.49,1632866.82,1624005.29,1626633.9,1625288.99,1631892.2,1632399.21,1629370.73,1631297.46,1629880,1630856.35,1625877.61,1624750.87,1627687.71,1628433.75,1631075.51,1628136.31,1633488.13,1629111.73,1624205.29,1626168.11,1627896.8,1623774.8,1632605.4,1630649.87,1632984.65,1630135.25,1625111.67,1631522.99,1626988.88,1627624.88,1627356.23,1628819.56,1625503.44,1629251.53,1624561.37,1631053.03,1624315.2,1629531.36,1632061.27,1628222.16,1633389.87,1625932.57,1627624.58,1627722.24,1631719.5,1628303.6,1623584.25,1632422.74,1625204.94,1632730.23,1624079.01,1630075.43,1629414.08,1630565.8,1627274.83,1629176.33,1624798.7,1633651.29,1627935.53,1625740.14,1630870.24,1631272.89,1626329.05,1626771.91,1623821.98,1632442.37,1629661.95,1633279.3,1622861.15,1632997.15,1632673.97,1623328.14,1625178.41,1624146.95,1632110.66,1628270.04,1628709.94,1627497,1631741.37,1622605.82,1633493.36,1629177.06,1624654.11,1633381.04,1630239.64,1623200.45,1632485,1623015.91,1623486.07,1632376.76,1623749.05,1631386.38,1622181.15,1632995.68,1631855.21,1627023.05,1622455.69,1622914.84,1623200.66,1624328.35,1632753.92,1623880.34,1630525.92,1626419.48,1630037.41,1627599.52,1623488.71,1633881.48,1625056.58,1633360.6,1629859.2,1631970.61,1621628.59,1622956.64,1622789.61,1628662.94,1630918.14,1622435.75,1623318,1629144.67,1623995.53,1623597.18,1632784.8,1625707.58,1622974.81,1622119.58,1631199.02,1628050.08,1623277.34,1621225.16,1632258.76,1633625.36,1622830.4,1630412.73,1633226.61,1632934.26,1626448.83,1630957.97,1623270.81,1627234.2,1634155.62,1629221.3,1623332.31,1633774,1630357.48,1630962.23,1633057.34,1633351.91,1632822.71,1629977.8,1634161.46,1633208.13,1630567.7,1625195.8,1623349.58,1630880.82,1630222.94,1627145.53,1631442.25,1633422.78,1626684.78,1634293.15,1630244.7,1632043.74,1633144.52,1634550.08,1630646.1,1634005.59,1630842.22,1633692.87,1634367.42,1633449.3,1630746.77,1631481.07,1633857.07,1634290.46,1631999.64,1632306.81,1634837.25,1633966.42,1631020.52,1634378.67,1631609.89,1635070.26,1632238.11,1634116.9,1634584.67,1622908.22,1632423.29,1635104.44,1631530.69,1632572.6,1631978.76,1635200.06,1634740.89,1634016.8,1634384.75,1633645.44,1633131.79,1631893.07,1633159.47,1634909.8,1634745.53,1632325.21,1631428.44,1631817,1633357.33,1635846.33,1635358.31,1633916.89,1634130.71,1632939,1634952.52,1632276.55,1633468.66,1633689.76,1635308.62,1631326.08,1632068.59,1634592.77,1636161.25,1633350.08,1634881.96,1634045.98,1635640.42,1633068.75,1631662.76,1631064.22,1635940.09,1634960.77,1635729.4,1634260.26,1632704.88,1633485.55,1635590.8,1631188.9,1631923.54,1634874.18,1634426.04,1633640.23,1633187.5,1635731.17,1631636.98,1636547.77,1632198.41,1632706.67,1635023.8,1636913.15,1634634.55,1633709.17,1632058.95,1633323.17,1635132.76,1631317.16,1636143.36,1633046.4,1632539.2,1632092.38,1631121.38,1634977.15,1634245.28,1630683.06,1633228.61,1633704.74,1632608.72,1631939.33,1635474.62,1634700.86,1633534.91,1636721.23,1632459.01,1635331.48,1633958.76,1631810.16,1634706.58,1637187.67,1634373.4,1636663.55,1635720.91,1631365.11,1630316.62,1633895.18,1631104.75,1634912.21,1636610.98,1630211.32,1629760.52,1631660.13,1634669.08,1630716.85,1631251.54,1634946.29,1636436.67,1629254.5,1631361,1630183.25,1635502.32,1634215.06,1634865.89,1635201.85,1632254.46,1633311.21,1633901.72,1634544.03,1629811.38,1628723.41,1631027.23,1636325.19,1627987.68,1628283.49,1634313.54,1628761.78,1629908.73,1628014.04,1628986.93,1630082.55,1637875.07,1631327.1,1633192.24,1628352.24,1629350.97,1634659.76,1636388.71,1628844.41,1628354.26,1628098.45,1630038.37,1635641.35,1627701.74,1628733.05,1629147.23,1630098.63,1627979.09,1628353.95,1629530.42,1627226.79,1627744.09,1632806.16,1628276.99,1628595.17,1638097.04,1628149,1631491.53,1625665.47,1627024.19,1625241.07,1626225.28,1625695.26,1633755.49,1623362.05,1636147.6,1630316.44,1639584.51,1620278.01,1618044.61,1631720.1,1616173.24,1631100.67,1639481.2,1631303.7,1623277.74,1630828.48,1649500.02,1628378.13,1611254.99,1651748.88,1607497.79,1649003.52,1617472.98,1602372.72,1644328.9,1618668.71};
//        StdDraw.setPenColor(Color.GREEN);
//        for (int i = 0; i < pY.length; i+=4) {
//            StdDraw.text(pX[i], pY[i], String.valueOf(i+1));
//            
//        }
//        TransitionsBuilder constructor
//                = new TransitionsBuilder(map);
//        Hashtable<String, Hashtable<String, Float>> transition_probability
//                = constructor.getTransitionProb();
//        System.out.println("transitions completed");
//        StdDraw.setPenColor(Color.GREEN);
//        for (Iterator<FromNode> iterator = map.iterator(); iterator.hasNext();) {
//            FromNode next = iterator.next();
//            if (next.isIsExit() && next.getZone() == 3) {
//                StdDraw.circle(next.getX(), next.getY(), 20);
//            }
//        }
//        StdDraw.setPenColor(Color.BLUE);
//        double xFPnt, yFPnt, xTPnt, yTPnt;
//        for (Map.Entry<String, Hashtable<String, Float>> entrySet : transition_probability.entrySet()) {
//            String key = entrySet.getKey();
//            Hashtable<String, Float> toValues = entrySet.getValue();
//            double[] fromCart = getCoordinates(key);
//            xFPnt = fromCart[0];
//            yFPnt = fromCart[1];
//            if (xFPnt == -1) {
//                continue;
//            }
//            for (Map.Entry<String, Float> toentry : toValues.entrySet()) {
//                String tokey = toentry.getKey();
//                double[] toCart = getCoordinates(tokey);
//                xTPnt = toCart[0];
//                yTPnt = toCart[1];
//                if (xTPnt == -1) {
//                    continue;
//                }
//                StdDraw.line(xFPnt, yFPnt, xTPnt, yTPnt);
//            }
//
//        }
        ploter.display_save();
    }
}
