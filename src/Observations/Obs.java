/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Observations;

/**
 *
 * @author essam
 */
public class Obs {
    String seq="";
    String timeStamp="";
    String vitPath="";

    public Obs(String seq, String timeStamp) {
        this.seq = seq;
        this.timeStamp = timeStamp;
    }

    public Obs(String seq, String timeStamp, String vitPath) {
        this.seq = seq;
        this.timeStamp = timeStamp;
        this.vitPath = vitPath;
    }

    
    public String getSeq() {
        return seq;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    
    public String getVitPath() {
        return vitPath;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setVitPath(String vitPath) {
        this.vitPath = vitPath;
    }
    
    
}
