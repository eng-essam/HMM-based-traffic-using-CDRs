package Viterbi;



//This class represents the state information as well as all predecessor states with their trans. prob.                    
public    class StateInfo {
    public double prob;
    public String v_path;
    public double v_prob;
    public TransNode first_node; //first predecessor state transition node (mynode)                                        

    public StateInfo(double p, String vp, double vph, TransNode pn){
	prob = p;
	v_path = vp;
	v_prob = vph;
	first_node = pn;
    }
}



