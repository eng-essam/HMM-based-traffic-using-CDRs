/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trajectory.POI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.math3.stat.Frequency;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.DataHandler;

/**
 *
 * @author essam
 */
public class Scan {

    /**
     *
     * @param seq
     * @param nvor
     * @param min_pnts
     * @return
     */
    private List<Integer> visited;

    private List<Integer> noise;

    /**
     * Convert sequence into array of integer observations
     *
     * @param seq
     * @return
     */
    public int[] convert(List<Integer> seq) {

        int i_segs[] = new int[seq.size()];
        for (int i = 0; i < seq.size(); i++) {
            int twr = seq.get(i);
            i_segs[i] = twr;
        }
        return i_segs;
    }

    /**
     * Convert sequence into array of integer observations
     *
     * @param seq
     * @return
     */
    public int[] convert(String seq) {
        String segs[] = seq.split(",");
        int i_segs[] = new int[segs.length];
        for (int i = 0; i < segs.length; i++) {
            int twr = Integer.parseInt(segs[i]);
            i_segs[i] = twr;
        }
        return i_segs;
    }

    /**
     * Add points
     *
     * @param freq
     * @param n
     * @return
     */
    public Cluster expand_cluster(Hashtable<Integer, Integer> freq, List<Integer> n) {
        Cluster c = new Cluster();
        int h_freq = Integer.MIN_VALUE;
        int c_zone = -1;
        for (Iterator<Integer> iterator = n.iterator(); iterator.hasNext();) {
            int twr = iterator.next();
            if (freq.containsKey(twr)) {
                int f = freq.get(twr);
                // get the highest frequency, to be the centroid of the cluster ..
                if (f > h_freq) {
                    h_freq = f;
                    c_zone = twr;
                }

                // mark visited zones 
                if (!visited.contains(twr)) {
                    visited.add(twr);
                }
//                else {
//                    System.out.println("Error: zone visited before by another cluster");
//                }
//                if (!c.contains(twr)) {
                c.addpoint(twr);
//                }
            }
        }

        c.set_centroid(c_zone);
        return c;
    }

    /**
     * Get frequencies of visited towers ..
     *
     * @param segs
     * @return
     */
    public Hashtable<Integer, Integer> get_freq(int[] segs) {
        Hashtable<Integer, Integer> twr_freq = new Hashtable<>();

        Frequency freq = new Frequency();
        for (int i = 0; i < segs.length; i++) {
            freq.addValue(segs[i]);

            if (!twr_freq.containsKey(segs[i])) {
                twr_freq.put(segs[i], -1);
            }
        }

        for (Map.Entry<Integer, Integer> entrySet : twr_freq.entrySet()) {
            int key = entrySet.getKey();
            int count = (int) freq.getCount(key);
            twr_freq.replace(key, count);
        }
        return twr_freq;
    }
    /**
     * get sample of POIs bounded by a specific region ...
     *
     * @param poi
     * @param stwr
     * @param etwr
     * @return
     */
    public Hashtable<Integer, List<trajectory.POI.Cluster>> get_poi_region(Hashtable<Integer, List<trajectory.POI.Cluster>> poi, int stwr, int etwr) {
        Hashtable<Integer, List<trajectory.POI.Cluster>> sample = new Hashtable<>();
//        System.out.println("POI size:\t" + poi.size());
        outter:
        for (Map.Entry<Integer, List<trajectory.POI.Cluster>> entrySet : poi.entrySet()) {
            Integer usr_key = entrySet.getKey();
            List<trajectory.POI.Cluster> value = entrySet.getValue();

            for (Iterator<trajectory.POI.Cluster> iterator = value.iterator(); iterator.hasNext();) {
                List<Integer> points = iterator.next().getpoints();
                for (Iterator<Integer> iterator1 = points.iterator(); iterator1.hasNext();) {
                    int pnt = iterator1.next();
                    if (pnt < stwr || pnt > etwr) {
                        continue outter;
                    }

                }

            }
            sample.put(usr_key, value);
        }
//        System.out.println("Sample size:\t" + sample.size());
        return sample;
    }

    public Hashtable<Integer, List<Cluster>> read_poi_file(String path) {
        Hashtable<Integer, List<Cluster>> clusters = new Hashtable<>();

        List<String> data = readfile(path);
        int id = -1;
        for (int i = 2; i < data.size(); i++) {
            String seg = data.get(i);
            if (seg.equals("user")) {
                i++;
                id = Integer.parseInt(data.get(i));
                i += 2;
                int n = Integer.parseInt(data.get(i));
                i++;
                List<Cluster> uc = new ArrayList<>();
                for (int j = 0; j < n; j++, i++) {
                    Cluster c = new Cluster<Integer>();
                    seg = data.get(i);
                    String s_seg[] = seg.split(",");
                    c.set_centroid(Integer.parseInt(s_seg[0]));
                    for (int k = 1; k < s_seg.length; k++) {
                        int s_seg1 = Integer.parseInt(s_seg[k]);
                        c.addpoint(s_seg1);
                    }
                    uc.add(c);

                }
                clusters.put(id, uc);
            } else {
                System.out.println("Error: " + seg);
            }

        }
        return clusters;
    }

    /**
     * Read both the transitions probabilities XML file and emissions as well.
     *
     * @param path
     * @return
     */
    public Hashtable<Integer, List<Cluster>> read_xml_poi(String path) {
        Hashtable<Integer, List<Cluster>> table = new Hashtable<>();
        org.w3c.dom.Document dpDoc;
//        for (int zone = 1; zone < 300; zone++) {
//            System.out.println("zone:\t"+zone);
        File dpXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dpDoc = dBuilder.parse(dpXmlFile);
            dpDoc.getDocumentElement().normalize();
            NodeList usrsPOIList = dpDoc.getElementsByTagName("user_node");

            for (int i = 0; i < usrsPOIList.getLength(); i++) {
                Node fromNodeNode = usrsPOIList.item(i);
                if (fromNodeNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) fromNodeNode;
                    int usr_id = Integer.parseInt(eElement.getAttribute("id"));

                    NodeList clusterList = eElement.getElementsByTagName("cluster");
                    List<Cluster> u_clusters = new ArrayList<>();
                    for (int j = 0; j < clusterList.getLength(); j++) {
                        Node clusterNode = clusterList.item(j);
                        if (clusterNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element clusterNodeElement = (Element) clusterNode;
                            Cluster<Integer> c = new Cluster<>();
                            int id = Integer.parseInt(clusterNodeElement.getAttribute("id"));
                            c.setId(id);
                            int centroid = Integer.parseInt(clusterNodeElement.getAttribute("centroid"));
                            c.set_centroid(centroid);
                            String elements = clusterNodeElement.getAttribute("members");
                            String e[] = elements.split(",");
                            for (int k = 0; k < e.length; k++) {
                                String e1 = e[k];
                                c.addpoint(Integer.parseInt(e1));

                            }
                            u_clusters.add(c);

                        }
                    }
                    table.put(usr_id, u_clusters);
                }

            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(Scan.class.getName()).log(Level.SEVERE, null, ex);
        }

        return table;
    }

    /**
     * Read file
     *
     * @param path
     * @return
     */
    private List<String> readfile(String path) {
        List<String> data = new ArrayList<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
            String line;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    
    public List<Integer> remove_visited(List<Integer> neighbours) {
        List<Integer> updated_ghbrs = new ArrayList<>();
        for (Iterator<Integer> iterator = neighbours.iterator(); iterator.hasNext();) {
            int zone = iterator.next();
            if (!visited.contains(zone)) {
                updated_ghbrs.add(zone);
            }
        }
        return updated_ghbrs;
    }

    public List<Cluster> scan_clusters(List<Integer> seq, Hashtable<Integer, ArrayList<Integer>> nvor, int min_pnts) {
        List<Cluster> clusters = new ArrayList<>();

        int[] i_seq = convert(seq);
        Hashtable<Integer, Integer> freq = sort_freq(get_freq(i_seq));
        visited = new ArrayList<>();
        noise = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entrySet : freq.entrySet()) {
            int zone_key = entrySet.getKey();
            if (visited.contains(zone_key)) {
                continue;
            }
            visited.add(zone_key);
//            long n = 1;
//            List<Integer> neighbours = null;
            // to avoid null exception for zones outside the targted range ..
//            if (nvor.containsKey(zone_key)) {

            List<Integer> neighbours = remove_visited(nvor.get(zone_key));
            neighbours.add(zone_key);

            long n = sum_neighbours(freq, neighbours);
//            }

            if (n > min_pnts) {
                Cluster c = expand_cluster(freq, neighbours);
                clusters.add(c);
            } else {
                noise.add(zone_key);
            }

        }
        /**
         * set IDs of the final cluster to be used in identifying locations
         * meaning....
         */
        for (int i = 0; i < clusters.size(); i++) {
            Cluster c = clusters.get(i);
            c.setId(i);
            clusters.set(i, c);

        }
        return clusters;
    }

    public List<Cluster> scan_clusters(String seq, Hashtable<Integer, ArrayList<Integer>> nvor, int min_pnts) {
        List<Cluster> clusters = new ArrayList<>();

        int[] i_seq = convert(seq);
        Hashtable<Integer, Integer> freq = sort_freq(get_freq(i_seq));
        visited = new ArrayList<>();
        noise = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entrySet : freq.entrySet()) {
            int zone_key = entrySet.getKey();
            if (visited.contains(zone_key)) {
                continue;
            }
            long n = 1;
            List<Integer> neighbours = null;
            // to avoid null exception for zones outside the targted range ..
            if (nvor.containsKey(zone_key)) {
                neighbours = nvor.get(zone_key);
                neighbours.add(zone_key);
                // remove visited nodes from the neighbors list ...
                neighbours = remove_visited(neighbours);

                n = sum_neighbours(freq, neighbours);
            }

            if (n > min_pnts) {
                Cluster c = expand_cluster(freq, neighbours);
                clusters.add(c);
            } else {
                visited.add(zone_key);
                noise.add(zone_key);
            }

        }
        return clusters;
    }

    /**
     * sort hashtable in an ascending order ...
     *
     * @param t
     * @return
     */
    public Hashtable<Integer, Integer> sort_freq(Hashtable<Integer, Integer> t) {

        Hashtable<Integer, Integer> sorted = new Hashtable();

        ArrayList<Map.Entry<Integer, Integer>> l = new ArrayList(t.entrySet());
        Collections.sort(l, new Comparator<Map.Entry<?, Integer>>() {

            @Override
			public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        System.out.println(l);
        for (Iterator<Map.Entry<Integer, Integer>> iterator = l.iterator(); iterator.hasNext();) {
            Map.Entry<Integer, Integer> next = iterator.next();
            sorted.put(next.getKey(), next.getValue());
//            System.out.println(next.getKey() + "\t" + next.getValue());
        }

        return sorted;
    }

    /**
     * Sum frequencies of a specific list of regions
     *
     * @param freq
     * @param neighbours
     * @return
     */
    public long sum_neighbours(Hashtable<Integer, Integer> freq, List<Integer> neighbours) {
        long sum = 0;
        for (Iterator<Integer> iterator = neighbours.iterator(); iterator.hasNext();) {
            int twr = iterator.next();
            if (freq.containsKey(twr)) {
                sum += freq.get(twr);
            }

        }
        return sum;
    }

    public void write_poi(Hashtable<Integer, List<Cluster>> u_poi, String path) {

        BufferedWriter writer = null;
        try {
            File logFile = new File(path);
            writer = new BufferedWriter(new FileWriter(logFile));

            writer.write("nusers\n");
            writer.write(u_poi.size() + "\n");
            for (Map.Entry<Integer, List<Cluster>> entrySet : u_poi.entrySet()) {
                Integer key = entrySet.getKey();
                writer.write("user\n");
                writer.write(key + "\n");
                List<Cluster> value = entrySet.getValue();
                writer.write("nclusters\n" + value.size() + "\n");
                for (Iterator<Cluster> iterator = value.iterator(); iterator.hasNext();) {
                    Cluster c = iterator.next();
                    List<Integer> pnts = c.getpoints();
//                    DoublePoint c = get_centriod(pnts);
                    writer.write(c.get_centroid().toString());
                    for (Iterator<Integer> iterator1 = pnts.iterator(); iterator1.hasNext();) {
                        int next = iterator1.next();
                        writer.write("," + next);
                    }
                    writer.newLine();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    public void write_xml_poi(Hashtable<Integer, List<Cluster>> u_poi, String path) {
        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("POI");
            doc.appendChild(rootElement);

            for (Map.Entry<Integer, List<Cluster>> entrySet : u_poi.entrySet()) {
                Integer usr_key = entrySet.getKey();
                List<Cluster> u_clusters = entrySet.getValue();

                Element usrNodeElement = doc.createElement("user_node");
                rootElement.appendChild(usrNodeElement);
                Attr attr = doc.createAttribute("id");
                attr.setValue(usr_key.toString());
                usrNodeElement.setAttributeNode(attr);

                for (int i = 0; i < u_clusters.size(); i++) {
                    Cluster c = u_clusters.get(i);

                    Element clusterNodeElement = doc.createElement("cluster");
                    usrNodeElement.appendChild(clusterNodeElement);

                    // set attribute to staff element
                    attr = doc.createAttribute("id");
                    attr.setValue(Integer.toString(c.getId()));
                    clusterNodeElement.setAttributeNode(attr);

                    attr = doc.createAttribute("centroid");
                    attr.setValue(c.get_centroid().toString());
                    clusterNodeElement.setAttributeNode(attr);

                    attr = doc.createAttribute("members");
                    attr.setValue(c.toString());
                    clusterNodeElement.setAttributeNode(attr);
                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }

}
