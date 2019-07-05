/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netconvert.test;

import java.util.ArrayList;

import mergexml.NetConstructor;
import netconvert.netconverter;
import utils.Edge;

/**
 *
 * @author essam
 */
public class main_netconvert {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	double scale = Double.parseDouble(args[0]);
        String path =args[1];
        String edge_path = args[2];
        
        
//        long scale =1000;
        
//alex
//        double minlat = 30.8362;
//        double minlon = 29.4969;
//netconverter nc = new netconverter(minlat, minlon);

        netconverter nc = new netconverter();

        nc.read_osm(path);

        ArrayList<Edge> edges = new ArrayList<>(nc.construct_map_edges());
        NetConstructor.write_edges(edges, edge_path);
        
        ArrayList<Edge> edges_no_proj = new ArrayList<>(nc.construct_map_edges_no_proj(scale));
        
        edge_path = edge_path.replace(".xml", "_no_proj.xml");
        NetConstructor.write_edges(edges_no_proj, edge_path);
        
//        List<Node> nodes = nc.getNodes();
//        System.out.println(nc.getXmin()+"\t"+nc.getYmin());
//        Node nd = nc.get_node(-144680);
//        System.out.println(nd.getLat()+"\t"+nd.getLon()+"\t"+nd.getX()+"\t"+nd.getY());
//        
//       try {
//
//            File file = new File(path);
//            JAXBContext jaxbContext = JAXBContext.newInstance(Osm.class);
//
//            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//            Osm osm = (Osm) jaxbUnmarshaller.unmarshal(file);
//            List<Way> ways = osm.getWay();
//            
//            for (Iterator<Way> iterator = ways.iterator(); iterator.hasNext();) {
//                Way way = iterator.next();
//                if (way.getId() == 24450797) {
//                    List<Way.Nd> nds = way.getNd();
//                    for (Iterator<Way.Nd> iterator1 = nds.iterator(); iterator1.hasNext();) {
//                        Way.Nd nd = iterator1.next();
//                        System.out.println(nd.getRef());
//                    }
//                    
//                }
//            }
////		System.out.println(customer);
//        } catch (JAXBException e) {
//            e.printStackTrace();
//        }
    }

}
