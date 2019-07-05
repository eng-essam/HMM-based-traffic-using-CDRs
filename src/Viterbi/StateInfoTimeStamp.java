package Viterbi;

//This class represents the state information as well as all predecessor states with their trans. prob.                    
public class StateInfoTimeStamp {

    public class ViterbiStateInfo {

        double prob;  // to be removed
        String v_path;
        double v_prob;
        int ts; // to be removed only used for debugging

        public ViterbiStateInfo(double p, String vpa, double vpr) {
            prob = p;
            v_path = vpa;
            v_prob = vpr;
        }

        //For debugging
        public ViterbiStateInfo(double p, String vpa, double vpr, int myts) {
            prob = p;
            v_path = vpa;
            v_prob = vpr;
            ts = myts;
        }

        //Typically used for updating the state
        public void Write(double p, String vpa, double vpr) {
            prob = p;
            v_path = vpa;
            v_prob = vpr;
            ts = -2; //to be removed, for debugging
        }

        //For debugging purposes, to remember the timestamp for the write operation in each ViterbiStateInfo
        public void Write(double p, String vpa, double vpr, int myts) {
            prob = p;
            v_path = vpa;
            v_prob = vpr;
            ts = myts;
        }
    };
    int latest;   // the index of the most recently written ViterbiStateInfo
    int lasttime; // the time at which the last write is done to teh ViterbiStateInfo

    // we keep the last two written ViterbiStateInfo
    // They should be the lasttimestamp and lasttimestamp-1
    ViterbiStateInfo[] mi;

    //can't remember why! ahm
    public TransNode first_node; //first predecessor state transition node (mynode)                                        
    // public TransNode first_node; //first predecessor state transition node (mynode)                                        

    public StateInfoTimeStamp(double p, String vp, double vph, TransNode pn) {
        latest = 0; // most recently written index
        lasttime = -1; // time at last write
        first_node = pn;
        mi = new ViterbiStateInfo[2];
        mi[0] = new ViterbiStateInfo(p, vp, vph);
        mi[1] = new ViterbiStateInfo(0, "", 0);

    }

    //probably we don't need, we can just increment the time by one, or maybe not!

    public ViterbiStateInfo Read(int ts_plus1) {
	// if the timestamp is older than expects then the value is zero
        // ts_plus1 is the timestamp +1
        // if the actual timestamp is less than last time, then read the old value
        //   otherwise read the new value
        //if((ts_plus1)<=lasttime)
        //    return mi[latest^1];
        //else
        //    return mi[latest];

	// what is acceptable is the following:
        // I want to read time stamp n and it exists
        // I want to read time stamp n-1 and it exists
	// 1- lasttime stamp is n, and current readts is n-1
        // 2- lasttime stamp is n, and current readts is n
        //	System.out.println("read from ts = "+lasttime+"at time ="+(ts_plus1-1));
        if (((ts_plus1 - 1) == lasttime) || ((ts_plus1 - 1) == (lasttime - 1))) {
            if ((ts_plus1) <= lasttime) {
                return mi[latest ^ 1];
            } else {
                return mi[latest];
            }
        } else {
            //	    	    System.out.println("reseting...");
            Write(0, "", 0, ts_plus1 - 1);
	    //lasttime = ts_plus1-1;
            //mi[0].Write(0,"",0);
            //mi[1].Write(0,"",0);
            return mi[latest]; //either 0 or 1

        }

    }

    public void Reset(double p, String vp, double vph) {

        latest = 0;
        lasttime = -1;
        mi[0].Write(p, vp, vph);
        mi[1].Write(0, "", 0);
    }

    public boolean Write(double p, String vp, double vph, int ts) {
        // check that we are not writing old values; the ts is the current writing time, and the lasttime is the time where teh last write operation has happened.
        if (ts <= lasttime) {
            System.out.println("State error in StateInfoTimeStamp; write time stamp is " + ts + ", while last written timestamp is" + lasttime);
//	    System.exit(0);
            return false;
        }

        latest = latest ^ 1;
        mi[latest].Write(p, vp, vph, ts); //ts is for debugging only
        if ((lasttime + 1) != ts) {
            mi[latest ^ 1].Write(0, "", 0, ts);
        }

        lasttime = ts; //is this correct? 
        return true;
    }

}
