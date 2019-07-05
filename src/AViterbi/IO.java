/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AViterbi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.DataHandler;

/**
 *
 * @author essam
 */
public class IO {

    /**
     * Read states 
     * 
     * @param path
     * @return 
     */
    public String[] read_states(String path) {
        List<String> data = readfile(path);
        int nstate = Integer.parseInt(data.get(1));
        String[] states = new String[nstate];
        for (int i = 0; i < nstate; i++) {
            states[i] = data.get(i+3);
        }
        return states;
    }

    public List<String> readfile(String path) {
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

    /**
     *
     * @param states input hidden states
     * @param path input path of the hmm file
     *
     *
     * @param start output the initial probabilities
     * @param trans_p output transition probabilities
     * @param emit_p output emission probabilities
     */
    public void readHMM(String path, double strt[], double trans_p[][], double emit_p[][]) {
        List<String> data = readfile(path);

        int nstates = Integer.parseInt(data.get(1));
        int nobs = Integer.parseInt(data.get(3));

        int initprob_index = data.indexOf("initProbs");
        int transprob_index = data.indexOf("transProbs");
        int emprob_index = data.indexOf("emProbs");

        // Read initial probabilities 
        for (int i = 0; i < nstates; i++) {
            strt[i] = Double.parseDouble(data.get(initprob_index + i + 1));
        }

        // Read transition probabilites
        for (int i = 0; i < nstates; i++) {
            String[] sttrans = data.get(transprob_index + i + 1).split(" ");
            for (int j = 0; j < nstates; j++) {
                double prob = 0;
                if (!sttrans[j].contains("nan")) {
                    prob = Double.parseDouble(sttrans[j]);
                }
                trans_p[i][j] = prob;
            }
        }

        // Read emission probabilities
        for (int i = 0; i < nstates; i++) {
            String[] sttems = data.get(emprob_index + i + 1).split(" ");
            for (int j = 0; j < nobs; j++) {
                double prob = 0;
                if (!sttems[j].contains("nan")) {
                    prob = Double.parseDouble(sttems[j]);
                }
                emit_p[i][j] = prob;
            }
        }
    }

    /**
     *
     * @param states input hidden states
     * @param path input path of the hmm file
     *
     *
     * @param start output the initial probabilities
     * @param trans_p output transition probabilities
     * @param emit_p output emission probabilities
     */
    public void readHMM(String[] states, String path, Hashtable<String, Double> strt, Hashtable<String, Hashtable<String, Double>> trans_p, Hashtable<String, Hashtable<String, Double>> emit_p) {
        List<String> data = readfile(path);

        int nstates = Integer.parseInt(data.get(1));
        int nobs = Integer.parseInt(data.get(3));

        int initprob_index = data.indexOf("initProbs");
        int transprob_index = data.indexOf("transProbs");
        int emprob_index = data.indexOf("emProbs");

        for (int i = 0; i < nstates; i++) {
            strt.put(states[i], Double.parseDouble(data.get(initprob_index + i + 1)));
        }

        for (int i = 0; i < nstates; i++) {
            String[] sttrans = data.get(transprob_index + i + 1).split(" ");
            Hashtable<String, Double> sttrans_table = new Hashtable<>();
            for (int j = 0; j < nstates; j++) {
                double prob = Double.parseDouble(sttrans[j]);
                if (prob == 0) {
                    continue;
                }
                sttrans_table.put(states[j], prob);
            }
            trans_p.put(states[i], sttrans_table);
        }

        for (int i = 0; i < nstates; i++) {
            String[] sttems = data.get(emprob_index + i + 1).split(" ");
            Hashtable<String, Double> emprob_table = new Hashtable<>();
            for (int j = 0; j < nobs; j++) {
                double prob = Double.parseDouble(sttems[j]);
                if (prob == 0) {
                    continue;
                }
                emprob_table.put(String.valueOf(j), prob);
            }
            emit_p.put(states[i], emprob_table);
        }
    }

}
