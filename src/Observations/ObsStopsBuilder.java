/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import mergexml.MergeXML;
import utils.ToNode;
import utils.Vertex;

/**
 *
 * @author essam
 */
public class ObsStopsBuilder {

    public static void buildObs_Time(String srcPath, String dstPath, int stower, int etower) {

        /**
         * Date format: 2013-01-26 13:00:00 yyyy-MM-dd HH:mm:ss
         */
        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BufferedReader reader;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Observations");
            doc.appendChild(rootElement);

            reader = new BufferedReader(new FileReader(srcPath));

            String line;
            String id = null;
            String nextID;
            int nextDay = -1;
            int curDay = -1;
            String siteID;
            StringBuilder obsr = null;
            /**
             * Towers rang delimiter.
             */
            char rlm = '/';
            /**
             * Date delimiter.
             */
            char dlm = '$';

            /**
             * Data must be sorted by the user ID Data set Arrana=ged as the
             * following ID Time Site ID
             */
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");

                nextID = lineSplit[0].trim();
                try {
                    Date nextDate = parserSDF.parse(lineSplit[1]);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(nextDate);
                    nextDay = cal.get(Calendar.DAY_OF_MONTH);
//cur = parserSDF.parse(lineSplit[1]).getDay();
                } catch (ParseException ex) {
                    Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (id == null) {
                    id = nextID;
                    curDay = nextDay;
                }
                siteID = lineSplit[2].trim();
                int site = Integer.parseInt(siteID);

                /**
                 * If the Date changed insert date delimiter.
                 */
                if (curDay != nextDay) {
//                    System.out.println("date changed");
                    if (obsr != null) {
                        obsr.append(dlm);
                    }
                    curDay = nextDay;
                }
                if (obsr == null && site >= stower && site <= etower) {
                    obsr = new StringBuilder(siteID);
                } else if (obsr != null && id.equals(nextID)) {

                    if (site >= stower && site <= etower) {
                        if (rlm != obsr.charAt(obsr.length() - 1)
                                || dlm != obsr.charAt(obsr.length() - 1)) {
                            obsr.append(",");
                        }
                        obsr.append(siteID);
                    } else if (rlm != obsr.charAt(obsr.length() - 1) || dlm != obsr.charAt(obsr.length() - 1)) {
//                        System.out.println(obsr.charAt(obsr.length() - 1));
//                         obsr.append(",");
                        obsr.append(rlm);
                    }

                } else {
                    /**
                     * User changed write the XL file Data Change the user ID ,
                     * and record site with the new user
                     */

                    if (obsr != null) {
                        Element userElement = doc.createElement("user");
                        rootElement.appendChild(userElement);

                        // set attribute to staff element
                        Attr attr = doc.createAttribute("id");
                        attr.setValue(id);
                        userElement.setAttributeNode(attr);

                        attr = doc.createAttribute("sitesSequanace");
                        attr.setValue(obsr.toString());
                        userElement.setAttributeNode(attr);
                    }
                    id = nextID;
                    obsr = null;

                }

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(dstPath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }
    public static void buildObsSeq(String srcPath, String dstPath, int stower, int etower) {

        BufferedReader reader;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Observations");
            doc.appendChild(rootElement);

            reader = new BufferedReader(new FileReader(srcPath));

            String line;
            String id = null;
            String nextID;
            String siteID;
            StringBuilder obsr = null;
            char dlm = '/';
            /**
             * Data must be sorted by the user ID Data set Arrana=ged as the
             * following ID Time Site ID
             */
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");

                nextID = lineSplit[0].trim();

                if (id == null) {
                    id = nextID;
                }
                siteID = lineSplit[2].trim();
                int site = Integer.parseInt(siteID);

                if (obsr == null && site >= stower && site <= etower) {
                    obsr = new StringBuilder(siteID);
                } else if (obsr != null && id.equals(nextID)) {

                    if (site >= stower && site <= etower) {
                        if (dlm != obsr.charAt(obsr.length() - 1)) {
                            obsr.append(",");
                        }
                        obsr.append(siteID);
                    } else if (dlm != obsr.charAt(obsr.length() - 1)) {
//                        System.out.println(obsr.charAt(obsr.length() - 1));
//                         obsr.append(",");
                        obsr.append(dlm);
                    }

                } else {
                    /**
                     * User changed write the XL file Data Change the user ID ,
                     * and record site with the new user
                     */

                    if (obsr != null) {
                        Element userElement = doc.createElement("user");
                        rootElement.appendChild(userElement);

                        // set attribute to staff element
                        Attr attr = doc.createAttribute("id");
                        attr.setValue(id);
                        userElement.setAttributeNode(attr);

                        attr = doc.createAttribute("sitesSequanace");
                        attr.setValue(obsr.toString());
                        userElement.setAttributeNode(attr);
                    }
                    id = nextID;
                    obsr = null;

                }

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(dstPath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }
    public static ArrayList<String> listFilesForFolder(final File folder) {
        ArrayList<String> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                files.add(fileEntry.getPath());
//                System.out.println(fileEntry.getName());
            }
        }
        return files;
    }
    public static Hashtable<Integer, Hashtable<Integer, Obs>> readObsDUT(String path) {
        Hashtable<Integer, Hashtable<Integer, Obs>> table = new Hashtable<>();
        org.w3c.dom.Document dpDoc;
//        for (int zone = 1; zone < 300; zone++) {
//            System.out.println("zone:\t"+zone);
        File dpXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dpDoc = dBuilder.parse(dpXmlFile);
            dpDoc.getDocumentElement().normalize();
            NodeList daysList = dpDoc.getElementsByTagName("week_day");

            for (int i = 0; i < daysList.getLength(); i++) {

                Node dayNode = daysList.item(i);

                if (dayNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) dayNode;
                    int day = Integer.parseInt(eElement.getAttribute("id"));

                    ToNode edge;
                    NodeList toNodeList = eElement.getElementsByTagName("user");
                    Hashtable<Integer, Obs> usrsTable = new Hashtable<>();
                    for (int j = 0; j < toNodeList.getLength(); j++) {
                        Node toNodeNode = toNodeList.item(j);
                        if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element usrElement = (Element) toNodeNode;

                            String id = usrElement.getAttribute("id");
                            Obs obs = new Obs(usrElement.getAttribute("seq"),
                                    usrElement.getAttribute("timestamp"),
                                    usrElement.getAttribute("viterbi"));
                            usrsTable.put(Integer.parseInt(id), obs);
                        }
                    }
                    table.put(day, usrsTable);
                }

            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return table;
    }
    /**
     *
     * Sun 07 Dec 2014 01:37:23 AM EET
     *
     * Write Observation tables
     *
     * @param obsTable
     * @param dstPath
     */
    public static void writeObsDUT(Hashtable<Integer, Hashtable<Integer, Obs>> obsTable, String dstPath) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("observation");
            doc.appendChild(rootElement);

            for (Map.Entry<Integer, Hashtable<Integer, Obs>> entrySet : obsTable.entrySet()) {
                Integer userID = entrySet.getKey();
                Element dayElement = doc.createElement("week_day");
                rootElement.appendChild(dayElement);

                // set attribute to staff element
                Attr attr = doc.createAttribute("id");
                attr.setValue(String.valueOf(userID));
                dayElement.setAttributeNode(attr);
//            System.out.println(pKey);
                Hashtable<Integer, Obs> value = entrySet.getValue();
                for (Map.Entry<Integer, Obs> entrySet1 : value.entrySet()) {
                    Integer weekDay = entrySet1.getKey();
                    Obs obs = entrySet1.getValue();

                    Element usrSeqElement = doc.createElement("user");
                    dayElement.appendChild(usrSeqElement);

                    attr = doc.createAttribute("id");
                    attr.setValue(String.valueOf(weekDay));
                    usrSeqElement.setAttributeNode(attr);

                    attr = doc.createAttribute("seq");
                    attr.setValue(obs.seq);
                    usrSeqElement.setAttributeNode(attr);

                    attr = doc.createAttribute("viterbi");
                    attr.setValue(obs.vitPath);
                    usrSeqElement.setAttributeNode(attr);

                    attr = doc.createAttribute("timestamp");
                    attr.setValue(obs.timeStamp);
                    usrSeqElement.setAttributeNode(attr);

                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(dstPath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }

    String dSPath;

    String xmlPath;

    final String RLM = "/";

    final String CLM = ",";

    final int HOUR = 60;

    public ObsStopsBuilder() {
    }

    public ObsStopsBuilder(String dSPath, String xmlPath) {
        this.dSPath = dSPath;
        this.xmlPath = xmlPath;
    }

    /**
     *
     * @param obs
     * @param distance_threshold
     * @param timing_threshold
     * @return
     */
    public Hashtable<Integer, Hashtable<String, Obs>> adaptiveTripStops(
            Hashtable<Integer, Hashtable<String, Obs>> obs,
            Hashtable<Integer, Vertex> towersXY,
            int dist_th, int time_th) {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        for (Map.Entry<Integer, Hashtable<String, Obs>> entrySet : obs.entrySet()) {
            Integer userID = entrySet.getKey();
            Hashtable<String, Obs> dailyObs = entrySet.getValue();
            for (Map.Entry<String, Obs> dailyObsEntry : dailyObs.entrySet()) {
                String dayID = dailyObsEntry.getKey();
                Obs observations = dailyObsEntry.getValue();
                String seq = observations.seq;
                String timeStamp = observations.timeStamp;
                /**
                 * Initiate daily observation trips
                 */
                String[] obsTowers = new String[]{seq};
                String[] obsTimes = new String[]{timeStamp};
                if (observations.seq.contains(RLM)) {
                    obsTowers = seq.split(RLM);
                    obsTimes = timeStamp.split(RLM);
                }

                /**
                 * generate partial trips (trips based on time stamp and
                 * distances).
                 */
                for (int i = 0; i < obsTimes.length; i++) {
                    try {
                        String[] towers = obsTowers[i].split(CLM);
                        String[] tstamps = obsTimes[i].split(CLM);

                        Obs newObs = findStops(towers, tstamps, towersXY, dist_th, time_th);

                        obsTowers[i] = newObs.seq;
                        obsTimes[i] = newObs.timeStamp;
                    } catch (ParseException ex) {
                        Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                /**
                 * Update observations for the current user.
                 */
                seq = "";
                timeStamp = "";
                for (int i = 0; i < obsTimes.length; i++) {
                    if (!seq.isEmpty()) {
                        seq += RLM;
                        timeStamp += RLM;
                    }
                    seq += obsTowers[i];
                    timeStamp += obsTimes[i];

                }
                observations = new Obs(seq, timeStamp);
                dailyObs.replace(dayID, observations);
            }
            /**
             * Update observations for the current day.
             */
            obs.replace(userID, dailyObs);
        }

        return obs;
    }
 /**
 * Sun 07 Dec 2014 01:37:23 AM EET
 *
 * Build observations per days for each user from CDRs with time stamp for
 * each observation.
 *
 * @param srcPath
 * @param dstPath
 * @param stower
 * @param etower
 * @return Hashtable<UserID, Hashtable<Week Day, Observation Sequence>>
 * obstable
 */
public Hashtable<Integer, Hashtable<Integer, Obs>> buildObsDUT(String srcPath, int stower, int etower) {

    /**
     * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
     */
    Hashtable<Integer, Hashtable<Integer, Obs>> obstable = new Hashtable<>();

    SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    BufferedReader reader;
    try {
        reader = new BufferedReader(new FileReader(srcPath));

        String line;
        String id = null;
        String nextID;
        int nextDay = -1;
        String time = null;
        int curDay = -1;
        String siteID;
        StringBuilder obsr = new StringBuilder();
        StringBuilder tObsr = new StringBuilder();
        /**
         * Towers rang delimiter.
         */
        char rlm = '/';

        /**
         * Data must be sorted by the user ID Data set Arrana=ged as the
         * following ID Time Site ID
         */
        Hashtable<Integer, Obs> userObs = null;
        while ((line = reader.readLine()) != null) {
            String lineSplit[] = line.split(",");

            nextID = lineSplit[0].trim();
//                if (Integer.parseInt(nextID) == 200) {
//                    break;
//                }
            try {
                Date nextDate = parserSDF.parse(lineSplit[1]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextDate);
                nextDay = cal.get(Calendar.DAY_OF_MONTH);
//                    time= cal .getTime().toString();
                time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

            } catch (ParseException ex) {
                Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (id == null) {
                userObs = new Hashtable<>();
                id = nextID;
                curDay = nextDay;
            }
            siteID = lineSplit[2].trim();
            int site = Integer.parseInt(siteID);

            /**
             * If the Date changed insert date delimiter.
             */
            if (curDay != nextDay) {
//                    System.out.println("date changed");
//                    if (obsr != null) {
//                        obsr.append(dlm);
//                        
//                    }

//                    if (obsr == null) {
//                        obsr = new StringBuilder(RLM);
//                    }
                if (obsr.length() != 0) {
                    userObs.put(curDay, new Obs(obsr.toString(), tObsr.toString()));
                    obsr = new StringBuilder();
                    tObsr = new StringBuilder();
                }
                curDay = nextDay;

//                    if (site >= stower && site <= etower) {
//                        obsr = new StringBuilder(siteID);
//                    } else {
//                        obsr = new StringBuilder(RLM);
//                    }
//                    continue;
            }

            if (obsr.length() == 0 && site >= stower && site <= etower) {
                obsr.append(siteID);
                tObsr.append(time);
            } else if (obsr.length() != 0 && id.equals(nextID)) {

                /**
                 * Current observation in the current region
                 */
                if (site >= stower && site <= etower) {

                    if (rlm != obsr.charAt(obsr.length() - 1)) {
                        obsr.append(CLM);
                        tObsr.append(CLM);
                    }
                    obsr.append(siteID);
                    tObsr.append(time);
                } else if (rlm != obsr.charAt(obsr.length() - 1)) {
//                        System.out.println(obsr.charAt(obsr.length() - 1));
//                         obsr.append(",");
                    obsr.append(rlm);
                    tObsr.append(rlm);
                }

            } else {
                /**
                 * User changed write the XL file Data Change the user ID ,
                 * and record site with the new user
                 */
                if (!userObs.isEmpty()) {
                    obstable.put(Integer.parseInt(id), userObs);
                    userObs = new Hashtable<>();
                }
                id = nextID;
                obsr = new StringBuilder();
                tObsr = new StringBuilder();

            }

        }

//            System.out.println("File saved!");
    } catch (FileNotFoundException ex) {
        Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
        Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
    }
    return obstable;
}

    public void buildObsSeq() {

        BufferedReader reader;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Observations");
            doc.appendChild(rootElement);

            reader = new BufferedReader(new FileReader(this.dSPath));

            String line;
            String id = null;
            String nextID;
            String siteID;
            StringBuilder obsr = null;

            /**
             * Data must be sorted by the user ID Data set Arrana=ged as the
             * following ID Time Site ID
             */
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");

                nextID = lineSplit[0].trim();

                if (id == null) {
                    id = nextID;
                }
                siteID = lineSplit[2].trim();

                if (obsr == null) {
                    obsr = new StringBuilder(siteID);
                } else if (id.equals(nextID)) {
                    obsr.append(",");
                    obsr.append(siteID);
                } else {
                    /**
                     * User changed write the XL file Data Change the user ID ,
                     * and record site with the new user
                     */

                    Element userElement = doc.createElement("user");
                    rootElement.appendChild(userElement);

                    // set attribute to staff element
                    Attr attr = doc.createAttribute("id");
                    attr.setValue(id);
                    userElement.setAttributeNode(attr);

                    attr = doc.createAttribute("sitesSequanace");
                    attr.setValue(obsr.toString());
                    userElement.setAttributeNode(attr);

                    id = nextID;
                    obsr = new StringBuilder(siteID);

                }

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(this.xmlPath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }

    /**
     * Mon 01 Dec 2014 11:37:23 PM EET
     *
     * Build observations per days for each user from CDRs.
     *
     * @param srcPath
     * @param dstPath
     * @param stower
     * @param etower
     * @return
     */
    public Hashtable<Integer, Hashtable<Integer, String>> buildObsTU(String srcPath, int stower, int etower) {

        /**
         * Hashtable<UserID, Hashtable<Week Day, Observation Sequence>> obstable
         */
        Hashtable<Integer, Hashtable<Integer, String>> obstable = new Hashtable<>();

        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(srcPath));

            String line;
            String id = null;
            String nextID;
            int nextDay = -1;
            int curDay = -1;
            String siteID;
            StringBuilder obsr = new StringBuilder();

            /**
             * Towers rang delimiter.
             */
            char rlm = '/';
            /**
             * Date delimiter.
             */
            char dlm = '$';

            /**
             * Data must be sorted by the user ID Data set Arrana=ged as the
             * following ID Time Site ID
             */
            Hashtable<Integer, String> userObs = null;
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");

                nextID = lineSplit[0].trim();
//                if (Integer.parseInt(nextID) == 50) {
//                    break;
//                }
                try {
                    Date nextDate = parserSDF.parse(lineSplit[1]);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(nextDate);
                    nextDay = cal.get(Calendar.DAY_OF_MONTH);
                } catch (ParseException ex) {
                    Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (id == null) {
                    userObs = new Hashtable<>();
                    id = nextID;
                    curDay = nextDay;
                }
                siteID = lineSplit[2].trim();
                int site = Integer.parseInt(siteID);

                /**
                 * If the Date changed insert date delimiter.
                 */
                if (curDay != nextDay) {
//                    System.out.println("date changed");
//                    if (obsr != null) {
//                        obsr.append(dlm);
//                        
//                    }

//                    if (obsr == null) {
//                        obsr = new StringBuilder(RLM);
//                    }
                    if (obsr.length() != 0) {
                        userObs.put(curDay, obsr.toString());
                        obsr = new StringBuilder();
                    }
                    curDay = nextDay;

//                    if (site >= stower && site <= etower) {
//                        obsr = new StringBuilder(siteID);
//                    } else {
//                        obsr = new StringBuilder(RLM);
//                    }
//                    continue;
                }

                if (obsr.length() == 0 && site >= stower && site <= etower) {
                    obsr.append(siteID);
                } else if (obsr.length() != 0 && id.equals(nextID)) {

                    /**
                     * Current observation in the current region
                     */
                    if (site >= stower && site <= etower) {

                        if (rlm != obsr.charAt(obsr.length() - 1)) {
                            obsr.append(",");
                        }
                        obsr.append(siteID);
                    } else if (rlm != obsr.charAt(obsr.length() - 1)) {
//                        System.out.println(obsr.charAt(obsr.length() - 1));
//                         obsr.append(",");
                        obsr.append(rlm);
                    }

                } else {
                    /**
                     * User changed write the XL file Data Change the user ID ,
                     * and record site with the new user
                     */
                    if (!userObs.isEmpty()) {
                        obstable.put(Integer.parseInt(id), userObs);
                        userObs = new Hashtable<>();
                    }
                    id = nextID;
                    obsr = new StringBuilder();

                }

            }

//            System.out.println("File saved!");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obstable;
    }

    /**
     * Calculate the Eculidean distance.
     *
     * @param x
     * @param x1
     * @param y
     * @param y1
     * @return
     */
    private double eculidean(double x, double x1, double y, double y1) {
        return Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));
    }

    /**
	     * Identify the stop points
	     *
	     * @param towers
	     * @param tstamps
	     * @param towersXY
	     * @param dist_th
	     * @param time_th
	     * @return
	     * @throws ParseException
	     */
	    public Obs findStops(String[] towers, String[] tstamps,
	            Hashtable<Integer, Vertex> towersXY,
	            int dist_th, int time_th) throws ParseException {
	
	        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
	        Calendar cal = Calendar.getInstance();
	
	        /**
	         * Stops sets and time stamps for these stops
	         */
	        ArrayList<String> stops = new ArrayList<>();
	        ArrayList<String> tstops = new ArrayList<>();
	
	        /**
	         * Buffers:
	         * <\n buffer holds sequence of observations that did not meet
	         * buffer clearance criterias.>
	         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
	         */
	        ArrayList<String> buffer = new ArrayList<>();
	        ArrayList<String> tbuffer = new ArrayList<>();
	        String trip = "";
	        String ttrip = "";
	        /**
	         * construct observations and time stamps
	         */
	        String time = "";
	        String obs = "";
	
	        double max_distance = 0;
	        int time_diff = 0;
	        for (int i = 0; i < towers.length; i++) {
	            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
	            for (int j = 0; j < buffer.size(); j++) {
	                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
	                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
	                if (tmp_distance > max_distance) {
	                    max_distance = tmp_distance;
	                }
	
	            }
	//            System.out.println(max_distance);
	            if (max_distance > dist_th) {
	//                System.out.println(max_distance);
	                /**
	                 * get time difference
	                 */
	
	                /**
	                 * if the time exceeds timing threshold, then check the distance
	                 * between towers. If this distance less than the distance
	                 * threshold, then previous tower is the end of the current
	                 * trip.
	                 *
	                 */
	                Date sTime = formatter.parse(tstamps[i]);
	                Date eTime = formatter.parse(tbuffer.get(0));
	                cal.setTime(sTime);
	                int hour = cal.get(Calendar.HOUR);
	                int minute = cal.get(Calendar.MINUTE);
	
	                cal.setTime(eTime);
	                int ehour = cal.get(Calendar.HOUR);
	                int eminute = cal.get(Calendar.MINUTE);
	
	                time_diff = (ehour - hour) * HOUR + (eminute - minute);
	
	                if (time_diff > time_th) {
	                    /**
	                     * Add buffer mode to the stops
	                     */
	                    int index = modeIndex(buffer);
	                    if (index != -1) {
	//                        System.out.println(Arrays.toString(buffer.toArray(new String[buffer.size()])));
	                        stops.add(buffer.get(index));
	                        tstops.add(tbuffer.get(index));
	                        if (!trip.isEmpty()) {
	                            trip += CLM;
	                            ttrip += CLM;
	                        }
	                        trip += buffer.get(index);
	                        ttrip += tbuffer.get(index);
	                    }
	
	                    /**
	                     * Add trips and time stamps to the observation sequences
	                     * then clear trips and time_trips string buffers.
	                     *
	                     */
	//                    System.out.println(Arrays.toString(towers));
	//                    System.out.println("Trip:" + trip);
	//                    System.out.println("Trip:" + ttrip);
	                    if (!trip.isEmpty()) {
	//                        System.out.println("\\-end");
	                        trip += RLM;
	                        ttrip += RLM;
	                    }
	                    time += ttrip;
	                    obs += trip;
	
	                    trip = "";
	                    ttrip = "";
	                } else {
	                    for (int j = 0; j < tbuffer.size(); j++) {
	
	                        if (!trip.isEmpty()) {
	                            trip += CLM;
	                            ttrip += CLM;
	                        }
	                        trip += buffer.get(j);
	                        ttrip += tbuffer.get(j);
	                    }
	//                    trip += Arrays.toString(buffer.toArray(new String[buffer.size()]));
	                }
	                buffer = new ArrayList<>();
	                tbuffer = new ArrayList<>();
	                /**
	                 * Reset maximum distances
	                 */
	                max_distance = 0;
	
	            }
	            buffer.add(towers[i]);
	            tbuffer.add(tstamps[i]);
	
	        }
	
	        if (!buffer.isEmpty()) {
	            /**
	             * Add buffer mode to the stops
	             */
	            int index = modeIndex(buffer);
	            if (index != -1) {
	//                System.out.println(Arrays.toString(buffer.toArray(new String[buffer.size()])));
	
	                stops.add(buffer.get(index));
	                tstops.add(tbuffer.get(index));
	                if (!trip.isEmpty()) {
	                    trip += CLM;
	                    ttrip += CLM;
	                }
	                trip += buffer.get(index);
	                ttrip += tbuffer.get(index);
	            }
	            //Record the last trip
	            time += ttrip;
	            obs += trip;
	        }
	
	//        System.out.println(obs + "\n" + time);
	        return new Obs(obs, time);
	
	    }

    /**
     * Identify the stop points
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     * @throws ParseException
     */
    public Obs findStops_old(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY,
            int dist_th, int time_th) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        /**
         * Stops sets and time stamps for these stops
         */
        ArrayList<String> stops = new ArrayList<>();
        ArrayList<String> tstops = new ArrayList<>();

        /**
         * Buffers:
         * <\n buffer holds sequence of observations that did not meet
         * buffer clearance criterias.>
         * <\n tbuffer holds time stamps values corresponding to those in the buffer.>
         */
        ArrayList<String> buffer = new ArrayList<>();
        ArrayList<String> tbuffer = new ArrayList<>();

        double max_distance = 0;
        for (int i = 0; i < towers.length; i++) {
            Vertex a = towersXY.get(Integer.parseInt(towers[i]));
            for (int j = 0; j < buffer.size(); j++) {
                Vertex b = towersXY.get(Integer.parseInt(buffer.get(j)));
                double tmp_distance = eculidean(a.getX(), b.getX(), a.getY(), b.getY());
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }

            }
            if (max_distance > dist_th) {
                /**
                 * get time difference
                 */

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                Date sTime = formatter.parse(tstamps[i]);
                Date eTime = formatter.parse(tbuffer.get(0));
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                int time_diff = (ehour - hour) * HOUR + (eminute - minute);

                if (time_diff > time_th) {
                    /**
                     * Add buffer mode to the stops
                     */
                    int index = modeIndex(buffer);
                    if (index != -1) {
                        stops.add(buffer.get(index));
                        tstops.add(tbuffer.get(index));
                    }

                } else {
                    /**
                     * Clear Buffers
                     */
                    buffer = new ArrayList<>();
                    tbuffer = new ArrayList<>();
                    /**
                     * Reset maximum distances
                     */
                    max_distance = 0;
                }

            }
            buffer.add(towers[i]);
            tbuffer.add(tstamps[i]);

        }
        if (!buffer.isEmpty()) {
            /**
             * Add buffer mode to the stops
             */
            int index = modeIndex(buffer);
            if (index != -1) {
//                stops = new ArrayList<>();
                stops.add(buffer.get(index));
//                tstops = new ArrayList<>();
                tstops.add(tbuffer.get(index));
            }
        }

        /**
         * construct observations and time stamps
         */
        String time = "";
        String obs = "";

        for (int i = 0; i < stops.size(); i++) {
            String s = stops.get(i);
            String t = tstops.get(i);

            if (!time.isEmpty()) {
                time += CLM;
                obs += CLM;
            }
            time += t;
            obs += s;
        }
        return new Obs(obs, time);

    }

    /**
     * Find trips from single sequence based on time stamps and towers
     * positions.
     *
     * @param towers
     * @param tstamps
     * @param towersXY
     * @param dist_th
     * @param time_th
     * @return
     */
    private Obs findTrips(String[] towers, String[] tstamps,
            Hashtable<Integer, Vertex> towersXY,
            int dist_th, int time_th) {
        /**
         * Marks array contain index of the towers that represent the starting
         * observation of a new trip.
         */
        boolean[] marks = new boolean[tstamps.length];
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        /**
         * add 0 as the start of the first tower in this seq.
         */
//        int mIndex = 0;
//        marks[mIndex++] = 0;

        int current = 0;
        for (int i = 1; i < tstamps.length; i++) {
            try {
                String tstamp = tstamps[i];

                /**
                 * if the time exceeds timing threshold, then check the distance
                 * between towers. If this distance less than the distance
                 * threshold, then previous tower is the end of the current
                 * trip.
                 *
                 */
                Date sTime = formatter.parse(tstamps[current]);
                Date eTime = formatter.parse(tstamp);
                cal.setTime(sTime);
                int hour = cal.get(Calendar.HOUR);
                int minute = cal.get(Calendar.MINUTE);

                cal.setTime(eTime);
                int ehour = cal.get(Calendar.HOUR);
                int eminute = cal.get(Calendar.MINUTE);

                int diff = (ehour - hour) * HOUR + (eminute - minute);
                /**
                 * check time difference with time threshold whatever the
                 * distance between the starting tower
                 */
                if (diff > time_th) {
                    /**
                     * Check distance, if it distance less than distance
                     * threshold mark the current tower as the start of a new
                     * trip.
                     */
                    Vertex sTower = towersXY.get(Integer.parseInt(towers[current]));
                    Vertex tower = towersXY.get(Integer.parseInt(towers[i]));

                    if (eculidean(sTower.getX(), tower.getX(), sTower.getY(), tower.getY()) < dist_th) {
                        /**
                         * Update the trip sequences
                         */
                        marks[i] = true;
                        current = i;
                    }

                }
            } catch (ParseException ex) {
                Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * construct observations and time stamps
         */
        String time = "";
        String obs = "";
        for (int i = 0; i < marks.length; i++) {
            if (marks[i]) {

                time += RLM + tstamps[i];
                obs += RLM + towers[i];
            } else {
                /**
                 * Add comma separators
                 */
                if (!time.isEmpty()) {
                    time += CLM;
                    obs += CLM;
                }
                time += tstamps[i];
                obs += towers[i];
            }

        }
        return new Obs(obs, time);
    }

    public String getdSPath() {
        return dSPath;
    }

    public String getXmlPath() {
        return xmlPath;
    }

    /**
     * Get the median index of an array list
     *
     * @param list
     * @return
     */
    public int modeIndex(ArrayList<String> list) {
        int index = -1;
        /**
         * If the list is empty return -1 as the median index, else if list size
         * is odd, make index it equal size/2+1 else make it equal size/2.
         */
        if (!list.isEmpty()) {
            list.trimToSize();
            int size = list.size();
            if (size % 2 != 0) {
                index = size / 2;
            } else {
                index = size / 2 - 1;
            }
        }
        return index;
    }

    /**
     * parse only distinct observations not all patterns exclude from the
     * observations can significantly determine users movement patterns
     */
    public void obtainDisObsr() {

        BufferedReader reader;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("Observations");
            doc.appendChild(rootElement);

            reader = new BufferedReader(new FileReader(this.dSPath));

            String line;
            String id = null;
            String nextID;
            String siteID;
            String siteID_Old = null;
            StringBuilder obsr = null;

            /**
             * Data must be sorted by the user ID Data set Arrana=ged as the
             * following ID Time Site ID
             */
            while ((line = reader.readLine()) != null) {
                String lineSplit[] = line.split(",");

                nextID = lineSplit[0].trim();

                if (id == null) {
                    id = nextID;
                }
                siteID = lineSplit[2].trim();

                if (siteID_Old == null) {
                    siteID_Old = siteID;
                } else if (siteID.equals(siteID_Old) && id.equals(nextID)) {
//                    System.out.println("same id");
                    continue;
                } else if (!siteID.equals(siteID_Old)) {
                    siteID_Old = siteID;
                }

                if (obsr == null) {
                    obsr = new StringBuilder(siteID);
                } else if (id.equals(nextID)) {
                    obsr.append(",");
                    obsr.append(siteID);
                } else {
                    /**
                     * User changed write the XL file Data Change the user ID ,
                     * and record site with the new user
                     */

                    Element userElement = doc.createElement("user");
                    rootElement.appendChild(userElement);

                    // set attribute to staff element
                    Attr attr = doc.createAttribute("id");
                    attr.setValue(id);
                    userElement.setAttributeNode(attr);

                    attr = doc.createAttribute("sitesSequanace");
                    attr.setValue(obsr.toString());
                    userElement.setAttributeNode(attr);

                    id = nextID;
                    obsr = new StringBuilder(siteID);

                }

            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(this.xmlPath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ObsStopsBuilder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }

    public Hashtable<Integer, Hashtable<Integer, String>> readObsTU(String path) {
        Hashtable<Integer, Hashtable<Integer, String>> table = new Hashtable<>();
        org.w3c.dom.Document dpDoc;
//        for (int zone = 1; zone < 300; zone++) {
//            System.out.println("zone:\t"+zone);
        File dpXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            dpDoc = dBuilder.parse(dpXmlFile);
            dpDoc.getDocumentElement().normalize();
            NodeList daysList = dpDoc.getElementsByTagName("week_day");

            for (int i = 0; i < daysList.getLength(); i++) {

                Node dayNode = daysList.item(i);

                if (dayNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) dayNode;
                    int day = Integer.parseInt(eElement.getAttribute("id"));

                    ToNode edge;
                    NodeList toNodeList = eElement.getElementsByTagName("user");
                    Hashtable<Integer, String> usrsTable = new Hashtable<>();
                    for (int j = 0; j < toNodeList.getLength(); j++) {
                        Node toNodeNode = toNodeList.item(j);
                        if (toNodeNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element usrElement = (Element) toNodeNode;
                            usrsTable.put(Integer.parseInt(usrElement.getAttribute("id")),
                                    usrElement.getAttribute("seq"));
                        }
                    }
                    table.put(day, usrsTable);
                }

            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(MergeXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return table;
    }

    public void setdSPath(String dSPath) {
        this.dSPath = dSPath;
    }

    public void setXmlPath(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    public Hashtable<Integer, Hashtable<Integer, String>> transposeWU(Hashtable<Integer, Hashtable<Integer, String>> obsTable) {
        Hashtable<Integer, Hashtable<Integer, String>> transposed = new Hashtable<>();

        for (Map.Entry<Integer, Hashtable<Integer, String>> entry : obsTable.entrySet()) {
            Integer userID = entry.getKey();
            Hashtable<Integer, String> dayObstable = entry.getValue();
            for (Map.Entry<Integer, String> daysEntry : dayObstable.entrySet()) {
                Integer weekday = daysEntry.getKey();
                String obsStr = daysEntry.getValue();

                if (transposed.containsKey(weekday)) {
                    Hashtable<Integer, String> usrObstable = transposed.get(weekday);
                    usrObstable.put(userID, obsStr);
                    transposed.replace(weekday, usrObstable);
                } else {
                    Hashtable<Integer, String> usrObstable = new Hashtable<>();
                    usrObstable.put(userID, obsStr);
                    transposed.put(weekday, usrObstable);
                }

            }

        }
        return transposed;
    }

    public Hashtable<Integer, Hashtable<Integer, Obs>> transposeWUT(Hashtable<Integer, Hashtable<Integer, Obs>> obsTable) {
        Hashtable<Integer, Hashtable<Integer, Obs>> transposed = new Hashtable<>();

        for (Map.Entry<Integer, Hashtable<Integer, Obs>> entry : obsTable.entrySet()) {
            Integer userID = entry.getKey();
            Hashtable<Integer, Obs> dayObstable = entry.getValue();
            for (Map.Entry<Integer, Obs> daysEntry : dayObstable.entrySet()) {
                Integer weekday = daysEntry.getKey();
                Obs obsStr = daysEntry.getValue();

                if (transposed.containsKey(weekday)) {
                    Hashtable<Integer, Obs> usrObstable = transposed.get(weekday);
                    usrObstable.put(userID, obsStr);
                    transposed.replace(weekday, usrObstable);
                } else {
                    Hashtable<Integer, Obs> usrObstable = new Hashtable<>();
                    usrObstable.put(userID, obsStr);
                    transposed.put(weekday, usrObstable);
                }

            }

        }
        return transposed;
    }

    /**
     *
     * Mon 01 Dec 2014 11:37:23 PM EET
     *
     * Write Observation tables
     *
     * @param obsTable
     * @param dstPath
     */
    public void writeObsTU(Hashtable<Integer, Hashtable<Integer, String>> obsTable, String dstPath) {

        try {

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("observation");
            doc.appendChild(rootElement);

            for (Map.Entry<Integer, Hashtable<Integer, String>> entrySet : obsTable.entrySet()) {
                Integer userID = entrySet.getKey();
                Element dayElement = doc.createElement("week_day");
                rootElement.appendChild(dayElement);

                // set attribute to staff element
                Attr attr = doc.createAttribute("id");
                attr.setValue(String.valueOf(userID));
                dayElement.setAttributeNode(attr);
//            System.out.println(pKey);
                Hashtable<Integer, String> value = entrySet.getValue();
                for (Map.Entry<Integer, String> entrySet1 : value.entrySet()) {
                    Integer weekDay = entrySet1.getKey();
                    String obs = entrySet1.getValue();

                    Element usrSeqElement = doc.createElement("user");
                    dayElement.appendChild(usrSeqElement);

                    // set attribute to staff element
                    attr = doc.createAttribute("id");
                    attr.setValue(String.valueOf(weekDay));
                    usrSeqElement.setAttributeNode(attr);

                    attr = doc.createAttribute("seq");
//                    attr.setValue(formatter.format(probability));
                    attr.setValue(obs);
                    usrSeqElement.setAttributeNode(attr);

                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(dstPath));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            System.out.println("File saved!");

        } catch (ParserConfigurationException | TransformerException pce) {
        }
    }
}
